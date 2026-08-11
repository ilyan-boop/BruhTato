package BruhTato.Utils;

import BruhTato.Enemies.MeleeEnemyComponent;
import BruhTato.Enemies.WizardEnemyComponent;
import BruhTato.Items.ItemComponent;
import BruhTato.Items.ItemType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameFactory implements EntityFactory {

    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {

        return entityBuilder(data)
                .type(EntityType.ENEMY)
                .bbox(new HitBox(new Point2D(25, 25), BoundingShape.circle(50.0)))
                .view(texture("Melee_Enemy_FullHealth.png", 150, 150))
                .with(new CollidableComponent(true))
                .with(new MeleeEnemyComponent(1.5)) // Fixed speed matching player steps
                .build();
    }

    @Spawns("wizardEnemy")
    public Entity newWizardEnemy(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.ENEMY)
                .bbox(new HitBox(new Point2D(25, 25), BoundingShape.circle(70.0)))
                .view(texture("Wizard_Enemy_FullHealth.png", 200, 200))
                .with(new WizardEnemyComponent())
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("item")
    public Entity newItem(SpawnData data) {
        ItemType type = data.get("itemType");
        if (type == null) {
            type = ItemType.HEALTH;
        }

        String textureName = switch (type) {
            case HEALTH -> "Health.png";
            case SHIELD -> "Shield.png";
            case SWORD -> "Sword.png";
            case SPEAR -> "Spear.png";
            case AXE -> "Axe.png";
        };

        try {
            return entityBuilder(data)
                    .type(EntityType.ITEM)
                    .bbox(new HitBox(BoundingShape.box(150, 150)))
                    .view(texture(textureName, 150, 150))
                    .with(new CollidableComponent(true))
                    .with(new ItemComponent(type))
                    .build();
        } catch (Exception e) {
            // Fallback shape visual if texture assets are not found
            Color fallbackColor = switch (type) {
                case HEALTH -> Color.LIMEGREEN;
                case SHIELD -> Color.CYAN;
                case SWORD -> Color.SILVER;
                case SPEAR -> Color.LIGHTBLUE;
                case AXE -> Color.DARKRED;
            };

            return entityBuilder(data)
                    .type(EntityType.ITEM)
                    .bbox(new HitBox(BoundingShape.box(150, 150)))
                    .view(new Rectangle(150, 150, fallbackColor))
                    .with(new CollidableComponent(true))
                    .with(new ItemComponent(type))
                    .build();
        }
    }
}