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

}