package BruhTato;

import BruhTato.Utils.GameFactory;
import BruhTato.Screens.MainMenu;
import BruhTato.Utils.BorderFactory;
import BruhTato.Player.Player;
import BruhTato.Screens.HUD;
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
        showMainMenu();
    }

    // NEW: Instantiates and attaches Main Menu UI
    private void showMainMenu() {
        MainMenu menu = new MainMenu(() -> {
            // Callback executed when any difficulty is selected
            System.out.println("Starting Game...");
            startGame();
        });

        menu.attachToUI();
    }

    // NEW: Clears world entities, resets game state, and shows Main Menu UI
    private void returnToMainMenu() {
        // Despawn all active entities in world (Player, Enemies, Borders)
        getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);

        player = null;
        waveManager = null;
        hud = null;

        // Reset global score property
        if (getWorldProperties().exists("score")) {
            set("score", 0);
        }

        showMainMenu();
    }

    // NEW: Handles initialization of game entities and HUD after difficulty selection
    // MODIFIED: Removed static test enemy spawn and attached returnToMainMenu callback
    private void startGame() {
        BorderFactory.spawnScreenBorders();

        player = new Player(this::returnToMainMenu);

        hud = new HUD();
        hud.attachToGame();

        // Link HUD to player for health bar updates
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
}