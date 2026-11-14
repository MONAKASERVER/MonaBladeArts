package net.monacraft.bladearts.item;

import org.bukkit.Material;

public enum BladeType {
    NAMELESS("nameless", "無銘刀《黎明》", Material.IRON_SWORD, 7.0, 480, 0, "monabladearts:blade_nameless"),
    WHITE("white", "白耀刀《月白》", Material.DIAMOND_SWORD, 9.0, 960, 25, "monabladearts:blade_white"),
    CRIMSON("crimson", "緋焔刀《赫灼》", Material.NETHERITE_SWORD, 11.0, 1550, 75, "monabladearts:blade_crimson"),
    GALE("gale", "翠嵐刀《風哭》", Material.DIAMOND_SWORD, 10.0, 1320, 55, "monabladearts:blade_gale"),
    VOID("void", "虚天刀《星蝕》", Material.NETHERITE_SWORD, 13.0, 2100, 150, "monabladearts:blade_void");

    private final String id;
    private final String displayName;
    private final Material material;
    private final double damage;
    private final int durability;
    private final int requiredKills;
    private final String model;

    BladeType(String id, String displayName, Material material, double damage, int durability, int requiredKills, String model) {
        this.id = id; this.displayName = displayName; this.material = material; this.damage = damage;
        this.durability = durability; this.requiredKills = requiredKills; this.model = model;
    }
    public String id() { return id; }
    public String displayName() { return displayName; }
    public Material material() { return material; }
    public double damage() { return damage; }
    public int durability() { return durability; }
    public int requiredKills() { return requiredKills; }
    public String model() { return model; }
    public static BladeType byId(String id) {
        for (BladeType value : values()) if (value.id.equalsIgnoreCase(id)) return value;
        return null;
    }
}
