package net.spookytime.tntrun;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.bukkit.util.Materials;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.spookytime.TntDestroyStrategy;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorldEditTntDestroyStrategy implements TntDestroyStrategy {
  
  @NotNull
  Plugin plugin;
  
  @NotNull
  Map<Long, List<Block>> blockRemovalQueue = new HashMap<>();
  
  @NotNull
  World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
  
  @Override
  public void scheduleTntDestroy(@NotNull Block block, int ticks) {
    long removalTime = Bukkit.getServer().getCurrentTick() + ticks;
    blockRemovalQueue.computeIfAbsent(removalTime, k -> new ArrayList<>()).add(block);
  }
  
  @Override
  public void enable() {
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      long currentTick = Bukkit.getServer().getCurrentTick();
      List<Block> blocksToRemove = blockRemovalQueue.remove(currentTick);
      
      if (blocksToRemove != null) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
          blocksToRemove.forEach(block -> {
            try {
              editSession.setBlock(BukkitAdapter.asBlockVector(block.getLocation()), BlockTypes.AIR.getDefaultState());
              editSession.setBlock(BukkitAdapter.asBlockVector(block.getLocation().subtract(0, 1, 0)), BlockTypes.AIR.getDefaultState());
            } catch (MaxChangedBlocksException e) {
              throw new RuntimeException(e);
            }
          });
        }
      }
    }, 1L, 1L);
  }
  
}
