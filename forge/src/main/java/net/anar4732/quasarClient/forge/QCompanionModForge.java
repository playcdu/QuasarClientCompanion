package net.anar4732.quasarClient.forge;

import dev.architectury.platform.forge.EventBuses;
import net.anar4732.quasarClient.QCompanionMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QCompanionMod.MOD_ID)
public final class QCompanionModForge {
    public QCompanionModForge() {
        EventBuses.registerModEventBus(QCompanionMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        QCompanionMod.init();
    }
}