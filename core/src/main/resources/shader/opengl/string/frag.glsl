#version 150 core

in vec2 vTexCoord;
in vec4 vColor;

uniform sampler2D uTexture;
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

    float coverage = texture(uTexture, vTexCoord).r;
    FragColor = vec4(vColor.rgb, vColor.a * coverage * accumulatedScissorCoverage);
}