package sudark2.Sudark.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public final class Boss extends JavaPlugin {

    static List<Location> zones = new ArrayList<>();
    static List<Location> endZones = new ArrayList<>();

    @Override
    public void onEnable() {

        TimeForBoss.checkTime(this);

        Bukkit.getPluginManager().registerEvents(new ZoneExpand(), this);
        Bukkit.getPluginCommand("boss").setExecutor(new TimeForBoss());

        new BukkitRunnable() {
            @Override
            public void run() {
                prepareZone();
            }
        }.runTaskLater(this, 5 * 20L);
    }

    public static Plugin getP() {
        return Bukkit.getPluginManager().getPlugin("Boss");
    }

    public void prepareZone() {
        //循声守卫
        zones.add(p(-16, 6, 14));
        endZones.add(p(15, -5, -16));
        //熔岩果冻
        zones.add(p(126, -8, 18));
        endZones.add(p(72, 7, -38));
        //骷髅王
        zones.add(p(170, 0, -34));
        endZones.add(p(222, 16, 18));
        //白玉贝
        zones.add(p(302, -2, -27));
        endZones.add(p(358, 16, 29));
        //银角
        zones.add(p(410, -6, -22));
        endZones.add(p(457, 10, 26));
        //金角
    }

    public Location p(int x, int y, int z) {
        return new Location(Bukkit.getWorld("BEEF-Boss"), x, y, z);
    }
}
