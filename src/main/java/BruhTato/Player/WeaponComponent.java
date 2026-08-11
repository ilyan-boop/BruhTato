package BruhTato.Player;

import BruhTato.Enemies.MeleeEnemyComponent;
import BruhTato.Items.WeaponType;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.*;

public class WeaponComponent extends Component {

    // Default weapon (Fists) baseline parameters
    private final double DEFAULT_ATTACK_RANGE = 200.0;   // Reach length of attack rectangle
    private final double DEFAULT_ATTACK_WIDTH = 20.0;    // Narrow width of the attack rectangle
    private final double DEFAULT_ATTACK_DURATION = 0.15; // How long (in seconds) swing stays visible
    private final int DEFAULT_ATTACK_DAMAGE = 1;         // 1 Hit per swing
    private final double DEFAULT_ATTACK_COOLDOWN = 0.4;  // Cooldown between swings

    // Current weapon state tracking
    private WeaponType currentWeapon = WeaponType.DEFAULT;
    private int remainingUses = -1; // -1 indicates infinite uses (default fists)

    // Attack Cooldown parameters
    private boolean canAttack = true;

    private Line aimIndicator;
    private Player playerRef;

    public void setPlayerRef(Player playerRef) {
        this.playerRef = playerRef;
    }

    @Override
    public void onAdded() {
        // Create aim indicator line
        aimIndicator = new Line();
        aimIndicator.setStroke(Color.color(1, 1, 1, 0.4)); // Semi-transparent white
        aimIndicator.setStrokeWidth(2);

        // Add line graphic directly to UI scene
        addUINode(aimIndicator);
    }

    @Override
    public void onUpdate(double tpf) {
        // 1. Get player center in world space
        Point2D playerCenterWorld = entity.getCenter();

        // 2. Adjust for camera viewport offset so UI line matches world position perfectly
        Point2D playerUI = playerCenterWorld.subtract(getGameScene().getViewport().getOrigin());

        // 3. Get mouse position in screen/UI space directly
        Point2D mouseUI = getInput().getMousePositionUI();

        // 4. Calculate aim vector using current weapon's range
        Point2D dir = mouseUI.subtract(playerUI).normalize();
        Point2D lineEnd = playerUI.add(dir.multiply(getCurrentAttackRange()));

        // 5. Update UI line endpoints
        aimIndicator.setStartX(playerUI.getX());
        aimIndicator.setStartY(playerUI.getY());
        aimIndicator.setEndX(lineEnd.getX());
        aimIndicator.setEndY(lineEnd.getY());
    }

    // Equips a new weapon and sets durability uses (10 uses for weapons, -1 for default)
    public void equipWeapon(WeaponType weaponType) {
        this.currentWeapon = weaponType;
        this.remainingUses = weaponType.getMaxUses();
        if (playerRef != null) {
            playerRef.updateHUDWeaponInfo();
        }
    }

    public void swing() {
        // Check if the attack is on cooldown
        if (!canAttack) {
            return;
        }

        // Lock attacking state
        canAttack = false;

        Point2D playerCenter = entity.getCenter();
        Point2D mouseWorld = getInput().getMousePositionWorld();

        // Calculate direction vector towards cursor and convert to angle in degrees
        Point2D dir = mouseWorld.subtract(playerCenter).normalize();
        double angle = Math.toDegrees(Math.atan2(dir.getY(), dir.getX()));

        Entity attackEntity;
        int damage = getDamageForCurrentWeapon();
        double cooldown = getCooldownForCurrentWeapon();

        // Branch hitbox creation based on weapon specs
        switch (currentWeapon) {
            case SPEAR -> {
                // Spear: Thin, long rectangle extending forward
                double spearRange = 400.0;
                double spearWidth = 15.0;

                Rectangle spearVisual = new Rectangle(spearRange, spearWidth, Color.RED);
                spearVisual.setOpacity(0.7);

                double spawnX = playerCenter.getX();
                double spawnY = playerCenter.getY() - (spearWidth / 2.0);

                attackEntity = entityBuilder()
                        .at(spawnX, spawnY)
                        .bbox(new HitBox(BoundingShape.box(spearRange, spearWidth)))
                        .view(spearVisual)
                        .with(new CollidableComponent(true))
                        .buildAndAttach();

                attackEntity.getTransformComponent().setRotationOrigin(new Point2D(0, spearWidth / 2.0));
                attackEntity.setRotation(angle);
            }
            case SWORD -> {
                // Sword: Circle centered exactly at the tip of the aiming line
                double radius = 80.0;
                Point2D attackCenter = playerCenter.add(dir.multiply(getCurrentAttackRange()));

                // Set centerX and centerY to radius so JavaFX renders from top-left (0,0) to (2*radius, 2*radius)
                Circle swordVisual = new Circle(radius, radius, radius, Color.RED);
                swordVisual.setOpacity(0.7);

                attackEntity = entityBuilder()
                        .at(attackCenter.getX() - radius, attackCenter.getY() - radius)
                        .bbox(new HitBox(BoundingShape.circle(radius)))
                        .view(swordVisual)
                        .with(new CollidableComponent(true))
                        .buildAndAttach();
            }
            case AXE -> {
                // Axe: Circle centered directly on the player's center position
                double radius = 200;

                Circle axeVisual = new Circle(radius, radius, radius, Color.RED);
                axeVisual.setOpacity(0.7);

                attackEntity = entityBuilder()
                        .at(playerCenter.getX() - radius, playerCenter.getY() - radius)
                        .bbox(new HitBox(BoundingShape.circle(radius)))
                        .view(axeVisual)
                        .with(new CollidableComponent(true))
                        .buildAndAttach();
            }
            default -> {
                // Default Fists: Standard narrow red rectangle extending from player
                Rectangle attackVisual = new Rectangle(DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WIDTH, Color.RED);
                attackVisual.setOpacity(0.7);

                double spawnX = playerCenter.getX();
                double spawnY = playerCenter.getY() - (DEFAULT_ATTACK_WIDTH / 2.0);

                attackEntity = entityBuilder()
                        .at(spawnX, spawnY)
                        .bbox(new HitBox(BoundingShape.box(DEFAULT_ATTACK_RANGE, DEFAULT_ATTACK_WIDTH)))
                        .view(attackVisual)
                        .with(new CollidableComponent(true))
                        .buildAndAttach();

                attackEntity.getTransformComponent().setRotationOrigin(new Point2D(0, DEFAULT_ATTACK_WIDTH / 2.0));
                attackEntity.setRotation(angle);
            }
        }

        // Damage enemies inside attack hitbox
        getGameWorld()
                .getEntitiesByType(EntityType.ENEMY)
                .stream()
                .filter(enemy -> attackEntity.isColliding(enemy))
                .forEach(enemy -> {
                    MeleeEnemyComponent enemyComp = enemy.getComponentOptional(MeleeEnemyComponent.class).orElse(null);
                    if (enemyComp != null && !enemyComp.isDead()) {
                        enemyComp.takeDamage(damage);
                    }
                });

        // Despawn attack box visual after duration
        runOnce(attackEntity::removeFromWorld, javafx.util.Duration.seconds(DEFAULT_ATTACK_DURATION));

        // Consume durability usage if not infinite
        if (remainingUses > 0) {
            remainingUses--;
            if (remainingUses == 0) {
                // Revert to default weapon when 10 uses expire
                equipWeapon(WeaponType.DEFAULT);
            } else if (playerRef != null) {
                playerRef.updateHUDWeaponInfo();
            }
        }

        // Reset attack cooldown based on equipped weapon
        runOnce(() -> canAttack = true, javafx.util.Duration.seconds(cooldown));
    }

    private double getCurrentAttackRange() {
        return switch (currentWeapon) {
            case SPEAR -> 400.0;
            case SWORD -> 150.0;
            case AXE -> 300.0;
            default -> DEFAULT_ATTACK_RANGE;
        };
    }

    private int getDamageForCurrentWeapon() {
        return switch (currentWeapon) {
            case SWORD -> 1;
            case SPEAR -> 1;
            case AXE -> 3;
            default -> DEFAULT_ATTACK_DAMAGE;
        };
    }

    private double getCooldownForCurrentWeapon() {
        return switch (currentWeapon) {
            case SWORD -> 0.35;
            case SPEAR -> 0.6;
            case AXE -> 1.0;
            default -> DEFAULT_ATTACK_COOLDOWN;
        };
    }

    @Override
    public void onRemoved() {
        removeUINode(aimIndicator);
    }

    public boolean canAttack() {
        return canAttack;
    }

    public WeaponType getCurrentWeapon() {
        return currentWeapon;
    }

    public int getRemainingUses() {
        return remainingUses;
    }
}