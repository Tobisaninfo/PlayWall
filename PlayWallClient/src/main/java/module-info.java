module de.tobias.playwall.playwall {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens de.tobias.playwall to javafx.fxml;
    exports de.tobias.playwall;
}