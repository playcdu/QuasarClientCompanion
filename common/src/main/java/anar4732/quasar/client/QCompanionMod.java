package anar4732.quasar.client;

import anar4732.quasar.api.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public final class QCompanionMod {
    public static final String MOD_ID = "quasar_client";
	public static final Logger LOGGER = LogManager.getLogger("Quasar Companion");
	public static final String VERSION = Platform.getMod(MOD_ID).getVersion();
	public static final QConfig CONFIG = QConfig.read().write();
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Component.class, new ComponentSerializer())
			.create();
	
	// CHAT
	public static final List<Integer> COLORS = List.of(0x28A745, 0x0DCAF0, 0x5865F2, 0xFF6B35, 0x9D4EDD, 0xE63946, 0xF77F00, 0x6C757D, 0xDC3545, 0x8B5CF6);
	public static final Set<QCChatChannel> CHAT_CHANNELS = new HashSet<>();
	public static final Map<String, List<QCPlayerMessage>> MESSAGES_BY_CHANNELS = new HashMap<>();
	public static final Set<UUID> IGNORED_PLAYERS = new HashSet<>();
	public static String selectedChannel = "*";
	public static boolean isAdmin = false;
	
	public static void onLogin() {
		CHAT_CHANNELS.clear();
		selectedChannel = "*";
		isAdmin = false;
		QCompanionAPI.registryAccess = Minecraft.getInstance().getConnection().registryAccess();
		
		if (QExpectPlatform.shouldSendPacket()) {
			JsonObject o = QCompanionNetworkManager.createObject("init");
			o.addProperty("version", VERSION);
			QCompanionNetworkManager.sendMessage(o);
		}
	}
	
	public static QCChatChannel getSelectedChannel() {
		return CHAT_CHANNELS.stream().filter(c -> c.displayName.equals(selectedChannel)).findFirst().orElse(null);
	}
	
	public static boolean shouldUseQChat() {
		return !QCompanionMod.CHAT_CHANNELS.isEmpty();
	}
	
	private static void newChannel(Collection<QCChatChannel> list) {
		List<QCChatChannel> sorted = new ArrayList<>(CHAT_CHANNELS);
		sorted.removeIf(c -> list.stream().anyMatch(nc -> nc.displayName.equals(c.displayName)));
		sorted.addAll(list);
		sorted.sort(Comparator.comparing(c -> c.displayName));
		CHAT_CHANNELS.clear();
		CHAT_CHANNELS.addAll(sorted);
	}

    public static void init() {
		LOGGER.info("Initializing Quasar Companion v{}", VERSION);
	    QCompanionNetworkManager.init();
		QCompanionNetworkManager.register("info", (player, o) -> {
			isAdmin = o.has("isAdmin") && o.get("isAdmin").getAsBoolean();
        });
		QCompanionNetworkManager.register("ignoredPlayers", (player, o) -> {
			IGNORED_PLAYERS.clear();
			IGNORED_PLAYERS.addAll(GSON.fromJson(o.get("players"), new TypeToken<Set<UUID>>(){}.getType()));
       });
		QCompanionNetworkManager.register("channels", (player, o) -> {
			newChannel(GSON.fromJson(o.get("channels"), new TypeToken<Set<QCChatChannel>>(){}.getType()));
		});
		QCompanionNetworkManager.register("channel", (player, o) -> {
			QCChatChannel channel = GSON.fromJson(o.get("channel"), QCChatChannel.class);
			newChannel(List.of(channel));
			selectedChannel = channel.displayName;
		});
		QCompanionNetworkManager.register("chat", (player, o) -> {
			QCPlayerMessage message = GSON.fromJson(o.get("message"), QCPlayerMessage.class);
			List<QCPlayerMessage> list = MESSAGES_BY_CHANNELS.computeIfAbsent(message.channel.displayName, k -> new ArrayList<>());
			list.add(message);
			
			while (list.size() > 100) {
				list.remove(list.size() - 1);
			}
			
			QCChatChannel channel = CHAT_CHANNELS.stream().filter(c -> c.displayName.equals(message.channel.displayName)).findAny().orElse(null);
			if (channel == null) {
				newChannel(List.of(message.channel));
				channel = message.channel;
			}
			channel.unseenMessages = true;
			
			if (!selectedChannel.equals("*") && Minecraft.getInstance().gui.getChat() instanceof QChatComponent qchat) {
				if (qchat.isChatFocused() && qchat.chatScrollbarPos > 0) {
					qchat.newMessageSinceScroll = true;
					qchat.scrollChat(1);
				}
			}
		});
    }
}