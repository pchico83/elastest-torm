package io.elastest.etm.service;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS;
import static org.springframework.http.HttpStatus.OK;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.ws.http.HTTPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TJobExecutionFile;
import io.elastest.etm.utils.ElastestConstants;

@Service
public class TJobService {
	private static final Logger logger = LoggerFactory.getLogger(TJobService.class);
	
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${registry.contextPath}")
    private String registryContextPath;
    
    @Value("${et.shared.folder}")
    private String sharedFolder;

	private final TJobRepository tJobRepo;
	private final TJobExecRepository tJobExecRepositoryImpl;
	private final TJobExecOrchestratorService epmIntegrationService;
		
	public TJobService(TJobRepository tJobRepo, TJobExecRepository tJobExecRepositoryImpl, TJobExecOrchestratorService epmIntegrationService) {
		super();
		this.tJobRepo = tJobRepo;
		this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
		this.epmIntegrationService = epmIntegrationService;		
	}

	public TJob createTJob(TJob tjob) {		
		return tJobRepo.save(tjob);
	}

	public void deleteTJob(Long tJobId) {
		TJob tJob = tJobRepo.findOne(tJobId);
		tJobRepo.delete(tJob);
	}

	public List<TJob> getAllTJobs() {
		return tJobRepo.findAll();
	}
	
	public TJobExecution executeTJob(Long tJobId, List<Parameter> parameters) {
		TJob tjob = tJobRepo.findOne(tJobId);
		TJobExecution tJobExec = new TJobExecution();
		tJobExec.setTjob(tjob);
		tJobExec.setParameters(parameters);
		tJobExec = tJobExecRepositoryImpl.save(tJobExec);
		
		//After first save, get real Id
		tJobExec.setLogIndex(tJobExec.getId().toString());
		tJobExec = tJobExecRepositoryImpl.save(tJobExec);
		
		if (!tjob.isExternal()) {
			epmIntegrationService.executeTJob(tJobExec, tjob.getSelectedServices());
		}
		
		return tJobExec;
	}
	
	public void finishTJobExecution(Long tJobExecId){
		TJobExecution tJobExecution = tJobExecRepositoryImpl.findOne(tJobExecId);
		tJobExecution.setResult(ResultEnum.FINISHED);
		
		tJobExecRepositoryImpl.save(tJobExecution);
	}
	
	public void deleteTJobExec(Long tJobExecId) {
		TJobExecution tJobExec = tJobExecRepositoryImpl.findOne(tJobExecId);
		tJobExecRepositoryImpl.delete(tJobExec);
	}

	public TJob getTJobById(Long tJobId) {
		return tJobRepo.findOne(tJobId);
	}
	
	public TJob getTJobByName(String name){
		return tJobRepo.findByName(name);
	}

	public List<TJobExecution> getTJobsExecutionsByTJobId(Long tJobId) {
		TJob tJob = tJobRepo.findOne(tJobId);
		return getTJobsExecutionsByTJob(tJob);
	}

	public List<TJobExecution> getTJobsExecutionsByTJob(TJob tJob) {
		return tJobExecRepositoryImpl.findByTJob(tJob);
	}

	public TJobExecution getTJobsExecution(Long tJobId, Long tJobExecId) {
		TJob tJob = tJobRepo.findOne(tJobId);
		return tJobExecRepositoryImpl.findByIdAndTJob(tJobExecId, tJob);
	}

	public TJob modifyTJob(TJob tJob) throws RuntimeException {
		if (tJobRepo.findOne(tJob.getId()) != null) {
			return tJobRepo.save(tJob);
		} else {
			throw new HTTPException(405);
		}
	}
	
	public List<TJobExecutionFile> getTJobExecutionFilesUrls(Long tJobId, Long tJobExecId){
		logger.info("Retrived the files generated by the TJob execution: {}", tJobExecId);
		List<TJobExecutionFile> filesList = new ArrayList<TJobExecutionFile>();
		
		TJob tJob = getTJobById(tJobId);
		tJob.getSelectedServices();
		
		String fileSeparator = IS_OS_WINDOWS ? "\\\\" : "/";
		String tJobExecFilePath = "tjobs" + fileSeparator + "tjob_" + tJobId
				+ fileSeparator + "exec_" + tJobExecId + fileSeparator;
		String tJobExecFolder = sharedFolder + tJobExecFilePath;
		
		
		logger.debug("Shared folder:" + tJobExecFolder);
		try {
			File file = ResourceUtils.getFile(tJobExecFolder);
			// If not in dev mode
			if (file.exists()) {
				List<String> servicesFolders = new ArrayList<>(Arrays.asList(file.list()));
				for (String serviceFolderName: servicesFolders) {
					logger.debug("Files folder:" + serviceFolderName);
					logger.debug("Full path:" + tJobExecFolder + serviceFolderName);
					File serviceFolder = ResourceUtils.getFile(tJobExecFolder + serviceFolderName);
					List<String> servicesFilesNames = new ArrayList<>(Arrays.asList(serviceFolder.list()));
					for (String serviceFileName: servicesFilesNames){
						filesList.add(new TJobExecutionFile(serviceFileName, getFileUrl(tJobExecFilePath + serviceFileName), serviceFolderName));
					}
				}
			}
		} catch (IOException fnfe) {
			logger.warn("Error building the URLs of the files of the executio {}", tJobExecId);
		}
		
		return filesList;
	}
	
	public String getFileUrl(String serviceFilePath) throws IOException {				
		String urlResponse = contextPath.replaceFirst("/", "") + registryContextPath + "/" + serviceFilePath.replace("\\\\", "/");		
		return urlResponse;
	}
	
	public void getFiles(Long tJobId, Long tJobExecId) {
		
	}

}