# Biome Cloud Transitions — Fabric 1.21.1

A separate client-only mod containing the cloud changes from WWOO backport .16. WWOO is optional. Requires Minecraft 1.21.1, Java 21 and Fabric Loader 0.18.5 or newer. Cirrus 1.4.1 requires its own newer loader and dependencies.

Install `biome-cloud-transitions-fabric-1.21.1-1.0.0.jar` in your client `mods` folder. If using the WWOO fork, replace the old WWOO build with .17, which removes its cloud hooks. Do not run this alongside WWOO .16 or another build containing the same cloud patches.

Cloud RGB uses Minecraft's sky biome sampling weights at the actual camera position. Moving into a biome progressively changes the cloud color toward that biome's color. Stopping holds the current color; walking back reverses it immediately. There is no timed fade and encoded palette alpha does not fade cloud visibility.

Vanilla, Sodium 0.8.13 and Cirrus 1.4.1 use the same cloud palette. Optional renderer integrations are enabled only when their target classes exist. Cirrus keeps its time, weather and lighting tint. Fast layers retain their configured opacity near the sun; Fancy retains native Cirrus behavior. The opacity patch applies to Cirrus's native shader. Shader-pack replacements are outside that patch.

The bundled palette contains all 22 explicit cloud colors from the latest WWOO build. Biomes without a cloud entry use white. The mod changes rendering only and includes no terrain or biome generation.

## Build locally

Install JDK 21 and Python 3. Put Java on PATH or set `CLOUD_JAVA_HOME` to the JDK directory. From this directory:

```sh
python3 port/bootstrap.py
python3 build.py
python3 tests/cloud_blending.py
```

The bootstrap downloads public Minecraft/Fabric dependencies and mappings; Minecraft binaries are not redistributed in the source archive. The installable JAR is written in the project root.

## Upload to GitHub

Upload the contents of this directory, including `.github/workflows/build.yml`, to your repository's `main` branch. The included workflow compiles the core JAR and optional diagnostic, tests actual vanilla/Sodium/Cirrus renderer hooks and shader output on software OpenGL, then uploads separate JAR artifacts. You can also start it under Actions → Build Biome Cloud Transitions → Run workflow.

## Optional diagnostic

The core does not need Fabric API or the diagnostic. To build the separate temporary diagnostic after the core:

```sh
python3 port/build_cloud_diagnostics.py
```

The diagnostic requires this cloud mod, Cirrus 1.4.1 and Fabric API. Replace the old diagnostic with the updated one; its F8 capture key and `/wwoocloud capture` command remain available. See `diagnostics/README.md`.

## Validation of the supplied JAR

The supplied core JAR separates and renames the verified build .16 cloud classes into this mod's own Java/resource namespace. All executable method bytecode is retained. Its palette, mixin registrations and class references were checked after extraction. A fresh standalone Minecraft launch was not available in the packaging environment; the included workflow provides source compilation and renderer regression checks when uploaded. Upstream palette attribution is in `NOTICE.txt`.
