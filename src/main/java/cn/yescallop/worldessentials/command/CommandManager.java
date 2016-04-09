package cn.yescallop.worldessentials.command;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandMap;

import cn.yescallop.worldessentials.WorldEssentials;
import cn.yescallop.worldessentials.command.defaults.SetWorldGamemodeCommand;
import cn.yescallop.worldessentials.command.defaults.SpawnCommand;
import cn.yescallop.worldessentials.command.defaults.TpwCommand;

import java.util.ArrayList;

public class CommandManager {

    public static void registerAll(WorldEssentials plugin) {
        CommandMap map = plugin.getServer().getCommandMap();
        ArrayList<Command> commands = new ArrayList<Command>(){
            {
                add(new SetWorldGamemodeCommand(plugin));
                add(new SpawnCommand(plugin));
                add(new TpwCommand(plugin));
            }
        };
        map.registerAll("worldessentials", commands);
    }
}