package net.spookytime.tntrun.hook;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PAPIExpansion extends PlaceholderExpansion {
  
  @NotNull
  Set<String> exclude;
  
  @Override
  public @NotNull String getIdentifier() {
    return "tr";
  }
  
  @Override
  public @NotNull String getAuthor() {
    return "Kepchyk1101";
  }
  
  @Override
  public @NotNull String getVersion() {
    return "1.0";
  }
  
  @Override
  @SuppressWarnings("SwitchStatementWithTooFewBranches")
  public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
    return switch (params) {
      case "online" -> String.valueOf(Bukkit.getOnlinePlayers()
        .stream()
        .filter(this::filter)
        .count());
      default -> null;
    };
  }
  
  private boolean filter(@NotNull Player player) {
    if (exclude.contains(player.getName())) {
      return false;
    }
    return player.getGameMode() != GameMode.SPECTATOR;
  }
  
}
