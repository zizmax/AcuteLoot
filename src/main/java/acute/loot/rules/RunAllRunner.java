package acute.loot.rules;

import lombok.AllArgsConstructor;
import org.bukkit.event.Event;

import java.util.Set;

@AllArgsConstructor
class RunAllRunner implements RuleRunner {

    private final Set<Rule> rules;

    @Override
    public void run(final Event e) {
        rules.stream()
                .filter(r -> r.metBy(e))
                .forEach(r -> r.spawn(r.generate(), e));
    }
}
