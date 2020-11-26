package acute.loot;

import org.bukkit.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LootSpecialEffectTest {

    @Test
    @DisplayName("Effect name cannot contain spaces or be empty")
    public void effectNameCannotHaveSpaces() {
        final List<LootMaterial> materials = Collections.singletonList(LootMaterial.SWORD);
        assertThrows(IllegalArgumentException.class, () -> new TestEffect("", "AL", 1, materials));
        assertThrows(IllegalArgumentException.class, () -> new TestEffect("  ", "AL", 1, materials));
        assertThrows(IllegalArgumentException.class, () -> new TestEffect("test effect", "AL", 1, materials));
        assertThrows(IllegalArgumentException.class, () -> new TestEffect("\n", "AL", 1, materials));
        assertDoesNotThrow(() -> new TestEffect("test", "AL", 1, materials));
        assertDoesNotThrow(() -> new TestEffect("test-effect", "FOO", 1, materials));
        assertDoesNotThrow(() -> new TestEffect("TeSt-EFfect", "TEST", 1, materials));
    }

    @Test
    @DisplayName("Valid materials list cannot be null or empty")
    public void validMaterialsCannotBeNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new TestEffect("test", "AL", 1, Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> new TestEffect("test", "AL", 1, null));
        assertDoesNotThrow(() -> new TestEffect("test", "LA", 1, Collections.singletonList(LootMaterial.SWORD)));
        assertDoesNotThrow(() -> new TestEffect("test", "123", 1, Arrays.asList(LootMaterial.SWORD, LootMaterial.PICK)));
    }

    public static class TestEffect extends LootSpecialEffect {

        public TestEffect(String name, String ns, int id, List<LootMaterial> validMaterials) {
            super(name, ns, id, validMaterials, null);
        }

        @Override
        public void apply(Event event) {}
    }
}
