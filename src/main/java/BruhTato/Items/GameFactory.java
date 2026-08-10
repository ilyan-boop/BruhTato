package BruhTato.Items;

import BruhTato.Enemies.EnemyComponent;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameFactory implements EntityFactory {

    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {
        double radius = 50.0; // Radius matching enemy body size

        return entityBuilder(data)
                .type(EntityType.ENEMY)
                .bbox(new HitBox(new Point2D(25, 25), BoundingShape.circle(radius)))
                .view(texture("Melee_Enemy_FullHealth.png", 150, 150))
                .with(new CollidableComponent(true))
                .with(new EnemyComponent(1.5)) // Fixed speed matching player steps
                .build();
    }
}