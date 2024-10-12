package net.spookytime.tntrun.command;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.spookytime.tntrun.TntRun;
import net.spookytime.tntrun.listener.TntRunListener;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TntRunCommand implements TabExecutor {
  
  @NotNull
  Set<String> exclude;
  
  @NotNull
  Location teleportTo;
  
  @NotNull
  Plugin plugin;
  
  @NotNull
  List<CuboidRegion> tnts;
  
  @NotNull
  World outRegionWorld;
  
  int djumpRandomizerPlayerAmount;
  
  @NotNull
  Random random = new Random();
  
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (!sender.isOp()) {
      return true;
    }
    
    if (args.length == 0) {
      return true;
    }
    
    String subcommand = args[0];
    switch (subcommand) {
      case "enable" -> handleEnableSubCommand(sender, args);
      case "disable" -> handleDisableSubCommand(sender, args);
      case "reset" -> handleResetSubCommand(sender, args);
      case "djump" -> handleDoubleJumpSubCommand(sender, args);
      case "refill" -> handleRefillSubCommand(sender, args);
    }
    
    return true;
  }
  
  private void handleEnableSubCommand(@NotNull CommandSender sender, String @NotNull [] args) {
    if (TntRun.destroyTnt) {
      sender.sendMessage(Component.text("Разрушение ТНТ уже включено!").color(NamedTextColor.RED));
      return;
    }
    
    TntRun.destroyTnt = true;
    sender.sendMessage(Component.text("Разрушение ТНТ включено!").color(NamedTextColor.GREEN));
  }
  
  private void handleDisableSubCommand(@NotNull CommandSender sender, String @NotNull [] args) {
    if (!TntRun.destroyTnt) {
      sender.sendMessage(Component.text("Разрушение ТНТ уже выключено!").color(NamedTextColor.RED));
      return;
    }
    
    TntRun.destroyTnt = false;
    sender.sendMessage(Component.text("Разрушение ТНТ выключено!").color(NamedTextColor.GREEN));
  }
  
  private void handleResetSubCommand(@NotNull CommandSender sender, String @NotNull [] args) {
    Bukkit.getOnlinePlayers()
      .stream()
      .filter(player -> !exclude.contains(player.getName()))
      .forEach(player -> {
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(teleportTo);
      });
    sender.sendMessage(Component.text("Игрокам установлен режим выживания и они телепортированы!").color(NamedTextColor.GREEN));
  }
  
  private void handleDoubleJumpSubCommand(@NotNull CommandSender sender, String @NotNull [] args) {
    ItemStack itemStack = new ItemStack(Material.FEATHER);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.displayName(Component.text("[★] Двойной прыжок").color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD));
    itemMeta.getPersistentDataContainer()
      .set(new NamespacedKey(plugin, "perk"), PersistentDataType.STRING, "double-jump");
    itemStack.setItemMeta(itemMeta);
    
    Bukkit.getOnlinePlayers().forEach(player -> player.getInventory().addItem(itemStack));
  }
  
  @SneakyThrows
  private void handleRefillSubCommand(@NotNull CommandSender sender, String @NotNull [] args) {
    TntRunListener.destroyed.clear();
    Bukkit.getScheduler().cancelTasks(plugin);
    for (CuboidRegion region : tnts) {
      EditSession editSession = WorldEdit.getInstance().newEditSession(outRegionWorld);
      RandomPattern randomPattern = new RandomPattern();
      randomPattern.add(BlockTypes.TNT.getDefaultState(), 100);
//      editSession.setBlocks(region, randomPattern);
      for (BlockVector3 vector : region) {
        if (editSession.getBlock(vector).getBlockType() == BlockTypes.AIR) {
          editSession.setBlock(vector, randomPattern.applyBlock(vector));
        }
      }
      editSession.close();
    }
    sender.sendMessage(Component.text("Динамит установлен!").color(NamedTextColor.GREEN));
  }
  
  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                              @NotNull String alias, @NotNull String[] args) {
    if (args.length == 1) {
      return List.of("enable", "disable", "reset", "djump", "refill");
    }
    
    return Collections.emptyList();
  }
  
}
