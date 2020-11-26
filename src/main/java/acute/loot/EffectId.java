package acute.loot;

import java.util.Objects;

public final class EffectId {
    public final String ns;
    public final int id;

    public EffectId(String ns, int id) {
        this.ns = ns;
        this.id = id;
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
}
