package BruhTato.Enemies;

import com.almasb.fxgl.entity.component.Component;

public abstract class BaseEnemyComponent extends Component {
    public abstract void takeDamage(int damage);
    public abstract boolean isDead();
}