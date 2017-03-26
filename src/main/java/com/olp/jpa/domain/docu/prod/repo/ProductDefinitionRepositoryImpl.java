/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa.domain.docu.prod.repo;

import com.olp.fwk.common.Constants;
import com.olp.jpa.common.AbstractRepositoryImpl;
import com.olp.jpa.domain.docu.prod.model.ProductDefinitionEntity;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author raghosh
 */
@Repository("prodDefinitionRepository")
public class ProductDefinitionRepositoryImpl extends AbstractRepositoryImpl<ProductDefinitionEntity, Long> implements ProductDefinitionRepository {

    @Override
    public String getLazyLoadElements() {
        return(" JOIN FETCH t.groups t2 JOIN FETCH t.originProductRef t3 JOIN FETCH t.revisions t4 JOIN FETCH t.variants t5 JOIN FETCH t.lobs t6 ");
    }

    @Override
    @Transactional(readOnly=true, noRollbackFor={javax.persistence.NoResultException.class})
    public ProductDefinitionEntity findByProductCode(String skuCode) {
        
        TypedQuery<ProductDefinitionEntity> query = getEntityManager().createNamedQuery("ProductDef.findByCode", ProductDefinitionEntity.class);
        query.setParameter("code", skuCode);
        query.setParameter("tenant", getTenantId());
        query.setParameter("global", Constants.GLOBAL_TENANT_ID);
        
        ProductDefinitionEntity result = query.getSingleResult();
        
        return(result);
    }

    //@Override
    //public List<ProductEntity> findSelfNChildren(String code) {
    //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    //}
    
}
