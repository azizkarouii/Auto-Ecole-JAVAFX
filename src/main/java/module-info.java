module tn.spring.autoecole {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires org.controlsfx.controls;

    opens tn.spring.autoecole to javafx.fxml;
    exports tn.spring.autoecole;

    opens tn.spring.autoecole.controllers to javafx.fxml;
    exports tn.spring.autoecole.controllers;

    opens tn.spring.autoecole.models to javafx.base;
    exports tn.spring.autoecole.models;
    exports tn.spring.autoecole.models.enums;
    opens tn.spring.autoecole.models.enums to javafx.base;
}