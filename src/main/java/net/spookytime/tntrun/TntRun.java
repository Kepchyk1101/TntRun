package net.spookytime.tntrun;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.spookytime.tntrun.command.TntRunCommand;
import net.spookytime.tntrun.hook.PAPIExpansion;
import net.spookytime.tntrun.listener.TntRunListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TntRun extends JavaPlugin {
  
  public static boolean destroyTnt = false;
  
  @Override
  @SuppressWarnings("DataFlowIssue")
  public void onEnable() {
    saveDefaultConfig();
    Set<String> exclude = new HashSet<>(getConfig().getStringList("exclude"));
    long tntDestroyDelay = getConfig().getLong("tnt-destroy-delay");
    Location teleportTo = getConfig().getLocation("teleport-to");
    World outRegionWorld = BukkitAdapter.adapt(Bukkit.getWorld(getConfig().getString("out-region.world")));
    String outRegionId = getConfig().getString("out-region.id");
    ProtectedRegion outRegion = getRegion(outRegionWorld, outRegionId);
    CuboidRegion region = new CuboidRegion(outRegion.getMinimumPoint(), outRegion.getMaximumPoint());
    int djumpRandomizerPlayerAmount = getConfig().getInt("double-jump-randomizer.player-amount");
    
    List<CuboidRegion> tntRegions = getConfig().getStringList("tnt-regions")
      .stream()
      .map(id -> getRegion(outRegionWorld, id))
      .map(protectedRegion -> new CuboidRegion(protectedRegion.getMinimumPoint(), protectedRegion.getMaximumPoint()))
      .toList();
    
    getCommand("tntrun").setExecutor(new TntRunCommand(exclude, teleportTo, this, tntRegions, outRegionWorld, djumpRandomizerPlayerAmount));
    getServer().getPluginManager().registerEvents(new TntRunListener(this, tntDestroyDelay, region, exclude), this);
    new PAPIExpansion(exclude).register();
  }
  
  @NotNull
  @SuppressWarnings("DataFlowIssue")
  private ProtectedRegion getRegion(@NotNull World world, @NotNull String id) {
    return WorldGuard.getInstance()
      .getPlatform()
      .getRegionContainer()
      .get(world)
      .getRegion(id);
  }
  
}
