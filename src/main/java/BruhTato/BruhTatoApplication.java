package BruhTato;

import BruhTato.Player.Player;
import BruhTato.Screens.HUD;
import BruhTato.Screens.MainMenu;
import BruhTato.Utils.BorderFactory;
import BruhTato.Utils.GameFactory;
import BruhTato.Utils.WaveManager;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BruhTatoApplication extends GameApplication {

    private Player player;
    private HUD hud;
    private WaveManager waveManager;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1920);
        settings.setHeight(1080);
        settings.setTitle("Bruhtato");
        settings.setVersion("1.2");
        settings.setDeveloperMenuEnabled(true);
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.W, () -> { if (player != null) player.moveUp(); });
        onKey(KeyCode.S, () -> { if (player != null) player.moveDown(); });
        onKey(KeyCode.A, () -> { if (player != null) player.moveLeft(); });
        onKey(KeyCode.D, () -> { if (player != null) player.moveRight(); });

        onBtnDown(MouseButton.PRIMARY, () -> { if (player != null) player.attack(); });
    }

    @Override
    protected void initGame() {
        getGameScene().setBackgroundColor(Color.web("#796b86"));
        getPhysicsWorld().setGravity(0, 0);

        getGameWorld().addEntityFactory(new GameFactory());
    }

    @Override
    protected void initUI() {
        showMainMenu();
    }

    private void showMainMenu() {
        MainMenu menu = new MainMenu(difficulty -> {
            System.out.println("Starting Game with difficulty: " + difficulty);
            startGame(difficulty);
        });

        menu.attachToUI();
    }

    private void returnToMainMenu() {
        getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);

        player = null;
        waveManager = null;
        hud = null;

        if (getWorldProperties().exists("score")) {
            set("score", 0);
        }

        showMainMenu();
    }

    private void startGame(String difficulty) {
        BorderFactory.spawnScreenBorders();

        player = new Player(this::returnToMainMenu);
        player.setDifficulty(difficulty);

        hud = new HUD();
        hud.attachToGame();

        if (player != null) {
            player.attachHUD(hud);
        }

        waveManager = new WaveManager(hud, this::returnToMainMenu);
        waveManager.startWaves();
    }

    @Override
    protected void onUpdate(double tpf) {
        if (player != null) {
            player.updateBorderDamage(tpf);
        }

        if (waveManager != null) {
            waveManager.onUpdate(tpf);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}