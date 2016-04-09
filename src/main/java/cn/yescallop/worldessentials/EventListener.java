package cn.yescallop.worldessentials;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerGameModeChangeEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.level.Location;
import cn.nukkit.utils.Config;

public class EventListener implements Listener {

    WorldEssentials plugin;

    public EventListener(WorldEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        Player player = event.getPlayer();
        if (from.level.equals(to.level)) return;
        plugin.setPlayerInfos(player);
        int gamemode = plugin.getPlayerGamemode(player, to.level);
        player.setGamemode(gamemode);
        player.getInventory().setContents(plugin.getPlayerInventoryContents(player, gamemode, to.level));
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