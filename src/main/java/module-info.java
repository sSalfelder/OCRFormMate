module com.github.ssalfelder.ocrformmate {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires spring.data.commons;
    requires spring.web;
    requires spring.beans;
    requires org.apache.tomcat.embed.core;
    requires jakarta.persistence;
    requires spring.boot.autoconfigure;
    requires spring.boot;

    opens com.github.ssalfelder.ocrformmate to javafx.fxml;
    exports com.github.ssalfelder.ocrformmate;
    exports com.github.ssalfelder.ocrformmate.controller;
    opens com.github.ssalfelder.ocrformmate.controller to javafx.fxml;
}