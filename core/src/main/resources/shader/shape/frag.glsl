#version 150 core

in vec2 vLocalPos;
in vec2 vHalfSize;
in vec4 vFillColor;
in vec4 vBorderColor;
flat in ivec4 vCornerRadii;
flat in ivec4 vEdgeThickness;
flat in int vBorderPosition;

out vec4 FragColor;

float sdRoundRect(vec2 p, vec2 halfSize, vec4 radii) {
    float r;
    if (p.x < 0.0) {
        r = (p.y < 0.0) ? radii.x : radii.z;
    } else {
        r = (p.y < 0.0) ? radii.y : radii.w;
    }
    vec2 q = abs(p) - halfSize + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

float selectThickness(vec2 p, vec2 halfSize, ivec4 t) {
    float dx = halfSize.x - abs(p.x);
    float dy = halfSize.y - abs(p.y);
    if (dx < dy) {
        return (p.x < 0.0) ? float(t.x) : float(t.y);
    } else {
        return (p.y < 0.0) ? float(t.z) : float(t.w);
    }
}

void main() {
    float thickness = selectThickness(vLocalPos, vHalfSize, vEdgeThickness);

    float innerOffset;
    float outerOffset;
    if (vBorderPosition == 0) {
        innerOffset = thickness;
        outerOffset = 0.0;
    } else if (vBorderPosition == 1) {
        innerOffset = thickness * 0.5;
        outerOffset = thickness * 0.5;
    } else {
        innerOffset = 0.0;
        outerOffset = thickness;
    }

    vec2 innerHalf = vHalfSize - innerOffset;
    vec2 outerHalf = vHalfSize + outerOffset;

    vec4 radii = vec4(vCornerRadii);
    vec4 innerRadii = radii;
    vec4 outerRadii = radii + vec4(innerOffset + outerOffset);

    float dInner = sdRoundRect(vLocalPos, innerHalf, innerRadii);
    float dOuter = sdRoundRect(vLocalPos, outerHalf, outerRadii);

    float aa = max(fwidth(dInner), 0.0001);

    float fillMask = 1.0 - smoothstep(-aa, aa, dInner);
    float outerMask = 1.0 - smoothstep(-aa, aa, dOuter);

    vec4 color = mix(vBorderColor, vFillColor, fillMask);

    color.a *= outerMask;

    FragColor = color;
}