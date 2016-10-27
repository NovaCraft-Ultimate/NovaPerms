package net.spideynn.bukkit.novaperms.bukkit;

import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.ServerOperator;

public class CustomPermissibleBase extends PermissibleBase {

    public CustomPermissibleBase(ServerOperator opable) {
        super(opable);
    }

    @Override
    public boolean hasPermission(String inName) {

        if (NovaPerms.getSettings().getPermissionNode().equals("none")) {
            return super.hasPermission(inName);
        }

        return super.hasPermission(NovaPerms.getSettings().getPermissionNode()) || super.hasPermission(inName);
    }

    @Override
    public boolean hasPermission(Permission perm) {

        if (NovaPerms.getSettings().getPermissionNode().equals("none")) {
            return super.hasPermission(perm);
        }

        return super.hasPermission(NovaPerms.getSettings().getPermissionNode()) || super.hasPermission(perm);
    }

}
