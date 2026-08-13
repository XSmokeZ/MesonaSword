package me.mesona.mesona_sword;

import me.mesona.mesona_sword.register.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(MesonaSword.MODID)
public class MesonaSword {
    public static final String MODID = "mesona_sword";

    public MesonaSword(IEventBus modEventBus) {
        Registry.init(modEventBus);
    }
}