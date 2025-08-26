package sudark2.Sudark.boss;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

import static sudark2.Sudark.boss.PlayerUtils.forNearbyPlayers;
import static sudark2.Sudark.boss.ZoneExpand.SpiralPlaneGenerator.resumeZone;

public class Shulker_BOSS {
    public static void newTask(Plugin plugin, Location loc, Player pl, int range) {

        BossBar bossBar = Bukkit.createBossBar("§lJadeShulker", BarColor.RED, BarStyle.SEGMENTED_20);
        Set<Shulker> shulkers = new HashSet<>();
        final int[] preNum = {0};

        forNearbyPlayers(loc, range, p -> {
            Title.title(p, "-§e§lJadeShulker§f-", "你永远无法逃离我的视线--白玉贝");
            p.teleport(loc.clone().add(0, 6, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 5 * 20, 10, false, false, false));
        });

        new BukkitRunnable() {
            int time = -1;

            @Override
            public void run() {
                time++;

                if (time == 0) {
                    forNearbyPlayers(loc, range, player -> {
                        Title.title(player, "-§e§l任务更新§f-", "找到所有的陶釉--挖掉它");
                    });
                }

                int num = 0;
                for (int dx = -25; dx <= 25; dx++) {
                    for (int dz = -25; dz <= 25; dz++) {
                        Location checkLoc = loc.clone().add(dx, 2, dz);
                        Block block = checkLoc.getBlock();
                        if (block.getType() == Material.WHITE_GLAZED_TERRACOTTA) {
                            num++;
                        }
                    }
                }

                bossBar.setProgress(num / 20.0);
                Bukkit.getOnlinePlayers().forEach(bossBar::removePlayer);
                forNearbyPlayers(loc, range, bossBar::addPlayer);

                if (num != preNum[0]) {
                    preNum[0] = num;
                    Shulker shulker = (Shulker) loc.getWorld().spawnEntity(loc.clone().add(0, 6, -1), EntityType.SHULKER);
                    shulker.setColor(DyeColor.WHITE);
                    shulkers.add(shulker);

                    forNearbyPlayers(loc, range, p -> {
                        ShulkerBullet bullet = (ShulkerBullet) p.getWorld().spawnEntity(p.getLocation().add(0, 4, 0), EntityType.SHULKER_BULLET);
                        bullet.setShooter(p);
                        bullet.setTarget(p);
                    });

                }

                if (num == 0) {
                    forNearbyPlayers(loc, range, p -> {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 10, false, false, false));
                        Success.success(p, new ItemStack(Material.COMMAND_BLOCK), 50, 1);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 5 * 20, 10, false, false, false));
                    });
                    cancel();
                    shulkers.forEach(Shulker::remove);
                    bossBar.removeAll();
                    resumeZone(loc, plugin);
                }

                if (time == 20 * 60) {
                    cancel();
                    bossBar.removeAll();
                    shulkers.forEach(Shulker::remove);
                    resumeZone(loc, plugin);
                }

            }
        }.runTaskTimer(plugin, 45 * 20L, 20L);


    }
}
