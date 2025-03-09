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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith; // NEW: Import ExtendWith
import org.mockito.MockedConstruction; // NEW: Import MockedConstruction
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension; // NEW: Import MockitoExtension

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class) // NEW: Extend with MockitoExtension - at the CLASS LEVEL
public class ApiTest {

    private static final TestHelper testHelper = new TestHelper();
    private static AcuteLoot acuteLoot;
    // REMOVED: private static MockedStatic<Bukkit> bukkitMockedStatic; // No longer needed for constructor mocking
    private static MockedStatic<LootMaterial> lootMaterialMockedStatic;
    private MockedConstruction<ItemStack> itemStackMockedConstruction; // NEW: MockedConstruction for ItemStack

    @BeforeEach
    public void setupEachTest() {
        testHelper.addTestResources();
        acuteLoot = Mockito.mock(AcuteLoot.class);
        final Logger logger = Logger.getLogger("AcuteLoot");
        logger.setLevel(Level.OFF);
        Mockito.when(acuteLoot.getLogger()).thenReturn(logger);
        API.setAcuteLoot(acuteLoot);
        //Mockito.when(acuteLoot.rarityChancePool()).thenReturn(testHelper.rarityChancePool);

        // REMOVED: Mock Bukkit.getServer() and related static mocking - not needed for constructor mocking
        // Server mockServer = Mockito.mock(Server.class);
        // UnsafeValues mockUnsafeValues = Mockito.mock(UnsafeValues.class);
        // Mockito.when(mockServer.getUnsafe()).thenReturn(mockUnsafeValues);
        // bukkitMockedStatic = Mockito.mockStatic(Bukkit.class);
        // bukkitMockedStatic.when(Bukkit::getServer).thenReturn(mockServer);
        // assertThat(Bukkit.getServer().getUnsafe(), is(mockUnsafeValues));

        // Mock ItemStack Constructor using MockedConstruction
        itemStackMockedConstruction = Mockito.mockConstruction(ItemStack.class, (mock, context) -> { // NEW: MockedConstruction
            // You can add specific mock behavior for ItemStack instances here if needed
            // For now, a default mock might be sufficient.
        });
        assertThat(itemStackMockedConstruction.constructed().isEmpty(), is(true)); // CHANGED: Use constructed().isEmpty()
        //Mock LootMaterial.values();
    }

    @AfterEach // CHANGED: From AfterAll to AfterEach - for MockedConstruction cleanup
    public void tearDownEachTest() { // CHANGED: Rename to tearDownEachTest to reflect @AfterEach
        testHelper.reset();
        if(itemStackMockedConstruction != null) { // NEW: Close MockedConstruction for ItemStack
            itemStackMockedConstruction.close();
        }
        // REMOVED: if(bukkitMockedStatic != null){ // No longer needed
        // REMOVED:    bukkitMockedStatic.close();
        // REMOVED: }
        if(lootMaterialMockedStatic != null){
            lootMaterialMockedStatic.close();
        }
    }

    @AfterAll
    public static void tearDownAll() { // NEW: Separate AfterAll method for static mocks if still needed later
        if(lootMaterialMockedStatic != null){
            lootMaterialMockedStatic.close();
        }
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