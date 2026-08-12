package BruhTato.Utils;

import BruhTato.Items.ItemType;
import BruhTato.Screens.HUD;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class WaveManager {

    private final HUD hud;
    private final Runnable onReturnToMenu;
    private int currentWave = 0;
    private int maxWaves = 3;
    private boolean waveInProgress = false;
    private boolean isBossSpawning = false;

    // Timer for periodic spawns during Wave 4
    private double wave4SpawnTimer = 0.0;
    private final double SPAWN_INTERVAL = 10.0;

    private String difficulty = "easy";
    private int[] waveEnemyCounts = {3, 6, 9};
    private int[] waveWizardCounts = {0, 0, 0};

    public WaveManager(HUD hud, Runnable onReturnToMenu) {
        this.hud = hud;
        this.onReturnToMenu = onReturnToMenu;
    }

    public WaveManager(HUD hud) {
        this(hud, null);
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty.toLowerCase();
        switch (this.difficulty) {
            case "medium" -> {
                maxWaves = 3;
                waveEnemyCounts = new int[]{6, 9, 12};
                waveWizardCounts = new int[]{1, 2, 4};
            }
            case "hard" -> {
                maxWaves = 4;
                waveEnemyCounts = new int[]{9, 12, 15, 1};
                waveWizardCounts = new int[]{2, 4, 6, 0};
            }
            default -> { // Easy
                maxWaves = 3;
                waveEnemyCounts = new int[]{3, 6, 9};
                waveWizardCounts = new int[]{0, 0, 0};
            }
        }
    }

    public void startWaves() {
        currentWave = 0;
        isBossSpawning = false;
        wave4SpawnTimer = 0.0;
        nextWave();
    }

    public void nextWave() {
        if (currentWave >= maxWaves) {
            triggerVictory();
            return;
        }

        currentWave++;
        waveInProgress = true;
        wave4SpawnTimer = 0.0;

        if (hud != null) {
            hud.updateWave(currentWave);
        }

        int countToSpawn = waveEnemyCounts[currentWave - 1];
        spawnWaveEnemies(countToSpawn);
        spawnWaveItems();
    }

    private void spawnWaveItems() {
        if (difficulty.equals("medium") || difficulty.equals("hard")) {
            getGameWorld().getEntitiesByType(EntityType.ITEM).forEach(Entity::removeFromWorld);
        }

        spawnRandomWeapon();

        if (difficulty.equals("hard")) {
            ItemType consumable = (currentWave >= 2 && random(0, 1) == 1) ? ItemType.SHIELD : ItemType.HEALTH;
            spawnItemInArena(consumable);
        } else {
            spawnItemInArena(ItemType.HEALTH);

            if (currentWave >= 2) {
                spawnItemInArena(ItemType.SHIELD);
            }
        }
    }

    private void spawnRandomWeapon() {
        ItemType[] weapons = {ItemType.SWORD, ItemType.SPEAR, ItemType.AXE};
        ItemType weaponType = weapons[random(0, weapons.length - 1)];
        spawnItemInArena(weaponType);
    }

    private void spawnRandomWizard() {
        double enemyWidth = 150.0;
        double enemyHeight = 150.0;
        double margin = 110.0;

        double minX = margin;
        double maxX = getAppWidth() - margin - enemyWidth;
        double minY = margin;
        double maxY = getAppHeight() - margin - enemyHeight;

        Point2D wizardSpawnPos = getRandomBorderPosition(minX, maxX, minY, maxY);
        spawn("wizardEnemy", wizardSpawnPos.getX(), wizardSpawnPos.getY());
    }

    private void spawnItemInArena(ItemType itemType) {
        double minX = 300.0;
        double maxX = getAppWidth() - 300.0;
        double minY = 250.0;
        double maxY = getAppHeight() - 250.0;

        spawnItemAtRandomLocation(itemType, minX, maxX, minY, maxY);
    }

    private void spawnItemAtRandomLocation(ItemType itemType, double minX, double maxX, double minY, double maxY) {
        double spawnX = random(minX, maxX);
        double spawnY = random(minY, maxY);

        spawn("item", new SpawnData(spawnX, spawnY).put("itemType", itemType));
    }

    private void spawnWaveEnemies(int totalCount) {
        // Boss spawn logic on Wave 4 of Hard difficulty
        if (difficulty.equals("hard") && currentWave == 4) {
            double bossWidth = 300.0;
            double bossHeight = 300.0;
            double bossMargin = 160.0;

            double bossMinX = bossMargin;
            double bossMaxX = getAppWidth() - bossMargin - bossWidth;
            double bossMinY = bossMargin;
            double bossMaxY = getAppHeight() - bossMargin - bossHeight;

            Point2D bossSpawnPos = getRandomBorderPosition(bossMinX, bossMaxX, bossMinY, bossMaxY);

            showBossSpawnWarningPrompt(bossSpawnPos);
            return;
        }

        double enemyWidth = 150.0;
        double enemyHeight = 150.0;
        double margin = 110.0;

        double minX = margin;
        double maxX = getAppWidth() - margin - enemyWidth;
        double minY = margin;
        double maxY = getAppHeight() - margin - enemyHeight;

        int wizardsToSpawn = waveWizardCounts[currentWave - 1];
        int meleeToSpawn = Math.max(0, totalCount - wizardsToSpawn);

        for (int i = 0; i < wizardsToSpawn; i++) {
            Point2D wizardSpawnPos = getRandomBorderPosition(minX, maxX, minY, maxY);
            spawn("wizardEnemy", wizardSpawnPos.getX(), wizardSpawnPos.getY());
        }

        for (int i = 0; i < meleeToSpawn; i++) {
            Point2D spawnPos = getRandomBorderPosition(minX, maxX, minY, maxY);
            spawn("enemy", spawnPos.getX(), spawnPos.getY());
        }
    }

    private void showBossSpawnWarningPrompt(Point2D spawnPos) {
        isBossSpawning = true;
        double radius = 150.0;

        Circle warningShape = new Circle(radius, radius, radius, Color.rgb(255, 0, 0, 0.45));
        warningShape.setStroke(Color.DARKRED);
        warningShape.setStrokeWidth(4.0);

        Entity warningPrompt = entityBuilder()
                .at(spawnPos.getX(), spawnPos.getY())
                .view(warningShape)
                .buildAndAttach();

        runOnce(() -> {
            if (warningPrompt.isActive()) {
                warningPrompt.getViewComponent().setOpacity(0.15);
            }
        }, Duration.seconds(0.3));

        runOnce(() -> {
            if (warningPrompt.isActive()) {
                warningPrompt.getViewComponent().setOpacity(1.0);
            }
        }, Duration.seconds(0.6));

        runOnce(() -> {
            if (warningPrompt.isActive()) {
                warningPrompt.getViewComponent().setOpacity(0.15);
            }
        }, Duration.seconds(0.9));

        runOnce(() -> {
            if (warningPrompt.isActive()) {
                warningPrompt.getViewComponent().setOpacity(1.0);
            }
        }, Duration.seconds(1.2));

        runOnce(() -> {
            if (warningPrompt.isActive()) {
                warningPrompt.removeFromWorld();
            }
            spawn("bossEnemy", spawnPos.getX(), spawnPos.getY());
            isBossSpawning = false;
        }, Duration.seconds(1.5));
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
        if (!waveInProgress || isBossSpawning) return;

        // Periodic weapon and wizard spawns every 10 seconds during Wave 4
        if (currentWave == 4) {
            wave4SpawnTimer += tpf;
            if (wave4SpawnTimer >= SPAWN_INTERVAL) {
                wave4SpawnTimer = 0.0;
                spawnRandomWeapon();
                spawnRandomWizard();
            }
        }

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