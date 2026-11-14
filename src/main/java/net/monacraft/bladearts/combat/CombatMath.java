package net.monacraft.bladearts.combat;

public final class CombatMath {
    private CombatMath() {}
    public static int nextCombo(int current, long elapsedTicks, long resetTicks, int max) {
        return elapsedTicks > resetTicks ? 1 : current % max + 1;
    }
    public static double refineMultiplier(int refine) { return 1.0 + Math.min(30, Math.max(0, refine)) * 0.025; }
    public static int clampedRankScore(int score) { return Math.max(0, Math.min(999, score)); }
}
