package me.mesona.mesona_sword.register;

import me.mesona.mesona_sword.MesonaSword;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItem {
    // 注册器
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MesonaSword.MODID);
    public static final DeferredItem<GrassSwordItem> MESONA_SWORD = ITEMS.register("mesona_sword", GrassSwordItem::new);
}
