module com.github.ssalfelder.ocrformmate {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens com.github.ssalfelder.ocrformmate to javafx.fxml;
    exports com.github.ssalfelder.ocrformmate;
    exports com.github.ssalfelder.ocrformmate.controller;
    opens com.github.ssalfelder.ocrformmate.controller to javafx.fxml;
}