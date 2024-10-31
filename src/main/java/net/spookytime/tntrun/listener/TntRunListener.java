package net.spookytime.tntrun.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.spookytime.TntDestroyStrategy;
import net.spookytime.tntrun.TntRun;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TntRunListener implements Listener {
  
  private static double PLAYER_BOUNDINGBOX_ADD = 0.3;
  
  @NotNull
  public static Set<Block> destroyed = new HashSet<>();
  @NotNull
  Plugin plugin;
  long tntDestroyDelay;
  @NotNull
  Region outRegion;
  @NotNull
  Set<String> exclude;
  @NotNull
  TntDestroyStrategy tntDestroyStrategy;
  
  @EventHandler
  private void on(@NotNull PlayerMoveEvent event) {
    if (!TntRun.destroyTnt) {
      return;
    }
    
    if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
      return;
    }
    
    Location subtract = event.getPlayer().getLocation().add(0, -1, 0);
    Block block = getBlockUnderPlayer(subtract.getBlockY(), subtract);
    if (block == null) {
      return;
    }
    
    if (block.getType() != Material.TNT) {
      return;
    }
    
    if (destroyed.contains(block)) {
      return;
    }
    
    destroyed.add(block);
    tntDestroyStrategy.scheduleTntDestroy(block, (int) tntDestroyDelay);
  }
  
  @EventHandler
  private void on1(@NotNull PlayerMoveEvent event) {
    Player player = event.getPlayer();
    if (exclude.contains(player.getName())) {
      return;
    }
    
    if (isInsideRegion(event.getTo())) {
      event.getPlayer().setGameMode(GameMode.SPECTATOR);
    }
  }
  
  private boolean isInsideRegion(@NotNull Location location) {
    return outRegion.contains(BukkitAdapter.asBlockVector(location));
  }
  
  @EventHandler
  private void on(@NotNull PlayerInteractEvent event) {
    Action action = event.getAction();
    if (action != Action.RIGHT_CLICK_AIR && action != Action.LEFT_CLICK_AIR) {
      return;
    }
    
    Player player = event.getPlayer();
    ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
    if (!itemInMainHand.hasItemMeta()) {
      return;
    }
    
    PersistentDataContainer pdc = itemInMainHand.getItemMeta().getPersistentDataContainer();
    String perk = pdc.get(new NamespacedKey(plugin, "perk"), PersistentDataType.STRING);
    if (perk == null || !perk.equals("double-jump")) {
      return;
    }
    
    Vector velocity = player.getVelocity();
    velocity.setY(1.10);
    velocity.add(
      player.getLocation()
        .getDirection()
        .normalize()
        .multiply(0.2)
    );
    player.setVelocity(velocity);
    itemInMainHand.setAmount(itemInMainHand.getAmount() - 1);
    player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
  }
  
  @EventHandler
  private void on(@NotNull PlayerJoinEvent event) {
    Player player = event.getPlayer();
    if (!player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
    }
  }
  
  @Nullable
  private Block getBlockUnderPlayer(int y, Location location) {
    PlayerPosition loc = new PlayerPosition(location.getX(), y, location.getZ());
    Block b11 = loc.getBlock(location.getWorld(), +PLAYER_BOUNDINGBOX_ADD, -PLAYER_BOUNDINGBOX_ADD);
    if (b11.getType() != Material.AIR) {
      return b11;
    }
    Block b12 = loc.getBlock(location.getWorld(), -PLAYER_BOUNDINGBOX_ADD, +PLAYER_BOUNDINGBOX_ADD);
    if (b12.getType() != Material.AIR) {
      return b12;
    }
    Block b21 = loc.getBlock(location.getWorld(), +PLAYER_BOUNDINGBOX_ADD, +PLAYER_BOUNDINGBOX_ADD);
    if (b21.getType() != Material.AIR) {
      return b21;
    }
    Block b22 = loc.getBlock(location.getWorld(), -PLAYER_BOUNDINGBOX_ADD, -PLAYER_BOUNDINGBOX_ADD);
    if (b22.getType() != Material.AIR) {
      return b22;
    }
    return null;
  }
  
  private static class PlayerPosition {
    
    private double x;
    private int y;
    private double z;
    
    public PlayerPosition(double x, int y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }
    
    public Block getBlock(World world, double addx, double addz) {
      return world.getBlockAt(NumberConversions.floor(x + addx), y, NumberConversions.floor(z + addz));
    }
  }
  
}
