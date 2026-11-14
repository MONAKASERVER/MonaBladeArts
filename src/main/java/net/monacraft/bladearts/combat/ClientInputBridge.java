package net.monacraft.bladearts.combat;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Optional client bridge. Packets are only input intents; all validation remains server-side. */
public final class ClientInputBridge implements PluginMessageListener {
    public static final String DEFAULT_CHANNEL = "monabladearts:input";
    private final CombatEngine combat;
    public ClientInputBridge(CombatEngine combat) { this.combat = combat; }
    @Override public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (message.length == 0 || message.length > 32) return;
        String action = new String(message, StandardCharsets.UTF_8).trim().toUpperCase(Locale.ROOT);
        switch (action) {
            case "ATTACK" -> combat.attack(player, null);
            case "SUMMON" -> combat.summonedSword(player);
            case "SPECIAL" -> combat.special(player);
            default -> { }
        }
    }
}
