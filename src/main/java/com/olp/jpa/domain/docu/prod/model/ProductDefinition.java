/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa.domain.docu.prod.model;

import com.olp.fwk.common.Constants;
import com.olp.jpa.common.RevisionControlBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author raghosh
 */
@XmlRootElement(name="product-definition", namespace="http://trilia-cloud.com/schema/entity/prod")
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlAccessorOrder(value=XmlAccessOrder.UNDEFINED)
@XmlType(propOrder={"id", "partitionCode", "productCode", "productVersion", "productName", "description", "compositionType"
    , "status", "isEnabled", "originProductRef", "revisionControl", "groups", "revisions", "variantNames", "variants"})
public class ProductDefinition implements Serializable {
    
    @XmlElement(name="product-definition-id")
    private Long id;
    
    @XmlTransient
    private String tenantId;
    
    @XmlAttribute(name="partition-code")
    private String partitionCode;
    
    @XmlAttribute(name="product-code")
    private String productCode;
    
    @XmlElement(name="product-version")
    private int productVersion;
    
    @XmlElement(name="product-name")
    private String productName;
    
    @XmlElement(name="description")
    private String description;
    
    @XmlElement(name="composition-type")
    private ProductAttributesBean.CompositionType compositionType;
    
    @XmlElement(name="status")
    private String status;
    
    @XmlElement(name="enabled-flag")
    private boolean isEnabled;
    
    @XmlElement(name="variant-name")
    private List<String> variantNames = new ArrayList<>();
    
    @XmlElement(name="origin-product-code")
    private String originProductRef;
    
    @XmlElement(name="product-group-asscn")
    private Set<ProductGroupAsscn> groups = new HashSet<>();
    
    @XmlElement(name="product-revision")
    private List<ProductRevision> revisions = new ArrayList<>();
    
    @XmlElement(name="product-variant")
    private Set<ProductVariant> variants = new HashSet<>();
    
    private RevisionControlBean revisionControl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPartitionCode() {
        return partitionCode;
    }

    public void setPartitionCode(String partitionCode) {
        this.partitionCode = partitionCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(int productVersion) {
        this.productVersion = productVersion;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductAttributesBean.CompositionType getCompositionType() {
        return compositionType;
    }

    public void setCompositionType(ProductAttributesBean.CompositionType compositionType) {
        this.compositionType = compositionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public List<String> getVariantNames() {
        return variantNames;
    }

    public void setVariantNames(List<String> variantNames) {
        this.variantNames = variantNames;
    }

    public String getOriginProductRef() {
        return originProductRef;
    }

    public void setOriginProductRef(String originProductRef) {
        this.originProductRef = originProductRef;
    }

    public Set<ProductGroupAsscn> getGroups() {
        return groups;
    }

    public void setGroups(Set<ProductGroupAsscn> groups) {
        this.groups = groups;
    }

    public List<ProductRevision> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<ProductRevision> revisions) {
        this.revisions = revisions;
    }

    public Set<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(Set<ProductVariant> variants) {
        this.variants = variants;
    }

    public RevisionControlBean getRevisionControl() {
        return revisionControl;
    }

    public void setRevisionControl(RevisionControlBean revisionControl) {
        this.revisionControl = revisionControl;
    }
    
    public ProductDefinitionEntity convertTo(int mode) {
        
        ProductDefinitionEntity entity = new ProductDefinitionEntity();
        
        entity.setId(this.id);
        
        entity.setTenantId(this.tenantId);
        
        entity.setProductCode(this.productCode);
        entity.setProductName(this.productName);
        entity.setProductVersion(this.productVersion);
        entity.setDescription(this.description);
        entity.setCompositionType(this.compositionType);
        
        if (this.originProductRef != null) {
            ProductDefinitionEntity origProd = new ProductDefinitionEntity();
            origProd.setProductCode(this.productCode);
            entity.setOriginProductRef(origProd);
        }
        
        if (this.revisions != null) {
            ArrayList<ProductRevisionEntity> revs = new ArrayList<>();
            for (ProductRevision pre : this.revisions) {
                ProductRevisionEntity rev = pre.convertTo(mode);
                revs.add(rev);
            }
            entity.setRevisions(revs);
        }
        
        if (this.variants != null) {
            HashSet<ProductVariantEntity> vars = new HashSet<>();
            for (ProductVariant pve : this.variants) {
                ProductVariantEntity var = pve.convertTo(mode);
                vars.add(var);
            }
            entity.setVariants(vars);
            entity.setVariantNames(this.variantNames);
        }
        
        if (this.groups != null) {
            HashSet<ProductGroupAsscnEntity> asscns = new HashSet<>();
            for (ProductGroupAsscn pgae : this.groups) {
                ProductGroupAsscnEntity asscn = pgae.convertTo(mode);
                // null out the product code here .. it's redundant
                asscns.add(asscn);
            }
            entity.setGroups(asscns);
        }
        
        entity.setRevisionControl(this.revisionControl);
        
        return(entity);
    }
}
