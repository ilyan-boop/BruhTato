package BruhTato.Player;

import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import javafx.geometry.Point2D;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Player {
    private Entity entity;
    private PhysicsComponent physics;

    public Player() {
        physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        Entity entity = entityBuilder()
                .type(EntityType.PLAYER).at(100, 100)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .with(physics)
                .with(new CollidableComponent(true))
                .viewWithBBox("src/main/resources/BRUHTATO_ASSETS/BRUHTATO/Bruhtato_FullHealth.png")
                .buildAndAttach();
    }

    public void moveUp() { physics.setVelocityY(-200); }
    public void moveDown() { physics.setVelocityY(200); }
    public void moveLeft() { physics.setVelocityX(-200); }
    public void moveRight() { physics.setVelocityX(200); }

    public void stop() {
        physics.setLinearVelocity(Point2D.ZERO);
    }

}
