package BruhTato.Player;

import BruhTato.Enemies.EnemyComponent;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle; // Replaced Circle with Rectangle

import static com.almasb.fxgl.dsl.FXGL.*;

public class WeaponComponent extends Component {

    private final double ATTACK_RANGE = 300.0;   // Reach length of attack rectangle
    private final double ATTACK_WIDTH = 20.0;    // Narrow width of the attack rectangle
    private final double ATTACK_DURATION = 0.15; // How long (in seconds) the swing stays visible
    private final int ATTACK_DAMAGE = 1;         // 1 Hit per swing toward 3 max HP

    // Attack Cooldown parameters
    private boolean canAttack = true;
    private final double ATTACK_COOLDOWN = 0.1;  // 1 second cooldown between swings

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
        // Check if the attack is on cooldown
        if (!canAttack) {
            return;
        }

        // Lock attacking state and start cooldown
        canAttack = false;

        Point2D playerCenter = entity.getCenter();
        Point2D mouseWorld = getInput().getMousePositionWorld();

        // Calculate direction vector towards cursor and convert to angle in degrees
        Point2D dir = mouseWorld.subtract(playerCenter).normalize();
        double angle = Math.toDegrees(Math.atan2(dir.getY(), dir.getX()));

        // Create narrow red rectangle visual extending from player along aim angle
        Rectangle attackVisual = new Rectangle(ATTACK_RANGE, ATTACK_WIDTH, Color.RED);
        attackVisual.setOpacity(0.7);

        // Calculate top-left spawn coordinate for the rectangle starting at player center
        double spawnX = playerCenter.getX();
        double spawnY = playerCenter.getY() - (ATTACK_WIDTH / 2.0);

        // Create attack entity with narrow rectangular HitBox
        Entity attackEntity = entityBuilder()
                .at(spawnX, spawnY)
                .bbox(new HitBox(BoundingShape.box(ATTACK_RANGE, ATTACK_WIDTH)))
                .view(attackVisual)
                .with(new CollidableComponent(true))
                .buildAndAttach();

        // Fixed transform origin call using FXGL's TransformComponent
        // Sets rotation pivot to the player's center (start of the rectangle on local Y-axis)
        attackEntity.getTransformComponent().setRotationOrigin(new Point2D(0, ATTACK_WIDTH / 2.0));
        attackEntity.setRotation(angle);

        // Damage enemies inside attack hitbox (no enemy i-frames evaluated here)
        getGameWorld()
                .getEntitiesByType(EntityType.ENEMY)
                .stream()
                .filter(enemy -> attackEntity.isColliding(enemy))
                .forEach(enemy -> {
                    EnemyComponent enemyComp = enemy.getComponentOptional(EnemyComponent.class).orElse(null);
                    if (enemyComp != null && !enemyComp.isDead()) {
                        enemyComp.takeDamage(ATTACK_DAMAGE); // Deals 1 hit of damage
                    }
                });

        // Despawn attack box visual after ATTACK_DURATION seconds
        runOnce(attackEntity::removeFromWorld, javafx.util.Duration.seconds(ATTACK_DURATION));

        // Reset attack cooldown
        runOnce(() -> canAttack = true, javafx.util.Duration.seconds(ATTACK_COOLDOWN));
    }

    @Override
    public void onRemoved() {
        removeUINode(aimIndicator);
    }

    // Getter to check if attack is off cooldown
    public boolean canAttack() {
        return canAttack;
    }
}