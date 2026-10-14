# Biome Cloud Transitions — Fabric 1.21.1

WWOO is not required. The mod includes its own biome cloud palette and renders biome transitions independently, with vanilla, Sodium and Cirrus support.

Java 21 and Fabric Loader 0.18.5 or newer. Cirrus 1.4.1 requires its own newer loader and dependencies.

Install `biome-cloud-transitions-fabric-1.21.1-1.0.1.jar` in your client `mods` folder.

Cloud RGB uses Minecraft's sky biome sampling weights at the actual camera position. Moving into a biome progressively changes the cloud color toward that biome's color. Stopping holds the current color; walking back reverses it immediately. There is no timed fade and encoded palette alpha does not fade cloud visibility.

Vanilla, Sodium 0.8.13 and Cirrus 1.4.1 use the same cloud palette. Optional renderer integrations are enabled only when their target classes exist. Cirrus keeps its time, weather and lighting tint. Fast layers retain their configured opacity near the sun; Fancy retains native Cirrus behavior. The opacity patch applies to Cirrus's native shader. Shader-pack replacements are outside that patch.

Version 1.0.1 supports the reference WWOO backport `.6` build. Its old instant tint and biome alpha hooks are neutralized while this mod is installed, so cloud-buffer rebuilds and native biome tint cannot override the spatial cloud transition. The original vanilla weather/time shading and default cloud alpha are retained. WWOO `.19` also remains supported. Replace the old cloud JAR; do not keep both cloud versions installed.

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

