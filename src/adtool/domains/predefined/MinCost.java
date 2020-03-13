package adtool.domains.predefined;

/**
 * A Domain defined on booleans.
 *
 * @author Piot Kordy
 */
public class MinCost extends MinTimeSeq
{
  //number 6
  static final long serialVersionUID = 665945232556446846L;

  public MinCost()
  {
    super();
  }

  public final String getName()
  {
    return "��С��������";
  }

  public final String getDescription()
  {
    final String name = "�����ɹ�����С���� ";
    final String vd = "&#x211D;\u208A\u222A{\u221E}";
    final String[] operators = { "min(<i>x</i>,<i>y</i>)",
                                "<i>x</i>&nbsp;+&nbsp;<i>y</i>",
                                "<i>x</i>&nbsp;+&nbsp;<i>y</i>",
                                "min(<i>x</i>,<i>y</i>)",
                                "<i>x</i>&nbsp;+&nbsp;<i>y</i>",
                                "min(<i>x</i>,<i>y</i>)",};
    return DescriptionGenerator.generateDescription(this, name, vd, operators);
  }
}
