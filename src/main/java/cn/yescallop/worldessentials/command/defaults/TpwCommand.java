package cn.yescallop.worldessentials.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.TranslationContainer;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.worldessentials.WorldEssentials;
import cn.yescallop.worldessentials.command.CommandBase;

public class TpwCommand extends CommandBase {

    public TpwCommand(WorldEssentials plugin) {
        super("tpw", plugin);
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(new TranslationContainer("commands.generic.usage", this.usageMessage));
            return false;
        }
        CommandSender target = sender;
        String levelStr;
        if (args.length > 1) {
            if (!sender.hasPermission("worldessentials.command.tpw.other")) {
                sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.permission"));
                return true;
            }
            target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(TextFormat.RED + lang.translateString("commands.tpw.player.notFound", args[1]));
                return true;
            }
            levelStr = args[0];
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(TextFormat.RED + lang.translateString("commands.generic.onlyPlayer"));
                return true;
            }
            levelStr = args[0];
        }
        Level level = plugin.getServer().getLevelByName(levelStr);
        if (level == null) {
            sender.sendMessage(TextFormat.RED + lang.translateString("commands.generic.level.notFound", levelStr));
            return true;
        }
        Player player = (Player) target;
        plugin.setPlayerInfos(player);
        player.teleport(plugin.getPlayerSpawn(player, level));
        int gamemode = plugin.getPlayerGamemode(player, level);
        player.setGamemode(gamemode);
        player.getInventory().setContents(plugin.getPlayerInventoryContents(player, gamemode, level));
        return true;
    }
}
