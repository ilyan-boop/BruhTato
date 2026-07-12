package BruhTato.Player;

import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Player {

    public Player() {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        Entity entity = entityBuilder()
                .type(EntityType.PLAYER).at(100, 100)
                .bbox(new HitBox(BoundingShape.box(50, 50)))
                .with(physics)
                .with(new CollidableComponent(true))
                .viewWithBBox("src/main/resources/BRUHTATO_ASSETS/BRUHTATO/Bruhtato_FullHealth.png")
                .buildAndAttach();
    }


}
