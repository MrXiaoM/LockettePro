package me.crafter.mc.lockettepro;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Team;

public class Dependency {
    
    protected static WorldGuardPlugin worldguard = null;
    protected static Plugin vault = null;
    protected static Permission permission = null;
    private static CoreProtectAPI coreProtectAPI;
    protected static boolean res = false;
    protected static boolean plot = false;
    public Dependency(Plugin plugin) {
        // WorldGuard
        Plugin worldguardplugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (!Config.worldguard || !(worldguardplugin instanceof WorldGuardPlugin)) {
            worldguard = null;
        } else {
            worldguard = (WorldGuardPlugin) worldguardplugin;
        }
        res = plugin.getServer().getPluginManager().getPlugin("Residence") != null;
        plot = plugin.getServer().getPluginManager().getPlugin("PlotSquared") != null;
        // Vault
        vault = plugin.getServer().getPluginManager().getPlugin("Vault");
        if (vault != null) {
            RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            permission = rsp.getProvider();
        }
        //&& CoreProtect.getInstance().getAPI().APIVersion() == 6
        if (Config.coreprotect && Bukkit.getPluginManager().getPlugin("CoreProtect") != null) {
        	try {
        		coreProtectAPI = CoreProtect.getInstance().getAPI();
                if (!coreProtectAPI.isEnabled()) {
                    coreProtectAPI = null;
                    plugin.getLogger().warning("CoreProtect API is not enabled!");
                }
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
    }
    
    public static boolean isProtectedFrom(Block block, Player player) {
        if (plot) {
            Location loc = BukkitUtil.getLocation(block.getLocation());
            PlotArea area = PlotSquared.get().getApplicablePlotArea(loc);
            Plot plot = area.getPlot(loc);
            if (plot != null && plot.isAdded(player.getUniqueId())) {
                return true;
            }
        }
        if (res) {
            ClaimedResidence res = ResidenceApi.getResidenceManager().getByLoc(block.getLocation());
            if (res != null) {
                if (res.getPermissions().playerHas(player, Flags.place, FlagPermissions.FlagCombo.OnlyTrue)) {
                    return true;
                }
            }
        }
        if (worldguard != null) {
            if (!worldguard.createProtectionQuery().testBlockPlace(player, block.getLocation(), block.getType())) {
                return true;
            }
        }
        return false;
    }
        
    public static boolean isPermissionGroupOf(String line, Player player){
        if (vault != null){
            try {
                String[] groups = permission.getPlayerGroups(player);
                for (String group : groups){
                    if (line.equals("[" + group + "]")) return true;
                }
            } catch (Exception e){}
        }
        return false;
    }
    
    public static boolean isScoreboardTeamOf(String line, Player player){
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team != null){
            if (line.equals("[" + team.getName() + "]")) return true;
        }
        return false;
    }

    public static void logPlacement(Player player, Block block) {
        if (coreProtectAPI != null && coreProtectAPI.isEnabled()) {
            coreProtectAPI.logPlacement(player.getName(), block.getLocation(), block.getType(), block.getBlockData());
        }
    }
}
