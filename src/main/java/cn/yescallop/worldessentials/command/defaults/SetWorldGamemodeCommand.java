package cn.yescallop.worldessentials.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.worldessentials.WorldEssentials;
import cn.yescallop.worldessentials.command.CommandBase;

public class SetWorldGamemodeCommand extends CommandBase {

    public SetWorldGamemodeCommand(WorldEssentials plugin) {
        super("setworldgamemode", plugin);
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) return false;
        String gamemodeStr;
        Level level;
        if (args.length > 1) {
            level = plugin.getServer().getLevelByName(args[0]);
            if (level == null) {
                sender.sendMessage(TextFormat.RED + lang.translateString("commands.generic.level.notFound", args[0]));
                return true;
            }
            gamemodeStr = args[1];
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(TextFormat.RED + lang.translateString("commands.generic.onlyInGame"));
                return true;
            }
            level = ((Player) sender).getLevel();
            gamemodeStr = args[0];
        }
        int gamemode = Server.getGamemodeFromString(gamemodeStr);
        if (gamemode == -1) {
            sender.sendMessage(TextFormat.RED + lang.translateString("commands.setworldgamemode.unknownGamemode", gamemodeStr));
            return true;
        }
        plugin.setLevelGamemode(level, gamemode);
        for (Player levelPlayer : level.getPlayers().values()) {
            levelPlayer.sendMessage(lang.translateString("commands.setworldgamemode.success", Server.getGamemodeString(gamemode)));
        }
        sender.sendMessage(lang.translateString("commands.setworldgamemode.success.others", new String[]{level.getName(), Server.getGamemodeString(gamemode)}));
        return true;
    }
}