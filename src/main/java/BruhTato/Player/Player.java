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
    //Declare data fields for FXGL
    private final PhysicsComponent physics;

    final int WALK_SPEED = 400;

    public Player() {
        //Define the player as a moving physics object
        physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);


        Entity entity = entityBuilder()
                //Spawns Player Entity at the designated coords
                .type(EntityType.PLAYER).at(100, 100)
                // Top circle cap
                .bbox(new HitBox(new Point2D(50, 25), BoundingShape.circle(50)))
                // Bottom circle cap
                .bbox(new HitBox(new Point2D(50, 80), BoundingShape.circle(50)))
                .with(physics)
                .with(new CollidableComponent(true))
                .view(texture("Bruhtato_FullHealth.png", 200, 200))
                .buildAndAttach();
    }

    public void moveUp() { physics.setVelocityY(-WALK_SPEED); }
    public void moveDown() { physics.setVelocityY(WALK_SPEED); }
    public void moveLeft() { physics.setVelocityX(-WALK_SPEED); }
    public void moveRight() { physics.setVelocityX(WALK_SPEED); }

    public void stop() {
        physics.setLinearVelocity(Point2D.ZERO);
    }

}
