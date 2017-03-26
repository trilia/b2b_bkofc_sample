/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa.domain.docu.prod.repo;

import com.olp.fwk.common.Constants;
import com.olp.fwk.common.Utils;
import com.olp.fwk.common.error.EntityValidationException;
import com.olp.jpa.common.AbstractServiceImpl;
import com.olp.jpa.common.ITextRepository;
import com.olp.jpa.common.ListOfValuesBean;
import com.olp.jpa.domain.docu.comm.repo.CategoryTreeRepository;
import com.olp.jpa.domain.docu.inv.model.ProductCodeGenEntity;
import com.olp.jpa.domain.docu.inv.model.ProductSkuEntity;
import com.olp.jpa.domain.docu.inv.model.SkuBean;
import com.olp.jpa.domain.docu.inv.repo.ProductCodeGenRepository;
import com.olp.jpa.domain.docu.prod.model.ProductAttributesBean;
import com.olp.jpa.domain.docu.prod.model.ProductAttributesBean.CompositionType;
import com.olp.jpa.domain.docu.prod.model.ProductCompositionEntity;
import com.olp.jpa.domain.docu.prod.model.ProductDefinitionEntity;
import com.olp.jpa.domain.docu.prod.model.ProductGroupAsscnEntity;
import com.olp.jpa.domain.docu.prod.model.ProductGroupEntity;
import com.olp.jpa.domain.docu.prod.model.ProductRevisionEntity;
import com.olp.jpa.domain.docu.prod.model.ProductSpecEntity;
import com.olp.jpa.domain.docu.prod.model.ProductTemplateEntity;
import com.olp.jpa.domain.docu.prod.model.ProductVariantEntity;
import com.olp.jpa.domain.docu.prod.model.TemplateElementEntity;
import com.olp.jpa.domain.docu.prod.model.VariantInfoBean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author raghosh
 */
@Service("prodDefinitionService")
public class ProductDefinitionServiceImpl extends AbstractServiceImpl<ProductDefinitionEntity, Long> implements ProductDefinitionService {
    
    @Autowired
    @Qualifier("prodDefinitionRepository")
    private ProductDefinitionRepository prodRepo;
    
    @Autowired
    @Qualifier("categoryTreeRepository")
    private CategoryTreeRepository catgRepo;
    
    @Autowired
    @Qualifier("prodRevisionRepository")
    private ProductRevisionRepository revRepo;
    
    @Autowired
    @Qualifier("prodRevisionService")
    private ProductRevisionService revSvc;
    
    @Autowired
    @Qualifier("prodGroupAsscnRepository")
    private ProductGroupAsscnRepository groupAsscnRepo;
    
    @Autowired
    @Qualifier("prodGroupRepository")
    private ProductGroupRepository groupRepo;
    
    @Autowired
    @Qualifier("prodCompositionRepository")
    private ProductCompositionRepository compRepo;
    
    @Autowired
    @Qualifier("prodCodeGenRepository")
    private ProductCodeGenRepository codeGenRepo;

    @Override
    protected JpaRepository<ProductDefinitionEntity, Long> getRepository() {
        return(prodRepo);
    }

    @Override
    protected ITextRepository<ProductDefinitionEntity, Long> getTextRepository() {
        return(prodRepo);
    }

    @Override
    protected String getAlternateKeyAsString(ProductDefinitionEntity entity) {
        StringBuilder buff = new StringBuilder();
        buff.append("{ \"product_code\" : \"").append(entity.getProductCode()).append("\" }");
        
        return(buff.toString());
    }
    
    @Override
    @Transactional(readOnly=true, noRollbackFor={javax.persistence.NoResultException.class})
    public ProductDefinitionEntity findByProductCode(String code) {
        
        ProductDefinitionEntity entity = prodRepo.findByProductCode(code);
        
        return(entity);
    }
    
    @Override
    public ProductDefinitionEntity doUpdate(ProductDefinitionEntity neu, ProductDefinitionEntity old) throws EntityValidationException {
        
        // All updates should happen on last revision id
        // So even if the input contains historical revision ids, the earlier ones will be ignored
        // and the last one be taken for update. Few other rules --
        //
        // 1. Revisions may not alter the distinct variants, although the allowed value for a variant can be added
        
        if (!Objects.equals(old.getProductCode(), neu.getProductCode()))
            throw new EntityValidationException("Mismatch in product code ! Existing - " + old.getProductCode() + " , new - " + neu.getProductCode());
        
        List<ProductRevisionEntity> neuRevs = neu.getRevisions();
        if (neuRevs != null && !neuRevs.isEmpty()) {
            ProductRevisionEntity rev = neuRevs.get(neuRevs.size() - 1);
            List<String> newVarNames = getVariantNames(rev);
            
            if ("ACTIVE".equals(old.getStatus())) {
                HashSet<String> newVarSet = new HashSet<>(newVarNames);
            
                List<String> oldVarNames = old.getVariantNames();
                HashSet<String> oldVarSet = new HashSet<>();
                if (oldVarNames != null) {
                    oldVarSet.addAll(oldVarNames);
                }
                if (!newVarSet.containsAll(oldVarSet))
                    throw new EntityValidationException("Distinct variants cannot be changed !!");
                if (!oldVarSet.containsAll(newVarSet))
                    throw new EntityValidationException("Distinct variants cannot be changed !!");
            }
            
            // After the above validation, safe to set the varnames
            old.setVariantNames(newVarNames);
            
            Set<ProductVariantEntity> newVars = getVariants(rev);
            Set<ProductVariantEntity> oldVars = old.getVariants();
            
            if (oldVars == null || oldVars.isEmpty()) {
                if (newVars == null || newVars.isEmpty()) {
                    // nothing to do
                } else {
                    //neu.setVariants(newVars);
                    oldVars.addAll(newVars);
                }
            } else {
                if (newVars == null || newVars.isEmpty()) {
                    // all variant options diabled
                    Iterator<ProductVariantEntity> oldVarIter = oldVars.iterator();
                    while (oldVarIter != null && oldVarIter.hasNext()) {
                        ProductVariantEntity oldVar = oldVarIter.next();
                        oldVar.setIsEnabled(false);
                    }
                } else {
                    // first iteration -- for all newVars see if there is existing entries. Only create additional ones
                    Iterator<ProductVariantEntity> newVarIter = newVars.iterator();
                    while (newVarIter != null && newVarIter.hasNext()) {
                        ProductVariantEntity newVar = newVarIter.next();
                        Iterator<ProductVariantEntity> oldVarIter = oldVars.iterator();
                        boolean found = false;
                        while (oldVarIter != null && oldVarIter.hasNext()) {
                            ProductVariantEntity oldVar = oldVarIter.next();
                            if (newVar.isEquivalent(oldVar)) {
                                found = true;
                                oldVar.setIsEnabled(true); // this is a corner case, where previously the variant diabled as shown in next iteration
                                                           // and now getting enabled once again.
                                break;
                            }
                        }
                        if (!found) {
                            // add to variants
                            oldVars.add(newVar);
                        }
                    }
                    
                    //second iteration -- for all oldVars see if there is missing entries. Disable them (don't delete)
                    Iterator<ProductVariantEntity> oldVarIter = oldVars.iterator();
                    while (oldVarIter != null && oldVarIter.hasNext()) {
                        ProductVariantEntity oldVar = oldVarIter.next();
                        Iterator<ProductVariantEntity> newVarIter2 = newVars.iterator();
                        boolean found = true;
                        while (newVarIter2 != null && newVarIter2.hasNext()) {
                            ProductVariantEntity newVar = newVarIter2.next();
                            if (newVar.isEquivalent(oldVar)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            oldVar.setIsEnabled(false);
                        }
                    }
                } // end if newVar == null || newVars is empty
            } // end if oldVars == null || oldVars is empty
            
            
        }
        
        boolean historicalUpdate = false;
        if ("ACTIVE".equals(old.getStatus())) {
            historicalUpdate = checkHistoricalUpdate(neu, old);
        }

        if (historicalUpdate) {
            old.setProductName(neu.getProductName());
            old.setDescription(neu.getDescription());
            old.setGroups(neu.getGroups());
            int ver = neuRevs == null ? 0 : neuRevs.size();
            old.setProductVersion(ver);
            if (neuRevs != null && !neuRevs.isEmpty()) {
                ProductRevisionEntity newRev = neuRevs.get(neuRevs.size() - 1);
                newRev.setId(null);
                newRev.setProductDefRef(old);
                addRevision(newRev);
                List<ProductRevisionEntity> oldRevs = old.getRevisions();
                if (!oldRevs.isEmpty()) {
                    ProductRevisionEntity oldRev = oldRevs.get(oldRevs.size()-1);
                    oldRev.setEndDate(newRev.getStartDate());
                }
                oldRevs.add(newRev);
            }
            
        } else {
            old.setProductName(neu.getProductName());
            old.setDescription(neu.getDescription());
            old.setGroups(neu.getGroups());
            int ver = neuRevs == null ? 0 : neuRevs.size();
            old.setProductVersion(ver);
            if (neuRevs != null && !neuRevs.isEmpty()) {
                ProductRevisionEntity newRev = neuRevs.get(neuRevs.size() - 1);
                newRev.setProductDefRef(old);
                int max = old.getRevisions().size() - 1;
                old.getRevisions().set(max, newRev);
            }
        }
        
        return(old);
    }
    
    
    @Override
    protected Outcome preProcess(int opCode, ProductDefinitionEntity entity) throws EntityValidationException {
        
        Outcome result = new Outcome();
        result.setResult(true);
        
        switch(opCode) {
            case ADD:
            case ADD_BULK:
                preProcessAdd(entity);
                break;
            case UPDATE:
            case UPDATE_BULK:
                preProcessUpdate(entity);
                break;
            case DELETE:
            case DELETE_BULK:
            default:
                break;
        }
        
        return(result);
    }

    @Override
    protected Outcome postProcess(int opCode, ProductDefinitionEntity entity) throws EntityValidationException {
        
        Outcome result = new Outcome();
        result.setResult(true);
        
        switch(opCode) {
            case ADD:
            case ADD_BULK:
            case UPDATE:
            case UPDATE_BULK:
            case DELETE:
            case DELETE_BULK:
            default:
                break;
        }
        
        return(result);
    }
    
    private void preProcessAdd(ProductDefinitionEntity entity) throws EntityValidationException {
        
        validate(entity);
        
        if (entity.getCompositionType() == CompositionType.COMPOSITE) {
            if (entity.getRevisions() != null && !entity.getRevisions().isEmpty()) {
                ProductRevisionEntity rev = entity.getRevisions().get(entity.getRevisions().size() - 1);
                Set<ProductCompositionEntity> comps = rev.getChildProdCompositions();
                if (comps != null && !comps.isEmpty()) {
                    Iterator<ProductCompositionEntity> iter = comps.iterator();
                    while (iter != null && iter.hasNext()) {
                        ProductCompositionEntity comp = iter.next();
                        ProductRevisionEntity childRev = comp.getChildProdRevisionRef();
                        childRev.getCompositionAsscns().add(comp);
                    }
                }
            }
        }
        
        ProductCodeGenEntity codeGen = new ProductCodeGenEntity();
        codeGen.setTenantId(getTenantId());
        codeGenRepo.save(codeGen);
        
        String s1 =  String.format("%08d", codeGen.getId());
        entity.setProductCode(s1);
        
        List<ProductRevisionEntity> revList = entity.getRevisions();
        if (revList != null && revList.size() > 0) {
            for (int i = 0; i < revList.size(); i++) {
            
                ProductRevisionEntity rev = revList.get(i);
                rev.setProductDefRef(entity);
                rev.setProductCode(entity.getProductCode());
                
                // set the variants based on last revision
                if (i == (revList.size() - 1)) {
                    
                    List<String> varNames = getVariantNames(rev);
                    Set<ProductVariantEntity> vars = getVariants(rev);
                    
                    if (vars == null || vars.isEmpty()) {
                        // no variants , so create default one
                        if (vars == null)
                            vars = new HashSet<>();
                        ProductVariantEntity var = new ProductVariantEntity();
                        var.setBaseVariant(true);
                        vars.add(var);
                    }
                    
                    if (vars.size() > 0) {
                        int count = 1;
                        for (ProductVariantEntity var : vars) {
                            String s = var.isBaseVariant() ? Constants.DEFAULT_PROD_VARIANT_CODE : String.format("%04d", count++);
                            var.setVariantCode(s);
                            var.setProductCode(entity.getProductCode());
                            var.setProductDefRef(entity);
                            var.setTenantId(getTenantId());
                            var.setRevisionControl(getRevisionControl());
                            //if (!var.isBaseVariant()) {
                                // A Product without variant should have base variant listed as sku
                                ProductSkuEntity sku = makeSku(var);
                                var.setInventorySku(sku);
                            //}
                        }
                    }

                    entity.setVariantNames(varNames);
                    entity.setVariants(vars);
                } // end if ( i == revList.size() - 1
            }
            
        }
        
        entity.setStatus("NEW");
        entity.setIsEnabled(true);
        entity.setTenantId(getTenantId());
        //entity.setRevisionControl(getRevisionControl());
    }
    
    private void preProcessUpdate(ProductDefinitionEntity entity) throws EntityValidationException {
        
        // All updates should happen on last revision id
        // So even if the input contains historical revision ids, the earlier ones will be ignored
        // and the last one be taken for update.
        
        List<ProductRevisionEntity> revs = revSvc.findByProductCode(entity.getProductCode());
        ArrayList<ProductRevisionEntity> oldRevs = new ArrayList<>(revs); // detaching the entities 
        if (entity.getRevisions() != null && entity.getRevisions().size() > 0) {
            List<ProductRevisionEntity> newRevs = entity.getRevisions();
            
            if (oldRevs.isEmpty()) {
                // TODO : Review if we need to support this case. While adding a product create the first revision mandatorily.
                oldRevs.addAll(newRevs);
            } else {
                // merge with existing revs
                for (int i=0; i < newRevs.size(); i++) {
                    ProductRevisionEntity newRev = newRevs.get(i);
                    if (newRev.getId() != null) {
                        boolean found = false; int k = -1;
                        for (int j=0; j < oldRevs.size(); j++ ) {
                            ProductRevisionEntity oldRev = oldRevs.get(j);
                            if (Objects.equals(newRev.getId(), oldRev.getId())) {
                                found = true;
                                k = j;
                                break;
                            }
                        } //end for j
                        if (found) {
                            // replace oldRev with new, only if it the last revision
                            if (k == (oldRevs.size() - 1))
                                oldRevs.set(k, newRev);
                        } else {
                            throw new EntityValidationException("Unknown revision id - " + newRev.getId());
                        }
                    } else {
                        // Do not allow new revisions i.e. with null ids , to be added here 
                        // Updates must happen on the latest revision. System will detect whether to add a new revision or not
                        
                        throw new EntityValidationException("Could not accept null revision id while update, in record sequence - " + i);
                        
                    } // end if newRev.getId == null
                    
                } // for newRevs.size
            }
            
            //entity.setRevisions(oldRevs);
            
            ArrayList<ProductRevisionEntity> oldRevs2 = new ArrayList<>();
            oldRevs2.add(oldRevs.get(oldRevs.size() - 1));
            
            // To optimize vallidation, we temporarily set the revisions to only the last one.
            // If validation passes, we restore all revisions
            entity.setRevisions(oldRevs2);
            
            validate(entity);
            
            entity.setRevisions(oldRevs);
            
        } // end if revisions != null
        
    }

    @Override
    @Transactional(readOnly=true, noRollbackFor={javax.persistence.NoResultException.class})
    public void validate(ProductDefinitionEntity entity) throws EntityValidationException {
        
        //1. Create the revisions
        if (entity.getRevisions() != null && entity.getRevisions().size() > 0) {
            
            List<ProductRevisionEntity> revisions = entity.getRevisions();
            //Date lastStart = Constants.JPA_MAX_DATE, lastEnd = Constants.JPA_MAX_DATE;
            Date lastStart = null, lastEnd = null;
            //int lastRevision = 0;
            for (int i=0; i < revisions.size(); i++) {
                
                ProductRevisionEntity rev = revisions.get(i);
                validateRevisionDates(rev.getStartDate(), rev.getEndDate(), lastStart, lastEnd);
                lastStart = rev.getStartDate();
                lastEnd = rev.getEndDate();
                
                rev.setRevisionNumber(i+1);
                
                revSvc.validate(rev);
                //if (entity.getCompositionType() == CompositionType.STANDARD) {
                //    if (rev.getCompositions() != null && rev.getCompositions().size() > 1) {
                //        throw new EntityValidationException("For product with STANDARD configuration, only one composition allowed in revision " + rev.getRevisionNumber());
                //    }
                //}
                rev.setProductDefRef(entity);
                
            }
            
        }
        
        ProductDefinitionEntity originProd = entity.getOriginProductRef(), originProd2 = null;
        if (originProd != null) {
            if (originProd.getId() == null) {
                try {
                    originProd2 = prodRepo.findByProductCode(originProd.getProductCode());
                } catch (javax.persistence.NoResultException ex) {
                    throw new EntityValidationException("Could not find origin product defintion with code - " + originProd.getProductCode());
                }
            } else {
                try {
                    originProd2 = prodRepo.findOne(originProd.getId());
                } catch (javax.persistence.NoResultException ex) {
                    throw new EntityValidationException("Could not find origin product defintion with id - " + originProd.getId());
                }
            }
            if (originProd2 ==  null)
                throw new EntityValidationException("Could not find origin product definition with code or id !");
        }
        
        // Validate product group association
        Set<ProductGroupAsscnEntity> groupAsscns = entity.getGroups();
        HashSet<ProductGroupAsscnEntity> groupAsscns2 = new HashSet<>();
        if (groupAsscns != null && groupAsscns.size() > 0) {
            
            for (ProductGroupAsscnEntity groupAsscn : groupAsscns) {
                //if (isManaged(groupAsscn))
                ProductGroupAsscnEntity groupAsscn2 = null;
                if (groupAsscn.getId() != null) {
                    // Mostly update use case. Load and validate
                    try {
                        groupAsscn2 = groupAsscnRepo.findOne(groupAsscn.getId());
                    } catch (javax.persistence.NoResultException ex) {
                        throw new EntityValidationException("Could not determine group asscn with id - " + groupAsscn.getId());
                    }
                } else {
                    groupAsscn2 = groupAsscn;
                }
                if (groupAsscn2 == null)
                    throw new EntityValidationException("Could not determine group asscn !");
                
                ProductDefinitionEntity def = groupAsscn2.getProductRef();
                if (def != null) {
                    if (entity.getId() == null) {
                        if (!Objects.equals(entity.getProductCode(), def.getProductCode()))
                            throw new EntityValidationException("Product code mismatch in group association. Current product code - " + entity.getProductCode() + ", association product code - " + def.getProductCode());

                    } else {
                        if (!Objects.equals(entity.getId(), def.getId()))
                            throw new EntityValidationException("Product id mismatch in group association. Current product id - " + entity.getId() + ", association product id - " + def.getId());
                        
                    }
                } else {
                    // no-op . The current entity will be set anyway
                }
                
                
                // check group
                ProductGroupEntity prodGroup = groupAsscn2.getGroupRef(), prodGroup2 = null;
                if (prodGroup == null) {
                    throw new EntityValidationException("Product group reference cannot be null in group association object !");
                } else {
                    if (prodGroup.getId() == null) {
                        try {
                            prodGroup2 = groupRepo.findByGroupCode(prodGroup.getGroupCode());
                        } catch (javax.persistence.NoResultException ex) {
                            throw new EntityValidationException("Could not determine product group with code - " + prodGroup.getGroupCode());
                        }
                    } else {
                        try {
                            prodGroup2 = groupRepo.findOne(prodGroup.getId());
                        } catch (javax.persistence.NoResultException ex) {
                            throw new EntityValidationException("Could not determine product group with id - " + prodGroup.getId());
                        }
                    }
                    if (prodGroup2 == null)
                        throw new EntityValidationException("Could not determine product group with id or code in group association !");
                }
                
                groupAsscn2.setProductRef(entity);
                groupAsscn2.setProductRefId(entity.getId());
                
                groupAsscn2.setGroupRef(prodGroup2);
                groupAsscn2.setGroupRefId(prodGroup2.getId());
                
                prodGroup2.getProductAsscns().add(groupAsscn2);
                
                groupAsscn2.setTenantId(getTenantId());
                groupAsscn2.setRevisionControl(getRevisionControl());
                
                groupAsscns2.add(groupAsscn2);
                
            } // end for
        } // product group association validation
        
        
        entity.setOriginProductRef(originProd2);
        entity.setGroups(groupAsscns2);
        
        this.updateTenantWithRevision(entity);
    }
    
    
    private List<String> getVariantNames(ProductRevisionEntity rev) {
        
        ArrayList<String> varNames = new ArrayList<>();
        
        List<ProductSpecEntity> specs = rev.getSpecifications();
        if (specs != null && specs.size() > 0) {
            for (ProductSpecEntity spec : specs) {
                TemplateElementEntity elem = spec.getTemplElementRef();
                if (elem != null && elem.isIsVariant())
                    varNames.add(elem.getElementName());
            } // end for
        }
        return(varNames);
    }
    
    private Set<ProductVariantEntity> getVariants(ProductRevisionEntity rev) throws EntityValidationException {
        
        
        List<List<Tuple>> variants = new ArrayList<>();
        
        
        List<ProductSpecEntity> specs = rev.getSpecifications();
        if (specs != null || specs.size() > 0) {
            for (ProductSpecEntity spec : specs) {
                TemplateElementEntity elem = spec.getTemplElementRef();
                if (elem.isIsVariant()) {
                    ArrayList<Tuple> tupleList = new ArrayList<>();
                    VariantInfoBean varInfo = spec.getVariantInfo();
                    if (varInfo != null && varInfo.getVariantValues() != null && varInfo.getVariantValues().size() > 0) {
                        for (ListOfValuesBean lov : varInfo.getVariantValues()) {
                            Tuple tuple = new Tuple();
                            tuple.elementName = elem.getElementName();
                            tuple.attributeValue = lov.getLovValue();
                            tupleList.add(tuple);
                        }
                        variants.add(tupleList);
                    }
                } // end if element is variant
            }
        } // end if specs != null
                
            
        
        Set<ProductVariantEntity> prodVars = new HashSet<>();
        
        if (variants.size() > 0) {
            List<List<Tuple>> product = Utils.cartesianProduct(variants);
            for (int i=0; product != null && i < product.size(); i++) {
                List<Tuple> tuples = product.get(i);
                ProductVariantEntity prodVar = new ProductVariantEntity();
                boolean found = false;
                for (int j = 0; tuples != null && j < tuples.size(); j++) {
                    Tuple tuple = tuples.get(j);
                    setVariantFieldValue(prodVar, tuple.elementName, tuple.attributeValue, j+1);
                    prodVar.setIsEnabled(true);
                    found = true;
                }
                if (found)
                    prodVars.add(prodVar);
            }
        }
        
        return(prodVars);
    }
    
    private void setVariantFieldValue(ProductVariantEntity variant, String elementName, String attributeValue, int index) throws EntityValidationException {
        
        if (index < 1 || index > 8)
            throw new EntityValidationException("No field with index " + index + ". Index should be between 1 and 8");
        
        Field f1 = null;
        try {
            f1 = variant.getClass().getDeclaredField("elementName" + index);
            f1.setAccessible(true);
            f1.set(variant, elementName);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException  ex) {
            throw new EntityValidationException("Error while accessing field - " + elementName + index, ex);
        }
        
        Field f2 = null;
        try {
            f2 = variant.getClass().getDeclaredField("attributeValue" + index);
            f2.setAccessible(true);
            f2.set(variant, attributeValue);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException  ex) {
            throw new EntityValidationException("Error while accessing field - " + elementName + index, ex);
        }
        
    }
    
    private ProductSkuEntity makeSku(ProductVariantEntity variant) {
        
        ProductSkuEntity sku = new ProductSkuEntity();
        
        SkuBean bean = new SkuBean();
        //TODO: Instead of defaulting a constant value for all, there has to be a configurable 
        //      default value for each tenant / merchant
        bean.setFamily(Constants.DEFAULT_PRODUCT_CATEGORY);
        if (variant.getProductDefRef() != null)
            bean.setProduct(variant.getProductDefRef().getProductCode());
        bean.setVariant(variant.getVariantCode());
        
        sku.setSku(bean);
        sku.setIsEnabled(true);
        sku.setStatus("NEW");
        sku.setTenantId(getTenantId());
        sku.setRevisionControl(getRevisionControl());
        
        return(sku);
    }
    
    private boolean checkHistoricalUpdate(ProductDefinitionEntity neu, ProductDefinitionEntity old) {
        
        boolean result = false;
        
        if (!Objects.equals(old.getCompositionType(), neu.getCompositionType()))
            return(true);
        
        List<ProductRevisionEntity> newRevs = neu.getRevisions();
        List<ProductRevisionEntity> oldRevs = old.getRevisions();
        ProductRevisionEntity oldRev = null, newRev = null;
        if (newRevs != null && newRevs.size() > 0) {
            newRev = newRevs.get(newRevs.size() - 1);
        }
        if (oldRevs != null && oldRevs.size() > 0) {
            oldRev = oldRevs.get(oldRevs.size() - 1);
        }
        if (oldRev == null) {
            if (newRev != null)
                result = true;
        } else {
            if (newRev != null) {
                // Pre-processing will ensure, newRev & oldRev have same id 
                Set<ProductCompositionEntity> oldComps = oldRev.getChildProdCompositions();
                Set<ProductCompositionEntity> newComps = newRev.getChildProdCompositions();
                if ( ! isEquivalent(newComps, oldComps) ) {
                    result = false;
                }
            }
        }
        
        return(result);
    }
    
    private boolean isEquivalent(ProductRevisionEntity neu, ProductRevisionEntity old) {
        
        boolean result = false;
        
        if (old.getProductDefRef().getCompositionType() == CompositionType.STANDARD) {
            
            ProductTemplateEntity newTemplate = neu.getProdTemplateRef();
            ProductTemplateEntity oldTemplate = old.getProdTemplateRef();
            if (!Objects.equals(oldTemplate.getId(), neu.getId()))
                result = true;
            
            List<ProductSpecEntity> newSpec = neu.getSpecifications();
            List<ProductSpecEntity> oldSpec = old.getSpecifications();
            
            if (isEquivalent(newSpec, oldSpec))
                result = true;
            
        } else {
            
            Set<ProductCompositionEntity> newComps = neu.getChildProdCompositions();
            Set<ProductCompositionEntity> oldComps = old.getChildProdCompositions();
            
            if (isEquivalent(newComps, oldComps))
                result = true;
            
        } // end if composition type 
        
        return(result);
    }
    
    private boolean isEquivalent(Set<ProductCompositionEntity> neu, Set<ProductCompositionEntity> old) {
        
        boolean result = true;
        
        if (neu.size() != old.size())
            return false;
        
        Iterator<ProductCompositionEntity> iterNew = neu.iterator();
        while (iterNew != null && iterNew.hasNext()) {
            ProductCompositionEntity newComp = iterNew.next();
            Iterator<ProductCompositionEntity> iterOld = old.iterator();
            boolean found = true;
            while (iterOld != null && iterOld.hasNext()) {
                ProductCompositionEntity oldComp = iterOld.next();
                if (Objects.equals(oldComp.getId(), newComp.getId())) {
                    found = true;
                    /*
                    if (!Objects.equals(oldComp.getProdTemplateRef().getId(), newComp.getProdTemplateRef().getId())) {
                        return(false);
                    } else {
                        if ( ! isEquivalent(newComp.getSpecifications(), oldComp.getSpecifications()) )
                            return(false);
                    }
                    */
                    if (!Objects.equals(oldComp.getChildProdRevisionRef().getId(), newComp.getChildProdRevisionRef().getId()))
                        return(false);
                }
            }
            if (!found) {
                return(false);
            }
        }
        
        return(result);
    }
    
    private boolean isEquivalent(List<ProductSpecEntity> neu, List<ProductSpecEntity> old) {
        
        boolean result = false;
        
        if (old == null || old.isEmpty()) {
            if (neu == null || neu.isEmpty())
                return(true);
            else
                return(false);
        } else {
            if (neu == null || neu.isEmpty())
                return(false);
            else {
                if (old.size() != neu.size())
                    return(false);
                else {
                    for (int i=0; i < neu.size(); i++) {
                        ProductSpecEntity newSpec = neu.get(i);
                        boolean found = false;
                        for (int j=0; j < old.size(); j++) {
                            ProductSpecEntity oldSpec = old.get(j);
                            if (Objects.equals(oldSpec.getId(), newSpec.getId())) {
                                found = true;
                                if (!Objects.equals(oldSpec.getTemplElementRef().getId(), newSpec.getTemplElementRef().getId())) {
                                    return(false);
                                } else {
                                    HashSet<String> oldValues = new HashSet<>(oldSpec.getAttributeValues());
                                    HashSet<String> newValues = new HashSet<>(newSpec.getAttributeValues());
                                    if (!newValues.containsAll(oldValues))
                                        return false;
                                    
                                    if (!oldValues.containsAll(newValues))
                                        return(false);
                                }
                            }
                        } // end for old.size 
                        if (!found)
                            return(false);
                        
                    } // end for neu.size
                } //end if old.size != neu.size
            }
        }
        
        return(result);
    }
    
    private void addRevision(ProductRevisionEntity rev) {
        
        // nullify all IDs
        
        Set<ProductCompositionEntity> comps = rev.getChildProdCompositions();
        if (comps != null && comps.size() > 0) {
            Iterator<ProductCompositionEntity> compIter = comps.iterator();
            while (compIter != null && compIter.hasNext()) {
                ProductCompositionEntity comp = compIter.next();
                comp.setParentProdRevisionRef(rev);
                comp.setId(null);
                /*
                List<ProductSpecEntity> specs = comp.getSpecifications();
                if (specs != null && specs.size() > 0) {
                    for (ProductSpecEntity spec : specs) {
                        spec.setId(null);
                        comp.setTenantId(getTenantId());
                        spec.setRevisionControl(getRevisionControl());
                    }
                }
                */
                //Set<ProductCompositionEntity> bundledProds = rev.getBundleAsscns();
                // nothing to change for bundledProds
                
                comp.setTenantId(getTenantId());
                comp.setRevisionControl(getRevisionControl());
            }
        }
        
        List<ProductSpecEntity> specs = rev.getSpecifications();
        if (specs != null && specs.size() > 0) {
            for (ProductSpecEntity spec : specs) {
                spec.setId(null);
                
                spec.setRevisionControl(getRevisionControl());
            }
        }
        
        revRepo.save(rev);
    }
    
    class Tuple {
        
        String elementName;
        
        String attributeValue;
        
    }

}
