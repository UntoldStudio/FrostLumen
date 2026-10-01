#version 150 core

in ivec2 aScreenPos;
in ivec2 aCenterPos;
in float aAngle;
in vec2 aLocalPos;
in vec2 aHalfSize;
in vec4 aFillColor;
in vec4 aBorderColor;
in ivec4 aCornerRadii;
in ivec4 aEdgeThickness;
in int aBorderPosition;

uniform mat4 uProjection;

out vec2 vLocalPos;
out vec2 vHalfSize;
out vec4 vFillColor;
out vec4 vBorderColor;
flat out ivec4 vCornerRadii;
flat out ivec4 vEdgeThickness;
flat out int vBorderPosition;

void main() {
    vec2 p = vec2(aScreenPos);
    vec2 c = vec2(aCenterPos);
    if (aAngle != 0.0) {
        float rad = radians(aAngle);
        float s = sin(rad);
        float co = cos(rad);
        vec2 d = p - c;
        p = c + vec2(d.x * co - d.y * s, d.x * s + d.y * co);
    }
    gl_Position = uProjection * vec4(p, 0.0, 1.0);

    vLocalPos = aLocalPos;
    vHalfSize = aHalfSize;
    vFillColor = aFillColor;
    vBorderColor = aBorderColor;
    vCornerRadii = aCornerRadii;
    vEdgeThickness = aEdgeThickness;
    vBorderPosition = aBorderPosition;
}
