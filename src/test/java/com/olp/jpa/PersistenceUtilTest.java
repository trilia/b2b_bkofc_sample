/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa;

import com.olp.fwk.config.ConfigurationBuilder;
import com.olp.fwk.config.error.ConfigurationException;
import javax.persistence.EntityManager;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author raghosh
 */
public class PersistenceUtilTest {
    
    @BeforeClass
    public static void beforeClass() throws ConfigurationException {
        ConfigurationBuilder.startup();
    }
    
    @AfterClass
    public static void afterClass() throws ConfigurationException {
        ConfigurationBuilder.shutdown();
    }
    
    @Test
    public void testGetEntityManager() {
        
        EntityManager em = PersistenceUtil.getEntityManager("productHub");
        
        assertNotNull("Entity Manager instance should not be null !!", em);
        
        em.close();
    }
    
    public void testGet() {
        
        
        
    }
    
}
