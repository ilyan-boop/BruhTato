package BruhTato.Enemies;

import BruhTato.Player.Player;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import javafx.geometry.Point2D;
import javafx.scene.effect.ColorAdjust;

import static com.almasb.fxgl.dsl.FXGL.*;

public class EnemyComponent extends Component {

    private double speed = 3.5; // Fixed per-tick step matching Player movement

    private int contactDamage = 10;
    private double knockbackDistance = 50.0;

    // NEW: Enemy Health & State tracking (Takes up to 3 hits)
    private int maxHealth = 3;
    private int currentHealth = 3;
    private boolean isDead = false;

    // NEW: Despawn fade timer configuration
    private double fadeTimer = 0.0;
    private final double FADE_DURATION = 1.0; // Seconds to fade out after dying

    public EnemyComponent(double speed) {
        this.speed = speed;
    }

    public EnemyComponent() {
        this(3.5);
    }

    @Override
    public void onUpdate(double tpf) {
        // NEW: If dead, freeze AI movement, disable contact damage, and handle fade out
        if (isDead) {
            fadeTimer += tpf;

            // Gradually reduce opacity until completely invisible
            double currentOpacity = entity.getViewComponent().getOpacity();
            double newOpacity = Math.max(0.0, currentOpacity - (tpf / FADE_DURATION));
            entity.getViewComponent().setOpacity(newOpacity);

            // Despawn from game world once fully faded
            if (fadeTimer >= FADE_DURATION || newOpacity <= 0) {
                entity.removeFromWorld();
            }
            return; // Stop processing movement/attacks
        }

        Entity playerEntity = FXGL.getGameWorld()
                .getSingletonOptional(EntityType.PLAYER)
                .orElse(null);

        if (playerEntity != null) {
            Point2D playerCenter = playerEntity.getCenter();
            Point2D enemyCenter = entity.getCenter();

            Point2D direction = playerCenter.subtract(enemyCenter);
            if (direction.magnitude() > 1.0) {
                direction = direction.normalize();

                // Fixed step movement (prevents frame delta slowdown)
                double dx = direction.getX() * speed;
                double dy = direction.getY() * speed;
                tryMove(dx, dy);
            }

            // Contact collision detection
            if (entity.isColliding(playerEntity)) {
                Player player = playerEntity.getObject("playerRef");
                if (player != null) {
                    Point2D knockbackDir = playerCenter.subtract(enemyCenter);
                    if (knockbackDir.magnitude() == 0) {
                        knockbackDir = new Point2D(1, 0);
                    } else {
                        knockbackDir = knockbackDir.normalize();
                    }

                    // Apply damage & knockback (Player class evaluates 2-second i-frames)
                    player.takeDamageWithKnockback(contactDamage, knockbackDir, knockbackDistance);
                }
            }
        }
    }

    // NEW: Method called when the player attacks this enemy
    public void takeDamage(int damage) {
        if (isDead) return; // Ignore hits if already dead

        currentHealth -= damage;

        if (currentHealth == 2 || currentHealth == 1) {
            // NEW: Switch to Half Health state visual
            updateToHalfHealthVisual();
        } else if (currentHealth <= 0) {
            // NEW: Switch to Dead state and initiate despawn fade
            die();
        }
    }

    // NEW: Updates visual texture to half health state
    private void updateToHalfHealthVisual() {
        try {
            // Replaces view with Half Health sprite if asset exists
            entity.getViewComponent().clearChildren();
            entity.getViewComponent().addChild(texture("Melee_Enemy_HalfHealth.png", 150, 150));
        } catch (Exception e) {
            // Fallback visual tint if Enemy_Half.png is not in assets yet
            ColorAdjust damagedTint = new ColorAdjust();
            damagedTint.setBrightness(-0.3); // Darken image to indicate half health
            entity.getViewComponent().getParent().setEffect(damagedTint);
        }
    }

    // NEW: Triggers death state, disables collisions, and updates texture
    private void die() {
        isDead = true;

        // Disable collision component so player and attacks pass through dead body
        entity.getComponentOptional(CollidableComponent.class).ifPresent(c -> c.setValue(false));

        try {
            // Replaces view with Dead sprite if asset exists
            entity.getViewComponent().clearChildren();
            entity.getViewComponent().addChild(texture("Melee_Enemy_Dead.png", 150, 150));
        } catch (Exception e) {
            // Fallback visual tint if Enemy_Dead.png is not in assets yet
            ColorAdjust deadTint = new ColorAdjust();
            deadTint.setHue(-0.8);
            deadTint.setSaturation(0.0); // Desaturate to gray/dead
            entity.getViewComponent().getParent().setEffect(deadTint);
        }
    }

    private void tryMove(double dx, double dy) {
        entity.translateX(dx);
        entity.translateY(dy);

        boolean hitsBorder = FXGL.getGameWorld()
                .getEntitiesByType(EntityType.BORDER)
                .stream()
                .anyMatch(entity::isColliding);

        if (hitsBorder) {
            entity.translateX(-dx);
            entity.translateY(-dy);
        }
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setContactDamage(int contactDamage) {
        this.contactDamage = contactDamage;
    }

    public boolean isDead() {
        return isDead;
    }
}