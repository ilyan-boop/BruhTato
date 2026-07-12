package BruhTato;

import BruhTato.Player.Player;
import BruhTato.Items.GameFactory;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.input.KeyCode;
import java.util.Random;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BruhTatoApplication extends GameApplication {

    private Player player;
    private GameFactory factory;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("Bruhtato");
        settings.setVersion("1.0");
    }

    @Override
    protected void initInput() {
        // Direct key mapping hookups
        onKey(KeyCode.W, () -> player.moveUp());
        onKey(KeyCode.S, () -> player.moveDown());
        onKey(KeyCode.A, () -> player.moveLeft());
        onKey(KeyCode.D, () -> player.moveRight());
    }

    @Override
    protected void initGame() {
        factory = new GameFactory();

        // Disable top-down world gravity
        getPhysicsWorld().setGravity(0, 0);

        // Spawn our player
        player = new Player();

        // Spawn random obstacles
        Random rand = new Random();
        for (int i = 0; i < 8; i++) {
            double w = rand.nextInt(60) + 40;
            double h = rand.nextInt(60) + 40;
            double x = rand.nextInt(700) + 50;
            double y = rand.nextInt(500) + 50;

            // Simple boundary buffer check to prevent trapping player at spawn (100, 100)
            if (x < 200 && y < 200) {
                i--;
                continue;
            }

            factory.spawnObstacle(x, y, w, h);
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        // Friction baseline: Reset velocities so player stops when keys are released
        if (player != null) {
            player.stop();
        }
    }
}