# Tween

This chapter will explain how to use Tween (tween animation) in the library.

## Core Concepts

All Tweens are essentially not bound to any UI. They are essentially just a value that grows over time, and you can use them anywhere.

A Tween consists of two parts: a Tween object (the carrier of data and some operations) and a TweenFunction (the easing function, which determines how it changes over time).

## Tween Scheduler

The Tween scheduler is a container that schedules a bunch of Tweens. In our library, NodeRoot has a Tween scheduler, which you can get via `getTweenScheduler`.

Each time the scheduler's `step()` method is called, it calls `step(delta)` on every registered Tween. A Tween only advances its value when it is stepped.

!!! warning

    NodeRoot automatically steps its own Tween scheduler during rendering; you do not need to step it manually.

## Tween Object

A Tween object is a data-carrying entity whose value grows over time. Currently, the library has three primitive-type Tween objects: `DoubleTween`, `FloatTween`, and `IntTween`, and two composite-type Tween objects: `ScaleOffsetTween` and `RGBATween`.

A Tween object should have a method that returns its own value, such as the `getCurrentValue` method of the five objects listed above, and a method for binding a setter.

!!! warning

    The time unit of a Tween object is milliseconds.

Example: DoubleTween's `getCurrentValue` method and setter-binding method (`registerAutoCallOnSetter`)

```java
--8<-- "core/src/main/java/top/untold/frostlumen/core/tween/DoubleTween.java:getCurrentValue"
        
--8<-- "core/src/main/java/top/untold/frostlumen/core/tween/DoubleTween.java:bindSetter"
```

The last parameter of all Tween object constructors generally determines whether it is automatically removed by the Tween scheduler after it finishes playing.

Example: Create a `ScaleOffsetTween` that smoothly moves from Scale coordinate `(0, 0)` to `(1, 1)` over 5 seconds, applies it to some node's position, and is automatically removed by the scheduler after playback finishes.

```java
ScaleOffsetTween scaleOffsetTween = new ScaleOffsetTween(ScaleOffset.ZERO, ScaleOffset.fromScale(1, 1), TweenFunctions.LINEAR, 5000L, true);

scaleOffsetTween.registerAutoCallOnSetter(node::setPosition);
// If it has not been registered with the scheduler yet
nodeRoot.getTweenScheduler().registerTween(scaleOffsetTween);
```

Example: Play a tween repeatedly forever

```java
tween.registerOnCompleteCallback(tween::play);
```

## TweenFunction

TweenFunction is the easing function. It determines the output value (ratio) at a given progress (range 0-1).

TweenFunction is also a functional interface in our library, defined as:

```java
@FunctionalInterface
public interface TweenFunction {
    double computeEased(double percent);
}
```

Here, `percent` is always 0-1, representing the current Tween percentage progress. The return value represents the mapped progress, generally 0-1. For example, Linear directly returns `percent`, meaning no change; QUAD_IN squares it once and then returns it. You can also exceed or fall below this value to create advanced effects (such as Back, which overshoots and then returns).

### Built-in Easing Function Library (TweenFunctions)

TweenFunctions is our prebuilt tween animation library. It has more than thirty types, which you can freely use and combine.

??? All Types

    | Group | Constant | Effect |
	|---|---|---|
	| Linear | `LINEAR` | Constant speed |
	| Sine | `SINE_IN` | Sine ease-in |
	| Sine | `SINE_OUT` | Sine ease-out |
	| Sine | `SINE_IN_OUT` | Sine ease-in-out |
	| Quad | `QUAD_IN` | Quadratic ease-in |
	| Quad | `QUAD_OUT` | Quadratic ease-out |
	| Quad | `QUAD_IN_OUT` | Quadratic ease-in-out |
	| Cubic | `CUBIC_IN` | Cubic ease-in |
	| Cubic | `CUBIC_OUT` | Cubic ease-out |
	| Cubic | `CUBIC_IN_OUT` | Cubic ease-in-out |
	| Quart | `QUART_IN` | Quartic ease-in |
	| Quart | `QUART_OUT` | Quartic ease-out |
	| Quart | `QUART_IN_OUT` | Quartic ease-in-out |
	| Quint | `QUINT_IN` | Quintic ease-in |
	| Quint | `QUINT_OUT` | Quintic ease-out |
	| Quint | `QUINT_IN_OUT` | Quintic ease-in-out |
	| Expo | `EXPO_IN` | Exponential ease-in |
	| Expo | `EXPO_OUT` | Exponential ease-out |
	| Expo | `EXPO_IN_OUT` | Exponential ease-in-out |
	| Circ | `CIRC_IN` | Circular ease-in |
	| Circ | `CIRC_OUT` | Circular ease-out |
	| Circ | `CIRC_IN_OUT` | Circular ease-in-out |
	| Back | `BACK_IN` | Pulls back a bit first, then moves forward |
	| Back | `BACK_OUT` | Overshoots on arrival, then settles back |
	| Back | `BACK_IN_OUT` | Pullback at both ends |
	| Elastic | `ELASTIC_IN` | Elastic ease-in |
	| Elastic | `ELASTIC_OUT` | Elastic ease-out |
	| Elastic | `ELASTIC_IN_OUT` | Elastic ease-in-out |
	| Bounce | `BOUNCE_IN` | Bounce ease-in |
	| Bounce | `BOUNCE_OUT` | Bounce ease-out |
	| Bounce | `BOUNCE_IN_OUT` | Bounce ease-in-out |
	| Other | `SMOOTHSTEP` | Smoothstep |
	| Other | `SMOOTHERSTEP` | Smootherstep |
	
	| Suffix | Meaning |
	|---|---|
	| `_IN` | Slow start |
	| `_OUT` | Slow end |
	| `_IN_OUT` | Slow at both ends |

### CustomTweenFunction

CustomTweenFunction is an important class among TweenFunctions. It implements the TweenFunction interface, so it can be passed to anywhere a TweenFunction is needed.

It has two constructors:

```java
    public CustomTweenFunction() {
        this(TweenFunctions.LINEAR);
    }
    public CustomTweenFunction(TweenFunction outOfRangeFunction) {
        setOutOfRangeFunction(outOfRangeFunction);
    }
```

The constructor parameter TweenFunction represents the function called if some interval to be used has not been registered.

It has three core methods:

```java
    public CustomTweenFunction setSegment(double start, double end, TweenFunction function) {
        tweenFunctions.put(start, end, function);
        return this;
    }

    public CustomTweenFunction setNestedSegment(double start, double end, Consumer<CustomTweenFunction> function) {
        return setNestedSegment(start, end, TweenFunctions.LINEAR, function);
    }

    public CustomTweenFunction setNestedSegment(double start, double end, TweenFunction nestedOutOfRangeFunction, Consumer<CustomTweenFunction> nestedConfigurator) {
        CustomTweenFunction function = new CustomTweenFunction(nestedOutOfRangeFunction);
        setSegment(start, end, function);
        nestedConfigurator.accept(function);
        return this;
    }
```

!!! warning

    The `start` and `end` above must be within 0-1; otherwise, they may never be called.

Among them, the first method `setSegment` has parameters: the start value of the interval, the end value of the interval, and which easing function to use for the interval. It returns `this` to facilitate chaining.

The third method `setNestedSegment` creates a new nested CustomTweenFunction and stores it in itself. Its parameters are: the start value of the interval, the end value of the interval, the function called by the new CustomTweenFunction if some interval to be used has not been registered, and the configuration function of the new CustomTweenFunction (you can continue to call these three methods on it to achieve nesting). It returns `this` to facilitate chaining.

The second method is a convenience overload of the third method; the function called by the new CustomTweenFunction if some interval to be used has not been registered is always LINEAR.

Example 1: Construct an easing function with LINEAR in the first half and QUAD_IN in the second half.

```java
CustomTweenFunction tweenFunction1 = new CustomTweenFunction()
                .setSegment(0, 0.5, TweenFunctions.LINEAR)
                .setSegment(0.5, 1, TweenFunctions.QUAD_IN);
```

Example 2: Register `tweenFunction1` from Example 1 into the first half of another CustomFunction, and use CUBIC_IN in the second half.

```java
CustomTweenFunction tweenFunction2 = new CustomTweenFunction()
                .setSegment(0, 0.5, tweenFunction1)
                .setSegment(0.5, 1, TweenFunctions.CUBIC_IN);
```

The expected effect of Example 2 is: 0-0.5 enters `tweenFunction1`; 0-0.25 (`tweenFunction1`'s 0-0.5) enters LINEAR; 0.25-0.5 (`tweenFunction1`'s 0.5-1) enters QUAD_IN; 0.5-1 enters CUBIC_IN.

Example 3: Rewrite the TweenFunction ultimately combined from Examples 1 and 2 as a directly constructed chain call.

```java
CustomTweenFunction tweenFunction3 = new CustomTweenFunction()
        .setNestedSegment(0, 0.5, function -> function
                .setSegment(0, 0.5, TweenFunctions.LINEAR)
                .setSegment(0.5, 1, TweenFunctions.QUAD_IN))
        .setSegment(0.5, 1, TweenFunctions.CUBIC_IN);
```

## Advanced Usage

### NumberTween

NumberTween is an abstract class. Primitive-type Tween classes generally directly inherit from it. It helps you manage the target time, easing function, etc. Subclasses only need to manage their own values and update their values by overriding the `applyEased(double eased)` method (generally you only need `startValue + (endValue - startValue) * eased`), reset their values by overriding `resetTween`, and provide the `getCurrentValue` method and the `registerAutoCallOnSetter` method.

!!! warning

    You generally need to call the parent class's `triggerOnUpdateCallback` method when the value is updated in the `applyEased` method; otherwise, registered update callbacks will not be triggered.

Example: IntTween class

```java
--8<-- "core/src/main/java/top/untold/frostlumen/core/tween/IntTween.java:classDefinition"
```

### ParentTween

ParentTween is an abstract class. Composite-type Tween classes generally directly inherit from it. It helps you manage children, update callbacks, etc. Subclasses generally only need to register their child Tween objects with the parent class (using the parent class's `addChildren` or `addChild` methods), and provide the `getCurrentValue` method and the `registerAutoCallOnSetter` method.

Example: ScaleOffsetTween class

```java
--8<-- "core/src/main/java/top/untold/frostlumen/core/tween/ScaleOffsetTween.java:classDefinition"
```