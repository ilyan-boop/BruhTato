module BruhTato{
    requires javafx.controls;
    requires com.almasb.fxgl.all;

    opens BruhTato to com.almasb.fxgl.core;
    opens BruhTato.Items to com.almasb.fxgl.core;
    opens BruhTato.Player to com.almasb.fxgl.core;
    opens BruhTato.Screens to com.almasb.fxgl.core;
    opens BruhTato.Utils to com.almasb.fxgl.core;
    opens assets.textures;
    opens assets.sounds;

    exports BruhTato;
}