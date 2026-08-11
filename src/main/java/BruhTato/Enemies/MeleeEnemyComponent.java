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

import static com.almasb.fxgl.dsl.FXGL.*;

public class MeleeEnemyComponent extends Component {

    private double speed; // Fixed per-tick step matching Player movement

    private int contactDamage = 50;
    private double knockbackDistance = 50.0;

    // Enemy Health & State tracking (Takes up to 3 hits)
    private int maxHealth = 3;
    private int currentHealth = 3;
    private boolean isDead = false;

    // Despawn/Respawn fade timer configuration
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

            // Trigger respawn once fully faded out
            if (fadeTimer >= FADE_DURATION || newOpacity <= 0) {
                respawn();
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

    // Method called when the player attacks this enemy (Enemy has NO i-frames; damage applies immediately)
    public void takeDamage(int damage) {
        if (isDead) return; // Ignore hits if already dead

        currentHealth -= damage;

        if (currentHealth == 2 || currentHealth == 1) {
            // Switch to Half Health state visual
            updateToHalfHealthVisual();
        } else if (currentHealth <= 0) {
            // Switch to Dead state and initiate despawn fade
            die();
        }
    }

    // NEW: Resets enemy state and teleports to a random location on screen
    private void respawn() {
        isDead = false;
        fadeTimer = 0.0;
        currentHealth = maxHealth;

        // Re-enable collision component so the player and attacks hit the enemy again
        entity.getComponentOptional(CollidableComponent.class).ifPresent(c -> c.setValue(true));

        // Restore full opacity and clear any tint effects
        entity.getViewComponent().setOpacity(1.0);
        if (entity.getViewComponent().getParent() != null) {
            entity.getViewComponent().getParent().setEffect(null);
        }

        // Restore default full-health texture
        try {
            Texture fullTexture = texture("Melee_Enemy_FullHealth.png", 150, 150);
            updateEntityTexture(fullTexture);
        } catch (Exception e) {
            // Fallback: If Melee_Enemy.png asset name differs, effect clearing above restores baseline visual
        }

        // Generate random coordinates within viewport bounds
        double margin = 100.0;
        double minX = margin;
        double maxX = Math.max(margin, getAppWidth() - margin - 150);
        double minY = margin;
        double maxY = Math.max(margin, getAppHeight() - margin - 150);

        double randomX = random(minX, maxX);
        double randomY = random(minY, maxY);

        // Reposition enemy entity
        entity.setPosition(randomX, randomY);
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
        isDead = true;

        // NEW: Award 100 points to score on enemy defeat
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