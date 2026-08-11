package BruhTato.Utils;

import BruhTato.Screens.HUD;
import BruhTato.Utils.EntityType;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class WaveManager {

    private final HUD hud;
    // NEW: Callback reference to return to Main Menu
    private final Runnable onReturnToMenu;
    private int currentWave = 0;
    private final int maxWaves = 3;
    private boolean waveInProgress = false;

    // Difficulty configuration (Easy mode defaults)
    private final int[] waveEnemyCounts = {3, 6, 9};

    // MODIFIED: Updated constructor to accept onReturnToMenu Runnable
    public WaveManager(HUD hud, Runnable onReturnToMenu) {
        this.hud = hud;
        this.onReturnToMenu = onReturnToMenu;
    }

    // NEW: Convenience constructor for backward compatibility
    public WaveManager(HUD hud) {
        this(hud, null);
    }

    public void startWaves() {
        currentWave = 0;
        nextWave();
    }

    public void nextWave() {
        if (currentWave >= maxWaves) {
            triggerVictory();
            return;
        }

        currentWave++;
        waveInProgress = true;

        if (hud != null) {
            hud.updateWave(currentWave);
        }

        int countToSpawn = waveEnemyCounts[currentWave - 1];
        spawnWaveEnemies(countToSpawn);
    }

    // MODIFIED: Spawns enemies only along the inner screen borders
    private void spawnWaveEnemies(int count) {
        double enemyWidth = 150.0;
        double enemyHeight = 150.0;
        double margin = 110.0; // Margin offset near border

        double minX = margin;
        double maxX = getAppWidth() - margin - enemyWidth;
        double minY = margin;
        double maxY = getAppHeight() - margin - enemyHeight;

        for (int i = 0; i < count; i++) {
            Point2D spawnPos = getRandomBorderPosition(minX, maxX, minY, maxY);
            spawn("enemy", spawnPos.getX(), spawnPos.getY());
        }
    }

    // NEW: Calculates random coordinates along top, bottom, left, or right border edges
    private Point2D getRandomBorderPosition(double minX, double maxX, double minY, double maxY) {
        int side = random(0, 3); // 0: Top, 1: Bottom, 2: Left, 3: Right

        return switch (side) {
            case 0 -> new Point2D(random(minX, maxX), minY);              // Top Border
            case 1 -> new Point2D(random(minX, maxX), maxY);              // Bottom Border
            case 2 -> new Point2D(minX, random(minY, maxY));              // Left Border
            default -> new Point2D(maxX, random(minY, maxY));             // Right Border
        };
    }

    public void onUpdate(double tpf) {
        if (!waveInProgress) return;

        // Check if all active enemies are defeated
        long activeEnemies = getGameWorld().getEntitiesByType(EntityType.ENEMY).stream()
                .filter(e -> e.isActive())
                .count();

        if (activeEnemies == 0) {
            waveInProgress = false;
            // Delay 2 seconds before launching next wave
            runOnce(this::nextWave, Duration.seconds(2.0));
        }
    }

    // MODIFIED: Displays victory screen via HUD instead of console output
    private void triggerVictory() {
        if (hud != null) {
            hud.showVictory(onReturnToMenu);
        } else {
            System.out.println("All waves cleared!");
        }
    }

    public int getCurrentWave() {
        return currentWave;
    }
}