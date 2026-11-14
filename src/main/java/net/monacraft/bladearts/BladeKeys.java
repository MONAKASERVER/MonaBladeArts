package net.monacraft.bladearts;

import org.bukkit.NamespacedKey;

public record BladeKeys(NamespacedKey id, NamespacedKey kills, NamespacedKey refine, NamespacedKey souls,
                        NamespacedKey rankScore, NamespacedKey lastRankUpdate) {
    public BladeKeys(MonaBladeArtsPlugin plugin) {
        this(new NamespacedKey(plugin, "blade_id"), new NamespacedKey(plugin, "kills"),
                new NamespacedKey(plugin, "refine"), new NamespacedKey(plugin, "souls"),
                new NamespacedKey(plugin, "rank_score"), new NamespacedKey(plugin, "last_rank_update"));
    }
}
