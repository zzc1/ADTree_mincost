/**
 * Author: Piotr Kordy (piotr.kordy@uni.lu <mailto:piotr.kordy@uni.lu>)
 * Date:   06/06/2013
 * Copyright (c) 2013,2012 University of Luxembourg -- Faculty of Science,
 *     Technology and Communication FSTC
 * All rights reserved.
 * Licensed under GNU Affero General Public License 3.0;
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package adtool.domains;

import adtool.adtree.ADTNode;
import adtool.adtree.Node;

import java.util.HashMap;
import java.util.Vector;

/**
 * Evaluate value of each node for given domain and value assignement map.
 */
public class Evaluator<Type>
{
  private Domain<Type> domain;
  private HashMap<ADTNode,Type> resultMap;
  /**
   * Constructs a new instance.
   * @param newDomain domain.
   */
  public Evaluator(final Domain<Type> newDomain)
  {
    resultMap=null;
    this.domain = newDomain;
  }

  public final Type getValue(final ADTNode node)
  {
    if (resultMap == null || node == null) {
      return null;
    }
    else {
      return resultMap.get(node);
    }
  }

  public final boolean reevaluate(final ADTNode root,
      final ValueAssignement<Type> newProMap, final ValueAssignement<Type> newOppMap)
  {
    if (newProMap == null||root == null || newOppMap == null) {
      System.out.println("null result");
      return false;
    }
    resultMap = new HashMap<ADTNode,Type>();
    evaluate(root,newProMap,newOppMap);
    return true;
  }

  /**
   * Do bottom up evaluation.
   * 
   * @param root
   * @return
   */
  private Type evaluate(final ADTNode root,ValueAssignement<Type> proMap,
      final ValueAssignement<Type> oppMap)
  {
    Type result = null;
    //leaf
    if (root.getType() == ADTNode.Type.LEAFO){
      if (oppMap.get(root.getName())==null){
        return domain.getDefaultValue(false);   
      }
      else{
        resultMap.put(root,oppMap.get(root.getName()));
        return oppMap.get(root.getName());
      }
    }
    else if(root.getType() == ADTNode.Type.LEAFP) {
      if (proMap.get(root.getName())==null){
        return domain.getDefaultValue(true);   
      }
      else{
        resultMap.put(root,proMap.get(root.getName()));
        return proMap.get(root.getName());
      }
    }
    else {
      final Vector<Node> children = root.getChildren();
      if (children == null) {
        System.err.println("Error in bottom up evaluation - "
            + "non leaf node without children");
        return null;
      }
      if (children.size() == 0) {
        System.err.println("Error in bottom up evaluation - "
            + "non leaf node without children");
        return null;
      }
      result = null;
      switch (root.getType()) {
        case OO:
          for (Node c:children){
            if(result == null){
              result = evaluate((ADTNode) children.elementAt(0),proMap,oppMap);
            }
            else{
              result = domain.oo(result,evaluate((ADTNode) c,proMap,oppMap));
            }
          }
          break;
        case AO:
          for (Node c:children){
            if(result == null){
              result = evaluate((ADTNode) children.elementAt(0),proMap,oppMap);
            }
            else{
              result = domain.ao(result,evaluate((ADTNode) c,proMap,oppMap));
            }
          }
          break;
        case OP:
          for (Node c:children){
            if(result == null){
              result = evaluate((ADTNode) children.elementAt(0),proMap,oppMap);
            }
            else{
              result = domain.op(result,evaluate((ADTNode) c,proMap,oppMap));
            }
          }
          break;
        case AP:
          for (Node c:children){
            if(result == null){
              result = evaluate((ADTNode) children.elementAt(0),proMap,oppMap);
            }
            else{
              result = domain.ap(result,evaluate((ADTNode) c,proMap,oppMap));
            }
          }
          break;
        case CO:
          if (children.size() != 2){
            System.err.println("Error in bottom up evaluation - "
                +" counter node with more than 2 children.");
            return null;
          }
          result = domain.co(
                evaluate((ADTNode) children.elementAt(0), proMap, oppMap),
              evaluate((ADTNode) children.elementAt(1),proMap,oppMap));
          break;
        case CP:
          if (children.size() != 2){
            System.err.println("Error in bottom up evaluation - "
                + "counter node with more than 2 children.");
            return null;
          }
          result = domain.cp(
              evaluate((ADTNode) children.elementAt(0), proMap, oppMap),
              evaluate((ADTNode) children.elementAt(1), proMap, oppMap));
          break;
        default:
          System.err.println("Error in bottom up evaluation - "
              + "Unknown node type.");
          return null;
      }
    }
    resultMap.put(root,result);
    return result;
  }
}

