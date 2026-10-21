package dev.biomecloudtransitions.mixin;

import dev.biomecloudtransitions.CloudWeather;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Snapshot native time/weather RGB at construction, before any return hook can recolor it. */
@Mixin(ClientLevel.class)
public abstract class CloudWeatherMixin {
    @ModifyExpressionValue(method="getCloudColor",at=@At(value="NEW",
        target="(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 biomeclouds$rememberWeather(Vec3 original) {
        CloudWeather.remember((ClientLevel)(Object)this,original);
        return original;
    }
}
