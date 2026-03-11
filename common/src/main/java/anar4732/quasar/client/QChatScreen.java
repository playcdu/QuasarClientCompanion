package anar4732.quasar.client;

import anar4732.quasar.api.QCChatChannel;
import anar4732.quasar.api.QCompanionNetworkManager;
import com.google.gson.JsonObject;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QChatScreen extends ChatScreen {
	private final QChatComponent chat = (QChatComponent) Minecraft.getInstance().gui.getChat();
	private final List<QButton> contextMenuButtons = new ArrayList<>();
	
	public QChatScreen(String initial) {
		super(initial);
	}
	
	@Override
	protected void init() {
		super.init();
		int x = 3;
		int y = this.height - chat.getHeight() - 12;
		int i = 0;
		QButton everythingButton;
		Minecraft mc = this.minecraft;
		this.addRenderableWidget(everythingButton = new QButton(x, this.height - chat.getHeight() - 12, 10, 10, Component.literal("§8*"), button -> {
			QCompanionMod.selectedChannel = "*";
			mc.setScreen(new QChatScreen(""));
		}).setSelected(QCompanionMod.selectedChannel.equals("*")));
		everythingButton.setTooltip(Tooltip.create(Component.literal("All Messages")));
		x += 11;
		for (QCChatChannel c : QCompanionMod.CHAT_CHANNELS) {
			boolean isCollapsed = QCompanionMod.CONFIG.collapsedTabs.contains(c.displayName);
			int width = mc.gui.getFont().width(c.displayName) + 10;
			if (isCollapsed) {
				width = 10;
			}
			if (x + width + 1 > chat.getWidth()) {
				x = 3;
				y -= 11;
			}
			int color = QCompanionMod.COLORS.get(i % QCompanionMod.COLORS.size());
			boolean isSelected = QCompanionMod.selectedChannel.equals(c.displayName);
			if (isSelected) {
				c.unseenMessages = false;
			}
			QButton button;
			this.addRenderableWidget(button = new QButton(x, y, width, 10, Component.literal(isCollapsed ? c.displayName.substring(0, 1) : c.displayName).withStyle(style -> style.withColor(color)), b -> {
				QCompanionMod.selectedChannel = c.displayName;
				mc.setScreen(new QChatScreen(""));
			}).setSelected(isSelected).setRedMarker(c.unseenMessages));
			if (isCollapsed) {
				button.setTooltip(Tooltip.create(Component.literal(c.displayName)));
			}
			x += width + 1;
			i++;
		}
		QButton plusButton;
		this.addRenderableWidget(plusButton = new QButton(x, y, 10, 10, Component.literal("§c+"), button -> {
			mc.setScreen(new QChatScreen("/q setChannel party "));
		}));
		plusButton.setTooltip(Tooltip.create(Component.literal("Create New Party")));
	}
	
	@Override
	public boolean handleChatInput(String input, boolean addToRecentChat) {
		input = this.normalizeChatMessage(input);
		if (!input.isEmpty()) {
			if (addToRecentChat) {
				this.minecraft.gui.getChat().addRecentChat(input);
			}
			
			if (input.startsWith("/")) {
				this.minecraft.player.connection.sendCommand(input.substring(1));
			} else if (QCompanionMod.selectedChannel.equals("*")) {
				this.minecraft.player.connection.sendChat(input);
			} else {
				this.minecraft.player.connection.sendCommand(QCompanionMod.getSelectedChannel().sendCommand.formatted(input).substring(1));
			}
		}
		return true;
	}
	
	@Override
	public @Nullable GuiEventListener getFocused() {
		return this.input; // Recent commands fix. Needed bc of the buttons on the screen
	}
	
	@Override
	protected void changeFocus(ComponentPath path) {
		this.input.setFocused(true); // prevent focus loss when moving thought history
	}
	
	private void openContextMenu(double mouseX, double mouseY, Map<String, Runnable> buttons) {
		for (GuiEventListener e : contextMenuButtons)
			this.removeWidget(e);
		contextMenuButtons.clear();
		if (buttons.isEmpty())
			return;
		
		int y = (int) mouseY;
		if (y + (11 * buttons.size()) > this.height) {
			y = this.height - (11 * buttons.size());
			if (y < 0) {
				y = 0;
			}
		}
		for (Map.Entry<String, Runnable> e : buttons.entrySet()) {
			QButton contextMenuButton;
			this.addRenderableWidget(contextMenuButton = new QButton((int) mouseX, y, 60, 11, Component.literal(e.getKey()), b -> {
				e.getValue().run();
			}).setContextMenuButton(true));
			contextMenuButtons.add(contextMenuButton);
			y += 11;
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// Context Menu
		boolean contextButtonClicked = false;
		if (button == 0) {
			for (GuiEventListener e : contextMenuButtons) {
				if (e.mouseClicked(mouseX, mouseY, button)) {
					this.setFocused(e);
					contextButtonClicked = true;
				}
			}
		}
		
		for (GuiEventListener e : contextMenuButtons)
			this.removeWidget(e);
		contextMenuButtons.clear();
		if (contextButtonClicked)
			return true;
		
		if (button == 1) {
			Map<String, Runnable> contextButtons = new HashMap<>();
			
			// User Name RC
			Style style = chat.getClickedComponentStyleAt(mouseX, mouseY);
			if (style != null) {
				ClickEvent clickEvent = style.getClickEvent();
				if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.SUGGEST_COMMAND && clickEvent.getValue().startsWith("/tell")) {
					String username = clickEvent.getValue().substring(6).split(" ")[0];
					
					contextButtons.put("PM", () -> {
						this.minecraft.setScreen(new QChatScreen("/tell " + username + " "));
					});
					
					PlayerInfo pi = this.minecraft.player.connection.getListedOnlinePlayers().stream().filter(i -> i.getProfile().getName().equals(username)).findAny().orElse(null);
					if (pi != null) {
						boolean userIgnored = QCompanionMod.IGNORED_PLAYERS.contains(pi.getProfile().getId());
						contextButtons.put(userIgnored ? "Unignore" : "Ignore", () -> {
							this.minecraft.player.connection.sendCommand("/q " + (userIgnored ? "unignore" : "ignore") + " " + username);
							this.minecraft.setScreen(null);
						});
					}
					
					if (QCompanionMod.isAdmin) {
						contextButtons.put("§4TP", () -> {
							this.minecraft.player.connection.sendCommand("tp " + username);
							this.minecraft.setScreen(null);
						});
						
						contextButtons.put("§4TP Here", () -> {
							this.minecraft.player.connection.sendCommand("tp " + username + " @s");
							this.minecraft.setScreen(null);
						});
						
						contextButtons.put("§eKick", () -> {
							this.minecraft.setScreen(new QChatScreen("/kick " + username + " "));
						});
						
						contextButtons.put("§cBan", () -> {
							this.minecraft.setScreen(new QChatScreen("/q ban " + username + " "));
						});
						
						if (pi != null) {
							contextButtons.put("§dView in Argus", () -> {
								Util.getPlatform().openUri("https://spy.playcdu.co/player/" + pi.getProfile().getId());
							});
						}
					}
					
					openContextMenu(mouseX, mouseY, contextButtons);
					return true;
				}
			}
			
			// Button RC
			for (GuiEventListener e : new ArrayList<>(this.children())) {
				if (e.isMouseOver(mouseX, mouseY) && e instanceof QButton qb) {
					QCChatChannel c = QCompanionMod.CHAT_CHANNELS.stream().filter(c2 -> c2.displayName.equals(qb.getMessage().getString()) || (qb.getTooltip() != null && c2.displayName.equals(qb.getTooltip().message.getString()))).findAny().orElse(null);
					if (c != null) {
						if (c.canClosed) {
							contextButtons.put("§cClose", () -> {
								QCompanionMod.CHAT_CHANNELS.remove(c);
								JsonObject o = QCompanionNetworkManager.createObject("closeChannel");
								o.addProperty("channel", c.displayName);
								QCompanionNetworkManager.sendMessage(o);
								if (QCompanionMod.selectedChannel.equals(c.displayName)) {
									QCompanionMod.selectedChannel = "*";
								}
								this.minecraft.setScreen(new QChatScreen(""));
							});
						}
						
						boolean isCollapsed = QCompanionMod.CONFIG.collapsedTabs.contains(c.displayName);
						contextButtons.put(isCollapsed ? "Expand" : "Collapse", () -> {
							if (isCollapsed) {
								QCompanionMod.CONFIG.collapsedTabs.remove(c.displayName);
							} else {
								QCompanionMod.CONFIG.collapsedTabs.add(c.displayName);
							}
							QCompanionMod.CONFIG.write();
							this.minecraft.setScreen(new QChatScreen(""));
						});
					}
					
					openContextMenu(mouseX, mouseY, contextButtons);
				}
			}
			
			return true;
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
}