/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa.common;

import com.olp.annotations.History;
import com.olp.jpa.util.JpaUtil;
import com.olp.annotations.MultiTenant;
import com.olp.annotations.SortCriteria;
import com.olp.fwk.common.Constants;
import com.olp.fwk.common.ContextManager;
import com.olp.fwk.common.IContext;
import com.olp.fwk.common.MultiTenantAccess;
import com.olp.fwk.common.error.EntityUCException;
import com.olp.fwk.datagrid.Grid;
import com.olp.fwk.datagrid.GridProvider;
import com.olp.jpa.util.LuceneUtil;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import org.apache.lucene.search.SortField;
import org.hibernate.Session;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.FuzzyContext;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 *
 * @author raghosh
 * @param <T>
 * @param <ID>
 */
@NoRepositoryBean
public abstract class AbstractRepositoryImpl<T extends Object, ID extends Serializable> implements /* JpaRepository<T, ID>, ITextRepository<T, ID> */
                                                                                         IBaseRepository<T, ID> {
    
    @Autowired
    private EntityManager __entityManager;
    
    @Autowired
    @Qualifier("mongoQuery")
    private NativeQueryBuilder<T> __mongoQueryBuilder;
    
    @Autowired
    @Qualifier("neo4jQuery")
    private NativeQueryBuilder<T> __neo4jQueryBuilder;
    
    //@Transactional
    public EntityManager getEntityManager() {
        return __entityManager;
    }

    
    //protected void setEntityManager(EntityManager entityManager) {
    //    this.__entityManager = entityManager;
    //}
    
    protected String getTenantId() {
        
        IContext ctx = ContextManager.getContext();
        String tid = ctx.getTenantId();
        
        return(tid);
    }
    
    @Override
    @Transactional(readOnly=true, noRollbackFor={javax.persistence.NoResultException.class})
    public List<T> findAll() {
        
        if (__entityManager == null)
            throw new IllegalStateException("Null Entity Manager found. Aborting !");
        
        Class<T> clazz = getBeanClass();

        IContext ctx = ContextManager.getContext();
        String tid = ctx.getTenantId();
        
        String className = clazz.getName();
        String shortClassName = className == null ? null : className.substring(className.lastIndexOf('.')+1);
        
        MtInfo mtf = getMtInfo(clazz);
        History hist = clazz.getAnnotation(History.class);
        
        QueryFragment qf = makeFindQueryFragment(shortClassName, mtf, hist, false);
        
        //String listQuery = makeFindQueryFragment(shortClassName, mtf, hist);
        
        String sortStr = makeDefaultSortString(clazz, "t");
        if (sortStr != null || "".equals(sortStr)) {
            qf.queryBuff.append(" ").append(sortStr);
        }
        
        TypedQuery<T> query = __entityManager.createQuery(qf.queryBuff.toString(), clazz);
        
        if (mtf.mtEnabled && qf.whereClauseExists) {
            query.setParameter("tenant", tid);
        }
        
        List<T> list = query.getResultList();
        
        return(list);
    }

    @Override
    @Transactional(readOnly=true)
    public List<T> findAll(Sort sort) {
        
        if (__entityManager == null)
                throw new IllegalStateException("Null Entity Manager found. Aborting !");
        
        Class<T> clazz = getBeanClass();
        String orderByStr = getSortString(sort, "t");
        if (orderByStr == null || "".equals(orderByStr))
            orderByStr = makeDefaultSortString(clazz, "t");
        
        IContext ctx = ContextManager.getContext();
        String tid = ctx.getTenantId();
        
        String className = clazz.getName();
        String shortClassName = className == null ? null : className.substring(className.lastIndexOf('.')+1);
        
        MtInfo mtf = getMtInfo(clazz);
        History hist = clazz.getAnnotation(History.class);
        
        QueryFragment qf = makeFindQueryFragment(shortClassName, mtf, hist, false);
        
        //String listQuery = makeFindQueryFragment(shortClassName, mtf, hist);
        
        qf.queryBuff.append(" ").append(orderByStr);
        
        TypedQuery<T> query = __entityManager.createQuery(qf.queryBuff.toString(), clazz);
        
        if (mtf.mtEnabled && qf.whereClauseExists) {
            query.setParameter("tenant", tid);
        }
        
        List<T> list = query.getResultList();
        
        return(list);
    }

    @Override
    @Transactional(readOnly=true)
    public List<T> findAll(Iterable<ID> itrbl) {
        
        if (__entityManager == null)
                throw new IllegalStateException("Null Entity Manager found. Aborting !");
        
        Class<T> clazz = getBeanClass();

        IContext ctx = ContextManager.getContext();
        String tid = ctx.getTenantId();
        
        String className = clazz.getName();
        String shortClassName = className == null ? null : className.substring(className.lastIndexOf('.')+1);
        
        MtInfo mtf = getMtInfo(clazz);
        History hist = clazz.getAnnotation(History.class);
        
        QueryFragment qf = makeFindQueryFragment(shortClassName, mtf, hist, false);
        
        //String listQuery = makeFindQueryFragment(shortClassName, mtf, hist);
        
        if (qf.whereClauseExists) {
            qf.queryBuff.append(" AND t.id IN :IDS ");
        } else {
            qf.queryBuff.append(" WHERE t.id IN :IDS ");
        }
        
        ArrayList<ID> idList = new ArrayList<>();
        if (itrbl != null) {
            Iterator<ID> iter = itrbl.iterator();
            while (iter.hasNext()) {
                //TODO: try to implement better strategy than looping
                ID id = iter.next();
                idList.add(id);
            }
        }
        
        TypedQuery<T> query = __entityManager.createQuery(qf.queryBuff.toString(), clazz);
        
        query.setParameter("IDS", idList);
        
        if (mtf.mtEnabled && qf.whereClauseExists) {
            query.setParameter("tenant", tid);
        }
        
        List<T> list = query.getResultList();
        
        return(list);
        
    }
    
    @Override
    public <S extends T> List<S> findAll(Example<S> exmpl) {
        throw new UnsupportedOperationException("Not yet implemented !");
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> exmpl, Sort sort) {
        throw new UnsupportedOperationException("Not yet implemented !");
    }
    
    @Override
    public <S extends T> S findOne(Example<S> exmpl) {
        throw new UnsupportedOperationException("Not yet implemented !");
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> exmpl, Pageable pgbl) {
        throw new UnsupportedOperationException("Not yet implemented !");
    }

    @Override
    public <S extends T> long count(Example<S> exmpl) {
        throw new UnsupportedOperationException("Not yet implemented !");
    }
    
    @Override
    public <S extends T> boolean exists(Example<S> exmpl) {
        throw new UnsupportedOperationException("Not yet implemented !");
    }
    
    @Transactional(readOnly=true)
    public List<T> findAllWithHistory() {
        if (__entityManager == null)
            throw new IllegalStateException("Null Entity Manager found. Aborting !");
        
        Class<T> clazz = getBeanClass();

        IContext ctx = ContextManager.getContext();
        String tid = ctx.getTenantId();
        
        String className = clazz.getName();
        String shortClassName = className == null ? null : className.substring(className.lastIndexOf('.')+1);
        
        MtInfo mtf = getMtInfo(clazz);
        History hist = clazz.getAnnotation(History.class);
        
        QueryFragment qf = makeFindQueryFragment(shortClassName, mtf, hist, true);
        
        //String listQuery = makeFindQueryFragment(shortClassName, mtf, hist);
        
        String sortStr = makeDefaultSortString(clazz, "t");
        if (sortStr != null || "".equals(sortStr)) {
            qf.queryBuff.append(" ").append(sortStr);
        }
        
        TypedQuery<T> query = __entityManager.createQuery(qf.queryBuff.toString(), clazz);
        
        if (mtf.mtEnabled && qf.whereClauseExists) {
            query.setParameter("tenant", tid);
        }
        
        List<T> list = query.getResultList();
        
        return(list);
    }
    
    @Override
    @Transactional
    public <S extends T> S save(S s) {
        
        Logger logger = Logger.getLogger(getClass().getName());
        
        if (s == null) {
            logger.log(Level.WARNING, "Nothing to save !!");
        } else {
            if (__entityManager == null)
                throw new IllegalStateException("Null Entity Manager found. Aborting !");
            
            IContext ctx = ContextManager.getContext();
            String sessionTid = ctx.getTenantId();
            
            MtInfo mtf = getMtInfo(s);
            String suppliedTid = mtf.tenantIdValue;
            
            if (mtf.mtEnabled) {
                if (suppliedTid ==  null || "".equals(suppliedTid)) {
                    
                    mtf.mtField.setAccessible(true);
                    try {
                        mtf.mtField.set(s, sessionTid);
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        logger.log(Level.SEVERE, "Could not set the tenant id for this domain object !");
                        throw new RuntimeException("Could not set the tenant id for this domain object !");
                    }
                
                } else {
                    String clazzStr = "import com.olp.jpa.domain.docu.be.model.MerchantEntity";
                    ClassLoader loader = this.getClass().getClassLoader();
                    Class clazz = null;
                    try {
                        clazz = loader.loadClass(clazzStr);
                    } catch (ClassNotFoundException ex) {
                        logger.log(Level.SEVERE, "Could not load class " + clazzStr, ex);
                        throw new RuntimeException("Could not load class " + clazzStr);
                    }
                    
                    if (!sessionTid.equals(suppliedTid) && !(s.getClass().isAssignableFrom(clazz))) {
                        logger.log(Level.SEVERE, "The session tenant id does not match the supplied tenant id - {0}", suppliedTid);
                        throw new RuntimeException("The session tenant id does not match the supplied tenant id - " + suppliedTid);
                    }
                }
            }
            
            if (!__entityManager.contains(s)) {
                try {
                    __entityManager.persist(s); 
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "Exception while saving entity !", t);
                    throw t;
                }
            }
            
        }
        
        return(s);
    }


    @Override
    @Transactional
    public <S extends T> List<S> save(Iterable<S> itrbl) {
        
        Logger logger = Logger.getLogger(getClass().getName());
        
        // This should be implemented with some native bulk api
        int i = 0;
        ArrayList<S> list = new ArrayList<>();
        if (itrbl != null) {
            Iterator<S> iter = itrbl.iterator();
            while (iter != null && iter.hasNext()) {
                ++i;
                S s = iter.next();
                try {
                    save(s);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "Could not save the entity instance !", t);
                    --i;
                }
                list.add(s);
            }
        }
        
        logger.log(Level.FINEST, "Number of records affected - {0}", i);
        
        return(list);
    }

    @Override
    @Transactional
    public void flush() {
        if (__entityManager == null)
            throw new IllegalStateException("Null Entity Manager found. Aborting !");
        __entityManager.flush();
    }

    @Override
    @Transactional
    public <S extends T> S saveAndFlush(S s) {
        
        if (__entityManager == null)
            throw new IllegalStateException("Null Entity Manager found. Aborting !");
        
        save(s);
        __entityManager.flush();
        
        return(s);
    }

    @Override
    @Transactional
    public void deleteInBatch(Iterable<T> itrbl) {
        delete(itrbl);
    }

    @Override
    @Transactional
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    @Transactional(readOnly=true)
    public T getOne(ID id) {
        
        T t = findOne(id);
        
        return(t);
    }

    @Override
    @Transactional(readOnly=true)
    public Page<T> findAll(Pageable pgbl) {
        
        if (__entityManager == null)
            throw new IllegalStateException("Null Entity Manager found. Aborting !");
        
        PageImpl<T> page = null;
        if (pgbl != null) {
            
            Class<T> clazz = getBeanClass();
            IContext ctx = ContextManager.getContext();
            String tid = ctx.getTenantId();
            
            int offset = pgbl.getOffset(); 
            int pgnum = pgbl.getPageNumber();
            int pgsize = pgbl.getPageSize(); if (pgsize == 0) pgsize = Constants.DEFAULT_PAGE_SIZE;
            
            Sort sort = pgbl.getSort();
            String orderByStr = getSortString(sort, "t");
            if (orderByStr == null || "".equals(orderByStr))
                orderByStr = makeDefaultSortString(clazz, "t");

            String className = clazz.getName();
            String shortClassName = className == null ? null : className.substring(className.lastIndexOf('.')+1);

            MtInfo mtf = getMtInfo(clazz);
            History hist = clazz.getAnnotation(History.class);
            
            QueryFragment qf = makeFindQueryFragment(shortClassName, mtf, hist, false);
            
            qf.queryBuff.append(" ").append(orderByStr);

            TypedQuery<T> query = __entityManager.createQuery(qf.queryBuff.toString(), clazz);
            query.setFirstResult(pgnum*pgsize);
            query.setMaxResults(pgsize);

            if (mtf.mtEnabled && qf.whereClauseExists) {
                query.setParameter("tenant", tid);
            }

            List<T> list = query.getResultList();
            
            Long count = count();   // TODO: Currently pagination involves two queries. Try to optimize later
            
            page = new PageImpl(list, pgbl, count);
            
        } else {
            List<T> list = findAll();
            page = new PageImpl(list);
        }
        
        return(page);
    }

    @Override
    @Transactional(readOnly=true, noRollbackFor={javax.persistence.NoResultException.class})
    public T findOne(ID id) {
        
        if (__entityManager == null)
                throw new IllegalStateException("Null Entity Manager found. Aborting !");
        
        Class<T> clazz = getBeanClass();
        
        IContext ctx = ContextManager.getContext();
        String tid = ctx.getTenantId();
        
        String className = clazz.getName();
        String shortClassName = className == null ? null : className.substring(className.lastIndexOf('.')+1);
        
        MtInfo mtf = getMtInfo(clazz);
        History hist = clazz.getAnnotation(History.class);
        
        QueryFragment qf = makeFindQueryFragment(shortClassName, mtf, hist, false);
        if (qf.whereClauseExists) {
            qf.queryBuff.append(" AND t.id = :idvalue ");
        } else {
            qf.queryBuff.append(" WHERE t.id = :idvalue ");
        }
        
        TypedQuery<T> query = __entityManager.createQuery(qf.queryBuff.toString(), clazz);
        
        query.setParameter("idvalue", id);
        if (mtf.mtEnabled && qf.whereClauseExists)  {
            query.setParameter("tenant", tid);
        }
        
        T bean = query.getSingleResult();
        
        return(bean);
    }

    @Override
    public boolean exists(ID id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*
    * Hibernate OGM does not support Criteria Queries. Hence re-written as simple JPQL 
    * 
    @Override
    public long count() {
        
        if (__entityManager == null)
                throw new IllegalStateException("Null Entity Manager found. Aborting !");
        
        Class<T> clazz = getBeanClass();

        IContext ctx = ContextManager.getContext();
        String tid = ctx.getTenantId();
               
        CriteriaBuilder cb = __entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> domain = cq.from(clazz);
        cq.select(cb.count(domain));
        
        MtInfo mtf = getMtInfo(clazz);
        ParameterExpression<String> p1 = cb.parameter(String.class);
        ParameterExpression<String> p2 = cb.parameter(String.class);
        if (mtf.mtEnabled) {
            if (mtf.mtLevel == MultiTenant.Levels.ONE_TENANT) {
                cq.where(cb.equal(domain.get(mtf.mtFieldName), p1));
            } else if (mtf.mtLevel == MultiTenant.Levels.ONE_N_GLOBAL) {
                cq.where(
                    cb.or(
                        cb.equal(domain.get(mtf.mtFieldName), p1), cb.equal(domain.get(mtf.mtFieldName), p2)
                    )
                );
            } else {
                // Should not come here .. throw exception
                throw new RuntimeException("Inconsistent multi-tenancy information for JPA bean " + clazz.getName());
            }
        } else {
            //no-op
        }
        
        TypedQuery<Long> countQuery = __entityManager.createQuery(cq);
        
        if (mtf.mtEnabled) {
            if (mtf.mtLevel == MultiTenant.Levels.ONE_TENANT) {
                countQuery.setParameter(p1, tid);
            } else if (mtf.mtLevel == MultiTenant.Levels.ONE_N_GLOBAL) {
                countQuery.setParameter(p1, tid);
                countQuery.setParameter(p2, "1001");
            } else {
                // Should not come here .. throw exception
                throw new RuntimeException("Inconsistent multi-tenancy information for JPA bean " + clazz.getName());
            }
        }
        
        Long count = countQuery.getSingleResult();
        
        return(count);
    }
    */
    
    @Transactional(readOnly=true)
    public List<T> findByForeignIdRef(String idField, String idFieldValue) {
        
        // Currently JP-QL queries with foreign key in where clause is not supported in OGM
        // https://forum.hibernate.org/viewtopic.php?f=31&t=1039494
        // https://hibernate.atlassian.net/browse/OGM-825
        
        Map<String, Object> map = __entityManager.getEntityManagerFactory().getProperties();
        
        Class<T> clazz = getBeanClass();
        MtInfo mtf = getMtInfo(clazz);
        List<T> list = null;
        
        String puName = (String) map.get("hibernate.ejb.persistenceUnitName");
        String strategy = (String) map.get("hibernate.ejb.naming_strategy");
        String datastore = null;
        
        if ((strategy != null && strategy.endsWith("OgmNamingStrategy"))) {
            // OGM type
            datastore = (String) map.get("hibernate.ogm.datastore.provider");
            if ("mongodb".equals(datastore)) {
                list = __mongoQueryBuilder.findByIdRef(clazz, mtf, idField, idFieldValue);
            }
        } else {
            // ORM Type
            IContext ctx = ContextManager.getContext();
            String tid = ctx.getTenantId();
            
            String className = clazz.getName();
            String shortClassName = className == null ? null : className.substring(className.lastIndexOf('.')+1);
            String s = sanitizeInput(idField);
            String queryStr = "SELECT t FROM " + shortClassName + " t WHERE " + s + " = :idval";
            
            Field f = mtf.mtField;
            String tenantFieldName = mtf.mtFieldName;
        
            if (mtf.mtEnabled) {
                if (mtf.mtLevel == MultiTenant.Levels.ONE_TENANT) {
                    queryStr += " AND t." + tenantFieldName + " = :tenant ";
                } else if (mtf.mtLevel == MultiTenant.Levels.ONE_N_GLOBAL) {
                    queryStr += " AND (t." + tenantFieldName + " = :tenant OR t." + tenantFieldName + " = '" + Constants.GLOBAL_TENANT_ID + "')";
                } else {
                    // should not come here. Throw exception
                    throw new RuntimeException("Inconsistent multi-tenancy information for JPA bean " + clazz.getName());
                }
            }
            
            TypedQuery<T> query = __entityManager.createQuery(queryStr, clazz);
            query.setParameter("idval", idFieldValue);
            if (mtf.mtEnabled) {
                query.setParameter("tenant", tid);
            }
            
            list = query.getResultList();
            
        }
        
        return(list);
    }
    
    @Override
    @Transactional(readOnly=true)
    public long count() {
        
        // Currently JP-QL aggregate queries are not supported in OGM 
        // Hence we'll use native queries depending on the datastore
                
        Map<String, Object> map = __entityManager.getEntityManagerFactory().getProperties();
        
        Class<T> clazz = getBeanClass();
        MtInfo mtf = getMtInfo(clazz);
        Long count = -1L;
        
        String puName = (String) map.get("hibernate.ejb.persistenceUnitName");
        String strategy = (String) map.get("hibernate.ejb.naming_strategy");
        String datastore = null;
        if ((strategy != null && strategy.endsWith("OgmNamingStrategy"))) {
            // OGM type
            datastore = (String) map.get("hibernate.ogm.datastore.provider");
            if ("mongodb".equals(datastore)) {
                count = __mongoQueryBuilder.count(clazz, mtf);
            }
        } else {
            // ORM Type
            IContext ctx = ContextManager.getContext();
            String tid = ctx.getTenantId();
            
            String className = clazz.getName();
            String shortClassName = className == null ? null : className.substring(className.lastIndexOf('.')+1);
            String countQuery = "SELECT COUNT(t.id) FROM " + shortClassName + " t ";
        
            Field f = mtf.mtField;
            String tenantFieldName = mtf.mtFieldName;
        
            if (mtf.mtEnabled) {
                if (mtf.mtLevel == MultiTenant.Levels.ONE_TENANT) {
                    countQuery += " WHERE t." + tenantFieldName + " = :tenant ";
                } else if (mtf.mtLevel == MultiTenant.Levels.ONE_N_GLOBAL) {
                    countQuery += " WHERE (t." + tenantFieldName + " = :tenant OR t." + tenantFieldName + " = '" + Constants.GLOBAL_TENANT_ID + "')";
                } else {
                    // should not come here. Throw exception
                    throw new RuntimeException("Inconsistent multi-tenancy information for JPA bean " + clazz.getName());
                }
            }

            TypedQuery<Long> query = __entityManager.createQuery(countQuery, Long.class);

            if (mtf.mtEnabled) {
                query.setParameter("tenant", tid);
            }
            
            count = query.getSingleResult();
        }
        
        return(count);
    }
    
    
    @Override
    @Transactional
    public void delete(ID id) {
        
        Logger logger = Logger.getLogger(getClass().getName() + " delete(ID)");
        
        if (id == null)
            throw new RuntimeException("Null object instance id received. Aborting !!");
        
        T object = findOne(id);
        if (object == null)
            throw new RuntimeException("No object with id " + id + " found !!");
        
        delete(object);
        
        logger.log(Level.FINEST, "Number of records affected - {0}", 1);
        
    }

    @Override
    @Transactional
    public void delete(T entity) {
        
        Logger logger = Logger.getLogger(getClass().getName() + " delete(T)");
        
        if (entity == null)
            throw new RuntimeException("Null object instance received. Aborting !!");
        
        //Class<T> clazz = getBeanClass();
        Class<? extends Object> clazz = entity.getClass();

        validateTenantIdAccess(entity, false);
        
        int i = 1;
        try {
            __entityManager.remove(entity);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exception while deleting entity instance !", t);
            --i;
            throw t;
        }
        
        logger.log(Level.FINEST, "Number of records affected - {0}", i);
        
    }

    @Override
    @Transactional
    public void delete(Iterable<? extends T> itrbl) {
        
        Logger logger = Logger.getLogger(getClass().getName() + " delete(Iterable)");
        int i = 0;
        if (itrbl == null) {
            logger.log(Level.WARNING, "Nothing to delete !!");
            //throw new RuntimeException("Nothing to delete !!");
        } else {
            Iterator<? extends T> iter = itrbl.iterator();
            while (iter != null && iter.hasNext()) {
                ++i;
                T object = iter.next();
                try {
                    delete(object);
                } catch (Throwable t) {
                    logger.log(Level.WARNING, "Could not delete entity instance !", t);
                    --i; // reduce count of deleted records
                    throw t; // Always rethrow this exception as it may have far reaching implications, causing data inconsistency
                }
            }
        }
        logger.log(Level.FINEST, "Number of records affected - {0}", i);
    }

    @Override
    @Transactional
    public void deleteAll() {
        
        //Should be implemented using DELETE syntax when OGM starts supporting
        
        Logger logger = Logger.getLogger(getClass().getName());
        
        List<T> list = findAllWithHistory();
        int i = 0;
        if (list == null) {
            logger.log(Level.WARNING, "Nothing to delete !!");
            //throw new RuntimeException("Nothing to delete !!");
        } else {
            Iterator<T> iter = list.iterator();
            while (iter != null && iter.hasNext()) {
                T object = iter.next();
                try {
                    delete(object);
                } catch (Throwable t) {
                    logger.log(Level.WARNING, "Could not delete object", t);
                    --i;
                    throw t; // Always rethrow this exception as it may have far reaching implications, causing data inconsistency
                }
            }
        }
        logger.log(Level.FINEST, "Number of records affected - {0}", i);
    }
    
    @Override
    public void detach(T entity) {
        if (__entityManager.contains(entity))
            __entityManager.detach(entity);
    }
    
    @Override
    public boolean isManaged(T entity) {
        boolean result = __entityManager.contains(entity);
        return(result);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<T> findText(String keywords, boolean fuzzy, Sort sortCriteria) {
        
        Logger logger = Logger.getLogger(getClass().getName());
        
        Class<T> clazz = getBeanClass();
        
        FullTextQuery jpaQuery = makeTextQuery(clazz, keywords, fuzzy, sortCriteria, logger);
        
        List<T> result = jpaQuery.getResultList();
        
        int count = result == null ? 0 : result.size();
        
        logger.log(Level.FINE, "{0} rows matched.", count);
        
        return(result);
    }
    
    @Override
    public Page<T> findText(String keywords, boolean fuzzy, Pageable pgbl) {
        
        Logger logger = Logger.getLogger(getClass().getName());
        
        Class<T> clazz = getBeanClass();
        
        Sort sort = pgbl == null ? null : pgbl.getSort();
        
        FullTextQuery jpaQuery = makeTextQuery(clazz, keywords, fuzzy, sort, logger);
        
        if (pgbl != null) {
            int offset = pgbl.getOffset(); 
            int pgnum = pgbl.getPageNumber();
            int pgsize = pgbl.getPageSize(); if (pgsize == 0) pgsize = Constants.DEFAULT_PAGE_SIZE;
            jpaQuery.setFirstResult(offset);
            jpaQuery.setMaxResults(pgsize);
            //jpaQuery.setFirstResult(2);
            //jpaQuery.setMaxResults(3);
        }
        
        List<T> result = jpaQuery.getResultList(); 
        
        int count1 = result == null ? 0 : result.size();
        int count2 = jpaQuery.getResultSize();
        
        logger.log(Level.FINE, "{0} rows fetched of estimated {1} matches.", new String[]{Integer.toString(count1), Integer.toString(count2)});
        
        PageImpl<T> page = new PageImpl(result, pgbl, count2);
        
        return(page);
        
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<T> findText(SearchCriteriaBean search, Sort sort) {
        
        
        
        return(null);
    }
    
    @Override
    @Transactional(readOnly=true)
    public Page<T> findText(SearchCriteriaBean search, Pageable pgbl) {
        
        
        return(null);
    }
    
    @Transactional(propagation=Propagation.REQUIRED)
    public Future createTextIndex() {
        
        Class<T> clazz = getBeanClass();
        
        MtInfo mtInfo = getMtInfo(clazz);
        
        String tenantId = "1001"; // Set it to global first
        if (mtInfo.mtEnabled) {
            
            IContext ctx = ContextManager.getContext();
            tenantId = ctx.getTenantId();
            
        }
        
        FullTextEntityManager ftem = org.hibernate.search.jpa.Search.getFullTextEntityManager(__entityManager);
        
        //SessionFactory factory = __entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        //Session session = factory.withOptions().tenantIdentifier(tenantId).openSession();
        
        //FullTextSession ftsession = org.hibernate.search.Search.getFullTextSession(session);
                
        //try {
            
            MassIndexer indexer = ftem.createIndexer(clazz);
        //    MassIndexer indexer = ftsession.createIndexer(clazz);
            Future<?> future = indexer.start();
        
            return(future);
            
        //} finally {
        //    if (session != null && session.isOpen())
        //        session.close();
        //    if (ftsession != null && ftsession.isOpen())
        //        ftsession.close();
        //}
        
    }
    
    public abstract String getLazyLoadElements();
    
    protected String getJoinFetchClause() {
        
        String s = getLazyLoadElements();
        if (s == null || "".equals(s))
            return("");
        else {
            //Check if the string starts with JOIN FETCH after trimming blank spaces. If yes don't add
            Pattern p = Pattern.compile("^\\s*[jJ][oO][iI][nN]\\s+[fF][eE][tT][cC][hH]\\s+.*$");
            Matcher m1 = p.matcher(s);
            if (m1.matches())
                return(s);
            else
                return(" JOIN FETCH " + s + " ");
        }
    }
    
    protected Class<T> getBeanClass() {
        
        //ClassLoader loader = Thread.currentThread().getContextClassLoader();
        //Class<T> clazz = (Class<T>) loader.loadClass(className);
        
        Type superclass = getClass().getGenericSuperclass();
        
        ParameterizedType pt = (ParameterizedType) superclass;
        
        //Type genType = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        
        Class<T> clazz = (Class<T>) pt.getActualTypeArguments()[0];
        
        return(clazz);
        
    }
    
    protected String getSortString(Sort sort, String tabAlias) {
        
        String orderByStr = "";
        if (sort != null) {
            orderByStr = " ORDER BY ";
            Iterator<Order> iter = sort.iterator();
            HashMap<String, String> map = new HashMap<>();
            //String colsStr = "";
            while(iter != null && iter.hasNext()) {
                Order order  = iter.next();
                String col = order.getProperty();
                Direction dir = order.getDirection();
                if (dir == null)
                    dir = Direction.ASC;
                String dirStr = dir.toString();
                String cols = map.get(dirStr);
                if (cols == null || "".equals(cols)) {
                    cols = tabAlias + "." + col;
                } else {
                    cols += ", " + tabAlias + "." + col;
                }
                map.put(dirStr, cols);
            }
            Iterator<String> iter2 = map.keySet() == null ? null : map.keySet().iterator();
            
            while (iter2 != null && iter2.hasNext()) {
                String dirStr = iter2.next();
                String cols = map.get(dirStr);
                orderByStr += cols + " " + dirStr + " ";
            }
        }
        
        return(orderByStr);
    }
    
    protected String makeDefaultSortString(Class<T> clazz, String tabAlias) {
        
        SortCriteria sc = clazz.getAnnotation(SortCriteria.class);
        String sortStr = "";
        if (sc != null) {
            String[] attrs = sc.attributes();
            if (attrs != null && attrs.length > 0) {
                sortStr += " ORDER BY ";
                for (int i = 0; i < attrs.length; i++) {
                    if (i == 0) {
                        sortStr += tabAlias + "." + attrs[i];
                    } else {
                        sortStr += ", " + tabAlias + "." + attrs[i];
                    }
                }
                sortStr += " " + sc.sortOrder();
            }
        }
        
        return(sortStr);
    }
    
    protected <S extends T> MtInfo getMtInfo(S s) {
        MtInfo mtf = getMtInfo((Class<T>) s.getClass());
        if (mtf.mtEnabled) {
            String value = null;
            Field f = mtf.mtField;
            f.setAccessible(true);
            try {
                value = (String) f.get(s);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new RuntimeException("Could not read value for " + mtf.mtFieldName + " in JPA Bean " + s.getClass().getName());
            }
            mtf.tenantIdValue = value;
        }
        
        return(mtf);
    }
    
    protected  MtInfo getMtInfo(Class<T> domain) {
        
        Field tfield = null;
        MultiTenant mt = domain.getAnnotation(MultiTenant.class);
        if (mt == null)
            throw new RuntimeException("The JPA Bean " + domain.getName() + " is not annotated with MultiTenant annotation. Please use the mandatory annotation and retry !");
        
        MtInfo mtf = new MtInfo();
        if (mt.level() == MultiTenant.Levels.NO_MT) {
            mtf.mtEnabled = false;
        } else {
            String fieldName = mt.fieldName();
            try {
                tfield = domain.getDeclaredField(fieldName);
                tfield.setAccessible(true);
            } catch (NoSuchFieldException | SecurityException ex) {
                throw new RuntimeException("The tenant indicator field - " + fieldName + " is not found or accessible in JPA Bean " + domain.getName(), ex);
            }
            mtf.mtEnabled = true;
            mtf.mtLevel = mt.level();
            mtf.mtFieldName = fieldName;
            mtf.mtField = tfield;
        }
        
        return(mtf);
    }
    
    
    protected <Z> Z sanitizeInput(Z input) {
        
        //escaping for SQL & XML Injection
        
        return(input);

    }
    
    Session OpenSession() {
        Session session = __entityManager.unwrap(Session.class);
        return(session);
    }
    
    public void closeSession(Session session) {
        if (session != null)
            session.close();
        session = null;
    }
    
    protected class MtInfo {
        
        boolean mtEnabled;
        
        MultiTenant.Levels mtLevel;
        
        Field mtField;
        
        String mtFieldName;
        
        String tenantIdValue;
        
    }
    
    private org.apache.lucene.search.SortField.Type luceneTypeMap(String fieldType) {
        
        if ("java.lang.Byte".equals(fieldType) || "byte".equals(fieldType)) {
            return(org.apache.lucene.search.SortField.Type.BYTES);
        } else if ("java.lang.Double".equals(fieldType) || "double".equals(fieldType)) {
            return(org.apache.lucene.search.SortField.Type.DOUBLE);
        } else if ("java.lang.Float".equals(fieldType) || "float".equals(fieldType)) {
            return(org.apache.lucene.search.SortField.Type.FLOAT);
        } else if ("java.lang.Integer".equals(fieldType) || "int".equals(fieldType)) {
            return(org.apache.lucene.search.SortField.Type.INT);
        } else if ("java.lang.Long".equals(fieldType) || "long".equals(fieldType)) {
            return(org.apache.lucene.search.SortField.Type.LONG);
        } else if ("java.lang.Short".equals(fieldType) || "short".equals(fieldType)) {
            return(org.apache.lucene.search.SortField.Type.INT);
        }  else {
            return(org.apache.lucene.search.SortField.Type.STRING);
        }
    }
    
    private FullTextQuery makeTextQuery(Class<T> clazz, String keywords, boolean fuzzy, Sort sortCriteria, Logger logger) {
        
        String s = sanitizeInput(keywords);
        
        Map<String, SearchableFieldMetaData> fieldsMap = JpaUtil.getSearchableFieldsAsMap(clazz, 2);
        if (fieldsMap == null) {
            // log it and initialize to empty list
            logger.log(Level.WARNING, "No search enabled field found in {0} . Search may not retrieve any result !", clazz.getName());
            fieldsMap = new HashMap<>();
        }
        
        Set<String> keySet = fieldsMap.keySet();
        String[] fieldArray = keySet.toArray(new String[keySet.size()]);
        
        MtInfo mtInfo = getMtInfo(clazz);
        
        String tenantId = Constants.GLOBAL_TENANT_ID;
        if (mtInfo.mtEnabled) {
            
            IContext ctx = ContextManager.getContext();
            tenantId = ctx.getTenantId();
        
        }
        
        String tenantFilterName = LuceneUtil.getTenantFilterName(clazz);
        
        Pattern p1 = Pattern.compile("^[\\\"]+[\\p{Print}]+[\\\"]+$");
        Matcher m1 = p1.matcher(s);
        boolean isPhrase = m1.matches();
        
        Pattern p2 = Pattern.compile("^[\\p{Print}]+[\\*\\?]+(\\p{Print})*");
        Matcher m2 = p2.matcher(s);
        boolean isWildCard = m2.matches();
        
        
        FullTextEntityManager ftem = org.hibernate.search.jpa.Search.getFullTextEntityManager(__entityManager);
        
        //SessionFactory factory = __entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        //Session session = factory.withOptions().tenantIdentifier(tenantId).openSession();
        
        //FullTextSession ftsession = org.hibernate.search.Search.getFullTextSession(session);
        
        QueryBuilder qb = ftem.getSearchFactory().buildQueryBuilder().forEntity(clazz).get();
        org.apache.lucene.search.Query query;
        
        if (isPhrase) {
            
            query = qb.phrase().onField(s).ignoreFieldBridge().sentence(s).createQuery();
        } else {
            // not a phrase query. Check wildcard
            if (isWildCard) {
                query = qb.keyword().wildcard().onField(s).ignoreFieldBridge().matching(s).createQuery();
            } else {
                // normal term query. Check for fuzzyness
                if (fuzzy) {
                    FuzzyContext fct = qb.keyword().fuzzy();
                    //fct.withEditDistanceUpTo(xxxx)  // define a default fuzzy distance 
                    //fct.withPrefixLength(xxxx)  // define a default fuzzy prefix length
                    query = fct.onFields(fieldArray).ignoreFieldBridge().matching(s).createQuery();
                } else {
                    query = qb.keyword().onFields(fieldArray).ignoreFieldBridge().matching(s).createQuery();
                }
            }
        }
        
        FullTextQuery jpaQuery = ftem.createFullTextQuery(query, clazz);
        
        if (mtInfo.mtEnabled && !(tenantFilterName == null || "".equals(tenantFilterName))) {
            jpaQuery.enableFullTextFilter(tenantFilterName).setParameter("tenantIdFieldName", mtInfo.mtFieldName);
            jpaQuery.enableFullTextFilter(tenantFilterName).setParameter("tenantIdValue", tenantId);
        }
        
        if (sortCriteria != null) {
            Iterator<Order> iter = sortCriteria.iterator();
            ArrayList<org.apache.lucene.search.SortField> sfList = new ArrayList<>();
            org.apache.lucene.search.SortField  sf;
            while (iter != null && iter.hasNext()) {
                Order order = iter.next();
                boolean reverse = order.isAscending() ? false : true;
                String fname = order.getProperty();
                SearchableFieldMetaData sfm = fieldsMap.get(fname);
                if (sfm != null) {
                    org.apache.lucene.search.SortField.Type luceneType = luceneTypeMap(sfm.getFieldClassName());
                    sf = new org.apache.lucene.search.SortField(fname, luceneType, reverse);
                    sfList.add(sf);
                }
            }
            
            org.apache.lucene.search.SortField[] sfArr = sfList.toArray(new org.apache.lucene.search.SortField[sfList.size()]);
            org.apache.lucene.search.Sort sort = new org.apache.lucene.search.Sort(sfArr);
            jpaQuery.setSort(sort);
        }
        
        return(jpaQuery);
    }
    
    private void validateTenantIdAccess(T entity, boolean allowCrossPartitionDataAccess) {
        
        IContext ctx = ContextManager.getContext();
        String tid = ctx.getTenantId();
        MultiTenantAccess.Level mtAccess = ctx.getSecurityContext().getMtAccessLevel();
        
        MtInfo mtf = getMtInfo(entity);
        
        if (mtAccess == MultiTenantAccess.Level.ONLY_GLOBAL) {
            if (!Constants.GLOBAL_TENANT_ID.equals(mtf.tenantIdValue))
                throw new RuntimeException("User does not have access to this data !!");
        } 
        
        if (!Objects.equals(tid, mtf.tenantIdValue)) {
            if (mtAccess == MultiTenantAccess.Level.ALL) {
                // do nothing , allow processing
            } else if (mtAccess == MultiTenantAccess.Level.CUSTOM) {
                // throw exception for now
                throw new RuntimeException("User does not have access to this data ");
            } else if (mtAccess == MultiTenantAccess.Level.ONLY_GLOBAL) {
                throw new RuntimeException("User does not have access to this data ");
            } else if (mtAccess == MultiTenantAccess.Level.ONLY_SELF) {
                throw new RuntimeException("User does not have access to this data ");
            } else if (mtAccess == MultiTenantAccess.Level.SELF_N_GLOBAL) {
                if (Constants.GLOBAL_TENANT_ID.equals(tid)) {
                    // global users may not manipulate tenant specific data
                    if (!allowCrossPartitionDataAccess)
                        throw new RuntimeException("User does not have access to perform the operation on tenant specific data !!");
                } else {
                    // other tenants cannot do certain operations on global data
                    if (!allowCrossPartitionDataAccess)
                        throw new RuntimeException("User does not have access to perform the operation on global data !!");
                }
            } else {
                // should not reach here 
                throw new RuntimeException("Inconsistent multi-tenant access control information. Could not determine user privilege to access to this data ");
            }
        }
        
    }
    
    private QueryFragment makeFindQueryFragment(String entityName, MtInfo mtf, History h, boolean withHistory) {
        
        //StringBuffer buff = new StringBuffer(100);
        QueryFragment qf = new QueryFragment();
        qf.whereClauseExists = true;
        //qf.tenantClauseExists = true;
        
        Field f = mtf.mtField;
        String tenantFieldName = mtf.mtFieldName;
        
        //String listQuery = "SELECT t FROM " + shortClassName + " t " + getJoinFetchClause();
        
        qf.queryBuff.append("SELECT t FROM ").append(entityName).append(" t ").append(getJoinFetchClause());
        
        System.out.println("JPQL Is -- " + qf.queryBuff.toString());
        
        if (mtf.mtEnabled) {
            
            MultiTenantAccess.Level mtAccess = ContextManager.getContext().getSecurityContext().getMtAccessLevel();
            
            if (mtAccess == MultiTenantAccess.Level.ONLY_SELF) {
                qf.queryBuff.append(" WHERE t.").append(tenantFieldName).append(" = :tenant ");
                //listQuery += " WHERE t." + tenantFieldName + " = :tenant ";
            } else if (mtAccess == MultiTenantAccess.Level.SELF_N_GLOBAL) {
                qf.queryBuff.append(" WHERE (t.").append(tenantFieldName).append(" = :tenant OR t.").append(tenantFieldName).append(" = '").append(Constants.GLOBAL_TENANT_ID).append("')");
                //listQuery += " WHERE (t." + tenantFieldName + " = :tenant OR t." + tenantFieldName + " = '" + Constants.GLOBAL_TENANT_ID + "')";
            } else if (mtAccess == MultiTenantAccess.Level.ONLY_GLOBAL) {
                qf.queryBuff.append(" WHERE (t.").append(tenantFieldName).append(" = '").append(Constants.GLOBAL_TENANT_ID).append("')");
            } else if (mtAccess == MultiTenantAccess.Level.ALL) {
                qf.whereClauseExists = false;
            } else if (mtAccess == MultiTenantAccess.Level.CUSTOM) {
                // TODO: execute the api to determine accesslevel . For now only self
                qf.queryBuff.append(" WHERE t.").append(tenantFieldName).append(" = :tenant ");
            } else {
                // should not come here. Throw exception
                throw new EntityUCException("Inconsistent multi-tenancy information for JPA bean " + entityName);
            }
            
            if (h != null) {
                //if (!withHistory)
                //    qf.queryBuff.append(" AND ").append(" t.").append(h.endFieldName()).append( " = NULL");
            }
            
        } else {
            //qf.tenantClauseExists = false;
            if (h != null) {
                //if (!withHistory)
                    //qf.queryBuff.append(" WHERE ").append(" t.").append(h.endFieldName()).append( " = NULL");
                    //qf.queryBuff.append(" WHERE ").append(" 1 = 1 ");
                qf.whereClauseExists = false;
            } else 
                qf.whereClauseExists = false;
        }
        
        /*
        if (mtf.mtEnabled) {
            if (mtf.mtLevel == MultiTenant.Levels.ONE_TENANT) {
                qf.queryBuff.append(" WHERE t.").append(tenantFieldName).append(" = :tenant ");
                //listQuery += " WHERE t." + tenantFieldName + " = :tenant ";
            } else if (mtf.mtLevel == MultiTenant.Levels.ONE_N_GLOBAL) {
                qf.queryBuff.append(" WHERE (t.").append(tenantFieldName).append(" = :tenant OR t.").append(tenantFieldName).append(" = '").append(Constants.GLOBAL_TENANT_ID).append("')");
                //listQuery += " WHERE (t." + tenantFieldName + " = :tenant OR t." + tenantFieldName + " = '" + Constants.GLOBAL_TENANT_ID + "')";
            } else if (mtf.mtLevel == MultiTenant.Levels.ALLOW_GLOBAL) {
                // TODO: check security context of logged on user. If has global access, then only don't add
                //       a tenant clause, otherwise add tenant clause with current tenant id.
                //       For now, don't add tenant clause to pass junit
                qf.whereClauseExists = false;
                //qf.tenantClauseExists = false;
            } else {
                // should not come here. Throw exception
                throw new EntityUCException("Inconsistent multi-tenancy information for JPA bean " + entityName);
            }
            
            if (h != null) {
                //if (!withHistory)
                //    qf.queryBuff.append(" AND ").append(" t.").append(h.endFieldName()).append( " = NULL");
            }
            
        } else {
            //qf.tenantClauseExists = false;
            if (h != null) {
                //if (!withHistory)
                    //qf.queryBuff.append(" WHERE ").append(" t.").append(h.endFieldName()).append( " = NULL");
                    //qf.queryBuff.append(" WHERE ").append(" 1 = 1 ");
                qf.whereClauseExists = false;
            } else 
                qf.whereClauseExists = false;
        }
        */
        
        return(qf);
    }
    
    class QueryFragment {
        
        StringBuffer queryBuff = new StringBuffer(100);
        
        boolean whereClauseExists;
        
        //boolean tenantClauseExists;
        
    }
    
}
