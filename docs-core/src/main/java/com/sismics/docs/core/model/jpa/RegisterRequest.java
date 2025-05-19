package com.sismics.docs.core.model.jpa;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_REGISTER_REQUEST")
public class RegisterRequest {

    @Id
    @Column(name = "USERNAME_C", length = 100, nullable = false)
    private String username;

    @Column(name = "PASSWORD_C", length = 100, nullable = false)
    private String password;

    @Column(name = "EMAIL_C", length = 100, nullable = false)
    private String email;

    @Column(name = "REQUEST_DATE_D", nullable = false)
    private Date requestDate;

    @Column(name = "STATUS_C", length = 50, nullable = false)
    private String status;

    // Getter and Setter for username (as primary key)
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }    

    // Getter and Setter for password
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }  
    
    // Getter and Setter for requestDate
    public Date getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }
    
    // Getter and Setter for status
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}