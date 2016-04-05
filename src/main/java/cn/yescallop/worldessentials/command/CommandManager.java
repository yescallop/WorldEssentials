package cn.yescallop.worldessentials.command;

import cn.nukkit.command.CommandMap;

import cn.yescallop.worldessentials.WorldEssentials;
import cn.yescallop.worldessentials.command.defaults.SetWorldGamemodeCommand;
import cn.yescallop.worldessentials.command.defaults.SpawnCommand;
import cn.yescallop.worldessentials.command.defaults.TpwCommand;

public class CommandManager {

    public static void registerAll(WorldEssentials plugin) {
        CommandMap map = plugin.getServer().getCommandMap();
        map.register("worldessentials", new SetWorldGamemodeCommand(plugin));
        map.register("worldessentials", new SpawnCommand(plugin));
        map.register("worldessentials", new TpwCommand(plugin));
    }
}