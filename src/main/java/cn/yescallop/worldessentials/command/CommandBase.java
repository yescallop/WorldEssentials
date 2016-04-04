package cn.yescallop.worldessentials.command;

import cn.nukkit.command.Command;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.yescallop.worldessentials.lang.BaseLang;
import cn.yescallop.worldessentials.WorldEssentials;

public abstract class CommandBase extends Command implements PluginIdentifiableCommand {

    protected WorldEssentials plugin;
    protected BaseLang lang;

    public CommandBase(String name, WorldEssentials plugin) {
        super(name);
        this.lang = plugin.getLanguage();
        this.description = lang.translateString("commands." + name + ".description");
        String usageMessage = lang.translateString("commands." + name + ".usage");
        this.usageMessage = usageMessage == null ? "/" + name : usageMessage;
        this.setPermission("worldessentials.command." + name);
        this.plugin = plugin;
    }

    @Override
    public WorldEssentials getPlugin() {
        return plugin;
    }
}