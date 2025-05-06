package com.github.ssalfelder.ocrformmate.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.github.ssalfelder.ocrformmate.init.OpenCvLoader;
import com.github.ssalfelder.ocrformmate.service.*;
import com.github.ssalfelder.ocrformmate.session.OcrSessionHolder;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Text;
import org.fxmisc.richtext.StyleClassedTextArea;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

import javafx.util.Duration;


@Component
public class OcrController {


    private final DocumentAlignmentService documentAlignmentService;
    private final FormFieldService formFieldDetectionService;
    private final AlignmentAnalyzer alignmentAnalyzer;
    private final PdfConverterService pdfConverterService;
    private final ImageScalerService imageScalerService;
    private final OcrService ocrService;
    private final ImageDisplayService imageDisplayService;
    private final PdfFormFillerService pdfFormFiller;
    private final ResourceLoader resourceLoader;

    @Autowired
    public OcrController(DocumentAlignmentService documentAlignmentService,
                         FormFieldService formFieldDetectionService,
                         AlignmentAnalyzer alignmentAnalyzer,
                         PdfConverterService pdfConverterService,
                         ImageScalerService imageScalerService,
                         OcrService ocrservice,
                         ImageDisplayService imageDisplayService,
                         PdfFormFillerService pdfFormFiller,
                         ResourceLoader resourceLoader) {
        this.documentAlignmentService = documentAlignmentService;
        this.formFieldDetectionService = formFieldDetectionService;
        this.alignmentAnalyzer = alignmentAnalyzer;
        this.pdfConverterService = pdfConverterService;
        this.imageScalerService = imageScalerService;
        this.ocrService = ocrservice;
        this.imageDisplayService = imageDisplayService;
        this.pdfFormFiller = pdfFormFiller;
        this.resourceLoader = resourceLoader;
    }

    @FXML
    private ComboBox<String> formTypeComboBox;
    @FXML
    private Button ocrButton;
    @FXML
    private Button transferButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ImageView imageView;
    @FXML
    private WebView pdfWebView;
    @FXML
    private ScrollPane imageScrollPane;
    @FXML
    private StyleClassedTextArea styledArea;

    private Map<String,String> lastRecognized;
    private final String[] FORMTYPE = {"Buergergeld", "Anmeldung"};
    private double scale = 1.0;
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;
    private File selectedFile = null;
    private  CitizenController citizenController;
    private Timeline progressTimeline;
    private final javafx.scene.text.Font ocrFont = javafx.scene.text.Font.loadFont(
            getClass().getResourceAsStream("/fonts/Roboto-Regular.ttf"), 14
    );


    public  void setCitizenController(CitizenController citizenController) {
        this.citizenController = citizenController;
    }

    @FXML
    private void initialize() {
        formTypeComboBox.getItems().addAll(FORMTYPE);
        formTypeComboBox.getSelectionModel().selectFirst();

        formTypeComboBox.setOnAction(event -> {
            String selectedType = formTypeComboBox.getSelectionModel().getSelectedItem();
            citizenController.updateAuthorityBasedOnFormType(selectedType);
        });

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
                showMessageInStyledArea("Ungültiges Format. Bitte wählen Sie PNG, JPG oder PDF.", "styled-error");
            } else {
                showImageWithoutOcr(selectedFile);
            }
        }


    }

    @FXML
    private void onOcrButtonClicked(ActionEvent event) {
        OpenCvLoader.init();
        String formType = formTypeComboBox.getValue();

        if (selectedFile != null) {
            startOcrTask(selectedFile, formType);
        } else {
            showMessageInStyledArea("Keine Datei ausgewählt.", "styled-default");
        }
    }

    public void printImageResolution(Image image) {
        System.out.println("Bildgröße:");
        System.out.println("  ➤ Breite:  " + image.getWidth());
        System.out.println("  ➤ Höhe:    " + image.getHeight());
    }

    private void startOcrTask(File selectedFile, String formType) {
        progressBar.setVisible(true);
        progressTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(2), new KeyValue(progressBar.progressProperty(), 1))
        );
        progressTimeline.setCycleCount(Animation.INDEFINITE);
        progressTimeline.play();


        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                runOcrWorkflow(selectedFile, formType);
                return null;
            }

            @Override
            protected void succeeded() {
                if (progressTimeline != null) {
                    progressTimeline.stop();
                }
                progressBar.setVisible(false);
                progressBar.setProgress(0);
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
                showMessageInStyledArea("Fehler bei der Dokument-Ausrichtung.", "styled-error");
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
            lastRecognized = recognizedFields;

            OcrSessionHolder.set(builder.toString());

            printImageResolution(image);
            citizenController.enableSubmitIfOcrAvailable();
        });

    }

    private void showImageWithoutOcr(File file) {
        try {
            String inputPath = file.getAbsolutePath();
            String outputPngPath = "preview_from_pdf.png";

            if (inputPath.toLowerCase().endsWith(".pdf")) {
                inputPath = pdfConverterService.convertPdfToPng(inputPath, outputPngPath, 300);
            } else {
                boolean success = imageDisplayService.removeAlphaChannel(inputPath, outputPngPath);
                if (!success) {
                    Platform.runLater(() -> {
                        showMessageInStyledArea("Fehler beim Anzeigen der Bilddatei.", "styled-error");
                    });
                }
            }

            Image image = new Image(new FileInputStream(outputPngPath));
            System.out.printf("Bildgröße (Original): %.1f x %.1f px%n", image.getWidth(), image.getHeight());

            Platform.runLater(() -> {
                imageView.setImage(image);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(false);

                showMessageInStyledArea("Bild geladen. OCR kann jetzt gestartet werden.", "styled-info");

                String selectedFormType = formTypeComboBox.getSelectionModel().getSelectedItem();
                if ("Buergergeld".equalsIgnoreCase(selectedFormType)) {
                    loadPdfViaPdfJs("Buergergeld.pdf");
                } else {
                    loadPdfViaPdfJs("Anmeldeformular_BMG.pdf");
                }



                Stage stage = (Stage) imageView.getScene().getWindow();
                stage.setWidth(1200);
                stage.setHeight(800);
                stage.centerOnScreen();

                PauseTransition pause = new PauseTransition(Duration.millis(100));
                pause.setOnFinished(e -> {
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
                showMessageInStyledArea("Fehler beim Anzeigen der Bilddatei.", "styled-error");
            });
        }
    }

    private void showFormattedOcrResult(Map<String, String> ocrResult) {
        styledArea.clear();
        final double[] maxWidth = {0};

        ocrResult.forEach((key, value) -> {
            String line = "[" + key + "]: " + value;
            int start = styledArea.getLength();
            styledArea.appendText(line + "\n");

            styledArea.setStyleClass(start, start + key.length() + 2, "field-name");
            styledArea.setStyleClass(start + key.length() + 2, styledArea.getLength(), "field-value");

            double lineWidth = estimateTextWidth(line);
            if (lineWidth > maxWidth[0]) {
                maxWidth[0] = lineWidth;
            }
        });

        styledArea.setPrefWidth(maxWidth[0] + 50);
    }

    @FXML
    protected void onTransferClicked(ActionEvent event) {
        try {
            Resource pdfRes = resourceLoader.getResource("classpath:static/pdf/Hauptantrag_Buergergeld.pdf");
            File template   = pdfRes.getFile();
            File outFile    = new File("output/Buergergeld_ausgefuellt.pdf");
            File parent = outFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            pdfFormFiller.fillForm(template, outFile, lastRecognized);
            pdfWebView.getEngine().load(outFile.toURI().toString());
        } catch (IOException ex) {
            ex.printStackTrace();
            DialogHelper.showError("Fehler", "Formular konnte nicht befüllt werden.");
        }
    }


    private void showMessageInStyledArea(String message, String styleClass) {
        styledArea.clear();
        styledArea.appendText(message);
        styledArea.setStyleClass(0, message.length(), styleClass);
    }

    public void syncFormTypeWithCitizenController() {
        String initialType = formTypeComboBox.getSelectionModel().getSelectedItem();
        citizenController.updateAuthorityBasedOnFormType(initialType);
    }

    private double estimateTextWidth(String text) {
        Text helper = new Text(text);
        helper.setFont(ocrFont);

        new Scene(new Group(helper));
        helper.applyCss();

        return helper.getLayoutBounds().getWidth();
    }

    private void loadPdfViaPdfJs(String pdfResourcePath) {
        String viewer = "http://localhost:8080/pdfjs/web/viewer.html";
        String pdf         = "/pdf/" + pdfResourcePath;
        String fullUrl = viewer
                + "?file=" + URLEncoder.encode(pdf, StandardCharsets.UTF_8)
                + "#page=1&zoom=page-width";
        System.out.println("[DEBUG] fullUrl = " + fullUrl);
        pdfWebView.getEngine().load(fullUrl);
    }
}
