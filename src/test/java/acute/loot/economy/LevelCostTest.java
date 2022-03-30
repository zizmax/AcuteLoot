package acute.loot.economy;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class LevelCostTest {

    @Test
    void constructor() {
        assertThrows(IllegalArgumentException.class, () -> new LevelCost(-1));
        assertThrows(IllegalArgumentException.class, () -> new LevelCost(0));
        assertDoesNotThrow(() -> new LevelCost(1));
    }

    @Test
    void pay() {
        final LevelCost cost = new LevelCost(1337);
        final Player player = mock(Player.class);

        when(player.getLevel()).thenReturn(1336);
        assertThat("Levels are not deducted when there are not enough", !cost.pay(player));
        verify(player, never()).giveExpLevels(anyInt());

        when(player.getLevel()).thenReturn(1338);
        assertThat("Levels are deducted when there are enough", cost.pay(player));
        verify(player).giveExpLevels(-1337);
    }
}