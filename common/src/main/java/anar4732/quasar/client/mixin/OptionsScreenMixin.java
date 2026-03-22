package anar4732.quasar.client.mixin;

import anar4732.quasar.client.gui.QSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
	protected OptionsScreenMixin() {
		super(Component.empty());
	}
	
	@Inject(at = @At("TAIL"), method = "init")
	private void addQuasarButton(CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		addRenderableWidget(Button.builder(Component.literal("§6Q"), btn -> mc.setScreen(new QSettingsScreen(this, mc.options)))
		                          .bounds(width / 2 + 104, height - 26, 20, 20)
		                          .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Quasar Settings")))
		                          .build());
	}
}