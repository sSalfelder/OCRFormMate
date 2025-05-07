package com.github.ssalfelder.ocrformmate.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/filled")
public class PdfServeController {

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getPdf(@PathVariable String filename) {
        File pdfFile = new File("output/pdf/" + filename);

        if (!pdfFile.exists()) {
            return ResponseEntity.notFound().build();
        }


        FileSystemResource resource = new FileSystemResource(pdfFile);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
