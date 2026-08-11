package BruhTato.Screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
        // NEW: Ensure global score property is initialized in FXGL
        if (!getWorldProperties().exists("score")) {
            set("score", 0);
        }

        healthText = getUIFactoryService().newText("HP: 100 / 100", Color.RED, 40.0);
        waveText = getUIFactoryService().newText("WAVE: 1", Color.GOLD, 30.0);
        scoreText = getUIFactoryService().newText("Score: 0", Color.GOLD, 30.0);

        // NEW: Listen for changes to global score property and update scoreText display
        getip("score").addListener((obs, oldVal, newVal) -> updateScore(newVal.intValue()));

        // Pass initialized FXGL nodes into VBox
        container = new VBox(8, healthText, waveText, scoreText);
        container.setPadding(new Insets(15));

        container.setTranslateX(10);
        container.setTranslateY(10);
    }

    public void attachToGame() {
        addUINode(container);
    }

    // NEW: Removes HUD overlay container from UI
    public void detachFromGame() {
        removeUINode(container);
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

    // NEW: Updates score UI display
    public void updateScore(int score) {
        if (scoreText != null) {
            scoreText.setText("Score: " + score);
        }
    }

    // MODIFIED: Added Main Menu return button and callback handling
    public void showGameOver(Runnable onReturnToMenu) {
        Text gameOverTitle = getUIFactoryService().newText("GAME OVER", Color.RED, 80.0);
        Text gameOverSubtext = getUIFactoryService().newText("You Were Defeated", Color.WHITE, 30.0);

        // NEW: Button to return player to the main menu
        Button mainMenuBtn = getUIFactoryService().newButton("MAIN MENU");
        mainMenuBtn.setStyle("-fx-font-size: 18px; -fx-min-width: 220px;");

        VBox gameOverBox = new VBox(15, gameOverTitle, gameOverSubtext, mainMenuBtn);
        gameOverBox.setAlignment(Pos.CENTER);

        // NEW: Action handler for Main Menu button
        mainMenuBtn.setOnAction(e -> {
            removeUINode(gameOverBox);
            detachFromGame();
            if (onReturnToMenu != null) {
                onReturnToMenu.run();
            }
        });

        // Center the VBox on screen using app dimensions
        double boxWidth = 500;
        double boxHeight = 250;
        gameOverBox.setPrefSize(boxWidth, boxHeight);
        gameOverBox.setTranslateX((getAppWidth() - boxWidth) / 2.0);
        gameOverBox.setTranslateY((getAppHeight() - boxHeight) / 2.0);

        addUINode(gameOverBox);
    }

    // NEW: Displays centered Victory screen with final score and Main Menu button
    public void showVictory(Runnable onReturnToMenu) {
        Text victoryTitle = getUIFactoryService().newText("VICTORY!", Color.GOLD, 80.0);
        Text victorySubtext = getUIFactoryService().newText("All Waves Cleared", Color.WHITE, 30.0);

        int finalScore = getWorldProperties().exists("score") ? geti("score") : 0;
        Text finalScoreText = getUIFactoryService().newText("Final Score: " + finalScore, Color.GOLD, 28.0);

        Button mainMenuBtn = getUIFactoryService().newButton("MAIN MENU");
        mainMenuBtn.setStyle("-fx-font-size: 18px; -fx-min-width: 220px;");

        VBox victoryBox = new VBox(15, victoryTitle, victorySubtext, finalScoreText, mainMenuBtn);
        victoryBox.setAlignment(Pos.CENTER);

        mainMenuBtn.setOnAction(e -> {
            removeUINode(victoryBox);
            detachFromGame();
            if (onReturnToMenu != null) {
                onReturnToMenu.run();
            }
        });

        double boxWidth = 500;
        double boxHeight = 300;
        victoryBox.setPrefSize(boxWidth, boxHeight);
        victoryBox.setTranslateX((getAppWidth() - boxWidth) / 2.0);
        victoryBox.setTranslateY((getAppHeight() - boxHeight) / 2.0);

        addUINode(victoryBox);
    }
}