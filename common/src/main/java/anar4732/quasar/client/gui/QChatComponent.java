package anar4732.quasar.client.gui;

import anar4732.quasar.api.QCPlayerMessage;
import anar4732.quasar.client.QCompanionMod;
import anar4732.quasar.client.util.ChatHeadsHook;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class QChatComponent extends ChatComponent {
	private int messagesOfSelectedChannelSize = 0;
	
	public QChatComponent() {
		super(Minecraft.getInstance());
	}
	
	private List<GuiMessage.Line> messagesOfSelectedChannel() {
		String channel = QCompanionMod.selectedChannel;
		if (channel.equals("*") || !QCompanionMod.shouldUseQChat()) {
			return this.trimmedMessages;
		}
		
		List<GuiMessage.Line> lines = new ArrayList<>();
		List<QCPlayerMessage> messages = QCompanionMod.MESSAGES_BY_CHANNELS.get(channel);
		
		if (messages == null) {
			return lines;
		}
		
		int w = Mth.floor(this.getWidth() / this.getScale());
		
		for (QCPlayerMessage m : messages) {
			if (Platform.isModLoaded("chat_heads") && m.playerInfo != null) {
				ChatHeadsHook.declareNextOwner(m);
			}
			
			List<FormattedCharSequence> l = ComponentRenderUtils.wrapComponents(m.message, w, this.minecraft.font);
			for (int j = 0; j < l.size(); j++) {
				FormattedCharSequence formattedCharSequence = l.get(j);
				lines.add(0, new GuiMessage.Line(0, formattedCharSequence, null, j == l.size() - 1));
			}
		}
		
		return lines;
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY) {
		if (!QCompanionMod.shouldUseQChat()) {
			super.render(guiGraphics, tickCount, mouseX, mouseY);
			return;
		}
		
		if (!this.isChatHidden()) {
			List<GuiMessage.Line> messages = messagesOfSelectedChannel();
			this.messagesOfSelectedChannelSize = messages.size();
			int messagesSize = messages.size();
			boolean focused = this.isChatFocused();
			if (messagesSize > 0 || focused) {
				float scale = (float)this.getScale();
				int maxWidth = Mth.ceil(this.getWidth() / scale);
				guiGraphics.pose().pushPose();
				guiGraphics.pose().scale(scale, scale, 1.0F);
				guiGraphics.pose().translate(4.0F, 0.0F, 0.0F);
				int maxHeight = Mth.floor((guiGraphics.guiHeight() - 40) / scale);
				double opacity = this.minecraft.options.chatOpacity().get() * 0.9F + 0.1F;
				double backgroundOpacity = this.minecraft.options.textBackgroundOpacity().get();
				double lineSpacing = this.minecraft.options.chatLineSpacing().get();
				int lineHeight = this.getLineHeight();
				int shownHeight = messagesSize * lineHeight;
				int p = (int)Math.round(-8.0 * (lineSpacing + 1.0) + 4.0 * lineSpacing);
				int lines = 0;
				
				for (int r = 0; r < this.getHeight() / (this.getLineHeight() * getScale()); r++) {
					int s = r + this.chatScrollbarPos;
					int u = (int)(255.0 * opacity);
					int v = (int)(255.0 * (focused ? backgroundOpacity : backgroundOpacity * 0.5));
					lines++;
					if (u > 3) {
						int y = maxHeight - r * lineHeight;
						int i = y + p;
						guiGraphics.pose().pushPose();
						guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
						guiGraphics.fill(-4, y - lineHeight, maxWidth + 4 + 4, y, v << 24);
						if (messages.size() > s) {
							GuiMessage.Line line = messages.get(s);
							guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
							if (Platform.isModLoaded("chat_heads")) {
								ChatHeadsHook.renderChatHead(guiGraphics, line, i, (float) opacity);
							}
							guiGraphics.drawString(this.minecraft.font, line.content(), 0, i, 0xffffff + (u << 24));
							if (Platform.isModLoaded("chat_heads")) {
								ChatHeadsHook.forgetRenderData();
							}
						}
						guiGraphics.pose().popPose();
					}
				}
				
				if (messages.isEmpty()) {
					float emptyTextScale = 1.75F;
					guiGraphics.pose().pushPose();
					guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
					guiGraphics.pose().scale(emptyTextScale, emptyTextScale, 1.0F);
					guiGraphics.drawString(this.minecraft.font, Component.literal("§8<EMPTY>"), maxWidth / 5, (int) ((maxHeight - 20) / emptyTextScale), 0xffffff);
					guiGraphics.pose().popPose();
				}
				
				// SCROLL BAR
				if (focused && !messages.isEmpty()) {
					int viewportHeight = lines * lineHeight;
					
					if (shownHeight > viewportHeight) {
						int trackPadding = 2;
						int trackTop = maxHeight - viewportHeight + trackPadding;
						int trackBottom = maxHeight - trackPadding;
						int trackHeight = Math.max(1, trackBottom - trackTop);
						int maxScrollLines = Math.max(1, messagesSize - lines);
						int thumbHeight = Mth.clamp(trackHeight * viewportHeight / shownHeight, 8, trackHeight);
						int thumbTravel = trackHeight - thumbHeight;
						int thumbOffset = this.chatScrollbarPos * thumbTravel / maxScrollLines;
						int thumbBottom = Mth.clamp(trackBottom - thumbOffset, trackTop + thumbHeight, trackBottom);
						int thumbTop = thumbBottom - thumbHeight;
						int color = this.newMessageSinceScroll ? 0xFFCC3333 : 0xFFFFFFFF;
						int barLeft = maxWidth - 3;
						int barRight = maxWidth - 1;
				
						guiGraphics.pose().pushPose();
						guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
						guiGraphics.fill(barLeft, trackTop, barRight, trackBottom, 0x55888888);
						guiGraphics.fill(barLeft, thumbTop, barRight, thumbBottom, color);
						guiGraphics.pose().popPose();
					}
				}
				
				guiGraphics.pose().popPose();
			}
		}
	}
	
	@Override
	public int getHeight() {
		return getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused().get() : this.minecraft.options.chatHeightUnfocused().get()) - 40;
	}
	
	@Override
	public GuiMessageTag getMessageTagAt(double mouseX, double mouseY) {
		return null;
	}
	
	@Override
	public void clearMessages(boolean clearSentMsgHistory) {
		super.clearMessages(clearSentMsgHistory);
		QCompanionMod.MESSAGES_BY_CHANNELS.clear();
	}
	
	@Nullable
	@Override
	public Style getClickedComponentStyleAt(double mouseX, double mouseY) {
		double d = this.screenToChatX(mouseX);
		double e = this.screenToChatY(mouseY);
		int i = this.getMessageLineIndexAt(d, e);
		List<GuiMessage.Line> messages = messagesOfSelectedChannel();
		if (i >= 0 && i < messages.size()) {
			GuiMessage.Line line = messages.get(i);
			return this.minecraft.font.getSplitter().componentStyleAtWidth(line.content(), Mth.floor(d));
		} else {
			return null;
		}
	}
	
	@Override
	public void scrollChat(int posInc) {
		this.chatScrollbarPos += posInc;
		int i = messagesOfSelectedChannelSize;
		if (this.chatScrollbarPos > i - this.getLinesPerPage()) {
			this.chatScrollbarPos = i - this.getLinesPerPage();
		}
		
		if (this.chatScrollbarPos <= 0) {
			this.chatScrollbarPos = 0;
			this.newMessageSinceScroll = false;
		}
	}
}