package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.FilePath;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;

public class FilePathDao {
    public String create(FilePath filePath) throws Exception {
        // 获取当前线程中的 EntityManager
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // 持久化注册请求实体
        em.persist(filePath);
        
        return filePath.getId();
    }

    public FilePath findById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(FilePath.class, id);
    }
    
}
