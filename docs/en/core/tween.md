# Tween

This chapter explains how to use Tween (tween animation) in the library.

## Core Concepts

Essentially, no Tween is bound to any UI. It is fundamentally just a value that grows over time, and you can use it anywhere.

A Tween consists of two parts: a Tween object (the carrier of data and some operations) and a TweenFunction (the easing function, which determines how the value changes over time).

## Tween Scheduler

The Tween scheduler is a carrier that schedules a bunch of Tweens. In our library, NodeRoot has a Tween scheduler, which you can obtain via `getTweenScheduler`.

Each time the scheduler's `step()` method is called, it calls `step(delta)` on every registered Tween. A Tween only advances its value when stepped.

You can register/unregister Tweens with the scheduler using the following methods:

```java
public void registerTween(Tween tween) {...}
public void registerTweens(Tween... tweens) {...}
public void unregisterTween(Tween tween) {...}
public void unregisterTweens(Tween... tweens) {...}
```

!!! warning

    NodeRoot automatically steps its own Tween scheduler when rendering; you do not need to step it manually.

## Tween Objects

A Tween object is a carrier that holds data and grows its value over time. Currently, the library has three basic-type Tween objects: `DoubleTween`, `FloatTween`, and `IntTween`, and two composite-type Tween objects: `ScaleOffsetTween` and `RGBATween`.

A Tween object should have a method that returns its own value, such as the `getCurrentValue` method of the five objects listed above.

The Tween base class has methods such as `pause()`, `resume()`, `reset()`, and `play()`.

pause: pause the Tween

resume: resume the Tween

reset: reset the Tween playback progress

play: calls `reset()` and `resume()`

You can call `setAlwaysRepeat` to set whether to repeat forever, and call `setRemainingRepeatCount` to set how many times the Tween repeats (not including the first time).

!!! warning

    The time unit of a Tween object is milliseconds.
    A newly created Tween will not automatically `play()`; you need to call it manually.

Example: DoubleTween's setter binding method (`bindSetter`)

```java
--8<-- "core/src/main/java/top/untold/frostlumen/core/tween/DoubleTween.java:getCurrentValue"
        
--8<-- "core/src/main/java/top/untold/frostlumen/core/tween/DoubleTween.java:bindSetter"
```

The last parameter of the constructor of all Tween objects generally determines whether it is automatically removed by the Tween scheduler after it finishes playing.

Example: Create a `ScaleOffsetTween` that smoothly moves from Scale coordinate (0, 0) to (1, 1) over 5 seconds, and have some node's position apply it; it will be automatically removed by the scheduler after playback finishes.

```java
ScaleOffsetTween scaleOffsetTween = new ScaleOffsetTween(ScaleOffset.ZERO, ScaleOffset.fromScale(1, 1), 5000L, TweenFunctions.LINEAR, true);

scaleOffsetTween.bindSetter(node::setPosition);
// If it has not yet been registered with the scheduler
nodeRoot.getTweenScheduler().registerTween(scaleOffsetTween);

scaleOffsetTween.play();
```

Example: Make a tween repeat forever

```java
tween.setAlwaysRepeat(true);
```

## Easing Functions (TweenFunction)

TweenFunction is an easing function. It determines what value (ratio) is output at a given progress (range 0-1).

In our library, TweenFunction is also a functional interface, defined as:

```java
@FunctionalInterface
public interface TweenFunction {
    double computeEased(double percent);
}
```

Here, `percent` is always 0-1, representing the current Tween percentage progress. The return value represents the mapped progress, generally 0-1. For example, Linear directly returns `percent`, meaning no change; QUAD_IN squares it once before returning. You can also go above/below this range for advanced effects (such as Back, which overshoots and then returns).

### Built-in Easing Function Library (TweenFunctions)

TweenFunctions is our preset tween animation library. It has more than thirty types, which you can use and combine freely.

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
	| Back | `BACK_IN` | Pulls back a bit at the start before moving forward |
	| Back | `BACK_OUT` | Overshoots on arrival then returns |
	| Back | `BACK_IN_OUT` | Pullback at both ends |
	| Elastic | `ELASTIC_IN` | Elastic oscillation ease-in |
	| Elastic | `ELASTIC_OUT` | Elastic oscillation ease-out |
	| Elastic | `ELASTIC_IN_OUT` | Elastic oscillation at both ends |
	| Bounce | `BOUNCE_IN` | Bounce ease-in |
	| Bounce | `BOUNCE_OUT` | Bounce ease-out |
	| Bounce | `BOUNCE_IN_OUT` | Bounce at both ends |
	| Other | `SMOOTHSTEP` | Smooth step |
	| Other | `SMOOTHERSTEP` | Smoother step |
	
	| Suffix | Meaning |
	|---|---|
	| `_IN` | Slow start |
	| `_OUT` | Slow end |
	| `_IN_OUT` | Slow at both ends |

### CustomTweenFunction

CustomTweenFunction is an important class in TweenFunction. It implements the TweenFunction interface, so it can be passed to any place that needs a TweenFunction.

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

    The `start` and `end` above must be within 0-1; otherwise they may never be called.

Among them, the first method `setSegment`'s parameters are: the start value of the interval, the end value of the interval, which easing function to use for the interval; returns `this` for convenient chaining.

The third method `setNestedSegment` creates a new nested `CustomTweenFunction` and stores it in itself. Its parameters are: the start value of the interval, the end value of the interval, the function called when some interval to be used by the new `CustomTweenFunction` has not been registered, and the configuration function for the new `CustomTweenFunction` (you can continue calling these three methods on it to achieve nesting); returns `this` for convenient chaining.

The second method is a convenience overload of the third method; the function called when some interval to be used by the new `CustomTweenFunction` has not been registered is always LINEAR.

Example 1: Construct an easing function whose first half is LINEAR and second half is QUAD_IN

```java
CustomTweenFunction tweenFunction1 = new CustomTweenFunction()
                .setSegment(0, 0.5, TweenFunctions.LINEAR)
                .setSegment(0.5, 1, TweenFunctions.QUAD_IN);
```

Example 2: Register `tweenFunction1` from Example 1 into the first half of another CustomFunction, and use CUBIC_IN for the second half

```java
CustomTweenFunction tweenFunction2 = new CustomTweenFunction()
                .setSegment(0, 0.5, tweenFunction1)
                .setSegment(0.5, 1, TweenFunctions.CUBIC_IN);
```

The expected effect of Example 2 is: 0-0.5 enters `tweenFunction1`; 0-0.25 (`tweenFunction1`'s 0-0.5) enters LINEAR; 0.25-0.5 (`tweenFunction1`'s 0.5-1) enters QUAD_IN; 0.5-1 enters CUBIC_IN.

Example 3: Rewrite the final combined TweenFunction from Examples 1 and 2 as a direct chained construction

```java
CustomTweenFunction tweenFunction3 = new CustomTweenFunction()
        .setNestedSegment(0, 0.5, function -> function
                .setSegment(0, 0.5, TweenFunctions.LINEAR)
                .setSegment(0.5, 1, TweenFunctions.QUAD_IN))
        .setSegment(0.5, 1, TweenFunctions.CUBIC_IN);
```

## Advanced Usage

### NumberTween

NumberTween is an abstract class. Basic-type Tween classes generally inherit directly from it. It helps you manage target time, easing functions, etc. Subclasses only need to manage their own value and update their value by overriding the `applyEased(double eased)` function (generally you only need `startValue + (endValue - startValue) * eased`), reset their value by overriding `resetTween`, and provide `getCurrentValue` and `bindSetter` methods.

!!! warning

    You generally need to call the parent class's `triggerOnUpdateCallback` method in the `applyEased` method when the value is updated; otherwise, registered update callbacks will not be triggered.

Example: IntTween class

```java
--8<-- "core/src/main/java/top/untold/frostlumen/core/tween/IntTween.java:classDefinition"
```

### CompositeTween

CompositeTween: composite-type Tween classes generally inherit directly from it. It helps you manage children, update callbacks, manage bound setters, etc. Subclasses generally only need to register their child Tween objects with the parent class (using the parent class's `addChildren` or `addChild` methods), provide a `getCurrentValue` method, and fill the parent class's generic parameter with the type of the Tween's value.

Example: ScaleOffsetTween class

```java
--8<-- "core/src/main/java/top/untold/frostlumen/core/tween/ScaleOffsetTween.java:classDefinition"
```