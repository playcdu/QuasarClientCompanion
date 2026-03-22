package anar4732.quasar.client.gui;

import anar4732.quasar.client.QCompanionMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CDUHeader extends ServerSelectionList.Entry {
    private static final Component CDU_LABEL = Component.literal(QCompanionMod.CONFIG.modPackConfig.serversTitle);
    private static final int COLOUR_GOLD_TOP    = 0xFFFFD700;
    private static final int COLOUR_GOLD_BOTTOM = 0xFFB8860B;
    private static final int COLOUR_SHADOW      = 0xFF5C4A00;

    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
        int centreX = this.mc.screen != null ? this.mc.screen.width / 2 : this.mc.getWindow().getGuiScaledWidth() / 2;
        int textWidth = this.mc.font.width(CDU_LABEL);
        int textY = top + (height - 9) / 2;
        int lineY = top + height / 2;
        int padding = 6;
        int lineLeft  = centreX - textWidth / 2 - padding - 40;
        int lineRight = centreX + textWidth / 2 + padding + 40;

        guiGraphics.fill(lineLeft, lineY - 1, centreX - textWidth / 2 - padding, lineY, COLOUR_GOLD_BOTTOM);
        guiGraphics.fill(centreX + textWidth / 2 + padding, lineY - 1, lineRight, lineY, COLOUR_GOLD_BOTTOM);
        guiGraphics.drawString(this.mc.font, CDU_LABEL, centreX - textWidth / 2 + 1, textY + 1, COLOUR_SHADOW, false);
        guiGraphics.drawString(this.mc.font, CDU_LABEL, centreX - textWidth / 2, textY, COLOUR_GOLD_TOP, false);
        guiGraphics.fill(centreX - textWidth / 2, textY + 10, centreX + textWidth / 2, textY + 11, COLOUR_GOLD_BOTTOM);
    }

    @Override
    @NotNull
    public Component getNarration() {
        return CDU_LABEL;
    }
}