package com.pepper.backend.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="userId", updatable = false, nullable = false)
    private long userId;
    @Size(min = 3, max = 30)
    @NotBlank(message = "username is mandatory")
    private String username;
    @Size(min = 8, max = 30)
    @NotBlank(message = "password is mandatory")
    private String password;
    @Email(message = "email is mandatory")
    private String email;
    @ManyToOne
    @JoinColumn(name="roleId", nullable=false)
    private Role role;

    @OneToMany(mappedBy="user")
    private Set<Post> posts;

    @OneToMany(mappedBy="user")
    private Set<Comment> UserComments;

    private String createdBy = "site";
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority(getRole().getRoleName()));
    }
}

// TODO Admin Create Admin
// TODO Admin Site -> Delete User, Ban User, Delete Post, Delete Comment,
// TODO VOTESYSTEM
// TODO PICTRUES for users and posts,
// TODO USER PROFILE