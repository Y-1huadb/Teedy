package com.sismics.docs.core.service;

import com.sismics.docs.BaseTransactionalTest;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

public class TestFileService extends BaseTransactionalTest{
    @Test
    public void createTemporaryFileTest() throws Exception {
        FileService fileService = new FileService();
        Path path = fileService.createTemporaryFile("test.txt");
        Assert.assertTrue(Files.exists(path));
    }
}
