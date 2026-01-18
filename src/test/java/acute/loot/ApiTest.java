package acute.loot;

import acute.loot.namegen.NameGenerator;
import com.github.phillip.h.acutelib.collections.IntegerChancePool;
import com.github.phillip.h.acutelib.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiTest {

    private static final TestHelper testHelper = new TestHelper();
    private static AcuteLoot acuteLoot;

    @BeforeAll
    public static void setup() {
        testHelper.addTestResources();
        acuteLoot = Mockito.mock(AcuteLoot.class);
        final Logger logger = Logger.getLogger("AcuteLoot");
        logger.setLevel(Level.OFF); // Turn off warnings about failing to roll names
        Mockito.when(acuteLoot.getLogger()).thenReturn(logger);
        API.setAcuteLoot(acuteLoot);
        Mockito.when(acuteLoot.rarityChancePool()).thenReturn(testHelper.rarityChancePool);

        Server mockServer = Mockito.mock(Server.class);
        UnsafeValues mockUnsafe = Mockito.mock(UnsafeValues.class);
        Mockito.when(mockServer.getUnsafe()).thenReturn(mockUnsafe);

        // Mock getServer().getLogger()
        Logger mockLogger = Mockito.mock(Logger.class);
        Mockito.when(mockServer.getLogger()).thenReturn(mockLogger);

        Bukkit.setServer(mockServer);
    }

    @AfterAll
    public static void tearDown() {
        testHelper.reset();
    }

    @Test
    @DisplayName("rollName() correct")
    public void rollNameCorrect() {
        final List<Material> lootMaterialList = Arrays.asList(Material.values());
        final API api = new API(Mockito.mock(Plugin.class), "TEST");

        final IntegerChancePool<NameGenerator> pool1 = new IntegerChancePool<>();
        pool1.add(testHelper.numberGenerator, 1);
        pool1.add(testHelper.capitalGenerator, 2);

        final IntegerChancePool<NameGenerator> pool2 = new IntegerChancePool<>();
        pool2.add(testHelper.rarityEchoGenerator, 1);
        pool2.add(testHelper.matEchoGenerator, 2);

        Mockito.when(acuteLoot.namePool()).thenReturn(pool1);
        for (int i = 0; i < 100; i++) {
            assertTrue(isUppercaseOrNumber(api.rollName(new ItemStack(Util.drawRandom(lootMaterialList)), testHelper.rarityChancePool.draw())));
            assertTrue(isUppercaseOrNumber(api.rollName(new ItemStack(Util.drawRandom(lootMaterialList)), null)));
            assertTrue(isUppercaseOrNumber(api.rollName(null, testHelper.rarityChancePool.draw())));
            assertTrue(isUppercaseOrNumber(api.rollName(null, null)));
        }

        Mockito.when(acuteLoot.namePool()).thenReturn(pool2);
        final List<String> lootMaterials = Arrays.stream(LootMaterial.values()).map(LootMaterial::toString).collect(Collectors.toList());
        for (int i = 0; i < 100; i++) {
            assertThat(api.rollName(new ItemStack(Util.drawRandom(lootMaterialList)), testHelper.rarityChancePool.draw()),
                    either(oneOf("Common", "Uncommon", "Rare")).or(in(lootMaterials)));
            assertThat(api.rollName(new ItemStack(Util.drawRandom(lootMaterialList)), null),
                    either(oneOf("Common", "Uncommon", "Rare")).or(in(lootMaterials)));
            assertThat(api.rollName(null, testHelper.rarityChancePool.draw()),
                    either(oneOf("Common", "Uncommon", "Rare")).or(in(lootMaterials)));
            assertThrows(AcuteLootException.class, () -> api.rollName(null, null));
        }

    }

    private boolean isUppercaseOrNumber(String s) {
        if (s.length() != 1) {
            return false;
        }

        final char c = s.charAt(0);
        return ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9');
    }

}