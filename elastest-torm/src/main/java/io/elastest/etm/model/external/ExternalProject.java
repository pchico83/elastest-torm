package io.elastest.etm.model.external;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.TJobExecution.ResultEnum;

@Entity
public class ExternalProject implements Serializable {
    private static final long serialVersionUID = 1L;

    public interface BasicAttExternalProject {
    }

    @JsonView({ BasicAttExternalProject.class })
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ BasicAttExternalProject.class })
    @Column(name = "name")
    @JsonProperty("name")
    private String name = null;

    @JsonView({ BasicAttExternalProject.class })
    @Column(name = "type")
    @JsonProperty("type")
    private TypeEnum type = null;

    @JsonView(BasicAttExternalProject.class)
    @JsonProperty("exTestCases")
    // bi-directional many-to-one association to ExternalTestCase
    @OneToMany(mappedBy = "exProject", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ExternalTestCase> exTestCases;

    /* **************************/
    /* ***** Constructors *******/
    /* **************************/

    public ExternalProject() {
    }

    public ExternalProject(Long id) {
        this.id = id == null ? 0 : id;
    }

    public enum TypeEnum {
        /* FINISED STATUS */

        TESTLINK("TESTLINK");

        private String value;

        TypeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static TypeEnum fromValue(String text) {
            for (TypeEnum b : TypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    /* *****************************/
    /* ***** Getters/Setters *******/
    /* *****************************/

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id == null ? 0 : id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public List<ExternalTestCase> getExTestCases() {
        return exTestCases;
    }

    public void setExTestCases(List<ExternalTestCase> exTestCases) {
        this.exTestCases = exTestCases;
    }

}
