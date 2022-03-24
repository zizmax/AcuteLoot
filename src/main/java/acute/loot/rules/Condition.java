package acute.loot.rules;

import org.bukkit.event.Event;

import java.util.Collection;

interface Condition {

    boolean metBy(final Event e);

    static Condition allOf(Collection<Condition> subConditions) {
        return e -> subConditions.stream().allMatch(c -> c.metBy(e));
    }

}
