package BruhTato.Items;

import com.almasb.fxgl.entity.component.Component;

// NEW: Component attached to item entities to identify their pickup behavior
public class ItemComponent extends Component {

    private final ItemType itemType;

    public ItemComponent(ItemType itemType) {
        this.itemType = itemType;
    }

    public ItemType getItemType() {
        return itemType;
    }
}