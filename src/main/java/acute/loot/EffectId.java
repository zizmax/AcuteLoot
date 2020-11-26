package acute.loot;

import java.util.Objects;

public final class EffectId {
    public final String ns;
    public final int id;

    public EffectId(String ns, int id) {
        this.ns = ns;
        this.id = id;
    }

    public EffectId(String str) {
        String[] parts = str.split(";");
        if (parts.length != 2) throw new IllegalArgumentException("Invalid EffectId");
        ns = parts[0];
        id = Integer.parseInt(parts[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
