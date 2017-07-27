package io.elastest.etm.test.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.elastest.etm.ElastestETMSpringBoot;
import io.elastest.etm.api.model.Project;
import io.elastest.etm.api.model.SutSpecification;
import io.elastest.etm.api.model.TJob;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElastestETMSpringBoot.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class SutApiItTest {
static final Logger log = LoggerFactory.getLogger(SutApiItTest.class);
	
	@LocalServerPort
    int serverPort;
	
	@Autowired
	TestRestTemplate restTemplate;
	
	long projectId;
	
	String baseSutName = "sut";
	
	static CountDownLatch latch;
		
	@BeforeAll
	static void staticSetup(){
		latch = new CountDownLatch(1);
	}
	
	@BeforeEach
    void setup() {
        log.info("App started on port {}", serverPort);
        projectId = createProjectToTesting("P00000000").getId();
    }
	
	@AfterEach
	void reset(){
		deleteProject(projectId);
	}
	
	
	@Test
	public void testCreateSutSpecification(){
		log.info("Start the test testCreateSutSpecification" );		
		
		String requestJson ="{"
				  + "\"description\": \"This is a SuT description example\","
				  + "\"id\": 0,"
				  + "\"name\": \"sut_definition_1\","
				  + "\"project\": { \"id\":"+ projectId + "},"
				  + "\"specification\": \"https://github.com/EduJGURJC/springbootdemo\""  
				+"}";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
		
		log.info("POST /api/sut");
		ResponseEntity<SutSpecification> response = restTemplate.postForEntity("/api/sut", entity, SutSpecification.class);
		log.info("Sut created:" + response.getBody());

		deleteSutSpecification(response.getBody().getId());

		assertAll("Validating sutSpecification Properties", 
				() -> assertTrue(response.getBody().getName().equals("sut_definition_1")),
				() -> assertNotNull(response.getBody().getId()),
				() -> assertTrue(response.getBody().getId() > 0)
				);
	}
	
	@Test
	public void testModifySut(){
		log.info("Start the test testModifySut" );
		
		SutSpecification sutSpec = createSutToTesting(projectId);
		sutSpec.setName("sut_definition_2");
						
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		log.info("Sut to modify:" +sutSpec.toString());
		
		String requestJson ="{"
				  + "\"description\": \"" + sutSpec.getDescription() + "\","
				  + "\"id\": " +sutSpec.getId() + ","
				  + "\"name\": \"" + sutSpec.getName() + "\","
				  + "\"project\": { \"id\":"+ sutSpec.getProject().getId() + "},"
				  + "\"specification\": \"" + sutSpec.getSpecification() + "\""  
				+"}";

		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
	
		log.info("PUT /api/sut");
		restTemplate.put("/api/sut", entity, SutSpecification.class);
		
		SutSpecification sutSpecModified = getSutById(sutSpec.getId());
		

		deleteSutSpecification(sutSpec.getId());

		assertAll("Validating sutSpecification Properties", 
				() -> assertTrue(sutSpecModified.getName().equals("sut_definition_2")),
				() -> assertNotNull(sutSpecModified.getId()),
				() -> assertTrue(sutSpecModified.getId() > 0)
				);
	}
	
	@Test
	public void testGetSuts(){
		log.info("Start the test testGetSuts" );
		
		List<SutSpecification> tSutsToGet = new ArrayList<>();
		
		for (int i = 0; i < 3; i++){
			tSutsToGet.add(createSutToTesting(projectId));
		}
        
        log.debug("GET /api/sut");
        ResponseEntity<SutSpecification[]> response = restTemplate.getForEntity("/api/sut", SutSpecification[].class);
        SutSpecification[] suts = response.getBody();
        
        for (SutSpecification sut: suts){
        	deleteSutSpecification(sut.getId());
		}
                        
        log.info("Suts Array size:" + suts.length);
        assertTrue(suts.length > 0);
	}
	
	@Test
	public void testDeleteSut(){
		log.info("Start the test testDeleteSut" );
		
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("sutId", createSutToTesting(projectId).getId());

        log.info("DELETE /api/sut/{sutId}");
        ResponseEntity<Long> response = restTemplate.exchange("/api/sut/{sutId}", HttpMethod.DELETE, null, Long.class, urlParams);
        log.info("Deleted sut:" + response.getBody().longValue());
        
        assertTrue(response.getBody().longValue() == urlParams.get("sutId"));
	}
	
	public SutSpecification getSutById(Long sutId){
		
		log.info("Start the method getSutById" );

		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("sutId", sutId);
	
		log.info("GET /api/sut/{sutId}");
		ResponseEntity<SutSpecification> response = restTemplate.getForEntity("/api/sut/{sutId}", SutSpecification.class, urlParams);
		
		return response.getBody();
		
	}
	
	private SutSpecification createSutToTesting(long projectId){		

		String requestJson ="{"
				  + "\"description\": \"This is a SuT description example\","
				  + "\"id\": 0,"
				  + "\"name\": \"sut_definition_1\","
				  + "\"project\": { \"id\":"+ projectId + "},"
				  + "\"specification\": \"https://github.com/EduJGURJC/springbootdemo\""  
				+"}";
	
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
	
		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
		
		log.info("POST /api/sut");
		ResponseEntity<SutSpecification> response = restTemplate.postForEntity("/api/sut", entity, SutSpecification.class);
		log.info("Sut created:" + response.getBody());

		return response.getBody();

	}
	
	private void deleteSutSpecification(Long sutId){
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("sutId", sutId);

        log.info("DELETE /api/sut/{sutId}");
        ResponseEntity<Long> response = restTemplate.exchange("/api/sut/{sutId}", HttpMethod.DELETE, null, Long.class, urlParams);
        log.info("Deleted sutSpecification:" + response.getBody().longValue());                
        
	}
	
	private Project createProjectToTesting(String projectName){		

		String requestJson = "{ \"id\": 0,\"name\": \"" + projectName + "\"}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);

		log.info("POST /project");
		ResponseEntity<Project> response = restTemplate.postForEntity("/api/project", entity, Project.class);

		return response.getBody();

	}
	
	private void deleteProject(Long projectToDeleteId){
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("projectId", projectToDeleteId);

        log.info("DELETE /project");
        ResponseEntity<Long> response = restTemplate.exchange("/api/project/{projectId}", HttpMethod.DELETE, null, Long.class, urlParams);
        log.info("Deleted project:" + response.getBody());                
        
	}

}