package me.mesona.mesona_sword.register;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static me.mesona.mesona_sword.MesonaSword.MODID;

@Deprecated
public class ModSound {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT.key(), MODID);
    public static final Supplier<SoundEvent> BELL_SOUND = SOUND_EVENTS.register(
            "bell", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "bell_sound"))
    );
}
