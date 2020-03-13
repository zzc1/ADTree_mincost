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
package adtool.adtree;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import adtool.Choices;
import adtool.domains.Domain;
import adtool.domains.DomainFactory;
import adtool.domains.ValueAssignement;
import adtool.domains.predefined.Parametrized;
import adtool.domains.rings.Ring;
import adtool.ui.ADTermView;
import adtool.ui.ADTreeCanvas;
import adtool.ui.ADTreeView;
import adtool.ui.DomainCanvas;
import adtool.ui.DomainView;
import adtool.ui.MainWindow;

import net.infonode.docking.View;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ADTXmlImport
{
  private MainWindow mainWindow;

  public ADTXmlImport(MainWindow window)
  {
    this.mainWindow=window;
  }
  /**
   * Imports tree from the xml file.
   *
   * @param fileStream stream from which we read - we assume stream is open and
   * we close it.
   */
  public void importFrom(FileInputStream fileStream)
  {
    try{
      StreamSource stream = new StreamSource(getClass().getClassLoader().getResourceAsStream("adtree.xsd"));
      Schema schema=SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(stream);
      Validator validator = schema.newValidator();
      DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
      factory.setSchema(schema);
      factory.setNamespaceAware(true);
      factory.setValidating(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setErrorHandler(new DefaultHandler());
      Document doc=builder.parse(fileStream);
      doc.getDocumentElement().normalize();
      validator.validate(new DOMSource(doc));
      Node treeNode=doc.getElementsByTagName("adtree").item(0);
      NodeList list = treeNode.getChildNodes();
      mainWindow.removeDomains();
      Vector<HashMap<String,HashMap<String,String>>> values=null;
      for(int i=0; i<list.getLength();i++ ){
        Node n=list.item(i);
        if(n.getNodeType() == Node.ELEMENT_NODE){
          Element e=(Element)n;
          if(e.getNodeName().equals("node")){
            values=importTree(e,list.getLength()-1);
          }
          else if(e.getNodeName().equals("domain")){
            addDomain(e,values);
          }
          mainWindow.createAttrDomainMenu();
        }
      }

      fileStream.close();
    }
    catch (ParserConfigurationException pce) {
      mainWindow.getStatusBar().reportError(pce.getMessage());
	  }
    catch (SAXException e) {
      mainWindow.getStatusBar().reportError(e.getMessage());
	  }
    catch (IOException e) {
      mainWindow.getStatusBar().reportError(e.getMessage());
    }
  }

  private void addValuations(HashMap<String,String> map, ValueAssignement<Ring> va)
  {
    if(map==null||va==null){
      return;
    }
    Collection<String> keys=map.keySet();
    for (String label:keys) {
      String value = map.get(label);
      Ring ring = (Ring)va.get(label);
      Ring defaultValue=(Ring)ring.clone();
      if(ring!=null){
        if(ring.updateFromString(value)){
          va.put(label,ring);
        }
        else{
          mainWindow.getStatusBar().reportWarning("Invalid value:\""+value+ "\" for node with label:\""+label+"\". Using default value.");
          va.put(label,defaultValue);
        }
      }
      else{
        mainWindow.getStatusBar().reportWarning("Cannot specify value for non refined node with label:"+label);
      }
    }
  }
  private void addDomain(Element e,Vector<HashMap<String,HashMap<String,String>>> values)
  {
    NodeList tools=e.getElementsByTagName("tool");
    boolean isADTool=false;
    for (int i = 0; i < tools.getLength(); ++i) {
      Element tool = (Element) tools.item(i);
      if("ADTOOL".equals(tool.getTextContent().trim().toUpperCase())){
        isADTool=true;
      }
    }
    if(!isADTool){
      return;
    }
    Domain<Ring> d = DomainFactory.createFromString(getChildTag(e,"class").getTextContent());
    System.out.println(getChildTag(e,"class").getTextContent());
    if(d instanceof Parametrized){
      Element child = getChildTag(e,"range");
      String parameter=null;
      if (child!=null){
        parameter=getChildTag(e,"range").getTextContent();
      }
      if (parameter ==null){
        mainWindow.getStatusBar().reportError("Domain "
            +getChildTag(e,"class").getTextContent()
            +" does not have required <range> tag.");
        return;
      }
      if(((Parametrized)d).getParameter() instanceof Integer){
        if (parameter.equals("Infinity")){
          ((Parametrized)d).setParameter(Integer.MAX_VALUE);
        }
        else{
          ((Parametrized)d).setParameter(Integer.parseInt(parameter));
        }
      }
      else{
        mainWindow.getStatusBar().reportError("Unknown type of parameter for the domain "+getChildTag(e,"class").getTextContent());
      }
    }
    if (d!=null){
      ADTNode root = ((ADTermView)mainWindow.getViews()[1].getComponent()).getTerms();
      View view=mainWindow.addAttributeDomain(d);
      @SuppressWarnings("unchecked")
      DomainCanvas<Ring> canvas = ((DomainView<Ring>)view.getComponent()).getCanvas();
      canvas.getValueAssPro().setDefault(true,root,d);
      if(d.isValueModifiable(true)){
        addValuations(values.elementAt(0).get(e.getAttribute("id")),canvas.getValueAssPro());
      }
      canvas.getValueAssOpp().setDefault(false,root,d);
      if(d.isValueModifiable(false)){
        addValuations(values.elementAt(1).get(e.getAttribute("id")),canvas.getValueAssOpp());
      }
      canvas.getTree().updateAllSizes();
      canvas.valuesUpdated();
    }
    else{
      mainWindow.getStatusBar().reportError("Failed to import domain "+getChildTag(e,"class").getTextContent()+".");
    }
  }

  private ADTreeNode.Type getType(Element e,ADTreeNode parent){
    if (e.getAttribute("switchRole").equals("yes")){
      if(parent.getType()==ADTreeNode.Type.OPPONENT){
        return ADTreeNode.Type.PROPONENT;
      }
      else{
        return ADTreeNode.Type.OPPONENT;
      }
    }
    else{
      return parent.getType();
    }
  }

  private ADTreeNode.RefinementType getRefinement(Element e){
    if (e.getAttribute("refinement").equals("conjunctive")){
      return ADTreeNode.RefinementType.CONJUNCTIVE;
    }
    else{
      return ADTreeNode.RefinementType.DISJUNCTIVE;
    }
  }
  private Collection<Element> getChildrenTag(Node n,String tag)
  {
    Vector<Element> result = new Vector<Element>();
    NodeList list=n.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
		  if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element eChild = (Element)list.item(i);
        if (eChild.getNodeName()==tag){
          result.add(eChild);
        }
      }
    }
    return result;
  }
  private Element getChildTag(Node n,String tag)
  {
    NodeList list=n.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
		  if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element eChild = (Element)list.item(i);
        if (eChild.getNodeName()==tag){
          return eChild;
        }
      }
    }
    return null;
  }
  private Vector<HashMap<String,HashMap<String,String>>> importTree(Element e,int noDomains){
    Vector<HashMap<String,HashMap<String,String>>> result=new Vector<HashMap<String,HashMap<String,String>>>(2);
    for (int i=0;i<2;i++){
      result.add(i,new HashMap<String,HashMap<String,String>>(2));
    }
    ADTreeCanvas canvas = ((ADTreeView)mainWindow.getViews()[0].getComponent()).getCanvas();
    canvas.newTree();
    ADTreeNode root=canvas.getRoot(true);
    root.setRefinementType(getRefinement(e));
    if (e.getAttribute("switchRole").equals("yes")){
      Choices.canv_Defender=ADTreeNode.Type.OPPONENT;
    }
    else{
      Choices.canv_Defender=ADTreeNode.Type.PROPONENT;
    }
    addChildren(e,root,result);
    String label = getChildTag(e,"label").getTextContent();
    canvas.setLabel(root,label);
    storeParameters(e,root,result);
    canvas.getTree().updateAllSizes();
    return result;
  }

  private void addChildren(Element e, ADTreeNode node,Vector<HashMap<String,HashMap<String,String>>> result){
    ADTreeCanvas canvas = ((ADTreeView)mainWindow.getViews()[0].getComponent()).getCanvas();
    Collection<Element> list=getChildrenTag(e,"node");
    for (Element eChild:list) {
      ADTreeNode child = new ADTreeNode(getType(eChild,node),getRefinement(eChild));
      if (eChild.getAttribute("switchRole").equals("yes")){
        canvas.getTree().addCounter(node,child);
      }
      else{
        canvas.getTree().addChild(node,child);
      }
      child.setLabel(getChildTag(eChild,"label").getTextContent());
      storeParameters(eChild,child,result);
      addChildren(eChild,child,result);
    }
  }
  private void storeParameters(Element e, ADTreeNode node,Vector<HashMap<String,HashMap<String,String>>> result){
    Collection<Element> listParam=getChildrenTag(e,"parameter");
    int j=1;//1 if opponent; 0 if proponent
    if(node.getType()==ADTreeNode.Type.PROPONENT){
      j=0;
    }
    for (Element eParam:listParam) {
      if (eParam.getAttribute("category").equals("basic")){
        String idDomain=eParam.getAttribute("domainId");
        if(!result.elementAt(j).containsKey(idDomain)){
          result.elementAt(j).put(idDomain,new HashMap<String,String>());
        }
        result.elementAt(j).get(idDomain).put(node.getLabel(),eParam.getTextContent());
      }
    }
  }

}
