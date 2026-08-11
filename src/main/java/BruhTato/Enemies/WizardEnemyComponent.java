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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class WizardEnemyComponent extends BaseEnemyComponent{

    private int spellDamage = 20;
    private double spellRadius = 100.0;
    private double castCooldown = 3.0; // Seconds between spell casts
    private double castTimer = 0.0;
    private boolean isCasting = false;

    // Enemy Health & State tracking
    private int currentHealth = 2;
    private boolean isDead = false;

    // Despawn fade timer configuration
    private double fadeTimer = 0.0;
    private final double FADE_DURATION = 1.0;

    @Override
    public void onUpdate(double tpf) {
        // If dead, handle despawn fade out
        if (isDead) {
            fadeTimer += tpf;
            double currentOpacity = entity.getViewComponent().getOpacity();
            double newOpacity = Math.max(0.0, currentOpacity - (tpf / FADE_DURATION));
            entity.getViewComponent().setOpacity(newOpacity);

            if (fadeTimer >= FADE_DURATION || newOpacity <= 0) {
                entity.removeFromWorld();
            }
            return;
        }

        // Wizard stands still and charges its spell on a timer
        if (!isCasting) {
            castTimer += tpf;
            if (castTimer >= castCooldown) {
                castTimer = 0.0;
                castSpell();
            }
        }
    }

    // Casts a spell circle centered on the player's location that flashes 3 times
    private void castSpell() {
        Entity playerEntity = FXGL.getGameWorld()
                .getSingletonOptional(EntityType.PLAYER)
                .orElse(null);

        if (playerEntity == null) return;

        isCasting = true;
        Point2D targetPos = playerEntity.getCenter();

        // Set circle center to (spellRadius, spellRadius) within its bounding box
        Circle warningCircle = new Circle(spellRadius, spellRadius, spellRadius);
        warningCircle.setFill(Color.rgb(255, 0, 0, 0.35));
        warningCircle.setStroke(Color.RED);
        warningCircle.setStrokeWidth(3);

        Entity spellEntity = entityBuilder()
                .at(targetPos.getX() - spellRadius, targetPos.getY() - spellRadius)
                .view(warningCircle)
                .buildAndAttach();

        // Flash 1 (On at 0.0s -> Off at 0.3s)
        runOnce(() -> spellEntity.getViewComponent().setOpacity(0.1), Duration.seconds(0.3));

        // Flash 2 (On at 0.6s -> Off at 0.9s)
        runOnce(() -> spellEntity.getViewComponent().setOpacity(1.0), Duration.seconds(0.6));
        runOnce(() -> spellEntity.getViewComponent().setOpacity(0.1), Duration.seconds(0.9));

        // Flash 3 / Impact (On at 1.2s -> Deal Damage -> Remove)
        runOnce(() -> {
            spellEntity.getViewComponent().setOpacity(1.0);
            warningCircle.setFill(Color.rgb(255, 0, 0, 0.75)); // Intensify red on hit

            // Check if player is inside spell radius during final flash
            Entity currentPlayer = FXGL.getGameWorld()
                    .getSingletonOptional(EntityType.PLAYER)
                    .orElse(null);

            if (currentPlayer != null) {
                double distance = currentPlayer.getCenter().distance(targetPos);
                if (distance <= spellRadius) {
                    Player player = currentPlayer.getObject("playerRef");
                    if (player != null) {
                        Point2D knockbackDir = currentPlayer.getCenter().subtract(targetPos);
                        if (knockbackDir.magnitude() == 0) knockbackDir = new Point2D(1, 0);
                        else knockbackDir = knockbackDir.normalize();

                        player.takeDamageWithKnockback(spellDamage, knockbackDir, 40.0);
                    }
                }
            }
        }, Duration.seconds(1.2));

        // Clean up spell circle and reset cast state
        runOnce(() -> {
            spellEntity.removeFromWorld();
            isCasting = false;
        }, Duration.seconds(1.5));
    }

    public void takeDamage(int damage) {
        if (isDead) return;

        currentHealth -= damage;
        triggerRedFlash();

        if (currentHealth == 2 || currentHealth == 1) {
            updateToHalfHealthVisual();
        } else if (currentHealth <= 0) {
            die();
        }
    }

    private void triggerRedFlash() {
        if (entity == null || entity.getViewComponent() == null) return;

        ColorAdjust redFlash = new ColorAdjust();
        redFlash.setHue(-0.12);
        redFlash.setSaturation(0.8);

        if (entity.getViewComponent().getParent() != null) {
            entity.getViewComponent().getParent().setEffect(redFlash);
        }

        runOnce(() -> {
            if (!isDead && entity.getViewComponent().getParent() != null) {
                entity.getViewComponent().getParent().setEffect(null);
            }
        }, Duration.seconds(0.15));
    }

    private void updateToHalfHealthVisual() {
        try {
            Texture halfTexture = texture("Wizard_Enemy_HalfHealth.png", 150, 150);
            updateEntityTexture(halfTexture);
        } catch (Exception e) {
            ColorAdjust damagedTint = new ColorAdjust();
            damagedTint.setBrightness(-0.3);
            if (entity.getViewComponent().getParent() != null) {
                entity.getViewComponent().getParent().setEffect(damagedTint);
            }
        }
    }

    private void die() {
        isDead = true;

        if (getWorldProperties().exists("score")) {
            inc("score", 150);
        } else {
            set("score", 150);
        }

        entity.getComponentOptional(CollidableComponent.class).ifPresent(c -> c.setValue(false));

        try {
            Texture deadTexture = texture("Wizard_Enemy_Dead.png", 150, 150);
            updateEntityTexture(deadTexture);
        } catch (Exception e) {
            ColorAdjust deadTint = new ColorAdjust();
            deadTint.setHue(-0.8);
            deadTint.setSaturation(0.0);
            if (entity.getViewComponent().getParent() != null) {
                entity.getViewComponent().getParent().setEffect(deadTint);
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

    public boolean isDead() {
        return isDead;
    }
}