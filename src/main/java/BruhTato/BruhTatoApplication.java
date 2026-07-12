package BruhTato;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

public class BruhTatoApplication extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("BruhTato Game");
        settings.setVersion("1.0");
    }

    @Override
    protected void initGame() {
    }
}