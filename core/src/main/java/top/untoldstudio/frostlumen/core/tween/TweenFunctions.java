/*
 * Copyright 2026 Untold Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.untoldstudio.frostlumen.core.tween;

public class TweenFunctions {
    public static final TweenFunction LINEAR = percent -> percent;

    public static final TweenFunction SINE_IN = percent -> 1 - Math.cos((percent * Math.PI) / 2);
    public static final TweenFunction SINE_OUT = percent -> Math.sin((percent * Math.PI) / 2);
    public static final TweenFunction SINE_IN_OUT = percent -> -(Math.cos(Math.PI * percent) - 1) / 2;

    public static final TweenFunction QUAD_IN = percent -> percent * percent;
    public static final TweenFunction QUAD_OUT = percent -> 1 - (1 - percent) * (1 - percent);
    public static final TweenFunction QUAD_IN_OUT = percent -> percent < 0.5 ? 2 * percent * percent : 1 - Math.pow(-2 * percent + 2, 2) / 2;

    public static final TweenFunction CUBIC_IN = percent -> percent * percent * percent;
    public static final TweenFunction CUBIC_OUT = percent -> 1 - Math.pow(1 - percent, 3);
    public static final TweenFunction CUBIC_IN_OUT = percent -> percent < 0.5 ? 4 * percent * percent * percent : 1 - Math.pow(-2 * percent + 2, 3) / 2;

    public static final TweenFunction QUART_IN = percent -> percent * percent * percent * percent;
    public static final TweenFunction QUART_OUT = percent -> 1 - Math.pow(1 - percent, 4);
    public static final TweenFunction QUART_IN_OUT = percent -> percent < 0.5 ? 8 * percent * percent * percent * percent : 1 - Math.pow(-2 * percent + 2, 4) / 2;

    public static final TweenFunction QUINT_IN = percent -> percent * percent * percent * percent * percent;
    public static final TweenFunction QUINT_OUT = percent -> 1 - Math.pow(1 - percent, 5);
    public static final TweenFunction QUINT_IN_OUT = percent -> percent < 0.5 ? 16 * percent * percent * percent * percent * percent : 1 - Math.pow(-2 * percent + 2, 5) / 2;

    public static final TweenFunction EXPO_IN = percent -> percent == 0 ? 0 : Math.pow(2, 10 * percent - 10);
    public static final TweenFunction EXPO_OUT = percent -> percent == 1 ? 1 : 1 - Math.pow(2, -10 * percent);
    public static final TweenFunction EXPO_IN_OUT = percent -> {
        if (percent == 0) return 0d;
        if (percent == 1) return 1d;
        return percent < 0.5 ? Math.pow(2, 20 * percent - 10) / 2 : (2 - Math.pow(2, -20 * percent + 10)) / 2;
    };

    public static final TweenFunction CIRC_IN = percent -> 1 - Math.sqrt(1 - Math.pow(percent, 2));
    public static final TweenFunction CIRC_OUT = percent -> Math.sqrt(1 - Math.pow(percent - 1, 2));
    public static final TweenFunction CIRC_IN_OUT = percent -> percent < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * percent, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * percent + 2, 2)) + 1) / 2;

    public static final TweenFunction BACK_IN = percent -> {
        double overshoot_amount = 1.70158;
        double shifted_overshoot = overshoot_amount + 1;
        return shifted_overshoot * percent * percent * percent - overshoot_amount * percent * percent;
    };
    public static final TweenFunction BACK_OUT = percent -> {
        double overshoot_amount = 1.70158;
        double shifted_overshoot = overshoot_amount + 1;
        return 1 + shifted_overshoot * Math.pow(percent - 1, 3) + overshoot_amount * Math.pow(percent - 1, 2);
    };
    public static final TweenFunction BACK_IN_OUT = percent -> {
        double overshoot_amount = 1.70158;
        double scaled_overshoot = overshoot_amount * 1.525;
        return percent < 0.5 ? (Math.pow(2 * percent, 2) * ((scaled_overshoot + 1) * 2 * percent - scaled_overshoot)) / 2 : (Math.pow(2 * percent - 2, 2) * ((scaled_overshoot + 1) * (percent * 2 - 2) + scaled_overshoot) + 2) / 2;
    };

    public static final TweenFunction ELASTIC_IN = percent -> {
        double period = (2 * Math.PI) / 3;
        if (percent == 0) return 0d;
        if (percent == 1) return 1d;
        return -Math.pow(2, 10 * percent - 10) * Math.sin((percent * 10 - 10.75) * period);
    };
    public static final TweenFunction ELASTIC_OUT = percent -> {
        double period = (2 * Math.PI) / 3;
        if (percent == 0) return 0d;
        if (percent == 1) return 1d;
        return Math.pow(2, -10 * percent) * Math.sin((percent * 10 - 0.75) * period) + 1;
    };
    public static final TweenFunction ELASTIC_IN_OUT = percent -> {
        double period = (2 * Math.PI) / 4.5;
        if (percent == 0) return 0d;
        if (percent == 1) return 1d;
        double sine_value = Math.sin((20 * percent - 11.125) * period);
        return percent < 0.5 ? -(Math.pow(2, 20 * percent - 10) * sine_value) / 2 : (Math.pow(2, -20 * percent + 10) * sine_value) / 2 + 1;
    };

    public static final TweenFunction BOUNCE_IN = percent -> 1 - bounceOut(1 - percent);
    public static final TweenFunction BOUNCE_OUT = TweenFunctions::bounceOut;
    public static final TweenFunction BOUNCE_IN_OUT = percent -> percent < 0.5 ? (1 - bounceOut(1 - 2 * percent)) / 2 : (1 + bounceOut(2 * percent - 1)) / 2;

    public static final TweenFunction SMOOTHSTEP = percent -> percent * percent * (3 - 2 * percent);
    public static final TweenFunction SMOOTHERSTEP = percent -> percent * percent * percent * (percent * (percent * 6 - 15) + 10);

    private TweenFunctions() {
    }

    private static double bounceOut(double percent) {
        double multiplier = 7.5625;
        double divisor = 2.75;
        if (percent < 1 / divisor) {
            return multiplier * percent * percent;
        } else if (percent < 2 / divisor) {
            percent -= 1.5 / divisor;
            return multiplier * percent * percent + 0.75;
        } else if (percent < 2.5 / divisor) {
            percent -= 2.25 / divisor;
            return multiplier * percent * percent + 0.9375;
        } else {
            percent -= 2.625 / divisor;
            return multiplier * percent * percent + 0.984375;
        }
    }
}