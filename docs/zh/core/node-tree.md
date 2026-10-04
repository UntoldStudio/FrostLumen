# 节点树

本章节将会讲解库内部的节点树模型

## ParentNode

所有节点,包括根节点都直接或间接继承ParentNode.它持有children列表.因此所有节点都可以有子项,叶子节点仅仅是没有子项的节点

## GuiNode

GuiNode继承ParentNode,因此也有children列表.它还拥有parent字段与root字段.

## NodeRoot

NodeRoot继承ParentNode,因此它也有children列表.与GuiNode不同,它作为节点根,没有parent,root就是他自己.

## Children

当你调用了ParentNode的某个实例的addChild方法并传入了某node实例的时候,它会做这些事:

1.如果node已有父项:调用父项的removeChild方法,传入node

2.递归将node的root字段设置为自己的root字段

3.把node.parent设置为this

4.把node添加进children列表

5.按子项的zIndex升序(zIndex小的在前面,zIndex一样则按插入顺序)排序children列表

6.让node重新计算自己的位置

如果你翻看源码会发现ParentNode没有第二步的行为,但是它通过sealed的唯二子类都重写了这个方法递归设置root

ParentNode还有一个addChildren便捷重载,它使用变长参数(GuiNode...),遍历这个变长参数列表调用addChild

当你调用了ParentNode的某个实例的removeChild方法并传入了某node实例后,它只会做这些:

如果自己真的有这个子项,那么将node从children列表移除,node.parent字段置空,递归设置它和它的子项的root为null

ParentNode还有一个removeChildren便捷重载,它也使用变长参数(GuiNode...),遍历这个变长参数列表调用removeChild

外部可以通过getChildren()拿到子节点列表(Collections.unmodifiableList包装的不可变视图)

## 各种事件在节点树上触发的顺序

渲染和通知性事件(比如窗口最大化最小化)按正序遍历children(父节点先处理,子节点再处理,目的:子节点渲染能直接画在父节点之上,且通知性事件父节点先处理可以让父节点先更新自己再更新子项)

鼠标点击,键盘输入等可取消事件按反序遍历children(子节点先处理,父节点再处理,目的:最上层节点能优先接收到鼠标事件,键盘事件等,否则用户点击鼠标是在最底层渲染的节点响应很反直觉)

注意:zIndex只影响同一个父项下所有子节点的排序

以下是一个演示用的节点树

```text
root
    -Frame1(zIndex=0)
        -Frame11(zIndex=-10)
        -Frame12(zIndex=1)
    -Frame2(zIndex=1)
        -Frame21(zIndex=100)
        -Frame22(zIndex=101)
```

渲染顺序:

Frame1-Frame11-Frame12-Frame2-Frame21-Frame22

鼠标/键盘事件接受顺序(任意一个节点取消了事件会停止分发)

Frame22-Frame21-Frame2-Frame12-Frame11-Frame1