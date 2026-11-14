package net.monacraft.bladearts;

import net.monacraft.bladearts.combat.CombatMath;
import net.monacraft.bladearts.combat.CombatRank;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CombatMathTest {
    @Test void comboAdvancesAndResets() {
        assertEquals(3, CombatMath.nextCombo(2, 4, 18, 4));
        assertEquals(1, CombatMath.nextCombo(4, 4, 18, 4));
        assertEquals(1, CombatMath.nextCombo(3, 19, 18, 4));
    }
    @Test void refineIsBounded() {
        assertEquals(1.0, CombatMath.refineMultiplier(-2));
        assertEquals(1.25, CombatMath.refineMultiplier(10));
        assertEquals(1.75, CombatMath.refineMultiplier(99));
    }
    @Test void rankBoundariesAreStable() {
        assertEquals(CombatRank.NONE, CombatRank.fromScore(19));
        assertEquals(CombatRank.D, CombatRank.fromScore(20));
        assertEquals(CombatRank.SSS, CombatRank.fromScore(640));
    }
}
