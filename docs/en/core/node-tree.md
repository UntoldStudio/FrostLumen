# Node Tree

This chapter explains the library's internal node tree model.

## ParentNode

All nodes, including the root node, directly or indirectly inherit from ParentNode. It holds a children list. Therefore, all nodes can have children; a leaf node is simply a node without children.

## GuiNode

GuiNode inherits from ParentNode, so it also has a children list. It also has a parent field and a root field.

## NodeRoot

NodeRoot inherits from ParentNode, so it also has a children list. Unlike GuiNode, as the node root, it has no parent, and its root is itself.

A NodeRoot instance has a `runTask(Runnable task)` method. Registered tasks will be executed before the next render.

## Children

When you call the addChild method on an instance of ParentNode and pass in a node instance, it does the following:

1. If the node already has a parent: call the parent's removeChild method, passing in the node.

2. Recursively set the node's root field to its own root field.

3. Set node.parent to this.

4. Add the node to the children list.

5. Sort the children list in ascending order by the children's zIndex (smaller zIndex comes first; if zIndex is the same, insertion order is used).

6. Have the node recalculate its own position.

If you look at the source code, you will find that ParentNode does not have the behavior of step 2, but its only two sealed subclasses both override this method to recursively set root.

ParentNode also has a convenience overload, addChildren, which uses varargs (GuiNode...) and iterates over the varargs list calling addChild.

When you call the removeChild method on an instance of ParentNode and pass in a node instance, it only does the following:

If it really has this child, then remove the node from the children list, set the node.parent field to null, and recursively set the root of it and its children to null.

ParentNode also has a convenience overload, removeChildren, which also uses varargs (GuiNode...) and iterates over the varargs list calling removeChild.

Externally, the child node list can be obtained via getChildren() (an immutable view wrapped by Collections.unmodifiableList).

## Order in Which Various Events Are Triggered on the Node Tree

Rendering and notification events (such as window maximize/minimize) traverse children in forward order (parent nodes are processed first, then child nodes; purpose: child nodes can be drawn directly on top of parent nodes during rendering, and notification events being processed by parent nodes first allows parent nodes to update themselves before updating children).

Cancelable events such as mouse clicks and keyboard input traverse children in reverse order (child nodes are processed first, then parent nodes; purpose: the topmost nodes can receive mouse events, keyboard events, etc. first; otherwise, it would be very counterintuitive for the user to click the mouse and have the bottommost rendered node respond).

!!! warning

    zIndex only affects the ordering of all child nodes under the same parent.

    All GLFW events will be scheduled to the rendering thread using runTask for processing.

Below is a node tree for demonstration:

```text
root
    -Frame1(zIndex=0)
        -Frame11(zIndex=-10)
        -Frame12(zIndex=1)
    -Frame2(zIndex=1)
        -Frame21(zIndex=100)
        -Frame22(zIndex=101)
```

Rendering order:

Frame1-Frame11-Frame12-Frame2-Frame21-Frame22

Mouse/keyboard event receiving order (if any node cancels the event, dispatch stops):

Frame22-Frame21-Frame2-Frame12-Frame11-Frame1