package BruhTato.Screens;

import BruhTato.Player.Player;
import BruhTato.Items.GameFactory;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.input.KeyCode;
import java.util.Random;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameScreen extends GameApplication {

    private Player player;
    private GameFactory factory;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("FXGL Architecture Game");
        settings.setVersion("1.0");
    }

    @Override
    protected void initInput() {
        // Direct WASD mapping. FXGL runs this every single frame automatically.
        onKey(KeyCode.W, () -> player.moveUp());
        onKey(KeyCode.S, () -> player.moveDown());
        onKey(KeyCode.A, () -> player.moveLeft());
        onKey(KeyCode.D, () -> player.moveRight());
    }

    @Override
    protected void initGame() {
        factory = new GameFactory();

        // 1. Built-in Screen Borders (Player cannot walk off screen)
        getPhysicsWorld().setGravity(0, 0); // Top-down game, no gravity!

        // 2. Spawn Player
        player = new Player();

        // 3. Generate Random Obstacles
        Random rand = new Random();
        for (int i = 0; i < 8; i++) {
            double w = rand.nextInt(60) + 40;
            double h = rand.nextInt(60) + 40;
            double x = rand.nextInt(700) + 50;
            double y = rand.nextInt(500) + 50;

            // Avoid spawning on initial player area
            if (x < 200 && y < 200) { i--; continue; }

            factory.spawnObstacle(x, y, w, h);
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        // Friction baseline: stop player when keys aren't held down
        player.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
