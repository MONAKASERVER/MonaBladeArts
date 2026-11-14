package net.monacraft.bladearts.combat;

public enum CombatRank {
    NONE(0, "-"), D(20, "D"), C(60, "C"), B(120, "B"), A(210, "A"), S(320, "S"), SS(460, "SS"), SSS(640, "SSS");
    private final int minimum;
    private final String label;
    CombatRank(int minimum, String label) { this.minimum = minimum; this.label = label; }
    public int minimum() { return minimum; }
    public String label() { return label; }
    public static CombatRank fromScore(int score) {
        CombatRank result = NONE;
        for (CombatRank rank : values()) if (score >= rank.minimum) result = rank;
        return result;
    }
}
