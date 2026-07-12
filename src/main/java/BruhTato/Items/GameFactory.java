package BruhTato.Items;

import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameFactory implements EntityFactory {

    public void spawnObstacle(double x, double y, double w, double h) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC); // Static means it's an unmovable wall

        entityBuilder()
                .type(EntityType.OBSTACLE)
                .at(x, y)
                .bbox(new HitBox(BoundingShape.box(w, h)))
                .view(new Rectangle(w, h, Color.RED))
                .with(physics)
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }
}
