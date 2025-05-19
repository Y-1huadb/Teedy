package com.sismics.docs.core.dao;

import java.util.Date;
import java.util.List;

import com.sismics.docs.core.model.jpa.RegisterRequest;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

public class RegisterRequestDao {

    /**
     * Creates a new registration request.
     *
     * @param registerRequest The RegisterRequest entity with username and password set.
     * @return The ID of the newly created registration request.
     * @throws Exception if a registration request for the same username already exists.
     */
    public String create(RegisterRequest registerRequest) throws Exception {
        // 获取当前线程中的 EntityManager
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // 检查是否已有相同用户名的注册请求，防止重复提交申请
        Query q = em.createQuery("select rr from RegisterRequest rr where rr.username = :username");
        q.setParameter("username", registerRequest.getUsername());
        List<?> existing = q.getResultList();
        if (!existing.isEmpty()) {
            throw new Exception("AlreadyExistingRegisterRequest");
        }
        
        // 设置注册请求日期和初始状态
        registerRequest.setRequestDate(new Date());
        registerRequest.setStatus("pending"); // pending, approved, rejected 等
        
        // 持久化注册请求实体
        em.persist(registerRequest);
        
        return registerRequest.getUsername();
    }

    /**
     * 根据用户名查找注册请求
     *
     * @param username 用户名（同时也是主键）
     * @return 对应的 RegisterRequest 实体，如果不存在则返回 null
     */
    public RegisterRequest findByUsername(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(RegisterRequest.class, username);
    }

    /**
     * 查询所有的注册请求
     *
     * @return 注册请求列表
     */
    public List<RegisterRequest> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            TypedQuery<RegisterRequest> q = em.createQuery("SELECT rr FROM RegisterRequest rr", RegisterRequest.class);
            return q.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * 删除指定的注册请求
     *
     * @param registerRequest 要删除的 RegisterRequest 实体
     */
    public void delete(RegisterRequest registerRequest) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        RegisterRequest rr = em.find(RegisterRequest.class, registerRequest.getUsername());
        if (rr != null) {
            em.remove(rr);
        }
    }
}