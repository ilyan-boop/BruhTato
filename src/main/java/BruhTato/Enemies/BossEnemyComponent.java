package BruhTato.Enemies;

import BruhTato.Player.Player;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BossEnemyComponent extends BaseEnemyComponent {

    private double speed = 2.0;
    private int contactDamage = 15;
    private double knockbackDistance = 60.0;

    // Boss Health & State tracking
    private final int maxHealth = 50;
    private int currentHealth = 50;
    private boolean isDead = false;

    // Despawn fade configuration
    private double fadeTimer = 0.0;
    private final double FADE_DURATION = 1.0;

    // Scaled AoE Attack configuration
    private int aoeDamage = 20;
    private double aoeRadius = 250.0; // Scaled up to match boss size
    private double castCooldown = 5.0;
    private double castTimer = 0.0;
    private boolean isCasting = false;

    private Circle warningCircle;
    private Entity aoeVisualEntity;

    // Top HP Bar UI Components
    private VBox hpBarContainer;
    private Rectangle hpBarFill;
    private final double HP_BAR_WIDTH = 500.0;
    private final double HP_BAR_HEIGHT = 22.0;

    public BossEnemyComponent(double speed) {
        this.speed = speed;
    }

    public BossEnemyComponent() {
        this(2.0);
    }

    @Override
    public void onAdded() {
        castTimer = FXGL.random(1.0, 3.0);
        createTopHpBar();
    }

    @Override
    public void onRemoved() {
        removeHpBar();
        if (aoeVisualEntity != null && aoeVisualEntity.isActive()) {
            aoeVisualEntity.removeFromWorld();
        }
    }

    @Override
    public void onUpdate(double tpf) {
        // If dead, handle despawn fade out and stop movement/attacks
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

        Entity playerEntity = FXGL.getGameWorld()
                .getSingletonOptional(EntityType.PLAYER)
                .orElse(null);

        // --- Melee Movement & Contact Collision ---
        if (playerEntity != null) {
            Point2D playerCenter = playerEntity.getCenter();
            Point2D enemyCenter = entity.getCenter();

            Point2D direction = playerCenter.subtract(enemyCenter);
            if (direction.magnitude() > 1.0) {
                direction = direction.normalize();

                Point2D separation = computeSeparation();
                Point2D moveVector = direction.add(separation);

                if (moveVector.magnitude() > 0) {
                    moveVector = moveVector.normalize();
                }

                double dx = moveVector.getX() * speed;
                double dy = moveVector.getY() * speed;
                tryMove(dx, dy);
            }

            // Contact damage check
            if (entity.isColliding(playerEntity)) {
                Player player = playerEntity.getObject("playerRef");
                if (player != null) {
                    Point2D knockbackDir = playerCenter.subtract(enemyCenter);
                    if (knockbackDir.magnitude() == 0) {
                        knockbackDir = new Point2D(1, 0);
                    } else {
                        knockbackDir = knockbackDir.normalize();
                    }

                    player.takeDamageWithKnockback(contactDamage, knockbackDir, knockbackDistance);
                }
            }
        }

        // --- Dynamic AoE Spell Timer & Position Lock ---
        if (!isCasting) {
            castTimer += tpf;
            if (castTimer >= castCooldown) {
                castTimer = 0.0;
                castCooldown = FXGL.random(4.5, 6.0);
                startAoeAttack();
            }
        } else if (aoeVisualEntity != null && aoeVisualEntity.isActive()) {
            // Keep AoE warning centered continuously on the moving Boss
            Point2D center = entity.getCenter();
            aoeVisualEntity.setPosition(center.getX() - aoeRadius, center.getY() - aoeRadius);
        }
    }

    private void startAoeAttack() {
        isCasting = true;
        Point2D center = entity.getCenter();

        warningCircle = new Circle(aoeRadius, aoeRadius, aoeRadius);
        warningCircle.setFill(Color.rgb(255, 0, 0, 0.35));
        warningCircle.setStroke(Color.RED);
        warningCircle.setStrokeWidth(3);

        aoeVisualEntity = entityBuilder()
                .at(center.getX() - aoeRadius, center.getY() - aoeRadius)
                .view(warningCircle)
                .buildAndAttach();

        // Flash 1 (On at 0.0s -> Off at 0.3s)
        runOnce(() -> {
            if (aoeVisualEntity != null && aoeVisualEntity.isActive()) {
                aoeVisualEntity.getViewComponent().setOpacity(0.1);
            }
        }, Duration.seconds(0.3));

        // Flash 2 (On at 0.6s -> Off at 0.9s)
        runOnce(() -> {
            if (aoeVisualEntity != null && aoeVisualEntity.isActive()) {
                aoeVisualEntity.getViewComponent().setOpacity(1.0);
            }
        }, Duration.seconds(0.6));

        runOnce(() -> {
            if (aoeVisualEntity != null && aoeVisualEntity.isActive()) {
                aoeVisualEntity.getViewComponent().setOpacity(0.1);
            }
        }, Duration.seconds(0.9));

        // Flash 3 / Impact (On at 1.2s -> Evaluate damage relative to Boss's current position)
        runOnce(() -> {
            if (aoeVisualEntity != null && aoeVisualEntity.isActive()) {
                aoeVisualEntity.getViewComponent().setOpacity(1.0);
                warningCircle.setFill(Color.rgb(255, 0, 0, 0.75));
            }

            Entity currentPlayer = FXGL.getGameWorld()
                    .getSingletonOptional(EntityType.PLAYER)
                    .orElse(null);

            if (currentPlayer != null && !isDead) {
                Point2D currentBossCenter = entity.getCenter();
                double distance = currentPlayer.getCenter().distance(currentBossCenter);
                double playerRadius = currentPlayer.getWidth() > 0 ? currentPlayer.getWidth() / 2.0 : 50.0;

                if (distance <= (aoeRadius + playerRadius)) {
                    Player player = currentPlayer.getObject("playerRef");
                    if (player != null) {
                        Point2D knockbackDir = currentPlayer.getCenter().subtract(currentBossCenter);
                        if (knockbackDir.magnitude() == 0) knockbackDir = new Point2D(1, 0);
                        else knockbackDir = knockbackDir.normalize();

                        player.takeDamageWithKnockback(aoeDamage, knockbackDir, 50.0);
                    }
                }
            }
        }, Duration.seconds(1.2));

        // Clean up AoE entity
        runOnce(() -> {
            if (aoeVisualEntity != null && aoeVisualEntity.isActive()) {
                aoeVisualEntity.removeFromWorld();
            }
            isCasting = false;
        }, Duration.seconds(1.5));
    }

    @Override
    public void takeDamage(int damage) {
        if (isDead) return;

        currentHealth = Math.max(0, currentHealth - damage);
        updateHpBar();
        triggerRedFlash();

        if (currentHealth <= maxHealth / 2 && currentHealth > 0) {
            updateToHalfHealthVisual();
        } else if (currentHealth <= 0) {
            die();
        }
    }

    private void createTopHpBar() {
        Text bossTitle = getUIFactoryService().newText("BOSS", Color.DARKRED, 22.0);

        Rectangle hpBarBg = new Rectangle(HP_BAR_WIDTH, HP_BAR_HEIGHT, Color.rgb(40, 40, 40, 0.8));
        hpBarBg.setStroke(Color.GOLD);
        hpBarBg.setStrokeWidth(2);

        hpBarFill = new Rectangle(HP_BAR_WIDTH, HP_BAR_HEIGHT, Color.RED);

        Pane barPane = new Pane(hpBarBg, hpBarFill);
        barPane.setPrefSize(HP_BAR_WIDTH, HP_BAR_HEIGHT);

        hpBarContainer = new VBox(4, bossTitle, barPane);
        hpBarContainer.setAlignment(Pos.CENTER);
        hpBarContainer.setTranslateX((getAppWidth() - HP_BAR_WIDTH) / 2.0);
        hpBarContainer.setTranslateY(15);

        addUINode(hpBarContainer);
    }

    private void updateHpBar() {
        if (hpBarFill != null) {
            double healthPercent = Math.max(0.0, (double) currentHealth / maxHealth);
            hpBarFill.setWidth(HP_BAR_WIDTH * healthPercent);
        }
    }

    private void removeHpBar() {
        if (hpBarContainer != null) {
            removeUINode(hpBarContainer);
            hpBarContainer = null;
        }
    }

    private Point2D computeSeparation() {
        Point2D separation = new Point2D(0, 0);
        double minDistance = 180.0; // Increased spacing for larger boss frame

        for (Entity other : FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY)) {
            if (other == entity) continue;

            BaseEnemyComponent otherComp = other.getComponentOptional(BaseEnemyComponent.class).orElse(null);
            if (otherComp != null && otherComp.isDead()) continue;

            double dist = entity.getCenter().distance(other.getCenter());
            if (dist < minDistance) {
                Point2D pushDir;
                if (dist > 0.001) {
                    pushDir = entity.getCenter().subtract(other.getCenter()).normalize();
                } else {
                    pushDir = new Point2D(random(-1.0, 1.0), random(-1.0, 1.0)).normalize();
                }
                double force = (minDistance - dist) / minDistance;
                separation = separation.add(pushDir.multiply(force * 1.5));
            }
        }
        return separation;
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
            Texture halfTexture = texture("Boss_Enemy_HalfHealth.png", 300, 300);
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
            inc("score", 1000);
        } else {
            set("score", 1000);
        }

        removeHpBar();
        entity.getComponentOptional(CollidableComponent.class).ifPresent(c -> c.setValue(false));

        try {
            Texture deadTexture = texture("Boss_Enemy_Dead.png", 300, 300);
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

    @Override
    public boolean isDead() {
        return isDead;
    }
}