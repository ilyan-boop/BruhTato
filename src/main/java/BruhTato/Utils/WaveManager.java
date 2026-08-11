package BruhTato.Utils;

import BruhTato.Screens.HUD;
import BruhTato.Utils.EntityType;
import javafx.util.Duration;
import static com.almasb.fxgl.dsl.FXGL.*;


public class WaveManager {
    private final HUD hud;
    private int currentWave = 0;
    private final int maxWaves = 3;
    private boolean waveInProgress = false;

    // Difficulty configuration (Easy mode defaults)
    private final int[] waveEnemyCounts = {3, 6, 9}; // Wave 1: 3, Wave 2: 6, Wave 3: 9

    public WaveManager(HUD hud) {
        this.hud = hud;
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

    private void spawnWaveEnemies(int count) {
        double margin = 150.0;
        double minX = margin;
        double maxX = Math.max(margin, getAppWidth() - margin - 150);
        double minY = margin;
        double maxY = Math.max(margin, getAppHeight() - margin - 150);

        for (int i = 0; i < count; i++) {
            double randomX = random(minX, maxX);
            double randomY = random(minY, maxY);
            spawn("enemy", randomX, randomY);
        }
    }

    public void onUpdate(double tpf) {
        if (!waveInProgress) return;

        // Check if all enemies in the current wave are defeated
        long activeEnemies = getGameWorld().getEntitiesByType(EntityType.ENEMY).stream()
                .filter(e -> e.isActive())
                .count();

        if (activeEnemies == 0) {
            waveInProgress = false;
            // Delay 2 seconds before launching next wave
            runOnce(this::nextWave, Duration.seconds(2.0));
        }
    }

    private void triggerVictory() {
        System.out.println("All waves cleared!");
        // Optional: Trigger a Victory screen or HUD message here
    }

    public int getCurrentWave() {
        return currentWave;
    }
}
