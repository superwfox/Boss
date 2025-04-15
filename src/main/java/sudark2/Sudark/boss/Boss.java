package sudark2.Sudark.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class Boss extends JavaPlugin {

   static List<Location> zones;
   static List<Location> endZones;

    @Override
    public void onEnable() {
        prepareZone();

    }

    public void prepareZone() {
        //循声守卫
        zones.add(p(-17, 6, 15));
        endZones.add(p(16, -5, -17));
        //TODO: 添加更多守卫
    }

    public Location p(int x, int y, int z) {
        return new Location(Bukkit.getWorld("BEEF-Boss"), x, y, z);
    }
}
