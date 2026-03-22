package anar4732.quasar.client.gui;

import anar4732.quasar.client.QCompanionMod;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class QSettingsScreen extends OptionsSubScreen {
    private static final int COLOUR_GOLD_DARK = 0xFFB8860B;

    private static final Component TITLE = Component.literal("Quasar Settings")
            .withStyle(s -> s.withColor(0xFFD700).withBold(true));

    public QSettingsScreen(Screen lastScreen, Options options) {
        super(lastScreen, options, TITLE);
    }

    @Override
    protected void addOptions() {
		int y = (int) (this.height * 0.25f);
		int x = (int) (this.width * 0.5f - 100);
		this.addRenderableWidget(new QButton(x, y, 200, 20, QCompanionMod.CONFIG.chatConfig.useQChat ? Component.literal("Use QChat: ").append(Component.literal("ON").withStyle(s -> s.withColor(0x55FF55).withBold(true))) : Component.literal("Use QChat: ").append(Component.literal("OFF").withStyle(s -> s.withColor(0xFF5555).withBold(true))), btn -> {
            QCompanionMod.CONFIG.chatConfig.useQChat = !QCompanionMod.CONFIG.chatConfig.useQChat;
            QCompanionMod.CONFIG.write();
            minecraft.setScreen(new QSettingsScreen(lastScreen, options));
		}));
		y += 24;
		QButton tabListNetworkPlayersBts;
	    this.addRenderableWidget(tabListNetworkPlayersBts = new QButton(x, y, 200, 20, QCompanionMod.CONFIG.tabListConfig.showNetworkPlayers ? Component.literal("Show Players from Network on Tab List: ").append(Component.literal("ON").withStyle(s -> s.withColor(0x55FF55).withBold(true))) : Component.literal("Show Players from Network on Tab List: ").append(Component.literal("OFF").withStyle(s -> s.withColor(0xFF5555).withBold(true))), btn -> {
		    QCompanionMod.CONFIG.tabListConfig.showNetworkPlayers = !QCompanionMod.CONFIG.tabListConfig.showNetworkPlayers;
		    QCompanionMod.CONFIG.write();
		    minecraft.setScreen(new QSettingsScreen(lastScreen, options));
	    }));
	    tabListNetworkPlayersBts.setTooltip(Tooltip.create(Component.literal("Requires relog to take effect").withStyle(s -> s.withColor(0xAAAAAA))));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int tw = font.width("Quasar Settings") + 30;
        int cx = width / 2;
        int lineY = 26;
        g.fill(cx - tw / 2, lineY, cx + tw / 2, lineY + 1, COLOUR_GOLD_DARK);
    }
}