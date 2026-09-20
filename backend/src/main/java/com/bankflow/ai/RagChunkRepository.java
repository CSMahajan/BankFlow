package com.bankflow.ai;

import com.bankflow.entity.RagChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RagChunkRepository
        extends JpaRepository<RagChunkEntity, Long> {

    List<RagChunkEntity> findBySourceIdOrderByChunkIndex(Long sourceId);

    void deleteBySourceId(Long sourceId);

    @Query(value = """
            SELECT rc.*
            FROM retail_banking.rag_chunks rc
            WHERE rc.audience IN (:audiences)
              AND rc.embedding IS NOT NULL
            ORDER BY rc.embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<RagChunkEntity> findNearestChunks(
            @Param("embedding") String embedding,
            @Param("audiences") List<String> audiences,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT
                rc.id AS id,
                rs.source_path AS sourcePath,
                rs.source_type AS sourceType,
                rc.section AS section,
                rc.content AS content,
                rc.audience AS audience,
                rc.embedding <=> CAST(:embedding AS vector) AS distance
            FROM retail_banking.rag_chunks rc
            JOIN retail_banking.rag_sources rs
                ON rs.id = rc.source_id
            WHERE rc.audience IN (:audiences)
              AND rc.embedding IS NOT NULL
            ORDER BY rc.embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<RagChunkSearchResult> findNearestChunksWithDistance(
            @Param("embedding") String embedding,
            @Param("audiences") List<String> audiences,
            @Param("limit") int limit
    );
}
