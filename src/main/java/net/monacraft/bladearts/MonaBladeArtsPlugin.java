package net.monacraft.bladearts;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import net.monacraft.bladearts.command.BladeCommand;
import net.monacraft.bladearts.combat.ClientInputBridge;
import net.monacraft.bladearts.combat.CombatEngine;
import net.monacraft.bladearts.item.BladeItemFactory;
import net.monacraft.bladearts.item.BladeForgeListener;
import net.monacraft.bladearts.item.RecipeService;
import net.monacraft.bladearts.slimefun.BladeGuideIntegration;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class MonaBladeArtsPlugin extends JavaPlugin implements SlimefunAddon {
    private BladeKeys keys;
    private BladeItemFactory items;
    private CombatEngine combat;
    private RecipeService recipes;
    private String channel;

    @Override public void onEnable() {
        saveDefaultConfig();
        keys = new BladeKeys(this);
        items = new BladeItemFactory(keys);
        combat = new CombatEngine(this, items, keys);
        recipes = new RecipeService(this, items);
        getServer().getPluginManager().registerEvents(combat, this);
        getServer().getPluginManager().registerEvents(recipes, this);
        getServer().getPluginManager().registerEvents(new BladeForgeListener(items, keys), this);
        recipes.registerAll();
        new BladeGuideIntegration(this).register();
        BladeCommand handler = new BladeCommand(this);
        PluginCommand command = Objects.requireNonNull(getCommand("bladearts"));
        command.setExecutor(handler); command.setTabCompleter(handler);
        if (getConfig().getBoolean("client-bridge.enabled", true)) {
            channel = getConfig().getString("client-bridge.channel", ClientInputBridge.DEFAULT_CHANNEL);
            getServer().getMessenger().registerIncomingPluginChannel(this, channel, new ClientInputBridge(combat));
        }
        getServer().getScheduler().runTaskTimer(this, combat::tickRanks, 20L, 20L);
        if (getConfig().getBoolean("resource-pack.regenerate-on-start", false)
                && getServer().getPluginManager().isPluginEnabled("CraftEngine")) {
            getServer().getScheduler().runTaskLater(this, () -> {
                getLogger().info("Running the deployment-only CraftEngine pack regeneration");
                getServer().dispatchCommand(getServer().getConsoleSender(), "ce reload all");
            }, 200L);
        }
        getLogger().info("MonaBladeArts enabled with server-authoritative combat and Slimefun guide integration");
    }

    @Override public void onDisable() {
        if (channel != null) getServer().getMessenger().unregisterIncomingPluginChannel(this, channel);
        if (combat != null) combat.clear();
    }
    public BladeKeys keys() { return keys; }
    public BladeItemFactory items() { return items; }
    public CombatEngine combat() { return combat; }
    public RecipeService recipes() { return recipes; }
    @Override public JavaPlugin getJavaPlugin() { return this; }
    @Override public String getBugTrackerURL() { return "https://github.com/MONAKASERVER/MonaBladeArts/issues"; }
}
