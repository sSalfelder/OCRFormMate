package com.github.ssalfelder.ocrformmate.controller;

import java.io.File;
import java.io.FileInputStream;
import java.text.Normalizer;
import java.util.Map;

import com.github.ssalfelder.ocrformmate.init.OpenCvLoader;
import com.github.ssalfelder.ocrformmate.service.*;
import com.github.ssalfelder.ocrformmate.session.OcrSessionHolder;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import javafx.animation.PauseTransition;
import javafx.util.Duration;



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
    private final CitizenController citizenController;
    private final ImageDisplayService imageDisplayService;

    @Autowired
    public OcrController(OcrResultService ocrResultService,
                         DocumentAlignmentService documentAlignmentService,
                         FormFieldService formFieldDetectionService,
                         AlignmentAnalyzer alignmentAnalyzer,
                         PdfConverterService pdfConverterService,
                         ImageScalerService imageScalerService,
                         OcrService ocrservice,
                         CitizenController citizenController,
                         ImageDisplayService imageDisplayService) {
        this.ocrResultService = ocrResultService;
        this.documentAlignmentService = documentAlignmentService;
        this.formFieldDetectionService = formFieldDetectionService;
        this.alignmentAnalyzer = alignmentAnalyzer;
        this.pdfConverterService = pdfConverterService;
        this.imageScalerService = imageScalerService;
        this.citizenController = citizenController;
        this.ocrService = ocrservice;
        this.imageDisplayService = imageDisplayService;
    }

    @FXML
    private ComboBox<String> formTypeComboBox;
    @FXML
    private Button ocrButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextFlow resultTextFlow;
    @FXML
    private ImageView imageView;
    @FXML
    private WebView pdfWebView;
    @FXML
    private ScrollPane imageScrollPane;

    private final String[] FORMTYPE = {"Buergergeld", "Anmeldung"};
    private double scale = 1.0;
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;
    private File selectedFile = null;

    @FXML
    private void initialize() {
        formTypeComboBox.getItems().addAll(FORMTYPE);
        formTypeComboBox.getSelectionModel().selectFirst();

        Platform.runLater(() -> {
            Stage stage = (Stage) imageView.getScene().getWindow();
            stage.setWidth(900);
            stage.setHeight(600);
            stage.centerOnScreen();
        });

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

    @FXML
    private void onfileOpenerClicked(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wählen Sie eine PDF- oder Bilddatei mit Formulardaten aus");
        selectedFile = fileChooser.showOpenDialog(ocrButton.getScene().getWindow());

        if (selectedFile != null) {
            String name = selectedFile.getName().toLowerCase();
            if (!(name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".pdf"))) {
                showMessageInTextFlow("Ungültiges Format. Bitte wählen Sie PNG, JPG oder PDF.", "red");
            } else {
                showImageWithoutOcr(selectedFile);
            }
        }


    }

    // Diese Methode wird beim Klick auf den Button aufgerufen
    @FXML
    private void onOcrButtonClicked(ActionEvent event) {
        OpenCvLoader.init();
        String formType = formTypeComboBox.getValue();

        if (selectedFile != null) {
            startOcrTask(selectedFile, formType);
        } else {
            showMessageInTextFlow("Keine Datei ausgewählt.", "black");
        }
    }

    public void printImageResolution(Image image) {
        System.out.println("Bildgröße:");
        System.out.println("  ➤ Breite:  " + image.getWidth());
        System.out.println("  ➤ Höhe:    " + image.getHeight());
    }

    private void startOcrTask(File selectedFile, String formType) {
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS); // animierter Balken

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                runOcrWorkflow(selectedFile, formType);
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
            Platform.runLater(() -> {
                showMessageInTextFlow("Fehler bei der Dokument-Ausrichtung.", "red");
            });
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

        Platform.runLater(() -> {
            imageView.setImage(image);

            showFormattedOcrResult(recognizedFields);

            DialogHelper.showInfo(
                    "Bitte überprüfen Sie Ihre Angaben",
                    "Kontrollieren Sie die automatisch erkannten Daten.\n\n"
                            + "Sie können alle Werte direkt im Textfeld korrigieren.\n"
                            + "Erst nach Ihrer Bestätigung werden die Angaben in das PDF-Formular übernommen."
            );

            StringBuilder builder = new StringBuilder();
            recognizedFields.forEach((k, v) -> builder.append("[").append(k).append("]: ").append(v).append("\n"));

            OcrSessionHolder.set(builder.toString());
            ocrResultService.saveOcrResult(builder.toString());

            printImageResolution(image);
            citizenController.enableSubmitIfOcrAvailable();
        });

    }

    private void showImageWithoutOcr(File file) {
        try {
            String inputPath = file.getAbsolutePath();
            String outputPngPath = "preview_from_pdf.png";

            // 1. Vorverarbeitung
            if (inputPath.toLowerCase().endsWith(".pdf")) {
                inputPath = pdfConverterService.convertPdfToPng(inputPath, outputPngPath, 300);
            } else {
                boolean success = imageDisplayService.removeAlphaChannel(inputPath, outputPngPath);
                if (!success) {
                    Platform.runLater(() -> {
                        showMessageInTextFlow("Fehler beim Anzeigen der Bilddatei.", "red");
                    });
                }
            }

            // 2. Bild laden (volle Qualität)
            Image image = new Image(new FileInputStream(outputPngPath));
            System.out.printf("Bildgröße (Original): %.1f x %.1f px%n", image.getWidth(), image.getHeight());

            Platform.runLater(() -> {
                imageView.setImage(image);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(false);

                showMessageInTextFlow("Bild geladen. OCR kann jetzt gestartet werden.", "green");

                // 3. Fenster ggf. vergrößern + zentrieren
                Stage stage = (Stage) imageView.getScene().getWindow();
                stage.setWidth(1200);
                stage.setHeight(800);
                stage.centerOnScreen();

                // 4. Nach Layout-Pass: Zoom automatisch berechnen
                PauseTransition pause = new PauseTransition(Duration.millis(100));
                pause.setOnFinished(e -> {
                    // Nutze das umschließende StackPane als Referenz
                    if (imageView.getParent() != null) {
                        double containerWidth = imageScrollPane.getViewportBounds().getWidth();
                        double containerHeight = imageScrollPane.getViewportBounds().getHeight();

                        double widthRatio = containerWidth / image.getWidth();
                        double heightRatio = containerHeight / image.getHeight();
                        scale = Math.min(widthRatio, heightRatio) * 1;

                        imageView.setScaleX(scale);
                        imageView.setScaleY(scale);
                        imageView.setTranslateX(0);
                        imageView.setTranslateY(0);

                        System.out.printf("Zoom gesetzt auf: %.3f (Container: %.0f x %.0f)%n",
                                scale, containerWidth, containerHeight);
                    }
                });
                pause.play();
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                showMessageInTextFlow("Fehler beim Anzeigen der Bilddatei.", "red");
            });
        }
    }

    private void showFormattedOcrResult(Map<String, String> ocrResult) {
        resultTextFlow.getChildren().clear();

        ocrResult.forEach((key, value) -> {
            Text field = new Text("[" + key + "]: ");
            field.setStyle("-fx-fill: #2b4f81; -fx-font-weight: bold;");

            Text val = new Text(value + "\n");
            val.setStyle("-fx-fill: black;");

            resultTextFlow.getChildren().addAll(field, val);
        });
    }

    private void showMessageInTextFlow(String message, String color) {
        resultTextFlow.getChildren().clear();
        Text msg = new Text(message);
        msg.setStyle("-fx-fill: " + color + "; -fx-font-weight: bold;");
        resultTextFlow.getChildren().add(msg);
    }
}