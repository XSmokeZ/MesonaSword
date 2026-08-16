package me.mesona.mesona_sword.register;

import com.mojang.serialization.Codec;
import me.mesona.mesona_sword.MesonaSword;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponent {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MesonaSword.MODID);

    // 注册剑模式数据组件（整数枚举）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SWORD_MODE =
            COMPONENTS.register("sword_mode", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    // 注册快速耐久损耗数据组件（布尔值）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> QUICK_DAMAGE =
            COMPONENTS.register("quick_damage", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());
}