package com.github.ssalfelder.ocrformmate.controller;

import java.io.File;
import java.io.FileInputStream;
import java.text.Normalizer;
import java.util.Map;

import com.github.ssalfelder.ocrformmate.init.OpenCvLoader;
import com.github.ssalfelder.ocrformmate.service.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.FileChooser;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;


import javax.print.Doc;

@Component
public class OcrController {

    private final OcrResultService ocrResultService;
    private final DocumentAlignmentService documentAlignmentService;
    private final FormFieldService formFieldDetectionService;
    private final AlignmentAnalyzer alignmentAnalyzer;
    private final PdfConverterService pdfConverterService;
    private final ImageScalerService imageScalerService;
    private final OcrService ocrService;

    @Autowired
    public OcrController(OcrResultService ocrResultService,
                         DocumentAlignmentService documentAlignmentService,
                         FormFieldService formFieldDetectionService,
                         AlignmentAnalyzer alignmentAnalyzer,
                         PdfConverterService pdfConverterService,
                         ImageScalerService imageScalerService){
        this.ocrResultService = ocrResultService;
        this.documentAlignmentService = documentAlignmentService;
        this.formFieldDetectionService = formFieldDetectionService;
        this.alignmentAnalyzer = alignmentAnalyzer;
        this.pdfConverterService = pdfConverterService;
        this.imageScalerService = imageScalerService;
        this.ocrService = new OcrService();
    }
    @FXML
    private ComboBox<String> formTypeComboBox;
    @FXML
    private Button ocrButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextArea resultTextArea;
    @FXML
    private ImageView imageView;

    private final String[] FORMTYPE = {"Auto", "Buergergeld", "Anmeldung"};
    private double scale = 1.0;
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;

    @FXML
    private void initialize(){
        formTypeComboBox.getItems().addAll(FORMTYPE);
        formTypeComboBox.getSelectionModel().selectFirst();

        // Zoom via STRG + Mausrad oder Touchpad
        imageView.setOnScroll((ScrollEvent event) -> {
            if (event.isControlDown() || event.isDirect()) { // Touchpad oder STRG
                double delta = event.getDeltaY();
                double zoomFactor = (delta > 0) ? 1.1 : 0.9;
                scale *= zoomFactor;
                imageView.setScaleX(scale);
                imageView.setScaleY(scale);
                event.consume();
            }
        });

        // Drag-to-Pan
        imageView.setOnMousePressed((MouseEvent event) -> {
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            translateAnchorX = imageView.getTranslateX();
            translateAnchorY = imageView.getTranslateY();
        });

        imageView.setOnMouseDragged((MouseEvent event) -> {
            imageView.setTranslateX(translateAnchorX + event.getSceneX() - mouseAnchorX);
            imageView.setTranslateY(translateAnchorY + event.getSceneY() - mouseAnchorY);
        });

        //Zurücksetzen per Doppelklick
        imageView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                scale = 1.0;
                imageView.setScaleX(scale);
                imageView.setScaleY(scale);
                imageView.setTranslateX(0);
                imageView.setTranslateY(0);
            }
        });
    }

    // Diese Methode wird beim Klick auf den Button aufgerufen
    @FXML
    private void onOcrButtonClicked(ActionEvent event) {
        OpenCvLoader.init();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wählen Sie eine PDF- oder Bilddatei mit Formulardaten aus");
        File selectedFile = fileChooser.showOpenDialog(ocrButton.getScene().getWindow());
        String formType = formTypeComboBox.getValue();

        if (selectedFile != null) {
            startOcrTask(selectedFile, formType);
        } else {
            resultTextArea.setText("Keine Datei ausgewählt.");
        }
    }

    public void printImageResolution(Image image) {
        System.out.println("🖼️ Bildgröße:");
        System.out.println("  ➤ Breite:  " + image.getWidth());
        System.out.println("  ➤ Höhe:    " + image.getHeight());
    }

    private void startOcrTask(File selectedFile, String formType) {
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS); // animierter Balken

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                runOcrWorkflow(selectedFile, formType); // das ist deine OCR-Logik
                return null;
            }

            @Override
            protected void succeeded() {
                progressBar.setVisible(false);
            }

            @Override
            protected void failed() {
                progressBar.setVisible(false);
            }
        };

        new Thread(task).start();
    }

    private void runOcrWorkflow(File selectedFile, String formType) throws Exception {

        String inputPath = selectedFile.getAbsolutePath();
        String tempPngPath = "converted_from_pdf.png";
        String normalizedInputPath = "normalized_input.png";

        if (inputPath.toLowerCase().endsWith(".pdf")) {
            inputPath = pdfConverterService.convertPdfToPng(inputPath, tempPngPath, 300);
        }

        inputPath = imageScalerService.scaleToTargetSize(inputPath, normalizedInputPath);

        String alignedPath = "aligned_temp.png";
        boolean success = documentAlignmentService.alignDocument(inputPath, alignedPath);

        if (!success) {
            Platform.runLater(() -> resultTextArea.setText("Fehler bei der Dokument-Ausrichtung."));
            return;
        }

        Mat alignedMat = imread(alignedPath);
        alignmentAnalyzer.analyzeLines(imread(inputPath), "Original");
        alignmentAnalyzer.analyzeLines(alignedMat, "Nach Ausrichtung");

        Mat withFieldsMarked = formFieldDetectionService.overlayTemplateFields(alignedMat, formType);
        imwrite("form_preview.png", withFieldsMarked);

        Image image = new Image(new FileInputStream("form_preview.png"));

        Map<String, Rect> fieldMap = formFieldDetectionService.getTemplateFields(formType);
        Map<String, String> recognizedFields = ocrService.recognizeFields(new File(alignedPath), fieldMap, formType);

        StringBuilder resultBuilder = new StringBuilder();
        recognizedFields.forEach((field, text) -> resultBuilder.append(field).append(": ").append(text).append("\n"));

        Platform.runLater(() -> {
            imageView.setImage(image);
            resultTextArea.setText(resultBuilder.toString());
            ocrResultService.saveOcrResult(resultBuilder.toString());
            printImageResolution(image);
        });
    }

}
