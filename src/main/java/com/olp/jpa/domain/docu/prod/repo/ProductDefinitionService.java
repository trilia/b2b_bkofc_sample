/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa.domain.docu.prod.repo;

import com.olp.fwk.common.error.EntityValidationException;
import com.olp.jpa.common.IJpaService;
import com.olp.jpa.domain.docu.prod.model.ProductDefinitionEntity;
import org.springframework.data.repository.NoRepositoryBean;

/**
 *
 * @author raghosh
 */
@NoRepositoryBean
public interface ProductDefinitionService extends IJpaService<ProductDefinitionEntity, Long> {
    
    public ProductDefinitionEntity findByProductCode(String code);
    
    //public List<ProductEntity> addProductWithVariants(ProductEntity baseProduct, List<VariantInfoBean> variants);
    
    public void validate(ProductDefinitionEntity entity) throws EntityValidationException;
    
}
