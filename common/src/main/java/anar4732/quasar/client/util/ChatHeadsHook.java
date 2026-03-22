package anar4732.quasar.client.util;

import anar4732.quasar.api.QCPlayerMessage;
import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.HeadData;
import dzwdz.chat_heads.config.RenderPosition;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.FormattedText;

public class ChatHeadsHook {
	public static void renderChatHead(GuiGraphics guiGraphics, GuiMessage.Line guiMessage, int y, float opacity) {
		HeadData headData = ChatHeads.getHeadData(guiMessage);
		if (headData != HeadData.EMPTY) {
			if (ChatHeads.CONFIG.renderPosition() == RenderPosition.BEFORE_LINE) {
				ChatHeads.renderChatHead(guiGraphics, 0, y, headData.playerInfo(), opacity);
			} else {
				ChatHeads.guiGraphics = guiGraphics;
				ChatHeads.renderHeadData = headData;
				ChatHeads.renderHeadOpacity = opacity;
			}
		}
	}
	
	public static void forgetRenderData() {
		ChatHeads.guiGraphics = null;
		ChatHeads.renderHeadData = HeadData.EMPTY;
	}
	
	public static void handleAddedChannelMessage(QCPlayerMessage message) {
		ChatHeads.handleAddedMessage(message.message, null, null);
		message.playerInfo = ChatHeads.lastSenderData.playerInfo();
		message.codePointIndex = ChatHeads.lastSenderData.codePointIndex();
		ChatHeads.lastSenderData = HeadData.EMPTY;
	}
	
	public static void declareNextOwner(QCPlayerMessage message) {
		ChatHeads.setLineData(new HeadData((PlayerInfo) message.playerInfo, message.codePointIndex >= 0 || message.playerInfo == null ? message.codePointIndex : findHeadPosition(message.message, (PlayerInfo) message.playerInfo)));
	}
	
	private static int findHeadPosition(FormattedText component, PlayerInfo playerInfo) {
		if (ChatHeads.CONFIG.renderPosition() == RenderPosition.BEFORE_NAME) {
			ClientPacketListener connection = Minecraft.getInstance().getConnection();
			if (connection != null) {
				ChatHeads.PlayerInfoCache playerInfoCache = new ChatHeads.PlayerInfoCache(connection);
				playerInfoCache.add(playerInfo);
				HeadData foundHeadData = ChatHeads.scanForPlayerName(component.getString(), playerInfoCache);
				
				if (foundHeadData.hasHeadPosition()) {
					return foundHeadData.codePointIndex();
				}
			}
		}
		return -1;
	}
}