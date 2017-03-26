/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa.domain.docu.prod;

import com.olp.fwk.common.BaseSpringAwareTest;
import com.olp.fwk.common.ContextManager;
import com.olp.fwk.common.IContext;
import com.olp.jpa.common.ListOfValuesBean;
import com.olp.jpa.domain.docu.comm.model.CategoryTreeEntity;
import com.olp.jpa.domain.docu.prod.model.DataValidationBean;
import com.olp.jpa.domain.docu.prod.model.FormatValidationBean;
import com.olp.jpa.domain.docu.prod.model.ProductCompositionEntity;
import com.olp.jpa.domain.docu.prod.model.ProductDefinitionEntity;
import com.olp.jpa.domain.docu.prod.model.ProductRevisionEntity;
import com.olp.jpa.domain.docu.prod.model.ProductSpecEntity;
import com.olp.jpa.domain.docu.prod.model.ProductTemplateEntity;
import com.olp.jpa.domain.docu.prod.model.TemplateElementEntity;
import com.olp.jpa.domain.docu.prod.model.TemplateElementInfoBean;
import com.olp.jpa.domain.docu.prod.model.VariantInfoBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 * @author raghosh
 */
public class CommonProd extends BaseSpringAwareTest {
    
    public static ProductTemplateEntity makeTemplate() {
        
        String s = getRandom();
        String tid = getTenantId();
        
        ProductTemplateEntity entity = new ProductTemplateEntity();
        entity.setTemplateCode("TEMPLATE_" + s);
        entity.setTenantId(tid);
        entity.setTemplateName("Template " + s);
        entity.setDescription("Template for UT");
        entity.setStartDate(new Date(112,1,1));
        
        return(entity);
    }
    
    public static TemplateElementEntity makeElementSimple() {
        
        String s = getRandom();
        String tid = getTenantId();
        
        TemplateElementEntity entity = new TemplateElementEntity();
        entity.setElementCode("ELEMENT_" + s);
        entity.setTenantId(tid);
        entity.setElementName("Element " + s);
        entity.setDataType(TemplateElementInfoBean.InfoType.SHORT_TEXT);
        entity.setInfoStyle(TemplateElementInfoBean.InfoStyle.SINGLE_VALUE);
        DataValidationBean bean1 = new DataValidationBean();
        bean1.setDataValidation(TemplateElementInfoBean.DataValidationType.NONE);
        bean1.setPresentation(TemplateElementInfoBean.Presentation.DEFAULT);
        entity.setDataValidation(bean1);
        FormatValidationBean bean2 = new FormatValidationBean();
        bean2.setFormatValidation(TemplateElementInfoBean.FormatValidationType.NONE);
        entity.setFormatValidation(bean2);
        entity.setInfoGroup("GLOBAL");
        entity.setInfoSubGroup("GENERAL");
        entity.setIsEnabled(true);
        entity.setIsRequired(true);
        entity.setIsSearchFilter(true);
        entity.setIsVariant(false);
        entity.setIsInherited(false);
        entity.setUnitOfMeasure("None");
        
        
        return(entity);
    }
    
    public static TemplateElementEntity makeElementLov() {
        
        String s = getRandom();
        String tid = getTenantId();
        
        TemplateElementEntity entity = new TemplateElementEntity();
        entity.setElementCode("ELEMENT_" + s);
        entity.setTenantId(tid);
        entity.setElementName("Element " + s);
        entity.setDataType(TemplateElementInfoBean.InfoType.SHORT_TEXT);
        entity.setInfoStyle(TemplateElementInfoBean.InfoStyle.LOV_MULTI);
        
        DataValidationBean bean1 = new DataValidationBean();
        bean1.setDataValidation(TemplateElementInfoBean.DataValidationType.STATIC_LOV);
        bean1.setPresentation(TemplateElementInfoBean.Presentation.DROPDOWN);
        
        ArrayList<ListOfValuesBean> values = new ArrayList<>();
        ListOfValuesBean lov1 = new ListOfValuesBean(); lov1.setLovCode("TEST1"); lov1.setLovValue("Test 1");
        ListOfValuesBean lov2 = new ListOfValuesBean(); lov2.setLovCode("TEST2"); lov2.setLovValue("Test 2");
        ListOfValuesBean lov3 = new ListOfValuesBean(); lov3.setLovCode("TEST3"); lov3.setLovValue("Test 3");
        values.add(lov1); values.add(lov2); values.add(lov3);
        
        bean1.setListOfValues(values);
        
        entity.setDataValidation(bean1);
        
        FormatValidationBean bean2 = new FormatValidationBean();
        bean2.setFormatValidation(TemplateElementInfoBean.FormatValidationType.NONE);
        entity.setFormatValidation(bean2);
        entity.setInfoGroup("GLOBAL");
        entity.setInfoSubGroup("GENERAL");
        entity.setIsEnabled(true);
        entity.setIsRequired(true);
        entity.setIsSearchFilter(true);
        entity.setIsVariant(false);
        entity.setIsInherited(false);
        entity.setUnitOfMeasure("None");
        
        
        return(entity);
    }
    
    public static TemplateElementEntity makeElementVariant() {
        
        String s = getRandom();
        String tid = getTenantId();
        
        TemplateElementEntity entity = new TemplateElementEntity();
        entity.setElementCode("ELEMENT_" + s);
        entity.setTenantId(tid);
        entity.setElementName("Element " + s);
        entity.setDataType(TemplateElementInfoBean.InfoType.SHORT_TEXT);
        entity.setInfoStyle(TemplateElementInfoBean.InfoStyle.SINGLE_VALUE);
        
        DataValidationBean bean1 = new DataValidationBean();
        bean1.setDataValidation(TemplateElementInfoBean.DataValidationType.NONE);
        bean1.setPresentation(TemplateElementInfoBean.Presentation.DEFAULT);
        
        entity.setDataValidation(bean1);
        
        FormatValidationBean bean2 = new FormatValidationBean();
        bean2.setFormatValidation(TemplateElementInfoBean.FormatValidationType.NONE);
        entity.setFormatValidation(bean2);
        entity.setInfoGroup("GLOBAL");
        entity.setInfoSubGroup("GENERAL");
        entity.setIsEnabled(true);
        entity.setIsRequired(true);
        entity.setIsSearchFilter(true);
        entity.setIsVariant(true);
        entity.setIsInherited(false);
        entity.setUnitOfMeasure("None");
        
        
        return(entity);
    }
    
    public static CategoryTreeEntity makeCategory(String code) {
        
        String s = getRandom();
        IContext ctx = ContextManager.getContext();
        
        CategoryTreeEntity tree = new CategoryTreeEntity();
        if (code == null || "".equals(code))
            tree.setCategoryCode("CATG_" + s);
        else
            tree.setCategoryCode(code);
        
        tree.setTenantId(ctx.getTenantId());
        
        tree.setCategoryName("Category " + s);
        tree.setCategorySource("UT_SOURCE");
        tree.setCategoryClass("UT_CLASS");
        tree.setIsEnabled(true);
        
        return(tree);
    }
    
    public static ProductDefinitionEntity makeProduct() {
        
        String s = getRandom();
        IContext ctx = ContextManager.getContext();
        
        ProductDefinitionEntity prod = new ProductDefinitionEntity();
        prod.setProductCode("PRODUCT_" + s);
        prod.setProductName("Product " + s);
        prod.setDescription("Sample UT product");
        
        
        return(prod);
    }
    
    public static ProductRevisionEntity makeRevisionSimple(String templCode, String prodCode, int revNum) {
        
        ProductRevisionEntity rev = new ProductRevisionEntity();
        rev.setProductCode(prodCode);
        rev.setRevisionNumber(revNum);
        
        //ProductCompositionEntity comp = makeComposition();
        
        ProductTemplateEntity tmpl = new ProductTemplateEntity();
        tmpl.setTemplateCode(templCode);
        
        rev.setProdTemplateRef(tmpl);
        
        //Set<ProductCompositionEntity> compList = new HashSet<>();
        //compList.add(comp);
        
        //rev.setCompositions(compList);
        
        return(rev);
    }
    
    public static ProductRevisionEntity makeRevisionBundle(List<String> childProds) {
        
        ProductRevisionEntity rev = new ProductRevisionEntity();
        
        Set<ProductCompositionEntity> compList = new HashSet<>();
        /*
        if (childProds != null) {
            
            for (String prodCode : childProds) {
                
                ProductDefinitionEntity pde = new ProductDefinitionEntity();
                pde.setProductCode(prodCode);

                ProductRevisionEntity rev2 = new ProductRevisionEntity();
                rev2.setProductCode(prodCode);
                rev2.setRevisionNumber(1);
                rev2.setProductDefRef(pde);

                ProductCompositionEntity comp = makeCompositionBundle();
                comp.setChildProdRevisionRef(rev2);
                //comp.setProdRevisionRef(rev); // don't set prod rev ref here, which is not persisted yet ! causes validation failure
                
                compList.add(comp);
            }
        }
        
        rev.setCompositions(compList);
        */
        
        if (childProds != null) {
            
            for (String prodCode : childProds) {
                
                ProductDefinitionEntity pde = new ProductDefinitionEntity();
                pde.setProductCode(prodCode);
                
                ProductRevisionEntity rev2 = new ProductRevisionEntity();
                rev2.setProductDefRef(pde);
                rev2.setRevisionNumber(1);
                
                ProductCompositionEntity comp = new ProductCompositionEntity();
                comp.setChildProdRevisionRef(rev2);
                
                compList.add(comp);
            }
            
        }
        
        
        rev.setChildProdCompositions(compList);
        
        return(rev);
    }
    
    public static ProductCompositionEntity makeComposition() {
        
        ProductCompositionEntity comp = new ProductCompositionEntity();
        
        return(comp);
        
    }
    
    public static ProductCompositionEntity makeCompositionBundle() {
        
        ProductCompositionEntity comp = new ProductCompositionEntity();
        
        return(comp);
        
    }
    
    public static ProductSpecEntity makeSpec(String elementCode, String value) {
        
        ProductSpecEntity spec = new ProductSpecEntity();
        
        TemplateElementEntity elem = new TemplateElementEntity();
        elem.setElementCode(elementCode);
        
        spec.setTemplElementRef(elem);
        
        ArrayList<String> values = new ArrayList<>();
        values.add(value);
        
        spec.setAttributeValues(values);
        
        return(spec);
    }
    
    
    
    public static ProductSpecEntity makeSpecVariant(String elementCode, List<String> values) {
        
        ProductSpecEntity spec = new ProductSpecEntity();
        
        TemplateElementEntity elem = new TemplateElementEntity();
        elem.setElementCode(elementCode);
        
        spec.setTemplElementRef(elem);
        
        VariantInfoBean varInfo = new VariantInfoBean();
        ArrayList<ListOfValuesBean> lov = new ArrayList<>();
        if (values != null) {
            for (String val : values) {
                ListOfValuesBean bean = new ListOfValuesBean();
                bean.setLovValue(val);
                bean.setLovCode(val);
                lov.add(bean);
            }
        }
        varInfo.setVariantValues(lov);
        varInfo.setRenderingStyle(VariantInfoBean.VarInfoStyle.LIST);
        
        spec.setVariantInfo(varInfo);
        
        return(spec);
    }
    
}
