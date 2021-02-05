package acute.loot;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public final class Util {

    private static final Random random = AcuteLoot.random;

    private Util() {
    }

    /**
     * Draw a random element from a list. If the list is empty, throw a NoSuchElementException.
     *
     * @param <T> type of the list
     * @param list the List to draw from
     * @return a random element from the list
     */
    public static <T> T drawRandom(List<T> list) {
        if (list.isEmpty()) throw new NoSuchElementException();
        if (list.size() == 1) return list.get(0);
        return list.get(random.nextInt(list.size()));
    }

    public static List<Location> getLine(Location from, double distance, double addition) {
        List<Location> locations = new ArrayList<>();
        final Vector direction = from.getDirection(); // End - Begin | length to 1
        for (double d = addition; d < distance; d += addition) {
            locations.add(from.clone().add(direction.clone().normalize().multiply(d)));
        }
        return locations;
    }

    public static Material validateMaterial(String materialString) {
        if (materialString == null) return null;
        materialString = materialString.trim();
        if (!materialString.equals("")) {
            Material material = null;
            try {
                material = Material.matchMaterial(materialString);
            } catch (IllegalArgumentException | NullPointerException e) {
                return null;
            }
            return material;
        }
        return null;
    }

    public static String getUIString(String messageName, AcuteLoot plugin){

        if(plugin.getConfig().contains("msg." + messageName)) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("msg." + messageName));
        }
        else{
            plugin.getLogger().warning("Config message error at: msg." + messageName);
            return ChatColor.DARK_RED + "[" +ChatColor.BLACK + "Config Error" + ChatColor.DARK_RED+ "]";
        }
    }
}
