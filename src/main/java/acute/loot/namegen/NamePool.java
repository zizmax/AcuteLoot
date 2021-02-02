package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;
import acute.loot.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface NamePool {

    List<String> getNames(LootMaterial lootMaterial, LootRarity lootRarity);

    default String drawName(LootMaterial lootMaterial, LootRarity lootRarity) {
        return Util.drawRandom(getNames(lootMaterial, lootRarity));
    }

    static List<String> readNames(String file) {
        if (file == null) return Collections.emptyList();

        try (Stream<String> stream = Files.lines(Paths.get(file))) {
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
