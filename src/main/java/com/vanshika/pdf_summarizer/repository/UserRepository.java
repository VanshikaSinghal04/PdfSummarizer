package com.vanshika.pdf_summarizer.repository;

import com.vanshika.pdf_summarizer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {//this interface extends JpaRepository, which provides basic CRUD(crud stands for create, read, update, delete) operations for the User entity. It also defines custom methods for finding users by their Google ID or email.
    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByEmail(String email);
}