#version 150 core

in ivec2 aScreenPos;
in ivec2 aCenterPos;
in float aAngle;
in vec4 aColor;

uniform mat4 uProjection;

out vec4 vColor;

void main() {
    vec2 p = vec2(aScreenPos);
    if (aAngle != 0.0) {
        float rad = radians(aAngle);
        float s = sin(rad);
        float co = cos(rad);
        vec2 c = vec2(aCenterPos);
        vec2 d = p - c;
        p = c + vec2(d.x * co - d.y * s, d.x * s + d.y * co);
    }
    gl_Position = uProjection * vec4(p, 0.0, 1.0);

    vColor = aColor;
}