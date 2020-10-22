package net.dxtrus.staffchatplus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.dxtrus.staffchatplus.commands.CommandStaffChatPlus;
import net.dxtrus.staffchatplus.events.PlayerListener;

public class StaffChatPlus extends JavaPlugin {

	private static StaffChatPlus instance;

	private File messagesFile;
	private File usersFile;
	private FileConfiguration messagesConfig;
	private FileConfiguration usersConfig;

	private boolean channelPersistence = false;
	private Map<Player, String> playerChannels = new HashMap<>();
	private List<String> channels = new ArrayList<>();

	public void onEnable() {
		instance = this;

		if (!setupConfigs(false)) {
			getLogger().info("Error setting up configs. Disabling.");
			setEnabled(false);
			return;
		}

		registerCommands();
		registerEvents();
		
	}

	public void onDisable() {
		saveConfig();
	}

	private boolean setupConfigs(boolean isReload) {
		if (!isReload && getResource("config.yml") != null)
			saveDefaultConfig();
		else
			super.reloadConfig();

		messagesFile = new File(getDataFolder(), "messages.yml");

		try {
			if (!messagesFile.exists())
				messagesFile.createNewFile();

			messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
			InputStream defaultsStream = getResource("messages.yml");

			if (defaultsStream != null) {
				messagesConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultsStream)));
				messagesConfig.options().copyDefaults(true);
				messagesConfig.save(messagesFile);
			}

			channels.clear();
			channels.add("global");

			for (String channel : getConfig().getConfigurationSection("channels").getKeys(false)) {
				if (channels.contains(channel.toLowerCase())) {
					continue;
				}

				channels.add(channel.toLowerCase());
			}

			channelPersistence = getConfig().getBoolean("channel-persistence", true);

			if (channelPersistence) {
				if (isReload && usersConfig != null)
					usersConfig.save(usersFile);

				usersFile = new File(getDataFolder(), "users.yml");
				if (!usersFile.exists())
					usersFile.createNewFile();

				usersConfig = YamlConfiguration.loadConfiguration(usersFile);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	private void registerCommands() {
		getCommand("staffchatplus").setExecutor(new CommandStaffChatPlus());
	}

	private void registerEvents() {
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
	}

	@Override
	public void reloadConfig() {
		setupConfigs(true);
	}

	@Override
	public void saveConfig() {
		super.saveConfig();

		try {
			usersConfig.save(usersFile);
		} catch (IOException ex) {
			ex.printStackTrace();
			getLogger().severe("Error saving user persistence file.");
		}
	}

	public boolean isInStaffChannel(Player p) {
		return playerChannels.containsKey(p) && channels.contains(playerChannels.get(p));
	}

	public boolean channelPersistence() {
		return channelPersistence;
	}

	public boolean channelExists(String channel) {
		return channels.contains(channel.toLowerCase());
	}

	public void joinChannel(Player p, String channelName) {
		if (!channelExists(channelName))
			return;

		if (playerChannels.containsKey(p))
			playerChannels.replace(p, channelName.toLowerCase());
		else
			playerChannels.put(p, channelName.toLowerCase());

		if (channelPersistence)
			usersConfig.set(p.getUniqueId() + ".channel", channelName.toLowerCase());

	}

	public void leaveStaffChat(Player p) {
		if (!playerChannels.containsKey(p))
			return;

		playerChannels.remove(p);
		if (channelPersistence) {
			usersConfig.set(p.getUniqueId() + ".is-in-channel", false);
		}
	}

	public FileConfiguration getUsersConfig() {
		return usersConfig;
	}

	public static String tl(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public static String i18n(String msg) {
		return tl(i18nNoColor(msg));
	}

	public static String i18n(String msg, Object... replacements) {
		return tl(i18nNoColor(msg, replacements));
	}

	public static String i18nNoColor(String msg) {
		return instance.messagesConfig.getString(msg);
	}

	public static String i18nNoColor(String msg, Object... replacements) {
		String newMsg = i18nNoColor(msg);

		for (int i = 0; i < replacements.length; i++) {
			if (!newMsg.contains("{" + i + "}"))
				continue;

			newMsg = newMsg.replace("{" + i + "}", String.valueOf(replacements[i]));
		}

		return newMsg;
	}

	public static Map<Player, String> getPlayerChannels() {
		return Collections.unmodifiableMap(instance.playerChannels);
	}

	public static List<String> getChannels() {
		return Collections.unmodifiableList(instance.channels);
	}

	public static StaffChatPlus getInstance() {
		return instance;
	}
}
