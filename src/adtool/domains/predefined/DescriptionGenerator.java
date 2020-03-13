package adtool.domains.predefined;

import adtool.domains.Domain;

import adtool.domains.rings.Ring;

public class DescriptionGenerator
{
  public static String generateDescription(Domain<?> d, String name,String valueDomain, String[] oper)
  {
    return   "<html><table align=\"left\" border=0>"
            + "<tr><th align=\"left\">"+name+"</th></tr>"
            + "<tr align=\"left\"><table border=0 cellpadding=7>"
            + "<tr border=0><th align=\"left\">ȡֵ��Χ:</th>"
            + "    <td colspan=1><nbr>"+valueDomain+"</nbr></td></tr></table></tr>"
            + "<tr><table border=0>"
            + "<tr><th></th><th border=0>������</th><td border=0>&nbsp;&nbsp;&nbsp;&nbsp;</td><th border=0>������</th></tr>"
            + "<tr><th align=\"left\">OR</th><td colspan=2 border=0><b>op&nbsp; </b>" +   oper[0]
            + "</td><td border=0><b>oo&nbsp; </b>" + oper[2] + "</td></tr>"
            + "<tr><th align=\"left\">AND</th><td colspan=2 border=0><b>ap&nbsp; </b>" +   oper[1]
            + "</td><td border=0><b>ao&nbsp; </b>" + oper[3] + "</td></tr>"
            + "<tr><th align=\"left\">COUNTER</th><td colspan=2 border=0><b>cp&nbsp; </b>"+oper[4]
            + "</td><td border=0><b>co&nbsp; </b>" + oper[5] + "</td></tr>"
            + "<tr><th align=\"left\" border=0>Ĭ��ֵ&nbsp;&nbsp;</th><td colspan=2 border=0>"+((Ring)d.getDefaultValue(true)).toUnicode()
            + "</td><td border=0>"+((Ring)d.getDefaultValue(false)).toUnicode()+"</td></tr>"
            + "<tr><th align=\"left\" border=0>�Ƿ���޸�</th><td colspan=2 border=0>"+(d.isValueModifiable(true)?"��":"��")+"</td>"
            + "<td border=0>"+(d.isValueModifiable(false)?"��":"��")+"</td></tr>"
            + "</table></tr>"
            + "<tr align=\"left\"><table border=0 cellpadding=1>"
            + "</table>"
            + "</html>";
  }
}
