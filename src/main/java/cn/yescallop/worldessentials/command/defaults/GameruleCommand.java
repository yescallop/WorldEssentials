package cn.yescallop.worldessentials.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.worldessentials.WorldEssentials;
import cn.yescallop.worldessentials.command.CommandBase;

public class GameruleCommand extends CommandBase {

    public GameruleCommand(WorldEssentials plugin) {
        super("gamerule", plugin);
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("keepInventory");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + lang.translateString("commands.generic.onlyPlayer"));
            return true;
        }
        Level level = ((Player) sender).getLevel();
        if (args.length == 1) {
            switch (args[0]) {
                case "keepInventory":
                    sender.sendMessage(args[0] + " = " + plugin.getLevelBooleanGamerule(level, args[0]));
                    break;
                default:
                    sender.sendMessage(lang.translateString("commands.gamerule.unknownGamerule", args[0]));
                    return true;
            }
            return true;
        }
        Object value;
        switch (args[0]) {
            case "keepInventory":
                switch (args[0]) {
                    case "true":
                        value = true;
                        break;
                    case "false":
                        value = false;
                        break;
                    default:
                        sender.sendMessage(lang.translateString("commands.gamerule.notBoolean", args[0]));
                        return true;
                }
                plugin.setLevelGamerule(level, args[0], value);
                break;
            default:
                sender.sendMessage(lang.translateString("commands.gamerule.unknownGamerule", args[0]));
                return true;
        }
        sender.sendMessage(lang.translateString("commands.gamerule.success", new String[]{args[0], value.toString()}));
        return true;
    }
}