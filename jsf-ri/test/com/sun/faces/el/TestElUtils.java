package com.sun.faces.el;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

public class TestElUtils extends TestCase {
    
    private final static Random  rnd           = new Random();

  // .(?:[ ]+|[\[{,(])cc[.].+[}]
  public void testIsCompositeComponentExpr() {
    assertFalse(ELUtils.isCompositeComponentExpr("crap"));

    assertTrue(ELUtils.isCompositeComponentExpr("#{cc.test}"));

  }

  // .[{]cc[.]attrs[.]\w+[}]
  // #{cc.attrs.myaction}
  public void testIsCompositeComponentMethodExprLookup() {
    assertFalse(ELUtils.isCompositeComponentMethodExprLookup("crap"));
    
    assertTrue(ELUtils.isCompositeComponentMethodExprLookup("#{cc.attrs.test}"));
    
    // why OK?
  }

  // (?:[ ]+|[\[{,(])cc[.]attrs[.]\w+[(].+[)]
  // legal:     #{cc.attrs.label('foo')}
  // illegal:   #{cc.attrs.bean.label('foo')}
  public void testIsCompositeComponentLookupWithArgs() {
    assertFalse(ELUtils.isCompositeComponentLookupWithArgs("crap"));
    assertFalse(ELUtils.isCompositeComponentLookupWithArgs("#{cc.attrs.bean.label('foo')}"));
    
    assertTrue(ELUtils.isCompositeComponentLookupWithArgs("#{cc.attrs.label('foo')}"));
    assertTrue(ELUtils.isCompositeComponentLookupWithArgs("${cc.attrs.label('param1', 'param2')}"));
    assertTrue(ELUtils.isCompositeComponentLookupWithArgs(" # { cc.attrs.label('foo') }"));
    
    // why OK?
    assertTrue(ELUtils.isCompositeComponentLookupWithArgs("#{cc.attrs.label('foo')"));
    assertTrue(ELUtils.isCompositeComponentLookupWithArgs(" X { cc.attrs.label(foo)}"));
    assertTrue(ELUtils.isCompositeComponentLookupWithArgs("#{cc.attrs.label('foo'))}"));
    assertTrue(ELUtils.isCompositeComponentLookupWithArgs("#,cc.attrs.label( )unmatched"));
    assertTrue(ELUtils.isCompositeComponentLookupWithArgs(" cc.attrs.label(foo)"));
  }
  
  public void testPerformance() {
      int expectedUniqueExpressions = 10000;
      int loop = 500000;
      
      List<String> argsExpressions = new ArrayList<String>();
      List<String> ccExpressions = new ArrayList<String>();
      List<String> methExpressions = new ArrayList<String>();
      for (int i=expectedUniqueExpressions; i > 0; i--) {
          argsExpressions.add("#{cc.attrs.mo('" + rndString(64)+ "')}");
          ccExpressions.add("#{cc."+rndString(64)+"}");
          methExpressions.add("#{cc.attrs."+rndString(64)+"}");
      }
      long start;
      boolean result = true;
      
      ELUtils.useCaching = false;
      // warmup
      for (int i=0; i< loop; i++) {
          int n = rnd.nextInt(expectedUniqueExpressions);
          result &= ELUtils.isCompositeComponentLookupWithArgs(argsExpressions.get(n));
          result &= ELUtils.isCompositeComponentExpr(ccExpressions.get(n));
          result &= ELUtils.isCompositeComponentMethodExprLookup(methExpressions.get(n));
      }
      
      start = System.currentTimeMillis();
      for (int i=0; i< loop; i++) {
          int n = rnd.nextInt(expectedUniqueExpressions);
          result &= ELUtils.isCompositeComponentLookupWithArgs(argsExpressions.get(n));
          result &= ELUtils.isCompositeComponentExpr(ccExpressions.get(n));
          result &= ELUtils.isCompositeComponentMethodExprLookup(methExpressions.get(n));
      }
      long durationUncached = System.currentTimeMillis()-start;
      
      ELUtils.useCaching = true;
      
      start = System.currentTimeMillis();
      for (int i=0; i< loop; i++) {
          int n = rnd.nextInt(expectedUniqueExpressions);
          result &= ELUtils.isCompositeComponentLookupWithArgs(argsExpressions.get(n));
          result &= ELUtils.isCompositeComponentExpr(ccExpressions.get(n));
          result &= ELUtils.isCompositeComponentMethodExprLookup(methExpressions.get(n));
      }
      long durationCached = System.currentTimeMillis()-start;
      
      assertTrue(result && (durationCached < durationUncached));
  }
  
  public final static String rndString(int length) {
    final CharSequence chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0, cl = chars.length(); i < length; i++) {
      sb.append(chars.charAt(rnd.nextInt(cl)));
    }
    return sb.toString();
  }

}
