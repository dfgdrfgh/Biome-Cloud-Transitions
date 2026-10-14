package dev.biomecloudtransitions;

import net.minecraft.client.Minecraft;
import java.lang.ref.WeakReference;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

/** Retain native time/weather shading independently of later biome cloud-color hooks. */
public final class CloudWeather {
    private static WeakReference<ClientLevel> level=new WeakReference<>(null);
    private static Vec3 nativeColor;
    private CloudWeather() {}
    public static void beginFrame() {level.clear();nativeColor=null;}
    public static void remember(ClientLevel source,Vec3 color) {level=new WeakReference<>(source);nativeColor=color;}
    public static Vec3 original(Vec3 fallback) {
        var client=Minecraft.getInstance();
        return client!=null&&client.level==level.get()&&nativeColor!=null?nativeColor:fallback;
    }
}
