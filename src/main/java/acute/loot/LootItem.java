package acute.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LootItem {

    private String version = "1.0";

    private final int rarity;
    private List<Integer> effects = new ArrayList<>();

    public LootItem(int rarity, List<Integer> effects) {
        this.rarity = rarity;
        this.effects = effects;
    }

    public LootItem(String lootcode) {
        String[] parts = lootcode.split(":");

        version = parts[1];

        if (version.equals("1.0")) {
            this.rarity = Integer.parseInt(parts[2]);

            for (String effect : parts[3].split("_"))
                if (!effect.isEmpty()) effects.add(Integer.parseInt(effect));
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

    // A version 1 lootcode is of the form
    // #AL:$vn:$rarity:$effects:#
    // Where:
    // $vn is the string "1.0"
    // $rarity is the integer rarity ID
    // $effects is an underscore '_' separated list of effect IDs, possible empty
    public String lootCode() {
        StringBuilder str = new StringBuilder();
        str.append("#AL:");
        str.append(version);
        str.append(':');
        str.append(rarity);
        str.append(':');
        for (int i = 0; i < effects.size(); i++) {
            if (i != 0) str.append("_");
            str.append(effects.get(i));
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
