package com.sun.faces.el;

import junit.framework.TestCase;

public class TestElUtils extends TestCase {

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
  }

  // (?:[ ]+|[\[{,(])cc[.]attrs[.]\w+[(].+[)]
  // legal: #{cc.attrs.label('foo')}
  // illegal:   #{cc.attrs.bean.label('foo')}
  public void testIsCompositeComponentLookupWithArgs() {
    assertFalse(ELUtils.isCompositeComponentLookupWithArgs("crap"));
    assertFalse(ELUtils.isCompositeComponentLookupWithArgs("#{cc.attrs.bean.label('foo')}"));
    
    assertTrue(ELUtils.isCompositeComponentLookupWithArgs("#{cc.attrs.label('foo')}"));
  }

}
