package cn.yescallop.worldessentials;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerGameModeChangeEvent;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;

public class EventListener implements Listener {

    WorldEssentials plugin;

    public EventListener(WorldEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityLevelChange(EntityLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;
        Level origin = event.getOrigin();
        Level target = event.getTarget();
        if (origin.equals(target)) return;
        plugin.setPlayerInfos(player);
        int gamemode = plugin.getPlayerGamemode(player, target);
        player.setGamemode(gamemode);
        player.getInventory().setContents(plugin.getPlayerInventoryContents(player, gamemode, target));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        Config playerConfig = plugin.getPlayerConfig(player, player.getLevel());
        playerConfig.set("inventories", plugin.getPlayerInventories(player, player.getLevel()));
        playerConfig.save();
        int gamemode = event.getNewGamemode();
        player.getInventory().setContents(plugin.getPlayerInventoryContents(player, gamemode, player.getLevel()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.getLevelBooleanGamerule(event.getEntity().getLevel(), "keepInventory")) {
            event.setKeepInventory(true);
            event.setKeepExperience(true);
        }
    }
}