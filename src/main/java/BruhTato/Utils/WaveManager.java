package BruhTato.Utils;

import BruhTato.Items.ItemType;
import BruhTato.Screens.HUD;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class WaveManager {

    private final HUD hud;
    private final Runnable onReturnToMenu;
    private int currentWave = 0;
    private final int maxWaves = 3;
    private boolean waveInProgress = false;

    // Difficulty configuration (Defaults to Easy)
    private int[] waveEnemyCounts = {3, 6, 9};

    public WaveManager(HUD hud, Runnable onReturnToMenu) {
        this.hud = hud;
        this.onReturnToMenu = onReturnToMenu;
    }

    public WaveManager(HUD hud) {
        this(hud, null);
    }

    public void setDifficulty(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "medium" -> waveEnemyCounts = new int[]{6, 9, 12};
            case "hard" -> waveEnemyCounts = new int[]{9, 12, 15};
            default -> waveEnemyCounts = new int[]{3, 6, 9};
        }
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
        spawnWaveItems();
    }

    // Spawns weapons and pickup items inside the playable field for the current wave
    private void spawnWaveItems() {
        double minX = 300.0;
        double maxX = getAppWidth() - 300.0;
        double minY = 250.0;
        double maxY = getAppHeight() - 250.0;

        // Select a random weapon type on each wave spawn
        ItemType[] weapons = {ItemType.SWORD, ItemType.SPEAR, ItemType.AXE};
        ItemType weaponType = weapons[random(0, weapons.length - 1)];

        spawnItemAtRandomLocation(weaponType, minX, maxX, minY, maxY);

        // Spawn consumable items (Health & Shield pickups)
        spawnItemAtRandomLocation(ItemType.HEALTH, minX, maxX, minY, maxY);

        if (currentWave >= 2) {
            spawnItemAtRandomLocation(ItemType.SHIELD, minX, maxX, minY, maxY);
        }
    }

    private void spawnItemAtRandomLocation(ItemType itemType, double minX, double maxX, double minY, double maxY) {
        double spawnX = random(minX, maxX);
        double spawnY = random(minY, maxY);

        spawn("item", new SpawnData(spawnX, spawnY).put("itemType", itemType));
    }

    private void spawnWaveEnemies(int count) {
        double enemyWidth = 150.0;
        double enemyHeight = 150.0;
        double margin = 110.0;

        double minX = margin;
        double maxX = getAppWidth() - margin - enemyWidth;
        double minY = margin;
        double maxY = getAppHeight() - margin - enemyHeight;

        for (int i = 0; i < count; i++) {
            Point2D spawnPos = getRandomBorderPosition(minX, maxX, minY, maxY);
            spawn("enemy", spawnPos.getX(), spawnPos.getY());
        }
    }

    private Point2D getRandomBorderPosition(double minX, double maxX, double minY, double maxY) {
        int side = random(0, 3);

        return switch (side) {
            case 0 -> new Point2D(random(minX, maxX), minY);
            case 1 -> new Point2D(random(minX, maxX), maxY);
            case 2 -> new Point2D(minX, random(minY, maxY));
            default -> new Point2D(maxX, random(minY, maxY));
        };
    }

    public void onUpdate(double tpf) {
        if (!waveInProgress) return;

        long activeEnemies = getGameWorld().getEntitiesByType(EntityType.ENEMY).stream()
                .filter(e -> e.isActive())
                .count();

        if (activeEnemies == 0) {
            waveInProgress = false;
            runOnce(this::nextWave, Duration.seconds(2.0));
        }
    }

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