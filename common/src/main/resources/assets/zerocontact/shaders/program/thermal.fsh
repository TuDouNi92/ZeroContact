#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D HeatSampler;
uniform sampler2D SceneDepthSampler;
uniform sampler2D WorldDepthSampler;
uniform float ColorMode;
uniform float BackgroundGain;
uniform float NoiseStrength;
uniform float VignetteStrength;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

vec3 thermalPalette(float temperature) {
    vec3 cold = vec3(0.08, 0.05, 0.18);
    vec3 purple = vec3(0.48, 0.14, 0.62);
    vec3 red = vec3(0.92, 0.16, 0.07);
    vec3 yellow = vec3(1.0, 0.72, 0.08);
    vec3 white = vec3(1.0, 0.98, 0.85);
    if (temperature < 0.25) return mix(cold, purple, temperature * 4.0);
    if (temperature < 0.5) return mix(purple, red, (temperature - 0.25) * 4.0);
    if (temperature < 0.75) return mix(red, yellow, (temperature - 0.5) * 4.0);
    return mix(yellow, white, (temperature - 0.75) * 4.0);
}

void main() {
    vec3 scene = texture(DiffuseSampler, texCoord).rgb;
    vec4 heat = texture(HeatSampler, texCoord);
    // The first-person model is rendered after the world heat mask. Its depth
    // therefore has to be compared at compositing time, after hand rendering.
    float sceneDepth = texture(SceneDepthSampler, texCoord).r;
    float worldDepth = texture(WorldDepthSampler, texCoord).r;
    if (heat.b < 0.5 && sceneDepth + 0.0001 < worldDepth) {
        heat.a = 0.0;
    }
    float luminance = dot(scene, vec3(0.2126, 0.7152, 0.0722));
    // Scene brightness is only an artistic cold background, not a temperature estimate.
    float background = min(0.32 + pow(max(luminance, 0.0), 0.65) * BackgroundGain, 0.68);
    float temperature = mix(background, heat.r, clamp(heat.a, 0.0, 1.0));
    float noise = fract(sin(dot(gl_FragCoord.xy + vec2(Time * 173.0),
            vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    temperature = clamp(temperature + noise * NoiseStrength, 0.0, 1.0);
    vec3 color = mix(vec3(temperature), thermalPalette(temperature), clamp(ColorMode, 0.0, 1.0));
    vec2 centered = texCoord * 2.0 - 1.0;
    float vignette = 1.0 - VignetteStrength * smoothstep(0.3, 1.6, dot(centered, centered));
    fragColor = vec4(color * vignette, 1.0);
}
