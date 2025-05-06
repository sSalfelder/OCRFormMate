# Java-Klassen
**src\main\java\com\github\ssalfelder\ocrformmate\OcrFormMateApp.java**

```
package com.github.ssalfelder.ocrformmate;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

@SpringBootApplication
@ComponentScan(basePackages = "com.github.ssalfelder.ocrformmate")
public class OcrFormMateApp extends Application {

    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {
        springContext = SpringApplication.run(OcrFormMateApp.class);

    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                OcrFormMateApp.class.getResource("/com/github/ssalfelder/ocrformmate/fxml/main.fxml"));

        loader.setControllerFactory(springContext::getBean);

        Parent root = loader.load();
        Scene scene = new Scene(root, 650, 151);
        stage.setTitle("OCRFormMate");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        springContext.close();
        Platform.exit();
    }

    public static ConfigurableApplicationContext getContext() {
        return springContext;
    }

    public static void main(String[] args) {
        // JavaFX-Start
        launch();
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\auth\CitizenSessionHolder.java**

```
```

**src\main\java\com\github\ssalfelder\ocrformmate\auth\ClerkSessionHolder.java**

```
package com.github.ssalfelder.ocrformmate.auth;

import com.github.ssalfelder.ocrformmate.model.Clerk;

public class ClerkSessionHolder {
    private static Clerk loggedInClerk;

    public static void setLoggedInClerk(Clerk clerk) {
        ClerkSessionHolder.loggedInClerk = clerk;
    }

    public static Clerk getLoggedInClerk() {
        return loggedInClerk;
    }

    public static void clear() {
        loggedInClerk = null;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\controller\AddressController.java**

```
package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.model.Address;
import com.github.ssalfelder.ocrformmate.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressRepository addressRepository;

    @GetMapping
    public Iterable<Address> getAll() {
        return addressRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Address> create(@RequestBody Address address) {
        Address saved = addressRepository.save(address);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\controller\AdminController.java**

```
package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.model.Admin;
import com.github.ssalfelder.ocrformmate.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @PostMapping("/admin/register")
    private ResponseEntity<Admin> register (@RequestBody Admin newAdmin) {
        //generate secret
        newAdmin.setSecret(UUID.randomUUID().toString());

        var savedAdmin = adminRepository.save(newAdmin);
        return new ResponseEntity<Admin>(savedAdmin, HttpStatus.CREATED);
    }

}
```

**src\main\java\com\github\ssalfelder\ocrformmate\controller\CitizenController.java**

```
package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import com.github.ssalfelder.ocrformmate.auth.CitizenSessionHolder;
import com.github.ssalfelder.ocrformmate.model.User;
import com.github.ssalfelder.ocrformmate.service.CitizenService;
import com.github.ssalfelder.ocrformmate.service.OcrAssignmentService;
import com.github.ssalfelder.ocrformmate.service.OcrResultService;
import com.github.ssalfelder.ocrformmate.session.OcrSessionHolder;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

import java.io.IOException;

@Component
public class CitizenController {

    //TODO Ausgabefenster mit dem zu beschreibenden Formular
    //TODO eingereicht am Zeitstempel auch im DialogHelper Fenster
    //TODO mehrseitiges PDF (ganzer Bürgergeldantrag)

    //TODO Clerkseite einrichten + zusätzliche Features
    //TODO Neue Generierung zu Vornamen und neue Datensätze
    @Autowired
    private OcrAssignmentService ocrAssignmentService;

    @FXML
    private ComboBox<String> citizenAuthorityChooser;

    @FXML
    private Label loginStatusLabel;

    @FXML
    private Button citizenOCRSubmitButton;

    @Autowired
    private CitizenService citizenService;

    @Autowired
    private OcrController ocrController;

    @Autowired
    private OcrResultService ocrResultService;

    private final String[] AUTHORITY = {"Jobcenter", "Meldeamt"};

    @FXML
    public void initialize() {
        updateLoginStatusLabel();
        citizenAuthorityChooser.getItems().addAll(AUTHORITY);
        citizenAuthorityChooser.getSelectionModel().selectFirst();

        citizenOCRSubmitButton.setDisable(!OcrSessionHolder.isAvailable());

        Platform.runLater(() -> {
            ocrController.setCitizenController(this);
            ocrController.syncFormTypeWithCitizenController();
        });

    }

    private void updateLoginStatusLabel() {
        if (CitizenSessionHolder.isLoggedIn()) {
            User user = CitizenSessionHolder.getUser();
            FontIcon icon = new FontIcon(MaterialDesignC.CHECK_CIRCLE);
            icon.setIconSize(16);
            icon.setIconColor(Color.GREEN);
            loginStatusLabel.setGraphic(icon);
            loginStatusLabel.setText(" Angemeldet als: " + user.getEmail());
        } else {
            FontIcon icon = new FontIcon(MaterialDesignC.CLOSE_CIRCLE);
            icon.setIconSize(16);
            icon.setIconColor(Color.RED);
            loginStatusLabel.setGraphic(icon);
            loginStatusLabel.setText(" Nicht angemeldet");
        }
    }

    @FXML
    protected void onCitizenLogin(ActionEvent event) {
        loadLoginMask();
    }

    @FXML
    protected void onCitizenLogout(ActionEvent event) {
        CitizenSessionHolder.clear();
        updateLoginStatusLabel();
        DialogHelper.showInfo("Abgemeldet", "Sie wurden erfolgreich abgemeldet.");
    }

    @FXML
    protected void onCitizenOCRSubmit(ActionEvent event) {
        loadLoginMask();

        String text = OcrSessionHolder.get();
        String authority = citizenAuthorityChooser.getValue();

        if (text == null || text.isBlank()) {
            System.out.println("Kein OCR-Ergebnis vorhanden.");
            return;
        }

        try {
            User user = CitizenSessionHolder.getUser();
            int userId = user.getId();
            ocrAssignmentService.saveForUser(text, userId, authority);
            System.out.println("OCR gespeichert für User " + user.getId() + " mit Behörde: " + authority);
            DialogHelper.showInfo("Erfolg", "Die Formulardaten wurden erfolgreich übermittelt.");
        } catch (Exception e) {
            System.err.println("Fehler beim Speichern: " + e.getMessage());
        }
    }

    public void enableSubmitIfOcrAvailable() {
        citizenOCRSubmitButton.setDisable(!OcrSessionHolder.isAvailable());

        String formType = OcrSessionHolder.getFormType();
        if ("Buergergeld".equals(formType)) {
            citizenAuthorityChooser.getSelectionModel().select("Jobcenter");
            citizenAuthorityChooser.setDisable(true);
        }
    }

    protected void loadLoginMask() {
        if (!CitizenSessionHolder.isLoggedIn()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/github/ssalfelder/ocrformmate/fxml/citizen-login.fxml"));
                loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
                Parent loginRoot = loader.load();

                Stage stage = new Stage();
                stage.setScene(new Scene(loginRoot));
                stage.setTitle("Login erforderlich");
                stage.initModality(Modality.APPLICATION_MODAL); // blockiert andere Fenster
                stage.showAndWait();

                if (!CitizenSessionHolder.isLoggedIn()) {
                    System.out.println("Login wurde abgebrochen.");
                    return;
                }

                updateLoginStatusLabel();
                DialogHelper.showInfo("Login erfolgreich", "Sie sind nun angemeldet als:\n" +
                        CitizenSessionHolder.getUser().getEmail());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateAuthorityBasedOnFormType(String type) {
        if ("Buergergeld".equals(type)) {
            citizenAuthorityChooser.getSelectionModel().select("Jobcenter");
            citizenAuthorityChooser.setDisable(true);
        } else if ("Anmeldung".equals(type)) {
            citizenAuthorityChooser.getSelectionModel().select("Meldeamt");
            citizenAuthorityChooser.setDisable(true);
        } else {
            citizenAuthorityChooser.setDisable(false);
        }
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\controller\CitizenLoginController.java**

```
package com.github.ssalfelder.ocrformmate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import com.github.ssalfelder.ocrformmate.auth.CitizenSessionHolder;
import com.github.ssalfelder.ocrformmate.model.User;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import com.github.ssalfelder.ocrformmate.ui.StyleHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;


@Component
public class CitizenLoginController {

    @FXML
    private TextField citizenUsername;
    @FXML
    private TextField citizenPassword;

    @FXML
    protected void goBack(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void registrationCitizen(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/ssalfelder/ocrformmate/" +
                "fxml/citizen-registration.fxml"));
        loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
        Parent registrationRoot = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Scene scene = (new Scene(registrationRoot));
        scene.getStylesheets().add(getClass().getResource("/CSS/registration.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Registrierung");
        stage.show();
    }

    @FXML
    protected void citizenLoginClick(ActionEvent event) {
        String email = citizenUsername.getText().trim();
        String password = citizenPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            DialogHelper.showWarning("Fehlende Eingaben", "Bitte E-Mail-Adresse und Passwort eingeben.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json;

        try {
            Map<String, String> payload = Map.of(
                    "email", email,
                    "password", password
            );
            json = mapper.writeValueAsString(payload);
        } catch (Exception e) {
            DialogHelper.showError("Fehler", "Login Daten konnten nicht vorbereitet werden.");
            e.printStackTrace();
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/user/validate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            User user = mapper.readValue(response.body(), User.class);

                            CitizenSessionHolder.setUser(user);

                            Platform.runLater(() -> {
                                Stage stage = (Stage) citizenUsername.getScene().getWindow();
                                stage.close();
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() ->
                                    DialogHelper.showError("Fehler", "Benutzerdaten konnten nicht gelesen werden.")
                            );
                        }
                    } else {
                        Platform.runLater(() ->
                                DialogHelper.showWarning("Login fehlgeschlagen", "E-Mail oder Passwort falsch.")
                        );
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() ->
                            DialogHelper.showError("Verbindungsfehler", "Der Server ist nicht erreichbar.")
                    );
                    return null;
                });

    }

    @FXML
    protected void citizenResetClick(ActionEvent event) {
        citizenUsername.setText("");
        citizenPassword.setText("");
    }

}
```

**src\main\java\com\github\ssalfelder\ocrformmate\controller\ClerkController.java**

```
package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.dto.ClerkLoginDTO;
import com.github.ssalfelder.ocrformmate.model.Clerk;
import com.github.ssalfelder.ocrformmate.repository.ClerkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/clerk")
public class ClerkController {

    @Autowired
    private ClerkRepository clerkRepository;

    @Autowired
    private Argon2PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<Clerk> register(@RequestBody Clerk newClerk) {

        String hashedPassword = passwordEncoder.encode(newClerk.getPassword());
        newClerk.setPassword(hashedPassword);

        // Generate API secret
        newClerk.setSecret(UUID.randomUUID().toString());

        Clerk savedClerk = clerkRepository.save(newClerk);
        savedClerk.setPassword(null);

        return new ResponseEntity<>(savedClerk, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestParam("id") int id) {
        Optional<Clerk> clerk = clerkRepository.findById(id);
        if (clerk.isPresent()) {
            Clerk result = clerk.get();
            result.setPassword(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        return new ResponseEntity<>("No user found with id " + id, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody ClerkLoginDTO login) {
        Optional<Clerk> optionalClerk = clerkRepository.findByEmail(login.getEmail());

        if (optionalClerk.isPresent()) {
            Clerk clerk = optionalClerk.get();

            if (passwordEncoder.matches(login.getPassword(), clerk.getPassword())) {
                return ResponseEntity.ok().body(clerk.getSecret());
            }
        }

        return new ResponseEntity<>("Wrong credentials / not found.", HttpStatus.UNAUTHORIZED);
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\controller\ClerkLoginController.java**

```
package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import com.github.ssalfelder.ocrformmate.auth.ClerkSessionHolder;
import com.github.ssalfelder.ocrformmate.model.Clerk;
import com.github.ssalfelder.ocrformmate.service.ClerkService;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClerkLoginController {

    @FXML
    private TextField secretField;

    private final ClerkService clerkService;

    @Autowired
    public ClerkLoginController(ClerkService clerkService) {
        this.clerkService = clerkService;
    }

    @FXML
    protected void handleLogin(ActionEvent event) {
        String input = secretField.getText().trim();

        try {
            Clerk clerk = clerkService.findBySecret(input);
            ClerkSessionHolder.setLoggedInClerk(clerk); // speichern
            DialogHelper.showInfo("Erfolg", "Willkommen, " + clerk.getFirstname());

            // Scene-Wechsel nach login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/.../clerk.fxml"));
            loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            DialogHelper.showError("Fehler", "Ungültiges Secret.");
        }
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\controller\ClerkOcrController.java**

```
package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.service.OcrAssignmentService;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClerkOcrController {

    @FXML
    private TextArea resultTextArea;

    private final OcrAssignmentService ocrAssignmentService;

    @Autowired
    public ClerkOcrController(OcrAssignmentService ocrAssignmentService) {
        this.ocrAssignmentService = ocrAssignmentService;
    }

    @FXML
    protected void onClerkOCRSubmit(ActionEvent event) {
        String text = resultTextArea.getText();

        if (text == null || text.isBlank()) {
            DialogHelper.showWarning("Fehlende Eingabe", "Keine OCR-Daten gefunden.");
            return;
        }

        ocrAssignmentService.saveForLoggedInClerk(text);
        DialogHelper.showInfo("Gespeichert", "OCR-Daten wurden gespeichert.");
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\controller\LoginController.java**

```
package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.service.AdminService;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginController {
    @FXML
    private TextField adminSecret;
    private final AdminService adminService;

    @Autowired
    public LoginController(AdminService adminService) {
        this.adminService = adminService;
    }

    @FXML
    protected void adminSecretClick(ActionEvent e) {
        String input = adminSecret.getText().trim();
        if (input.isEmpty()) {
            DialogHelper.showWarning("Fehlende Eingabe", "Bitte Secret-Key eingeben.");
            return;
        }
        boolean valid = adminService.isSecretValid(1, input);
        if (valid) DialogHelper.showInfo("Erfolg", "Zugang gewährt!");
        else DialogHelper.showError("Fehler", "Ungültiger Secret-Key");
    }

}

```

**src\main\java\com\github\ssalfelder\ocrformmate\controller\MainController.java**

```
package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    private final String[] FORMTYPE = {"Jobcenter", "Meldeamt"};

    @FXML
    protected void citizenClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/citizen.fxml"));
        loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
        root = loader.load();
        switchScene(event);

    }

    @FXML
    protected void clerkClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/clerk-login.fxml"));
        loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
        root = loader.load();
        switchScene(event);
    }

    @FXML
    protected void registrationClerk(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/admin-password-dialog.fxml"));
        loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
        root = loader.load();
        switchScene(event);
    }

    @FXML
    protected void registrationCitizen(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/registration-citizen-.fxml"));
        loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
        root = loader.load();
        switchScene(event);
    }

    private void switchScene(ActionEvent event) {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}

```

**src\main\java\com\github\ssalfelder\ocrformmate\controller\OcrController.java**

```
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
```

**src\main\java\com\github\ssalfelder\ocrformmate\controller\RegistrationCitizenController.java**

```
package com.github.ssalfelder.ocrformmate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import com.github.ssalfelder.ocrformmate.ui.StyleHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.github.ssalfelder.ocrformmate.validation.*;

import java.io.IOException;
import java.net.http.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class RegistrationCitizenController {

    @FXML private TextField firstnameField;
    @FXML private TextField lastnameField;
    @FXML private TextField postalCodeField;
    @FXML private TextField streetField;
    @FXML private TextField houseNumberField;
    @FXML private TextField cityField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @Autowired
    private InputValidatorService inputValidator;

    @Autowired
    private PasswordValidatorService passwordValidator;

    @FXML
    public void handleBackClick() {
        loadLoginScene();
    }

    @FXML
    public void handleResetClick() {
        getAllInputFields().forEach(field -> {
            field.clear();
            StyleHelper.clearError(field);
        });
    }

    @FXML
    public void handleRegisterClick() throws Exception {
        // Feldprüfung
        List<Pair<TextInputControl, String>> fields = List.of(
                new Pair<>(firstnameField, "Vornamen"),
                new Pair<>(lastnameField, "Nachnamen"),
                new Pair<>(postalCodeField, "Postleitzahl"),
                new Pair<>(streetField, "Straße"),
                new Pair<>(houseNumberField, "Hausnummer"),
                new Pair<>(cityField, "Ort"),
                new Pair<>(emailField, "E-Mail"),
                new Pair<>(passwordField, "Passwort")
        );

        if (!inputValidator.isValidFirstname(firstnameField.getText())) {
            StyleHelper.markError(firstnameField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie einen gültigen Vornamen ein.");
            return;
        }

        if (!inputValidator.isValidLastname(lastnameField.getText())) {
            StyleHelper.markError(lastnameField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie einen gültigen Nachnamen ein.");
            return;
        }

        if (!inputValidator.isValidStreet(streetField.getText())) {
            StyleHelper.markError(streetField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie eine gültige Straße ein.");
            return;
        }

        if (!inputValidator.isValidHouseNumber(houseNumberField.getText())) {
            StyleHelper.markError(houseNumberField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie eine gültige Hausnummer ein.");
            return;
        }

        if (!inputValidator.isValidPostalCode(postalCodeField.getText())) {
            StyleHelper.markError(postalCodeField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie eine gültige Postleitzahl ein.");
            return;
        }

        if (!inputValidator.isValidCity(cityField.getText())) {
            StyleHelper.markError(cityField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie einen gültigen Ortsnamen ein.");
            return;
        }

        if (!phoneField.getText().isEmpty()) {
            if (!inputValidator.isValidPhone(phoneField.getText())) {
                StyleHelper.markError(phoneField);
                DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte achten sie bei der Telefonnummer " +
                        "auf ein gültiges Eingabeformat.");
                return;
            }
        }

        if (!inputValidator.isValidEmail(emailField.getText())) {
            StyleHelper.markError(emailField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie eine gültige Email-Adresse ein.");
            return;
        }

        PasswordValidationResult pwResult = passwordValidator.validatePassword(passwordField.getText());

        if (!pwResult.isValid()) {
            StyleHelper.markError(passwordField);
            String combinedErrors = String.join("\n", pwResult.getErrorMessages());
            DialogHelper.showWarning("Unsicheres Passwort", combinedErrors);
            return;
        }


        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            StyleHelper.markError(confirmPasswordField);
            DialogHelper.showError("Fehler", "Die Passwörter stimmen nicht überein.");
            return;
        }


        Map<String, String> payload = new HashMap<>();
        payload.put("firstname", firstnameField.getText());
        payload.put("lastname", lastnameField.getText());
        payload.put("postalCode", postalCodeField.getText());
        payload.put("street", streetField.getText());
        payload.put("houseNumber", houseNumberField.getText());
        payload.put("city", cityField.getText());
        payload.put("phoneNumber", phoneField.getText());
        payload.put("email", emailField.getText());
        payload.put("password", passwordField.getText());


        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/user/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 201) {
                        System.out.println("Registrierung erfolgreich!");

                        Platform.runLater(() -> {
                            DialogHelper.showInfo("Erfolg", "Registrierung abgeschlossen. Du wirst zum Login weitergeleitet.");
                            loadLoginScene();
                        });

                    } else {
                        Platform.runLater(() ->
                                DialogHelper.showError("Registrierung fehlgeschlagen",
                                "Serverantwort: " + response.body())
                        );
                        System.out.println(">>> StatusCode: " + response.statusCode());
                        System.out.println(">>> Body: " + response.body());
                        System.out.println(">>> Full Response: " + response);

                    }
                });
    }

    private void loadLoginScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/github/ssalfelder/ocrformmate/fxml/citizen-login.fxml"));
            loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) firstnameField.getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            scene.getStylesheets().add(getClass().getResource("/CSS/login.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<TextInputControl> getAllInputFields() {
        return List.of(
                firstnameField,
                lastnameField,
                postalCodeField,
                streetField,
                houseNumberField,
                cityField,
                phoneField,
                emailField,
                passwordField,
                confirmPasswordField
        );
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\controller\UserController.java**

```
package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.dto.UserLoginDTO;
import com.github.ssalfelder.ocrformmate.dto.UserRegistrationDTO;
import com.github.ssalfelder.ocrformmate.model.Address;
import com.github.ssalfelder.ocrformmate.model.User;
import com.github.ssalfelder.ocrformmate.repository.AddressRepository;
import com.github.ssalfelder.ocrformmate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private Argon2PasswordEncoder passwordEncoder;

    /**
     * Registrierung eines neuen Users über DTO
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDTO dto) {
        try {
            System.out.println("Registrierung gestartet");
            System.out.println("DTO erhalten: ");

            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Benutzer mit dieser E-Mail existiert bereits.");
            }

            String fullStreet = dto.getStreet() + " " + dto.getHouseNumber();

            // Adresse finden oder neu anlegen
            Optional<Address> existing = addressRepository.findByStreetAndPostalCodeAndCity(
                    fullStreet, dto.getPostalCode(), dto.getCity()
            );

            Address address = existing.orElseGet(() -> {
                Address newAddress = new Address();
                newAddress.setStreet(fullStreet);
                newAddress.setPostalCode(dto.getPostalCode());
                newAddress.setCity(dto.getCity());
                return addressRepository.save(newAddress);
            });

            User user = new User();
            user.setFirstname(dto.getFirstname());
            user.setLastname(dto.getLastname());
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setPhoneNumber(dto.getPhoneNumber());
            user.setAddress(address);
            user.setSecret(UUID.randomUUID().toString());


            User savedUser = userRepository.save(user);

            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();

            String errorClass = ex.getClass().getSimpleName();
            String errorMessage = ex.getMessage() != null ? ex.getMessage() : "<keine Nachricht>";
            String cause = (ex.getCause() != null) ? ex.getCause().toString() : "<keine Ursache>";

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler: " + errorClass + " – " + errorMessage + " – caused by: " + cause);
        }
    }

    /**
     * Benutzer anhand der ID abrufen
     */
    @GetMapping
    public ResponseEntity<?> get(@RequestParam(value = "id") int id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No user found with id " + id, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Benutzer anhand von E-Mail + Passwort validieren
     */
    @PostMapping("/validate")
    public ResponseEntity<User> validate(@RequestBody UserLoginDTO login) {
        return userRepository.findByEmail(login.getEmail())
                .filter(user -> passwordEncoder.matches(login.getPassword(), user.getPassword()))
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("UserController aktiv");
    }

}
```

**src\main\java\com\github\ssalfelder\ocrformmate\dto\ClerkLoginDTO.java**

```
package com.github.ssalfelder.ocrformmate.dto;

public class ClerkLoginDTO {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\dto\UserLoginDTO.java**

```
package com.github.ssalfelder.ocrformmate.dto;

public class UserLoginDTO {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\dto\UserRegistrationDTO.java**

```
package com.github.ssalfelder.ocrformmate.dto;

public class UserRegistrationDTO {

    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String phoneNumber;
    private String houseNumber;
    private String street;
    private String postalCode;
    private String city;


    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\init\OpenCvLoader.java**

```
package com.github.ssalfelder.ocrformmate.init;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.javacpp.Loader;

public class OpenCvLoader {
    private static boolean loaded = false;

    public static boolean init() {
        if (!loaded) {
            Loader.load(opencv_core.class);
            loaded = true;
            System.out.println("OpenCV via Bytedeco erfolgreich geladen.");
        }
        return loaded;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\model\Address.java**

```
package com.github.ssalfelder.ocrformmate.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Ort")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Strasse", nullable = false)
    private String street;

    @Column(name = "Postleitzahl", nullable = false)
    private String postalCode;

    @Column(name = "Ort", nullable = false)
    private String city;

    public Integer getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

```

**src\main\java\com\github\ssalfelder\ocrformmate\model\Admin.java**

```
package com.github.ssalfelder.ocrformmate.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Email", unique = true, nullable = false)
    private String email;

    @Column(name = "Passwort", nullable = false)
    private String password;

    private String secret;

    @Column(name = "Erstellt", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "Aktualisiert")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\model\Clerk.java**

```
package com.github.ssalfelder.ocrformmate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "Mitarbeiter")
public class Clerk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Vorname", nullable = false)
    private String firstname;

    @OneToMany(mappedBy = "clerk", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<OcrResult> ocrResults;

    @Column(name = "Nachname", nullable = false)
    private String lastname;

    @Column(name = "Postleitzahl", nullable = false)
    private String postalCode;

    @Column(name = "Email", unique = true, nullable = false)
    private String email;

    @Column(name = "Passwort", nullable = false)
    private String password;

    @Column(name = "Behörde")
    private String authority;

    @Column(name = "Erstellt", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "Aktualisiert")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    private String secret;

    public String getSecret() {
        return secret;
    }

    @JsonIgnore
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\model\OcrResult.java**

```
package com.github.ssalfelder.ocrformmate.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocr_result")
public class OcrResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recognized_text", length = 5000)
    private String recognizedText;

    private LocalDateTime createdAt;

    @Column(name = "Behörde")
    private String authority;

    // für Bürger
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "Kundennummer")
    private User user;

    // für Sachbearbeiter
    @ManyToOne
    @JoinColumn(name = "clerk_id", referencedColumnName = "id")
    private Clerk clerk;

    // ggf. weitere Felder: Dateiname, Dokumenttyp etc.

    public OcrResult() {
        this.createdAt = LocalDateTime.now();
    }

    public OcrResult(String recognizedText) {
        this.recognizedText = recognizedText;
        this.createdAt = LocalDateTime.now();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Clerk getClerk() {
        return clerk;
    }

    public void setClerk(Clerk clerk) {
        this.clerk = clerk;
    }

    public String getRecognizedText() {
        return recognizedText;
    }

    public void setRecognizedText(String recognizedText) {
        this.recognizedText = recognizedText;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\model\User.java**

```
package com.github.ssalfelder.ocrformmate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "Benutzer")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Kundennummer")
    private Integer id;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<OcrResult> ocrResults;


    @Column(name = "Vorname", nullable = false)
    private String firstname;

    @Column(name = "Nachname", nullable = false)
    private String lastname;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "adresse_id", referencedColumnName = "id")
    private Address address;

    @Column(name = "Telefonnummer")
    private String phoneNumber;

    @Column(name = "Email", unique = true, nullable = false)
    private String email;

    @Column(name = "Passwort", nullable = false)
    private String password;

    @Column(name = "Erstellt", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "Aktualisiert")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String secret;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getSecret() {
        return secret;
    }

    @JsonIgnore
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\repository\AddressRepository.java**

```
package com.github.ssalfelder.ocrformmate.repository;

import com.github.ssalfelder.ocrformmate.model.Address;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends CrudRepository<Address, Integer> {

    Optional<Address> findByStreetAndPostalCodeAndCity(String street, String postalCode, String city);
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\repository\AdminRepository.java**

```
package com.github.ssalfelder.ocrformmate.repository;

import com.github.ssalfelder.ocrformmate.model.Admin;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AdminRepository extends CrudRepository<Admin, Integer> {
    Optional<Admin> findByEmailAndPassword(String email, String password);
    Optional<Admin> findBySecret(String secret);
    Optional<Admin> findById(int id);
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\repository\ClerkRepository.java**

```
package com.github.ssalfelder.ocrformmate.repository;

import com.github.ssalfelder.ocrformmate.model.Clerk;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ClerkRepository extends CrudRepository<Clerk, Integer> {

    Optional<Clerk> findByEmail(String email);
    Optional<Clerk> findBySecret(String secret);
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\repository\OcrResultRepository.java**

```
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
```

**src\main\java\com\github\ssalfelder\ocrformmate\repository\UserRepository.java**

```
package com.github.ssalfelder.ocrformmate.repository;

import com.github.ssalfelder.ocrformmate.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findBySecret(String secret);
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\security\PasswordEncoderConfig.java**

```
package com.github.ssalfelder.ocrformmate.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public Argon2PasswordEncoder passwordEncoder() {

        return new Argon2PasswordEncoder(16,
                                        32,
                                          2,
                                        65536,
                                           5);
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\security\SecurityConfig.java**

```
package com.github.ssalfelder.ocrformmate.security;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @PostConstruct
    public void init() {
        System.out.println(">>> SecurityConfig wurde geladen!");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/user/register",
                                "/user/validate",
                                "/clerk/register",
                                "/clerk/validate",
                                "/ping",
                                "/pdfjs/**",
                                "/pdf/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form.disable())

                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
    @Bean
    public UserDetailsService noopUserDetailsService() {
        return username -> null;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\AdminService.java**

```
package com.github.ssalfelder.ocrformmate.service;

import com.github.ssalfelder.ocrformmate.model.Admin;
import com.github.ssalfelder.ocrformmate.repository.AdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public boolean isSecretValid(Integer adminId, String inputSecret) {
        Admin admin = adminRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Admin nicht gefunden"));
        return admin.getSecret().equals(inputSecret);
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\AlignmentAnalyzer.java**

```
package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_imgproc.Vec2fVector;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Component
public class AlignmentAnalyzer {

    public void analyzeLines(Mat image, String label) {
        // In Graustufen umwandeln
        Mat gray = new Mat();
        if (image.channels() > 1) {
            cvtColor(image, gray, COLOR_BGR2GRAY);
        } else {
            gray = image.clone();
        }

        // Canny-Kantenerkennung
        Mat edges = new Mat();
        Canny(gray, edges, 50, 200);

        // Hough-Linien finden
        Vec2fVector lines = new Vec2fVector();
        HoughLines(edges, lines, 1, Math.PI / 180, 100, 0, 0, 0, Math.PI);

        // Linienklassifikation
        int totalLines = (int) lines.size();
        int horizontal = 0, vertical = 0, diagonal = 0;

        for (int i = 0; i < totalLines; i++) {
            float rho = lines.get(i).get(0);
            float theta = lines.get(i).get(1);

            double angleDeg = Math.toDegrees(theta);

            if (angleDeg >= 85 && angleDeg <= 95) {
                horizontal++;
            } else if (angleDeg <= 5 || angleDeg >= 175) {
                vertical++;
            } else {
                diagonal++;
            }
        }

        System.out.println("Analyse für: " + label);
        System.out.println("  ➤ Gesamtlinien: " + totalLines);
        System.out.println("  ➤ Horizontal:   " + horizontal);
        System.out.println("  ➤ Vertikal:     " + vertical);
        System.out.println("  ➤ Diagonal:     " + diagonal);
        System.out.println("----------------------------------");
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\CitizenService.java**

```
package com.github.ssalfelder.ocrformmate.service;

import com.github.ssalfelder.ocrformmate.model.OcrResult;
import com.github.ssalfelder.ocrformmate.model.User;
import com.github.ssalfelder.ocrformmate.repository.OcrResultRepository;
import com.github.ssalfelder.ocrformmate.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CitizenService {

    private final OcrResultRepository ocrResultRepository;
    private final UserRepository userRepository;

    public CitizenService(OcrResultRepository ocrResultRepository, UserRepository userRepository) {
        this.ocrResultRepository = ocrResultRepository;
        this.userRepository = userRepository;
    }

}
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\ClerkService.java**

```
package com.github.ssalfelder.ocrformmate.service;

import com.github.ssalfelder.ocrformmate.model.Clerk;
import com.github.ssalfelder.ocrformmate.repository.ClerkRepository;
import org.springframework.stereotype.Service;

@Service
public class ClerkService {

    private final ClerkRepository clerkRepository;

    public ClerkService(ClerkRepository clerkRepository) {
        this.clerkRepository = clerkRepository;
    }

    public boolean isSecretValid(Integer clerkId, String inputSecret) {
        return clerkRepository.findById(clerkId)
                .map(clerk -> clerk.getSecret().equals(inputSecret))
                .orElse(false);
    }

    public Clerk findBySecret(String secret) {
        return clerkRepository.findBySecret(secret)
                .orElseThrow(() -> new RuntimeException("Kein Clerk mit diesem Secret"));
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\DocumentAlignmentService.java**

```
package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class DocumentAlignmentService {

    public boolean alignDocument(String inputImagePath, String outputImagePath) {
        Mat original = opencv_imgcodecs.imread(inputImagePath);
        if (original.empty()) {
            System.err.println("Konnte Bild nicht laden: " + inputImagePath);
            return false;
        }

        // Graustufen umwandeln, blurren, Kanten finden
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(original, gray, opencv_imgproc.COLOR_BGR2GRAY);

        Mat blurred = new Mat();
        opencv_imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        Mat edges = new Mat();
        opencv_imgproc.Canny(blurred, edges, 75, 200);

        // Konturen finden
        MatVector contours = new MatVector();
        Mat hierarchy = new Mat();
        opencv_imgproc.findContours(
                edges,
                contours,
                hierarchy,
                opencv_imgproc.RETR_LIST,
                opencv_imgproc.CHAIN_APPROX_SIMPLE
        );

        // Dokument-Kontur suchen
        Point2fVector docContour = findDocumentContour(contours);
        if (docContour == null) {
            System.out.println("Kein Formularrahmen erkannt – verwende Originalbild.");
            opencv_imgcodecs.imwrite(outputImagePath, original);
            return true;
        }

        // Ecken neu anordnen
        Point2fVector sortedCorners = reorderCorners(docContour);

        // Zielgröße definieren
        double width = 800;
        double height = 1000;
        Point2fVector dstCorners = new Point2fVector(
                new Point2f(0, 0),
                new Point2f((float) width, 0),
                new Point2f((float) width, (float) height),
                new Point2f(0, (float) height)
        );

        Mat srcMat = new Mat(sortedCorners);
        Mat dstMat = new Mat(dstCorners);

        // Perspektivische Transformation berechnen
        Mat transform = opencv_imgproc.getPerspectiveTransform(srcMat, dstMat);
        Mat aligned = new Mat();
        opencv_imgproc.warpPerspective(original, aligned, transform, new Size((int) width, (int) height));

        boolean success = opencv_imgcodecs.imwrite(outputImagePath, aligned);
        if (!success) {
            System.err.println("Konnte ausgerichtetes Bild nicht speichern: " + outputImagePath);
        }
        return success;
    }

    private Point2fVector findDocumentContour(MatVector contours) {
        List<Mat> candidates = new ArrayList<>();
        for (long i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            double area = opencv_imgproc.contourArea(contour);
            if (area < 1000) continue;

            Mat approxCurve = new Mat();
            opencv_imgproc.approxPolyDP(contour, approxCurve, 0.02 * opencv_imgproc.arcLength(contour, true), true);

            Point2fVector approx = new Point2fVector(approxCurve);
            if (approx.size() == 4) {
                candidates.add(contour);
            }
        }

        if (!candidates.isEmpty()) {
            Mat largest = Collections.max(candidates, Comparator.comparingDouble(opencv_imgproc::contourArea));
            Mat approxCurve = new Mat();
            opencv_imgproc.approxPolyDP(largest, approxCurve, 0.02 * opencv_imgproc.arcLength(largest, true), true);

            Point2fVector approx = new Point2fVector(approxCurve);
            return approx;
        }
        return null;
    }

    private Point2fVector reorderCorners(Point2fVector polygon) {
        // Listenkonvertierung für Sortierung
        List<Point2f> pts = new ArrayList<>();
        for (long i = 0; i < polygon.size(); i++) {
            pts.add(polygon.get(i));
        }

        // Sortiere nach Y
        pts.sort(Comparator.comparingDouble(p -> p.y()));

        Point2f top1 = pts.get(0);
        Point2f top2 = pts.get(1);
        Point2f bottom1 = pts.get(2);
        Point2f bottom2 = pts.get(3);

        Point2f topLeft, topRight;
        if (top1.x() < top2.x()) {
            topLeft = top1;
            topRight = top2;
        } else {
            topLeft = top2;
            topRight = top1;
        }

        Point2f bottomLeft, bottomRight;
        if (bottom1.x() < bottom2.x()) {
            bottomLeft = bottom1;
            bottomRight = bottom2;
        } else {
            bottomLeft = bottom2;
            bottomRight = bottom1;
        }

        Point2fVector ordered = new Point2fVector(4);
        ordered.put(0, topLeft);
        ordered.put(1, topRight);
        ordered.put(2, bottomRight);
        ordered.put(3, bottomLeft);

        return ordered;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\FormFieldService.java**

```
package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class FormFieldService {

    private static final Map<String, Rect> BUERGERGELDFIELDS = Map.ofEntries(
            Map.entry("familienname", new Rect(212, 1113, 711, 127)),
            Map.entry("vorname", new Rect(922, 1123, 852, 91)),
            Map.entry("geburtsname", new Rect(213, 1252, 943, 124)),
            Map.entry("geburtsdatum", new Rect(1156, 1256, 616, 120)),
            Map.entry("geburtsort", new Rect(212, 1405, 708, 110)),
            Map.entry("geburtsland", new Rect(922, 1404, 849, 114)),
            Map.entry("geschlecht", new Rect(211, 1540, 474, 108)),
            Map.entry("staatsangehoerigkeit", new Rect(686, 1540, 1086, 108)),
            Map.entry("einreisedatum", new Rect(216, 1767, 1556, 109)),
            Map.entry("rentenversicherungsnummer", new Rect(213, 1918, 701, 115)),
            Map.entry("strasse_hausnummer", new Rect(217, 2059, 1555, 131)),
            Map.entry("wohnhaft_bei", new Rect(212, 2205, 1560, 99)),
            Map.entry("postleitzahl", new Rect(216, 2340, 469, 114)),
            Map.entry("wohnort", new Rect(687, 2350, 1087, 111)),
            Map.entry("telefonnummer", new Rect(212, 2542, 708, 123)),
            Map.entry("email", new Rect(924, 2552, 851, 118)),
            Map.entry("antrag_spaeter", new Rect(1134, 2679, 633, 123)),
            Map.entry("antrag_folgemonat", new Rect(641, 2808, 1133, 98)),
            Map.entry("familienstand_getrennt", new Rect(785, 3105, 986, 93)),
            Map.entry("familienstand_geschieden", new Rect(566, 3189, 1204, 89)),
            Map.entry("familienstand_lp", new Rect(940, 3265, 832, 119))
    );

    private static final Map<String, Map<String, Rect>> FORM_TEMPLATES = Map.of(
            "BUERGERGELD", BUERGERGELDFIELDS,
            "ANMELDUNG", Map.of(
                    "strasse", new Rect(120, 300, 180, 50),
                    "ort", new Rect(320, 300, 180, 50)
            )
    );


    /**
     * Zeichnet die Felder eines Templates als grüne Rechtecke ins Dokument.
     */
    public Mat overlayTemplateFields(Mat alignedDoc, String formType) {
        Map<String, Rect> fields = getTemplateFields(formType);
        for (Rect rect : fields.values()) {
            rectangle(alignedDoc, rect, new Scalar(0, 255, 0, 0), 2, LINE_8, 0);
        }

        System.out.println("Felder aus Template [" + formType + "] gezeichnet: " + fields.size());
        imwrite("form_preview.png", alignedDoc);
        return alignedDoc;
    }

    /**
     * Gibt die benannten Felder eines Templates zurück.
     */
    public Map<String, Rect> getTemplateFields(String formType) {
        return FORM_TEMPLATES.getOrDefault(formType.toUpperCase(), Map.of());
    }
}

```

**src\main\java\com\github\ssalfelder\ocrformmate\service\HandwritingClient.java**

```
package com.github.ssalfelder.ocrformmate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bytedeco.opencv.opencv_core.Rect;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HandwritingClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String recognize(File imageFile) throws IOException, InterruptedException {
        HttpRequest request = buildMultipartRequest(imageFile);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseRecognizedText(response.body());
        } else {
            throw new RuntimeException("OCR error: " + response.statusCode());
        }
    }

    public Map<String, String> recognizeWithFields(File imageFile, Map<String, Rect> fields, String formType) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // JSON für Feldkoordinaten erzeugen
        Map<String, int[]> jsonFields = new HashMap<>();
        for (Map.Entry<String, Rect> entry : fields.entrySet()) {
            Rect r = entry.getValue();
            jsonFields.put(entry.getKey(), new int[]{r.x(), r.y(), r.width(), r.height()});
        }
        String fieldsJson = mapper.writeValueAsString(jsonFields);

        // Multipart-Request vorbereiten
        String boundary = "----OCRBoundary1234";
        String part1 = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"image\"; filename=\"" + imageFile.getName() + "\"\r\n"
                + "Content-Type: image/png\r\n\r\n";
        String part2 = "\r\n--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"fields\"\r\n\r\n"
                + fieldsJson + "\r\n";
        String part3 = "\r\n--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"formType\"\r\n\r\n"
                + formType + "\r\n";
        String part4 = "--" + boundary + "--\r\n";

        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(part1.getBytes());
        body.write(imageBytes);
        body.write(part2.getBytes());
        body.write(part3.getBytes());
        body.write(part4.getBytes());

        // Anfrage an den neuen OCR-Endpoint
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:6000/handwriting/recognizeFields"))
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // JSON → Map<String, String>
            return mapper.readValue(response.body(), new TypeReference<Map<String, String>>() {});
        } else {
            throw new RuntimeException("OCR error: HTTP " + response.statusCode());
        }
    }


    private HttpRequest buildMultipartRequest(File file) throws IOException {
        String boundary = "----OCRBoundary1234";
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        String part1 = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"image\"; filename=\"" + file.getName() + "\"\r\n"
                + "Content-Type: image/png\r\n\r\n";
        String part2 = "\r\n--" + boundary + "--\r\n";

        byte[] payload = ByteBuffer
                .allocate(part1.getBytes().length + fileBytes.length + part2.getBytes().length)
                .put(part1.getBytes())
                .put(fileBytes)
                .put(part2.getBytes())
                .array();

        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/handwriting/recognize"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();
    }

    private String parseRecognizedText(String json) {
        try {
            JsonNode root = mapper.readTree(json);

            if (root.has("skipped") && root.get("skipped").asBoolean()) {
                return "__SKIPPED__"; // Platzhalter für: "Nur vorgedruckter Text erkannt"
            }

            return root.has("text") ? root.get("text").asText() : "";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\ImageDisplayService.java**

```
package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Service;

import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class ImageDisplayService {

    /**
     * Entfernt den Alphakanal (Transparenz) aus einem Bild, falls vorhanden.
     * Speichert das Ergebnis als neue Datei im RGB-Format.
     *
     * @param inputPath  Pfad zur PNG-Datei mit möglichem Alpha
     * @param outputPath Zielpfad für das Bild ohne Alpha
     * @return true, wenn erfolgreich
     */
    public boolean removeAlphaChannel(String inputPath, String outputPath) {
        Mat input = imread(inputPath, IMREAD_UNCHANGED);
        if (input.empty()) {
            System.err.println("Bild konnte nicht geladen werden: " + inputPath);
            return false;
        }

        if (input.channels() == 4) {
            Mat rgb = new Mat();
            cvtColor(input, rgb, COLOR_BGRA2BGR);
            return imwrite(outputPath, rgb);
        } else {
            return imwrite(outputPath, input);
        }
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\ImageScalerService.java**

```
package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;

import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

@Service
public class ImageScalerService {

    // Zielgröße: A4 bei 300 DPI
    private final Size targetSize = new Size(2480, 3508);

    public String scaleToTargetSize(String inputPath, String outputPath) {
        Mat original = imread(inputPath);
        if (original.empty()) {
            throw new RuntimeException("Bild konnte nicht geladen werden: " + inputPath);
        }

        Mat scaled = new Mat();
        opencv_imgproc.resize(original, scaled, targetSize);
        imwrite(outputPath, scaled);
        return outputPath;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\OcrAssignmentService.java**

```
package com.github.ssalfelder.ocrformmate.service;

import com.github.ssalfelder.ocrformmate.auth.ClerkSessionHolder;
import com.github.ssalfelder.ocrformmate.model.Clerk;
import com.github.ssalfelder.ocrformmate.model.OcrResult;
import com.github.ssalfelder.ocrformmate.repository.ClerkRepository;
import com.github.ssalfelder.ocrformmate.repository.OcrResultRepository;
import com.github.ssalfelder.ocrformmate.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class OcrAssignmentService {

    private final OcrResultRepository ocrResultRepository;
    private final UserRepository userRepository;
    private final ClerkRepository clerkRepository;

    public OcrAssignmentService(OcrResultRepository ocrResultRepository,
                                UserRepository userRepository,
                                ClerkRepository clerkRepository) {
        this.ocrResultRepository = ocrResultRepository;
        this.userRepository = userRepository;
        this.clerkRepository = clerkRepository;
    }

    public OcrResult saveForUser(String text, int userId, String authority) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        OcrResult result = new OcrResult();
        result.setRecognizedText(text);
        result.setUser(user);
        result.setAuthority(authority);
        return ocrResultRepository.save(result);
    }

    public OcrResult saveForLoggedInClerk(String text) {
        Clerk clerk = ClerkSessionHolder.getLoggedInClerk();

        if (clerk == null) throw new IllegalStateException("Kein Clerk eingeloggt");

        OcrResult result = new OcrResult();
        result.setRecognizedText(text);
        result.setClerk(clerk);
        result.setAuthority(clerk.getAuthority());
        return ocrResultRepository.save(result);
    }

}
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\OcrResultService.java**

```
package com.github.ssalfelder.ocrformmate.service;

import com.github.ssalfelder.ocrformmate.model.OcrResult;
import com.github.ssalfelder.ocrformmate.repository.OcrResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OcrResultService {

    @Autowired
    private OcrResultRepository ocrResultRepository;

    public OcrResult saveOcrResult(String recognizedText) {
        OcrResult ocrResult = new OcrResult(recognizedText);
        return ocrResultRepository.save(ocrResult);
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\OcrService.java**

```
package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

@Service
public class OcrService {
    private HandwritingClient handwritingClient;

    public OcrService() {
        this.handwritingClient = new HandwritingClient();
    }

    public Map<String, String> recognizeFields(File imageFile, Map<String, Rect> fields, String formType) throws Exception {
        return handwritingClient.recognizeWithFields(imageFile, fields, formType);
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\PdfConverterService.java**

```
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
```

**src\main\java\com\github\ssalfelder\ocrformmate\service\PdfFormFillerService.java**

```
package com.github.ssalfelder.ocrformmate.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class PdfFormFillerService {
    private static final Map<String,String> FIELD_NAME_MAP = Map.of(
            "familienname",     "PersFam",
            "vorname",          "PersVorn",
            "geburtsdatum",     "PersGebDat"
    );

    public void fillForm(File templatePdf, File outPdf, Map<String,String> ocrValues) throws IOException {
        try (PDDocument doc = PDDocument.load(templatePdf)) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            if (form == null) throw new IllegalStateException("Kein AcroForm im PDF!");

            System.out.println("→ Alle Formularfelder im Template:");
            form.getFields().forEach(f -> System.out.println("   • " + f.getFullyQualifiedName()));

            for (var entry : ocrValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                String pdfFieldName = FIELD_NAME_MAP.get(key);
                if (pdfFieldName != null) {
                    PDField field = form.getField(pdfFieldName);
                    if (field != null) {
                        field.setValue(value);
                    } else {
                        System.err.println("Feld nicht gefunden: " + pdfFieldName);
                    }
                }
            }
            doc.save(outPdf);
        }
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\session\OcrSessionHolder.java**

```
package com.github.ssalfelder.ocrformmate.session;

public class OcrSessionHolder {
    private static String recognizedText;
    private static String formType;

    public static void set(String text) {
        recognizedText = text;
    }

    public static String get() {
        return recognizedText;
    }

    public static String getFormType() {
        return formType;
    }

    public static void setFormType(String formType) {
        OcrSessionHolder.formType = formType;
    }

    public static boolean isAvailable() {
        return recognizedText != null && !recognizedText.isBlank();
    }

    public static void clear() {
        recognizedText = null;
        formType = null;
    }

}
```

**src\main\java\com\github\ssalfelder\ocrformmate\ui\DialogHelper.java**

```
package com.github.ssalfelder.ocrformmate.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.util.Pair;

import java.util.List;
import java.util.Map;

public class DialogHelper {

    private DialogHelper() {
    }
    public static void showConfirmation(String title, String message) {
        show(Alert.AlertType.CONFIRMATION, title, message);
    }

    public static void showError(String title, String message) {
        show(Alert.AlertType.ERROR, title, message);
    }

    public static void showInfo(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    public static void showWarning(String title, String message) {
        show(Alert.AlertType.WARNING, title, message);
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean validateRequiredFields(List<Pair<TextInputControl, String>> fields) {
        int counter = 0;
        String value = "";

        for (Pair<TextInputControl, String> pair : fields) {
            TextInputControl control = pair.getKey();
            String fieldName = pair.getValue();

            //Vorherige Fehler entfernen
            control.getStyleClass().remove("error-border");

            if (control.getText().isBlank()) {
                counter++;
                value = fieldName;

                StyleHelper.markError(control);
            }

        }
        if (counter == 1) {
            showWarning("Fehlende Eingabe", "Bitte " + value + " eingeben.");
            return false;
        } else if (counter > 0) {
            showWarning("Fehlende Eingaben", "Bitte die nicht optionalen Felder ausfüllen.");
            return false;
        }
        return true;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\ui\StyleHelper.java**

```
package com.github.ssalfelder.ocrformmate.ui;

import javafx.scene.control.TextInputControl;

public class StyleHelper {

    public static void markError(TextInputControl control) {
        if (!control.getStyleClass().contains("error-border")) {
            control.getStyleClass().add("error-border");
        }

        control.textProperty().addListener((obs, oldVal, newVal) ->
                control.getStyleClass().remove("error-border"));
    }

    public static void clearError(TextInputControl control) {
        control.getStyleClass().remove("error-border");
    }

}
```

**src\main\java\com\github\ssalfelder\ocrformmate\validation\InputValidatorService.java**

```
package com.github.ssalfelder.ocrformmate.validation;

import org.springframework.stereotype.Service;

@Service
public class InputValidatorService {

    public boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$");
    }

    public boolean isValidPostalCode(String postalCode) {
        return postalCode != null && postalCode.matches("\\d{5}");
    }

    public boolean isValidCity(String city) {
        return city != null && city.matches("^[A-Za-zÄäÖöÜüß\\-\\.\\s]{2,40}$");
    }

    public boolean isValidPhone(String phone) {
        return phone != null && phone.matches("[\\d +()-]{7,20}");
    }

    public boolean isValidStreet(String street) {
        return street != null && street.matches("^[\\p{L}0-9 .\\-]{2,50}$");
    }

    public boolean isValidHouseNumber(String houseNumber) {
        return houseNumber != null && houseNumber.matches("^\\d+[a-zA-Z]?$");
    }

    public boolean isValidFirstname(String firstname) {
        return firstname != null && firstname.matches("^[A-Za-zÄäÖöÜüß\\- ]{2,30}$");
    }

    public boolean isValidLastname(String lastname) {
        return lastname != null && lastname.matches("^[A-Za-zÄäÖöÜüß\\- ]{2,30}$");
    }
}

```

**src\main\java\com\github\ssalfelder\ocrformmate\validation\PasswordValidationResult.java**

```
package com.github.ssalfelder.ocrformmate.validation;

import java.util.ArrayList;
import java.util.List;

public class PasswordValidationResult {
    private final List<String> errorMessages = new ArrayList<>();

    public void addError(String message) {
        errorMessages.add(message);
    }

    public boolean isValid() {
        return errorMessages.isEmpty();
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
```

**src\main\java\com\github\ssalfelder\ocrformmate\validation\PasswordValidatorService.java**

```
package com.github.ssalfelder.ocrformmate.validation;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class PasswordValidatorService {

    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:'\",.<>/?";

    public PasswordValidationResult validatePassword(String password) {
        PasswordValidationResult result = new PasswordValidationResult();

        if (password == null || password.length() < 8) {
            result.addError("Mindestens 8 Zeichen erforderlich.");
        }

        if (!password.matches(".*[A-Z].*")) {
            result.addError("Mindestens ein Großbuchstabe erforderlich.");
        }

        if (!password.matches(".*[a-z].*")) {
            result.addError("Mindestens ein Kleinbuchstabe erforderlich.");
        }

        if (!password.matches(".*\\d.*")) {
            result.addError("Mindestens eine Ziffer erforderlich.");
        }

        if (!password.matches(".*[" + Pattern.quote(SPECIAL_CHARS) + "].*")) {
            result.addError("Mindestens ein Sonderzeichen erforderlich.");
        }

        return result;
    }
}

```

# Python-Skripte
**python\ocr\api.py**

```
from flask import Flask, request, jsonify
from PIL import Image
from pathlib import Path
from transformers import TrOCRProcessor, VisionEncoderDecoderModel, GenerationConfig
from ocr.postprocessing import postprocess_all
from concurrent.futures import ThreadPoolExecutor, as_completed
import torch

app = Flask(__name__)

LOCAL_MODEL_PATH = Path("../models/fhswf_trocr").resolve()

if not LOCAL_MODEL_PATH.exists():
    raise FileNotFoundError(f"Model path not found: {LOCAL_MODEL_PATH}")

processor = TrOCRProcessor.from_pretrained(LOCAL_MODEL_PATH, local_files_only=True, use_fast=True)
model = VisionEncoderDecoderModel.from_pretrained(LOCAL_MODEL_PATH, local_files_only=True)
model.eval()

generation_config = GenerationConfig.from_pretrained(LOCAL_MODEL_PATH, local_files_only=True)


def optimize_generation_config_for_speed(config):
    config.num_beams = 1
    config.do_sample = False
    config.length_penalty = 1.0
    config.no_repeat_ngram_size = 0
    config.early_stopping = False
    config.use_cache = True
    config.max_length = 50
    return config


generation_config = optimize_generation_config_for_speed(generation_config)

# OCR-Feldfunktion (für parallele Threads)
def recognize_single_field(name, crop, processor, model, config):
    try:
        pixel_values = processor(images=crop, return_tensors="pt").pixel_values
        print(f"[{name}] final config in use:")
        for k, v in config.to_dict().items():
            print(f"{k:25}: {v}")
        with torch.no_grad():
            generated_ids = model.generate(pixel_values, generation_config=config)
        text = processor.batch_decode(generated_ids, skip_special_tokens=True)[0]
        return name, text.strip()
    except Exception as e:
        return name, f"[Fehler: {e}]"

@app.route("/handwriting/recognize", methods=["POST"])
def recognize_handwriting():
    if "image" not in request.files:
        return jsonify({"error": "No 'image' file in request"}), 400

    file = request.files["image"]
    try:
        image = Image.open(file.stream).convert("RGB")
    except Exception as e:
        return jsonify({"error": f"Cannot open image: {e}"}), 400

    try:
        pixel_values = processor(images=image, return_tensors="pt").pixel_values
        print("Final config in use:")
        print(generation_config)
        with torch.no_grad():
            generated_ids = model.generate(pixel_values, generation_config=generation_config)
        text = processor.batch_decode(generated_ids, skip_special_tokens=True)[0]
        return jsonify({"text": text.strip()})
    except Exception as e:
        return jsonify({"error": f"OCR failed: {e}"}), 500

@app.route("/handwriting/recognizeFields", methods=["POST"])
def recognize_fields():
    if "image" not in request.files or "fields" not in request.form:
        return jsonify({"error": "Image or fields missing"}), 400

    try:
        image = Image.open(request.files["image"]).convert("RGB")
        fields = request.form.get("fields")
        field_boxes = eval(fields)  # Format: {"feldname": [x, y, w, h]}
    except Exception as e:
        return jsonify({"error": f"Parsing error: {e}"}), 400

    # Parallelisierte Felderkennung
    with ThreadPoolExecutor(max_workers=6) as executor:
        futures = []
        for name, (x, y, w, h) in field_boxes.items():
            crop = image.crop((x, y, x + w, y + h))
            futures.append(executor.submit(recognize_single_field, name, crop, processor, model, generation_config))

        results = {}
        for future in as_completed(futures):
            name, text = future.result()
            results[name] = text

    form_type = request.form.get("formType", "default")
    corrected_results = postprocess_all(results, form_type=form_type)
    return jsonify(corrected_results)

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=6000, debug=True, use_reloader=False)
```

**python\ocr\postprocessing.py**

```
import json
import os
from functools import lru_cache
from rapidfuzz import process, fuzz

DICTIONARY_DIR = "../dictionaries"

@lru_cache(maxsize=32)
def load_dictionary(form_type: str) -> dict:
    path = os.path.join(DICTIONARY_DIR, f"{form_type}.json")
    try:
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)
    except FileNotFoundError:
        return {}

def fuzzy_correct(value, choices):
    result = process.extractOne(value, choices, scorer=fuzz.ratio)
    if result and len(result) >= 2:
        match, score = result[0], result[1]
        return match if score > 80 else value
    return value


def postprocess_all(results: dict, form_type: str = "default") -> dict:
    dictionary = load_dictionary(form_type)
    corrected = {}

    for field_name, value in results.items():
        values = dictionary.get(field_name)
        if isinstance(value, str) and isinstance(values, list):
            corrected[field_name] = fuzzy_correct(value, values)
        else:
            corrected[field_name] = value

    return corrected
```

**python\util\convert-to-png.py**

```
from pdf2image import convert_from_path

POPPLER_PATH = r"C:\poppler-24.08.0\Library\bin"

pages = convert_from_path(
    "../Hauptantrag_Buergergeld.pdf",
    dpi=300,
    poppler_path=POPPLER_PATH
)

pages[0].save("buergergeld_formular_blanko.png", "PNG")
```

**python\util\csv-in-json.py**

```
import csv
import json

cities = set()

import json

cities = set()

# Textdatei Zeile für Zeile lesen
with open("../csvs/orte.csv", encoding="utf-8") as f:
    for line in f:
        city = line.strip().strip('"')  # Entferne Zeilenumbruch und Anführungszeichen
        if city:
            cities.add(city)

# Wörterbuch aktualisieren
with open("../dictionaries/Buergergeld.json", "r+", encoding="utf-8") as f:
    data = json.load(f)
    data["wohnort"] = sorted(cities)
    f.seek(0)
    json.dump(data, f, indent=2, ensure_ascii=False)
    f.truncate()
```

**python\util\download_fhswf_model.py**

```
from transformers import TrOCRProcessor, VisionEncoderDecoderModel
import os

save_dir = "../models/fhswf_trocr"

os.makedirs(save_dir, exist_ok=True)

print("Lade Prozessor...")
processor = TrOCRProcessor.from_pretrained("fhswf/TrOCR_german_handwritten")
processor.save_pretrained(save_dir)

print("Lade Modell...")
model = VisionEncoderDecoderModel.from_pretrained("fhswf/TrOCR_german_handwritten")
model.save_pretrained(save_dir)

print("\n Inhalt des Modellverzeichnisses:")
for fname in os.listdir(save_dir):
    print(" -", fname)

expected_files = ["config.json", "pytorch_model.bin"]
missing = [f for f in expected_files if not os.path.exists(os.path.join(save_dir, f))]
if missing:
    print("\nFehlende Dateien:", missing)
    print("Der Download scheint unvollständig. Bitte prüfe deine Internetverbindung oder versuche es mit 'local_files_only=False'.")
else:
    print("\nAlle wichtigen Dateien vorhanden. Du kannst das Modell jetzt lokal verwenden.")
```

**python\util\generate_code_markdown.py**

```

import os
from pathlib import Path

INCLUDED_JAVA_SUBFOLDER = "src/main/java/com/github/ssalfelder/ocrformmate"
INCLUDED_PYTHON_SUBFOLDERS = [
    "python/ocr",
    "python/util",
    "python/handwriting_dataset/scripts"
]

EXCLUDED_FILES = {"__init__.py", "__main__.py"}

def generate_code_overview_markdown(java_root: Path, python_root: Path, output_path: Path):
    markdown_lines = []

    def add_code_section_markdown(header, file_path):
        markdown_lines.append(f"**{header}**\n")
        markdown_lines.append("```")
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
            markdown_lines.extend([line.rstrip() for line in file])
        markdown_lines.append("```")
        markdown_lines.append("")

    def collect_files(base_path: Path, extension: str, allowed_paths: list):
        collected = []
        for rel in allowed_paths:
            target_dir = base_path / Path(rel)
            for foldername, _, filenames in os.walk(target_dir):
                for filename in filenames:
                    if filename.endswith(extension) and filename not in EXCLUDED_FILES:
                        full_path = os.path.join(foldername, filename)
                        rel_path = os.path.relpath(full_path, base_path)
                        collected.append((rel_path, full_path))
        return collected

    java_files = collect_files(base_path=project_root, extension='.java', allowed_paths=[INCLUDED_JAVA_SUBFOLDER])
    python_files = collect_files(base_path=project_root, extension='.py', allowed_paths=INCLUDED_PYTHON_SUBFOLDERS)

    markdown_lines.append("# Java-Klassen")
    for rel_path, full_path in java_files:
        add_code_section_markdown(rel_path, full_path)

    markdown_lines.append("# Python-Skripte")
    for rel_path, full_path in python_files:
        add_code_section_markdown(rel_path, full_path)

    output_path.parent.mkdir(parents=True, exist_ok=True)
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write("\n".join(markdown_lines))


if __name__ == "__main__":
    script_dir = Path(__file__).resolve().parent
    project_root = script_dir.parent.parent  # OCRFormMate/
    java_root = project_root / "src"
    python_root = project_root / "python"
    output_file = python_root / "output" / "OCRFormMate_Code_Uebersicht.md"

    generate_code_overview_markdown(java_root, python_root, output_file)
    print(f"Markdown-Datei erstellt unter: {output_file}")
```

**python\util\plz-in-json.py**

```
import csv
import json

postleitzahlen = set()

with open("../csvs/postleitzahlen.csv", encoding="utf-8") as f:
    reader = csv.DictReader(f, delimiter=";")
    for row in reader:
        plz = row["PLZ"].strip()
        if plz:
            postleitzahlen.add(plz)

with open("../dictionaries/Buergergeld.json", "r+", encoding="utf-8") as f:
    data = json.load(f)
    data["postleitzahl"] = sorted(postleitzahlen)
    f.seek(0)
    json.dump(data, f, indent=2, ensure_ascii=False)
    f.truncate()

```

**python\util\vornamen-in-json.py**

```
import csv
import json

firstnames = set()

with open("../csvs/vornamen.csv", encoding="utf-8") as f:
    reader = csv.DictReader(f, delimiter=";")
    for row in reader:
        firstname = row["vorname"].strip()
        if firstname:
            firstnames.add(firstname)

with open("../dictionaries/vornamen.json", "w", encoding="utf-8") as f:
    json.dump(sorted(firstnames), f, indent=2, ensure_ascii=False)
```

**python\handwriting_dataset\scripts\combine_and_chunk.py**

```
import json
import random
import shutil
from pathlib import Path
from collections import defaultdict
from PIL import Image, ImageOps
from tqdm import tqdm
from concurrent.futures import ThreadPoolExecutor, as_completed

from handwriting_dataset.mappings.folder_to_label import FOLDER_TO_LABEL

BASE_DATASET_DIRS = [
    Path("E:/dataset"),
    Path("E:/dataset_augmented_chars"),
]

AUGMENTED_FOLDER_NAME = "dataset_augmented_chars"
OUTPUT_BASE = Path("E:/huggingface_datasets/handwriting_chunks")
EXAMPLES_PER_CLASS_PER_CHUNK = 100
MAX_CHUNKS = 25
FINAL_SIZE = (384, 384)

OUTPUT_BASE.mkdir(parents=True, exist_ok=True)
OUTPUT_IMAGES = OUTPUT_BASE / "images"
OUTPUT_IMAGES.mkdir(exist_ok=True)

def normalize_contrast(image: Image.Image) -> Image.Image:
    gray = image.convert("L")
    mean_pixel = sum(gray.getdata()) / (gray.width * gray.height)
    if mean_pixel < 127:
        gray = ImageOps.invert(gray)
    return gray.convert("RGB")

def prepare_image(image: Image.Image, size=(384, 384)) -> Image.Image:
    return ImageOps.pad(image, size, color="white", centering=(0.5, 0.5), method=Image.BICUBIC)

def generate_jsonls_if_missing(base_input: Path):
    for subdir in sorted(base_input.iterdir()):
        if not subdir.is_dir():
            continue

        image_files = list(subdir.glob("*.png"))
        if not image_files:
            continue

        jsonl_path = subdir / "dataset.jsonl"
        images_dir = subdir / "images"

        if jsonl_path.exists() and len(list(images_dir.glob("*.png"))) >= len(image_files):
            tqdm.write(f"Überspringe {subdir.name}, da bereits verarbeitet.")
            continue

        images_dir.mkdir(parents=True, exist_ok=True)

        folder_label = subdir.name
        true_label = FOLDER_TO_LABEL.get(folder_label, folder_label if len(folder_label) == 1 else None)
        if true_label is None:
            print(f"Kein Mapping für '{folder_label}' – übersprungen.")
            continue

        with open(jsonl_path, "w", encoding="utf-8") as f_jsonl:
            for i, image_path in enumerate(tqdm(image_files, desc=f"Autogen: {folder_label}")):
                try:
                    image = Image.open(image_path).convert("RGB")
                    if base_input.name != AUGMENTED_FOLDER_NAME:
                        image = normalize_contrast(image)
                    image = prepare_image(image)

                    new_name = f"{folder_label}_{i:05}.png"
                    image.save(images_dir / new_name)

                    f_jsonl.write(json.dumps({
                        "image": f"images/{new_name}",
                        "text": true_label
                    }, ensure_ascii=False) + "\n")
                except Exception as e:
                    print(f"Fehler bei {image_path}: {e}")

tqdm.write("Prüfe auf fehlende dataset.jsonl-Dateien")
for dataset_dir in BASE_DATASET_DIRS:
    generate_jsonls_if_missing(dataset_dir)

def process_jsonl_entry(entry, source_dir, is_augmented):
    try:
        label = entry["text"].split("TEXT:")[-1].strip()
        relative_path = Path(entry["image"]).relative_to("images")
        src_image_path = source_dir / relative_path

        base_stem = Path(entry["image"]).stem
        new_name = f"{base_stem}_{random.randint(1000, 9999)}.png"

        dest_image_path = OUTPUT_IMAGES / new_name

        if is_augmented:
            shutil.copy2(src_image_path, dest_image_path)
        else:
            with Image.open(src_image_path) as img:
                norm_img = normalize_contrast(img)
                norm_img.save(dest_image_path)

        entry["image"] = f"images/{new_name}"
        return label, entry
    except Exception as e:
        print(f"Fehler bei {entry.get('image', '???')}: {e}")
        return None

label_to_entries = defaultdict(list)

jsonl_paths = []
for base_dir in BASE_DATASET_DIRS:
    jsonl_paths.extend(base_dir.rglob("dataset.jsonl"))

tqdm.write("Verarbeite JSONLs parallel...")

with ThreadPoolExecutor(max_workers=8) as executor:
    futures = []
    for path in jsonl_paths:
        source_dir = path.parent / "images"
        is_augmented = AUGMENTED_FOLDER_NAME in str(path.resolve())
        with open(path, "r", encoding="utf-8") as f:
            for line in f:
                entry = json.loads(line)
                futures.append(executor.submit(process_jsonl_entry, entry, source_dir, is_augmented))

    for future in tqdm(as_completed(futures), total=len(futures), desc="Einträge kopieren"):
        result = future.result()
        if result is not None:
            label, processed_entry = result
            label_to_entries[label].append(processed_entry)

possible_chunks = min(len(entries) // EXAMPLES_PER_CLASS_PER_CHUNK for entries in label_to_entries.values())
num_chunks = min(possible_chunks, MAX_CHUNKS)
print(f"\nMaximal {num_chunks} vollständige Chunks möglich.")

for chunk_idx in range(1, num_chunks + 1):
    chunk_entries = []
    for label, entries in label_to_entries.items():
        if len(entries) >= EXAMPLES_PER_CLASS_PER_CHUNK:
            selected = random.sample(entries, EXAMPLES_PER_CLASS_PER_CHUNK)
        else:
            selected = []
        chunk_entries.extend(selected)
        label_to_entries[label] = [e for e in entries if e not in selected]

    random.shuffle(chunk_entries)
    chunk_path = OUTPUT_BASE / f"dataset_chunk_{chunk_idx:02}.jsonl"
    with open(chunk_path, "w", encoding="utf-8") as f_out:
        for entry in chunk_entries:
            f_out.write(json.dumps(entry, ensure_ascii=False) + "\n")

    print(f"Chunk {chunk_idx:02} gespeichert mit {len(chunk_entries)} Beispielen → {chunk_path}")

print("\nAlle Chunks erstellt.")
```

**python\handwriting_dataset\scripts\csv-extraction.py**

```
import os
import pandas as pd
import numpy as np
from PIL import Image
from tqdm import tqdm
import re

def clean_label_for_folder(label):
    return re.sub(r'[<>:"/\\|?*]', '_', label)

df = pd.read_csv('../../csvs/train-gocr.csv', low_memory=False)

print("Spaltennamen:", df.columns[:10])

LABEL_COLUMN = df.columns[0]
PIXEL_COLUMNS = df.columns[1:]

pixel_count = len(PIXEL_COLUMNS)
img_size = int(np.sqrt(pixel_count))
if img_size * img_size != pixel_count:
    raise ValueError(f"Bildgröße unklar – {pixel_count} Pixel ergeben kein Quadrat.")

print(f"Bildgröße erkannt: {img_size}x{img_size}")

output_root = 'D:/dataset_gocr3'
os.makedirs(output_root, exist_ok=True)

for idx, row in tqdm(df.iterrows(), total=len(df), desc="Speichere Bilder"):
    try:
        label = str(row[LABEL_COLUMN]).strip()

        if label.isupper():
            folder_name = clean_label_for_folder(label.lower() + label)
        else:
            folder_name = clean_label_for_folder(label)

        folder_path = os.path.join(output_root, folder_name)
        os.makedirs(folder_path, exist_ok=True)

        pixels = row[PIXEL_COLUMNS].astype(float).to_numpy().reshape((img_size, img_size))
        img = Image.fromarray(np.uint8(pixels), mode='L')  # 'L' = 8-bit-Graustufen
        img_path = os.path.join(folder_path, f'image_{idx}.png')
        img.save(img_path)

    except Exception as e:
        print(f"Fehler bei Zeile {idx}: {e}")
```

**python\handwriting_dataset\scripts\generate_characters.py**

```
import os
import random
import numpy as np
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageOps, ImageEnhance, ImageFilter
import json
from tqdm import tqdm

CHARACTERS = ["@", "-", "&", "€", "/", ".", ":", ",", "(", ")", "+", "*", "ß"]
IMAGES_PER_CHAR_AND_FONT = 500
FIELD_NAME = "Zeichen"
FIELD_SIZE = (128, 128)  # Initiale Leinwandgröße
FINAL_SIZE = (384, 384)  # Nach Padding/Skalierung

BASE_DIR = Path(__file__).resolve().parent.parent
BASE_OUTPUT_DIR = Path("E:/dataset_augmented_chars")
OUTPUT_DIR = BASE_OUTPUT_DIR / "images"
JSONL_PATH = BASE_OUTPUT_DIR / "dataset.jsonl"
FONT_DIR = BASE_DIR.parent / "handwriting-fonts"

# Maximale Rotation in Grad
ROTATION_RANGES = {
    "@": (-8, 8),
    "-": (-4, 4),
    "&": (-10, 10),
    "€": (-8, 8),
    "/": (-8, 8),
    ".": (-3, 3),
    ":": (-4, 4),
    ",": (-5, 5),
    "(": (-7, 7),
    ")": (-7, 7),
    "+": (-5, 5),
    "*": (-6, 6),
    "ß": (-8, 8),
    "default": (-7, 7)
}

# Max Verschiebung im Feld
PLACEMENT_JITTER_X = 5  # horizontal
PLACEMENT_JITTER_Y = 3  # vertikal

# Linien/Boxen
ADD_LINES_PROB = 0.6
ADD_BOX_PROB = 0.1
LINE_COLOR_RANGE = (190, 235)  # Grauwertbereich
LINE_WIDTH_RANGE = (1, 2)

# Helligkeit & Kontrast
BRIGHTNESS_RANGE = (0.7, 1.3)
CONTRAST_RANGE = (0.8, 1.2)
CONTRAST_PROB = 0.5

# Blur
GAUSSIAN_BLUR_PROB = 0.2
GAUSSIAN_BLUR_RADIUS_RANGE = (0.5, 1.2)

# Noise
GAUSSIAN_NOISE_PROB = 0.15
GAUSSIAN_NOISE_STD_RANGE = (3, 15)

# Shear
ADD_SHEAR_PROB = 0.15
MAX_SHEAR_FACTOR = 0.08

os.makedirs(OUTPUT_DIR, exist_ok=True)
os.makedirs(JSONL_PATH.parent, exist_ok=True)

def prepare_image(image: Image.Image, size=FINAL_SIZE) -> Image.Image:
    """
    Skaliert das Bild proportional und fügt Ränder hinzu, um die Zielgröße
    zu erreichen. Zentriert das Bild.
    """
    return ImageOps.pad(image, size, color="white", centering=(0.5, 0.5), method=Image.BICUBIC)

def add_gaussian_noise(image: Image.Image, std_range=GAUSSIAN_NOISE_STD_RANGE) -> Image.Image:
    """ Fügt Gauss'sches Rauschen zum Bild hinzu. """
    if image.mode != 'RGB':
        image = image.convert('RGB')
    np_img = np.array(image).astype(np.float32)
    std = random.uniform(*std_range)
    noise = np.random.normal(0, std, np_img.shape)
    noisy_img = np.clip(np_img + noise, 0, 255).astype(np.uint8)
    return Image.fromarray(noisy_img)

def apply_shear(image: Image.Image, max_shear_factor=MAX_SHEAR_FACTOR) -> Image.Image:
    """ Wendet eine leichte Scherung auf das Bild an. """
    shear_x = random.uniform(-max_shear_factor, max_shear_factor)
    # shear_y = random.uniform(-max_shear_factor, max_shear_factor) # Optionale Y-Scherung
    shear_y = 0
    matrix = (1, shear_x, 0, shear_y, 1, 0)
    return image.transform(image.size, Image.AFFINE, matrix, resample=Image.BICUBIC, fillcolor="white")


def safe_char_name(c):
    """ Erzeugt sichere Dateinamen für Zeichen. """
    return {
        "@": "at", "-": "minus", "&": "and", "€": "euro", "/": "slash",
        ".": "point", ":": "colon", ",": "comma", "(": "left_bracket",
        ")": "right_bracket", "+": "plus", "*": "asterisk"
    }.get(c, f"char_{ord(c)}")

fonts = list(FONT_DIR.glob("*.ttf"))
if not fonts:
    raise RuntimeError(f"Keine Fonts im Ordner gefunden: {FONT_DIR}")
print(f"{len(fonts)} Fonts gefunden.")


def realistic_augment(image: Image.Image, char: str) -> Image.Image:
    """
    Wendet eine Kette von Augmentierungen an, um realistischere
    Handschriftvariationen und Scan-Artefakte zu simulieren.
    """
    augmented_image = image.copy()  # Arbeite auf einer Kopie

    # Rotation
    rotation_range = ROTATION_RANGES.get(char, ROTATION_RANGES["default"])
    angle = random.uniform(*rotation_range)
    if angle != 0:
        augmented_image = augmented_image.rotate(angle, resample=Image.BICUBIC, fillcolor="white", expand=False)

    # Scherung
    if char not in ["-", ".", ",", ":"] and random.random() < ADD_SHEAR_PROB:
        augmented_image = apply_shear(augmented_image)

    # Helligkeit & Kontrast
    augmented_image = ImageEnhance.Brightness(augmented_image).enhance(random.uniform(*BRIGHTNESS_RANGE))
    if random.random() < CONTRAST_PROB:
        # print("Applying Contrast")
        augmented_image = ImageEnhance.Contrast(augmented_image).enhance(random.uniform(*CONTRAST_RANGE))

    # Blur
    if random.random() < GAUSSIAN_BLUR_PROB:
        radius = random.uniform(*GAUSSIAN_BLUR_RADIUS_RANGE)
        augmented_image = augmented_image.filter(ImageFilter.GaussianBlur(radius=radius))

    # Noise
    if random.random() < GAUSSIAN_NOISE_PROB:
        augmented_image = add_gaussian_noise(augmented_image)

    return augmented_image

counter = 0

print("Starte Bildgenerierung...")
with open(JSONL_PATH, "w", encoding="utf-8") as jsonl_file:
    for font_path in tqdm(fonts, desc="Fonts"):
        try:
            font_test_size = 50
            font = ImageFont.truetype(str(font_path), font_test_size)
        except OSError as e:
            print(f"WARNUNG: Konnte Font nicht laden: {font_path.name} - {e}. Überspringe.")
            continue

        for char in tqdm(CHARACTERS, desc="Characters", leave=False):
            for i in range(IMAGES_PER_CHAR_AND_FONT):
                try:
                    font_size = random.randint(44, 54)  # Leichte Variation der Schriftgröße
                    font = ImageFont.truetype(str(font_path), font_size)  # Font mit aktueller Größe laden

                    image = Image.new("RGB", FIELD_SIZE, color="white")
                    draw = ImageDraw.Draw(image)

                    try:
                        bbox = draw.textbbox((0, 0), char, font=font, anchor="lt")
                    except ValueError:
                        try:
                            tw = draw.textlength(char, font=font)
                            th = font_size  # Annäherung
                            bbox = (0, 0, tw, th)
                        except Exception:
                            print(
                                f"WARNUNG: Konnte BBox für '{char}' mit Font {font_path.name} nicht ermitteln. Überspringe.")
                            continue

                    text_width = bbox[2] - bbox[0]
                    text_height = bbox[3] - bbox[1]

                    # Position berechnen (Zentriert + Jitter)
                    base_x = (FIELD_SIZE[0] - text_width) // 2
                    base_y = (FIELD_SIZE[1] - text_height) // 2

                    # Platzierungs-Jitter hinzufügen
                    jitter_x = random.randint(-PLACEMENT_JITTER_X, PLACEMENT_JITTER_X)
                    jitter_y = random.randint(-PLACEMENT_JITTER_Y, PLACEMENT_JITTER_Y)

                    # Endgültige Posituib
                    draw_x = base_x + jitter_x
                    draw_y = base_y + jitter_y

                    draw_x = max(0, min(FIELD_SIZE[0] - text_width, draw_x))
                    draw_y = max(0, min(FIELD_SIZE[1] - text_height, draw_y))

                    draw.text((draw_x, draw_y - bbox[1]), char, font=font, fill="black",
                              anchor="lt")

                    augmented_image = realistic_augment(image, char)

                    draw_bg = ImageDraw.Draw(augmented_image)
                    img_w, img_h = augmented_image.size

                    # Grundlinie
                    if random.random() < ADD_LINES_PROB:

                        line_y_rel = bbox[3] * 0.9
                        abs_line_y = draw_y + line_y_rel
                        abs_line_y += random.uniform(-1, 1)

                        # Augmentierungsübertragung
                        line_y_final = min(img_h - 2, max(0, int(abs_line_y)))

                        line_x_start = max(0, draw_x - 5)  # Start linkslastig
                        line_x_end = min(img_w - 1, draw_x + text_width + 5)  # Ende rechtslastig
                        line_color_val = random.randint(*LINE_COLOR_RANGE)
                        line_color = (line_color_val,) * 3
                        line_width = random.randint(*LINE_WIDTH_RANGE)
                        if line_x_start < line_x_end:  # Gültigkeitsprüfung
                            draw_bg.line([(line_x_start, line_y_final), (line_x_end, line_y_final)],
                                         fill=line_color, width=line_width)

                        if random.random() < ADD_BOX_PROB:
                            box_margin = 3
                            box_color_val = random.randint(*LINE_COLOR_RANGE)
                            box_color = (box_color_val,) * 3
                            box_width = random.randint(1, 2)
                            corner_len = 5
                            # Oben-Links Ecke
                            x0, y0 = max(0, draw_x - box_margin), max(0, draw_y - bbox[1] - box_margin)
                            draw_bg.line([(x0, y0), (x0 + corner_len, y0)], fill=box_color, width=box_width)
                            draw_bg.line([(x0, y0), (x0, y0 + corner_len)], fill=box_color, width=box_width)
                            # Unten-Rechts Ecke
                            x1, y1 = min(img_w - 1, draw_x + text_width + box_margin), min(img_h - 1,
                                                                                           draw_y + text_height - bbox[
                                                                                               1] + box_margin)
                            draw_bg.line([(x1, y1), (x1 - corner_len, y1)], fill=box_color, width=box_width)
                            draw_bg.line([(x1, y1), (x1, y1 - corner_len)], fill=box_color, width=box_width)

                    # Abschluss Padding/Skalierung
                    final_image = prepare_image(augmented_image, size=FINAL_SIZE)

                    # Speicherung
                    image_filename = f"{FIELD_NAME}_{counter:06}.png"
                    char_folder_name = safe_char_name(char)
                    char_dir = OUTPUT_DIR / char_folder_name
                    os.makedirs(char_dir, exist_ok=True)
                    image_path = char_dir / image_filename

                    final_image.save(image_path)

                    jsonl_file.write(json.dumps({
                        "image": f"images/{char_folder_name}/{image_filename}",
                        "text": char
                    }, ensure_ascii=False) + "\n")

                    counter += 1

                except Exception as e:
                    print(f"\nFEHLER bei der Verarbeitung von '{char}' mit Font {font_path.name}, Index {i}: {e}")
                    import traceback

                    traceback.print_exc()
                    continue

print(f"\n{counter} Bilder generiert und gespeichert in '{OUTPUT_DIR}'")
print(f"JSONL-Datei gespeichert unter: {JSONL_PATH}")
```

**python\handwriting_dataset\scripts\image_preprocessing.py**

```

from PIL import Image, ImageOps

def prepare_image(image: Image.Image, size=(384, 384)) -> Image.Image:
    padded = ImageOps.pad(image, size, color="white", centering=(0.5, 0.5), method=Image.BICUBIC)
    return padded
```

**python\handwriting_dataset\scripts\upload_chunks_to_hf.py**

```
from datasets import load_dataset, Features, Value, Image
from pathlib import Path
import json

base_dir = Path("E:/huggingface_datasets/handwriting_chunks")
image_dir = base_dir / "images"


chunk_files = sorted(base_dir.glob("dataset_chunk_*.jsonl"))

features = Features({
    "image": Image(),
    "text": Value("string")
})

for chunk_file in chunk_files:
    chunk_name = chunk_file.stem
    chunk_index = int(chunk_name.split("_")[-1])

    print(f"\nVerarbeite {chunk_name}...")

    fixed_jsonl = base_dir / f"dataset_temp_{chunk_index:02}.jsonl"

    with open(chunk_file, "r", encoding="utf-8") as f_in, open(fixed_jsonl, "w", encoding="utf-8") as f_out:
        for line in f_in:
            entry = json.loads(line)
            image_path = image_dir / Path(entry["image"]).name
            entry["image"] = str(image_path.resolve())
            f_out.write(json.dumps(entry, ensure_ascii=False) + "\n")

    dataset = load_dataset(
        "json",
        data_files=str(fixed_jsonl),
        split="train",
        features=features
    )

    repo_name = f"sSalfelder/handwriting_sl_chunk{chunk_index}"
    dataset.push_to_hub(repo_name, private=True)

    print(f"{chunk_name} erfolgreich hochgeladen nach {repo_name}!")

print("\nAlle Chunks wurden erfolgreich hochgeladen!")
```

**python\handwriting_dataset\scripts\upload_model_to_hf.py**

```
from huggingface_hub import HfApi

model_path = "../../models/fhswf_trocr"


api = HfApi()

api.create_repo(
    repo_id="sSalfelder/formfelder_trocr",
    repo_type="model",
    private=True
)
api.upload_folder(
    folder_path=model_path,
    repo_id="sSalfelder/formfelder_trocr",
    repo_type="model"
)
```

**python\handwriting_dataset\scripts\handwriting_dataset\mappings\folder_to_label.py**

```
FOLDER_TO_LABEL = {
    "aA": "A",
    "aa": "A",
    "bB": "B",
    "bb": "B",
    "cC": "C",
    "cc": "C",
    "dD": "D",
    "dd": "D",
    "eE": "E",
    "ee": "E",
    "fF": "F",
    "ff": "F",
    "gG": "G",
    "gg": "G",
    "hH": "H",
    "hh": "H",
    "iI": "I",
    "ii": "I",
    "jJ": "J",
    "jj": "J",
    "kK": "K",
    "kk": "K",
    "lL": "L",
    "ll": "L",
    "mM": "M",
    "mm": "M",
    "nN": "N",
    "nn": "N",
    "oO": "O",
    "oo": "O",
    "pP": "P",
    "pp": "P",
    "qQ": "Q",
    "qq": "Q",
    "rR": "R",
    "rr": "R",
    "sS": "S",
    "ss": "S",
    "tT": "T",
    "tt": "T",
    "uU": "U",
    "uu": "U",
    "vV": "V",
    "vv": "V",
    "wW": "W",
    "ww": "W",
    "xX": "X",
    "xx": "X",
    "yY": "Y",
    "yy": "Y",
    "zZ": "Z",
    "zz": "Z",
    "sZ": "ß",
    "and": "&",
    "asterisk": "*",
    "at": "@",
    "colon": ":",
    "comma": ",",
    "euro": "€",
    "left_bracket": "(",
    "right_bracket": ")",
    "minus": "-",
    "plus": "+",
    "point": ".",
    "slash": "/"
}
```
