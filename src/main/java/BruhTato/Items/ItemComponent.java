package BruhTato.Items;

import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.*;

public class ItemComponent extends Component {

    private final ItemType itemType;
    private Text promptText;

    public ItemComponent(ItemType itemType) {
        this.itemType = itemType;
    }

    @Override
    public void onAdded() {
        // Create standard JavaFX Text to avoid FXGL font binding restrictions
        promptText = new Text("[E] Pick Up");
        promptText.setFill(Color.GOLD);
        promptText.setFont(Font.font("Verdana", FontWeight.BOLD, 30));
        promptText.setStroke(Color.BLACK);
        promptText.setStrokeWidth(0.5);

        // Position prompt centered above the item sprite
        promptText.setTranslateX(-5);
        promptText.setTranslateY(-20);

        // Hide prompt by default
        promptText.setVisible(false);

        // Attach prompt as a child to the item's view component
        entity.getViewComponent().addChild(promptText);
    }

    @Override
    public void onUpdate(double tpf) {
        // Check if the player entity is colliding with this item
        boolean isPlayerColliding = getGameWorld()
                .getEntitiesByType(EntityType.PLAYER)
                .stream()
                .anyMatch(player -> player.isColliding(entity));

        // Show text when player is in range, hide when they step away
        if (promptText != null) {
            promptText.setVisible(isPlayerColliding);
        }
    }

    public ItemType getItemType() {
        return itemType;
    }
}