package net.spideynn.bukkit.novaperms.bukkit.command;

import net.spideynn.bukkit.novaperms.NovaPermsAPI;
import net.spideynn.bukkit.novaperms.Group;
import net.spideynn.bukkit.novaperms.bukkit.NovaPerms;
import net.spideynn.bukkit.novaperms.bukkit.events.PermissionUpdatedEvent;
import net.spideynn.bukkit.novaperms.bukkit.events.PlayerPermissionUpdatedEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.spideynn.bukkit.novaperms.NovaPermsAPI.getUUID;

@net.spideynn.bukkit.novaperms.bukkit.command.Command(name = "npreload", description = "reloads all groups or player permissions", aliases = {"novapermsreload", "npr"})
public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("novaperms.reload")) {
            sender.sendMessage("§cYou are not allowed to use this command.");
            return true;
        }

        if (NovaPerms.getSettings().isNeedOp() && !sender.isOp()) {
            sender.sendMessage("§cYou need OP to execute this command.");
            return true;
        }

        if (args.length > 0) {
            Player p = Bukkit.getPlayer(args[0]);
            if (p == null || !p.isOnline()) {
                sender.sendMessage("§cCan't find player: " + args[0]);
                return true;
            }
            NovaPerms.unlogAttachment(p);
            NovaPerms.generateAttachment(p);
            NovaPermsAPI.clear(getUUID(p.getName()));
            Bukkit.getPluginManager().callEvent(new PlayerPermissionUpdatedEvent(p));
            sender.sendMessage("§aPlayer " + p.getName() + " has been reloaded.");
            return true;
        }

        Bukkit.getOnlinePlayers().forEach(NovaPerms::unlogAttachment);

        sender.sendMessage("§a" + Group.getGroups().size() + " groups loaded.");
        NovaPermsAPI.clear();
        Bukkit.getPluginManager().callEvent(new PermissionUpdatedEvent(true));

        Bukkit.getOnlinePlayers().forEach(NovaPerms::generateAttachment);
        sender.sendMessage("§a" + NovaPerms.attachments.size() + " players registered.");
        return false;
    }

}
