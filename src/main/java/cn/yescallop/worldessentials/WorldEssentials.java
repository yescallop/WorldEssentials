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
import cn.nukkit.Player;
import cn.yescallop.worldessentials.command.CommandManager;
import cn.yescallop.worldessentials.lang.BaseLang;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

public class WorldEssentials extends PluginBase implements Listener {
    
    private File worldsFolder;
    private BaseLang lang;
    
    @Override
    public void onEnable() {
        this.getDataFolder().mkdirs();
        worldsFolder = new File(this.getDataFolder(), "worlds");
        worldsFolder.mkdirs();
        this.getServer().getPluginManager().registerEvents(this, this);
        lang = new BaseLang(this.getServer().getLanguage().getLang());
        CommandManager.registerAll(this);
        this.getLogger().info("WorldEssentials 加载成功！");
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

    public BaseLang getLanguage() {
        return lang;
    }

    public Location getPlayerSpawn(Player player, Level level) {
        LinkedHashMap<String, Object> infos = (LinkedHashMap<String, Object>) getPlayerConfig(player, level).getAll();
        Vector3 pos;
        if (infos.size() == 0) {
            pos = level.getSafeSpawn();
        } else {
            pos = new Vector3((double) infos.get("x"), (double) infos.get("y"), (double) infos.get("z"));
        }
        return Location.fromObject(pos, level);
    }

    public void setLevelGamemode(Level level, int gamemode) {
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

    public int getPlayerGamemode(Player player, Level level) {
        return getPlayerConfig(player, level).getInt("gamemode", getLevelGamemode(level));
    }

    public LinkedHashMap<Integer, Item> getPlayerInventoryContents(Player player, int gamemode, Level level) {
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

    public int getLevelGamemode(Level level) {
        return getLevelConfig(level).getInt("gamemode", this.getServer().getDefaultGamemode());
    }

    public void setPlayerInfos(Player player) {
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

    public LinkedHashMap<String, LinkedHashMap<Integer, Object>> getPlayerInventories(Player player, Level level) {
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

    public Config getLevelConfig(Level level) {
        return new Config(new File(worldsFolder, level.getName() + ".yml"), Config.YAML);
    }

    public Config getPlayerConfig(Player player, Level level) {
        return new Config(new File(getLevelFolder(level), player.getName().toLowerCase() + ".yml"), Config.YAML);
    }

    public Config[] getLevelPlayerConfigs(Level level) {
        File[] files = getLevelFolder(level).listFiles();
        ArrayList<Config> configs = new ArrayList<Config>();
        for (File file : files) {
            configs.add(new Config(file, Config.YAML));
        }
        return (Config[]) configs.toArray(new Config[]{});
    }

    public File getLevelFolder(Level level) {
        File folder = new File(worldsFolder, level.getName());
        folder.mkdirs();
        return folder;
    }
}