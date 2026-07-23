package BruhTato.Screens;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.*;

public class HUD {

    private final Text healthText;
    private final Text waveText;
    private final Text scoreText;
    private final VBox container;

    public HUD() {

        healthText = getUIFactoryService().newText("HP: 100 / 100", Color.RED, 40.0);
        waveText = getUIFactoryService().newText("WAVE: 1", Color.GOLD, 30.0);
        scoreText = getUIFactoryService().newText("Score: 0", Color.GOLD, 30.0);

        // Pass initialized FXGL nodes into VBox
        container = new VBox(8, healthText, waveText, scoreText);
        container.setPadding(new Insets(15));

        container.setTranslateX(10);
        container.setTranslateY(10);
    }

    public void attachToGame() {
        addUINode(container);
    }

    public void updateHealth(int currentHP, int maxHP) {
        if (healthText != null) {
            healthText.setText("HP: " + currentHP + " / " + maxHP);
        }
    }

    public void updateWave(int waveNumber) {
        if (waveText != null) {
            waveText.setText("WAVE: " + waveNumber);
        }
    }
}