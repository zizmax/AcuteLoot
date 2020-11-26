package acute.loot;

import org.bukkit.ChatColor;
import org.bukkit.event.Event;

import java.util.Arrays;
import java.util.List;

public class TestHelper {

    public final LootRarity common;
    public final LootRarity uncommon;
    public final LootRarity rare;

    public final VoidEffect effect1;
    public final VoidEffect effect2;
    public final VoidEffect effect3;

    public final List<LootRarity> rarities;
    public final List<LootSpecialEffect> effects;

    public TestHelper() {
        common = new LootRarity(-10, "Common", 0.0, ChatColor.AQUA.toString());
        uncommon = new LootRarity(1, "Uncommon", 0.1, ChatColor.BLACK.toString());
        rare = new LootRarity(25, "Rare", 0.3, ChatColor.BLUE.toString());

        final List<LootMaterial> matList = Arrays.asList(LootMaterial.SWORD, LootMaterial.PICK);
        effect1 = new VoidEffect("effect-1", 1, matList, null);
        effect2 = new VoidEffect("effect-2", -100, matList, null);
        effect3 = new VoidEffect("effect-3", 1234, matList, null);

        rarities = Arrays.asList(common, uncommon, rare);
        effects = Arrays.asList(effect1, effect2, effect3);
    }

    /**
     * Set up a "standard" set of test effects, rarities, and items
     */
    public void addTestResources() {
        LootRarity.registerRarity(common);
        LootRarity.registerRarity(uncommon);
        LootRarity.registerRarity(rare);

        LootSpecialEffect.registerEffect(effect1);
        LootSpecialEffect.registerEffect(effect2);
        LootSpecialEffect.registerEffect(effect3);
    }

    public static class VoidEffect extends LootSpecialEffect {

        public VoidEffect(String name, int id, List<LootMaterial> validMaterials, AcuteLoot plugin) {
            super(name, id, validMaterials, plugin);
        }

        @Override
        public void apply(Event event) {}
    }
}
