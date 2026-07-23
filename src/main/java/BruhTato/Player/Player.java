package BruhTato.Player;

import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Player {

    private final Entity entity;
    private static final double SPEED = 7;

    public Player() {
        entity = entityBuilder()
                .type(EntityType.PLAYER)
                .at(100, 100)
                .bbox(new HitBox(new Point2D(50, 25), BoundingShape.circle(50)))
                .bbox(new HitBox(new Point2D(50, 80), BoundingShape.circle(50)))
                .with(new CollidableComponent(true))
                .view(texture("Bruhtato_FullHealth.png", 200, 200))
                .buildAndAttach();
    }

    // Helper method to move safely only if no obstacle is hit
    private void tryMove(double dx, double dy) {
        entity.translateX(dx);
        entity.translateY(dy);

        // Check if moving here caused an overlap with an OBSTACLE
        boolean hitsObstacle = getGameWorld()
                .getEntitiesByType(EntityType.OBSTACLE) // Ensure you have OBSTACLE in EntityType
                .stream()
                .anyMatch(obstacle -> entity.isColliding(obstacle));

        // If it collides, revert the movement immediately
        if (hitsObstacle) {
            entity.translateX(-dx);
            entity.translateY(-dy);
        }
    }

    public void moveUp() { tryMove(0, -SPEED); }
    public void moveDown() { tryMove(0, SPEED); }
    public void moveLeft() { tryMove(-SPEED, 0); }
    public void moveRight() { tryMove(SPEED, 0); }

    public Entity getEntity() { return entity; }
}