module org.example.bruhtato {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;

    opens org.example.bruhtato to javafx.fxml;
    exports org.example.bruhtato;
}