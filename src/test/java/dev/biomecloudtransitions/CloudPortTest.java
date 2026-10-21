package dev.biomecloudtransitions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CloudPortTest {
    @Test void spatialSamplerMatchesNativeSkyWeights() throws Exception { CloudBlendCheck.check(); }
    @Test void fastCloudOpacityPatchIsIdempotentAndKeepsLighting() {
        String source="uniform float CirrusSunWeight;\ncolor.a = mix(color.a, 1.0, sunIllumination * CirrusSunWeight);";
        String patched=CloudOpacity.patch("cirrus_clouds.fsh",source);
        assertTrue(patched.contains("uniform float WwooFastOpacity;"));
        assertTrue(patched.contains("sunIllumination * CirrusSunWeight * (1.0 - WwooFastOpacity)"));
        assertEquals(patched,CloudOpacity.patch("cirrus_clouds.fsh",patched));
    }
    @Test void customShaderSourceIsKeptIntact() {
        String custom="void main() { /* a resource pack replaces cloud shading */ }";
        assertEquals(custom,CloudOpacity.patch("cirrus_clouds.fsh",custom));
    }
    @Test void encodedAlphaDoesNotAffectBiomeRgb() {
        assertEquals(CloudBlend.Color.fromArgb(0x11828282),CloudBlend.Color.fromArgb(0xff828282));
    }
}
