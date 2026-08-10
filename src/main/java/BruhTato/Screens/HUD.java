package BruhTato.Screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

    // Renders centered Game Over message on the screen
    public void showGameOver() {
        Text gameOverTitle = getUIFactoryService().newText("GAME OVER", Color.RED, 80.0);
        Text gameOverSubtext = getUIFactoryService().newText("You Were Defeated", Color.WHITE, 30.0);

        VBox gameOverBox = new VBox(15, gameOverTitle, gameOverSubtext);
        gameOverBox.setAlignment(Pos.CENTER);

        // Center the VBox on screen using app dimensions
        double boxWidth = 500;
        double boxHeight = 200;
        gameOverBox.setPrefSize(boxWidth, boxHeight);
        gameOverBox.setTranslateX((getAppWidth() - boxWidth) / 2.0);
        gameOverBox.setTranslateY((getAppHeight() - boxHeight) / 2.0);

        addUINode(gameOverBox);
    }
}