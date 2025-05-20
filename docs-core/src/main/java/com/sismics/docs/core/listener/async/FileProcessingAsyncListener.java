package com.sismics.docs.core.listener.async;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.FileDao;
import com.sismics.docs.core.dao.FilePathDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.event.FileCreatedAsyncEvent;
import com.sismics.docs.core.event.FileEvent;
import com.sismics.docs.core.event.FileUpdatedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.FilePath;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.docs.core.util.format.FormatHandler;
import com.sismics.docs.core.util.format.FormatHandlerUtil;
import com.sismics.util.ImageUtil;
import com.sismics.util.Scalr;

/**
 * Listener on file processing.
 * 
 * @author bgamard
 */
public class FileProcessingAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileProcessingAsyncListener.class);

    /**
     * File created.
     *
     * @param event File created event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void on(final FileCreatedAsyncEvent event) {
        if (log.isInfoEnabled()) {
            log.info("File created event: " + event.toString());
        }

        processFile(event, true);
    }

    /**
     * File updated.
     *
     * @param event File updated event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void on(final FileUpdatedAsyncEvent event) {
        log.info("File updated event: " + event.toString());

        processFile(event, false);
    }

    /**
     * Process a file :
     * Generate thumbnails
     * Extract and save text content
     *
     * @param event File event
     * @param isFileCreated True if the file was just created
     */
    private void processFile(FileEvent event, boolean isFileCreated) {
        AtomicReference<com.sismics.docs.core.model.jpa.File> file = new AtomicReference<>();
        AtomicReference<User> user = new AtomicReference<>();

        // Open a first transaction to get what we need to start the processing
        TransactionUtil.handle(() -> {
            // Generate thumbnail, extract content
            file.set(new FileDao().getActiveById(event.getFileId()));
            if (file.get() == null) {
                // The file has been deleted since
                return;
            }

            // Get the creating user from the database for its private key
            UserDao userDao = new UserDao();
            user.set(userDao.getById(file.get().getUserId()));
        });

        // Process the file outside of a transaction
        if (user.get() == null || file.get() == null) {
            // The user or file has been deleted
            FileUtil.endProcessingFile(event.getFileId());
            return;
        }
        
        FileCopyAndPush(event);
        String content = extractContent(event, user.get(), file.get());

        // Open a new transaction to save the file content
        TransactionUtil.handle(() -> {
            // Save the file to database
            FileDao fileDao = new FileDao();
            com.sismics.docs.core.model.jpa.File freshFile = fileDao.getActiveById(event.getFileId());
            if (freshFile == null) {
                // The file has been deleted since the text extraction started, ignore the result
                return;
            }

            freshFile.setContent(content);
            fileDao.update(freshFile);

            // Update index with the updated file
            if (isFileCreated) {
                AppContext.getInstance().getIndexingHandler().createFile(freshFile);
            } else {
                AppContext.getInstance().getIndexingHandler().updateFile(freshFile);
            }
        });

        FileUtil.endProcessingFile(event.getFileId());
    }

    /**
     * Extract text content from a file.
     * This is executed outside of a transaction.
     *
     * @param event File event
     * @param user User whom created the file
     * @param file Fresh file
     * @return Text content
     */
    private String extractContent(FileEvent event, User user, com.sismics.docs.core.model.jpa.File file) {
        // Find a format handler
        FormatHandler formatHandler = FormatHandlerUtil.find(file.getMimeType());
        if (formatHandler == null) {
            log.info("Format unhandled: " + file.getMimeType());
            return null;
        }

        // Generate file variations
        try {
            Cipher cipher = EncryptionUtil.getEncryptionCipher(user.getPrivateKey());
            BufferedImage image = formatHandler.generateThumbnail(event.getUnencryptedFile());
            if (image != null) {
                // Generate thumbnails from image
                BufferedImage web = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, 1280);
                BufferedImage thumbnail = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, 256);
                image.flush();

                // Write "web" encrypted image
                Path outputFile = DirectoryUtil.getStorageDirectory().resolve(file.getId() + "_web");
                try (OutputStream outputStream = new CipherOutputStream(Files.newOutputStream(outputFile), cipher)) {
                    ImageUtil.writeJpeg(web, outputStream);
                }

                // Write "thumb" encrypted image
                outputFile = DirectoryUtil.getStorageDirectory().resolve(file.getId() + "_thumb");
                try (OutputStream outputStream = new CipherOutputStream(Files.newOutputStream(outputFile), cipher)) {
                    ImageUtil.writeJpeg(thumbnail, outputStream);
                }
            }
        } catch (Throwable e) {
            log.error("Unable to generate thumbnails for: " + file, e);
        }

        // Extract text content from the file
        long startTime = System.currentTimeMillis();
        String content = null;
        log.info("Start extracting content from: " + file);
        try {
            content = formatHandler.extractContent(event.getLanguage(), event.getUnencryptedFile());
        } catch (Throwable e) {
            log.error("Error extracting content from: " + file, e);
        }
        log.info(MessageFormat.format("File content extracted in {0}ms: " + file.getId(), System.currentTimeMillis() - startTime));

        return content;
    }
    private void FileCopyAndPush(FileEvent event){
        java.io.File repoDir = new java.io.File("/home/y/Desktop/Sustech/Software_Engineering/code/Teedy");
        java.io.File sourceFile = new java.io.File(event.getUnencryptedFile().toString());
        java.io.File destFile = new java.io.File(repoDir, "tmp/" + sourceFile.getName());

        try{
            // 复制文件
            Files.copy(sourceFile.toPath(), destFile.toPath());
            Git git = Git.open(repoDir);
            git.add().addFilepattern("tmp/" + sourceFile.getName()).call();
            git.commit().setMessage("Add file: " + sourceFile.getName()).call();
            git.push()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("Y-1huadb", System.getenv("GITHUB_TOKEN")))
                .call();

            FilePathDao filePathDao = new FilePathDao();
            FilePath filePath = new FilePath();
            filePath.setId(event.getFileId());
            filePath.setPath("https://raw.githubusercontent.com/Y-1huadb/Teedy/refs/heads/master/"+"tmp/" + sourceFile.getName());
            filePathDao.create(filePath);

        }catch (Exception e){ 
            e.printStackTrace();
        }
    }
}
