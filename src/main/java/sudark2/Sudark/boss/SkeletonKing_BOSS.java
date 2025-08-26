package sudark2.Sudark.boss;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static sudark2.Sudark.boss.Success.success;
import static sudark2.Sudark.boss.ZoneExpand.SpiralPlaneGenerator.resumeZone;

public class SkeletonKing_BOSS {

    public static void newTask(Plugin plugin, Location Core, Player pl, int range) {
        BossBar bossBar = Bukkit.createBossBar("§lSkeletonKING", BarColor.YELLOW, BarStyle.SEGMENTED_20);

        Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(e -> {
            if (e instanceof Player p) {
                Title.title(p, "-§e§lSkeletonKing§f-", "恭迎你们的新王——骸骨之王");
                Location plCore = Core.clone().add(7, 10, 0);
                plCore.setYaw(90);
                p.teleport(plCore);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 5, 100, false, false, false));
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
                    skeleton = (WitherSkeleton) Core.getWorld().spawnEntity(Core.clone().add(0, 3, 0), EntityType.WITHER_SKELETON);
                    skeleton.setMaxHealth(40);
                    skeleton.setHealth(40);
                    skeleton.setCustomName("骸骨将军");
                    armor(skeleton);

                    Core.getWorld().spawnParticle(Particle.SCULK_SOUL, Core.clone().add(0, 4, 0), 60, 0.8f, 0.8f, 0.8f, 0.1f);
                }

                time++;

                {
                    int bar = 0;
                    if (skeleton != null && !skeleton.isDead())
                        bar = (int) ((int) skeleton.getHealth() / skeleton.getMaxHealth() * 4) + 16;
                    if (wither != null && !wither.isDead())
                        bar = (int) (wither.getHealth() / 200 * 13) + 3;
                    if (witherHead != null)
                        bar = head;
                    bossBar.setProgress(bar / 20f);
                    Bukkit.getOnlinePlayers().forEach(bossBar::removePlayer);
                    Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(e -> {
                        if (e instanceof Player p) bossBar.addPlayer(p);
                    });
                }

                if (skeleton != null && skeleton.isDead() && sb) {
                    wither = (Wither) Core.getWorld().spawnEntity(Core.clone().add(0, 5, 0), EntityType.WITHER);
                    wither.setMaxHealth(600);
                    wither.setHealth(200);
                    wither.setCustomName("骸骨真身");
                    Core.getWorld().spawnParticle(Particle.SCULK_SOUL, Core.clone().add(0, 3, 0), 60, 0.8f, 0.8f, 0.8f, 0.1f);

                    for (int i = -1; i <= 1; i += 2) {

                        WitherSkeleton skeletonX = (WitherSkeleton) Core.getWorld().spawnEntity(
                                Core.clone().add(i * 4, 3, 0 * 4), EntityType.WITHER_SKELETON
                        );
                        armorX(skeletonX);
                        Core.getWorld().spawnParticle(Particle.CLOUD, Core.clone().add(i * 4, 0, 0), 10, 0.8f, 0.8f, 0.8f, 0.1f);

                    }
                    for (int j = -1; j <= 1; j += 2) {

                        WitherSkeleton skeletonX = (WitherSkeleton) Core.getWorld().spawnEntity(
                                Core.clone().add(0, 3, j * 4), EntityType.WITHER_SKELETON
                        );
                        armorX(skeletonX);
                        Core.getWorld().spawnParticle(Particle.CLOUD, Core.clone().add(0, 0, j * 4), 10, 0.8f, 0.8f, 0.8f, 0.1f);

                    }

                    sb = false;
                }

                if (wither != null && skeleton != null && skeleton.isDead() && wither.isDead()) {
                    if (wb) {
                        witherHead = (BlockDisplay) Head.getWorld().spawnEntity(Head, EntityType.BLOCK_DISPLAY);
                        witherHead.setBlock(Material.WITHER_SKELETON_SKULL.createBlockData());

                        Core.getWorld().spawnParticle(Particle.SCULK_SOUL, Core.clone().add(0, 5, 0), 60, 0.8f, 0.8f, 0.8f, 0.1f);

                        WitherHead = new BukkitRunnable() {
                            @Override
                            public void run() {
                                Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(en -> {
                                    if (en instanceof Player p) {
                                        p.playSound(p, Sound.ENTITY_WITHER_SHOOT, 1, 1);
                                        p.sendActionBar("把 §c红沙§f 全炸掉！");

                                        Location from = Head.clone().add(0, 0.5, 0);
                                        Location to = p.getLocation().clone().add(0, 1.5, 0);
                                        Vector dir = to.toVector().subtract(from.toVector()).normalize();

                                        WitherSkull skull = (WitherSkull) Head.getWorld().spawnEntity(from, EntityType.WITHER_SKULL);
                                        skull.setVelocity(dir.multiply(1.5));
                                        skull.setCharged(true);
                                        skull.setCustomName("骸骨残躯");

                                        // 获取 from → to 的方向向量（玩家头部）
                                        Vector direction = to.clone().subtract(from).toVector().normalize();
                                        Vector3f dirVec = new Vector3f((float) direction.getX(), (float) direction.getY(), (float) direction.getZ());
                                        Quaternionf rotation = new Quaternionf().rotateTo(new Vector3f(0, 0, 1), dirVec);
                                        Vector3f scale = new Vector3f(3f, 3f, 3f);
                                        Vector3f translation = new Vector3f(0f, 0f, 0f);
                                        Transformation transformation = new Transformation(translation, rotation, scale, rotation);

                                        witherHead.setTransformation(transformation);

                                    }
                                });
                            }
                        }.runTaskTimer(plugin, 0, 30);

                        wb = false;
                    } else {
                        Location center = Core.clone().add(0, 3, 0);
                        int cx = center.getBlockX();
                        int cy = center.getBlockY();
                        int cz = center.getBlockZ();
                        int count = 0;

                        for (int x = cx - 5; x <= cx + 5; x++) {
                            for (int z = cz - 5; z <= cz + 5; z++) {
                                for (int y = cy; y <= cy + 3; y++) { // 只向上延伸3格
                                    Block block = center.getWorld().getBlockAt(x, y, z);
                                    if (block.getType() == Material.RED_SAND) {
                                        count++;
                                    }
                                }
                            }
                        }
                        head = Math.min(count / 6, 20);
                    }
                }


                if (head <= 0) {
                    Head.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, Head, 30, 1.2f, 1.2f, 1.2f, 0.1f);

                    cancel();
                    WitherHead.cancel();

                    witherHead.remove();

                    bossBar.removeAll();

                    Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(entity -> {

                        if (entity instanceof Player pl) {
                            success(pl, new ItemStack(Material.REPEATING_COMMAND_BLOCK), 55, 3);
                        }
                    });
                    resumeZone(Core, plugin);
                    return;
                }

                if (time == 20 * 60) {
                    cancel();
                    WitherHead.cancel();
                    bossBar.removeAll();

                    if (witherHead != null) witherHead.remove();
                    if (skeleton != null) skeleton.remove();
                    if (wither != null) wither.remove();
                    resumeZone(Core, plugin);
                }

            }
        }.runTaskTimer(plugin, 35 * 20, 20);
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
