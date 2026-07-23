package BruhTato.Items;

import BruhTato.Enemies.EnemyComponent;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;

public class GameFactory implements EntityFactory {

    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.ENEMY)
                .viewWithBBox(FXGL.texture("Melee_Enemy_FullHealth.png", 200, 200))
                .with(new EnemyComponent(150.0))
                .collidable()
                .build();
    }
}