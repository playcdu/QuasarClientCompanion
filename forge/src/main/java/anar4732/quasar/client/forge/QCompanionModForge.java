package anar4732.quasar.client.forge;

import anar4732.quasar.client.QCompanionMod;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QCompanionMod.MOD_ID)
public final class QCompanionModForge {
    public QCompanionModForge() {
        EventBuses.registerModEventBus(QCompanionMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        QCompanionMod.init();
    }
}