package anar4732.quasar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class QButton extends Button {
	private final Minecraft mc = Minecraft.getInstance();
	private boolean selected = false;
	private boolean redMarker = false;
	private boolean isContextMenuButton = false;
	
	protected QButton(int x, int y, int width, int height, Component message, OnPress onPress) {
		super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
	}
	
	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (isContextMenuButton) {
			guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
		}
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), this.getBackgroundOpacity() << 24);
		if (selected) {
			guiGraphics.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), 0xffffffff);
		}
		if (redMarker) {
			guiGraphics.fill(getX() + getWidth() - 4, getY() + 1, getX() + getWidth() - 2, getY() + 3, 0x88ff0000);
		}
		if (isContextMenuButton) {
			int borderColor = isHovered() ? 0xffffffff : 0x88ffffff;
			guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + 1, borderColor);
			guiGraphics.fill(getX(), getY(), getX() + 1, getY() + getHeight(), borderColor);
			guiGraphics.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), borderColor);
			guiGraphics.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), borderColor);
		}
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = this.active ? 16777215 : 10526880;
		this.renderString(guiGraphics, mc.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
	}
	
	private int getBackgroundOpacity() {
		if (isContextMenuButton) {
			return 255;
		} else if (this.isHovered()) {
			return 125;
		} else {
			return 150;
		}
	}
	
	public QButton setSelected(boolean selected) {
		this.selected = selected;
		return this;
	}
	
	public QButton setRedMarker(boolean redMarker) {
		this.redMarker = redMarker;
		return this;
	}
	
	public QButton setContextMenuButton(boolean contextMenuButton) {
		this.isContextMenuButton = contextMenuButton;
		return this;
	}
}