#version 150 core

in ivec2 aScreenPos;
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
    gl_Position = uProjection * vec4(vec2(aScreenPos), 0.0, 1.0);

    vLocalPos = aLocalPos;
    vHalfSize = aHalfSize;
    vFillColor = aFillColor;
    vBorderColor = aBorderColor;
    vCornerRadii = aCornerRadii;
    vEdgeThickness = aEdgeThickness;
    vBorderPosition = aBorderPosition;
}
