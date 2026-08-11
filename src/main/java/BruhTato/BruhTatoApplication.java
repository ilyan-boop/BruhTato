package BruhTato;

import BruhTato.Utils.GameFactory;
import BruhTato.Screens.MainMenu;
import BruhTato.Utils.BorderFactory;
import BruhTato.Player.Player;
import BruhTato.Screens.HUD;
import BruhTato.Utils.WaveManager;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
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
        // MODIFIED: Added null checks so inputs are ignored before the player is spawned
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

        // MODIFIED: Deferred spawning borders, player, and enemy until after difficulty selection
    }

    @Override
    protected void initUI() {
        // MODIFIED: Show only the Main Menu initially
        MainMenu menu = new MainMenu(() -> {
            // Callback executed when any difficulty is selected
            System.out.println("Starting Game...");
            startGame();
        });

        menu.attachToUI();
    }

    // NEW: Handles initialization of game entities and HUD after difficulty selection
    private void startGame() {
        BorderFactory.spawnScreenBorders();

        player = new Player();

        double centerX = getAppWidth() / 2.0;
        double centerY = getAppHeight() / 2.0;
        spawn("enemy", centerX, centerY);

        hud = new HUD();
        hud.attachToGame();

        // Link HUD to player for health bar updates
        if (player != null) {
            player.attachHUD(hud);
        }

        waveManager = new WaveManager(hud);
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
}