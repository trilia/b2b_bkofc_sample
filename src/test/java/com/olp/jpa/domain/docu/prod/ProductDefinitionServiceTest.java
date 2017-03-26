/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa.domain.docu.prod;

import com.olp.fwk.common.BaseSpringAwareTest;
import com.olp.jpa.domain.docu.comm.model.CategoryTreeEntity;
import com.olp.jpa.domain.docu.comm.repo.CategoryTreeService;
import com.olp.jpa.domain.docu.prod.model.ProductAttributesBean;
import com.olp.jpa.domain.docu.prod.model.ProductAttributesBean.CompositionType;
import com.olp.jpa.domain.docu.prod.model.ProductCompositionEntity;
import com.olp.jpa.domain.docu.prod.model.ProductDefinitionEntity;
import com.olp.jpa.domain.docu.prod.model.ProductRevisionEntity;
import com.olp.jpa.domain.docu.prod.model.ProductSpecEntity;
import com.olp.jpa.domain.docu.prod.model.ProductTemplateEntity;
import com.olp.jpa.domain.docu.prod.model.ProductVariantEntity;
import com.olp.jpa.domain.docu.prod.model.TemplateElementEntity;
import com.olp.jpa.domain.docu.prod.repo.ProductDefinitionService;
import com.olp.jpa.domain.docu.prod.repo.ProductRevisionRepository;
import com.olp.jpa.domain.docu.prod.repo.ProductRevisionService;
import com.olp.jpa.domain.docu.prod.repo.ProductTemplateService;
import com.olp.jpa.domain.docu.prod.repo.ProductVariantService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;

/**
 *
 * @author raghosh
 */
public class ProductDefinitionServiceTest extends BaseSpringAwareTest {
    
    @Autowired
    @Qualifier("categoryTreeService")
    private CategoryTreeService catgSvc;
    
    @Autowired
    @Qualifier("prodDefinitionService")
    private ProductDefinitionService prodSvc;
    
    @Autowired
    @Qualifier("prodRevisionService")
    private ProductRevisionService revSvc;
    
    @Autowired
    @Qualifier("prodRevisionRepository")
    private ProductRevisionRepository revRepo;
    
    @Autowired
    @Qualifier("prodTemplateService")
    private ProductTemplateService tmplSvc;
    
    @Autowired
    @Qualifier("prodVariantService")
    private ProductVariantService variantSvc;
    

    @Before
    public void before() {
        
        prodSvc.deleteAll(false);
        
        addCategory(null);
        
        //setUp();
        
        try {
            ProductTemplateEntity templ = tmplSvc.findLatestTemplateByCode("ROOT_TEMPLATE");
            tmplSvc.delete(templ.getId());
        } catch (javax.persistence.NoResultException ex) {
            
        }
    }
    
    @After
    public void after() {
        
        try {
            ProductTemplateEntity templ = tmplSvc.findLatestTemplateByCode("ROOT_TEMPLATE");
            tmplSvc.delete(templ.getId());
        } catch (javax.persistence.NoResultException ex) {
            
        }
        
        try {
            CategoryTreeEntity tree = catgSvc.findByCategoryCode("UT_TEST010");
            catgSvc.delete(tree.getId());
        } catch (javax.persistence.NoResultException ex) {
            
        }
        
    }
    
    //@Test
    public void test_dummy() {
        
    }

    @Test
    public void test_addWithSimpleElements() {
        
        
        addTemplate();
        
        ProductDefinitionEntity prod = CommonProd.makeProduct();
        prod.setCompositionType(ProductAttributesBean.CompositionType.STANDARD);
        
        ProductRevisionEntity rev = CommonProd.makeRevisionSimple("ROOT_TEMPLATE", prod.getProductCode(), 1);
        
        ProductTemplateEntity templ = tmplSvc.findLatestTemplateByCode("ROOT_TEMPLATE");
        ProductSpecEntity spec1 = CommonProd.makeSpec(templ.getTemplateElements().get(0).getElementCode(), "Sample Value 1");
        ProductSpecEntity spec2 = CommonProd.makeSpec(templ.getTemplateElements().get(1).getElementCode(), "Sample Value 2");
        
        ArrayList<ProductSpecEntity> specList = new ArrayList<>();
        specList.add(spec1);
        specList.add(spec2);
        
        
        /*
        Set<ProductCompositionEntity> comps = rev.getCompositions();
        for (ProductCompositionEntity comp : comps) {
            comp.setSpecifications(specList);
        }
        */
        
        
        rev.setSpecifications(specList);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2012, 4, 2);
        
        rev.setStartDate(cal.getTime());
        
        ArrayList<ProductRevisionEntity> revs = new ArrayList<>();
        revs.add(rev);
        
        prod.setRevisions(revs);
        
        prodSvc.add(prod);
        
        
        List<ProductDefinitionEntity> list = prodSvc.findAll();
        
        assertNotNull("List of products should not be null", list);
        
        //ProductDefinitionEntity prod2 = prodSvc.findByProductCode(prod.getProductCode());
        ProductDefinitionEntity prod2 = list.get(0);
        
        assertNotNull("Product definition should not be null", prod2);
        assertNotNull("Prouct revisions should not be null", prod2.getRevisions());
        assertEquals("Only 1 revision", 1, prod2.getRevisions().size());
        //assertNotNull("Product composition should not be null", prod2.getRevisions().get(0).getCompositions());
        //assertEquals("Only 1 composition", 1, prod2.getRevisions().get(0).getCompositions().size());
        //assertEquals("No bundle associations", 0, prod2.getRevisions().get(0).getBundleAsscns().size());
        
        ProductRevisionEntity rev2 = revRepo.findByProdCodeAndRevNum(prod2.getProductCode(), 1);
        
        assertNotNull("Product revision should not be null", rev2);
        
    }
    
    //@Test
    public void test_addWithLovElements() {
        
        addTemplateWithLov();
        
        ProductDefinitionEntity prod = CommonProd.makeProduct();
        prod.setCompositionType(ProductAttributesBean.CompositionType.STANDARD);
        
        ProductRevisionEntity rev = CommonProd.makeRevisionSimple("ROOT_TEMPLATE", prod.getProductCode(), 1);
        
        ProductTemplateEntity templ = tmplSvc.findLatestTemplateByCode("ROOT_TEMPLATE");
        ProductSpecEntity spec1 = CommonProd.makeSpec(templ.getTemplateElements().get(0).getElementCode(), "Sample Value 1");
        ProductSpecEntity spec2 = CommonProd.makeSpec(templ.getTemplateElements().get(1).getElementCode(), "Sample Value 2");
        ProductSpecEntity spec3 = CommonProd.makeSpec(templ.getTemplateElements().get(2).getElementCode(), "Test 1");
        ProductSpecEntity spec4 = CommonProd.makeSpec(templ.getTemplateElements().get(3).getElementCode(), "Test 2");
        
        ArrayList<ProductSpecEntity> specList = new ArrayList<>();
        specList.add(spec1);
        specList.add(spec2);
        specList.add(spec3);
        specList.add(spec4);
        
        /*
        Set<ProductCompositionEntity> comps = rev.getCompositions();
        for (ProductCompositionEntity comp : comps) {
            comp.setSpecifications(specList);
        }
        */
        
        rev.setSpecifications(specList);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2012, 4, 2);
        
        rev.setStartDate(cal.getTime());
        
        ArrayList<ProductRevisionEntity> revs = new ArrayList<>();
        revs.add(rev);
        
        prod.setRevisions(revs);
        
        prodSvc.add(prod);
        
        ProductDefinitionEntity prod2 = prodSvc.findByProductCode(prod.getProductCode());
        
        assertNotNull("Product definition should not be null", prod2);
    }
    
    //@Test
    public void test_addWithVariants() {
        
        addTemplateWithVariant();
        
        ProductDefinitionEntity prod = CommonProd.makeProduct();
        prod.setCompositionType(ProductAttributesBean.CompositionType.STANDARD);
        
        ProductRevisionEntity rev = CommonProd.makeRevisionSimple("ROOT_TEMPLATE", prod.getProductCode(), 1);
        
        ProductTemplateEntity templ = tmplSvc.findLatestTemplateByCode("ROOT_TEMPLATE");
        ProductSpecEntity spec1 = CommonProd.makeSpec(templ.getTemplateElements().get(0).getElementCode(), "Sample Value 1");
        ProductSpecEntity spec2 = CommonProd.makeSpec(templ.getTemplateElements().get(1).getElementCode(), "Sample Value 2");
        List<String> color = new ArrayList<>(); color.add("Red"); color.add("Green"); color.add("Blue"); color.add("Yellow");
        ProductSpecEntity spec3 = CommonProd.makeSpecVariant(templ.getTemplateElements().get(2).getElementCode(), color);
        List<String> size = new ArrayList<>(); size.add("Small"); size.add("Medium"); size.add("Large");
        ProductSpecEntity spec4 = CommonProd.makeSpecVariant(templ.getTemplateElements().get(3).getElementCode(), size);
        
        ArrayList<ProductSpecEntity> specList = new ArrayList<>();
        specList.add(spec1); specList.add(spec2); specList.add(spec3); specList.add(spec4);
        
        /*
        Set<ProductCompositionEntity> comps = rev.getCompositions();
        for (ProductCompositionEntity comp : comps) {
            comp.setSpecifications(specList);
        }
        */
        
        rev.setSpecifications(specList);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2012, 4, 2);
        
        rev.setStartDate(cal.getTime());
        
        ArrayList<ProductRevisionEntity> revs = new ArrayList<>();
        revs.add(rev);
        
        prod.setRevisions(revs);
        
        prodSvc.add(prod);
        
        ProductDefinitionEntity prod2 = prodSvc.findByProductCode(prod.getProductCode());
        
        assertNotNull("Product definition should not be null", prod2);
        
        assertNotNull("Product revisions should not be null", prod2.getRevisions());
        
        assertNotNull("Product specs should not be null", prod2.getRevisions().get(0).getSpecifications());
        
        assertTrue("More than 2 specs", prod2.getRevisions().get(0).getSpecifications().size() > 0);
        
        List<ProductVariantEntity> variants = variantSvc.findAll();
        
        assertNotNull("Product variants should not be null", variants);
        
        assertTrue("More than 6 variants", variants.size() > 6);
    }
    
    //@Test
    public void test_addBundle() {
        
        addTemplateWithVariant();
        
        ProductDefinitionEntity prod1 = CommonProd.makeProduct();
        
        prod1.setCompositionType(ProductAttributesBean.CompositionType.STANDARD);
        
        ProductRevisionEntity rev1 = CommonProd.makeRevisionSimple("ROOT_TEMPLATE", prod1.getProductCode(), 1);
        
        ProductTemplateEntity templ = tmplSvc.findLatestTemplateByCode("ROOT_TEMPLATE");
        
        ProductSpecEntity spec11 = CommonProd.makeSpec(templ.getTemplateElements().get(0).getElementCode(), "Sample Value 1");
        ProductSpecEntity spec12 = CommonProd.makeSpec(templ.getTemplateElements().get(1).getElementCode(), "Sample Value 2");
        List<String> color = new ArrayList<>(); color.add("Red"); color.add("Green"); color.add("Blue"); color.add("Yellow");
        ProductSpecEntity spec13 = CommonProd.makeSpecVariant(templ.getTemplateElements().get(2).getElementCode(), color);
        List<String> size = new ArrayList<>(); size.add("Small"); size.add("Medium"); size.add("Large");
        ProductSpecEntity spec14 = CommonProd.makeSpecVariant(templ.getTemplateElements().get(3).getElementCode(), size);
        
        ArrayList<ProductSpecEntity> specList1 = new ArrayList<>();
        specList1.add(spec11); specList1.add(spec12); specList1.add(spec13); specList1.add(spec14);
        
        /*
        Set<ProductCompositionEntity> comps = rev1.getCompositions();
        for (ProductCompositionEntity comp : comps) {
            comp.setSpecifications(specList1);
        }
        */
        
        rev1.setSpecifications(specList1);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2012, 4, 2);
        
        rev1.setStartDate(cal.getTime());
        
        ArrayList<ProductRevisionEntity> revs1 = new ArrayList<>();
        revs1.add(rev1);
        
        prod1.setRevisions(revs1);
        
        ProductDefinitionEntity prod2 = CommonProd.makeProduct();
        
        prod2.setCompositionType(ProductAttributesBean.CompositionType.STANDARD);
        
        ProductRevisionEntity rev2 = CommonProd.makeRevisionSimple("ROOT_TEMPLATE", prod2.getProductCode(), 1);
        
        ProductSpecEntity spec21 = CommonProd.makeSpec(templ.getTemplateElements().get(0).getElementCode(), "Sample Value 1");
        ProductSpecEntity spec22 = CommonProd.makeSpec(templ.getTemplateElements().get(1).getElementCode(), "Sample Value 2");
        List<String> styles = new ArrayList<>(); styles.add("Round Neck"); styles.add("V-neck"); styles.add("Collar");
        ProductSpecEntity spec23 = CommonProd.makeSpecVariant(templ.getTemplateElements().get(2).getElementCode(), styles);
        //List<String> size = new ArrayList<>(); size.add("Small"); size.add("Medium"); size.add("Large");
        //ProductSpecEntity spec24 = CommonProd.makeSpecVariant(templ.getTemplateElements().get(3).getElementCode(), size);
        
        ArrayList<ProductSpecEntity> specList2 = new ArrayList<>();
        specList2.add(spec21); specList2.add(spec22); specList2.add(spec23); //specList.add(spec24);
        
        /*
        Set<ProductCompositionEntity> comps2 = rev2.getCompositions();
        for (ProductCompositionEntity comp : comps2) {
            comp.setSpecifications(specList2);
        }
        */
        
        rev2.setSpecifications(specList2);
        
        rev2.setStartDate(cal.getTime());
        
        ArrayList<ProductRevisionEntity> revs2 = new ArrayList<>();
        revs2.add(rev2);
        
        prod2.setRevisions(revs2);
        
        ArrayList<ProductDefinitionEntity> prodList = new ArrayList<>();
        prodList.add(prod1); prodList.add(prod2);
        
        prodSvc.addAll(prodList, false);
        
        ProductDefinitionEntity prod12 = prodSvc.findByProductCode(prod1.getProductCode());
        assertNotNull("Product definition should not be null", prod12);
        assertNotNull("Product revisions should not be null", prod12.getRevisions());
        
        ProductDefinitionEntity prod22 = prodSvc.findByProductCode(prod2.getProductCode());
        assertNotNull("Product definition should not be null", prod22);
        assertNotNull("Product revisions should not be null", prod22.getRevisions());
        
        //ProductRevisionEntity rev12 = revSvc.findByProductRevision(prod12.getProductCode(), 1);
        //ProductRevisionEntity rev12 = revRepo.findByProdCodeAndRevNum(prod12.getProductCode(), 1);
        //List<ProductRevisionEntity> list  = revRepo.findByProductCode(prod12.getProductCode());

        // Create the product bundle
        
        ProductDefinitionEntity prod3 = CommonProd.makeProduct();
        
        prod3.setCompositionType(ProductAttributesBean.CompositionType.COMPOSITE);
        
        ArrayList<String> prodCodes = new ArrayList<>();
        prodCodes.add(prod1.getProductCode()); prodCodes.add(prod2.getProductCode());
        
        ProductRevisionEntity rev3 = CommonProd.makeRevisionBundle(prodCodes);
        rev3.setStartDate(cal.getTime());
        
        
        ArrayList<ProductRevisionEntity> revs3 = new ArrayList<>();
        revs3.add(rev3);
        
        prod3.setRevisions(revs3);
        
        prodSvc.add(prod3);
        
        /*
        ProductDefinitionEntity prod32 = prodSvc.findByProductCode(prod3.getProductCode());
        
        assertNotNull("Product definition should not be null", prod32);
        
        assertNotNull("Product revisions should not be null", prod32.getRevisions());
        
        assertNotNull("Product specs should not be null", prod32.getRevisions().get(0).getSpecifications());
        
        assertEquals("2 compositions",2, prod32.getRevisions().get(0).getChildProdCompositions().size());
        */
        
        List<ProductDefinitionEntity> prods2 = prodSvc.findAll();
        
        assertNotNull("Product should not be null", prods2);
        assertTrue("Atleast 3 products", prods2.size() >= 3);
        int i = 0, j = 0;
        for (ProductDefinitionEntity def : prods2) {    
            if (def.getCompositionType() == CompositionType.COMPOSITE) {
                assertEquals("Two product in composite", 2, def.getRevisions().get(0).getChildProdCompositions().size());
                i++;
            } else {
                if (def.getRevisions().get(0).getCompositionAsscns().size() > 0) {
                    j++;
                }
            }
        }
        assertEquals("one product is composite", 1, i);
        assertEquals("two product is part of one composite", 2, j);
        
    }
    
    private void setUp() {
        
        addTemplate();
        
        ProductDefinitionEntity prod = CommonProd.makeProduct();
        prod.setCompositionType(ProductAttributesBean.CompositionType.STANDARD);
        
        ProductRevisionEntity rev = CommonProd.makeRevisionSimple("ROOT_TEMPLATE", prod.getProductCode(), 1);
        
        ProductTemplateEntity templ = tmplSvc.findLatestTemplateByCode("ROOT_TEMPLATE");
        ProductSpecEntity spec1 = CommonProd.makeSpec(templ.getTemplateElements().get(0).getElementCode(), "Sample Value 1");
        ProductSpecEntity spec2 = CommonProd.makeSpec(templ.getTemplateElements().get(1).getElementCode(), "Sample Value 2");
        
        //ProductSpecEntity spec1 = makeSpec();
        //ProductSpecEntity spec2 = makeSpec();
        
        ArrayList<ProductSpecEntity> specList = new ArrayList<>();
        specList.add(spec1);
        specList.add(spec2);
        
        /*
        Set<ProductCompositionEntity> comps = rev.getCompositions();
        for (ProductCompositionEntity comp : comps) {
            comp.setSpecifications(specList);
        }
        */
        
        rev.setSpecifications(specList);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2012, 4, 2);
        
        rev.setStartDate(cal.getTime());
        
        ArrayList<ProductRevisionEntity> revs = new ArrayList<>();
        revs.add(rev);
        prod.setRevisions(revs);
        
        prodSvc.add(prod);
        
    }
    
    private void addTemplate() {
        
        ProductTemplateEntity tmpl = null;

        try {
            tmpl = tmplSvc.findLatestTemplateByCode("ROOT_TEMPLATE");
        } catch (javax.persistence.NoResultException ex) {
            tmpl = CommonProd.makeTemplate();
            tmpl.setTemplateCode("ROOT_TEMPLATE");
            ArrayList<TemplateElementEntity> elemList = new ArrayList<>();
        
            TemplateElementEntity elem1 = CommonProd.makeElementSimple();
            elem1.setTemplateRef(tmpl);

            TemplateElementEntity elem2 = CommonProd.makeElementSimple();
            elem2.setTemplateRef(tmpl);

            elemList.add(elem1); elemList.add(elem2);

            tmpl.setTemplateElements(elemList);

            CategoryTreeEntity tree = new CategoryTreeEntity();
            tree.setCategoryCode("UT_TEST010");

            tmpl.setCategoryRef(tree);

            tmplSvc.add(tmpl);
            
        }
    
    }
    
    private void addTemplateWithLov() {
        
        ProductTemplateEntity tmpl = null;

        try {
            tmpl = tmplSvc.findLatestTemplateByCode("ROOT_TEMPLATE");
        } catch (javax.persistence.NoResultException ex) {
            tmpl = CommonProd.makeTemplate();
            tmpl.setTemplateCode("ROOT_TEMPLATE");
            ArrayList<TemplateElementEntity> elemList = new ArrayList<>();
            
            TemplateElementEntity elem1 = CommonProd.makeElementSimple();
            elem1.setTemplateRef(tmpl);

            TemplateElementEntity elem2 = CommonProd.makeElementSimple();
            elem2.setTemplateRef(tmpl);
            
            TemplateElementEntity elem3 = CommonProd.makeElementLov();
            elem3.setTemplateRef(tmpl);

            TemplateElementEntity elem4 = CommonProd.makeElementLov();
            elem4.setTemplateRef(tmpl);

            elemList.add(elem1); elemList.add(elem2); elemList.add(elem3); elemList.add(elem4);

            tmpl.setTemplateElements(elemList);

            CategoryTreeEntity tree = new CategoryTreeEntity();
            tree.setCategoryCode("UT_TEST010");

            tmpl.setCategoryRef(tree);

            tmplSvc.add(tmpl);
            
        } 
        
    }
    
    private void addTemplateWithVariant() {
        
        ProductTemplateEntity tmpl = null;

        try {
            tmpl = tmplSvc.findLatestTemplateByCode("ROOT_TEMPLATE");
        } catch (javax.persistence.NoResultException ex) {
            tmpl = CommonProd.makeTemplate();
            tmpl.setTemplateCode("ROOT_TEMPLATE");
            ArrayList<TemplateElementEntity> elemList = new ArrayList<>();
            
            TemplateElementEntity elem1 = CommonProd.makeElementSimple();
            elem1.setTemplateRef(tmpl);

            TemplateElementEntity elem2 = CommonProd.makeElementSimple();
            elem2.setTemplateRef(tmpl);
            
            TemplateElementEntity elem3 = CommonProd.makeElementVariant();
            elem3.setElementName("Color");
            elem3.setTemplateRef(tmpl);

            TemplateElementEntity elem4 = CommonProd.makeElementVariant();
            elem4.setElementName("Size");
            elem4.setTemplateRef(tmpl);

            elemList.add(elem1); elemList.add(elem2); elemList.add(elem3); elemList.add(elem4);

            tmpl.setTemplateElements(elemList);

            CategoryTreeEntity tree = new CategoryTreeEntity();
            tree.setCategoryCode("UT_TEST010");

            tmpl.setCategoryRef(tree);

            tmplSvc.add(tmpl);
            
        }
    
    }
    
    private void addCategory(String s) {
        
        String code;
        if (s == null || "".equals(s)) {
            code = "CATG_" + getRandom();
        } else
            code = s;
        
        CategoryTreeEntity entity = null;
        
        try {
            entity = catgSvc.findByCategoryCode("UT_TEST010");
        } catch (javax.persistence.NoResultException ex) {
            entity = new CategoryTreeEntity();
            entity.setCategoryCode("UT_TEST010");
            entity.setCategoryName("UT " + code);
            entity.setCategorySource("UT_TEST");
            entity.setCategoryClass("UT");
            catgSvc.add(entity);
        }
        
        String code2;
        if (s == null || "".equals(s)) {
            code2 = "CATG_" + getRandom();
        } else
            code2 = s;
        
        CategoryTreeEntity entity2 = null; 
        try {
            entity2 = catgSvc.findByCategoryCode("UT_TEST020");
        } catch (javax.persistence.NoResultException ex) {
            entity2 = new CategoryTreeEntity();
            entity2.setCategoryCode("UT_TEST020");
            entity2.setCategoryName("UT " + code2);
            entity2.setCategorySource("UT_TEST");
            entity2.setCategoryClass("UT");
            entity2.setParentCategoryRef(entity);
            catgSvc.add(entity2);
        }
        
    }
    
    private static ProductSpecEntity makeSpec() {
        ProductSpecEntity spec = new ProductSpecEntity();
        ArrayList<String> specs = new ArrayList<>();
        specs.add("First Spec");
        //spec.setSpecs(specs);
        spec.setTenantId(getTenantId());
        return(spec);
    }
    
}
