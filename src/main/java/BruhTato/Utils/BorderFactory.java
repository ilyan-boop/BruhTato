package BruhTato.Utils;

import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BorderFactory {

    private static final double BORDER_THICKNESS = 40.0;
    private static final double SCREEN_WIDTH = 1920.0;
    private static final double SCREEN_HEIGHT = 1080.0;

    public static void spawnScreenBorders() {
        Color borderTileColor = Color.rgb(180, 50, 50, 0.8); // Semi-transparent red walls

        // 1. TOP BORDER
        createBorder(0, -BORDER_THICKNESS, SCREEN_WIDTH, BORDER_THICKNESS, borderTileColor);

        // 2. BOTTOM BORDER
        createBorder(0, SCREEN_HEIGHT, SCREEN_WIDTH, BORDER_THICKNESS, borderTileColor);

        // 3. LEFT BORDER
        createBorder(-BORDER_THICKNESS, 0, BORDER_THICKNESS, SCREEN_HEIGHT, borderTileColor);

        // 4. RIGHT BORDER
        createBorder(SCREEN_WIDTH, 0, BORDER_THICKNESS, SCREEN_HEIGHT, borderTileColor);
    }

    private static void createBorder(double x, double y, double width, double height, Color color) {
        entityBuilder()
                .type(EntityType.BORDER)
                .at(x, y)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .view(new Rectangle(width, height, color))
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }
}