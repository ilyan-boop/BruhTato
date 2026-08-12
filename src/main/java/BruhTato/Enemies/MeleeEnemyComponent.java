package BruhTato.Enemies;

import BruhTato.Player.Player;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class MeleeEnemyComponent extends BaseEnemyComponent {

    private double speed; // Fixed per-tick step matching Player movement

    private int contactDamage = 10;
    private double knockbackDistance = 50.0;

    // Enemy Health & State tracking (Takes up to 3 hits)
    private int maxHealth = 3;
    private int currentHealth = 3;
    private boolean isDead = false;

    // Despawn fade timer configuration
    private double fadeTimer = 0.0;
    private final double FADE_DURATION = 1.0; // Seconds to fade out after dying

    public MeleeEnemyComponent(double speed) {
        this.speed = speed;
    }

    public MeleeEnemyComponent() {
        this(3.5);
    }

    @Override
    public void onUpdate(double tpf) {
        // If dead, freeze AI movement, disable contact damage, and handle fade out
        if (isDead) {
            fadeTimer += tpf;

            // Gradually reduce opacity until completely invisible
            double currentOpacity = entity.getViewComponent().getOpacity();
            double newOpacity = Math.max(0.0, currentOpacity - (tpf / FADE_DURATION));
            entity.getViewComponent().setOpacity(newOpacity);

            // Permanently remove entity from world once fully faded out
            if (fadeTimer >= FADE_DURATION || newOpacity <= 0) {
                entity.removeFromWorld();
            }
            return; // Stop processing movement/attacks while dying
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

                // Calculate separation force away from nearby active enemies to prevent merging
                Point2D separation = computeSeparation();
                Point2D moveVector = direction.add(separation);

                if (moveVector.magnitude() > 0) {
                    moveVector = moveVector.normalize();
                }

                // Fixed step movement (prevents frame delta slowdown)
                double dx = moveVector.getX() * speed;
                double dy = moveVector.getY() * speed;
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

    // Calculates a push vector away from neighboring living enemies to avoid stacking
    private Point2D computeSeparation() {
        Point2D separation = new Point2D(0, 0);
        double minDistance = 110.0; // Distance threshold to trigger body bumping/separation

        for (Entity other : FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY)) {
            if (other == entity) continue;

            // Ignore dead/fading enemies
            MeleeEnemyComponent otherComp = other.getComponentOptional(MeleeEnemyComponent.class).orElse(null);
            if (otherComp != null && otherComp.isDead()) continue;

            double dist = entity.getCenter().distance(other.getCenter());
            if (dist < minDistance) {
                Point2D pushDir;
                if (dist > 0.001) {
                    pushDir = entity.getCenter().subtract(other.getCenter()).normalize();
                } else {
                    // Pick a random direction to split enemies spawned on the exact same coordinate
                    pushDir = new Point2D(random(-1.0, 1.0), random(-1.0, 1.0)).normalize();
                }
                // Push force increases as enemies get closer to each other
                double force = (minDistance - dist) / minDistance;
                separation = separation.add(pushDir.multiply(force * 1.5));
            }
        }
        return separation;
    }

    // Method called when the player attacks this enemy (Enemy has NO i-frames; damage applies immediately)
    public void takeDamage(int damage) {
        if (isDead) return; // Ignore hits if already dead

        currentHealth -= damage;

        // Trigger visual red damage flash
        triggerRedFlash();

        if (currentHealth == 2 || currentHealth == 1) {
            // Switch to Half Health state visual
            updateToHalfHealthVisual();
        } else if (currentHealth <= 0) {
            // Switch to Dead state and initiate despawn fade
            die();
        }
    }

    // Displays a temporary red flash filter on the enemy sprite upon taking damage
    private void triggerRedFlash() {
        if (entity == null || entity.getViewComponent() == null) return;

        ColorAdjust redFlash = new ColorAdjust();
        redFlash.setHue(-0.005);
        redFlash.setSaturation(1.0);

        if (entity.getViewComponent().getParent() != null) {
            entity.getViewComponent().getParent().setEffect(redFlash);
        }

        // Restore default appearance after 0.15s
        runOnce(() -> {
            if (!isDead && entity.getViewComponent().getParent() != null) {
                entity.getViewComponent().getParent().setEffect(null);
            }
        }, Duration.seconds(0.15));
    }

    // Updates visual texture to half health state
    private void updateToHalfHealthVisual() {
        try {
            Texture halfTexture = texture("Melee_Enemy_HalfHealth.png", 150, 150);
            updateEntityTexture(halfTexture);
        } catch (Exception e) {
            ColorAdjust damagedTint = new ColorAdjust();
            damagedTint.setBrightness(-0.3); // Darken image to indicate half health
            if (entity.getViewComponent().getParent() != null) {
                entity.getViewComponent().getParent().setEffect(damagedTint);
            }
        }
    }

    // Triggers death state, disables collisions, and updates texture
    private void die() {
        play("enemy_death.wav");
        isDead = true;

        // Award 100 points to score on enemy defeat
        if (getWorldProperties().exists("score")) {
            inc("score", 100);
        } else {
            set("score", 100);
        }

        // Disable collision component so player and attacks pass through dead body
        entity.getComponentOptional(CollidableComponent.class).ifPresent(c -> c.setValue(false));

        try {
            Texture deadTexture = texture("Melee_Enemy_Dead.png", 150, 150);
            updateEntityTexture(deadTexture);
        } catch (Exception e) {
            ColorAdjust deadTint = new ColorAdjust();
            deadTint.setHue(-0.8);
            deadTint.setSaturation(0.0); // Desaturate to gray/dead
            if (entity.getViewComponent().getParent() != null) {
                entity.getViewComponent().getParent().setEffect(deadTint);
            }
        }
    }

    // Swaps underlying Texture image without invoking clearChildren(), preserving FXGL dev tool debug hitbox overlays
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