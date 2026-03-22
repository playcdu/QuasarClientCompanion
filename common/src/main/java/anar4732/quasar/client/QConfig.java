package anar4732.quasar.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import dev.architectury.platform.Platform;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static anar4732.quasar.client.QCompanionMod.MOD_ID;

public class QConfig implements Serializable {
	public static final Path PATH = Platform.getConfigFolder().resolve(MOD_ID);
	private static final File FILE = PATH.resolve("config.json").toFile();
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.enableComplexMapKeySerialization()
			.setLenient()
			.create();
	
	public static QConfig read() {
		if (!PATH.toFile().exists() && !PATH.toFile().mkdirs())
			throw new RuntimeException("Failed to create " + PATH.toAbsolutePath());
		try {
			if (!FILE.exists() && !FILE.createNewFile())
				throw new RuntimeException("Failed to create " + FILE.getAbsolutePath());
			QConfig config = GSON.fromJson(new FileReader(FILE, StandardCharsets.UTF_8), QConfig.class);
			config = config == null ? new QConfig() : config;
			return config;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public QConfig write() {
		try {
			FileWriter writer = new FileWriter(FILE, StandardCharsets.UTF_8);
			JsonWriter jsonWriter = new JsonWriter(writer);
			jsonWriter.setIndent("\t");
			GSON.toJson(this, QConfig.class, jsonWriter);
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return this;
	}
	
	// ==================================================================================================== //
	
	public ModPackConfig modPackConfig = new ModPackConfig();
	public ChatConfig chatConfig = new ChatConfig();
	public TabListConfig tabListConfig = new TabListConfig();
	public Set<String> collapsedTabs = new HashSet<>();
	
	// ==================================================================================================== //
	
	public static class ModPackConfig implements Serializable {
		public String clusterName = "MAYV";
		public String serversTitle = "Craft Down Under";
		public String welcomeTitle = "Welcome to §6§lCraft Down Under§r§f!";
		public String welcomeMessage = "Use code §6§l4732§r§f to get 15% off purchases on CDU store!";
		public Set<ServerEntry> servers = new HashSet<>(
				Set.of(
						new ServerEntry("Example Server 1", "play.mayv.com"),
						new ServerEntry("Example Server 2", "play.craftdownunder.com")
				)
		);
	}
	
	public static class ServerEntry implements Serializable {
		public String name;
		public String address;
		
		public ServerEntry() {
			// For GSON
		}
		
		public ServerEntry(String name, String address) {
			this.name = name;
			this.address = address;
		}
	}
	
	public static class ChatConfig implements Serializable {
		public boolean useQChat = true;
	}
	
	public static class TabListConfig implements Serializable {
		public boolean showNetworkPlayers = true;
	}
}