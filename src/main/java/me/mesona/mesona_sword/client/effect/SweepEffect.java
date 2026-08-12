package me.mesona.mesona_sword.client.effect;

import net.minecraft.world.phys.Vec3;

public class SweepEffect {
    public final Vec3 pos;
    public final float yaw;
    public final float scale;
    public int age;
    public final int maxAge;

    public SweepEffect(Vec3 pos, float yaw, float scale, int maxAge) {
        this.pos = pos;
        this.yaw = yaw;
        this.scale = scale;
        this.age = 0;
        this.maxAge = maxAge;
    }

    public boolean tick() {
        age++;
        return age >= maxAge;
    }

    public int getFrameIndex(int totalFrames) {
        return Math.min(age * totalFrames / Math.max(maxAge, 1), totalFrames - 1);
    }
}