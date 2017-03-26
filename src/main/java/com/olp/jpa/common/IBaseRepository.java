/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa.common;

import java.io.Serializable;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 *
 * @author raghosh
 * @param <T>
 * @param <ID>
 */
@NoRepositoryBean
public interface IBaseRepository<T extends Object, ID extends Serializable> extends JpaRepository<T, ID>, ITextRepository<T, ID> {
    
    public EntityManager getEntityManager();
    
    public void detach(T entity);
    
    public boolean isManaged(T entity);
}
