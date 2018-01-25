package io.elastest.etm.model.external;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.elastest.etm.model.external.ExternalProject.BasicAttExternalProject;
import io.elastest.etm.model.external.ExternalTestExecution.BasicAttExternalTestExecution;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ExternalTestCase {
    public interface BasicAttExternalTestCase {
    }

    @EmbeddedId
    @JsonView({ BasicAttExternalTestCase.class, BasicAttExternalProject.class,
            BasicAttExternalTestExecution.class })
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    private ExternalId id = null;

    @JsonView({ BasicAttExternalTestCase.class, BasicAttExternalProject.class,
            BasicAttExternalTestExecution.class })
    @Column(name = "name")
    @JsonProperty("name")
    private String name = null;

    @JsonView({ BasicAttExternalTestCase.class, BasicAttExternalProject.class,
            BasicAttExternalTestExecution.class })
    @Column(name = "fields", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("fields")
    private String fields = null;

    // bi-directional many-to-one association to ExternalProject
    @JsonView({ BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "pjExternalId", referencedColumnName = "externalId"),
            @JoinColumn(name = "pjExternalSystemId", referencedColumnName = "externalSystemId") })
    private ExternalProject exProject;

    // bi-directional many-to-one association to ExternalTestExecution
    @JsonView({ BasicAttExternalTestCase.class, BasicAttExternalProject.class })
    @OneToMany(mappedBy = "exTestCase", cascade = CascadeType.REMOVE)
    private List<ExternalTestExecution> exTestExecs;

    /* **************************/
    /* ***** Constructors *******/
    /* **************************/

    public ExternalTestCase() {
    }

    public ExternalTestCase(ExternalId id) {
        this.id = id;
    }

    /* *****************************/
    /* ***** Getters/Setters *******/
    /* *****************************/

    public ExternalId getId() {
        return id;
    }

    public void setId(ExternalId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public ExternalProject getExProject() {
        return exProject;
    }

    public void setExProject(ExternalProject exProject) {
        this.exProject = exProject;
    }

    public List<ExternalTestExecution> getExTestExecs() {
        return exTestExecs;
    }

    public void setExTestExecs(List<ExternalTestExecution> exTestExecs) {
        this.exTestExecs = exTestExecs;
    }

}