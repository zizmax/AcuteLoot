package acute.loot;

import org.bukkit.Material;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum LootMaterial {

    SWORD, BOW, HELMET, BOOTS, CROSSBOW, // Can have effects (for now)
    PICK, SHOVEL, HOE, AXE, PANTS, CHEST_PLATE, FISHING_ROD, TRIDENT, // Have names
    GENERIC, UNKNOWN;

    public static LootMaterial lootMaterialForMaterial(Material type) {
        final List<Material> materialList = Events.materials;

        // Return the mapping for this material,
        // or GENERIC if it is unregistered but in the events materials list, else UNKNOWN
        return materialMap.getOrDefault(type, materialList.contains(type) ? GENERIC : UNKNOWN);
    }

    // This map will translate Material -> LootMaterial
    private static final Map<Material, LootMaterial> materialMap;

    static {
        final Map<Material, LootMaterial> matMap = new HashMap<>();

        // Swords
        matMap.put(Material.WOODEN_SWORD, SWORD);
        matMap.put(Material.STONE_SWORD, SWORD);
        matMap.put(Material.IRON_SWORD, SWORD);
        matMap.put(Material.GOLDEN_SWORD, SWORD);
        matMap.put(Material.DIAMOND_SWORD, SWORD);
        if (AcuteLoot.serverVersion > 15) matMap.put(Material.NETHERITE_SWORD, SWORD);

        // Picks
        matMap.put(Material.WOODEN_PICKAXE, PICK);
        matMap.put(Material.STONE_PICKAXE, PICK);
        matMap.put(Material.IRON_PICKAXE, PICK);
        matMap.put(Material.GOLDEN_PICKAXE, PICK);
        matMap.put(Material.DIAMOND_PICKAXE, PICK);
        if (AcuteLoot.serverVersion > 15) matMap.put(Material.NETHERITE_PICKAXE, PICK);

        // Shovels
        matMap.put(Material.WOODEN_SHOVEL, SHOVEL);
        matMap.put(Material.STONE_SHOVEL, SHOVEL);
        matMap.put(Material.IRON_SHOVEL, SHOVEL);
        matMap.put(Material.GOLDEN_SHOVEL, SHOVEL);
        matMap.put(Material.DIAMOND_SHOVEL, SHOVEL);
        if (AcuteLoot.serverVersion > 15) matMap.put(Material.NETHERITE_SHOVEL, SHOVEL);

        // Axes
        matMap.put(Material.WOODEN_AXE, AXE);
        matMap.put(Material.STONE_AXE, AXE);
        matMap.put(Material.IRON_AXE, AXE);
        matMap.put(Material.GOLDEN_AXE, AXE);
        matMap.put(Material.DIAMOND_AXE, AXE);
        if (AcuteLoot.serverVersion > 15) matMap.put(Material.NETHERITE_AXE, AXE);

        // Hoes
        matMap.put(Material.WOODEN_HOE, HOE);
        matMap.put(Material.STONE_HOE, HOE);
        matMap.put(Material.IRON_HOE, HOE);
        matMap.put(Material.GOLDEN_HOE, HOE);
        matMap.put(Material.DIAMOND_HOE, HOE);
        if (AcuteLoot.serverVersion > 15) matMap.put(Material.NETHERITE_HOE, HOE);

        // Helmets
        matMap.put(Material.TURTLE_HELMET, HELMET);
        matMap.put(Material.LEATHER_HELMET, HELMET);
        matMap.put(Material.CHAINMAIL_HELMET, HELMET);
        matMap.put(Material.IRON_HELMET, HELMET);
        matMap.put(Material.GOLDEN_HELMET, HELMET);
        matMap.put(Material.DIAMOND_HELMET, HELMET);
        if (AcuteLoot.serverVersion > 15) matMap.put(Material.NETHERITE_HELMET, HELMET);

        // Chest plates
        matMap.put(Material.LEATHER_CHESTPLATE, CHEST_PLATE);
        matMap.put(Material.CHAINMAIL_CHESTPLATE, CHEST_PLATE);
        matMap.put(Material.IRON_CHESTPLATE, CHEST_PLATE);
        matMap.put(Material.GOLDEN_CHESTPLATE, CHEST_PLATE);
        matMap.put(Material.DIAMOND_CHESTPLATE, CHEST_PLATE);
        if (AcuteLoot.serverVersion > 15) matMap.put(Material.NETHERITE_CHESTPLATE, CHEST_PLATE);

        // Leggings
        matMap.put(Material.LEATHER_LEGGINGS, PANTS);
        matMap.put(Material.CHAINMAIL_LEGGINGS, PANTS);
        matMap.put(Material.IRON_LEGGINGS, PANTS);
        matMap.put(Material.GOLDEN_LEGGINGS, PANTS);
        matMap.put(Material.DIAMOND_LEGGINGS, PANTS);
        if (AcuteLoot.serverVersion > 15) matMap.put(Material.NETHERITE_LEGGINGS, PANTS);

        // Boots
        matMap.put(Material.LEATHER_BOOTS, BOOTS);
        matMap.put(Material.CHAINMAIL_BOOTS, BOOTS);
        matMap.put(Material.IRON_BOOTS, BOOTS);
        matMap.put(Material.GOLDEN_BOOTS, BOOTS);
        matMap.put(Material.DIAMOND_BOOTS, BOOTS);
        if (AcuteLoot.serverVersion > 15) matMap.put(Material.NETHERITE_BOOTS, BOOTS);

        // Bow
        matMap.put(Material.BOW, BOW);

        // Fishing Rod
        matMap.put(Material.FISHING_ROD, FISHING_ROD);

        // Crossbow
        matMap.put(Material.CROSSBOW, CROSSBOW);

        // Trident
        matMap.put(Material.TRIDENT, TRIDENT);

        materialMap = Collections.unmodifiableMap(matMap);
    }
}
