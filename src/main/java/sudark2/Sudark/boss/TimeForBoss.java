package sudark2.Sudark.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Sign;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

public class TimeForBoss {
    public static int T;

    public static void checkTime(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                T = now.get(11);
                Random R = new Random();

                if (18 <= T || T <= 6) {
                    T = R.nextInt(0, 1);
                } else {
                    T = R.nextInt(1, 2);
                }
                T = 2;

                int x = R.nextInt(-2000, 2000);
                while (Bukkit.getWorld("BEEF-DUNE").getHighestBlockAt(x, x).getType().equals(Material.WATER)) {
                    x = R.nextInt(-2000, 2000);
                }

                int finalX = x;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Sign sign = (Sign) Bukkit.getWorld("BEEF-Main").getBlockAt(3, 3, 2).getState();
                        sign.setLine(0, "§e§lBOSS");
                        sign.setLine(1, "========");
                        sign.setLine(2, "§b§l" + finalX);
                        sign.setLine(3, "========");
                        sign.update();
                    }
                }.runTaskLater(plugin, 20 * 5L);


                Location loc = Bukkit.getWorld("BEEF-DUNE").getHighestBlockAt(x, x).getLocation().add(0, 1, 0);

                loc.getBlock().setType(Material.DRAGON_EGG);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 50);
                        if (!loc.getBlock().getType().equals(Material.DRAGON_EGG)) cancel();
                    }
                }.runTaskTimer(plugin, 0, 20L);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        loc.getBlock().setType(Material.AIR);
                    }
                }.runTaskLater(plugin, 20 * 60 * 30L);

            }
        }.runTaskTimer(plugin, 0, 20 * 60 * 60L);
    }
}
