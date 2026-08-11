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

    // Callback reference to return to Main Menu upon defeat
    private final Runnable onReturnToMenu;

    private final int maxHealth = 100;
    private int currentHealth = 100;
    private boolean isPushingBorder = false;

    // Player death tracking flag
    private boolean isDead = false;

    // Invincibility frame (i-frame) parameters
    private boolean isInvincible = false;
    private final double INVINCIBILITY_DURATION = 2.0; // 2 Seconds

    // Shield powerup parameters (scalable by difficulty)
    private boolean isShielded = false;
    private int healthRestoreAmount = 25; // Default normal heal amount
    private double shieldDuration = 5.0;   // Default normal shield duration in seconds

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

    // Adjusts item pickup potency according to difficulty setting
    public void setDifficulty(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy" -> {
                healthRestoreAmount = 40;
                shieldDuration = 7.0;
            }
            case "hard" -> {
                healthRestoreAmount = 15;
                shieldDuration = 3.0;
            }
            default -> { // Normal
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

    // Updates weapon status display on the HUD and syncs sprite texture
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
        if (isDead) return; // Block attacks when dead
        weaponComponent.swing();
    }

    private void tryMove(double dx, double dy) {
        if (isDead) return; // Block movement when dead

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

        // Check for item collisions on player movement
        checkItemCollisions();
    }

    // Checks collision with spawned item entities
    private void checkItemCollisions() {
        getGameWorld().getEntitiesByType(EntityType.ITEM).stream()
                .filter(entity::isColliding)
                .forEach(itemEntity -> {
                    ItemComponent itemComp = itemEntity.getComponentOptional(ItemComponent.class).orElse(null);
                    if (itemComp != null) {
                        applyItemEffect(itemComp.getItemType());
                        itemEntity.removeFromWorld(); // Despawn collected item
                    }
                });
    }

    // Applies state modification based on item type
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

    // Grants temporary invulnerability with a visual aura effect
    private void grantShieldInvulnerability(double duration) {
        isShielded = true;

        ColorAdjust shieldTint = new ColorAdjust();
        shieldTint.setHue(0.6); // Cyan/Blue aura
        if (entity.getViewComponent().getParent() != null) {
            entity.getViewComponent().getParent().setEffect(shieldTint);
        }

        runOnce(() -> {
            isShielded = false;
            if (!isDead && entity.getViewComponent().getParent() != null) {
                entity.getViewComponent().getParent().setEffect(null);
            }
        }, Duration.seconds(duration));
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
        if (isDead || isInvincible || isShielded) return; // Block damage & knockback if dead, invincible, or shielded

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
        if (isDead || isInvincible || isShielded) return; // Block damage if dead, invincible, or shielded

        currentHealth = Math.max(0, currentHealth - amount);
        updateHUD();

        // Deduct score when taking damage (ensuring score doesn't drop below 0)
        if (getWorldProperties().exists("score")) {
            int currentScore = geti("score");
            set("score", Math.max(0, currentScore - amount));
        }

        // Update player texture visual based on health and weapon state
        updateHealthVisual();

        if (currentHealth <= 0) {
            die();
            return;
        }

        // Trigger Invincibility State for non-lethal damage
        isInvincible = true;

        // Visual Effects: Red Flash + Opacity Blink
        ColorAdjust redFlash = new ColorAdjust();
        redFlash.setHue(-0.005);
        redFlash.setSaturation(1.0);
        if (entity.getViewComponent().getParent() != null) {
            entity.getViewComponent().getParent().setEffect(redFlash);
        }
        entity.getViewComponent().setOpacity(0.5);

        // Remove red tint flash after 0.15s
        runOnce(() -> {
            if (!isShielded && entity.getViewComponent().getParent() != null) {
                entity.getViewComponent().getParent().setEffect(null);
            }
        }, Duration.seconds(0.15));

        // Expire invincibility and restore opacity after 2.0 seconds
        runOnce(() -> {
            if (!isDead) {
                isInvincible = false;
                entity.getViewComponent().setOpacity(1.0);
            }
        }, Duration.seconds(INVINCIBILITY_DURATION));
    }

    // Handles death state, disables collisions, and displays Game Over screen
    private void die() {
        isDead = true;

        // Disable collision component so enemies pass through dead player
        entity.getComponentOptional(CollidableComponent.class).ifPresent(c -> c.setValue(false));

        // Display Game Over overlay on HUD
        if (hud != null) {
            hud.showGameOver(onReturnToMenu);
        }

        System.out.println("Player Defeated!");
    }

    // Dynamically updates player sprite based on health status (Full/Half/Dead) and equipped weapon
    private void updateHealthVisual() {
        if (currentHealth <= 0) {
            try {
                Texture deadTexture = texture("Bruhtato_Dead.png", 200, 200);
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