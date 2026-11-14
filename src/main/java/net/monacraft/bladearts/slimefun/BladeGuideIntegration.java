package net.monacraft.bladearts.slimefun;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.monacraft.bladearts.MonaBladeArtsPlugin;
import net.monacraft.bladearts.item.BladeType;
import net.monacraft.bladearts.item.SoulItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class BladeGuideIntegration {
    private final MonaBladeArtsPlugin plugin;
    public BladeGuideIntegration(MonaBladeArtsPlugin plugin) { this.plugin = plugin; }

    public void register() {
        if (!plugin.getConfig().getBoolean("slimefun-guide.enabled", true)) return;
        NestedItemGroup root = new NestedItemGroup(key("root"), icon(Material.NETHERITE_SWORD, "刀剣戦闘", NamedTextColor.AQUA));
        SubItemGroup blades = new SubItemGroup(key("blades"), root, icon(Material.DIAMOND_SWORD, "刀剣・鍛錬", NamedTextColor.RED), 1);
        SubItemGroup souls = new SubItemGroup(key("souls"), root, icon(Material.ECHO_SHARD, "刀魂素材", NamedTextColor.LIGHT_PURPLE), 2);
        root.register(plugin); blades.register(plugin); souls.register(plugin);
        RecipeType table = new RecipeType(key("crafting"), icon(Material.CRAFTING_TABLE, "通常の作業台", NamedTextColor.GREEN));
        for (BladeType type : BladeType.values()) {
            SlimefunItemStack display = new SlimefunItemStack("MONABLADEARTS_" + type.name(), plugin.items().blade(type));
            new SlimefunItem(blades, display, table, plugin.recipes().guideRecipe(type)).register(plugin);
        }
        for (SoulItem type : SoulItem.values()) {
            SlimefunItemStack display = new SlimefunItemStack("MONABLADEARTS_" + type.name(), plugin.items().soul(type, 1));
            ItemStack unit = type == SoulItem.FRAGMENT ? new ItemStack(Material.AMETHYST_SHARD) : plugin.items().soul(SoulItem.values()[type.ordinal() - 1], 1);
            ItemStack[] recipe = type == SoulItem.FRAGMENT ? new ItemStack[0] : new ItemStack[]{unit, unit, unit, unit, new ItemStack(Material.AMETHYST_BLOCK), unit, unit, unit, unit};
            new SlimefunItem(souls, display, type == SoulItem.FRAGMENT ? RecipeType.NULL : table, recipe).register(plugin);
        }
        plugin.getLogger().info("Registered blade combat content in the Slimefun guide");
    }

    private NamespacedKey key(String value) { return new NamespacedKey(plugin, "guide_" + value); }
    private ItemStack icon(Material material, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }
}
