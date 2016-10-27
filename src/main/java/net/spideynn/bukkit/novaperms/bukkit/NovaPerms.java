package net.spideynn.bukkit.novaperms.bukkit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.SneakyThrows;
import net.spideynn.bukkit.novaperms.Group;
import net.spideynn.bukkit.novaperms.MongoConnection;
import net.spideynn.bukkit.novaperms.bukkit.command.Command;
import net.spideynn.bukkit.novaperms.bukkit.command.ReloadCommand;
import net.spideynn.bukkit.novaperms.bukkit.vault.VaultMongoBridge;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static net.spideynn.bukkit.novaperms.NovaPermsAPI.getUUID;

public class NovaPerms extends JavaPlugin {

    @Getter
    private static NovaPerms instance;

    @Getter
    private static Configuration settings;

    public static final Map<UUID, PermissionAttachment> attachments = Maps.newLinkedHashMap();

    private static Field field;

    @Override
    public void onEnable() {
        try {
            field = getCraftHumanEntityClass().getDeclaredField("perm");
            field.setAccessible(true);
        } catch (ReflectiveOperationException e1) {
            System.out.println("[NovaPerms] Couldn't find CraftHumanEntityClass! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;

        if (!Files.exists(new File(getDataFolder(), "config.yml").toPath())) {
            saveDefaultConfig();
        }

        settings = Configuration.load(this);
        MongoConnection.load(settings.getMongoHost(), settings.getMongoPort(), settings.getDefaultGroup(), settings.getMongoUsername(), settings.getMongoPassword(), false, settings.isUseAuthentication());

        if (settings.isUseVault()) {
            Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
            if (vault != null) {
                new VaultMongoBridge(vault, this);
            }
        }

        getServer().getPluginManager().registerEvents(new NovaListener(), this);
        registerCommand(new ReloadCommand());

        System.out.println("[NovaPerms] Enabled version: " + getDescription().getVersion());

    }

    public static void generateAttachment(Player p) {

        if (p == null || !p.isOnline()) {
            return;
        }

        try {
            field.set(p, new CustomPermissibleBase(p));
        } catch (ReflectiveOperationException | NullPointerException e) {
            e.printStackTrace();
        }

        PermissionAttachment attachment = p.addAttachment(instance);

        String name = MongoConnection.getGroup(getUUID(p.getName()));

        if (name == null) {
            name = getSettings().getDefaultGroup();
        }

        Group group = Group.getGroup(name);
        Preconditions.checkNotNull(group);

        group.getPermissions().forEach(permission -> {
            if (permission.startsWith("-")) {
                attachment.setPermission(permission.substring(1), false);
            } else {
                attachment.setPermission(permission, true);
            }
        });

        attachments.put(getUUID(p.getName()), attachment);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends HumanEntity> getCraftHumanEntityClass() throws ReflectiveOperationException {
        return (Class<? extends HumanEntity>) Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftHumanEntity");
    }

    public static void unlogAttachment(Player p) {
        PermissionAttachment attachment = attachments.remove(getUUID(p.getName()));

        if (attachment == null) {
            System.err.println("[NovaPerms]" + p.getName() + "'s attachment is null?");
            return;
        }

        p.removeAttachment(attachment);
    }

    /*
        doing some experimenting below :D
     */
    private void registerCommand(CommandExecutor executor) {
        Preconditions.checkNotNull(executor);
        Command command = executor.getClass().getAnnotation(Command.class);
        Preconditions.checkNotNull(command, "Couldn't register " + executor.getClass().getSimpleName() + "! @Command not found.");

        CommandMap map = getCommandMap();
        PluginCommand cmd = newCommand(command.name(), this);
        cmd.setDescription(command.description());
        cmd.setExecutor(executor);
        cmd.setTabCompleter(newInstance(command.tabCompleter()));

        if (!command.permission().equals("")) {
            cmd.setPermission(command.permission());
            if (!command.permissionMessage().equals("")) {
                cmd.setPermissionMessage(command.permissionMessage());
            }
        }

        if (!command.usage().equals("")) {
            cmd.setUsage(command.usage());
        }

        if (command.aliases().length != 0) {
            cmd.setAliases(Arrays.asList(command.aliases()));
        }

        map.register(getClass().getSimpleName().toLowerCase(), cmd);
        System.out.printf("[NovaPerms] Registered command %s", command.name());
    }

    @SneakyThrows
    private PluginCommand newCommand(String name, Plugin owner) {
        Constructor<? extends org.bukkit.command.Command> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);
        return (PluginCommand) constructor.newInstance(name, owner);
    }

    @SneakyThrows
    public <T> T newInstance(Class<? extends T> clazz) {
        Constructor<T> constructor = (Constructor<T>) clazz.getConstructors()[0];

        if (constructor.getParameterTypes().length == 0) {
            return constructor.newInstance();
        } else {
            return constructor.newInstance(this);
        }
    }

    @SneakyThrows
    private CommandMap getCommandMap() {
        Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        f.setAccessible(true);
        return (CommandMap) f.get(Bukkit.getServer());
    }


}
