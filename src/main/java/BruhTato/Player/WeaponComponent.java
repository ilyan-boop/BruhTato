package BruhTato.Player;

import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import static com.almasb.fxgl.dsl.FXGL.*;

public class WeaponComponent extends Component {

    private final double ATTACK_RANGE = 150.0;    // Reach distance from player center
    private final double ATTACK_RADIUS = 60.0;   // Size of the attack circle hitbox
    private final double ATTACK_DURATION = 0.15; // How long (in seconds) the swing stays visible
    private final int ATTACK_DAMAGE = 25;

    private Line aimIndicator;

    @Override
    public void onAdded() {
        // Create aim indicator line
        aimIndicator = new Line();
        aimIndicator.setStroke(Color.color(1, 1, 1, 0.4)); // Semi-transparent white
        aimIndicator.setStrokeWidth(2);

        // Add line graphic directly to UI scene
        addUINode(aimIndicator);
    }

    @Override
    public void onUpdate(double tpf) {
        // 1. Get player center in world space
        Point2D playerCenterWorld = entity.getCenter();

        // 2. Adjust for camera viewport offset so UI line matches world position perfectly
        Point2D playerUI = playerCenterWorld.subtract(getGameScene().getViewport().getOrigin());

        // 3. Get mouse position in screen/UI space directly
        Point2D mouseUI = getInput().getMousePositionUI();

        // 4. Calculate aim vector
        Point2D dir = mouseUI.subtract(playerUI).normalize();
        Point2D lineEnd = playerUI.add(dir.multiply(ATTACK_RANGE));

        // 5. Update UI line endpoints
        aimIndicator.setStartX(playerUI.getX());
        aimIndicator.setStartY(playerUI.getY());
        aimIndicator.setEndX(lineEnd.getX());
        aimIndicator.setEndY(lineEnd.getY());
    }

    public void swing() {
        Point2D playerCenter = entity.getCenter();
        Point2D mouseWorld = getInput().getMousePositionWorld();

        // Calculate direction towards cursor in world space
        Point2D dir = mouseWorld.subtract(playerCenter).normalize();
        // The attack center point is placed at ATTACK_RANGE away from the player
        Point2D attackCenterPos = playerCenter.add(dir.multiply(ATTACK_RANGE));

        // 1. Visual representation of melee swing (translucent red circle)
        Circle attackVisual = new Circle(ATTACK_RADIUS, Color.RED);
        attackVisual.setOpacity(0.7);

        // 2. Spawn temporary attack entity in world
        // When using BoundingShape.circle, the position in entityBuilder() is the CENTER.
        Entity attackEntity = entityBuilder()
                .at(attackCenterPos) // Center of the swing
                .bbox(new HitBox(BoundingShape.circle(ATTACK_RADIUS))) // Circular hitbox
                .view(attackVisual)
                .with(new CollidableComponent(true))
                .buildAndAttach();

        // 3. Damage enemies inside attack hitbox
        getGameWorld()
                .getEntitiesByType(EntityType.ENEMY)
                .stream()
                .filter(enemy -> attackEntity.isColliding(enemy))
                .forEach(enemy -> {
                    System.out.println("Hit enemy! Dealing " + ATTACK_DAMAGE + " damage.");
                });

        // 4. Despawn attack box after ATTACK_DURATION seconds
        runOnce(attackEntity::removeFromWorld, javafx.util.Duration.seconds(ATTACK_DURATION));
    }

    @Override
    public void onRemoved() {
        removeUINode(aimIndicator);
    }
}