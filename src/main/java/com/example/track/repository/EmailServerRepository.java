package com.example.track.repository;


import com.example.track.entity.EmailAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailServerRepository extends JpaRepository<EmailAccount,String> {
}
