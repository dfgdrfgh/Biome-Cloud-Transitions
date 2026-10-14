package dev.biomecloudtransitions.mixin;

import dev.biomecloudtransitions.CloudAttributes;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.biomecloudtransitions.CloudBlend;
import dev.biomecloudtransitions.CloudWeather;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/** Sodium replaces vanilla cloud drawing, so its own color uniform needs the tint. */
@Pseudo
@Mixin(targets="net.caffeinemc.mods.sodium.client.render.immediate.CloudRenderer", remap=false)
public abstract class SodiumCloudRendererMixin {
    @Unique private CloudBlend.Color wwoo$frameColor;

    @Inject(method="render", at=@At("HEAD"), remap=false)
    private void wwoo$sampleCloudColor(CallbackInfo ci) {
        CloudWeather.beginFrame();
        wwoo$frameColor = CloudAttributes.color();
    }

    @ModifyArgs(method="render", at=@At(value="INVOKE",
        target="Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V",
        ordinal=0, remap=false), remap=false)
    private void wwoo$tintCloudColor(Args args) {
        var color = wwoo$frameColor;
        args.set(0, (float)args.get(0) * (float)color.red());
        args.set(1, (float)args.get(1) * (float)color.green());
        args.set(2, (float)args.get(2) * (float)color.blue());
        // Keep Sodium's opacity and its later white shader-state reset unchanged.
    }

    @ModifyExpressionValue(method="render",at=@At(value="INVOKE",
        target="Lnet/minecraft/class_638;method_23785(F)Lnet/minecraft/class_243;",remap=false),remap=false)
    private Vec3 biomeclouds$nativeWeatherColor(Vec3 original) {
        return CloudWeather.original(original);
    }
}

