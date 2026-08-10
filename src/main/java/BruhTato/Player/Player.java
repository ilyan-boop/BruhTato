package BruhTato.Player;

import BruhTato.Screens.HUD;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.effect.ColorAdjust;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Player {

    private final Entity entity;
    private final WeaponComponent weaponComponent;
    private static final double SPEED = 3.5;
    private HUD hud;

    private final int maxHealth = 100;
    private int currentHealth = 100;
    private boolean isPushingBorder = false;


    private double borderDamageTimer = 0.0;
    private final double DAMAGE_INTERVAL = 1.0; // Every 1.0 seconds
    private final int BORDER_DAMAGE = 5;

    public Player() {
        weaponComponent = new WeaponComponent();

        entity = entityBuilder()
                .type(EntityType.PLAYER)
                .at(100, 100)
                .bbox(new HitBox(new Point2D(50, 25), BoundingShape.circle(50)))
                .bbox(new HitBox(new Point2D(50, 80), BoundingShape.circle(50)))
                .with(weaponComponent)
                .with(new CollidableComponent(true))
                .with("playerRef", this)
                .view(texture("Bruhtato_FullHealth.png", 200, 200))
                .buildAndAttach();
    }

    public void attachHUD(HUD hud) {
        this.hud = hud;
        updateHUD();
    }

    public void attack() {
        weaponComponent.swing();
    }


    private void tryMove(double dx, double dy) {
        entity.translateX(dx);
        entity.translateY(dy);


        boolean hitsBorder = getGameWorld()
                .getEntitiesByType(EntityType.BORDER)
                .stream()
                .anyMatch(entity::isColliding);

        if (hitsBorder) {
            isPushingBorder = true;

            entity.translateX(-dx);
            entity.translateY(-dy);
        }
    }

    public void updateBorderDamage(double tpf) {

        if (isPushingBorder) {
            borderDamageTimer += tpf;

            if (borderDamageTimer >= DAMAGE_INTERVAL) {
                takeDamage(BORDER_DAMAGE);
                borderDamageTimer = 0.0; // Reset 1-second interval
            }
        } else {
            borderDamageTimer = 0.0;
        }

        isPushingBorder = false;
    }

    public void takeDamage(int amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        updateHUD();

        // --- Red Flash Visual Effect ---
        ColorAdjust redFlash = new ColorAdjust();
        redFlash.setHue(-0.005);      // Shift hue to red spectrum
        redFlash.setSaturation(1.0); // Boost color intensity

        // Apply effect to the entity view
        entity.getViewComponent().getParent().setEffect(redFlash);

        // Remove the flash effect after 0.15 seconds
        runOnce(() -> {
            entity.getViewComponent().getParent().setEffect(null);
        }, javafx.util.Duration.seconds(0.15));
        // -------------------------------

        if (currentHealth <= 0) {
            System.out.println("Player Defeated!");
        }
    }

    private void updateHUD() {
        if (hud != null) {
            hud.updateHealth(currentHealth, maxHealth);
        }
    }

    public void moveUp() { tryMove(0, -SPEED); }
    public void moveDown() { tryMove(0, SPEED); }
    public void moveLeft() { tryMove(-SPEED, 0); }
    public void moveRight() { tryMove(SPEED, 0); }

    public Entity getEntity() { return entity; }
    public int getCurrentHealth() { return currentHealth; }
}