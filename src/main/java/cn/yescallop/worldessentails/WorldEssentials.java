package cn.yescallop.worldessentials;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.TranslationContainer;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.Player;
import cn.nukkit.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WorldEssentials extends PluginBase implements Listener {
    
    private File worldsFolder;
    private File playersFolder;
    
    @Override
    public void onEnable() {
        this.getDataFolder().mkdirs();
        worldsFolder = new File(this.getDataFolder(), "worlds");
        worldsFolder.mkdirs();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info("WorldEssentials 加载成功！");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Level level;
        CommandSender target;
        Player player;
        switch(cmd.getName()) {
            case "setworldgamemode":
                if (args.length == 0) return false;
                String gamemodeStr;
                if (args.length > 1) {
                    level = this.getServer().getLevelByName(args[0]);
                    if (level == null) {
                        sender.sendMessage(TextFormat.RED + "世界 '" + args[0] + "' 不存在");
                        return true;
                    }
                    gamemodeStr = args[1];
                } else {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(TextFormat.RED + "你只能在游戏中执行此命令");
                        return true;
                    }
                    level = ((Player) sender).getLevel();
                    gamemodeStr = args[0];
                }
                int gamemode = Server.getGamemodeFromString(gamemodeStr);
                if (gamemode == -1) {
                    sender.sendMessage(TextFormat.RED + "未知的游戏模式 '" + gamemodeStr + "'");
                    return true;
                }
                setLevelGamemode(level, gamemode);
                for (Player levelPlayer : level.getPlayers().values()) {
                    levelPlayer.setGamemode(gamemode);
                    levelPlayer.sendMessage("此世界的游戏模式已被设置为 " + Server.getGamemodeString(gamemode));
                }
                sender.sendMessage("世界 '" + level.getName() + "' 的游戏模式已被设置为 " + Server.getGamemodeString(gamemode));
                break;
            case "tpw":
                if (args.length == 0) return false;
                target = sender;
                String levelStr;
                if (args.length > 1) {
                    if (!sender.hasPermission("worldessentials.command.tpw.other")) {
                        sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.permission"));
                        return true;
                    }
                    target = this.getServer().getPlayer(args[0]);
                    if (target == null) {
                        sender.sendMessage(TextFormat.RED + "玩家 '" + args[0] + "' 不存在");
                        return true;
                    }
                    levelStr = args[1];
                } else {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(TextFormat.RED + "你只能在游戏中执行此命令");
                        return true;
                    }
                    levelStr = args[0];
                }
                level = this.getServer().getLevelByName(levelStr);
                if (level == null) {
                    sender.sendMessage(TextFormat.RED + "世界 '" + levelStr + "' 不存在");
                    return true;
                }
                player = (Player) target;
                setPlayerLevelInfos(player);
                player.teleport(getPlayerLevelSpawn(player, level));
                player.setGamemode(getPlayerLevelGamemode(player, level));
                player.getInventory().setContents(getPlayerLevelInventoryContents(player, level));
                break;
            case "spawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(TextFormat.RED + "你只能在游戏中执行此命令");
                    return true;
                }
                player = (Player) sender;
                level = player.getLevel();
                player.teleport(level.getSafeSpawn());
                break;
        }
        return true;
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (!from.getLevel().equals(to.getLevel())) setPlayerLevelInfos(event.getPlayer());
    }
    
    private Location getPlayerLevelSpawn(Player player, Level level) {
        LinkedHashMap<String, Object> infos = (LinkedHashMap<String, Object>) getPlayerLevelConfig(player, level).getAll();
        Vector3 pos;
        if (infos.size() == 0) {
            pos = level.getSafeSpawn();
        } else {
            pos = new Vector3((double) infos.get("x"), (double) infos.get("y"), (double) infos.get("z"));
        }
        return Location.fromObject(pos, level);
    }
    
    private void setLevelGamemode(Level level, int gamemode) {
        Config levelConfig = getLevelConfig(level);
        levelConfig.set("gamemode", gamemode);
        levelConfig.save();
        for (Config playerConfig : getPlayerLevelConfigs(level)) {
            playerConfig.remove("gamemode");
            playerConfig.save();
        }
    }
    
    private int getPlayerLevelGamemode(Player player, Level level) {
        return getPlayerLevelConfig(player, level).getInt("gamemode", getLevelGamemode(level));
    }
    
    private Map<Integer, Item> getPlayerLevelInventoryContents(Player player, Level level) {
        HashMap<Integer, ArrayList<Object>> inventory  = (HashMap<Integer, ArrayList<Object>>) getPlayerLevelConfig(player, level).get("inventory", new HashMap<Integer, ArrayList<Object>>());
        HashMap<Integer, Item> contents = new HashMap<Integer, Item>();
        for (Map.Entry entry : inventory.entrySet()) {
            Object[] item = (Object[]) ((ArrayList<Object>) entry.getValue()).toArray(new Object[]{});
            contents.put((Integer) entry.getKey(), new Item((int) item[0], (int) item[1], (int) item[2], (String) item[3]));
        }
        return contents;
    }
    
    private int getLevelGamemode(Level level) {
        return getLevelConfig(level).getInt("gamemode", this.getServer().getDefaultGamemode());
    }
    
    private void setPlayerLevelInfos(Player player) {
        HashMap<Integer, Object[]> inventory = new HashMap<Integer, Object[]>();
        for (Map.Entry entry : player.getInventory().getContents().entrySet()) {
            Item item = (Item) entry.getValue();
            inventory.put((Integer) entry.getKey(), new Object[]{item.getId(), item.getDamage(), item.getCount(), item.getName()});
        }
        LinkedHashMap<String, Object> infos = new LinkedHashMap<String, Object>(){
            {
            put("x", player.x);
            put("y", player.y);
            put("z", player.z);
            put("inventory", inventory);
            put("gamemode", player.getGamemode());
            }
        };
        Config playerConfig = getPlayerLevelConfig(player, player.getLevel());
        playerConfig.setAll(infos);
        playerConfig.save();
    }
    
    private Config getLevelConfig(Level level) {
        return new Config(new File(worldsFolder, level.getName() + ".yml"), Config.YAML);
    }
    
    private Config getPlayerLevelConfig(Player player, Level level) {
        return new Config(new File(getLevelFolder(level), player.getName().toLowerCase() + ".yml"), Config.YAML);
    }
    
    private Config[] getPlayerLevelConfigs(Level level) {
        File[] files = getLevelFolder(level).listFiles();
        ArrayList<Config> configs = new ArrayList<Config>();
        for (File file : files) {
            configs.add(new Config(file, Config.YAML));
        }
        return (Config[]) configs.toArray(new Config[]{});
    }
    
    private File getLevelFolder(Level level) {
        File folder = new File(worldsFolder, level.getName());
        folder.mkdirs();
        return folder;
    }
}