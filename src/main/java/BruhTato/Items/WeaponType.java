package BruhTato.Items;

// NEW: Enum specifying attack stats, hitboxes, and durability for each weapon
public enum WeaponType {
    DEFAULT("Fists", 1, 0.4, 150.0, -1),   // Standard infinite-use weapon
    SWORD("Sword", 1, 0.4, 150.0, 20),             // Average stats, circle hitbox, 10 uses
    SPEAR("Spear", 1, 0.7, 180.0, 20),            // Slower speed, long thin rectangle, 10 uses
    AXE("Axe", 3, 1.1, 300.0, 10);                // Slowest, highest damage, big circle, 10 uses

    private final String displayName;
    private final int damage;
    private final double cooldown; // Attack cooldown in seconds
    private final double range;    // Reach / size factor
    private final int maxUses;

    WeaponType(String displayName, int damage, double cooldown, double range, int maxUses) {
        this.displayName = displayName;
        this.damage = damage;
        this.cooldown = cooldown;
        this.range = range;
        this.maxUses = maxUses;
    }

    public String getDisplayName() { return displayName; }
    public int getDamage() { return damage; }
    public double getCooldown() { return cooldown; }
    public double getRange() { return range; }
    public int getMaxUses() { return maxUses; }
}