


















































































/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa.common;

import com.olp.annotations.MultiTenant;
import com.olp.fwk.common.Constants;
import com.olp.fwk.common.ContextManager;
import com.olp.fwk.common.IContext;
import com.olp.fwk.common.error.EntityUCException;
import com.olp.jpa.util.JpaUtil;
import com.olp.fwk.common.error.EntityValidationException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author raghosh
 * @param <T>
 * @param <ID>
 */
@NoRepositoryBean
public abstract class AbstractServiceImpl<T extends Object, ID extends Serializable> implements IJpaService<T, ID> {
    
    
    protected abstract JpaRepository<T, ID> getRepository();
    
    protected abstract ITextRepository<T, ID> getTextRepository();
    
    /**
     * A string representation (usually a json / xml format) of the alternate keys of the entity
     * which can uniquely identify an entity instance.
     * @param entity
     * @return string in json / xml format representing the alternate key.
     */
    protected abstract String getAlternateKeyAsString(T entity);
    
    /**
     * It is preferable that all server side validations are done centrally in this method.
     * 
     * Programmers have the choice to throw an exception or return Outcome set to  false to indicate error
     * condition.
     * 
     * @param opCode frequently used CRUD style operation codes are defined in this class. Any custom opCode
     *      should use values above 100.
     * @param entity entity to be processed
     * @return Outcome with validation status and error message, if any.
     * @throws com.olp.fwk.common.error.EntityValidationException
     */
    protected abstract Outcome preProcess(int opCode, T entity) throws EntityValidationException;
    
    /**
     * This lifecycle method is invoked after persisting and/or fetching the entity.
     * @param opCode frequently used CRUD style operation codes are defined in this class.  Any custom opCode
     *      should use values above 100.
     * @param entity entity to be processed
     * @return  A set which contains the index of entity elements in the list, which failed validation, if any.
     * @throws com.olp.fwk.common.error.EntityValidationException
     */
    protected abstract Outcome postProcess(int opCode, T entity) throws EntityValidationException;
    
    //protected abstract Outcome preProcess(int opCode, T entity, Object... foreignEntities);

    @Override
    @Transactional(readOnly=true, noRollbackFor={javax.persistence.NoResultException.class})
    public List<T> findAll() {
        
        JpaRepository<T, ID> repository = getRepository();
        
        List<T> list = null;
        
        try {
            
            list = repository.findAll();
        
            postProcessPvt(IJpaService.FIND_BULK, list, true);
            
        } catch (EntityValidationException e) {
            throw new EntityUCException(e);
        } 
        
        return(list);
        
    }

    @Override
    @Transactional(readOnly=true)
    public List<T> findAll(SortCriteriaBean sort) {
        
        JpaRepository<T, ID> repository = getRepository();
        
        List<T> list;
        
        try {
            
            if (sort == null) {
                list = repository.findAll();
                postProcessPvt(IJpaService.FIND_BULK, list, true);
            } else {
                Sort s = JpaUtil.convert(sort);
                list = repository.findAll(s);
                postProcessPvt(IJpaService.FIND_BULK, list, true);
            }
        
        } catch (EntityValidationException e) {
            throw new EntityUCException(e);
        }
        
        return(list);
        
    }
    
    @Override
    @Transactional(readOnly=true)
    public Page<T> findAll(Pageable req) {
        
        JpaRepository<T, ID> repository = getRepository();
        
        Page<T> page = repository.findAll(req);
        
        try {
            postProcessPvt(IJpaService.FIND_BULK, page.getContent(), true);
        } catch (EntityValidationException e) {
            throw new EntityUCException(e);
        }
        
        return(page);
        
    }

    @Override
    @Transactional(readOnly=true, noRollbackFor={javax.persistence.NoResultException.class})
    public T find(ID id) {
        
        JpaRepository<T, ID> repository = getRepository();
        
        T bean = repository.findOne(id);
        
        try {
            postProcessPvt(IJpaService.FIND, bean);
        } catch (EntityValidationException e) {
            throw new EntityUCException(e);
        }
        
        return(bean);
    }

    @Override
    @Transactional(readOnly=true)
    public List<T> find(List<ID> list) {
        
        JpaRepository<T, ID> repository = getRepository();
        
        List<T> list2 = repository.findAll(list);
        
        try {
            postProcessPvt(IJpaService.FIND_BULK, list2, true);
        } catch (EntityValidationException e) {
            throw new EntityUCException(e);
        }        
        
        return(list2);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<T> findText(String keywords, boolean fuzzy, SortCriteriaBean sort) {
        
        ITextRepository<T, ID> repository = getTextRepository();
        
        Sort s = JpaUtil.convert(sort);
        
        List<T> list = repository.findText(keywords, fuzzy, s);
        
        try {
            postProcessPvt(IJpaService.FIND_BULK, list, true);
        } catch (EntityValidationException e) {
            throw new EntityUCException(e);
        }
        
        return(list);
    }
    
    @Override
    @Transactional(readOnly=true)
    public Page<T> findText(String keywords, boolean fuzzy, Pageable req) {
        
        ITextRepository<T, ID> repository = getTextRepository();
        
        Page<T> page = repository.findText(keywords, fuzzy, req);
        
        try {
            postProcessPvt(IJpaService.FIND_BULK, page.getContent(), true);
        } catch (EntityValidationException e) {
            throw new EntityUCException(e);
        }
        
        return(page);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<T> findText(SearchCriteriaBean search, SortCriteriaBean sort) {
        return(null);
    }
    
    @Override
    @Transactional(readOnly=true)
    public Page<T> findText(SearchCriteriaBean search, Pageable req) {
        return(null);
    }

    @Override
    @Transactional
    public T add(T entity) {
        
        Logger logger = Logger.getLogger(getClass().getName());
        
        JpaRepository<T, ID> repository = getRepository();
        T bean;
        
        try {
            
            preProcessPvt(IJpaService.ADD, entity);
            
            checkRequiredAttrs(entity, true);
        
            bean = repository.save(entity);
        
            postProcessPvt(IJpaService.ADD, bean);
        } catch (Throwable e) {
            throw new EntityUCException(e);
        }
        
        return(bean);
        
    }

        
    @Override
    @Transactional
    public List<ID> addAll(List<T> list, boolean ignoreError) {
        
        Logger logger = Logger.getLogger(this.getClass().getName());
        
        if (list == null) {
            logger.log(Level.WARNING, "No entity list found !!");
            return null;
        }
        
        ArrayList<ID> idList = new ArrayList<>();
        
        JpaRepository<T, ID> repository = getRepository();
        Iterator<T> iter = list.iterator();
        
        if (ignoreError) {
            while (iter.hasNext()) {
                T bean = iter.next();
                
                try {
                    preProcessPvt(IJpaService.ADD, bean);
                    checkRequiredAttrs(bean, true);
                    repository.save(bean);
                    postProcessPvt(IJpaService.ADD, bean);
                    ID id = JpaUtil.getIdFieldValue(bean);
                    idList.add(id);
                } catch (Throwable t) {
                    String s = getAlternateKeyAsString(bean);
                    logger.log(Level.WARNING, "Error while adding entity instance " + s, t);
                }
            }
        } else {
            try {
                // TODO : devise a way to call checkRequiredAttrs() 
                
                preProcessPvt(IJpaService.ADD_BULK, list, ignoreError);
                repository.save(list);
                postProcessPvt(IJpaService.ADD_BULK, list, ignoreError);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Error while adding bulk entity instances !!", t);
                throw new EntityUCException(t);
            }
            
            while (iter.hasNext()) {
                T bean = iter.next();
                ID id = JpaUtil.getIdFieldValue(bean);
                idList.add(id);
            }
        }
        
        return(idList);
        
    }

    @Override
    @Transactional
    public T update(T entity) {
        
        ID id = JpaUtil.getIdFieldValue(entity);
        if (id == null)
            throw new RuntimeException("The id cannot be null for update !! In - " + entity.getClass().getName());
        
        JpaRepository<T, ID> repository = getRepository();
        //IBaseRepository<T, ID> repo = getRepository();
        if (IBaseRepository.class.isAssignableFrom(repository.getClass())) {
            
            // Detach the entity first so that in case the caller has modified a managed 
            // entity object, the changes are not persisted before validation and further processing.
            
            // Had to disable detaching the entity because of issues in updation of entities in
            // MerchantServiceImpl . Need to check behavior in latest hibernate ogm.
            
            ((IBaseRepository) repository).detach(entity);
        }
        
        T oldEntity = repository.findOne(id);
        if (oldEntity == null)
            throw new RuntimeException("No entity found with ID - " + id.toString());
        
        T updated = null;
        try {
            
            preProcessPvt(IJpaService.UPDATE, entity);
            
            checkRequiredAttrs(entity, false);
        
            updated = doUpdatePvt(entity, oldEntity); // in case of historical updates, a new entity instance will be returned
        
            postProcessPvt(IJpaService.UPDATE, updated);
        } catch (Throwable t) {
            throw new EntityUCException(t);
        }
        
        return(updated);
        
    }
    
    @Override
    @Transactional
    public List<ID> updateAll(List<T> list, boolean ignoreError) {
        
        Logger logger = Logger.getLogger(this.getClass().getName());
        
        if (list == null) {
            logger.log(Level.WARNING, "No entity list found !!");
            return null;
        }
        
        ArrayList<ID> idList = new ArrayList<>();
        
        JpaRepository<T, ID> repository = getRepository();
        Iterator<T> iter = list.iterator();
        
        //if (ignoreError) {
            while (iter.hasNext()) {
                T bean = iter.next();
                try {
                    if (AbstractRepositoryImpl.class.isAssignableFrom(repository.getClass())) {
            
                        // Detach the entity first so that in case the caller has modified a managed 
                        // entity object, the changes are not persisted before validation and further processing.

                        ((AbstractRepositoryImpl) repository).detach(bean);
                    }
                    preProcessPvt(IJpaService.UPDATE, bean);
                    checkRequiredAttrs(bean, false);
                    repository.save(bean);
                    postProcessPvt(IJpaService.UPDATE, bean);
                    ID id = JpaUtil.getIdFieldValue(bean);
                    idList.add(id);
                } catch (Throwable t) {
                    String s = getAlternateKeyAsString(bean);
                    if (ignoreError) {
                        logger.log(Level.WARNING, "Error while adding entity instance " + s, t);
                        //Long longId = new Long(-1);
                        //ID errId = (ID) longId;
                        //idList.add(errId);
                    } else {
                        logger.log(Level.SEVERE, "Error while adding entity instance " + s, t);
                        throw new EntityUCException(t); 
                    }
                }
            }
        //} else {
        
        /*
            The if-else block has been commented out, because of the way JPA deals with updates and detached entities
        */
            
        /*
            try {
                preProcessPvt(IJpaService.UPDATE_BULK, list, ignoreError);
                repository.save(list);
                postProcessPvt(IJpaService.UPDATE_BULK, list, ignoreError);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Error while adding bulk entity instances !!", t);
                throw t;
            }
            
            while (iter.hasNext()) {
                T bean = iter.next();
                ID id = JpaUtil.getIdFieldValue(bean);
                idList.add(id);
            }
        */
        //}
        
        return(idList);
    }
    
    @Override
    public void delete(ID id) {
        
        if (id == null)
            throw new RuntimeException("The id cannot be null for delete !! ");
        
        JpaRepository<T, ID> repository = getRepository();
        
        T entity = repository.findOne(id);
        
        try {
            preProcessPvt(IJpaService.DELETE, entity);
        
            repository.delete(id);
        
            postProcessPvt(IJpaService.DELETE, entity);
        } catch (Throwable t) {
            throw new EntityUCException(t);
        }
        
    }
    
    @Override
    @Transactional
    public void deleteAll(boolean ignoreError) {
        
        JpaRepository<T, ID> repository = getRepository();
        
        // We have to fetch all records here so that preProcessing & postProcessing
        // can happen. Although there could be some issues due to transaction boundaries.
        // The list returned by respository.findAll() may not be the same in em 
        // within repository.deleteAll
        List<T> list = repository.findAll();
        
        try {
            preProcessPvt(IJpaService.DELETE_BULK, list, ignoreError);
        
            repository.delete(list);
            
            postProcessPvt(IJpaService.DELETE_BULK, list, ignoreError);
        } catch (Throwable t) {
            throw new EntityUCException(t);
        }
        
        // No post processing in delete.
        
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
    
    //@Transactional
    //protected Session openSession() {
        
        //AbstractRepositoryImpl<T, ID> repo = (AbstractRepositoryImpl<T, ID>) getRepository();
        //Session session = repo.OpenSession();
    //    EntityManager em = __entityManager.getEntityManagerFactory().createEntityManager();
    //    Session session = em.unwrap(Session.class);
        
    //    return(session);
    //}
    
    //protected void closeSession(Session session) {
    //    if (session != null)
    //        session.close();
    //    session = null;
    //}
    
    private T doUpdatePvt(T newEntity, T oldEntity) {
        
        Logger logger = Logger.getLogger(getClass().getName());
        
        T result;
        try {
            result = doUpdate(newEntity, oldEntity);
        } catch (EntityValidationException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new EntityUCException("Error while updating entity", ex);
        }
        
        //if (!result.isResult()) {
        //    throw new EntityUCException(result.getErrorMessage());
        //}
        
        return(result);
    }
    
    private Outcome preProcessPvt(int opCode, T entity) throws EntityValidationException {
        
        Logger logger = Logger.getLogger(getClass().getName());
        
        switch(opCode) {
            case ADD : 
            case ADD_BULK : 
                //updateTenantWithRevision(entity);
                //break;
            case UPDATE : 
            case UPDATE_BULK : {
                updateRevisionControl(entity);
                break;
            }
            case DELETE : {
                break;
            }
            case DELETE_BULK : {
                break;
            }
            default : {
                //break;
            };
        } // end switch case
            
        Outcome result = preProcess(opCode, entity);
        if (!result.isResult()) {
                    
            String s1 = entity.getClass().getName(), s2 = getAlternateKeyAsString(entity);
            logger.log(Level.SEVERE, "Pre-process validation failed for entity instance {0} - {1}", new String[] {s1, s2} );
            logger.log(Level.SEVERE, result.getErrorMessage());
        
            throw new EntityValidationException("Pre-process validation failed for entity instance " + s1 + " - " + s2);

        } // end if !result
        
        return(result);
    }
    
    private void preProcessPvt(int opCode, List<T> entityList, boolean ignoreError) throws EntityValidationException {
        
        Logger logger = Logger.getLogger(getClass().getName());
        
        for (int i=0; entityList != null && i < entityList.size(); i++) {
            
            T entity = entityList.get(i);
        
            if (entity != null) {
                Outcome result = preProcessPvt(opCode, entity);
                if (!result.isResult()) {
                    
                    String s1 = entity.getClass().getName(), s2 = getAlternateKeyAsString(entity);
                    logger.log(Level.WARNING, "Pre-process validation failed for entity instance {0} - {1}", new String[] {s1, s2} );
                    
                    if (!ignoreError)
                        throw new EntityValidationException("Pre-process validation failed for entity instance " + s1 + " - " + s2);
                    
                } // end if !result
            } // end if entity != null
        }
        
    }

    private Outcome postProcessPvt(int opCode, T entity) throws EntityValidationException {
        
        Outcome result = postProcess(opCode, entity);
        
        return(result);
    }
    
    //@Transactional
    private void postProcessPvt(int opCode, List<T> entityList, boolean ignoreError) throws EntityValidationException {
        
        Logger logger = Logger.getLogger(getClass().getName());
        
        for (int i=0; entityList != null && i < entityList.size(); i++) {
            
            T entity = entityList.get(i);
        
            if (entity != null) {
                Outcome result = postProcessPvt(opCode, entity);
                if (!result.isResult()) {
                    
                    String s1 = entity.getClass().getName(), s2 = getAlternateKeyAsString(entity);
                    logger.log(Level.WARNING, "Post-process validation failed for entity instance {0} - {1}", new String[] {s1, s2} );
                    logger.log(Level.WARNING, result.getErrorMessage());
                    
                    if (!ignoreError)
                        throw new EntityValidationException("Post-process validation failed for entity instance " + s1 + " - " + s2);
                    
                } // end if !result
            } // end if entity != null
        }
        
    }
    
    protected void updateRevisionControl(T entity) {
        
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        updateRevisionControl(entity, date);
    }
    
    protected void updateRevisionControl(T entity, Date date) {
        
        Logger logger = Logger.getLogger(getClass().getName());
        
        if (entity != null) {
            
            Field revField = JpaUtil.getRevControlField(entity);
            if (revField == null) {
                String s1 = getBeanClass().getName();
                String s2 = getAlternateKeyAsString(entity);
                logger.log(Level.SEVERE, "Could not obtain revision control field information for {0}, instance - {1}", new String[]{s1, s2});
                throw new EntityUCException("Could not obtain revision control information for " + s1 + ", instance - " + s2);
            }
            
            RevisionControlBean ctrlBean = JpaUtil.getRevControlBean(entity, revField);
            if (ctrlBean != null) {
                
                Date date2;
                if (date == null) {
                    Calendar cal = Calendar.getInstance();
                    date2 = cal.getTime();
                } else {
                    date2 = date;
                }
                // TODO: set the user context when it is ready & available in the thread
                ctrlBean.setRevisionDate(date2);

                if (ctrlBean.getCreationDate() == null) {
                    ctrlBean.setCreationDate(date2);
                }
                
                if (ctrlBean.getCreatedBy() == null) {
                    ctrlBean.setCreatedBy("BGP"); // TODO: Read security context and update accordingly
                    ctrlBean.setCreatedById(101);
                }
                
                //if (ctrlBean.getRevisedBy() == null) {
                    ctrlBean.setRevisedBy("BGP"); // TODO: Read security context and update accordingly
                    ctrlBean.setRevisedById(101);
                //}
            }
            
        } else {
            logger.log(Level.WARNING, "Null entity instance received. Nothing to do !!");
        }
    }
    
    protected void updateRevisionControl(List<T> entityList, Date date) {
        
        Logger logger = Logger.getLogger(getClass().getName());
        
        if (entityList == null) {
            logger.log(Level.SEVERE, "Null entity list received. Nothing to do !!");
            return;
            //throw new IllegalArgumentException("Null entity list received !!");
        }
        
        Iterator<T> iter = entityList.iterator();
        
        Date date2;
        if (date == null) {
            Calendar cal = Calendar.getInstance();
            date2 = cal.getTime();
        } else {
            date2 = date;
        }
        
        while (iter.hasNext()) {
            T bean = iter.next();
            updateRevisionControl(bean, date2);
        }
        
    }
    
    protected void updateTenantWithRevision(T entity) {
        
        Field f = JpaUtil.getTenantIdField(entity);
        Object val = null;
        
        try {
            f.setAccessible(true);
            val = f.get(entity);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException("Error in accessing tenant value for entity class " + entity.getClass().getName());
        }
        
        if (val == null) {
            try {
                f.set(entity, getTenantId());
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new RuntimeException("Error in setting tenant value for entity class " + entity.getClass().getName());
            }
        }
        
        updateRevisionControl(entity);
    }
    
    protected T doUpdate(T newEntity, T oldEntity) throws EntityValidationException {
        
        Outcome result = new Outcome();
        result.setResult(true);
        
        try {
            JpaUtil.updateEntityAttributes(newEntity, oldEntity, null);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(AbstractServiceImpl.class.getName()).log(Level.SEVERE, "Exception while updating entity attributes !", ex);
            result.setErrorMessage("Exception while updating entity attributes ! " + ex.getMessage());
            result.setResult(false);
        }
        
        return(oldEntity);
        
    }
    
    protected String getTenantId() {
        
        IContext ctx = ContextManager.getContext();
        String tid = ctx.getTenantId();
        
        return(tid);
    }
    
    protected RevisionControlBean getRevisionControl() {
        
        RevisionControlBean bean = new RevisionControlBean();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        bean.setCreatedBy("BGP");
        bean.setCreatedById(101);
        bean.setCreationDate(cal.getTime());
        bean.setObjectVersionNumber("1.0");
        bean.setRevisedBy("BGP");
        bean.setRevisedById(101);
        bean.setRevisionDate(cal.getTime());
        
        return(bean);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    protected void validateRevisionDates(Date start, Date end, Date prevEnd) throws EntityValidationException {
        /*
        // Commenting out previous implementation. 
        
        if (start == null)
            throw new EntityValidationException("Null start date received !");
        
        if (end == null)
            end = Constants.JPA_MAX_DATE;
        
        if (prevEnd != null && prevEnd.compareTo(Constants.JPA_MAX_DATE) == 0) {
            if (end.compareTo(Constants.JPA_MAX_DATE) == 0)
                throw new EntityValidationException("Only one active record with end date " + Constants.JPA_MAX_DATE + " is allowed !");
        }
        
        if (prevEnd != null && prevEnd.compareTo(start) != 0)
            throw new EntityValidationException("The start date " + start + " does not match with previous end date " + prevEnd);
        
        if (start.after(end))
            throw new EntityValidationException("Start date " + start + " cannot be later than end date " + end);
        
        */
        
        this.validateRevisionDates(start, end, null, prevEnd);
    }
    
    protected void validateRevisionDates(Date start, Date end, Date prevStart, Date prevEnd) throws EntityValidationException {
        
        if (start == null)
            throw new EntityValidationException("Start date cannot be null !");
        
        if (end == null)
            end = Constants.JPA_MAX_DATE;
        
        /*
            1. MAX_DATE >= end >= start == prevEnd >= prevStart
            2. if end = MAX_DATE && prevEnd = MAX_DATE then Rule #1 implies --> start = MAX_DATE 
               However when the new historical record with no end date (i.e. end = MAX_DATE) is compared with
               last historical record available, (i.e. prevEnd = MAX_DATE) then rule #2 can be often violated
               as  start < MAX_DATE . We pass the test and caller takes the responsibility of setting prevEnd = start
               after the validation is over. Then  Ruile #1 is again restored.
        */
        
        if (Constants.JPA_MAX_DATE.compareTo(end) < 0)
            throw new EntityValidationException("End date cannot be later than " + Constants.JPA_MAX_DATE);
        
        if ( end.compareTo(start) < 0 )
            throw new EntityValidationException("End date must be greater or equal to start date !");
        
        if ( prevEnd != null) {
            if (Constants.JPA_MAX_DATE.compareTo(prevEnd) > 0 && start.compareTo(prevEnd) != 0) {
                // Implies if prevEnd = MAX_DATE, we skip this portion of test i.e. pass the this check
                throw new EntityValidationException("Start date " + start + " must be equal to previous end date " + prevEnd + " !");
            }
            
            /*
            // This check is not required at this moment, becuase it is assumed that prevStart & prevEnd will be vaidated in the 
            // previous iteration.
            
            if ( prevStart != null) {
                if (prevEnd.compareTo(prevStart) < 0)
                    throw new EntityValidationException("Previous end date " + prevEnd + " must the later than previous start date " + prevStart + " !");
            }
            */
        }
        
        
    }
    
    protected void checkRequiredAttrs(T entity, boolean allowNullId) throws EntityValidationException {
        
        Class<? extends Object> clazz = entity.getClass();
        
        MultiTenant mt = clazz.getAnnotation(MultiTenant.class);
        if (mt == null)
            throw new EntityValidationException("Mandatory annotation MultiTenant missing with entity - " + clazz.getName());
        
        Field[] fields = clazz.getDeclaredFields();
        if (fields != null) {
            
            for (Field f : fields) {
                
                f.setAccessible(true);
                
                // tenantId field not checked as this is causing validation issues and will be automatically handled by repository layer
                if (f.getName().equals(mt.fieldName())) 
                    continue;
                
                Id id = f.getAnnotation(Id.class);
                if (id != null && allowNullId)
                    continue;
                
                Column c = f.getAnnotation(Column.class);
                if (c != null) {
                    if (!c.nullable()) {
                        Object value = null;
                        
                        try {
                            value = f.get(entity);
                        } catch (IllegalArgumentException | IllegalAccessException ex) {
                            throw new EntityValidationException("Error while reading a required attribute", ex);
                        }
                        if (value == null)
                            throw new EntityValidationException("A required attribute " + f.getName() + " is null !!");
                    }
                } // end if c!= null
                
            } // end for
            
        } // end if fields != null
        
    }
    
    public class Outcome {
        
        private boolean result;
        
        private String errorMessage;

        public boolean isResult() {
            return result;
        }

        public void setResult(boolean result) {
            this.result = result;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
    }
    
}