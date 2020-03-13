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
import adtool.adtree.ADTreeNode;
import adtool.adtree.Node;
import adtool.domains.rings.Ring;
public class ValuationDomain<Type>
{
  static final long serialVersionUID = 665464411570251703L;
  private Domain<Type> domain;
  transient private Evaluator<Type> evaluator;
  private ValueAssignement<Type> valueAssPro;
  private ValueAssignement<Type> valueAssOpp;
  private boolean showAllLabels; //this should be in the domainCanvas but it would require not easy changes in XML export

  public ValuationDomain(final Domain<Type> newDomain){
    this.domain=newDomain;
    this.evaluator = new Evaluator<Type>(domain);
    this.valueAssPro = new ValueAssignement<Type>();
    this.valueAssOpp = new ValueAssignement<Type>();
    this.showAllLabels=true; 
  }

  public Type getValue(ADTreeNode node)
  {
    Type result =null;
    String key = node.getTerm().getName();
    if(node.getType() == ADTreeNode.Type.PROPONENT){
      result = valueAssPro.get(key);
    }
    else {
      result = valueAssOpp.get(key);
    }
    return result;
  }

  public final Type getTermValue(final ADTNode node)
  {
    return evaluator.getValue(node);
  }

  public void setValue(boolean proponent, String key,Type value){
    if(proponent){
      valueAssPro.put(key,value);
    }
    else{
      valueAssOpp.put(key,value);
    }
  }
  public boolean hasEvaluator()
  {
    return evaluator!=null;
  }

  public void refreshAllValues(ADTreeNode root)
  {
    ValueAssignement<Type> proNew = new ValueAssignement<Type>();
    ValueAssignement<Type> oppNew = new ValueAssignement<Type>();
    refreshAllValues(root.getTerm(),proNew,oppNew);
    valueAssPro = proNew;
    valueAssOpp = oppNew;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void refreshAllValues(final ADTNode node, final ValueAssignement proNew, final ValueAssignement oppNew)
  {
    String name = node.getName();
    if (node.getType() == ADTNode.Type.LEAFP){
      Ring value = (Ring)valueAssPro.get(name);
      if (value == null){
        proNew.put(name, getDomain().getDefaultValue(true));
      }
      else{
        proNew.put(name,value);
      }
    }
    else if (node.getType() == ADTNode.Type.LEAFO){
      Ring value = (Ring)valueAssOpp.get(name);
      if (value == null){
        oppNew.put(name, getDomain().getDefaultValue(false));
      }
      else{
        oppNew.put(name,value);
      }
    }
    else {
      if (node.getChildren() != null) {
        for(Node c:node.getChildren()){
          if (c != null) {
            refreshAllValues((ADTNode)c,proNew,oppNew);
          }
        }
      }
    }
  }

  public void valuesUpdated(ADTreeNode root)
  {
    evaluator.reevaluate(root.getTerm(),valueAssPro,valueAssOpp);
  }

  public void treeChanged(ADTreeNode root)
  {
    this.valueAssPro.setDefault(true,root.getTerm(),domain);
    this.valueAssOpp.setDefault(false,root.getTerm(),domain);
    this.evaluator.reevaluate(root.getTerm(), valueAssPro, valueAssOpp);
    this.refreshAllValues(root);
  }
  /**
   * Gets the domain for this instance.
   *
   * @return The domain.
   */
  public Domain<Type> getDomain()
  {
    return domain;
  }
  /**
   * Sets the valueAssPro for this instance.
   *
   * @param newValueAssPro new value assignement.
   */
  public void setValueAssPro(ValueAssignement<Type> newValueAss,ADTreeNode root)
  {
    this.valueAssPro = newValueAss;
    evaluator.reevaluate(root.getTerm(),valueAssPro,valueAssOpp);
  }

   /* Sets the valueAssOpp for this instance.
   *
   * @param newValueAssOpp new value assignement.
   */
  public void setValueAssOpp(ValueAssignement<Type> newValueAss,ADTreeNode root)
  {
    this.valueAssOpp = newValueAss;
    evaluator.reevaluate(root.getTerm(),valueAssPro,valueAssOpp);
  }

  /**
   * Gets the valueAssPro for this instance.
   *
   * @return The valueAssPro.
   */
  public ValueAssignement<Type> getValueAssPro()
  {
    return this.valueAssPro;
  }
   /** Gets the valueAssOpp for this instance.
   *
   * @return The valueAssOpp.
   */
  public ValueAssignement<Type> getValueAssOpp()
  {
    return this.valueAssOpp;
  }

  /**
   * @return the showAllLabels
   */
  public boolean isShowAllLabels()
  {
    return showAllLabels;
  }

  /**
   * @param showAllLabels the showAllLabels to set
   */
  public void setShowAllLabels(boolean showAllLabels)
  {
    this.showAllLabels = showAllLabels;
  }

}
