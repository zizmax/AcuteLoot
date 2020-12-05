package acute.loot;

import org.bukkit.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class LootSpecialEffectTest {

    @Test
    @DisplayName("Effect ID namespace consists of uppercase and underscores")
    public void effectIdUppercaseAndUnderscores() {
        assertThat(EffectId.validNamespace("AL"), is(true));
        assertThat(EffectId.validNamespace(LootSpecialEffect.AL_NS), is(true));
        assertThat(EffectId.validNamespace("FOO"), is(true));
        assertThat(EffectId.validNamespace("HELLO-WORLD"), is(true));
        assertThat(EffectId.validNamespace("A-----B"), is(true));

        assertThat(EffectId.validNamespace("A"), is(false));
        assertThat(EffectId.validNamespace("A------"), is(false));
        assertThat(EffectId.validNamespace("------A"), is(false));
        assertThat(EffectId.validNamespace(""), is(false));
        assertThat(EffectId.validNamespace("    "), is(false));
        assertThat(EffectId.validNamespace("\n\r\t"), is(false));
        assertThat(EffectId.validNamespace(null), is(false));

        assertThrows(IllegalArgumentException.class, () -> new EffectId("---", 1));
        assertThrows(IllegalArgumentException.class, () -> new EffectId("hello", 1));
        assertThrows(IllegalArgumentException.class, () -> new EffectId(null, 1));

        assertDoesNotThrow(() -> new EffectId("AL", 1));
        assertDoesNotThrow(() -> new EffectId("HELLO-WORLD", -100));
    }

    @Test
    @DisplayName("Effect ID can be parsed from String")
    public void effectIdCanBeParsedFromString() {
        assertThat(new EffectId("AL;1234"), is(new EffectId("AL", 1234)));
        assertThat(new EffectId("TEST;-1"), is(new EffectId("TEST", -1)));

        assertThrows(IllegalArgumentException.class, () -> new EffectId(";1"));
        assertThrows(IllegalArgumentException.class, () -> new EffectId("TEST;"));
        assertThrows(IllegalArgumentException.class, () -> new EffectId("TEST_ID"));
        assertThrows(IllegalArgumentException.class, () -> new EffectId("TEST;one"));
        assertThrows(IllegalArgumentException.class, () -> new EffectId("TEST; 1"));

        assertDoesNotThrow(() -> new EffectId("AL;-123"));
        assertDoesNotThrow(() -> new EffectId("TEST;0"));
    }

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
        assertDoesNotThrow(() -> new TestEffect("test", "TE-ST", 1, Arrays.asList(LootMaterial.SWORD, LootMaterial.PICK)));
    }

    @Test
    @DisplayName("Effect constructions fails on invalid namespace")
    public void effectThrowsOnInvalidNamespace() {
        assertThrows(IllegalArgumentException.class, () -> new TestEffect("test", null, 1, Collections.singletonList(LootMaterial.SWORD)));
        assertThrows(IllegalArgumentException.class, () -> new TestEffect("test", "A", 1, Collections.singletonList(LootMaterial.SWORD)));
        assertThrows(IllegalArgumentException.class, () -> new TestEffect("test", "namespace", 1, Collections.singletonList(LootMaterial.SWORD)));
    }

    public static class TestEffect extends LootSpecialEffect {

        public TestEffect(String name, String ns, int id, List<LootMaterial> validMaterials) {
            super(name, ns, id, validMaterials, name.replace('-', ' '));
        }

        @Override
        public void apply(Event event) {}
    }
}
