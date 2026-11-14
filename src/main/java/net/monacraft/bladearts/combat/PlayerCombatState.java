package net.monacraft.bladearts.combat;

public final class PlayerCombatState {
    public int comboStep;
    public long lastAttackTick;
    public long lastActionTick;
    public long lastSummonedSwordTick;
    public long lastSpecialTick;
    public long sneakStartedTick;
    public long lastJustGuardTick;
    public int rankScore;
    public long lastRankGainTick;
    public boolean drawn;
}
