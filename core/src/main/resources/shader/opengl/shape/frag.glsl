#version 150 core

in vec2 vLocalPos;
in vec2 vHalfSize;
in vec4 vFillColor;
in vec4 vBorderColor;
flat in ivec4 vCornerRadii;
flat in ivec4 vEdgeThickness;
flat in int vBorderPosition;

uniform float uViewportHeight;
uniform int uScissorRectCount;
uniform vec4 uScissorRects[48];

out vec4 FragColor;

float scissorRoundRectDistance(vec2 localPosition, vec2 halfSize, vec4 cornerRadii) {
    float radius;
    if (localPosition.x < 0.0) {
        radius = (localPosition.y < 0.0) ? cornerRadii.x : cornerRadii.z;
    } else {
        radius = (localPosition.y < 0.0) ? cornerRadii.y : cornerRadii.w;
    }
    vec2 q = abs(localPosition) - halfSize + radius;
    return length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - radius;
}

float evaluateScissorCoverage(vec2 screenPosition, vec4 rectangleGeometry, vec4 rectangleOrientation, vec4 rectangleCornerRadiusTail) {
    vec2 relativePosition = screenPosition - rectangleGeometry.xy;
    vec2 localPosition = vec2(
    relativePosition.x * rectangleOrientation.x + relativePosition.y * rectangleOrientation.y,
    -relativePosition.x * rectangleOrientation.y + relativePosition.y * rectangleOrientation.x
    );

    float maxCornerRadius = min(rectangleGeometry.z, rectangleGeometry.w);
    vec4 cornerRadii = min(vec4(
                           rectangleOrientation.z,
                           rectangleOrientation.w,
                           rectangleCornerRadiusTail.x,
                           rectangleCornerRadiusTail.y
                           ), vec4(maxCornerRadius));

    float signedDistance;
    if (cornerRadii.x + cornerRadii.y + cornerRadii.z + cornerRadii.w < 0.5) {
        vec2 edgeDistance = abs(localPosition) - rectangleGeometry.zw;
        signedDistance = max(edgeDistance.x, edgeDistance.y);
    } else {
        signedDistance = scissorRoundRectDistance(localPosition, rectangleGeometry.zw, cornerRadii);
    }

    float antialiasWidth = max(fwidth(signedDistance), 0.0001);
    return 1.0 - smoothstep(-antialiasWidth, antialiasWidth, signedDistance);
}

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
    float accumulatedScissorCoverage = 1.0;
    if (uScissorRectCount > 0) {
        vec2 screenPosition = vec2(gl_FragCoord.x, uViewportHeight - gl_FragCoord.y);
        for (int rectangleIndex = 0; rectangleIndex < uScissorRectCount; rectangleIndex++) {
            accumulatedScissorCoverage *= evaluateScissorCoverage(screenPosition,
                                                                  uScissorRects[rectangleIndex * 3],
                                                                  uScissorRects[rectangleIndex * 3 + 1],
                                                                  uScissorRects[rectangleIndex * 3 + 2]);
        }
    }

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
    vec4 offset = vec4(innerOffset + outerOffset);
    vec4 innerRadii = radii;
    vec4 outerRadii = mix(vec4(0.0), radii + offset, step(vec4(0.001), radii));

    float dInner = sdRoundRect(vLocalPos, innerHalf, innerRadii);
    float dOuter = sdRoundRect(vLocalPos, outerHalf, outerRadii);

    float aa = max(fwidth(dInner), 0.0001);

    float fillMask = 1.0 - smoothstep(-aa, aa, dInner);
    float outerMask = 1.0 - smoothstep(-aa, aa, dOuter);

    vec4 color = vFillColor;
    if (thickness > 0.0) {
        color = mix(vBorderColor, vFillColor, fillMask);
    }

    color.a *= outerMask;
    color.a *= accumulatedScissorCoverage;

    FragColor = color;
}