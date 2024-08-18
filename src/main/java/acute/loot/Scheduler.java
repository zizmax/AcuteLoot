package acute.loot;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

// Folia compatability template provided by: @kangarko via https://www.youtube.com/watch?v=JwSqUnncVk8

//FIXME: go through all instances of schedulers and replace with the correct one from the list below. some of them may need modification to work properly. Not yet tested at ALL on regular Paper

public final class Scheduler {

    private static final boolean isFolia = Bukkit.getVersion().contains("Folia");

    public static void runRegion(Location location, Runnable runnable) {
        if (isFolia)
            Bukkit.getRegionScheduler().execute(AcuteLoot.getInstance(), location, runnable);

        else
            Bukkit.getScheduler().runTask(AcuteLoot.getInstance(), runnable);
    }

    public static void runEntity(Entity entity, Runnable runnable) {
        if (isFolia)
            entity.getScheduler().run(AcuteLoot.getInstance(), t -> runnable.run(), runnable);

        else
            Bukkit.getScheduler().runTask(AcuteLoot.getInstance(), runnable);
    }

    public static Task runLater(Runnable runnable, long delayTicks) {
        if (isFolia)
            return new Task(Bukkit.getGlobalRegionScheduler()
                    .runDelayed(AcuteLoot.getInstance(), t -> runnable.run(), delayTicks));

        else
            return new Task(Bukkit.getScheduler().runTaskLaterAsynchronously(AcuteLoot.getInstance(), runnable, delayTicks));
    }

    public static Task runTimerRegion(Runnable runnable, Location location, long delayTicks, long periodTicks) {
        if (isFolia)
            return new Task(Bukkit.getRegionScheduler()
                    .runAtFixedRate(AcuteLoot.getInstance(), location, t -> runnable.run(), delayTicks < 1 ? 1 : delayTicks, periodTicks));

        else
            return new Task(Bukkit.getScheduler().runTaskTimer(AcuteLoot.getInstance(), runnable, delayTicks, periodTicks));
    }

    public static Task runTimerEntity(Entity entity, Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia)
            return new Task(entity.getScheduler()
                    .runAtFixedRate(AcuteLoot.getInstance(), t -> runnable.run(), runnable, delayTicks < 1 ? 1 : delayTicks, periodTicks < 1 ? 1: periodTicks));

        else
            return new Task(Bukkit.getScheduler().runTaskTimer(AcuteLoot.getInstance(), runnable, delayTicks, periodTicks));
    }

    public static boolean isFolia() {
        return isFolia;
    }

    public static class Task {

        private Object foliaTask;
        private BukkitTask bukkitTask;

        Task(Object foliaTask) {
            this.foliaTask = foliaTask;
        }

        Task(BukkitTask bukkitTask) {
            this.bukkitTask = bukkitTask;
        }

        public void cancel() {
            if (foliaTask != null)
                ((ScheduledTask) foliaTask).cancel();
            else
                bukkitTask.cancel();
        }
    }
}