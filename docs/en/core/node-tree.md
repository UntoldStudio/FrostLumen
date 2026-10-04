# Node Tree

This chapter will explain the node tree model inside the library.

## ParentNode

All nodes, including the root node, directly or indirectly inherit from ParentNode. It holds a children list. Therefore, all nodes can have children; a leaf node is simply a node with no children.

## GuiNode

GuiNode inherits from ParentNode, so it also has a children list. It also has a parent field and a root field.

## NodeRoot

NodeRoot inherits from ParentNode, so it also has a children list. Unlike GuiNode, as the node root, it has no parent, and its root is itself.

## Children

When you call the addChild method on an instance of ParentNode and pass in a node instance, it will do the following:

1. If the node already has a parent: call the parent's removeChild method, passing in the node.
2. Recursively set the node's root field to its own root field.
3. Set node.parent to this.
4. Add the node to the children list.
5. Sort the children list in ascending order by the children's zIndex (smaller zIndex comes first; if zIndex is the same, insertion order is used).
6. Make the node recalculate its own position.

If you look at the source code, you will find that ParentNode does not have the behavior of step 2, but both of its only two subclasses permitted by sealed override this method to recursively set root.

ParentNode also has a convenience addChildren overload, which uses varargs (GuiNode...), iterates over this varargs list, and calls addChild.

When you call the removeChild method on an instance of ParentNode and pass in a node instance, it only does the following:

If it really has this child, then remove the node from the children list, set the node.parent field to null, and recursively set the root of it and its children to null.

ParentNode also has a convenience removeChildren overload, which also uses varargs (GuiNode...), iterates over this varargs list, and calls removeChild.

Externally, you can get the child node list through getChildren() (an immutable view wrapped by Collections.unmodifiableList).

## Order in Which Various Events Are Triggered on the Node Tree

Rendering and notification events (such as window maximize/minimize) traverse children in forward order (parent nodes are processed first, then child nodes; purpose: child node rendering can be drawn directly on top of parent nodes, and for notification events, processing parent nodes first allows parent nodes to update themselves before updating their children).

Cancellable events such as mouse clicks and keyboard input traverse children in reverse order (child nodes are processed first, then parent nodes; purpose: topmost nodes can receive mouse events, keyboard events, etc. first; otherwise, it would be very counterintuitive if a user's mouse click were responded to by the node rendered at the very bottom).

Note: zIndex only affects the ordering of all child nodes under the same parent.

The following is an example node tree for demonstration:

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

Mouse/keyboard event receiving order (if any node cancels the event, distribution stops):

Frame22-Frame21-Frame2-Frame12-Frame11-Frame1