package net.monacraft.bladearts.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.monacraft.bladearts.MonaBladeArtsPlugin;
import net.monacraft.bladearts.combat.CombatRank;
import net.monacraft.bladearts.item.BladeType;
import net.monacraft.bladearts.item.SoulItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class BladeCommand implements CommandExecutor, TabCompleter {
    private final MonaBladeArtsPlugin plugin;
    public BladeCommand(MonaBladeArtsPlugin plugin) { this.plugin = plugin; }
    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("guide")) {
            sender.sendMessage(Component.text("刀剣戦闘 操作", NamedTextColor.AQUA));
            sender.sendMessage(Component.text("左攻撃: 4段連撃 / ダッシュ攻撃: 踏み込み / 空中攻撃: 空中連撃", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("右クリック: 召喚剣 / スニーク+右クリック: 奥義 / スニーク: 防御・直前防御", NamedTextColor.GRAY));
            return true;
        }
        if (args[0].equalsIgnoreCase("status") && sender instanceof Player player) {
            var state = plugin.combat().inspect(player);
            sender.sendMessage(Component.text("戦闘評価: " + CombatRank.fromScore(state.rankScore).label() + " (" + state.rankScore + "pt)", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("連撃段階: " + state.comboStep + " / 抜刀状態: " + state.drawn, NamedTextColor.GRAY));
            return true;
        }
        if (!sender.hasPermission("monabladearts.admin")) { sender.sendMessage(Component.text("権限がありません", NamedTextColor.RED)); return true; }
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(Component.text("設定を再読込しました。戦闘定数の一部は再起動後に反映されます。", NamedTextColor.GREEN));
            return true;
        }
        if (args[0].equalsIgnoreCase("give") && args.length >= 3) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) { sender.sendMessage(Component.text("対象がオンラインではありません", NamedTextColor.RED)); return true; }
            String id = args[2].toLowerCase(Locale.ROOT);
            int amount = args.length >= 4 ? Math.max(1, Math.min(64, parse(args[3], 1))) : 1;
            BladeType blade = BladeType.byId(id);
            ItemStack result = null;
            if (blade != null) result = plugin.items().blade(blade);
            if (result == null) for (SoulItem soul : SoulItem.values()) if (soul.id().equalsIgnoreCase(id)) { result = plugin.items().soul(soul, amount); break; }
            if (result == null) { sender.sendMessage(Component.text("不明なアイテムIDです", NamedTextColor.RED)); return true; }
            target.getInventory().addItem(result);
            sender.sendMessage(Component.text(target.getName() + "へ付与しました", NamedTextColor.GREEN));
            return true;
        }
        return false;
    }

    @Override public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return prefix(List.of("guide", "status", "give", "reload"), args[0]);
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) return prefix(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> ids = new ArrayList<>();
            Arrays.stream(BladeType.values()).map(BladeType::id).forEach(ids::add);
            Arrays.stream(SoulItem.values()).map(SoulItem::id).forEach(ids::add);
            return prefix(ids, args[2]);
        }
        return List.of();
    }
    private List<String> prefix(List<String> values, String prefix) { return values.stream().filter(v -> v.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))).toList(); }
    private int parse(String value, int fallback) { try { return Integer.parseInt(value); } catch (NumberFormatException ignored) { return fallback; } }
}
