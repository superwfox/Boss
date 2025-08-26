package sudark2.Sudark.boss;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

import static sudark2.Sudark.boss.Success.success;
import static sudark2.Sudark.boss.ZoneExpand.SpiralPlaneGenerator.resumeZone;

public class Silverhorn_BOSS {
    public static void newTask(Plugin plugin, Location Core, Player pl, int range) {
        BossBar bossBar = Bukkit.createBossBar("SilverHorn", BarColor.RED, BarStyle.SOLID);

        Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(e -> {
            if (e instanceof Player p) {
                Title.title(p, "-§e§lSilverHorn§f-", "谁敢越我山头——银角");
                p.teleport(Core.clone().add(-2,0,-2));
            }
        });

        new BukkitRunnable() {
            int time = 0;
            Vindicator vindicator;
            World world = Core.getWorld();
            Set<Breeze> breezes = new HashSet<>();
            final int MaxLife = 400;

            @Override
            public void run() {
                time++;

                if (time == 1200 || !pl.getWorld().equals(Core.getWorld()) || pl.getLocation().distanceSquared(Core) > 900) {
                    resumeZone(Core, plugin);
                    if (vindicator != null) vindicator.remove();
                    if (!breezes.isEmpty()) breezes.forEach(Breeze::remove);
                    bossBar.removeAll();
                    cancel();
                    return;
                }

                if (time == 5) {
                    vindicator = (Vindicator) world.spawnEntity(Core.clone().add(2, 10, 2), EntityType.VINDICATOR);
                    vindicator.setMaxHealth(MaxLife);
                    vindicator.setHealth(MaxLife);
                    EntityEquipment ep = vindicator.getEquipment();
                    ItemStack[] eps = armor(plugin);
                    ep.setHelmet(eps[0]);
                    ep.setHelmetDropChance(0.25f);
                    ep.setChestplate(eps[1]);
                    ep.setChestplateDropChance(0.25f);
                    ep.setLeggings(eps[2]);
                    ep.setLeggingsDropChance(0.25f);
                    ep.setBoots(eps[3]);
                    ep.setBootsDropChance(0.25f);
                    for (int i = 0; i < 4; i++) {
                        Breeze breeze = (Breeze) world.spawnEntity(Core.clone().add(4,10,4), EntityType.BREEZE);
                        breeze.setGlowing(true);
                        breezes.add(breeze);
                    }
                }

                if (vindicator != null && vindicator.isDead()) {
                    bossBar.removeAll();
                    resumeZone(Core, plugin);
                    Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(e -> {
                        if (e instanceof Player pl) {
                            success(pl, new ItemStack(Material.COMMAND_BLOCK), 50, 1);
                        }
                    });
                    if (!breezes.isEmpty()) breezes.forEach(Breeze::remove);
                    cancel();
                    return;
                }

                Bukkit.getOnlinePlayers().forEach(bossBar::removePlayer);

                Core.getWorld().getNearbyEntities(Core, range, range, range).forEach(e -> {
                    if (e instanceof Player pl) {
                        bossBar.addPlayer(pl);
                    }
                });

                if (vindicator != null) bossBar.setProgress(vindicator.getHealth() / MaxLife);

            }
        }.runTaskTimer(plugin, 300, 20L);

    }

    public static ItemStack[] armor(Plugin plugin) {
        ItemStack[] itemStacks = new ItemStack[4];
        itemStacks[0] = strength(Material.IRON_HELMET, 3, plugin);
        itemStacks[1] = strength(Material.IRON_CHESTPLATE, 8, plugin);
        itemStacks[2] = strength(Material.IRON_LEGGINGS, 6, plugin);
        itemStacks[3] = strength(Material.IRON_BOOTS, 3, plugin);
        return itemStacks;
    }

    public static ItemStack strength(Material m, int amount, Plugin plugin) {
        ItemStack itemStack = new ItemStack(m, 1);
        ItemMeta meta = itemStack.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "custom_armor");

        AttributeModifier modifier = new AttributeModifier(
                key,
                amount,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.ARMOR
        );
        AttributeModifier toughness = new AttributeModifier(
                key,
                3,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.ARMOR
        );
        AttributeModifier kickback = new AttributeModifier(
                key,
                1,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.ARMOR
        );
        meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, kickback);

        meta.addAttributeModifier(Attribute.ARMOR, modifier);
        meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, toughness);

        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
