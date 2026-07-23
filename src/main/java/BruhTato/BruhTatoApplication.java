package BruhTato;

import BruhTato.Player.Player;
import BruhTato.Items.GameFactory;
import BruhTato.Screens.HUD;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.input.UserAction;
import javafx.scene.input.KeyCode;
import java.util.Random;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BruhTatoApplication extends GameApplication {

    private Player player;
    private GameFactory factory;
    private HUD hud;

    @Override
    protected void initSettings(GameSettings settings) {
        //RESOLUTION \/
        settings.setWidth(1920);
        settings.setHeight(1080);
        settings.setTitle("Bruhtato");
        settings.setVersion("1.0");

        settings.setDeveloperMenuEnabled(true);
    }

    @Override
    protected void initInput() {
        // onKey automatically executes move() every tick while key is down
        onKey(KeyCode.W, () -> player.moveUp());
        onKey(KeyCode.S, () -> player.moveDown());
        onKey(KeyCode.A, () -> player.moveLeft());
        onKey(KeyCode.D, () -> player.moveRight());
    }

    @Override
    protected void initGame() {
        final int OBSTACLE_COUNT = 10;
        factory = new GameFactory();

        // Disable top-down world gravity
        getPhysicsWorld().setGravity(0, 0);

        // Spawn our player
        player = new Player();

        // Spawn random obstacles
        Random rand = new Random();
        for (int i = 0; i < OBSTACLE_COUNT; i++) {
            double w = 100;
            double h = 100;
            double x = rand.nextInt(1820)+100;
            double y = rand.nextInt(980)+100;

            // Simple boundary buffer check to prevent trapping player at spawn (100, 100)
            if (x < 200 && y < 200) {
                i--;
                continue;
            }

            factory.spawnObstacle(x, y, w, h);
        }

        //SIDE BORDERS
        factory.spawnObstacle(-50,0,1,1080);
        factory.spawnObstacle(1970,0,1,1080);

        //TOP BOTTOM BORDERS
        factory.spawnObstacle(0,-50,1920,1);
        factory.spawnObstacle(0,1130,1920,1);


    }

    @Override
    protected void onUpdate(double tpf) {
    }

    @Override
    protected void initUI(){
        hud = new HUD();
        hud.attachToGame();
    }
}