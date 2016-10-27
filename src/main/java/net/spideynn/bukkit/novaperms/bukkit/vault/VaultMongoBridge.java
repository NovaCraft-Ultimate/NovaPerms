package net.spideynn.bukkit.novaperms.bukkit.vault;

import net.spideynn.bukkit.novaperms.MongoConnection;
import net.spideynn.bukkit.novaperms.MongoConnection.Result;
import net.spideynn.bukkit.novaperms.NovaPermsAPI;
import net.spideynn.bukkit.novaperms.Group;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import java.util.Set;
import java.util.stream.Collectors;

public class VaultMongoBridge extends Permission {

    public VaultMongoBridge(Plugin vault, Plugin perms) {
        System.out.printf("[%s][Permission] %s hooked.", vault.getDescription().getName(), "NovaPerms");
        Bukkit.getServicesManager().register(Permission.class, this, perms, ServicePriority.Highest);
    }

    @Override
    public String getName() {
        return "NovaPerms";
    }


    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean hasSuperPermsCompat() {
        return true;
    }

    @Override
    public boolean playerHas(String s, String s1, String s2) {
        Player p = Bukkit.getPlayer(s1);
        return !(p == null || !p.isOnline()) && (p.hasPermission(s2) || NovaPermsAPI.getPermissionsOfPlayer(NovaPermsAPI.getUUID(s1)).contains(s2));
    }

    @Override
    public boolean playerAdd(String s, String s1, String s2) {
        return false; //we're not providing this feature
    }

    @Override
    public boolean playerRemove(String s, String s1, String s2) {
        return false; //we're not providing this feature
    }

    @Override
    public boolean groupHas(String s, String s1, String s2) {
        Group group = Group.getGroup(s1);
        return group.getPermissions().contains(s2);
    }

    @Override
    public boolean groupAdd(String s, String s1, String s2) {
        return MongoConnection.addPermission(s1, s2) == Result.RESULT_SUCCESS;
    }

    @Override
    public boolean groupRemove(String s, String s1, String s2) {
        return MongoConnection.removePermission(s1, s2) == Result.RESULT_SUCCESS;
    }

    @Override
    public boolean playerInGroup(String s, String s1, String s2) {
        return getPrimaryGroup(s, s1).equals(s2);
    }

    @Override
    public boolean playerAddGroup(String s, String s1, String s2) {
        return false; // Not providing.
    }

    @Override
    public boolean playerRemoveGroup(String world, String player, String group) {
        return false; // Not providing.
    }

    @Override
    public String[] getPlayerGroups(String world, String player) {
        return new String[]{getPrimaryGroup(world, player)};
    }

    @Override
    public String getPrimaryGroup(String world, String player) {
        return NovaPermsAPI.getGroup(NovaPermsAPI.getUUID(player)).getName();
    }

    @Override
    public String[] getGroups() {
        Set<String> groups = Group.getGroups().stream().map(Group::getName).collect(Collectors.toSet());
        return groups.toArray(new String[0]);
    }

    @Override
    public boolean hasGroupSupport() {
        return true;
    }
}
