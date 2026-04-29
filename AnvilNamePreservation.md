# Goals
- Support preserving custom names when shields and elytras are turned into AcuteLoot via anvils.
- Add new anvil-specific config options for overwriting names/colors to match existing enchanting options.
- Utilize the existing `PreserveCustomNameNamer` mechanism.
- Create a persistent plan file in the codebase for AI validation.

# Non-Goals
- Fixing the "Already AL" TODO bug (preventing re-rolling of existing AcuteLoot items). Don't do this.

# Key Files & Context
- Git branch management.
- `roadmap/preserve-anvil-names.md`: The persistent plan file for AI validation.
- `src/main/resources/config.yml`: Where new settings will be added.
- `src/main/java/acute/loot/AcuteLoot.java`: Where generators are initialized and config is read.
- `src/main/java/acute/loot/LootCreationEventListener.java`: Where the anvil renaming event is handled.

# Implementation Steps

1. **Update `config.yml`:**
   - Under `loot-sources.anvils`, add two new config options to match the structure of enchanting:
     ```yaml
       anvils: # Currently only for shields and elytra
         enabled: true
         overwrite-existing-name: true
         overwrite-existing-colors: true # If overwrite-existing-name is true this option does nothing
     ```

2. **Create `anvilGenerator` in `AcuteLoot.java`:**
   - Add a new public variable: `public LootItemGenerator anvilGenerator;`.
   - In `onEnable()`, read the new configuration values (defaulting to `true` to preserve existing behavior if the user hasn't updated their config file):
     `boolean anvilOverwriteNames = getConfig().getBoolean("loot-sources.anvils.overwrite-existing-name", true);`
     `boolean anvilOverwriteColors = getConfig().getBoolean("loot-sources.anvils.overwrite-existing-colors", true);`
   - Instantiate `anvilGenerator` utilizing these booleans and the `nameGenChancePool`.

3. **Update `LootCreationEventListener.java`:**
   - Modify the `anvilListener` method to use `plugin.anvilGenerator` instead of `plugin.lootGenerator` for shields and elytras.
   - Add concise, clear comments explaining the generator swap.

# Verification & Testing

To determine if this is working perfectly, follow these exact steps on a local test server:

**Setup:**
1. Build the plugin and install it on the server.
2. Ensure your `config.yml` has the new options under `loot-sources.anvils`.

**Test Case 1: Preserving Custom Names**
1. Set `overwrite-existing-name: false` under `anvils` in `config.yml`. Save and reload (`/al reload`).
2. Give yourself a vanilla Shield and an item to repair it with (e.g. Planks).
3. Place them in an Anvil.
4. Type a custom name like "Captain's Defense" in the text box.
5. Take the resulting item from the anvil.
6. **Expected:** Run `/al info` on the shield. It should successfully be an AcuteLoot item with rarity and stats, but its display name should be "Captain's Defense" (with an AcuteLoot rarity color applied to it if `global-loot-name-color` allows).

**Test Case 2: Procedural Generation (Blank Rename Box)**
1. With `overwrite-existing-name: false` still set, put a vanilla Elytra and Phantom Membranes in the anvil.
2. Do **NOT** type a custom name in the rename box.
3. Take the resulting item.
4. **Expected:** It should generate a standard, procedural AcuteLoot name (e.g. "Legendary Wings").

**Test Case 3: Overwrite Config Works**
1. Set `overwrite-existing-name: true` under `anvils` in `config.yml`. Save and reload (`/al reload`).
2. Put a vanilla Shield and Planks in the Anvil.
3. Type a custom name like "Test Shield".
4. Take the resulting item.
5. **Expected:** The name "Test Shield" should be ignored and completely overwritten by a procedural AcuteLoot name.
