package com.eureka.challenge.repository;

import com.eureka.challenge.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByEmail(String email);
    List<Client> findByApiKey(String apiKey);
}
