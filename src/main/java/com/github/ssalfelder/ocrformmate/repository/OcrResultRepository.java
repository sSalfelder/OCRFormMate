package com.github.ssalfelder.ocrformmate.repository;

import com.github.ssalfelder.ocrformmate.model.OcrResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OcrResultRepository extends JpaRepository<OcrResult, Long> {
    List<OcrResult> findByAuthority(String authority);

    List<OcrResult> findByUserIdAndAuthority(Integer userId, String authority); // optional
}
