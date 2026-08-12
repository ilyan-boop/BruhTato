package BruhTato.Player;

import BruhTato.Items.ItemComponent;
import BruhTato.Items.ItemType;
import BruhTato.Items.WeaponType;
import BruhTato.Screens.HUD;
import BruhTato.Utils.EntityType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Player {

    private final Entity entity;
    private final WeaponComponent weaponComponent;
    private static final double SPEED = 3.5;
    private HUD hud;

    private final Runnable onReturnToMenu;

    private final int maxHealth = 100;
    private int currentHealth = 100;
    private boolean isPushingBorder = false;

    private boolean isDead = false;

    private boolean isInvincible = false;
    private final double INVINCIBILITY_DURATION = 2.0;

    private boolean isShielded = false;
    private double shieldTimer = 0.0;
    private int healthRestoreAmount = 25;
    private double shieldDuration = 5.0;

    private double borderDamageTimer = 0.0;
    private final double DAMAGE_INTERVAL = 1.0;
    private final int BORDER_DAMAGE = 5;

    public Player(Runnable onReturnToMenu) {
        this.onReturnToMenu = onReturnToMenu;
        weaponComponent = new WeaponComponent();
        weaponComponent.setPlayerRef(this);

        entity = entityBuilder()
                .type(EntityType.PLAYER)
                .at(960, 540)
                .bbox(new HitBox(new Point2D(50, 25), BoundingShape.circle(50)))
                .bbox(new HitBox(new Point2D(50, 80), BoundingShape.circle(50)))
                .with(weaponComponent)
                .with(new CollidableComponent(true))
                .with("playerRef", this)
                .view(texture("Bruhtato_FullHealth.png", 200, 200))
                .buildAndAttach();
    }

    public Player() {
        this(null);
    }

    public void setDifficulty(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy" -> {
                healthRestoreAmount = 40;
                shieldDuration = 10.0;
            }
            case "hard" -> {
                healthRestoreAmount = 15;
                shieldDuration = 3.0;
            }
            default -> { // Medium
                healthRestoreAmount = 25;
                shieldDuration = 5.0;
            }
        }
    }

    public void attachHUD(HUD hud) {
        this.hud = hud;
        updateHUD();
        updateHUDWeaponInfo();
    }

    public void updateHUDWeaponInfo() {
        if (weaponComponent != null) {
            WeaponType weapon = weaponComponent.getCurrentWeapon();
            if (hud != null) {
                hud.updateWeapon(weapon.getDisplayName(), weaponComponent.getRemainingUses(), weapon.getMaxUses());
            }
            updateHealthVisual();
        }
    }

    public void attack() {
        if (isDead) return;
        weaponComponent.swing();
    }

    // Called when pressing the interact key ('E') to pick up nearby items
    public void interact() {
        if (isDead) return;

        getGameWorld().getEntitiesByType(EntityType.ITEM).stream()
                .filter(entity::isColliding)
                .findFirst()
                .ifPresent(itemEntity -> {
                    ItemComponent itemComp = itemEntity.getComponentOptional(ItemComponent.class).orElse(null);
                    if (itemComp != null) {
                        applyItemEffect(itemComp.getItemType());
                        itemEntity.removeFromWorld();
                    }
                });
    }

    private void tryMove(double dx, double dy) {
        if (isDead) return;

        entity.translateX(dx);
        entity.translateY(dy);

        boolean hitsBorder = getGameWorld()
                .getEntitiesByType(EntityType.BORDER)
                .stream()
                .anyMatch(entity::isColliding);

        if (hitsBorder) {
            isPushingBorder = true;
            entity.translateX(-dx);
            entity.translateY(-dy);
        }
    }

    private void applyItemEffect(ItemType type) {
        switch (type) {
            case HEALTH -> {
                currentHealth = Math.min(maxHealth, currentHealth + healthRestoreAmount);
                updateHUD();
                updateHealthVisual();
            }
            case SHIELD -> grantShieldInvulnerability(shieldDuration);
            case SWORD -> {
                weaponComponent.equipWeapon(WeaponType.SWORD);
                updateHealthVisual();
            }
            case SPEAR -> {
                weaponComponent.equipWeapon(WeaponType.SPEAR);
                updateHealthVisual();
            }
            case AXE -> {
                weaponComponent.equipWeapon(WeaponType.AXE);
                updateHealthVisual();
            }
        }
    }

    private void grantShieldInvulnerability(double duration) {
        isShielded = true;
        shieldTimer = duration;

        ColorAdjust shieldTint = new ColorAdjust();
        shieldTint.setHue(-0.5);
        if (entity.getViewComponent().getParent() != null) {
            entity.getViewComponent().getParent().setEffect(shieldTint);
        }
    }

    public void onUpdate(double tpf) {
        if (isDead) return;

        updateBorderDamage(tpf);

        // Update timed shield
        if (isShielded) {
            shieldTimer -= tpf;
            if (shieldTimer <= 0) {
                isShielded = false;
                shieldTimer = 0.0;
                if (entity.getViewComponent().getParent() != null) {
                    entity.getViewComponent().getParent().setEffect(null);
                }
            }
        }

        // Update HUD shield display
        if (hud != null) {
            hud.updateShieldStatus(isShielded, shieldTimer, tpf);
        }
    }

    public void updateBorderDamage(double tpf) {
        if (isDead) return;

        if (isPushingBorder) {
            borderDamageTimer += tpf;
            if (borderDamageTimer >= DAMAGE_INTERVAL) {
                takeDamage(BORDER_DAMAGE);
                borderDamageTimer = 0.0;
            }
        } else {
            borderDamageTimer = 0.0;
        }
        isPushingBorder = false;
    }

    public void takeDamageWithKnockback(int amount, Point2D direction, double distance) {
        if (isDead || isInvincible || isShielded) return;

        takeDamage(amount);
        applyKnockback(direction, distance);
    }

    public void applyKnockback(Point2D direction, double distance) {
        if (isDead) return;

        double dx = direction.getX() * distance;
        double dy = direction.getY() * distance;

        entity.translateX(dx);
        entity.translateY(dy);

        boolean hitsBorder = getGameWorld()
                .getEntitiesByType(EntityType.BORDER)
                .stream()
                .anyMatch(entity::isColliding);

        if (hitsBorder) {
            entity.translateX(-dx);
            entity.translateY(-dy);
        }
    }

    public void takeDamage(int amount) {
        if (isDead || isInvincible || isShielded) return;

        currentHealth = Math.max(0, currentHealth - amount);
        updateHUD();

        if (getWorldProperties().exists("score")) {
            int currentScore = geti("score");
            set("score", Math.max(0, currentScore - amount));
        }

        updateHealthVisual();

        if (currentHealth <= 0) {
            die();
            return;
        }

        isInvincible = true;

        ColorAdjust redFlash = new ColorAdjust();
        redFlash.setHue(-0.08);
        redFlash.setSaturation(1.0);
        if (entity.getViewComponent().getParent() != null) {
            entity.getViewComponent().getParent().setEffect(redFlash);
        }
        entity.getViewComponent().setOpacity(0.5);

        runOnce(() -> {
            if (!isShielded && entity.getViewComponent().getParent() != null) {
                entity.getViewComponent().getParent().setEffect(null);
            }
        }, Duration.seconds(0.15));

        runOnce(() -> {
            if (!isDead) {
                isInvincible = false;
                entity.getViewComponent().setOpacity(1.0);
            }
        }, Duration.seconds(INVINCIBILITY_DURATION));
    }

    private void die() {
        isDead = true;

        entity.getComponentOptional(CollidableComponent.class).ifPresent(c -> c.setValue(false));

        if (hud != null) {
            hud.showGameOver(onReturnToMenu);
        }

        System.out.println("Player Defeated!");
    }

    private void updateHealthVisual() {
        if (currentHealth <= 0) {
            try {
                Texture deadTexture = texture("Bruhtato_Dead.png", 220, 220);
                updateEntityTexture(deadTexture);
            } catch (Exception e) {
                ColorAdjust deadTint = new ColorAdjust();
                deadTint.setSaturation(-1.0);
                if (entity.getViewComponent().getParent() != null) {
                    entity.getViewComponent().getParent().setEffect(deadTint);
                }
            }
            return;
        }

        String healthPrefix = (currentHealth > maxHealth / 2) ? "Bruhtato_FullHealth" : "Bruhtato_HalfHealth";
        WeaponType weapon = (weaponComponent != null) ? weaponComponent.getCurrentWeapon() : WeaponType.DEFAULT;

        String weaponSuffix = switch (weapon) {
            case AXE -> "_Axe";
            case SPEAR -> "_Spear";
            case SWORD -> "_Sword";
            default -> "";
        };

        String textureFileName = healthPrefix + weaponSuffix + ".png";

        try {
            Texture playerTexture = texture(textureFileName, 200, 200);
            updateEntityTexture(playerTexture);
        } catch (Exception e) {
            if (currentHealth <= maxHealth / 2) {
                ColorAdjust damagedTint = new ColorAdjust();
                damagedTint.setBrightness(-0.3);
                if (entity.getViewComponent().getParent() != null) {
                    entity.getViewComponent().getParent().setEffect(damagedTint);
                }
            } else {
                if (entity.getViewComponent().getParent() != null) {
                    entity.getViewComponent().getParent().setEffect(null);
                }
            }
        }
    }

    private void updateEntityTexture(Texture newTexture) {
        boolean textureUpdated = false;
        for (Node node : entity.getViewComponent().getChildren()) {
            if (node instanceof Texture) {
                Texture t = (Texture) node;
                t.setImage(newTexture.getImage());
                textureUpdated = true;
                break;
            }
        }
        if (!textureUpdated) {
            entity.getViewComponent().addChild(newTexture);
        }
    }

    private void updateHUD() {
        if (hud != null) {
            hud.updateHealth(currentHealth, maxHealth);
        }
    }

    public void moveUp() { tryMove(0, -SPEED); }
    public void moveDown() { tryMove(0, SPEED); }
    public void moveLeft() { tryMove(-SPEED, 0); }
    public void moveRight() { tryMove(SPEED, 0); }

    public Entity getEntity() { return entity; }
    public int getCurrentHealth() { return currentHealth; }
    public boolean isDead() { return isDead; }
}