package BruhTato.Enemies;

import BruhTato.Player.Player;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class EnemyComponent extends Component {

    private double speed;

    // Contact damage configuration
    private int contactDamage = 10;
    private double damageInterval = 1.0; // Seconds between damage ticks
    private double damageTimer = 0.0;

    public EnemyComponent(double speed) {
        this.speed = speed;
    }

    public EnemyComponent() {
        this(150.0); // Default speed in pixels per second
    }

    @Override
    public void onUpdate(double tpf) {
        // 1. Locate player and move toward them
        Entity playerEntity = FXGL.getGameWorld()
                .getSingletonOptional(EntityType.PLAYER)
                .orElse(null);

        if (playerEntity != null) {
            Point2D playerCenter = playerEntity.getCenter();
            Point2D enemyCenter = entity.getCenter();

            // Calculate direction vector toward player
            Point2D direction = playerCenter.subtract(enemyCenter);
            if (direction.magnitude() > 0) {
                direction = direction.normalize();

                // Move using step-checking system matching Player class
                double dx = direction.getX() * speed * tpf;
                double dy = direction.getY() * speed * tpf;
                tryMove(dx, dy);
            }

            // 2. Check for contact with player and deal damage
            if (entity.isColliding(playerEntity)) {
                damageTimer += tpf;
                if (damageTimer >= damageInterval) {
                    // Get Player reference from entity or game scene and apply damage
                    Player player = playerEntity.getObject("playerRef");
                    if (player != null) {
                        player.takeDamage(contactDamage);
                    }
                    damageTimer = 0.0; // Reset timer for next tick
                }
            } else {
                damageTimer = 0.0; // Reset timer if not in contact
            }
        }
    }

    // Step-checking movement system identical to Player logic
    private void tryMove(double dx, double dy) {
        entity.translateX(dx);
        entity.translateY(dy);

        // Revert move if step intersects a border wall
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
}