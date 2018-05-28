package com.sun.faces.facelets.impl;

import java.io.IOException;
import java.net.URL;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;

import com.sun.faces.facelets.el.ContextualCompositeValueExpression;
import com.sun.faces.facelets.el.TagValueExpression;
import com.sun.faces.facelets.tag.TagAttributeImpl;

import junit.framework.TestCase;

public class TestTagAttributeImpl extends TestCase {

  public void testGetValueExpressionFaceletContextClass() {
    
    FaceletContext fc = mockFaceletContext();
    
    TagAttributeImpl tai = new TagAttributeImpl(null, null, null, null, "#{cc.foo}");
    ValueExpression ve = tai.getValueExpression(fc, String.class);
    assertNotNull(ve);
    assertTrue(ve instanceof TagValueExpression);
    assertTrue(((TagValueExpression)ve).getWrapped() instanceof ContextualCompositeValueExpression);
    
    
    tai = new TagAttributeImpl(null, null, null, null, "#{cc.attr.method}");
    ve = tai.getValueExpression(fc, String.class);
    assertNotNull(ve);
    assertTrue(ve instanceof TagValueExpression);
    
   // tai = new TagAttributeImpl(null, null, null, null, "#{cc.attrs.label('foo')}");
   // tai.getValueExpression(fc, String.class);
   // -> exception 
    
  }

  private FaceletContext mockFaceletContext() {
    FaceletContext fc = new FaceletContext() {

      @Override
      public FacesContext getFacesContext() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String generateUniqueId(String base) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ExpressionFactory getExpressionFactory() {
        //return ExpressionFactory.newInstance();
        return new ExpressionFactory() {

          @Override
          public Object coerceToType(Object arg0, Class<?> arg1) throws ELException {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public MethodExpression createMethodExpression(ELContext arg0, String arg1, Class<?> arg2, Class<?>[] arg3) throws ELException, NullPointerException {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public ValueExpression createValueExpression(Object arg0, Class<?> arg1) {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public ValueExpression createValueExpression(ELContext arg0, String arg1, Class<?> arg2) throws NullPointerException, ELException {
            // TODO Auto-generated method stub
            return null;
          }
          
        };
      }

      @Override
      public void setVariableMapper(VariableMapper varMapper) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void setFunctionMapper(FunctionMapper fnMapper) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void setAttribute(String name, Object value) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public Object getAttribute(String name) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public void includeFacelet(UIComponent parent, String relativePath) throws IOException {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void includeFacelet(UIComponent parent, URL absolutePath) throws IOException {
        // TODO Auto-generated method stub
        
      }

      @Override
      public ELResolver getELResolver() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public FunctionMapper getFunctionMapper() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public VariableMapper getVariableMapper() {
        // TODO Auto-generated method stub
        return null;
      }
      
    };
    return fc;
  }

}
