package sudark2.Sudark.boss;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;
import static sudark2.Sudark.boss.ZoneExpand.SpiralPlaneGenerator.generateSpiralBlockQueueAsync;

public class ZoneExpand implements Listener {

    @EventHandler
    public void onBossSpawn(PlayerInteractEvent e) {

        if (e.getClickedBlock() == null) return;
        Block bl = e.getClickedBlock();
        Player pl = e.getPlayer();
        if (!e.getClickedBlock().getType().equals(Material.DRAGON_EGG)) return;
        if (!pl.getWorld().getName().equals("BEEF-DUNE")) return;

        generateSpiralBlockQueueAsync(this, Boss.zones.get(),Boss.endZones.get() ).thenAccept(blockQueue -> {
            // 同步搬运
            SpiralPlaneGenerator.moveBlocksSpirally(
                    getPlugin(Boss.class),
                    blockQueue,
                    bl.getLocation(),
                    8,
                    1L // 每2tick执行
            );
        });
    }

    public void expand(Block bl, Player pl) {

    }

    public class SpiralPlaneGenerator {

        // 方向枚举：上、右、下、左（顺时针）
        private enum Direction {
            UP(0, -1), RIGHT(1, 0), DOWN(0, 1), LEFT(-1, 0);

            final int dx, dz;

            Direction(int dx, int dz) {
                this.dx = dx;
                this.dz = dz;
            }

            // 左转90度
            Direction turnLeft() {
                return values()[(ordinal() + 3) % 4];
            }
        }

        /**
         * 异步生成螺旋顺序的方块队列，包含所有Y层的方块
         * @param plugin Bukkit插件实例
         * @param corner1 矩形区域一个角的坐标
         * @param corner2 矩形区域对角的坐标
         * @return CompletableFuture<Queue<Location>> 按螺旋顺序存储的方块位置
         */
        public static CompletableFuture<Queue<Location>> generateSpiralBlockQueueAsync(Plugin plugin, Location corner1, Location corner2) {
            return CompletableFuture.supplyAsync(() -> {
                Queue<Location> blockQueue = new LinkedList<>();
                World world = corner1.getWorld();

                int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
                int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
                int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
                int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

                int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
                int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());

                int centerX = (minX + maxX) / 2;
                int centerZ = (minZ + maxZ) / 2;

                // 记录已访问的X-Z位置
                Set<String> visitedXZ = new HashSet<>();
                Queue<Location> spiralPoints = new LinkedList<>();

                // 初始化螺旋：从中心点开始，朝右
                int x = centerX;
                int z = centerZ;
                Direction direction = Direction.RIGHT;

                // 添加中心点的所有Y层
                String key = x + "," + z;
                if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) {
                    for (int y = minY; y <= maxY; y++) {
                        spiralPoints.add(new Location(world, x, y, z));
                    }
                    visitedXZ.add(key);
                }

                // 螺旋生成（X-Z平面）
                while (x >= minX - 1 && x <= maxX + 1 && z >= minZ - 1 && z <= maxZ + 1) {
                    // 检查左边
                    Direction leftDir = direction.turnLeft();
                    int leftX = x + leftDir.dx;
                    int leftZ = z + leftDir.dz;
                    String leftKey = leftX + "," + leftZ;

                    if (!visitedXZ.contains(leftKey) && leftX >= minX && leftX <= maxX && leftZ >= minZ && leftZ <= maxZ) {
                        // 左边未访问，转向左边
                        direction = leftDir;
                        x = leftX;
                        z = leftZ;
                    } else {
                        // 继续向前
                        x += direction.dx;
                        z += direction.dz;
                    }

                    // 添加当前点的所有Y层
                    key = x + "," + z;
                    if (x >= minX && x <= maxX && z >= minZ && z <= maxZ && !visitedXZ.contains(key)) {
                        for (int y = minY; y <= maxY; y++) {
                            spiralPoints.add(new Location(world, x, y, z));
                        }
                        visitedXZ.add(key);
                    }

                    // 检查是否卡住
                    boolean stuck = true;
                    for (Direction dir : Direction.values()) {
                        int nx = x + dir.dx;
                        int nz = z + dir.dz;
                        if (nx >= minX && nx <= maxX && nz >= minZ && nz <= maxZ && !visitedXZ.contains(nx + "," + nz)) {
                            stuck = false;
                            break;
                        }
                    }
                    if (stuck) {
                        break;
                    }
                }

                // 补全：添加遗漏的X-Z点（包含所有Y层）
                for (x = minX; x <= maxX; x++) {
                    for (z = minZ; z <= maxZ; z++) {
                        key = x + "," + z;
                        if (!visitedXZ.contains(key)) {
                            for (int y = minY; y <= maxY; y++) {
                                spiralPoints.add(new Location(world, x, y, z));
                            }
                            visitedXZ.add(key);
                        }
                    }
                }

                blockQueue.addAll(spiralPoints);
                return blockQueue;
            });
        }

        /**
         * 同步将方块队列搬到目标位置，按螺旋顺序重新出现
         * @param plugin Bukkit插件实例
         * @param blockQueue 方块队列（来自generateSpiralBlockQueueAsync）
         * @param target 目标中心坐标（新位置）
         * @param blocksPerTick 每tick设置的方块数
         * @param tickDelay 每次设置的间隔（tick）
         */
        public static void moveBlocksSpirally(Plugin plugin, Queue<Location> blockQueue, Location target, int blocksPerTick, long tickDelay) {
            World targetWorld = target.getWorld();
            int targetX = target.getBlockX();
            int targetY = target.getBlockY();
            int targetZ = target.getBlockZ();

            new BukkitRunnable() {
                @Override
                public void run() {
                    // 每次处理blocksPerTick个方块
                    for (int i = 0; i < blocksPerTick && !blockQueue.isEmpty(); i++) {
                        Location source = blockQueue.poll();
                        if (source == null) {
                            cancel();
                            return;
                        }

                        // 计算相对坐标
                        int relX = source.getBlockX();
                        int relY = source.getBlockY();
                        int relZ = source.getBlockZ();

                        // 目标位置 = 目标中心 + 相对偏移
                        Location newLoc = new Location(targetWorld, targetX + relX, targetY + relY, targetZ + relZ);

                        // 复制方块类型（空气用默认值）
                        Material blockType = source.getBlock().getType();
                        if (blockType == Material.AIR) {
                            blockType = Material.STONE; // 可自定义
                        }
                        newLoc.getBlock().setType(blockType);
                    }

                    // 队列为空，任务结束
                    if (blockQueue.isEmpty()) {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, tickDelay); // 同步执行
        }
    }

}
