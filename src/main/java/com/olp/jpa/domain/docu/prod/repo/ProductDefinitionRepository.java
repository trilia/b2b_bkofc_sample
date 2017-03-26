/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa.domain.docu.prod.repo;

import com.olp.jpa.common.ITextRepository;
import com.olp.jpa.domain.docu.prod.model.ProductDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 *
 * @author raghosh
 */
@NoRepositoryBean
public interface ProductDefinitionRepository extends JpaRepository<ProductDefinitionEntity, Long>, ITextRepository<ProductDefinitionEntity, Long> {
    
    public ProductDefinitionEntity findByProductCode(String code);
    
    //public List<ProductEntity> findSelfNChildren(String code);
    
}
