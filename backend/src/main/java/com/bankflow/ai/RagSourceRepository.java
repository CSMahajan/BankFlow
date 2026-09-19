package com.bankflow.ai;

import com.bankflow.entity.RagSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RagSourceRepository extends JpaRepository<RagSource, Long> {

    Optional<RagSource> findBySourcePath(String sourcePath);
}