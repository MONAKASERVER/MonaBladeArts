package net.monacraft.bladearts.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.monacraft.bladearts.BladeKeys;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class BladeItemFactory {
    private final BladeKeys keys;
    public BladeItemFactory(BladeKeys keys) { this.keys = keys; }

    public ItemStack blade(BladeType type) {
        ItemStack stack = craftEngine(type.model(), type.material());
        ItemMeta meta = stack.getItemMeta();
        meta.setItemModel(NamespacedKey.fromString(type.model()));
        meta.displayName(Component.text(type.displayName(), NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                line("抜刀・納刀・地上/空中連撃対応", NamedTextColor.GRAY),
                line("基礎威力: " + type.damage(), NamedTextColor.RED),
                line("耐久: " + type.durability(), NamedTextColor.YELLOW),
                line("討伐数: 0  /  精錬: +0", NamedTextColor.DARK_AQUA),
                line("魂量: 0/100", NamedTextColor.LIGHT_PURPLE)));
        meta.getPersistentDataContainer().set(keys.id(), PersistentDataType.STRING, type.id());
        meta.getPersistentDataContainer().set(keys.kills(), PersistentDataType.INTEGER, 0);
        meta.getPersistentDataContainer().set(keys.refine(), PersistentDataType.INTEGER, 0);
        meta.getPersistentDataContainer().set(keys.souls(), PersistentDataType.INTEGER, 0);
        if (meta instanceof Damageable damageable) damageable.setMaxDamage(type.durability());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack soul(SoulItem type, int amount) {
        ItemStack stack = craftEngine(type.model(), type.material());
        stack.setAmount(amount);
        ItemMeta meta = stack.getItemMeta();
        meta.setItemModel(NamespacedKey.fromString(type.model()));
        meta.displayName(Component.text(type.displayName(), NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.getPersistentDataContainer().set(keys.id(), PersistentDataType.STRING, type.id());
        stack.setItemMeta(meta);
        return stack;
    }

    public BladeType type(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) return null;
        String id = stack.getItemMeta().getPersistentDataContainer().get(keys.id(), PersistentDataType.STRING);
        return id == null ? null : BladeType.byId(id);
    }

    public String id(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) return null;
        return stack.getItemMeta().getPersistentDataContainer().get(keys.id(), PersistentDataType.STRING);
    }

    public int get(ItemStack stack, org.bukkit.NamespacedKey key) {
        if (stack == null || !stack.hasItemMeta()) return 0;
        return stack.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public void setCombatData(ItemStack stack, int kills, int refine, int souls) {
        BladeType type = type(stack);
        if (type == null) return;
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(keys.kills(), PersistentDataType.INTEGER, Math.max(0, kills));
        meta.getPersistentDataContainer().set(keys.refine(), PersistentDataType.INTEGER, Math.max(0, refine));
        meta.getPersistentDataContainer().set(keys.souls(), PersistentDataType.INTEGER, Math.max(0, Math.min(100, souls)));
        meta.lore(List.of(
                line("抜刀・納刀・地上/空中連撃対応", NamedTextColor.GRAY),
                line("基礎威力: " + type.damage(), NamedTextColor.RED),
                line("耐久: " + type.durability(), NamedTextColor.YELLOW),
                line("討伐数: " + kills + "  /  精錬: +" + refine, NamedTextColor.DARK_AQUA),
                line("魂量: " + souls + "/100", NamedTextColor.LIGHT_PURPLE)));
        stack.setItemMeta(meta);
    }

    public void damage(ItemStack stack, int amount) {
        if (stack == null || !(stack.getItemMeta() instanceof Damageable damageable)) return;
        damageable.setDamage(Math.min(damageable.getMaxDamage(), damageable.getDamage() + Math.max(0, amount)));
        stack.setItemMeta(damageable);
    }

    private ItemStack craftEngine(String id, org.bukkit.Material fallback) {
        try {
            var definition = CraftEngineItems.byId(id);
            if (definition != null) return definition.buildBukkitItem();
        } catch (LinkageError | RuntimeException ignored) {}
        return new ItemStack(fallback);
    }
    private static Component line(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }
}
