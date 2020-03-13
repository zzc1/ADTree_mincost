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

import adtool.Choices;
import adtool.domains.ValuationDomain;
import adtool.domains.predefined.Parametrized;
import adtool.domains.rings.Ring;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ADTXmlExport
{
  private ADTreeForGui tree;
  private HashMap<Integer, ValuationDomain<Ring>> domains;

  public ADTXmlExport(ADTreeForGui node,HashMap<Integer, ValuationDomain<Ring>> map)
  {
    this.tree = node;
    this.domains=map;
  }
  Element transform(ADTreeNode node,Document doc)
  {
    Element result = doc.createElement("node");
    Element label = doc.createElement("label");
    label.insertBefore(doc.createTextNode(node.getLabel()), label.getLastChild());
    result.appendChild(label);
    if (node.getRefinmentType()==ADTreeNode.RefinementType.DISJUNCTIVE){
      result.setAttribute("refinement","disjunctive");
    }
    else{
      result.setAttribute("refinement","conjunctive");
    }
    if (node.getLevel()==0&&
        Choices.canv_Defender==ADTreeNode.Type.OPPONENT){ //changed role for root and tree
      result.setAttribute("switchRole","yes");
    }
    if (tree.getParent(node)!=null&&node.getType()!=tree.getParent(node).getType()){
      result.setAttribute("switchRole","yes");
    }
    List<ADTreeNode> children=tree.getChildrenList(node,true);
    ADTNode term=node.getTerm();
    //adding values from domains
    if(Choices.main_saveDomains){
      for (Integer i:domains.keySet()){
        ValuationDomain<?> vd=domains.get(i);
        String domainId=vd.getDomain().getClass().getSimpleName()+new Integer(i.intValue()+1).toString();
        if(term.isEditable(vd.getDomain())){//modifable values
          if(vd.getValue(node)!=null){
            result.appendChild(createParameter(doc,domainId,"basic",vd.getValue(node).toString()));
          }
        }
        else{
          if(Choices.main_saveDerivedValues){
            if ((term.getType() == ADTNode.Type.CP || term.getType() == ADTNode.Type.CO)
                && ((ADTNode)term.getChildren().elementAt(0)).getChildren().size()==0) {
              String value = vd.getTermValue((ADTNode) term.getChildren().elementAt(0)).toString();
              result.appendChild(createParameter(doc,domainId,"default",value));
            }
            else {
              result.appendChild(createParameter(doc,domainId,"derived",vd.getTermValue(node.getTerm()).toString()));
            }
          }
        }
        if ((term.getType() == ADTNode.Type.CP || term.getType() == ADTNode.Type.CO)
             && ((ADTNode)term.getChildren().elementAt(0)).getChildren().size()==0
             && Choices.main_saveDerivedValues && vd.isShowAllLabels()) {
            result.appendChild(createParameter(doc,domainId,"derived",vd.getTermValue(node.getTerm()).toString()));
        }
      }
    }
    Iterator<ADTreeNode> iterator=children.iterator();
    while (iterator.hasNext()) {
      result.appendChild(transform(iterator.next(),doc));
    }
    return result;
  }
  private Element createParameter(Document doc,String domainId,String category,String content)
  {
     Element parameter = doc.createElement("parameter");
//      String domainId=vd.getDomain().getClass().getSimpleName()+new Integer(i.intValue()+1).toString();
     parameter.setAttribute("domainId",domainId);
     if(!category.equals("basic")){
       parameter.setAttribute("category",category);
     }
     parameter.setTextContent(content);
     return parameter;
  }
  private void exportDomains(Document doc,Element rootElement){
    for (Integer i:domains.keySet()){
      ValuationDomain<?> vd=domains.get(i);
      Element domainNode = doc.createElement("domain");
      Element classNode = doc.createElement("class");
      Element toolNode = doc.createElement("tool");
      classNode.setTextContent(vd.getDomain().getClass().getName());
      toolNode.setTextContent("ADTool");
      domainNode.appendChild(classNode);
      String domainId=vd.getDomain().getClass().getSimpleName()+new Integer(i.intValue()+1).toString();
      domainNode.setAttribute("id",domainId);
      if(vd.getDomain() instanceof Parametrized){
        Element paramNode = doc.createElement("range");
        paramNode.setTextContent(((Parametrized)vd.getDomain()).getParameter().toString());
        domainNode.appendChild(paramNode);
      }
      domainNode.appendChild(toolNode);
      rootElement.appendChild(domainNode);
    }
  }
  /**
   * Exports tree into the xml file.
   *
   * @param fileStream stream to which we write - we assume stream is open and
   * we close it.
   */
  public void exportTo(FileOutputStream fileStream)
  {
    if (tree==null) return;
    try{
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      StreamSource stream = new StreamSource(getClass().getClassLoader().getResourceAsStream("adtree.xsd"));
      Schema schema=schemaFactory.newSchema(stream);
      factory.setSchema(schema);
      Validator validator = schema.newValidator();
      factory.setNamespaceAware(true);
      factory.setValidating(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      //builder.setEntityResolver(new EntityManager());
      Document doc = builder.newDocument();
      doc.setXmlStandalone(true);
      Element rootElement = doc.createElement("adtree");
		  doc.appendChild(rootElement);

      Element node = transform(tree.getRoot(true),doc);
      rootElement.appendChild(node);
      //exporting domains
      if (Choices.main_saveDomains){
        exportDomains(doc,rootElement);
      }
      //validating
      validator.validate(new DOMSource(doc));
      // write the content into xml file
      prettyPrint(doc,fileStream);
      fileStream.close();
    }
    catch (ParserConfigurationException pce) {
      pce.printStackTrace();
	  }
    catch (SAXException e) {
      e.printStackTrace();
	  }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static final void prettyPrint(Document xml,FileOutputStream out) {
    try{
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        tf.transform(new DOMSource(xml), new StreamResult(out));
    }
    catch (TransformerException tfe) {
      tfe.printStackTrace();
    }
  }
}
