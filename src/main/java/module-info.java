module BruhTato {
    requires javafx.controls;
    requires com.almasb.fxgl.all;

    // Open the exact package namespaces pointing to your capitalized folders
    opens BruhTato to com.almasb.fxgl.core;
    opens BruhTato.Player to com.almasb.fxgl.core;
    opens BruhTato.Items to com.almasb.fxgl.core;
    opens BruhTato.Screens to com.almasb.fxgl.core;
    opens BruhTato.Utils to com.almasb.fxgl.core;
    opens BruhTato.Enemies to com.almasb.fxgl.core;

    exports BruhTato;
}