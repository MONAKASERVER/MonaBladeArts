package net.monacraft.bladearts.combat;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.monacraft.bladearts.BladeKeys;
import net.monacraft.bladearts.MonaBladeArtsPlugin;
import net.monacraft.bladearts.item.BladeItemFactory;
import net.monacraft.bladearts.item.BladeType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class CombatEngine implements Listener {
    private final MonaBladeArtsPlugin plugin;
    private final BladeItemFactory items;
    private final BladeKeys keys;
    private final Map<UUID, PlayerCombatState> states = new HashMap<>();
    private final Set<UUID> internalDamage = new HashSet<>();
    private final long comboReset;
    private final long globalCooldown;
    private final int maxTargets;
    private final double range;
    private final boolean pvp;

    public CombatEngine(MonaBladeArtsPlugin plugin, BladeItemFactory items, BladeKeys keys) {
        this.plugin = plugin; this.items = items; this.keys = keys;
        comboReset = plugin.getConfig().getLong("combat.combo-reset-ticks", 18);
        globalCooldown = plugin.getConfig().getLong("combat.global-cooldown-ticks", 4);
        maxTargets = plugin.getConfig().getInt("combat.max-targets-per-swing", 8);
        range = plugin.getConfig().getDouble("combat.hit-range", 4.25);
        pvp = plugin.getConfig().getBoolean("combat.pvp-enabled", false);
    }

    private PlayerCombatState state(Player player) { return states.computeIfAbsent(player.getUniqueId(), ignored -> new PlayerCombatState()); }
    private long tick() { return Bukkit.getCurrentTick(); }
    private boolean disabled(Player player) { return plugin.getConfig().getStringList("combat.disabled-worlds").contains(player.getWorld().getName()); }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onVanillaAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (internalDamage.remove(event.getEntity().getUniqueId())) return;
        BladeType blade = items.type(player.getInventory().getItemInMainHand());
        if (blade == null || disabled(player)) return;
        event.setCancelled(true);
        if (event.getEntity() instanceof LivingEntity target) attack(player, target);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSwing(PlayerArmSwingEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (items.type(player.getInventory().getItemInMainHand()) == null || disabled(player)) return;
        attack(player, null);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !event.getAction().isRightClick()) return;
        Player player = event.getPlayer();
        if (items.type(player.getInventory().getItemInMainHand()) == null || disabled(player)) return;
        event.setCancelled(true);
        if (player.isSneaking()) special(player); else summonedSword(player);
    }

    public void attack(Player player, LivingEntity preferred) {
        BladeType blade = items.type(player.getInventory().getItemInMainHand());
        if (blade == null || player.isDead() || disabled(player)) return;
        PlayerCombatState state = state(player);
        long now = tick();
        if (now - state.lastActionTick < globalCooldown) return;
        state.lastActionTick = now;
        state.drawn = true;

        int step = CombatMath.nextCombo(state.comboStep, now - state.lastAttackTick, comboReset, 4);
        state.comboStep = step;
        state.lastAttackTick = now;

        boolean air = !player.isOnGround();
        boolean drawSlash = player.isSneaking();
        boolean dash = player.isSprinting() && !air;
        double multiplier = drawSlash ? 1.45 : air ? (step == 3 ? 1.65 : 1.12) : dash ? 1.30 : switch (step) {
            case 1 -> 0.92; case 2 -> 1.0; case 3 -> 1.18; default -> 1.42;
        };
        double arc = drawSlash ? 120 : step == 4 ? 165 : 78;
        double actionRange = dash ? range + 1.4 : range;
        if (dash) player.setVelocity(player.getVelocity().add(player.getLocation().getDirection().setY(0).normalize().multiply(0.62)));
        if (air && step == 3) player.setVelocity(player.getVelocity().setY(-0.55));

        List<LivingEntity> targets = findTargets(player, preferred, actionRange, arc);
        int refine = items.get(player.getInventory().getItemInMainHand(), keys.refine());
        double damage = blade.damage() * multiplier * CombatMath.refineMultiplier(refine);
        int hitCount = 0;
        for (LivingEntity target : targets) {
            if (!canHit(player, target)) continue;
            deal(player, target, damage);
            applyKnockback(player, target, step, air);
            hitCount++;
        }
        slashEffect(player, step, drawSlash, dash, air, hitCount > 0);
        if (hitCount > 0) addRank(player, state, 8 + hitCount * 3 + step * 2);
        damageBlade(player, 1);
    }

    public void summonedSword(Player player) {
        PlayerCombatState state = state(player);
        long now = tick();
        long cooldown = plugin.getConfig().getLong("combat.summoned-sword-cooldown-ticks", 12);
        if (now - state.lastSummonedSwordTick < cooldown) return;
        int cost = plugin.getConfig().getInt("souls.summoned-sword-cost", 5);
        ItemStack blade = player.getInventory().getItemInMainHand();
        int souls = items.get(blade, keys.souls());
        if (souls < cost) { player.sendActionBar(Component.text("魂量が不足しています", NamedTextColor.RED)); return; }
        state.lastSummonedSwordTick = now;
        items.setCombatData(blade, items.get(blade, keys.kills()), items.get(blade, keys.refine()), souls - cost);
        Location start = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.6));
        Vector direction = start.getDirection().normalize();
        var result = player.getWorld().rayTraceEntities(start, direction, 24, 0.35, entity -> entity instanceof LivingEntity living && canHit(player, living));
        for (double distance = 0; distance <= (result == null ? 24 : result.getHitPosition().distance(start.toVector())); distance += 0.7) {
            Location point = start.clone().add(direction.clone().multiply(distance));
            player.getWorld().spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
        }
        if (result != null && result.getHitEntity() instanceof LivingEntity target) {
            BladeType type = items.type(blade);
            deal(player, target, type.damage() * 0.8);
            addRank(player, state, 14);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ALLAY_ITEM_THROWN, SoundCategory.PLAYERS, 1.1f, 1.65f);
    }

    public void special(Player player) {
        PlayerCombatState state = state(player);
        long now = tick();
        long cooldown = plugin.getConfig().getLong("combat.special-cooldown-ticks", 80);
        if (now - state.lastSpecialTick < cooldown) {
            player.sendActionBar(Component.text("奥義再使用まで " + ((cooldown - (now - state.lastSpecialTick)) / 20.0) + "秒", NamedTextColor.YELLOW));
            return;
        }
        int cost = plugin.getConfig().getInt("souls.special-cost", 30);
        ItemStack blade = player.getInventory().getItemInMainHand();
        int souls = items.get(blade, keys.souls());
        if (souls < cost) { player.sendActionBar(Component.text("奥義には魂量" + cost + "が必要です", NamedTextColor.RED)); return; }
        state.lastSpecialTick = now;
        items.setCombatData(blade, items.get(blade, keys.kills()), items.get(blade, keys.refine()), souls - cost);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.45f, 1.8f);
        List<LivingEntity> targets = findTargets(player, null, 8.0, 170);
        BladeType type = items.type(blade);
        for (int wave = 0; wave < 5; wave++) {
            final int pass = wave;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline() || items.type(player.getInventory().getItemInMainHand()) == null) return;
                for (LivingEntity target : targets) {
                    if (!target.isValid() || target.isDead() || !canHit(player, target)) continue;
                    deal(player, target, type.damage() * (pass == 4 ? 1.2 : 0.36));
                    effectLine(target.getLocation().add(0, target.getHeight() * 0.5, 0), pass);
                }
            }, wave * 3L);
        }
        addRank(player, state, 35);
        damageBlade(player, 3);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onGuard(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !player.isSneaking()) return;
        if (items.type(player.getInventory().getItemInMainHand()) == null || !plugin.getConfig().getBoolean("guard.enabled", true)) return;
        PlayerCombatState state = state(player);
        long now = tick();
        long window = plugin.getConfig().getLong("guard.just-guard-window-ticks", 5);
        if (now - state.sneakStartedTick <= window && now - state.lastJustGuardTick > plugin.getConfig().getLong("guard.just-guard-cooldown-ticks", 16)) {
            event.setCancelled(true);
            state.lastJustGuardTick = now;
            player.getWorld().spawnParticle(Particle.FLASH, player.getEyeLocation(), 1);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.2f, 1.8f);
            addRank(player, state, 22);
            if (event instanceof EntityDamageByEntityEvent by && plugin.getConfig().getBoolean("guard.reflect-projectiles", true)) reflect(player, by.getDamager());
        } else {
            event.setDamage(event.getDamage() * (1.0 - plugin.getConfig().getDouble("guard.damage-reduction", 0.70)));
        }
    }

    @EventHandler public void onSneak(PlayerToggleSneakEvent event) { if (event.isSneaking()) state(event.getPlayer()).sneakStartedTick = tick(); }

    @EventHandler(ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        ItemStack blade = killer.getInventory().getItemInMainHand();
        BladeType type = items.type(blade);
        if (type == null) return;
        int kills = items.get(blade, keys.kills()) + 1;
        int souls = Math.min(plugin.getConfig().getInt("souls.maximum", 100), items.get(blade, keys.souls()) + plugin.getConfig().getInt("souls.gain-per-kill", 3));
        items.setCombatData(blade, kills, items.get(blade, keys.refine()), souls);
        double fragmentChance = isBoss(event.getEntity()) ? 0.65 : 0.18;
        if (ThreadLocalRandom.current().nextDouble() < fragmentChance) event.getDrops().add(items.soul(net.monacraft.bladearts.item.SoulItem.FRAGMENT, 1));
        killer.sendActionBar(Component.text("魂量 " + souls + "/100  •  討伐 " + kills, NamedTextColor.LIGHT_PURPLE));
    }

    public void tickRanks() {
        long now = tick();
        int delay = plugin.getConfig().getInt("combat.rank-decay-delay-ticks", 100);
        int decay = plugin.getConfig().getInt("combat.rank-decay-per-second", 4);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerCombatState state = states.get(player.getUniqueId());
            if (state == null || items.type(player.getInventory().getItemInMainHand()) == null) continue;
            if (now - state.lastRankGainTick > delay) state.rankScore = CombatMath.clampedRankScore(state.rankScore - decay);
            CombatRank rank = CombatRank.fromScore(state.rankScore);
            player.sendActionBar(Component.text("戦闘評価 " + rank.label(), rank.ordinal() >= CombatRank.S.ordinal() ? NamedTextColor.GOLD : NamedTextColor.AQUA)
                    .append(Component.text("  " + state.rankScore + "pt", NamedTextColor.GRAY)));
        }
    }

    @EventHandler public void onQuit(PlayerQuitEvent event) { states.remove(event.getPlayer().getUniqueId()); }
    @EventHandler public void onWorld(PlayerChangedWorldEvent event) { states.remove(event.getPlayer().getUniqueId()); }
    public PlayerCombatState inspect(Player player) { return state(player); }
    public void clear() { states.clear(); internalDamage.clear(); }

    private List<LivingEntity> findTargets(Player player, LivingEntity preferred, double maxRange, double arcDegrees) {
        List<LivingEntity> result = new ArrayList<>();
        if (preferred != null && canHit(player, preferred) && player.getLocation().distanceSquared(preferred.getLocation()) <= maxRange * maxRange) result.add(preferred);
        Vector look = player.getEyeLocation().getDirection().normalize();
        double threshold = Math.cos(Math.toRadians(arcDegrees / 2.0));
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), maxRange, maxRange, maxRange)) {
            if (!(entity instanceof LivingEntity living) || living == player || result.contains(living) || !canHit(player, living)) continue;
            Vector toward = living.getEyeLocation().toVector().subtract(player.getEyeLocation().toVector());
            if (toward.lengthSquared() == 0 || toward.normalize().dot(look) < threshold) continue;
            result.add(living);
        }
        result.sort(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(player.getLocation())));
        return result.size() > maxTargets ? new ArrayList<>(result.subList(0, maxTargets)) : result;
    }

    private boolean canHit(Player attacker, LivingEntity target) {
        if (!target.isValid() || target.isDead() || isProtectedNpc(target)) return false;
        if (target instanceof Player playerTarget) {
            if (!pvp || playerTarget.getGameMode() == GameMode.CREATIVE || playerTarget.getGameMode() == GameMode.SPECTATOR) return false;
        }
        return target.getWorld() == attacker.getWorld();
    }

    private boolean isProtectedNpc(LivingEntity entity) {
        if (entity.hasMetadata("NPC") || entity.hasMetadata("shopkeeper")) return true;
        for (String tag : entity.getScoreboardTags()) {
            String lower = tag.toLowerCase(java.util.Locale.ROOT);
            if (lower.contains("npc") || lower.contains("shopkeeper") || lower.contains("citizens")) return true;
        }
        return false;
    }

    private boolean isBoss(LivingEntity entity) {
        for (String tag : entity.getScoreboardTags()) {
            String lower = tag.toLowerCase(java.util.Locale.ROOT);
            if (lower.contains("boss") || lower.contains("mythicmob")) return true;
        }
        return entity.getPersistentDataContainer().getKeys().stream().anyMatch(key -> key.getKey().toLowerCase(java.util.Locale.ROOT).contains("boss"));
    }

    private void deal(Player player, LivingEntity target, double amount) {
        internalDamage.add(target.getUniqueId());
        try { target.damage(Math.max(0, amount), player); }
        finally { internalDamage.remove(target.getUniqueId()); }
    }

    private void applyKnockback(Player player, LivingEntity target, int step, boolean air) {
        Vector away = target.getLocation().toVector().subtract(player.getLocation().toVector()).setY(0);
        if (away.lengthSquared() > 0) away.normalize().multiply(step == 4 ? 0.55 : 0.18);
        if (air) away.setY(0.25); else if (step == 3) away.setY(0.42);
        target.setVelocity(target.getVelocity().add(away));
    }

    private void slashEffect(Player player, int step, boolean draw, boolean dash, boolean air, boolean hit) {
        Location center = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.5));
        Color color = draw ? Color.fromRGB(210, 235, 255) : dash ? Color.fromRGB(80, 235, 195) : air ? Color.fromRGB(180, 120, 255) : Color.fromRGB(245, 245, 255);
        Particle.DustOptions dust = new Particle.DustOptions(color, 0.9f);
        Vector forward = player.getEyeLocation().getDirection().normalize();
        Vector side = forward.clone().crossProduct(new Vector(0, 1, 0));
        if (side.lengthSquared() < 0.01) side = new Vector(1, 0, 0); else side.normalize();
        for (int i = -8; i <= 8; i++) {
            double angle = Math.toRadians(i * 7.5 + step * 8);
            Location point = center.clone().add(side.clone().multiply(Math.sin(angle) * 2.0)).add(0, Math.cos(angle) * 1.15, 0);
            player.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, dust);
        }
        player.swingMainHand();
        player.getWorld().playSound(player.getLocation(), hit ? Sound.ENTITY_PLAYER_ATTACK_SWEEP : Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, SoundCategory.PLAYERS, 1.0f, 1.25f + step * 0.08f);
    }

    private void effectLine(Location center, int pass) {
        Particle.DustOptions dust = new Particle.DustOptions(pass == 4 ? Color.WHITE : Color.fromRGB(130, 70, 255), pass == 4 ? 1.4f : 0.8f);
        double angle = Math.toRadians(pass * 36.0);
        for (double d = -2.5; d <= 2.5; d += 0.25) center.getWorld().spawnParticle(Particle.DUST, center.clone().add(Math.cos(angle) * d, Math.sin(angle) * d, Math.sin(angle * 0.5) * d), 1, 0, 0, 0, 0, dust);
        center.getWorld().playSound(center, Sound.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 0.8f, 1.2f + pass * 0.1f);
    }

    private void reflect(Player player, Entity source) {
        if (!(source instanceof Projectile projectile)) return;
        ProjectileSource shooter = projectile.getShooter();
        projectile.setShooter(player);
        projectile.setVelocity(projectile.getVelocity().multiply(-1.25));
        if (projectile instanceof AbstractArrow arrow) arrow.setCritical(true);
        if (shooter instanceof LivingEntity living) projectile.setRotation(living.getYaw(), living.getPitch());
    }

    private void addRank(Player player, PlayerCombatState state, int points) {
        state.rankScore = CombatMath.clampedRankScore(state.rankScore + points);
        state.lastRankGainTick = tick();
        player.getPersistentDataContainer().set(keys.rankScore(), PersistentDataType.INTEGER, state.rankScore);
        player.getPersistentDataContainer().set(keys.lastRankUpdate(), PersistentDataType.LONG, System.currentTimeMillis());
    }

    private void damageBlade(Player player, int amount) {
        ItemStack stack = player.getInventory().getItemInMainHand();
        if (!(stack.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable)) return;
        int next = damageable.getDamage() + amount;
        if (next >= damageable.getMaxDamage()) {
            player.getInventory().setItemInMainHand(null);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            return;
        }
        damageable.setDamage(next);
        stack.setItemMeta(damageable);
    }
}
