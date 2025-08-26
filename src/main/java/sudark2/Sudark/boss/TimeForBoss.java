package sudark2.Sudark.boss;

import org.bukkit.*;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import static sudark2.Sudark.boss.Title.title;


public class TimeForBoss implements CommandExecutor {
    public static int T;
    static String msg = "§e龙蛋§f会在下一个§b整点§f刷新";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (commandSender instanceof Player pl) {
            title(pl, "§e§lDRAGON_EGG", msg);
            return true;
        }
        return false;
    }

    public static void checkTime(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {

                Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                T = now.get(11);
                Random R = new Random();
                World world = Bukkit.getWorld("BEEF-DUNE");

                if (18 <= T || T <= 6) {
                    T = R.nextInt(0, 2);
                } else {
                    T = R.nextInt(2, 5);
                }

                int x = R.nextInt(-2000, 2000);

                while (x > -100 && x < 100 || world.getHighestBlockAt(x, x).getType().equals(Material.WATER))
                    x = R.nextInt(-2000, 2000);

                int finalX = x;
                Location loc = getHighestPos(world, x, x).add(0, 1, 0);
                int finalY = loc.getBlockY();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        msg = "[§e" + finalX + " §f,§b " + finalY + " §f,§e " + finalX + "]";
                        CommandBlock cb = (CommandBlock) Bukkit.getWorld("BEEF-DUNE").getBlockAt(21, 81, 17).getState();
                        cb.setCommand("sb " + (finalX - 20) + " " + (finalY - 6) + " " + (finalX - 20) + " " +
                                (finalX + 20) + " " + (finalY + 14) + " " + (finalX + 20) + " " + "BEEF-DUNE");
                        cb.update();
                    }
                }.runTaskLater(plugin, 20 * 5L);

                loc.getBlock().setType(Material.DRAGON_EGG);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 50);
                        if (!loc.getBlock().getType().equals(Material.DRAGON_EGG)) {
                            cancel();
                            msg = "§e龙蛋§f会在下一个§b整点§f刷新";
                        }
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

    public static Location getHighestPos(World world, int x, int z) {
        for (int y = world.getHighestBlockYAt(x, z); y > 0; y--) {
            if (world.getBlockAt(x, y, z).getType().isSolid()) return new Location(world, x, y, z);
        }
        return null;
    }


}
