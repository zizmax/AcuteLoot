package acute.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LootItem {

    private String version = "1.0";

    private final int rarity;
    private List<EffectId> effects = new ArrayList<>();

    @Deprecated
    public LootItem(int rarity, List<Integer> effects) {
        this.rarity = rarity;
        this.effects = effects.stream().map(id -> new EffectId(LootSpecialEffect.AL_NS, id)).collect(Collectors.toList());
    }

    public LootItem(List<EffectId> effects, int rarity) {
        this.rarity = rarity;
        this.effects = effects;
    }

    public LootItem(String lootcode) {
        if (lootcode == null || lootcode.trim().isEmpty()) throw new IllegalArgumentException("LootCode cannot be null or empty");
        String[] parts = lootcode.split(":");
        if (parts.length < 2) throw new IllegalArgumentException("Invalid loot code, could not read version");

        version = parts[1];

        if (version.equals("1.0")) {
            this.rarity = Integer.parseInt(parts[2]);

            for (String effect : parts[3].split("_"))
                if (!effect.isEmpty()) effects.add(new EffectId(LootSpecialEffect.AL_NS, Integer.parseInt(effect)));
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
        return lootCodeV1();
    }

    // A version 1 lootcode is of the form
    // #AL:$vn:$rarity:$effects:#
    // Where:
    // $vn is the string "1.0"
    // $rarity is the integer rarity ID
    // $effects is an underscore '_' separated list of effect IDs, possible empty
    public String lootCodeV1() {
        StringBuilder str = new StringBuilder();
        str.append("#AL:");
        str.append(version);
        str.append(':');
        str.append(rarity);
        str.append(':');
        for (int i = 0; i < effects.size(); i++) {
            if (i != 0) str.append("_");
            str.append(effects.get(i).id);
        }
        str.append(":#");
        return str.toString();
    }

    // A version 2 lootcode is of the form
    // #AL:$vn:$rarity:$effects:#
    // Where:
    // $vn is the string "1.0"
    // $rarity is the integer rarity ID
    // $effects is an underscore '_' separated list of namespaced effect IDs, possible empty
    //
    // a namespaced effect id is of the form $NS;$id, where $NS is the namespace of the plugin
    // that added the effect and $id is the integer effect ID
    public String lootCodeV2() {
        StringBuilder str = new StringBuilder();
        str.append("#AL:");
        str.append(version);
        str.append(':');
        str.append(rarity);
        str.append(':');
        for (int i = 0; i < effects.size(); i++) {
            if (i != 0) str.append("_");
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LootItem lootItem = (LootItem) o;
        return rarity == lootItem.rarity &&
                effects.equals(lootItem.effects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rarity, effects);
    }

}
