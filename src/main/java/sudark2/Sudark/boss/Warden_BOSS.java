package sudark2.Sudark.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static sudark2.Sudark.boss.Success.success;
import static sudark2.Sudark.boss.ZoneExpand.SpiralPlaneGenerator.resumeZone;

public class Warden_BOSS {
    public static void newTask(Plugin plugin, Location Core, Player pl, int range) {
        BossBar bossBar = Bukkit.createBossBar("WARDEN", BarColor.RED, BarStyle.SOLID);

        Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(e -> {
            if (e instanceof Player p) {
                Title.title(p, "-§e§lWARDEN§f-", "源自深渊的咆哮——循声守卫");
            }
        });

        new BukkitRunnable() {
            int time = 0;
            Warden warden = null;
            @Override
            public void run() {

                if (time == 0) {
                    warden = (Warden) Core.getWorld().spawnEntity(Core.add(-5, 0, 9), EntityType.WARDEN);
                    warden.setAnger(pl, 100);
                }

                bossBar.setProgress(warden.getHealth() / warden.getMaxHealth());

                time++;
                if (time == 60 * 24) {
                    cancel();
                    bossBar.removeAll();
                    warden.remove();
                    resumeZone(Core,plugin);
                }

                if (warden.isDead()) {
                    Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(e -> {
                        if (e instanceof Player pl) {
                            success(pl, new ItemStack(Material.COMMAND_BLOCK), 50, 1);
                        }
                    });
                    resumeZone(Core,plugin);
                    bossBar.removeAll();
                    cancel();
                    return;
                }

                Bukkit.getOnlinePlayers().forEach(bossBar::removePlayer);

                Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(e -> {
                    if (e instanceof Player pl) {
                        bossBar.addPlayer(pl);
                    }
                });

            }
        }.runTaskTimer(plugin, 8 * 20, 20L);

    }
}
