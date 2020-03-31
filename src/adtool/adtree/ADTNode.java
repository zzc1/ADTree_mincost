package adtool.adtree;

import adtool.Choices;
import adtool.domains.Domain;

import java.util.Vector;

/**
 * An ADTNode.
 */
public class ADTNode extends SimpleNode
{
  /**
   * Name of the node.
   */
  private String name;
  protected Vector<Node> children;
  /**
   * Type of node.
   */
  public enum Type
  {
    OO, AO, OP, AP, CO, CP, LEAFO, LEAFP
  }

  private Type type;

  /**
   * Constructor.
   * 
   * @param id
   *          the id
   */
  public ADTNode(final int id)
  {
    super(id);
    name = "";
    children = new Vector<Node>();
  }

  /**
   * Gets the children for this instance.
   *
   * @return The children.
   */
  public Vector<Node> getChildren()
  {
    return children;
  }


  /**
   * Gets the name for this instance.
   *
   * @return The name.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Sets the type of a node.
   * 
   * @param t
   *          the type
   */
  public final void setType(final Type t)
  {
    type = t;
  }

  /**
   * Set the name.
   * 
   * @param n
   *          the name
   */
  public final void setName(final String n)
  {
    name = n;
  }

  /**
   * Sets the children for this instance.
   *
   * @param children The children.
   */
  public void setChildren(Vector<Node> children)
  {
    this.children = children;
  }

  /**
   * Gets the id for this instance.
   *
   * @return The id.
   */
  public int getId()
  {
    return this.id;
  }

  /**
   * Gets the type for this instance.
   *
   * @return The type.
   */
  public Type getType()
  {
    return this.type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.javacc.examples.jjtree.eg2.SimpleNode#toString()
   */

  public String toString()
  {
    return toString(0);
  }
  /**
   * Removes all children of a node.
   * 
   * @param node node of which children should be removed.
   */
  public final void removeAllChildren(final Node node)
  {
    for (Node n:getChildren()){
      removeAllChildren(n);
    }
    getChildren().clear();
  }
  /**
   * Removes a node.
   * 
   * @param child node to be removed from the list of children.
   */
  public final void removeChild(final Node child)
  {
    if (children == null){
      System.err.println("Tried to remove child from node with no children");
      return;
    }
    final int index = getChildren().indexOf(child);
    if (index == -1) {
      System.err.println("Tried to remove child from that"
          + " is not contained in children");
      return;
    }
    final Vector<Node> newChildren = ((ADTNode)child).getChildren();
    getChildren().remove(index);
    getChildren().addAll(index,newChildren);
    for(Node c:newChildren){
      c.jjtSetParent(this);
    }
  }
  /**
   * Adds a child at a specified index and assigs to it number of children.
   * 
   * @param type type of the added child
   * @param name name of the added child
   * @param indexAt index at which we add child (from 0 to number of children
   * parent has)
   * @param noChildren number of children to be transfered.
   */
  public final void addChildAt(Type type,
                               String name, final int indexAt, final int noChildren)
  {
    final ADTNode child = new ADTNode(-1);
    child.setType(type);
    child.setName(name);
    Vector<Node> newChildren=null;
    //System.out.println("Adding node:"+child+ " No Children:"+noChildren);
    //Vector<Node> children =getChildren();
    if (noChildren >0){
      //if (children.size()<(indexAt+noChildren)){
        //children.setSize(indexAt+noChildren);
      //}
      newChildren = new Vector<Node>(children.subList(
        indexAt, indexAt + noChildren));
      this.children.removeAll(newChildren);
      for(Node n:newChildren){
        if(n!=null){
          n.jjtSetParent(child);
        }
      }
    }
    this.children.add(indexAt,child);
    child.jjtSetParent(this);
    ((ADTNode)child).setChildren(newChildren);

  }
  /**
   * Performs a check if the tree node is editable. 
   * 
   * @param domain domain under which we check the node editability.
   * @return true if we can modify the node and false otherwise.
   */
  public final boolean isEditable(final Domain<?>  domain)
  {
	boolean result = false;
    if(type == Type.LEAFO && domain.isValueModifiable(false)){
      result = true;
    }
    else if(type == Type.LEAFP && domain.isValueModifiable(true)){
      result = true;
    }
    else if(type == Type.CP){
      final Type childType = ((ADTNode)getChildren().elementAt(0)).getType();
      result = childType == Type.LEAFP
              && domain.isValueModifiable(true);
    }
    else if(type == Type.CO){
      final Type childType = ((ADTNode)getChildren().elementAt(0)).getType();
      result = childType == Type.LEAFO
                && domain.isValueModifiable(false);
    }
    //return true;
   return result;
  }
  /**
   * Pretty print a string representation of a tree.
   * 
   * @param level
   * @return
   */
  private String toString(final int level)
  {
    String result = "";
    String indent = "";
    String currIndent = "";
    String eol = "";
    if (Choices.adt_multiLine) {
      for (int i = 0; i < Choices.adt_indentLevel; i++) {
        indent += " ";
      }
      for (int i = 0; i < level; i++) {
        currIndent += indent;
      }
      eol = "\n";
    }
    if (children != null) {
      for (int i = 0; i < children.size(); ++i) {
        final ADTNode n = (ADTNode) children.elementAt(i);
        if (n != null) {
          result += n.toString(level + 1);
          if ((i + 1) < children.size()) {
            result += ",";
          }
          result += eol;
        }
        else{
          System.err.println("Null child at index:"+i);
        }
      }
    }
    switch (type) {
      case OO:
        result = currIndent + "oo(" + eol + result + currIndent + ")";
        break;
      case AO:
        result = currIndent + "ao(" + eol + result + currIndent + ")";
        break;
      case OP:
        result = currIndent + "op(" + eol + result + currIndent + ")";
        break;
      case AP:
        result = currIndent + "ap(" + eol + result + currIndent + ")";
        break;
      case CO:
        result = currIndent + "co(" + eol + result + currIndent + ")";
        break;
      case CP:
        result = currIndent + "cp(" + eol + result + currIndent + ")";
        break;
      case LEAFO:
        result = currIndent + name;
        break;
      case LEAFP:
        result = currIndent + name;
        break;
      default:
        result = "unknown";
        break;
    }
    return result;
  }

  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Vector<Node>(i + 1);
    }
    else if (i >= children.size()) {
      children.setSize(i+1);
    }
    children.set(i,n);
    n.jjtSetParent(this);
  }

  public final Node jjtGetChild(int i) {
    return children.elementAt(i);
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.size();
  }
  public void dump(String prefix) {
    System.out.println(toString(prefix));
    if (children != null) {
      for (int i = 0; i < children.size(); ++i) {
        final SimpleNode n = (SimpleNode)children.elementAt(i);
        if (n != null) {
          n.dump(prefix + " ");
        }
      }
    }
  }
}
