package sudark2.Sudark.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static sudark2.Sudark.boss.Success.success;

public class Magma_BOSS {
    public static void newTask(Plugin plugin, Location Core, Player pl, int range) {

        BossBar bossBar = Bukkit.createBossBar("§lMagmaCube", BarColor.PINK, BarStyle.SEGMENTED_10);

        Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(e -> {
            if (e instanceof Player p) {
                Title.title(p, "-§e§lMagmaCube§f-", "炙手又可爱的难得——熔岩果冻");
                p.teleport(Core.clone().add(0, 0, 5));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 5, 10, false, false, false));
            }
        });

        new BukkitRunnable() {
            int time = 0;
            MagmaCube magmaCube = null;
            BukkitTask ability;
            Listener listener = null;

            @Override
            public void run() {
                //initialise
                if (time == 0) {
                    magmaCube = (MagmaCube) Core.getWorld().spawnEntity(Core, EntityType.MAGMA_CUBE);
                    magmaCube.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(2);
                    magmaCube.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(4);
                    magmaCube.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(14);
                    magmaCube.getAttribute(Attribute.ATTACK_KNOCKBACK).setBaseValue(7);
                    magmaCube.setSize(4);

                    ability = new BukkitRunnable() {
                        @Override
                        public void run() {
                            Random rand = new Random();
                            switch (rand.nextInt(3)) {
                                case 0:
                                    Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(entity -> {
                                        if (entity instanceof Player pl) {
                                            magmaCube.teleport(pl);
                                            magmaCube.setSize(3);
                                            magmaCube.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(10);
                                            magmaCube.getAttribute(Attribute.ATTACK_KNOCKBACK).setBaseValue(5);
                                        }
                                    });
                                case 1:
                                    Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(entity -> {
                                        if (entity instanceof Player pl) {
                                            magmaCube.setSize(1);
                                            Core.getWorld().spawnEntity(pl.getLocation(), EntityType.BLAZE);
                                        }
                                    });
                                case 2:
                                    new BukkitRunnable() {
                                        int time = 0;

                                        @Override
                                        public void run() {
                                            time++;
                                            pl.getWorld().spawnParticle(Particle.FLAME, Core.clone().add(0, 0, 5), 20, 1, 1, 1);
                                            if (time == 10) {
                                                cancel();
                                                magmaCube.teleport(Core.clone().add(0, 0, 5));
                                                magmaCube.setSize(4);
                                            }
                                        }
                                    }.runTaskTimer(plugin, 0, 8L);
                            }
                        }
                    }.runTaskTimer(plugin, 200, 200L);

                    class MagmaCubeListener implements Listener {
                        int health = 10;
                        boolean cd = false;

                        @EventHandler
                        public void onEntityDamage(EntityDamageByEntityEvent e) {

                            if (e.getEntity().equals(magmaCube)) {
                                if (magmaCube.getSize() == 1) {
                                    e.setCancelled(true);
                                    health++;
                                    bossBar.setProgress(health / 10.0);
                                    return;
                                }
                                e.setDamage(0);

                                if (!cd) {
                                    health--;
                                    bossBar.setProgress(health / 10.0);
                                    cd = true;
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            cd = false;
                                        }
                                    }.runTaskLater(plugin, 20L);
                                }
                            }

                            if (health == 0) {
                                magmaCube.remove();
                                HandlerList.unregisterAll(this);

                                cancel();
                                ability.cancel();
                                bossBar.removeAll();
                                Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(entity -> {
                                    if (entity instanceof Player pl) {
                                        success(pl, new ItemStack(Material.CHAIN_COMMAND_BLOCK), 65, 2);
                                    }
                                });
                            }

                        }
                    }

                    listener = new MagmaCubeListener();
                    Bukkit.getPluginManager().registerEvents(listener, plugin);
                }

                //function
                Bukkit.getOnlinePlayers().forEach(bossBar::removePlayer);
                AtomicReference<Boolean> exist = new AtomicReference<>(false);
                Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(e -> {
                    if (e instanceof Player p) bossBar.addPlayer(p);
                    if (e instanceof MagmaCube) exist.set(true);
                });

                time++;

                if (!exist.get() || time == 60 * 24) {
                    cancel();
                    ability.cancel();
                    bossBar.removeAll();
                    magmaCube.remove();
                    HandlerList.unregisterAll(listener);
                }

            }
        }.runTaskTimer(plugin, 50 * 20L, 20L);

    }
}
