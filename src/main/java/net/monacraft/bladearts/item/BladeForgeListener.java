package net.monacraft.bladearts.item;

import net.monacraft.bladearts.BladeKeys;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

public final class BladeForgeListener implements Listener {
    private final BladeItemFactory items;
    private final BladeKeys keys;
    public BladeForgeListener(BladeItemFactory items, BladeKeys keys) { this.items = items; this.keys = keys; }

    @EventHandler
    public void onPrepare(PrepareAnvilEvent event) {
        ItemStack blade = event.getInventory().getFirstItem();
        ItemStack catalyst = event.getInventory().getSecondItem();
        if (items.type(blade) == null || !SoulItem.INGOT.id().equals(items.id(catalyst))) return;
        int refine = items.get(blade, keys.refine());
        if (refine >= 30) { event.setResult(null); return; }
        ItemStack result = blade.clone();
        items.setCombatData(result, items.get(blade, keys.kills()), refine + 1, items.get(blade, keys.souls()));
        event.setResult(result);
        event.getView().setRepairCost(Math.min(39, 5 + refine));
    }
}
