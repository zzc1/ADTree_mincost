package adtool.domains;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;
import java.util.Vector;

import org.reflections.Reflections;

import adtool.domains.predefined.MinCost;
import adtool.domains.predefined.MinTimeSeq;
import adtool.domains.rings.Ring;

public abstract class DomainFactory
{
  private static final String[] oldNames={
    "RealG0MinSumCost",
    "RealG0MinSum",
    "RealG0MaxSum",
  };
  private static final String[] newNames={
    "MinCost",
    "MinTimeSeq",
  };
  private static final String domainsPrefix
    = "adtool.domains.predefined";

  /**
   * Constructs a new instance.
   */
  public DomainFactory()
  {
  }
  public static Boolean isObsolete(Domain<Ring> domain)
  {
    return isObsolete(domain.getClass().getSimpleName());
  }

  public static Boolean isObsolete(String domainName)
  {
    if (Arrays.asList(oldNames).contains(domainName)){
      return true;
    }
    else{
      return false;
    }
  }
  public static Domain<Ring> updateDomain(Domain<Ring> d){
    if (isObsolete(d)){
      String newName=updateDomainName(d.getClass().getSimpleName());
      return createFromString(updateDomainName(newName));
    }
    else return d;
  }
  public static String updateDomainName(String name)
  {
    String result = name;
    for(int i=0; i<oldNames.length; i++){
      result=result.replace(oldNames[i],newNames[i]);
    }
    return result;
  }

  /**
   * Creates predefined domain from string name.
   *
   * @param domainName domain class name
   * @return created domain.
   */
  @SuppressWarnings("unchecked")
  public static Domain<Ring> createFromString(String domainName)
  {
    String name=domainName;
   // if(!domainName.startsWith(domainsPrefix)){
     // name=domainsPrefix+"."+domainName;
    //}
    Constructor<Domain<Ring>>[] ct=null;
    try{
      final Class<?> c = Class.forName(name);
      ct = (Constructor<Domain<Ring>>[]) c.getDeclaredConstructors();
    }
    catch(ClassNotFoundException e){
      System.err.println("Class with name "+name+" not found");
      return null;
    }
    Domain<Ring> d = null;
    if (ct.length == 1) {
        try {
			d = ct[0].newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
    }
    return d;
  }
  /**
   * Get domain class name as string.
   *
   * @param d domain.
   * @return domain class name.
   */
  public static String getClassName(Domain<Ring> d){
    return d.getClass().getSimpleName();
  }

  /**
   * Returns string list of predefined domains.
   *
   * @return array of strings with domain names.
   */
  @SuppressWarnings("all")
  public static Vector<Domain<?>> getPredefinedDomains()
  {
    Vector<Domain<?>> result = new Vector<Domain<?>>();
    Reflections reflections = new Reflections(domainsPrefix);
    Set<Class<? extends Domain>> m = reflections.getSubTypesOf(Domain.class);
    for (Class<? extends Domain> c : m) {
      Domain<Ring> d = null;
      Constructor<Domain<Ring>>[] ct = (Constructor<Domain<Ring>>[]) c
          .getDeclaredConstructors();
      try {
        if (ct.length == 1) {
          d = ct[0].newInstance();
          if(!DomainFactory.isObsolete(d)){
            result.add((Domain<Ring>)d);
          }
        }
      }
      catch (InstantiationException e) {
        System.err.println(e);
        return null;
      }
      catch (IllegalAccessException e) {
        System.err.println(e);
        return null;
      }
      catch (InvocationTargetException e) {
        System.err.println(e);
        return null;
      }
    }
    if (result.size()==0){
      result.add(new MinTimeSeq());
      result.add(new MinCost());
    }
    return result;
  }
}

