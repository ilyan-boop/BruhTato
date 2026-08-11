package BruhTato.Player;

import BruhTato.Screens.HUD;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.Node;
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

    // Player death tracking flag
    private boolean isDead = false;

    // Invincibility frame (i-frame) parameters
    private boolean isInvincible = false;
    private final double INVINCIBILITY_DURATION = 2.0; // 2 Seconds

    private double borderDamageTimer = 0.0;
    private final double DAMAGE_INTERVAL = 1.0;
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
        if (isDead) return; // Block attacks when dead
        weaponComponent.swing();
    }

    private void tryMove(double dx, double dy) {
        if (isDead) return; // Block movement when dead

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
        if (isDead) return;

        if (isPushingBorder) {
            borderDamageTimer += tpf;
            if (borderDamageTimer >= DAMAGE_INTERVAL) {
                takeDamage(BORDER_DAMAGE);
                borderDamageTimer = 0.0;
            }
        } else {
            borderDamageTimer = 0.0;
        }
        isPushingBorder = false;
    }

    public void takeDamageWithKnockback(int amount, Point2D direction, double distance) {
        if (isDead || isInvincible) return; // Block damage & knockback if dead or invincible

        takeDamage(amount);
        applyKnockback(direction, distance);
    }

    public void applyKnockback(Point2D direction, double distance) {
        if (isDead) return;

        double dx = direction.getX() * distance;
        double dy = direction.getY() * distance;

        entity.translateX(dx);
        entity.translateY(dy);

        boolean hitsBorder = getGameWorld()
                .getEntitiesByType(EntityType.BORDER)
                .stream()
                .anyMatch(entity::isColliding);

        if (hitsBorder) {
            entity.translateX(-dx);
            entity.translateY(-dy);
        }
    }

    public void takeDamage(int amount) {
        if (isDead || isInvincible) return; // Block damage if dead or invincible

        currentHealth = Math.max(0, currentHealth - amount);
        updateHUD();

        // MODIFIED: Deduct score when taking damage (ensuring score doesn't drop below 0)
        if (getWorldProperties().exists("score")) {
            int currentScore = geti("score");
            set("score", Math.max(0, currentScore - amount));
        }

        // Update player texture visual based on health state (Full -> Half -> Dead)
        updateHealthVisual();

        if (currentHealth <= 0) {
            die();
            return;
        }

        // Trigger Invincibility State for non-lethal damage
        isInvincible = true;

        // Visual Effects: Red Flash + Opacity Blink
        ColorAdjust redFlash = new ColorAdjust();
        redFlash.setHue(-0.005);
        redFlash.setSaturation(1.0);
        entity.getViewComponent().getParent().setEffect(redFlash);
        entity.getViewComponent().setOpacity(0.5);

        // Remove red tint flash after 0.15s
        runOnce(() -> {
            entity.getViewComponent().getParent().setEffect(null);
        }, javafx.util.Duration.seconds(0.15));

        // Expire invincibility and restore opacity after 2.0 seconds
        runOnce(() -> {
            if (!isDead) {
                isInvincible = false;
                entity.getViewComponent().setOpacity(1.0);
            }
        }, javafx.util.Duration.seconds(INVINCIBILITY_DURATION));
    }

    // Handles death state, disables collisions, and displays Game Over screen
    private void die() {
        isDead = true;

        // Disable collision component so enemies pass through dead player
        entity.getComponentOptional(CollidableComponent.class).ifPresent(c -> c.setValue(false));

        // Display Game Over overlay on HUD
        if (hud != null) {
            hud.showGameOver();
        }

        System.out.println("Player Defeated!");
    }

    // Updates player sprite texture between Full, Half, and Dead states safely
    private void updateHealthVisual() {
        if (currentHealth <= 0) {
            try {
                Texture deadTexture = texture("Bruhtato_Dead.png", 200, 200);
                updateEntityTexture(deadTexture);
            } catch (Exception e) {
                // Fallback tint if Bruhtato_Dead.png asset is missing
                ColorAdjust deadTint = new ColorAdjust();
                deadTint.setSaturation(-1.0); // Desaturate
                if (entity.getViewComponent().getParent() != null) {
                    entity.getViewComponent().getParent().setEffect(deadTint);
                }
            }
        } else if (currentHealth <= maxHealth / 2) {
            try {
                Texture halfTexture = texture("Bruhtato_HalfHealth.png", 200, 200);
                updateEntityTexture(halfTexture);
            } catch (Exception e) {
                // Fallback tint if Bruhtato_HalfHealth.png asset is missing
                ColorAdjust damagedTint = new ColorAdjust();
                damagedTint.setBrightness(-0.3);
                if (entity.getViewComponent().getParent() != null) {
                    entity.getViewComponent().getParent().setEffect(damagedTint);
                }
            }
        }
    }

    private void updateEntityTexture(Texture newTexture) {
        boolean textureUpdated = false;
        for (Node node : entity.getViewComponent().getChildren()) {
            if (node instanceof Texture) {
                Texture t = (Texture) node;
                t.setImage(newTexture.getImage());
                textureUpdated = true;
                break;
            }
        }
        if (!textureUpdated) {
            entity.getViewComponent().addChild(newTexture);
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
    public boolean isDead() { return isDead; }
}