package BruhTato.Player;

import com.almasb.fxgl.entity.component.Component;

public class PlayerComponent extends Component {

    private boolean up = false;
    private boolean down = false;
    private boolean left = false;
    private boolean right = false;

    @Override
    public void onUpdate(double tpf) {
        double dx = 0;
        double dy = 0;

        // Walk speed in pixels per second
        double speed = 400;
        if (up) dy -= speed * tpf;
        if (down) dy += speed * tpf;
        if (left) dx -= speed * tpf;
        if (right) dx += speed * tpf;

        // Directly move entity position
        entity.translateX(dx);
        entity.translateY(dy);
    }

    public void moveUp() { up = true; }
    public void stopUp() { up = false; }

    public void moveDown() { down = true; }
    public void stopDown() { down = false; }

    public void moveLeft() { left = true; }
    public void stopLeft() { left = false; }

    public void moveRight() { right = true; }
    public void stopRight() { right = false; }
}