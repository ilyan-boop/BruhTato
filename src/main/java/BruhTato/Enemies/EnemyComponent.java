package BruhTato.Enemies;

import BruhTato.Utils.EntityType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class EnemyComponent extends Component {

    private double speed;

    public EnemyComponent(double speed) {
        this.speed = speed;
    }

    public EnemyComponent() {
        this(150.0); // Default speed if none provided
    }

    @Override
    public void onUpdate(double tpf) {
        // Locate the player
        Entity player = FXGL.getGameWorld()
                .getSingletonOptional(EntityType.PLAYER)
                .orElse(null);

        if (player != null) {
            // Track towards player center
            Point2D direction = player.getCenter()
                    .subtract(entity.getCenter())
                    .normalize();

            entity.translate(direction.multiply(speed * tpf));
        }
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}