package cn.yescallop.worldessentials;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.player.PlayerGameModeChangeEvent;
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
import java.util.Map;
import java.util.LinkedHashMap;

public class WorldEssentials extends PluginBase implements Listener {
    
    private File worldsFolder;
    
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
        int gamemode;
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
                gamemode = Server.getGamemodeFromString(gamemodeStr);
                if (gamemode == -1) {
                    sender.sendMessage(TextFormat.RED + "未知的游戏模式 '" + gamemodeStr + "'");
                    return true;
                }
                setLevelGamemode(level, gamemode);
                for (Player levelPlayer : level.getPlayers().values()) {
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
                    target = this.getServer().getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(TextFormat.RED + "玩家 '" + args[1] + "' 不存在");
                        return true;
                    }
                    levelStr = args[0];
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
                setPlayerInfos(player);
                player.teleport(getPlayerSpawn(player, level));
                gamemode = getPlayerGamemode(player, level);
                player.setGamemode(gamemode);
                player.getInventory().setContents(getPlayerInventoryContents(player, gamemode, level));
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
        if (!event.isCancelled()) {
            Location from = event.getFrom();
            Location to = event.getTo();
            Player player = event.getPlayer();
            if (!from.level.equals(to.level)) setPlayerInfos(player);
            int gamemode = getPlayerGamemode(player, to.level);
            player.setGamemode(gamemode);
            player.getInventory().setContents(getPlayerInventoryContents(player, gamemode, to.level));
        }
    }
    
    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            Config playerConfig = getPlayerConfig(player, player.getLevel());
            playerConfig.set("inventories", getPlayerInventories(player, player.getLevel()));
            playerConfig.save();
            int gamemode = event.getNewGamemode();
            player.getInventory().setContents(getPlayerInventoryContents(player, gamemode, player.getLevel()));
        }
    }
    
    private Location getPlayerSpawn(Player player, Level level) {
        LinkedHashMap<String, Object> infos = (LinkedHashMap<String, Object>) getPlayerConfig(player, level).getAll();
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
        for (Config playerConfig : getLevelPlayerConfigs(level)) {
            playerConfig.set("gamemode", gamemode);
            playerConfig.save();
        }
        for (Player levelPlayer : level.getPlayers().values()) {
            levelPlayer.setGamemode(gamemode);
        }
    }
    
    private int getPlayerGamemode(Player player, Level level) {
        return getPlayerConfig(player, level).getInt("gamemode", getLevelGamemode(level));
    }
    
    private LinkedHashMap<Integer, Item> getPlayerInventoryContents(Player player, int gamemode, Level level) {
        LinkedHashMap<String, LinkedHashMap<Integer, ArrayList<Integer>>> inventories = (LinkedHashMap<String, LinkedHashMap<Integer, ArrayList<Integer>>>) getPlayerConfig(player, level).get("inventories");
        if (inventories == null) return new LinkedHashMap<>();
        LinkedHashMap<Integer, ArrayList<Integer>> inventory;
        switch (gamemode) {
            case 0:
                inventory = (LinkedHashMap<Integer, ArrayList<Integer>>) inventories.get("survival");
                break;
            case 1:
                inventory = (LinkedHashMap<Integer, ArrayList<Integer>>) inventories.get("creative");
                break;
            default:
                return new LinkedHashMap<>();
        }
        if (inventory == null) return new LinkedHashMap<>();
        LinkedHashMap<Integer, Item> contents = new LinkedHashMap<Integer, Item>();
        for (Map.Entry entry : inventory.entrySet()) {
            Integer[] item = (Integer[]) ((ArrayList<Integer>) entry.getValue()).toArray(new Integer[]{});
            contents.put((Integer) entry.getKey(), Item.get((int) item[0], (int) item[1], (int) item[2]));
        }
        return contents;
    }
    
    private int getLevelGamemode(Level level) {
        return getLevelConfig(level).getInt("gamemode", this.getServer().getDefaultGamemode());
    }
    
    private void setPlayerInfos(Player player) {
        Config playerConfig = getPlayerConfig(player, player.getLevel());
        LinkedHashMap<String, Object> infos = new LinkedHashMap<String, Object>(){
            {
            put("x", player.x);
            put("y", player.y);
            put("z", player.z);
            put("inventories", getPlayerInventories(player, player.getLevel()));
            put("gamemode", player.getGamemode());
            }
        };
        playerConfig.setAll(infos);
        playerConfig.save();
    }
    
    private LinkedHashMap<String, LinkedHashMap<Integer, Object>> getPlayerInventories(Player player, Level level) {
        LinkedHashMap<Integer, Object> inventory = new LinkedHashMap<Integer, Object>();
        for (Map.Entry entry : player.getInventory().getContents().entrySet()) {
            Item item = (Item) entry.getValue();
            inventory.put((int) entry.getKey(), new int[]{item.getId(), item.getDamage(), item.getCount()});
        }
        Config playerConfig = getPlayerConfig(player, player.getLevel());
        LinkedHashMap<String, LinkedHashMap<Integer, Object>> inventories = (LinkedHashMap<String, LinkedHashMap<Integer, Object>>) playerConfig.get("inventories", new LinkedHashMap<String, LinkedHashMap<Integer, Object>>());
        switch (player.getGamemode()) {
            case 0:
                inventories.put("survival", inventory);
                break;
            case 1:
                inventories.put("creative", inventory);
                break;
        }
        return inventories;
    }
    
    private Config getLevelConfig(Level level) {
        return new Config(new File(worldsFolder, level.getName() + ".yml"), Config.YAML);
    }
    
    private Config getPlayerConfig(Player player, Level level) {
        return new Config(new File(getLevelFolder(level), player.getName().toLowerCase() + ".yml"), Config.YAML);
    }
    
    private Config[] getLevelPlayerConfigs(Level level) {
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