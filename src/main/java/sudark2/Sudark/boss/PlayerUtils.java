package sudark2.Sudark.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class PlayerUtils {
    public static void forNearbyPlayers(Location loc, double range, Consumer<Player> action) {
        loc.getWorld().getNearbyEntities(loc, range, range, range).forEach(entity -> {
            if (entity instanceof Player player) {
                action.accept(player);
            }
        });
    }

    public static void forAllOnlinePlayers(Consumer<Player> action) {
        Bukkit.getOnlinePlayers().forEach(action);
    }
}

