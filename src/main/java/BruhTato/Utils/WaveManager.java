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
}
