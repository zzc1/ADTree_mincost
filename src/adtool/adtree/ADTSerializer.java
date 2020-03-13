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

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import adtool.Choices;
import adtool.domains.Domain;
import adtool.domains.DomainFactory;
import adtool.domains.ValuationDomain;
import adtool.domains.ValueAssignement;
import adtool.domains.rings.Ring;
import adtool.ui.ADTermView;
import adtool.ui.ADTreeCanvas;
import adtool.ui.ADTreeView;
import adtool.ui.DetailsView;
import adtool.ui.DomainCanvas;
import adtool.ui.DomainView;
import adtool.ui.MainWindow;
import adtool.ui.ValuationView;

import net.infonode.docking.RootWindow;
import net.infonode.docking.View;

public class ADTSerializer
{
  private MainWindow mainWindow;
  private View[] views;

  public ADTSerializer(MainWindow mw)
  {
    mainWindow=mw;
    views=mainWindow.getViews();
  }

  private void writeOptions(ObjectOutputStream out) throws IOException
  {
     out.writeObject(Choices.canv_BackgroundColor);
     out.writeObject(Choices.canv_EdgesColor);
     out.writeObject(Choices.canv_TextColorAtt);
     out.writeObject(Choices.canv_TextColorDef);
     out.writeObject(Choices.canv_FillColorAtt);
     out.writeObject(Choices.canv_FillColorDef);
     out.writeObject(Choices.canv_BorderColorAtt);
     out.writeObject(Choices.canv_BorderColorDef);
     out.writeObject(Choices.canv_EditableColor);
     out.writeObject(Choices.canv_ShapeAtt);
     out.writeObject(Choices.canv_ShapeDef);
     out.writeObject(Choices.canv_Font);
     out.writeObject(Choices.canv_Defender);
     out.writeObject(Choices.canv_ArcSize);
     out.writeObject(Choices.canv_ArcPadding);
     out.writeObject(Choices.canv_LineWidth);
     out.writeObject(Choices.canv_DoAntialiasing);
  }

  private void readOptions(ObjectInputStream in) throws IOException,ClassNotFoundException
  {
    Choices.canv_BackgroundColor = (Color)in.readObject();
    Choices.canv_EdgesColor    = (Color)in.readObject();
    Choices.canv_TextColorAtt  = (Color)in.readObject();
    Choices.canv_TextColorDef  = (Color)in.readObject();
    Choices.canv_FillColorAtt  = (Color)in.readObject();
    Choices.canv_FillColorDef  = (Color)in.readObject();
    Choices.canv_BorderColorAtt= (Color)in.readObject();
    Choices.canv_BorderColorDef= (Color)in.readObject();
    Choices.canv_EditableColor = (Color)in.readObject();
    Choices.canv_ShapeAtt      = (Choices.ShapeType)in.readObject();
    Choices.canv_ShapeDef      = (Choices.ShapeType)in.readObject();
    Choices.canv_Font          = (Font)in.readObject();
    Choices.canv_Defender      = (ADTreeNode.Type)in.readObject();
    Choices.canv_ArcSize       = (Integer)in.readObject();
    Choices.canv_ArcPadding    = (Integer)in.readObject();
    Choices.canv_LineWidth     = (Integer)in.readObject();
    Choices.canv_DoAntialiasing= (Boolean)in.readObject();
  }

  public ADTreeForGui loadVer1(ADTreeNode root,ObjectInputStream in)
  {
    ADTreeForGui newTree=null;
    try {
      Choices.currentSaveVer=1;
      RootWindow rootWindow=mainWindow.getRootWindow();
      @SuppressWarnings("unchecked")
      Map<ADTreeNode, List<ADTreeNode>> childrenMap =(Map<ADTreeNode, List<ADTreeNode>>)in.readObject();
      @SuppressWarnings("unchecked")
      Map<ADTreeNode, ADTreeNode>       parents     = (Map<ADTreeNode, ADTreeNode>)in.readObject();
      int maxCounter = 0;
      for(ADTreeNode node:parents.keySet()){
        maxCounter = Math.max(maxCounter,node.getId());
        if(node.isAboveFolded()){
          node.setAboveFolded(false);
        }
      }
      ADTreeNode.resetCounter(maxCounter+1);
      newTree = new ADTreeForGui(root,childrenMap,parents);
      views[0].setComponent(new ADTreeView(newTree,mainWindow));
      ADTreeCanvas canvas = ((ADTreeView)views[0].getComponent()).getCanvas();
      views[1].setComponent(new ADTermView(canvas));
      views[2].setComponent(new ValuationView());
      views[3].setComponent(new DetailsView());
      readOptions(in);
      Boolean savedLayout = (Boolean)in.readObject();
      mainWindow.removeDomains();
      Vector<Integer>domainsIds=null;
      if (savedLayout){
        rootWindow.read(in, true);
        domainsIds=new Vector<Integer>();
        for (View v : mainWindow.getDynamicViews().values()) {
          domainsIds.add(new Integer(((DomainView<?>) v.getComponent()).getId()));
        }
      }
      ((ADTreeView)views[0].getComponent()).getCanvas().setFocus(null);
      ((ValuationView)views[2].getComponent()).assignCanvas(null);
      ((DetailsView)views[3].getComponent()).assignCanvas(null);
      Integer noDomains = (Integer)in.readObject();
      mainWindow.getValuations().clear();
      for(int i = 0; i < noDomains; i++){
        @SuppressWarnings("unchecked")
        Domain<Ring>  d = DomainFactory.updateDomain((Domain<Ring>)in.readObject());//in case domain class is obsolete
        ValuationDomain<Ring> vd= new ValuationDomain<Ring>(d);
        @SuppressWarnings("unchecked")
        ValueAssignement<Ring> vass=(ValueAssignement<Ring>)in.readObject();
        vd.setValueAssPro(vass,root);
        @SuppressWarnings("unchecked")
        ValueAssignement<Ring> vass2=(ValueAssignement<Ring>)in.readObject();
        vd.setValueAssOpp(vass2,root);
        if (!savedLayout){
          mainWindow.getValuations().put(new Integer(i),vd);
          View view = mainWindow.getDynamicView(i);
          System.out.println("Adding domain window when loading");
          mainWindow.addDomainWindow(view);
        }
        else{
          mainWindow.getValuations().put(domainsIds.elementAt(i),vd);
        }
      }
      newTree.updateAllSizes();
      Choices.currentSaveVer=-1;
    }
    catch (final IOException e) {
      mainWindow.getStatusBar().reportError(e.getMessage());
    }
    catch(ClassNotFoundException ex)
    {
      ex.printStackTrace();
    }
    return newTree;
  }

  public void loadVerN(Integer verNo,ObjectInputStream in) throws IOException,ClassNotFoundException
  {
    if (verNo==2){
      String description = (String)in.readObject();
      @SuppressWarnings("unchecked")
      HashMap<ADTreeNode, String> comments = (HashMap<ADTreeNode, String>)in.readObject();
      Object o = in.readObject();
      if (o instanceof ADTreeNode){//old version of save
        ADTreeForGui tree =loadVer1((ADTreeNode)o,in);
        if (tree!=null){
          tree.setDescription(description);
          tree.setComments(comments);
        }
      }
    }
    else{
      mainWindow.getStatusBar().reportError("Loading not implemented for version of the save: "+verNo);
    }
  }

  public void loadFromStreamTree(ObjectInputStream in)
  {
    if (in != null) {
      try {
        Object o = in.readObject();
        if (o instanceof ADTreeNode){
          loadVer1((ADTreeNode)o,in);//old non versioned save type assumed to be version 1
          mainWindow.updateDynamicViewTitles();

        }
        else if (o instanceof Integer){
          loadVerN((Integer)o,in); //version no 2
        }
        //((ADTreeView)views[0].getComponent()).getCanvas().newTree();
        in.close();
      }
      catch (final IOException e) {
        mainWindow.getStatusBar().reportError(e.getMessage());
      }
      catch(ClassNotFoundException ex)
      {
        ex.printStackTrace();
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void saveToStreamTree(ObjectOutputStream out)
  {
    if (out != null) {
      try {
        HashMap<Integer,MainWindow.DynamicView> dynamicViews = mainWindow.getDynamicViews();
        ADTreeForGui tree = ((ADTreeView) views[0].getComponent()).getCanvas().getTree();
        out.writeObject(Choices.saveVersion);
        out.writeObject(tree.getDescription());
        out.writeObject(tree.getComments());
        out.writeObject(tree.getRoot(true));
        out.writeObject(tree.getChildrenMap());
        out.writeObject(tree.getParents());
        writeOptions(out);
        out.writeObject(new Boolean(Choices.main_saveLayout));
        if (Choices.main_saveLayout) {
          mainWindow.getRootWindow().write(out, true);
        }
        out.writeObject(new Integer(dynamicViews.size()));
        for (View v : dynamicViews.values()) {
          out.writeObject(((DomainView<Ring>) v.getComponent()).getCanvas()
              .getDomain());
          out.writeObject(((DomainView<Ring>) v.getComponent()).getCanvas()
              .getValueAssPro());
          out.writeObject(((DomainView<Ring>) v.getComponent()).getCanvas()
              .getValueAssOpp());
        }
        out.close();
      }
      catch (final IOException e2) {
        mainWindow.getStatusBar().reportError(e2.getMessage());
      }
    }
  }
}

