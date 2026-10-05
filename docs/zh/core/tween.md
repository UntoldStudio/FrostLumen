# Tween

本章节将会讲解库内的Tween(补间动画)的使用方式

## 核心概念

所有Tween本质不绑定任何UI,它本质只是一个根据时间增长的值,你可以将它用在任何地方

Tween由Tween对象(数据和部分操作的载体)与TweenFunction(缓动函数,决定如何随着时间变化)两部分组成

## Tween调度器

Tween调度器是一个调度一堆Tween的载体.在我们的库中,NodeRoot就有一个Tween调度器,你可以通过`getTweenScheduler`来获取

调度器每次被调用step()方法的时候都会调用注册的每个Tween的step(delta),Tween只有被step才会推进值

!!! warning

    NodeRoot在渲染时会自动步进它自己的Tween调度器,你不需要手动步进

## Tween对象

Tween对象是承载数据的载体,它跟随时间增长自己的值.目前库内拥有`DoubleTween` `FloatTween` `IntTween`三种基本类型的Tween对象
与`ScaleOffsetTween` `RGBATween` 两种复合类型的Tween对象

一个Tween对象应有能返回自己值的方法,如上列五种对象的`getCurrentValue`方法,一个绑定Setter的方法

!!! warning

    Tween对象的时间单位是毫秒

示例:DoubleTween的getCurrentValue方法与绑定Setter的方法(`registerAutoCallOnSetter`)

```java
--8<-- "core/src/main/java/top/untold/frostlumen/core/tween/DoubleTween.java:getCurrentValue"
        
--8<-- "core/src/main/java/top/untold/frostlumen/core/tween/DoubleTween.java:bindSetter"
```

所有Tween对象的构造函数最后一个参数一般决定它在播放完后是否被Tween调度器自动移除

示例:创建一个ScaleOffsetTween,使用5秒时间从Scale坐标(0, 0)平滑移到(1, 1),并让某node的position应用它,播放完成后自动被调度器移除

```java
ScaleOffsetTween scaleOffsetTween = new ScaleOffsetTween(ScaleOffset.ZERO, ScaleOffset.fromScale(1, 1), TweenFunctions.LINEAR, 5000L, true);

scaleOffsetTween.registerAutoCallOnSetter(node::setPosition);
//如果还没注册到调度器的话
nodeRoot.getTweenScheduler().registerTween(scaleOffsetTween);
```

示例:永久重复播放某tween

```java
tween.registerOnCompleteCallback(tween::play);
```

## 缓动函数(TweenFunction)

TweenFunction是缓动函数,它决定进度(范围0-1)在某处的时侯输出的值(比例)是多少

TweenFunction在我们的库中还是一个函数式接口,定义为:

```java
@FunctionalInterface
public interface TweenFunction {
    double computeEased(double percent);
}
```

其中,percent恒为0-1,代表当前Tween百分比进度,返回值代表映射后的进度,一般是0-1,比如Linear直接返回percent表示不做改变,QUAD_IN先做一次平方再返回,你也可以超过/低于这个值以做高级效果(如Back,冲过再返回)

### 内置缓动函数库(TweenFunctions)

TweenFunctions是我们的预制补间动画库,它拥有三十多种类型,你可以自由使用,自由搭配

??? 所有类型

    | 组 | 常量 | 效果 |
	|---|---|---|
	| Linear | `LINEAR` | 匀速 |
	| Sine | `SINE_IN` | 正弦加速进入 |
	| Sine | `SINE_OUT` | 正弦减速退出 |
	| Sine | `SINE_IN_OUT` | 正弦两端缓入缓出 |
	| Quad | `QUAD_IN` | 二次加速进入 |
	| Quad | `QUAD_OUT` | 二次减速退出 |
	| Quad | `QUAD_IN_OUT` | 二次两端缓入缓出 |
	| Cubic | `CUBIC_IN` | 三次加速进入 |
	| Cubic | `CUBIC_OUT` | 三次减速退出 |
	| Cubic | `CUBIC_IN_OUT` | 三次两端缓入缓出 |
	| Quart | `QUART_IN` | 四次加速进入 |
	| Quart | `QUART_OUT` | 四次减速退出 |
	| Quart | `QUART_IN_OUT` | 四次两端缓入缓出 |
	| Quint | `QUINT_IN` | 五次加速进入 |
	| Quint | `QUINT_OUT` | 五次减速退出 |
	| Quint | `QUINT_IN_OUT` | 五次两端缓入缓出 |
	| Expo | `EXPO_IN` | 指数加速进入 |
	| Expo | `EXPO_OUT` | 指数减速退出 |
	| Expo | `EXPO_IN_OUT` | 指数两端缓入缓出 |
	| Circ | `CIRC_IN` | 圆形加速进入 |
	| Circ | `CIRC_OUT` | 圆形减速退出 |
	| Circ | `CIRC_IN_OUT` | 圆形两端缓入缓出 |
	| Back | `BACK_IN` | 起步先往回拉一下再前进 |
	| Back | `BACK_OUT` | 到达时冲过头再退回 |
	| Back | `BACK_IN_OUT` | 两端都有回拉 |
	| Elastic | `ELASTIC_IN` | 弹性震荡进入 |
	| Elastic | `ELASTIC_OUT` | 弹性震荡退出 |
	| Elastic | `ELASTIC_IN_OUT` | 弹性震荡两端 |
	| Bounce | `BOUNCE_IN` | 弹跳进入 |
	| Bounce | `BOUNCE_OUT` | 弹跳退出 |
	| Bounce | `BOUNCE_IN_OUT` | 弹跳两端 |
	| Other | `SMOOTHSTEP` | 平滑阶跃 |
	| Other | `SMOOTHERSTEP` | 更平滑的阶跃 |
	
	| 后缀 | 含义 |
	|---|---|
	| `_IN` | 慢启动 |
	| `_OUT` | 慢结束 |
	| `_IN_OUT` | 两端都慢 |

### CustomTweenFunction

CustomTweenFunction是TweenFunction中重要的类.它实现了TweenFunction接口,因此它可以传给任何需要TweenFunction的地方

它有两个构造函数:

```java
    public CustomTweenFunction() {
        this(TweenFunctions.LINEAR);
    }
    public CustomTweenFunction(TweenFunction outOfRangeFunction) {
        setOutOfRangeFunction(outOfRangeFunction);
    }
```

构造参数TweenFunction代表如果某个要用的区间没有被注册所调用的函数

它有三个核心方法:

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

    上面的start,end必须在0-1之内,否则可能永远不会被调用

其中,第一个方法`setSegment`的参数有:该区间开始值,该区间结束值,该区间使用什么缓动函数,返回this方便链式调用

第三个方法`setNestedSegment`会新建一个嵌套的CustomTweenFunction存入自己,参数有:该区间开始值,该区间结束值,新的CustomTweenFunction某个要用的区间没有被注册所调用的函数,新的CustomTweenFunction的配置函数(你可以在那上面继续调用该三个方法,实现嵌套),返回this方便链式调用

第二个方法是第三个方法的便捷重载,新的CustomTweenFunction某个要用的区间没有被注册所调用的函数恒为LINEAR

示例1:构造一个前半段LINEAR,后半段QUAD_IN的缓动函数

```java
CustomTweenFunction tweenFunction1 = new CustomTweenFunction()
                .setSegment(0, 0.5, TweenFunctions.LINEAR)
                .setSegment(0.5, 1, TweenFunctions.QUAD_IN);
```

示例2:将示例1的tweenFunction1注册到另外一个CustomFunction的前半段,后半段使用CUBIC_IN

```java
CustomTweenFunction tweenFunction2 = new CustomTweenFunction()
                .setSegment(0, 0.5, tweenFunction1)
                .setSegment(0.5, 1, TweenFunctions.CUBIC_IN);
```

示例2的预期效果为:0-0.5进入tweenFunction1,0-0.25(tweenFunction1的0-0.5)进入LINEAR,0.25-0.5(tweenFunction1的0.5-1)进入QUAD_IN,0.5-1进入CUBIC_IN

示例3:将示例1和示例2最终组合的TweenFunction改成链式调用直接构造

```java
CustomTweenFunction tweenFunction3 = new CustomTweenFunction()
        .setNestedSegment(0, 0.5, function -> function
                .setSegment(0, 0.5, TweenFunctions.LINEAR)
                .setSegment(0.5, 1, TweenFunctions.QUAD_IN))
        .setSegment(0.5, 1, TweenFunctions.CUBIC_IN);
```

## 高级用法

### NumberTween

NumberTween是一个抽象类,基本类型的Tween类一般直接继承他.它会帮你管理目标时间,缓动函数等,子类仅需管理自己的值并且通过重写`applyEased(double eased)`函数来更新自己的值(一般来说你只需要startValue + (endValue - startValue) * eased),通过重写`resetTween`来重置自己的值,提供`getCurrentValue`方法与`registerAutoCallOnSetter`方法

!!! warning

    你一般需要在`applyEased`方法中,值更新的时候调用父类的`triggerOnUpdateCallback`方法,否则注册的更新回调不会触发

示例:IntTween类

```java
--8<-- "core/src/main/java/top/untold/frostlumen/core/tween/IntTween.java:classDefinition"
```

### ParentTween

ParentTween是一个抽象类,复合类型的Tween类一般直接继承他,它会帮你管理子项,更新回调等,子类一般仅需把自己的子Tween对象注册给父类(使用父类的`addChildren`或`addChild`方法),提供`getCurrentValue`方法与`registerAutoCallOnSetter`方法

示例:ScaleOffsetTween类

```java
--8<-- "core/src/main/java/top/untold/frostlumen/core/tween/ScaleOffsetTween.java:classDefinition"
```