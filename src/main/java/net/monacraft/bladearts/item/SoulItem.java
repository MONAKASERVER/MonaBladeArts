package net.monacraft.bladearts.item;

import org.bukkit.Material;

public enum SoulItem {
    FRAGMENT("soul_fragment", "刀魂の欠片", Material.AMETHYST_SHARD, "monabladearts:soul_fragment"),
    SOUL("blade_soul", "凝縮刀魂", Material.ECHO_SHARD, "monabladearts:blade_soul"),
    INGOT("soul_ingot", "魂鋼", Material.NETHERITE_INGOT, "monabladearts:soul_ingot"),
    CRYSTAL("soul_crystal", "魂晶核", Material.PRISMARINE_CRYSTALS, "monabladearts:soul_crystal"),
    SPHERE("soul_sphere", "霊環珠", Material.ENDER_EYE, "monabladearts:soul_sphere"),
    TRAPEZOHEDRON("soul_trapezohedron", "星幽多面核", Material.NETHER_STAR, "monabladearts:soul_trapezohedron");

    private final String id, displayName, model;
    private final Material material;
    SoulItem(String id, String displayName, Material material, String model) {
        this.id = id; this.displayName = displayName; this.material = material; this.model = model;
    }
    public String id() { return id; }
    public String displayName() { return displayName; }
    public Material material() { return material; }
    public String model() { return model; }
}
