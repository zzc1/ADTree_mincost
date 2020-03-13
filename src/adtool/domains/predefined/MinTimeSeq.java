package adtool.domains.predefined;

import adtool.domains.Domain;

import adtool.domains.rings.RealG0;

public class MinTimeSeq implements Domain<RealG0>
{
  //number 4
  static final long serialVersionUID = 465945232556446844L;
 
  public MinTimeSeq()
  {
  }

  public final RealG0 getDefaultValue(final boolean proponent)
  {
    if  (proponent){
      return new RealG0(0);
    }
    else{
      return new RealG0(0);
    }
  }

  public final boolean isValueModifiable(final boolean proponent)
  {
    //return proponent;
	  return true;
  }

  public String getName()
  {
    return "�����ɹ���Ҫ����Сʱ��(������ִ��)";
  }

  public String getDescription()
  {
    final String name = "�����ɹ���Ҫ����Сʱ��(������ִ��)";
    final String vd = "&#x211D;\u208A\u222A{\u221E}";
    final String[] operators = { "min(<i>x</i>,<i>y</i>)",
                                "<i>x</i>&nbsp;+&nbsp;<i>y</i>",
                                "<i>x</i>&nbsp;+&nbsp;<i>y</i>",
                                "min(<i>x</i>,<i>y</i>)",
                                "<i>x</i>&nbsp;+&nbsp;<i>y</i>",
                                "min(<i>x</i>,<i>y</i>)",};
    return DescriptionGenerator.generateDescription(this, name, vd, operators);
  }


  public final RealG0 op(final RealG0 a, final RealG0 b)
  {
    return RealG0.min(a,b);
  }

 
  public final RealG0 ap(final RealG0 a, final RealG0 b)
  {
    return RealG0.sum(a,b);
  }


  public final RealG0 oo(final RealG0 a,final  RealG0 b)
  {
    return RealG0.sum(a,b);
  }


  public final RealG0 ao(final RealG0 a, final RealG0 b)
  {
    return RealG0.min(a,b);
  }


  public final RealG0 cp(final RealG0 a,final  RealG0 b)
  {
    return RealG0.sum(a,b);
  }

  public final RealG0 co(final RealG0 a, final RealG0 b)
  {
    return RealG0.min(a,b);
  }
}


