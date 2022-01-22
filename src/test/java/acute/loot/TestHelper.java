package acute.loot;

import acute.loot.namegen.NameGenerator;
import com.github.phillip.h.acutelib.collections.IntegerChancePool;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;

import java.util.*;

/**
 * This class will set up a "standard" set of test effects and rarities.
 */
public class TestHelper {

    public final LootRarity common;
    public final LootRarity uncommon;
    public final LootRarity rare;

    public final VoidEffect effect1;
    public final VoidEffect effect2;
    public final VoidEffect effect3;
    public final VoidEffect ns_effect1;
    public final VoidEffect ns_effect2;
    public final VoidEffect ns_effect3;

    public final NameGenerator capitalGenerator;
    public final NameGenerator numberGenerator;
    public final NameGenerator matEchoGenerator;
    public final NameGenerator rarityEchoGenerator;

    public final List<LootRarity> rarities;
    public final List<LootSpecialEffect> effects;

    public final IntegerChancePool<LootRarity> rarityChancePool;
    public final IntegerChancePool<LootSpecialEffect> effectChancePool;
    public final IntegerChancePool<NameGenerator> nameGeneratorChancePool;

    public TestHelper() {
        common = new LootRarity(-10, "Common", 0.0, ChatColor.AQUA.toString());
        uncommon = new LootRarity(1, "Uncommon", 0.1, ChatColor.BLACK.toString());
        rare = new LootRarity(25, "Rare", 0.3, ChatColor.BLUE.toString());

        final List<LootMaterial> matList = Arrays.asList(LootMaterial.SWORD, LootMaterial.PICK);
        effect1 = new VoidEffect("effect-1", LootSpecialEffect.AL_NS, 1, matList);
        effect2 = new VoidEffect("effect-2", LootSpecialEffect.AL_NS, -100, matList);
        effect3 = new VoidEffect("effect-3", LootSpecialEffect.AL_NS, 1234, matList);
        ns_effect1 = new VoidEffect("ns-effect-1", "NS-A", 1, matList);
        ns_effect2 = new VoidEffect("ns-effect-2", "NS-A", 2, matList);
        ns_effect3 = new VoidEffect("ns-effect-2", "NS-B", 1, matList);

        rarities = Arrays.asList(common, uncommon, rare);
        effects = Arrays.asList(effect1, effect2, effect3, ns_effect1, ns_effect2, ns_effect3);

        rarityChancePool = new IntegerChancePool<>();
        rarityChancePool.add(common, 6);
        rarityChancePool.add(uncommon, 3);
        rarityChancePool.add(rare, 1);

        effectChancePool = new IntegerChancePool<>();
        effectChancePool.add(effect1, 10);
        effectChancePool.add(effect2, 20);
        effectChancePool.add(effect3, 30);
        effectChancePool.add(ns_effect1, 15);
        effectChancePool.add(ns_effect2, 25);
        effectChancePool.add(ns_effect3, 35);

        nameGeneratorChancePool = new IntegerChancePool<>();

        final Random random = new Random();
        capitalGenerator = new NameGenerator() {
            @Override
            public String generate(LootMaterial lootMaterial, LootRarity rarity) {
                return "" + ((char) ('A' + random.nextInt(26)));
            }

            @Override
            public Optional<Long> countNumberOfNames() {
                return Optional.of(26L);
            }
        };
        nameGeneratorChancePool.add(capitalGenerator, 1);

        numberGenerator = new NameGenerator() {
            @Override
            public String generate(LootMaterial lootMaterial, LootRarity rarity) {
                return String.valueOf(random.nextInt(10));
            }

            @Override
            public Optional<Long> countNumberOfNames() {
                return Optional.of(10L);
            }
        };
        nameGeneratorChancePool.add(numberGenerator, 2);

        matEchoGenerator = new NameGenerator() {
            @Override
            public String generate(LootMaterial lootMaterial, LootRarity rarity) {
                if (lootMaterial == null) {
                    throw new NoSuchElementException();
                }
                return lootMaterial.name();
            }

            @Override
            public Optional<Long> countNumberOfNames() {
                return Optional.of(0L);
            }
        };
        nameGeneratorChancePool.add(matEchoGenerator, 1);

        rarityEchoGenerator = new NameGenerator() {
            @Override
            public String generate(LootMaterial lootMaterial, LootRarity rarity) {
                if (rarity == null) {
                    throw new NoSuchElementException();
                }
                return rarity.getName();
            }

            @Override
            public Optional<Long> countNumberOfNames() {
                return Optional.of(0L);
            }
        };
        nameGeneratorChancePool.add(rarityEchoGenerator, 2);
    }

    public void addTestResources() {
        LootRarity.registerRarity(common);
        LootRarity.registerRarity(uncommon);
        LootRarity.registerRarity(rare);

        LootSpecialEffect.registerEffect(effect1);
        LootSpecialEffect.registerEffect(effect2);
        LootSpecialEffect.registerEffect(effect3);
    }

    public void reset() {
        LootRarity.getRarities().clear();
        LootSpecialEffect.unregisterEffect(effect1);
        LootSpecialEffect.unregisterEffect(effect2);
        LootSpecialEffect.unregisterEffect(effect3);
    }

    public static class VoidEffect extends LootSpecialEffect {

        public VoidEffect(String name, String ns, int id, List<LootMaterial> validMaterials) {
            super(name, ns, id, validMaterials, name.replace('-', ' '));
        }

        @Override
        public void apply(Event event) {}
    }
}
