package sudark2.Sudark.boss;

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

                if (18 <= T || T <= 6) {
                    Random R = new Random();
                    T = R.nextInt(1);
                } else {
                    Random R = new Random();
                    T = R.nextInt(1);
                }

                T = 1;

            }
        }.runTaskTimer(plugin, 0, 20 * 60 * 60L);
    }
}
