module org.example.bruhtato {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;

    opens BruhTato to javafx.fxml;
    exports BruhTato;
}