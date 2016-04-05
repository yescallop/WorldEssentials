package cn.yescallop.worldessentials.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.TranslationContainer;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;

import cn.yescallop.worldessentials.WorldEssentials;
import cn.yescallop.worldessentials.command.CommandBase;

public class SpawnCommand extends CommandBase {

    public SpawnCommand(WorldEssentials plugin) {
        super("spawn", plugin);
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + lang.translateString("commands.generic.onlyPlayer"));
            return true;
        }
        if (!this.testPermission(sender)) {
            return true;
        }
        Player player = (Player) sender;
        Level level = player.getLevel();
        player.teleport(level.getSafeSpawn());
        return true;
    }
}
