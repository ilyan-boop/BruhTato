package BruhTato;

import BruhTato.Items.GameFactory;
import BruhTato.Utils.BorderFactory;
import BruhTato.Player.Player;
import BruhTato.Screens.HUD;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BruhTatoApplication extends GameApplication {

    private Player player;
    private HUD hud;

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
        onKey(KeyCode.W, () -> player.moveUp());
        onKey(KeyCode.S, () -> player.moveDown());
        onKey(KeyCode.A, () -> player.moveLeft());
        onKey(KeyCode.D, () -> player.moveRight());

        onBtnDown(MouseButton.PRIMARY, () -> player.attack());
    }

    @Override
    protected void initGame() {
        getPhysicsWorld().setGravity(0, 0);

        getGameWorld().addEntityFactory(new GameFactory());

        BorderFactory.spawnScreenBorders();

        player = new Player();

        double centerX = getAppWidth() / 2.0;
        double centerY = getAppHeight() / 2.0;
        spawn("enemy", centerX, centerY);
    }

    @Override
    protected void initUI() {
        hud = new HUD();
        hud.attachToGame();

        // Link HUD to player for health bar updates
        if (player != null) {
            player.attachHUD(hud);
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        if (player != null) {
            player.updateBorderDamage(tpf);
        }
    }
}