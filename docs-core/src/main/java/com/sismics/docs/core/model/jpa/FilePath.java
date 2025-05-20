package com.sismics.docs.core.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_FILE_PATH")
public class FilePath {
    @Id
    @Column(name = "FIL_ID_C", length = 36)
    private String id;

    @Column(name = "PATH_C", length = 100, nullable = false)
    private String path;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}