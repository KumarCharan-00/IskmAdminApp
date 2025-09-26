package org.iskm.admin.web.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor // for jpa it needs
@AllArgsConstructor
@Table(name = "users")  
public class User {

    @Id
    private String userId;

    @Column(nullable=false, unique=true)
    private String username;

    @Column(nullable=false,length = 512)
    private String password;
    private String creationTime;
}