package sudark2.Sudark.boss;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class Success {

    public static void success(Player pl, ItemStack gift, int lvl, int grade) {

        pl.getInventory().addItem(gift);

        firework(pl, grade);

        lvlUp(pl, lvl);

    }

    public static void firework(Player pl, int grade) {
        new BukkitRunnable() {
            int time = 0;

            @Override
            public void run() {
                time++;
                if (time == 8) {
                    cancel();
                }

                Firework fw = (Firework) pl.getWorld().spawnEntity(pl.getLocation(), EntityType.FIREWORK_ROCKET);
                FireworkMeta fwm = fw.getFireworkMeta();
                switch (grade) {
                    case 1 ->
                            fwm.addEffect(FireworkEffect.builder().withColor(Color.BLACK).withColor(Color.YELLOW).build());
                    case 2 ->
                            fwm.addEffect(FireworkEffect.builder().withColor(Color.AQUA).withColor(Color.YELLOW).trail(true).build());
                    case 3 ->
                            fwm.addEffect(FireworkEffect.builder().withColor(Color.RED).withColor(Color.YELLOW).withFade(Color.ORANGE).flicker(true).build());
                }
                fwm.setPower(0);
                fw.setFireworkMeta(fwm);

            }
        }.runTaskTimer(Boss.getP(), 0, 15L);
    }

    public static void lvlUp(Player pl, int lvl) {
        new BukkitRunnable() {
            int time = 0;

            @Override
            public void run() {
                if (time <= 5)
                    pl.giveExpLevels(1);
                if (time <= 10)
                    pl.giveExpLevels(1);
                if (time <= 15)
                    pl.giveExpLevels(1);
                if (time + 30 < lvl)
                    pl.giveExpLevels(1);
                time++;

                if (time + 30 == lvl) {
                    pl.playSound(pl, Sound.ENTITY_PLAYER_LEVELUP, 1.1f, 0.8f);
                    cancel();
                    return;
                }
                pl.playSound(pl, Sound.BLOCK_GLASS_HIT, 1, 1);
            }
        }.runTaskTimer(Boss.getP(), 0, 5L);

    }
}
