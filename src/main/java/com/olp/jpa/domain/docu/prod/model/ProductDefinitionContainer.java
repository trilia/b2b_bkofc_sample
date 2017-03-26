/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa.domain.docu.prod.model;

import com.olp.jpa.common.EntityContainerTemplate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author raghosh
 */
@XmlRootElement(name="product-definitions", namespace="http://trilia-cloud.com/schema/entity/prod")
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlAccessorOrder(value=XmlAccessOrder.UNDEFINED)
@XmlType(propOrder={"prodList"})
public class ProductDefinitionContainer implements EntityContainerTemplate<ProductDefinition, Long>, Serializable {
    
    List<ProductDefinition> prodList = new ArrayList<>();

    @Override
    public List<ProductDefinition> getEntityList() {
        return(prodList);
    }
    
    public void setEntityList(List<ProductDefinition> prods) {
        if (prods != null)
            this.prodList = prods;
        else
            this.prodList.clear();
    }
}
