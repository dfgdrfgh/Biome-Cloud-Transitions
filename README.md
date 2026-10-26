# Biome Cloud Transitions — Fabric 1.21.1

WWOO is not required. The mod includes its own biome cloud palette and renders biome transitions independently, with vanilla, Sodium and Cirrus support.

Java 21 and Fabric Loader 0.18.5 or newer. Cirrus 1.4.1 requires its own newer loader and dependencies.

Install `biome-cloud-transitions-fabric-1.21.1-1.0.2.jar` in your client `mods` folder.

Cloud RGB and opacity use Minecraft's sky biome sampling weights at the actual camera position. Moving into a biome progressively blends cloud color and transparency toward that biome's configured palette. Stopping holds the current color/opacity; walking back reverses immediately. There is no timed fade. Biomes without custom palette attributes use vanilla-style white clouds at 0.8 opacity.

Vanilla, Sodium 0.8.13 and Cirrus 1.4.1 use the same cloud palette. Optional renderer integrations are enabled only when their target classes exist. Cirrus keeps its time, weather and lighting tint. Fast layers retain their configured opacity near the sun and Fancy retains native Cirrus behavior; both are scaled by the smoothly blended biome opacity. The opacity patch applies to Cirrus's native shader. Shader-pack replacements are outside that patch.

Version 1.0.2 builds on the core render-pipeline fixes and also enables spatially blended biome cloud opacity. It preserves the native colors and the vanilla 0.8 vertex alpha, and applies the opacity multiplier at draw/shader time for vanilla, Sodium and Cirrus. Version 1.0.1 fixed the core render pipeline. It captures native cloud weather/time RGB before later biome return hooks, uses that native base in vanilla/Sodium/Cirrus, and keeps vanilla cloud vertex alpha at 0.8. The spatial biome tint is then applied once at draw time. Cloud-buffer rebuilds cannot bake a second biome tint or a changing biome alpha into the transition. There is no WWOO-specific runtime patch or dependency. Tests run the complete JAR alone and alongside the original `.6` rendering hooks, with a separate control that reproduces the old popping. Replace the old cloud JAR; do not keep both cloud versions installed.

## Build locally

Install JDK 21 and Python 3. Put Java on PATH or set `CLOUD_JAVA_HOME` to the JDK directory. From this directory:

```sh
python3 port/bootstrap.py
python3 build.py
python3 tests/cloud_blending.py
```

The bootstrap downloads public Minecraft/Fabric dependencies and mappings; Minecraft binaries are not redistributed in the source archive. The installable JAR is written in the project root.

## Upload to GitHub

Upload the contents of this directory, including `.github/workflows/build.yml`, to your repository's `main` branch. The workflow compiles the standalone cloud JAR, tests actual vanilla/Sodium/Cirrus renderer hooks and shader output on software OpenGL, then uploads the installable JAR under the run's Artifacts section. You can also start it under Actions → Build Biome Cloud Transitions → Run workflow.

## Validation of the supplied JAR

The supplied core JAR separates and renames the verified build .16 cloud classes into this mod's own Java/resource namespace. All executable method bytecode is retained. Its palette, mixin registrations and class references were checked after extraction. A fresh standalone Minecraft launch was not available in the packaging environment; the included workflow provides source compilation and renderer regression checks when uploaded. Upstream palette attribution is in `NOTICE.txt`.

