package acute.loot;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DebugModule implements Module {

    private final AcuteLoot plugin;

    @Override
    public void enable() {
        plugin.debug = true;
    }

    @Override
    public void disable() {
        plugin.debug = false;
    }
}
