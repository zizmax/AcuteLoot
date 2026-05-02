package acute.loot;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;

import java.util.*;

/**
 * Enum for the type of loot, e.g. SWORD or BOW.
 */
public enum LootMaterial {

    SWORD, BOW, HELMET, BOOTS, CROSSBOW, // Can have effects (for now)
    PICK, SHOVEL, HOE, AXE, PANTS, CHEST_PLATE, FISHING_ROD, TRIDENT, // Have names only
    SHIELD, ELYTRA, // Only become AcuteLoot from anvils
    MACE, SPEAR,
    GENERIC, UNKNOWN;

    private static Set<Material> genericMaterialsList = Collections.emptySet();

    public static void setGenericMaterialsList(final Collection<Material> genericMaterialsList) {
        LootMaterial.genericMaterialsList = new HashSet<>(genericMaterialsList);
    }

    /*
     Return the mapping for this material,
     or GENERIC if it is unregistered but in the events materials list, else UNKNOWN
    */
    public static LootMaterial lootMaterialForMaterial(Material type) {
        return materialMap.getOrDefault(type, genericMaterialsList.contains(type) ? GENERIC : UNKNOWN);
    }

    // This map will translate Material -> LootMaterial
    private static final Map<Material, LootMaterial> materialMap;

    static {
        final Map<Material, LootMaterial> matMap = new HashMap<>();

        // Swords
        register(matMap, "WOODEN_SWORD", SWORD);
        register(matMap, "STONE_SWORD", SWORD);
        register(matMap, "IRON_SWORD", SWORD);
        register(matMap, "GOLDEN_SWORD", SWORD);
        register(matMap, "DIAMOND_SWORD", SWORD);
        register(matMap, "NETHERITE_SWORD", SWORD);

        // Picks
        register(matMap, "WOODEN_PICKAXE", PICK);
        register(matMap, "STONE_PICKAXE", PICK);
        register(matMap, "IRON_PICKAXE", PICK);
        register(matMap, "GOLDEN_PICKAXE", PICK);
        register(matMap, "DIAMOND_PICKAXE", PICK);
        register(matMap, "NETHERITE_PICKAXE", PICK);

        // Shovels
        register(matMap, "WOODEN_SHOVEL", SHOVEL);
        register(matMap, "STONE_SHOVEL", SHOVEL);
        register(matMap, "IRON_SHOVEL", SHOVEL);
        register(matMap, "GOLDEN_SHOVEL", SHOVEL);
        register(matMap, "DIAMOND_SHOVEL", SHOVEL);
        register(matMap, "NETHERITE_SHOVEL", SHOVEL);

        // Axes
        register(matMap, "WOODEN_AXE", AXE);
        register(matMap, "STONE_AXE", AXE);
        register(matMap, "IRON_AXE", AXE);
        register(matMap, "GOLDEN_AXE", AXE);
        register(matMap, "DIAMOND_AXE", AXE);
        register(matMap, "NETHERITE_AXE", AXE);

        // Hoes
        register(matMap, "WOODEN_HOE", HOE);
        register(matMap, "STONE_HOE", HOE);
        register(matMap, "IRON_HOE", HOE);
        register(matMap, "GOLDEN_HOE", HOE);
        register(matMap, "DIAMOND_HOE", HOE);
        register(matMap, "NETHERITE_HOE", HOE);

        // Helmets
        register(matMap, "TURTLE_HELMET", HELMET);
        register(matMap, "LEATHER_HELMET", HELMET);
        register(matMap, "CHAINMAIL_HELMET", HELMET);
        register(matMap, "IRON_HELMET", HELMET);
        register(matMap, "GOLDEN_HELMET", HELMET);
        register(matMap, "DIAMOND_HELMET", HELMET);
        register(matMap, "NETHERITE_HELMET", HELMET);

        // Chest plates
        register(matMap, "LEATHER_CHESTPLATE", CHEST_PLATE);
        register(matMap, "CHAINMAIL_CHESTPLATE", CHEST_PLATE);
        register(matMap, "IRON_CHESTPLATE", CHEST_PLATE);
        register(matMap, "GOLDEN_CHESTPLATE", CHEST_PLATE);
        register(matMap, "DIAMOND_CHESTPLATE", CHEST_PLATE);
        register(matMap, "NETHERITE_CHESTPLATE", CHEST_PLATE);

        // Leggings
        register(matMap, "LEATHER_LEGGINGS", PANTS);
        register(matMap, "CHAINMAIL_LEGGINGS", PANTS);
        register(matMap, "IRON_LEGGINGS", PANTS);
        register(matMap, "GOLDEN_LEGGINGS", PANTS);
        register(matMap, "DIAMOND_LEGGINGS", PANTS);
        register(matMap, "NETHERITE_LEGGINGS", PANTS);

        // Boots
        register(matMap, "LEATHER_BOOTS", BOOTS);
        register(matMap, "CHAINMAIL_BOOTS", BOOTS);
        register(matMap, "IRON_BOOTS", BOOTS);
        register(matMap, "GOLDEN_BOOTS", BOOTS);
        register(matMap, "DIAMOND_BOOTS", BOOTS);
        register(matMap, "NETHERITE_BOOTS", BOOTS);

        // Bow
        register(matMap, "BOW", BOW);

        // Fishing Rod
        register(matMap, "FISHING_ROD", FISHING_ROD);

        // Crossbow
        register(matMap, "CROSSBOW", CROSSBOW);

        // Trident
        register(matMap, "TRIDENT", TRIDENT);

        // Shield
        register(matMap, "SHIELD", SHIELD);

        // Elytra
        register(matMap, "ELYTRA", ELYTRA);

        // Mace
        register(matMap, "MACE", MACE);

        // Spear
        register(matMap, "WOODEN_SPEAR", SPEAR);
        register(matMap, "STONE_SPEAR", SPEAR);
        register(matMap, "COPPER_SPEAR", SPEAR);
        register(matMap, "IRON_SPEAR", SPEAR);
        register(matMap, "GOLDEN_SPEAR", SPEAR);
        register(matMap, "DIAMOND_SPEAR", SPEAR);
        register(matMap, "NETHERITE_SPEAR", SPEAR);
        register(matMap, "SPEAR", SPEAR);

        materialMap = Collections.unmodifiableMap(matMap);
    }

    private static void register(Map<Material, LootMaterial> map, String materialName, LootMaterial category) {
        XMaterial.matchXMaterial(materialName)
                 .map(XMaterial::parseMaterial)
                 .ifPresent(mat -> map.put(mat, category));
    }
}
