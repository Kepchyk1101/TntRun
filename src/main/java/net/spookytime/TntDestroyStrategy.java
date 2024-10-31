package net.spookytime;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public interface TntDestroyStrategy {
  
  void scheduleTntDestroy(@NotNull Block block, int ticks);
  
  default void enable() {
  
  }
  
  default void disable() {
  
  }
  
}
