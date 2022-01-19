package acute.loot;

import com.github.phillip.h.acutelib.util.Checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Representation of a piece of loot's rarity and effects.
 */
public class LootItem {

    private final int rarity;
    private List<EffectId> effects = new ArrayList<>();

    public LootItem(final LootRarity rarity, final List<LootSpecialEffect> effects) {
        this(rarity.getId(), effects.stream().map(LootSpecialEffect::effectId).collect(Collectors.toList()));
    }

    public LootItem(int rarity, List<EffectId> effects) {
        this.rarity = rarity;
        this.effects = effects;
    }

    /**
     * Parse a new LootItem from the given lootcode.
     *
     * @param lootcode the lootcode, cannot be empty or null and must be a known version
     */
    public LootItem(String lootcode) {
        Checks.requireNonEmpty(lootcode, "LootCode cannot be null or empty");
        String[] parts = lootcode.split(":");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid loot code, could not read version");
        }

        String version = parts[1];

        if (version.equals("1.0")) {
            this.rarity = Integer.parseInt(parts[2]);

            for (String effect : parts[3].split("_")) {
                if (!effect.isEmpty()) {
                    effects.add(new EffectId(LootSpecialEffect.AL_NS, Integer.parseInt(effect)));
                }
            }
        } else if (version.equals("2.0")) {
            this.rarity = Integer.parseInt(parts[2]);

            for (String effect : parts[3].split("_")) {
                if (!effect.isEmpty()) {
                    final String[] effectParts = effect.split(";");
                    effects.add(new EffectId(effectParts[0], Integer.parseInt(effectParts[1])));
                }
            }
        } else {
            throw new IllegalArgumentException("Unknown LootCode version '" + version + "'.");
        }
    }

    public LootRarity rarity() {
        return LootRarity.get(rarity);
    }

    public int rarityRaw() {
        return rarity;
    }

    public String lootCode() {
        return lootCodeV2();
    }

    public static String currentLootcodeVersion() {
        return "2.0";
    }

    // A version 1 lootcode is of the form
    // #AL:$vn:$rarity:$effects:#
    // Where:
    // $vn is the string "1.0"
    // $rarity is the integer rarity ID
    // $effects is an underscore '_' separated list of effect IDs, possible empty
    protected String lootCodeV1() {
        if (effects.stream().anyMatch(e -> !e.ns.equals(LootSpecialEffect.AL_NS))) {
            throw new IllegalStateException("Cannot use v1.0 lootcode with non-AcuteLoot namespace effects");
        }
        StringBuilder str = new StringBuilder();
        str.append("#AL:");
        str.append("1.0");
        str.append(':');
        str.append(rarity);
        str.append(':');
        for (int i = 0; i < effects.size(); i++) {
            if (i != 0) {
                str.append("_");
            }
            str.append(effects.get(i).id);
        }
        str.append(":#");
        return str.toString();
    }

    // A version 2 lootcode is of the form
    // #AL:$vn:$rarity:$effects:#
    // Where:
    // $vn is the string "2.0"
    // $rarity is the integer rarity ID
    // $effects is an underscore '_' separated list of namespaced effect IDs, possible empty
    //
    // a namespaced effect id is of the form $NS;$id, where $NS is the namespace of the plugin
    // that added the effect and $id is the integer effect ID
    protected String lootCodeV2() {
        StringBuilder str = new StringBuilder();
        str.append("#AL:");
        str.append("2.0");
        str.append(':');
        str.append(rarity);
        str.append(':');
        for (int i = 0; i < effects.size(); i++) {
            if (i != 0) {
                str.append("_");
            }
            str.append(effects.get(i).ns);
            str.append(';');
            str.append(effects.get(i).id);
        }
        str.append(":#");
        return str.toString();
    }

    public List<LootSpecialEffect> getEffects() {
        return effects.stream().map(LootSpecialEffect::get).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LootItem lootItem = (LootItem) o;
        return rarity == lootItem.rarity &&
                effects.equals(lootItem.effects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rarity, effects);
    }

}
