package anar4732.quasar.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;

public class CDUOnlineServerEntry extends ServerSelectionList.OnlineServerEntry {
    private static final int COLOUR_GOLD        = 0xFFFFD700;
    private static final int COLOUR_GOLD_SHADOW = 0xFF5C4A00;
    private static final int COLOUR_BG_TINT     = 0x18FFD700;

    private final Minecraft mc = Minecraft.getInstance();
	
    public CDUOnlineServerEntry(ServerSelectionList list, JoinMultiplayerScreen screen, ServerData serverData) {
        list.super(screen, serverData);
    }

    @Override
    public void render(GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
        g.fill(left, top, left + width, top + height, COLOUR_BG_TINT);
        super.render(g, index, top, left, width, height, mouseX, mouseY, hovering, partialTick);

        String name = getServerData().name;
        int nameX = left + 32 + 3;
        int nameY = top + 1;
        g.drawString(mc.font, name, nameX + 1, nameY + 1, COLOUR_GOLD_SHADOW, false); // shadow
        g.drawString(mc.font, name, nameX, nameY, COLOUR_GOLD, false); // gold
    }
}