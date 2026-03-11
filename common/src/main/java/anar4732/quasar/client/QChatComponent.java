package anar4732.quasar.client;

import anar4732.quasar.api.QCPlayerMessage;
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
		if (channel.equals("*")) {
			return this.trimmedMessages;
		}
		
		List<GuiMessage.Line> lines = new ArrayList<>();
		List<QCPlayerMessage> messages = QCompanionMod.MESSAGES_BY_CHANNELS.get(channel);
		
		if (messages == null) {
			return lines;
		}
		
		int w = Mth.floor(this.getWidth() / this.getScale());
		
		for (QCPlayerMessage m : messages) {
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
				
				for (int r = 0; r + this.chatScrollbarPos < Math.max(messages.size(), 100) && r < this.getLinesPerPage(); r++) {
					int s = r + this.chatScrollbarPos;
					int u = (int)(255.0 * opacity);
					int v = (int)(255.0 * (focused ? backgroundOpacity : 0.25));
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
							guiGraphics.drawString(this.minecraft.font, line.content(), 0, i, 0xffffff + (u << 24));
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
					int totalHeight = lines * lineHeight;
					int pos = this.chatScrollbarPos * totalHeight / messagesSize - maxHeight;
					int u = totalHeight * totalHeight / shownHeight;
					if (totalHeight < shownHeight) {
						int w = this.newMessageSinceScroll ? 0xcc3333 : 0xffffff;
						guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
						guiGraphics.fill(maxWidth + 1, totalHeight, maxWidth + 3, maxHeight - 5, 0x55888888);
						guiGraphics.fill(maxWidth + 1, -pos - 5, maxWidth + 3, -pos - u + 3, (w | 0xff << 24));
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
	public double getScale() {
		return 0.75;
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