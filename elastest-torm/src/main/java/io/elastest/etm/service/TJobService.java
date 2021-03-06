package io.elastest.etm.service;

import java.io.IOException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.ws.http.HTTPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.model.EimMonitoringConfig.BeatsStatusEnum;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TJobExecutionFile;

@Service
public class TJobService {
    private static final Logger logger = LoggerFactory
            .getLogger(TJobService.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${registry.contextPath}")
    private String registryContextPath;

    private final TJobRepository tJobRepo;
    private final TJobExecRepository tJobExecRepositoryImpl;
    private final TJobExecOrchestratorService tJobExecOrchestratorService;
    private final EsmService esmService;

    Map<String, Future<Void>> asyncExecs = new HashMap<String, Future<Void>>();

    public TJobService(TJobRepository tJobRepo,
            TJobExecRepository tJobExecRepositoryImpl,
            TJobExecOrchestratorService epmIntegrationService,
            EsmService esmService) {
        super();
        this.tJobRepo = tJobRepo;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.tJobExecOrchestratorService = epmIntegrationService;
        this.esmService = esmService;
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

    public String getMapNameByTJobExec(TJobExecution tJobExec) {
        return tJobExec.getTjob().getId() + "_" + tJobExec.getId();
    }

    public TJobExecution executeTJob(Long tJobId, List<Parameter> parameters,
            List<Parameter> sutParameters) throws HttpClientErrorException {
        TJob tJob = tJobRepo.findOne(tJobId);

        SutSpecification sut = tJob.getSut();
        // Checks if has sut instrumented by elastest and beats status is
        // activating yet
        if (sut != null && sut.isInstrumentedByElastest()
                && sut.getEimMonitoringConfig() != null
                && sut.getEimMonitoringConfig()
                        .getBeatsStatus() == BeatsStatusEnum.ACTIVATING) {
            throw new HttpClientErrorException(HttpStatus.ACCEPTED);
        }

        TJobExecution tJobExec = new TJobExecution();
        tJobExec.setStartDate(new Date());
        if (tJob.getSut() != null && sutParameters != null
                && !sutParameters.isEmpty()) {
            tJob.getSut().setParameters(sutParameters);
        }
        tJobExec.setTjob(tJob);
        if (parameters != null && !parameters.isEmpty()) {
            tJobExec.setParameters(parameters);
        }
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        // After first save, get real Id
        tJobExec.generateMonitoringIndex();
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        Future<Void> asyncExec;
        if (!tJob.isExternal()) {
            asyncExec = tJobExecOrchestratorService.executeTJob(tJobExec,
                    tJob.getSelectedServices());
            asyncExecs.put(getMapNameByTJobExec(tJobExec), asyncExec);
        } else {
            tJobExecOrchestratorService.executeExternalJob(tJobExec);
        }

        return tJobExec;
    }

    public TJobExecution stopTJobExec(Long tJobExecId) {
        TJobExecution tJobExec = tJobExecRepositoryImpl.findOne(tJobExecId);
        String mapKey = getMapNameByTJobExec(tJobExec);
        Future<Void> asyncExec = asyncExecs.get(mapKey);

        boolean cancelExecuted = false;

        try {
            cancelExecuted = asyncExec.cancel(true);
            // If is not cancelled, stop async Exec and stop services
            if (cancelExecuted) {
                logger.info("Forcing Execution Stop");
                try {
                    tJobExec = tJobExecOrchestratorService
                            .forceEndExecution(tJobExec);
                    logger.info("Execution Stopped Successfully!");
                } catch (Exception e) {
                    logger.error("Error on forcing Execution stop");
                }
            } else { // If is already finished, gets TJobExec
                tJobExec = tJobExecRepositoryImpl.findOne(tJobExecId);
            }
            asyncExecs.remove(mapKey);
        } catch (Exception e) {
            logger.info("Error during forcing stop", e);
        }
        return tJobExec;
    }

    public void endExternalTJobExecution(long tJobExecId, int result) {
        logger.info("Finishing the external Job.");
        TJobExecution tJobExec = this.getTJobExecById(tJobExecId);
        tJobExec.setResult(ResultEnum.values()[result]);
        tJobExecRepositoryImpl.save(tJobExec);
        try {
            tJobExecOrchestratorService.deprovideServices(tJobExec);
        } catch (Exception e) {
            logger.error(
                    "Exception during desprovisioning of the TSS associated with an External TJob.");
        }
    }

    public void deleteTJobExec(Long tJobExecId) {
        TJobExecution tJobExec = tJobExecRepositoryImpl.findOne(tJobExecId);
        tJobExecRepositoryImpl.delete(tJobExec);
    }

    public TJob getTJobById(Long tJobId) {
        return tJobRepo.findOne(tJobId);
    }

    public TJob getTJobByName(String name) {
        return tJobRepo.findByName(name);
    }

    public List<TJobExecution> getAllTJobExecs() {
        return tJobExecRepositoryImpl.findAll();
    }

    public TJobExecution getTJobExecById(Long id) {
        return tJobExecRepositoryImpl.findOne(id);
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

    public List<TJobExecutionFile> getTJobExecutionFilesUrls(Long tJobId,
            Long tJobExecId) throws InterruptedException {
        return esmService.getTJobExecutionFilesUrls(tJobId, tJobExecId);
    }

    public String getFileUrl(String serviceFilePath) throws IOException {
        String urlResponse = contextPath.replaceFirst("/", "")
                + registryContextPath + "/"
                + serviceFilePath.replace("\\\\", "/");
        return urlResponse;
    }

    public void getFiles(Long tJobId, Long tJobExecId) {

    }

}