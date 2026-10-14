package dev.biomecloudtransitions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Lifecycle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.CubicSampler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Traverse native camera/biome lookups with unmodified WWOO .6 hooks and the complete release cloud mod. */
public final class LegacyWwooCloudProbe implements PreLaunchEntrypoint {
    private static void require(boolean value,String message) {if(!value)throw new AssertionError(message);}
    private static void near(double actual,double expected,String message) {
        require(Math.abs(actual-expected)<2e-7,message+": "+actual+" != "+expected);
    }
    private static Object unconstructed(Class<?> type) throws Exception {
        var unsafe=Class.forName("sun.misc.Unsafe");var field=unsafe.getDeclaredField("theUnsafe");field.setAccessible(true);
        return unsafe.getMethod("allocateInstance",Class.class).invoke(field.get(null),type);
    }
    private static Field field(Class<?> type,Class<?> valueType,boolean isStatic) throws Exception {
        for(Class<?> cursor=type;cursor!=null;cursor=cursor.getSuperclass())for(Field f:cursor.getDeclaredFields())
            if(f.getType()==valueType&&Modifier.isStatic(f.getModifiers())==isStatic){f.setAccessible(true);return f;}
        throw new NoSuchFieldException(type+" field of type "+valueType);
    }
    private static Method hook(Class<?> type,String name,int parameters) {
        var methods=Arrays.stream(type.getDeclaredMethods()).filter(m->m.getName().contains(name)&&m.getParameterCount()==parameters).toList();
        require(methods.size()==1,"one applied "+name+" hook");var method=methods.getFirst();method.setAccessible(true);return method;
    }
    @Override public void onPreLaunch() {
        try {check();System.exit(0);}catch(Throwable failure){failure.printStackTrace();System.exit(1);}
    }
    private static void check() throws Exception {
        boolean standalone=FabricLoader.getInstance().isModLoaded("biome_cloud_transitions");
        boolean legacy=FabricLoader.getInstance().isModLoaded("wwoo");
        SharedConstants.tryDetectVersion();Bootstrap.bootStrap();RenderSystem.initRenderThread();
        var registry=new MappedRegistry<Biome>(Registries.BIOME,Lifecycle.stable());
        var sourceKey=ResourceKey.create(Registries.BIOME,ResourceLocation.parse("minecraft:desert"));
        var targetKey=ResourceKey.create(Registries.BIOME,ResourceLocation.parse("minecraft:pale_garden"));
        Registry.register(registry,sourceKey,(Biome)unconstructed(Biome.class));
        Registry.register(registry,targetKey,(Biome)unconstructed(Biome.class));
        var source=registry.getHolderOrThrow(sourceKey);var target=registry.getHolderOrThrow(targetKey);
        ClientLevel level=(ClientLevel)unconstructed(ClientLevel.class);
        field(ClientLevel.class,BiomeManager.class,false).set(level,new BiomeManager((x,y,z)->x<0?source:target,0));
        var camera=new Camera();field(Camera.class,Entity.class,false).set(camera,unconstructed(Pig.class));
        var position=Arrays.stream(Camera.class.getDeclaredMethods()).filter(m->Arrays.equals(m.getParameterTypes(),
            new Class<?>[]{double.class,double.class,double.class})&&m.getReturnType()==void.class).findFirst().orElseThrow();
        position.setAccessible(true);
        GameRenderer gameRenderer=(GameRenderer)unconstructed(GameRenderer.class);
        field(GameRenderer.class,Camera.class,false).set(gameRenderer,camera);
        Minecraft client=(Minecraft)unconstructed(Minecraft.class);client.level=level;
        field(Minecraft.class,GameRenderer.class,false).set(client,gameRenderer);field(Minecraft.class,Minecraft.class,true).set(null,client);
        Class<?> rendererClass=Class.forName("net.minecraft.class_761");Object renderer=unconstructed(rendererClass);
        var tint=legacy?hook(ClientLevel.class,"wwoo$cloudColor",2):null;
        var alpha=legacy?hook(rendererClass,"wwoo$cloudAlpha",1):null;
        var refresh=legacy?hook(rendererClass,"wwoo$refreshCloudAlpha",1):null;
        if(legacy) {var previousAlpha=rendererClass.getDeclaredField("wwoo$previousAlpha");previousAlpha.setAccessible(true);previousAlpha.setFloat(renderer,-1f);}
        var regenerate=rendererClass.getDeclaredField("field_4107");regenerate.setAccessible(true);
        Method sample=standalone?hook(rendererClass,"wwoo$sampleCloudColor",1):null;
        Method draw=standalone?hook(rendererClass,"wwoo$tintCloudDraw",5):null;
        Method capture=standalone?hook(ClientLevel.class,"biomeclouds$rememberWeather",1):null;
        Method nativeColorHook=standalone?hook(rendererClass,"biomeclouds$nativeWeatherColor",1):null;
        Method nativeAlpha=standalone?hook(rendererClass,"biomeclouds$nativeCloudAlpha",1):null;
        if(standalone&&legacy) {
            var exported=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(".mixin.out/class/net/minecraft/class_638.class"))).accept(exported,0);
            var nativeMethod=exported.methods.stream().filter(m->m.name.equals("method_23785")).findFirst().orElseThrow();
            int captureIndex=-1,legacyIndex=-1;
            for(var insn:nativeMethod.instructions)if(insn instanceof MethodInsnNode call) {
                if(call.name.contains("biomeclouds$rememberWeather"))captureIndex=nativeMethod.instructions.indexOf(insn);
                if(call.name.contains("wwoo$cloudColor"))legacyIndex=nativeMethod.instructions.indexOf(insn);
            }
            require(captureIndex>=0&&legacyIndex>captureIndex,"native weather captured before other biome return hooks");
            var rendererExport=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(".mixin.out/class/net/minecraft/class_761.class"))).accept(rendererExport,0);
            var build=rendererExport.methods.stream().filter(m->m.name.equals("method_3239")).findFirst().orElseThrow();
            boolean oldAlpha=false,nativeAlphaSeen=false;int vertices=0;
            for(var insn:build.instructions)if(insn instanceof MethodInsnNode call) {
                if(call.name.contains("wwoo$cloudAlpha")) {require(!nativeAlphaSeen,"native alpha is applied after existing argument hooks");oldAlpha=true;}
                if(call.name.contains("biomeclouds$nativeCloudAlpha")) {require(oldAlpha,"native alpha owns final vertex argument");nativeAlphaSeen=true;}
                if(call.owner.equals("net/minecraft/class_4588")&&call.name.equals("method_22915")) {
                    require(oldAlpha&&nativeAlphaSeen,"both actual alpha hooks applied before vertex upload");vertices++;oldAlpha=false;nativeAlphaSeen=false;
                }
            }
            require(vertices>0,"actual vanilla cloud vertex uploads checked");
        }
        var modelView=new Matrix4f();var projection=new Matrix4f();
        double largestStep=0;int rebuilds=0;float[] previous=null;Vec3 weather=new Vec3(.7,.8,.9);
        for(int direction:new int[]{1,-1})for(int step=0;step<=256;step++) {
            double x=direction*(-32+step*.25);position.invoke(camera,x,64d,0d);
            if(standalone)sample.invoke(renderer,new CallbackInfo("renderClouds",false));
            var callback=new CallbackInfoReturnable<Vec3>("getCloudColor",true,weather);
            if(standalone)capture.invoke(level,weather);
            if(legacy)tint.invoke(level,.5f,callback);Vec3 nativeColor=callback.getReturnValue();
            if(standalone)nativeColor=(Vec3)nativeColorHook.invoke(renderer,nativeColor);
            float bakedAlpha=legacy?(float)alpha.invoke(renderer,.8f):.8f;
            if(standalone)bakedAlpha=(float)nativeAlpha.invoke(renderer,bakedAlpha);
            regenerate.setBoolean(renderer,false);if(legacy)refresh.invoke(renderer,new CallbackInfo("renderClouds",false));
            if(regenerate.getBoolean(renderer))rebuilds++;
            float[] pixel;
            if(standalone) {
                near(nativeColor.x,weather.x,"old RGB does not overwrite the smooth renderer");
                near(nativeColor.y,weather.y,"weather green retained");near(nativeColor.z,weather.z,"weather blue retained");
                near(bakedAlpha,.8f,"old biome vertex alpha cannot pop");
                RenderSystem.setShaderColor((float)nativeColor.x,(float)nativeColor.y,(float)nativeColor.z,.65f);
                float[] state=RenderSystem.getShaderColor().clone();float[][] captured={null};
                Operation<Void> operation=args->{captured[0]=RenderSystem.getShaderColor().clone();return null;};
                draw.invoke(renderer,null,modelView,projection,null,operation);pixel=captured[0];
                require(Arrays.equals(state,RenderSystem.getShaderColor()),"shader state restored after combined draw");
                Vec3 expected=CubicSampler.gaussianSampleVec3(new Vec3(x,64,0).subtract(2,2,2).scale(.25),
                    (qx,qy,qz)->qx<0?new Vec3(1,1,1):Vec3.fromRGB24(0x828282));
                near(pixel[0],weather.x*expected.x,"native camera produces smooth cloud red");
                near(pixel[1],weather.y*expected.y,"native camera produces smooth cloud green");
                near(pixel[2],weather.z*expected.z,"native camera produces smooth cloud blue");
                near(pixel[3],.65f,"combined shader alpha remains constant across old biome alpha changes");
            } else pixel=new float[]{(float)nativeColor.x,(float)nativeColor.y,(float)nativeColor.z,bakedAlpha};
            if(previous!=null)for(int channel=0;channel<3;channel++)largestStep=Math.max(largestStep,Math.abs(pixel[channel]-previous[channel]));
            previous=pixel;
        }
        if(standalone) {
            require(largestStep<.02,"no instantaneous color jump from old vanilla-renderer hooks");
            require(legacy?rebuilds>=3:rebuilds==0,"native color/alpha remain stable even when another hook forces a cloud rebuild");
            System.out.println((legacy?"LEGACY_WWOO_TRANSITION_PASS":"STANDALONE_TRANSITION_PASS")+": complete cloud JAR; WWOO present="+legacy+"; native camera forward/reverse crossing; RGB follows sky kernel; alpha stable through native buffer rebuilds; largest step="+largestStep);
        } else {
            require(largestStep>.1,"control reproduces original instant cloud-color popping");
            require(rebuilds>=3,"control reproduces old biome alpha buffer rebuilds");
            System.out.println("LEGACY_WWOO_POP_CONTROL_PASS: unmodified .6 hooks reproduce color/alpha jumps; largest step="+largestStep+" rebuilds="+rebuilds);
        }
    }
}
