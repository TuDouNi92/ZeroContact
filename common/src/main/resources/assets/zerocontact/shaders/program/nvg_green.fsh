#version 150

uniform sampler2D DiffuseSampler;

uniform float Time;
uniform float Gain;
uniform float Gamma;
uniform float NoiseStrength;
uniform float VignetteStrength;
uniform vec3 PhosphorColor;

in vec2 texCoord;

out vec4 fragColor;

// 生成用于颗粒效果的伪随机数。
float hash21(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

void main() {
    vec3 source = texture(DiffuseSampler, texCoord).rgb;

    // 1. 提取画面亮度，去掉原来的色彩。
    float perceptualLum =
    dot(source, vec3(0.2126, 0.7152, 0.0722));

    float peakLum =
    max(source.r, max(source.g, source.b));

    float luminance = mix(
            perceptualLum,
            peakLum,
            0.35
    );

    // 2. 放大弱光信号。
    // 指数曲线让高亮部分平滑接近 1，避免直接乘法截断。
    float signal = 1.0 - exp(-luminance * Gain);

    // 小于 1 的指数进一步抬高中低亮度。
    signal = pow(max(signal, 0.0), max(Gamma, 0.01));

    // 3. 动态颗粒：暗部稍多，亮部稍少。
    // 此处仅把它当作随时间变化的噪声种子。
    float phase = floor(Time * 24000.0);
    float noise = hash21(
            floor(gl_FragCoord.xy) + vec2(phase, phase * 0.37)
    ) - 0.5;

    float noiseAmount = NoiseStrength * mix(1.0, 0.35, signal);
    signal = clamp(signal + noise * noiseAmount, 0.0, 1.0);

    float phosphor = 0.004 + signal * 0.996;

    vec3 color = phosphor * PhosphorColor;

    // 强光逐渐失去磷色偏并趋向白
    float highlight = smoothstep(0.70, 0.97, signal);
    color = mix(
            color,
            vec3(phosphor),
            highlight * 0.8
    );

    // 5. 柔和的椭圆暗角，模拟边缘亮度下降。
    vec2 centered = texCoord * 2.0 - 1.0;
    float edge = smoothstep(0.25, 1.35, length(centered));
    float vignette = 1.0 - edge * VignetteStrength;

    color *= vignette;

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}