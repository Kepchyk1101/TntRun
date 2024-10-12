package net.spookytime.tntrun.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TntRunListener implements Listener {
  
  @NotNull
  public static Set<Block> destroyed = new HashSet<>();
  @NotNull
  Plugin plugin;
  long tntDestroyDelay;
  @NotNull
  Region outRegion;
  @NotNull
  Set<String> exclude;
  
  @EventHandler
  private void on(@NotNull PlayerMoveEvent event) {
    if (!TntRun.destroyTnt) {
      return;
    }
    
    if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
      return;
    }
    
    Block block = event.getPlayer()
      .getLocation()
      .subtract(0, 1, 0)
      .getBlock();
    if (block.getType() != Material.TNT) {
      return;
    }
    
    if (destroyed.contains(block)) {
      return;
    }
    
    destroyed.add(block);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      block.setType(Material.AIR);
      Block relative = block.getRelative(0, -1, 0);
      if (relative.getType() == Material.TNT) {
        relative.setType(Material.AIR);
      }
    }, tntDestroyDelay);
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
    if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
      return;
    }
    
    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
  }
  
}
