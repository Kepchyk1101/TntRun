package net.spookytime;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PacketTntDestroyStrategy implements TntDestroyStrategy {
  
  @NotNull
  Plugin plugin;
  
  @NotNull
  Map<Long, List<Block>> blockRemovalQueue = new HashMap<>();
  
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
        for (Block block : blocksToRemove) {
          block.setType(Material.AIR);
          Block relative = block.getRelative(0, -1, 0);
          if (relative.getType() == Material.TNT) {
            relative.setType(Material.AIR);
          }
        }
      }
    }, 1L, 1L);
  }
  
}
