package sudark2.Sudark.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import static sudark2.Sudark.boss.Success.success;

public class SkeletonKing_BOSS {

    public static void newTask(Plugin plugin, Location Core, Player pl, int range) {
        BossBar bossBar = Bukkit.createBossBar("§lSkeletonKING", BarColor.YELLOW, BarStyle.SEGMENTED_20);

        Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(e -> {
            if (e instanceof Player p) {
                Title.title(p, "-§e§lSkeletonKing§f-", "恭迎你们的新王——骸骨将军");
                p.teleport(Core.clone().add(0, 0, 5));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 5, 10, false, false, false));
            }
        });

        new BukkitRunnable() {
            int time = 0, head = 3;

            WitherSkeleton skeleton = null;
            Boolean sb = true;

            Wither wither = null;
            Boolean wb = true;

            BlockDisplay witherHead = null;
            BukkitTask WitherHead = null;
            Location Head = Core.clone().add(0, 5, 0);

            @Override
            public void run() {

                if (time == 0) {
                    skeleton = (WitherSkeleton) Core.getWorld().spawnEntity(Core, EntityType.WITHER_SKELETON);
                    skeleton.setMaxHealth(40);
                    skeleton.setHealth(40);
                    skeleton.setCustomName("骸骨将军");
                    armor(skeleton);
                }

                time++;

                {
                    int bar = 0;
                    if (!skeleton.isDead()) bar = (int) ((int) skeleton.getHealth() / skeleton.getMaxHealth() * 4) + 16;
                    if (!wither.isDead()) bar = (int) (wither.getHealth() / wither.getMaxHealth() * 13) + 3;
                    if (witherHead != null) bar = head;
                    bossBar.setProgress(bar / 20f);
                    Bukkit.getOnlinePlayers().forEach(bossBar::removePlayer);
                    Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(e -> {
                        if (e instanceof Player p) bossBar.addPlayer(p);
                    });
                }

                if (skeleton.isDead() && sb) {
                    wither = (Wither) Core.getWorld().spawnEntity(Core, EntityType.WITHER);
                    wither.setMaxHealth(400);
                    wither.setHealth(199);
                    wither.setCustomName("骸骨真身");

                    for (int i = 0; i < 4; i++) {
                        WitherSkeleton skeletonX = (WitherSkeleton) Core.getWorld().spawnEntity(Core, EntityType.WITHER_SKELETON);
                        armorX(skeletonX);
                    }

                    sb = true;
                }

                if (skeleton.isDead() && wither.isDead() && wb) {
                    witherHead = (BlockDisplay) Head.getWorld().spawnEntity(Head, EntityType.BLOCK_DISPLAY);
                    witherHead.setBlock(Material.WITHER_SKELETON_SKULL.createBlockData());

                    witherHead.setDisplayWidth(2);
                    witherHead.setDisplayHeight(2);

                    WitherHead = new BukkitRunnable() {
                        @Override
                        public void run() {
                            Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(en -> {
                                if (en instanceof Player p) {
                                    p.playSound(p, Sound.ENTITY_WITHER_SHOOT, 1, 1);

                                    Location from = Head.clone().add(0, 0.5, 0);
                                    Location to = p.getLocation().clone().add(0, 1.5, 0);
                                    Vector dir = to.toVector().subtract(from.toVector()).normalize();

                                    WitherSkull skull = (WitherSkull) Head.getWorld().spawnEntity(from, EntityType.WITHER_SKULL);
                                    skull.setVelocity(dir.multiply(1.2));
                                    skull.setCharged(true);
                                    skull.setCustomName("骸骨残躯");

                                    Vector direction = to.toVector().subtract(from.toVector()).normalize();
                                    double yaw = Math.atan2(-direction.getX(), direction.getZ()); // 朝向角度（绕 Y 轴）
                                    float angleRadians = (float) yaw; // 弧度

                                    AxisAngle4f rotation = new AxisAngle4f(0, 1, 0, angleRadians);

                                    Vector3f translation = new Vector3f(0f, 0f, 0f);
                                    Vector3f scale = new Vector3f(1f, 1f, 1f);
                                    Transformation transformation = new Transformation(translation, rotation, scale, rotation);
                                    witherHead.setTransformation(transformation);

                                }
                            });
                        }
                    }.runTaskTimer(plugin, 0, 40);

                    class SkeletonKingListener implements Listener {
                        @EventHandler
                        public void onEntityDamage(EntityDamageByEntityEvent e) {
                            if (e.getEntity() instanceof BlockDisplay bl) {
                                if (bl == witherHead) {
                                    head--;
                                    Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(en -> {
                                        if (en instanceof Player p) p.playSound(p, Sound.ENTITY_WITHER_HURT, 1, 1);
                                    });
                                }
                            }

                        }
                    }
                    Bukkit.getPluginManager().registerEvents(new SkeletonKingListener(), plugin);

                    wb = false;
                }

                if (head <= 0) {
                    cancel();
                    WitherHead.cancel();

                    witherHead.remove();

                    Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(entity -> {
                        if (entity instanceof Player pl) {
                            success(pl, new ItemStack(Material.REPEATING_COMMAND_BLOCK), 55, 3);
                        }
                    });
                }

            }
        }.runTaskTimer(plugin, 15 * 20, 20);
    }

    public static void armor(WitherSkeleton skeleton) {
        skeleton.getEquipment().setHelmet(enchant(Material.NETHERITE_HELMET));
        skeleton.getEquipment().setChestplate(enchant(Material.NETHERITE_CHESTPLATE));
        skeleton.getEquipment().setLeggings(enchant(Material.NETHERITE_LEGGINGS));
        skeleton.getEquipment().setBoots(enchant(Material.NETHERITE_BOOTS));
        skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
    }

    public static void armorX(WitherSkeleton skeleton) {
        skeleton.getEquipment().setHelmet(enchant(Material.GOLDEN_HELMET));
        skeleton.getEquipment().setChestplate(enchant(Material.GOLDEN_CHESTPLATE));
        skeleton.getEquipment().setLeggings(enchant(Material.GOLDEN_LEGGINGS));
        skeleton.getEquipment().setBoots(enchant(Material.GOLDEN_BOOTS));
        skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));
    }

    public static ItemStack enchant(Material material) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.addUnsafeEnchantment(Enchantment.PROTECTION, 5);
        itemStack.addUnsafeEnchantment(Enchantment.THORNS, 5);
        return itemStack;
    }
}
