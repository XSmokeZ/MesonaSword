package me.mesona.mesona_sword.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class ListHelper {
    private static final List<LivingEntity> entityList = new ArrayList<>();
    private static final List<LivingEntity> entityRemoveList = new ArrayList<>();
    public static void addEntityToList(LivingEntity livingEntity) {
        if (livingEntity != null)
            entityList.add(livingEntity);
    }
    public static void removeEntityToList(LivingEntity livingEntity) {
        if (livingEntity != null)
            entityList.remove(livingEntity);
    }
    public static boolean entityContainsInList(LivingEntity livingEntity) {
        if (livingEntity == null)
            return false;
        return entityList.contains(livingEntity);
    }
    public static void addEntityToRemoveList(LivingEntity livingEntity) {
        if (livingEntity != null)
            entityRemoveList.add(livingEntity);
    }
    public static void removeEntityToRemoveList(LivingEntity livingEntity) {
        if (livingEntity != null)
            entityRemoveList.remove(livingEntity);
    }
    public static boolean entityContainsInRemoveList(LivingEntity livingEntity) {
        if (livingEntity == null)
            return false;
        return entityRemoveList.contains(livingEntity);
    }
}
