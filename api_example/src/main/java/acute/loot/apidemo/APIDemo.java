package acute.loot.apidemo;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin class. NO references to AcuteLoot here!
 */
public class APIDemo extends JavaPlugin {

    private ALIntegration alIntegration;

    @Override
    public void onEnable() {
        getLogger().info("Enabling ALApiDemo");
        if (Bukkit.getServer().getPluginManager().getPlugin("AcuteLoot") != null) {
            getLogger().info("AcuteLoot is enabled! Using integration.");
            alIntegration = new ALIntegration(this);
        } else {
            getLogger().info("AcuteLoot is not enabled! Not using integration.");
        }

        checkAcuteLootApiVersion();
        addAcuteLootEffectsIfAvailable();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ALApiDemo");
        removeAcuteLootEffectsIfAvailable();
        alIntegration = null;
    }

    private void checkAcuteLootApiVersion() {
        if (alIntegration != null) {
            if (alIntegration.checkVersion()) {
                getLogger().info("AcuteLoot API matches requirements");
            } else {
                getLogger().warning("AcuteLoot API does not match requirements, features may not work!");
            }
        }
    }

    private void addAcuteLootEffectsIfAvailable() {
        if (alIntegration != null) {
            alIntegration.addEffects();
        }
    }

    private void removeAcuteLootEffectsIfAvailable() {
        if (alIntegration != null) {
            alIntegration.removeEffects();
        }
    }
}
