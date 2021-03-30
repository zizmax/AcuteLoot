package acute.loot;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Namespace'd ID for a loot effect.
 */
public final class EffectId {
    public final String ns;
    public final int id;

    /**
     * Construct a new EffectId with the given namespace and id.
     *
     * @param ns the namespace
     * @param id the id
     */
    public EffectId(String ns, int id) {
        if (!validNamespace(ns)) {
            throw new IllegalArgumentException("Invalid namespace. Namespaces must be at least two characters long, " +
                                                "consist solely of uppercase letters and dashes and may not start or end with a dash.");
        }
        this.ns = ns;
        this.id = id;
    }

    /**
     * Construct a new EffectId from a string in ns;id format.
     *
     * @param str the string to parse
     */
    public EffectId(String str) {
        String[] parts = str.split(";");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid EffectId");
        }
        ns = parts[0];
        if (!validNamespace(ns)) {
            throw new IllegalArgumentException("Invalid namespace. Namespaces must be at least two characters long, " +
                    "consist solely of uppercase letters and dashes and may not start or end with a dash.");
        }
        id = Integer.parseInt(parts[1]);
    }

    private static final Pattern pattern = Pattern.compile("[A-Z][A-Z-]*[A-Z]");

    public static boolean validNamespace(final String ns) {
        return ns != null && pattern.matcher(ns).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EffectId effectId = (EffectId) o;
        return id == effectId.id &&
                Objects.equals(ns, effectId.ns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ns, id);
    }

    @Override
    public String toString() {
        return String.format("%s;%d", ns, id);
    }
}
