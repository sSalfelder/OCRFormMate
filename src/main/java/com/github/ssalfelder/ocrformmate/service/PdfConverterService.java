package com.github.ssalfelder.ocrformmate.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfConverterService {

    public String convertPdfToPng(String pdfPath, String outputPath, int dpi) throws Exception {
        Path pdfFilePath = Paths.get(pdfPath);
        try (PDDocument document = PDDocument.load((InputStream) pdfFilePath)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, dpi); // Seite 0
            ImageIO.write(image, "png", new File(outputPath));
        }
        return outputPath;
    }
}
