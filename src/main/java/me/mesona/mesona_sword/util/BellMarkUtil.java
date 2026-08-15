package me.mesona.mesona_sword.util;

import me.mesona.mesona_sword.MesonaSword;
import me.mesona.mesona_sword.network.BellMarkSyncPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class BellMarkUtil {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MesonaSword.MODID);

    public static final Supplier<AttachmentType<UUID>> BELL_MARK = ATTACHMENT_TYPES.register(
            "bell_mark",
            () -> AttachmentType.builder(() -> UUID.randomUUID())
                    .serialize(UUIDUtil.CODEC)
                    .copyOnDeath()
                    .build()
    );

    // 全局索引：玩家UUID -> 该玩家标记的所有实体网络ID集合
    private static final Map<UUID, Set<Integer>> PLAYER_MARK_INDEX = new ConcurrentHashMap<>();

    public static void setMark(LivingEntity entity, ServerPlayer player) {
        UUID playerId = player.getUUID();

        // 如果实体已经被其他玩家标记，先移除旧索引
        if (entity.hasData(BELL_MARK)) {
            UUID oldPlayer = entity.getData(BELL_MARK);
            if (!oldPlayer.equals(playerId)) {
                Set<Integer> oldSet = PLAYER_MARK_INDEX.get(oldPlayer);
                if (oldSet != null) {
                    oldSet.remove(entity.getId());
                    if (oldSet.isEmpty()) {
                        PLAYER_MARK_INDEX.remove(oldPlayer);
                    }
                }
            }
        }

        entity.setData(BELL_MARK, playerId);
        Set<Integer> set = PLAYER_MARK_INDEX.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet());

        // 只有新添加时才发送同步包
        if (set.add(entity.getId())) {
            player.connection.send(new BellMarkSyncPacket(BellMarkSyncPacket.Action.ADD, List.of(entity.getId())));
        }
    }

    public static void removeMark(LivingEntity entity, ServerPlayer player) {
        if (!entity.hasData(BELL_MARK)) return;

        UUID playerId = entity.getData(BELL_MARK);
        entity.removeData(BELL_MARK);

        Set<Integer> set = PLAYER_MARK_INDEX.get(playerId);
        if (set != null) {
            set.remove(entity.getId());
            if (set.isEmpty()) {
                PLAYER_MARK_INDEX.remove(playerId);
            }
        }

        player.connection.send(new BellMarkSyncPacket(BellMarkSyncPacket.Action.REMOVE, List.of(entity.getId())));
    }

    /**
     * 获取指定玩家标记的所有存活实体
     */
    public static List<LivingEntity> getMarkedEntities(Level level, UUID playerId) {
        Set<Integer> ids = PLAYER_MARK_INDEX.get(playerId);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<LivingEntity> result = new ArrayList<>();
        for (int entityId : ids) {
            Entity entity = level.getEntity(entityId);
            if (entity instanceof LivingEntity living && entity.isAlive()) {
                result.add(living);
            }
        }
        return result;
    }

    /**
     * 清理已失效的索引条目并同步到客户端
     */
    public static void cleanupIndex(Level level, ServerPlayer player) {
        UUID playerId = player.getUUID();
        Set<Integer> set = PLAYER_MARK_INDEX.get(playerId);
        if (set == null) return;

        List<Integer> removed = new ArrayList<>();
        set.removeIf(id -> {
            Entity entity = level.getEntity(id);
            boolean dead = entity == null || !entity.isAlive() || !entity.hasData(BELL_MARK);
            if (dead) removed.add(id);
            return dead;
        });

        if (!removed.isEmpty()) {
            player.connection.send(new BellMarkSyncPacket(BellMarkSyncPacket.Action.REMOVE, removed));
        }

        if (set.isEmpty()) {
            PLAYER_MARK_INDEX.remove(playerId);
        }
    }
}