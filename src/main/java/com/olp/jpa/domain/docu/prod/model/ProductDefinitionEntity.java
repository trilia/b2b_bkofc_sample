/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olp.jpa.domain.docu.prod.model;

import com.olp.annotations.KeyAttribute;
import com.olp.annotations.MultiTenant;
import com.olp.annotations.SortCriteria;
import com.olp.fwk.common.Constants;
import com.olp.jpa.common.RevisionControlBean;
import com.olp.jpa.common.TenantBasedSearchFilterFactory;
import com.olp.jpa.domain.docu.inv.model.ProductSkuEntity;
import com.olp.jpa.domain.docu.prod.model.ProductAttributesBean.CompositionType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.FullTextFilterDef;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

/**
 *
 * @author raghosh
 */
@Entity
@Table(name = "trl_product_definitions", uniqueConstraints = {@UniqueConstraint(columnNames = {"tenant_id", "product_code"})})
@Indexed(index = "SetupDataIndex")
@FullTextFilterDef(name="filter-product-definition", impl=TenantBasedSearchFilterFactory.class)
@NamedQueries({
    @NamedQuery(name = "ProductDef.findByCode", query = "SELECT t1 FROM ProductDefinitionEntity t1 JOIN FETCH t1.groups t2 JOIN FETCH t1.originProductRef t3 JOIN FETCH t1.revisions t4 JOIN FETCH t1.variants t5 JOIN FETCH t1.lobs t6 WHERE t1.productCode = :code AND ( t1.tenantId = :tenant OR t1.tenantId = :global ) ORDER BY t1.id")
    //@NamedQuery(name = "ProductDef.findActiveByCode", query = "SELECT t from ProductTemplateEntity t WHERE t.templateCode = :code AND ( t.tenantId = :tenant OR t.tenantId = :global ) AND t.endDate = :enddate ")
})
@MultiTenant(level = MultiTenant.Levels.ONE_N_GLOBAL)
@SortCriteria(attributes={"productCode"})
public class ProductDefinitionEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "product_def_id", nullable = false)
    private Long id;
    
    @KeyAttribute
    @Fields({
            @Field(analyze = Analyze.NO, store = Store.YES)
    })
    @Column(name="tenant_id", nullable=false)
    private String tenantId;
    
    @KeyAttribute
    @Column(name="product_code", nullable=false)
    @Fields({
        @Field(analyze=Analyze.NO)
    })
    private String productCode;
    
    @KeyAttribute
    @Column(name="product_version", nullable=false)
    @Fields({
        @Field(analyze=Analyze.NO)
    })
    private int productVersion;
    
    @Column(name="product_name", nullable=true)
    @Fields({
        @Field(analyze=Analyze.YES)
    })
    private String productName;
    
    @Column(name="description", nullable=true)
    @Fields({
        @Field(analyze=Analyze.YES)
    })
    private String description;
    
    @Column(name="composition_type", nullable=false)
    @Fields({
        @Field(analyze=Analyze.NO)
    })
    @Enumerated(EnumType.STRING)
    private CompositionType compositionType;
    
    @Column(name="status", nullable=false)
    @Fields({
        @Field(analyze=Analyze.NO)
    })
    private String status;
    
    @Column(name="enabled_flag", nullable=false)
    @Fields({
        @Field(analyze=Analyze.NO)
    })
    private boolean isEnabled;
    
    @Column(name="variant_names", nullable=true)
    @Fields({
        @Field(analyze=Analyze.YES)
    })
    @ElementCollection
    private List<String> variantNames = new ArrayList<>();
    
    @ManyToOne(optional=true)
    @JoinColumn(name="origin-product_ref")
    @ContainedIn
    private ProductDefinitionEntity originProductRef;
    
    @OneToMany(mappedBy="productDefRef", cascade={CascadeType.ALL})
    @IndexedEmbedded(includeEmbeddedObjectId=true, depth=1)
    private Set<ProductGroupAsscnEntity> groups = new HashSet<>();
    
    @OneToMany(mappedBy="productDefRef", cascade={CascadeType.ALL})
    @IndexedEmbedded(includeEmbeddedObjectId=true, depth=1)
    private List<ProductRevisionEntity> revisions = new ArrayList<>();
    
    @OneToMany(mappedBy="productDefRef", cascade={CascadeType.ALL})
    @IndexedEmbedded(includeEmbeddedObjectId=true, depth=1)
    private Set<ProductVariantEntity> variants = new HashSet<>();
    
    @OneToMany(mappedBy="productDefRef", cascade={CascadeType.ALL})
    @IndexedEmbedded(includeEmbeddedObjectId=true, depth=1)
    private Set<ProductLobEntity> lobs = new HashSet<>();
    
    @Embedded
    @IndexedEmbedded
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

    public CompositionType getCompositionType() {
        return compositionType;
    }

    public void setCompositionType(CompositionType compositionType) {
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
        if (variantNames != null)
            this.variantNames = variantNames;
        else
            this.variantNames.clear();
    }

    public ProductDefinitionEntity getOriginProductRef() {
        return originProductRef;
    }

    public void setOriginProductRef(ProductDefinitionEntity originProductRef) {
        this.originProductRef = originProductRef;
    }

    public Set<ProductGroupAsscnEntity> getGroups() {
        return groups;
    }

    public void setGroups(Set<ProductGroupAsscnEntity> groups) {
        if (groups != null)
            this.groups = groups;
        else
            this.groups.clear();
    }

    public Set<ProductVariantEntity> getVariants() {
        return variants;
    }

    public void setVariants(Set<ProductVariantEntity> variants) {
        if (variants != null)
            this.variants = variants;
        else
            this.variants.clear();
    }

    public List<ProductRevisionEntity> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<ProductRevisionEntity> revisions) {
        if (revisions != null)
            this.revisions = revisions;
        else
            this.revisions.clear();
    }

    public Set<ProductLobEntity> getLobs() {
        return lobs;
    }

    public void setLobs(Set<ProductLobEntity> lobs) {
        if (lobs != null)
            this.lobs = lobs;
        else
            this.lobs.clear();
    }

    public RevisionControlBean getRevisionControl() {
        return revisionControl;
    }

    public void setRevisionControl(RevisionControlBean revisionControl) {
        this.revisionControl = revisionControl;
    }
    
    public ProductDefinition convertTo(int mode) {
        
        ProductDefinition bean = new ProductDefinition();
        
        if (mode <= Constants.CONV_COMPLETE_DEFINITION)
            bean.setId(this.id);
        
        bean.setTenantId(this.tenantId);
        bean.setPartitionCode("ddadada");
        bean.setProductCode(this.productCode);
        bean.setProductName(this.productName);
        bean.setProductVersion(this.productVersion);
        bean.setDescription(this.description);
        bean.setCompositionType(this.compositionType);
        
        if (this.originProductRef != null)
            bean.setOriginProductRef(this.originProductRef.productCode);
        
        if (this.revisions != null) {
            ArrayList<ProductRevision> revs = new ArrayList<>();
            for (ProductRevisionEntity pre : this.revisions) {
                ProductRevision rev = pre.convertTo(mode);
                revs.add(rev);
            }
            bean.setRevisions(revs);
        }
        
        if (this.variants != null) {
            HashSet<ProductVariant> vars = new HashSet<>();
            for (ProductVariantEntity pve : this.variants) {
                ProductVariant var = pve.convertTo(mode);
                vars.add(var);
            }
            bean.setVariants(vars);
            bean.setVariantNames(this.variantNames);
        }
        
        if (this.groups != null) {
            HashSet<ProductGroupAsscn> asscns = new HashSet<>();
            for (ProductGroupAsscnEntity pgae : this.groups) {
                ProductGroupAsscn asscn = pgae.convertTo(mode);
                // null out the product code here .. it's redundant
                asscns.add(asscn);
            }
            bean.setGroups(asscns);
        }
        
        if (mode <= Constants.CONV_WITH_REVISION_INFO)
            bean.setRevisionControl(this.revisionControl);
        
        return(bean);
    }
}