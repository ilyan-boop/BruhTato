package BruhTato.Enemies;

import BruhTato.Player.Player;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class EnemyComponent extends Component {

    private double speed; // Fixed per-tick step matching Player movement

    private int contactDamage = 10;
    private double knockbackDistance = 100.0;

    public EnemyComponent(double speed) {
        this.speed = speed;
    }

    public EnemyComponent() {
        this(1.0);
    }

    @Override
    public void onUpdate(double tpf) {
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
}