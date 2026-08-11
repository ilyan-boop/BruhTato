package BruhTato.Screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.*;

public class MainMenu {

    private final VBox menuContainer;

    public MainMenu(Runnable onStartGame) {
        menuContainer = new VBox(20);
        menuContainer.setAlignment(Pos.CENTER);
        menuContainer.setPadding(new Insets(30));

        // Center menu container on screen
        double width = 450;
        double height = 450;
        menuContainer.setPrefSize(width, height);
        menuContainer.setTranslateX((getAppWidth() - width) / 2.0);
        menuContainer.setTranslateY((getAppHeight() - height) / 2.0);

        showTitleMenu(onStartGame);
    }

    // Step 1: Initial Start Menu
    private void showTitleMenu(Runnable onStartGame) {
        menuContainer.getChildren().clear();

        Text titleText = getUIFactoryService().newText("BRUHTATO", Color.GOLD, 64.0);

        Button startBtn = getUIFactoryService().newButton("START");
        startBtn.setStyle("-fx-font-size: 20px; -fx-min-width: 220px;");
        startBtn.setOnAction(e -> showDifficultyMenu(onStartGame));

        Button exitBtn = getUIFactoryService().newButton("EXIT");
        exitBtn.setStyle("-fx-font-size: 20px; -fx-min-width: 220px;");
        exitBtn.setOnAction(e -> getGameController().exit());

        menuContainer.getChildren().addAll(titleText, startBtn, exitBtn);
    }

    // Step 2: Difficulty Selection Menu
    private void showDifficultyMenu(Runnable onStartGame) {
        menuContainer.getChildren().clear();

        Text difficultyTitle = getUIFactoryService().newText("SELECT DIFFICULTY", Color.WHITE, 36.0);

        Button easyBtn = getUIFactoryService().newButton("EASY");
        easyBtn.setStyle("-fx-font-size: 18px; -fx-min-width: 220px;");
        easyBtn.setOnAction(e -> launchGame(onStartGame));

        Button mediumBtn = getUIFactoryService().newButton("MEDIUM");
        mediumBtn.setStyle("-fx-font-size: 18px; -fx-min-width: 220px;");
        mediumBtn.setOnAction(e -> launchGame(onStartGame));

        Button hardBtn = getUIFactoryService().newButton("HARD");
        hardBtn.setStyle("-fx-font-size: 18px; -fx-min-width: 220px;");
        hardBtn.setOnAction(e -> launchGame(onStartGame));

        Button backBtn = getUIFactoryService().newButton("BACK");
        backBtn.setStyle("-fx-font-size: 16px; -fx-min-width: 220px;");
        backBtn.setOnAction(e -> showTitleMenu(onStartGame));

        menuContainer.getChildren().addAll(difficultyTitle, easyBtn, mediumBtn, hardBtn, backBtn);
    }

    // Removes the UI menu overlay and triggers gameplay initialization
    private void launchGame(Runnable onStartGame) {
        removeUINode(menuContainer);
        if (onStartGame != null) {
            onStartGame.run();
        }
    }

    public void attachToUI() {
        addUINode(menuContainer);
    }
}