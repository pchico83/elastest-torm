package io.elastest.etm.docker;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.LogConfig.LoggingType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import io.elastest.etm.api.model.SutExecution;
import io.elastest.etm.service.sut.SutService;

@Service
public class DockerService {

	private String testImage = "";
	private static String appImage = "edujgurjc/torm-loadapp";

	@Autowired
	private SutService sutService;

	public void loadBasicServices(DockerExecution dockerExec) throws Exception {
		try {
			configureDocker(dockerExec);
			createNetwork(dockerExec);
			if (dockerExec.isWithSut()) {
				startSut(dockerExec);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/* Config Methods */

	public void configureDocker(DockerExecution dockerExec) {
		boolean windowsSo = false;
		if (windowsSo) {
			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost("tcp://192.168.99.100:2376").build();
			dockerExec.setDockerClient(DockerClientBuilder.getInstance(config).build());
		} else {
			dockerExec.setDockerClient(DockerClientBuilder.getInstance().build());
		}
	}

	public void createNetwork(DockerExecution dockerExec) {
		dockerExec.generateNetwork();
		dockerExec.getDockerClient().createNetworkCmd().withName(dockerExec.getNetwork()).exec();
	}

	/* Starting Methods */

	public void startSut(DockerExecution dockerExec) {
		SutExecution sutExec = sutService.createSutExecutionBySut(dockerExec.gettJobexec().getTjob().getSut());
		try {
			System.out.println("Starting sut " + dockerExec.getExecutionId());
			String envVar = "REPO_URL=" + sutExec.getSutSpecification().getSpecification();

			LogConfig logConfig = new LogConfig();
			logConfig.setType(LoggingType.SYSLOG);
			Map<String, String> configMap = new HashMap<String, String>();
			configMap.put("syslog-address", "tcp://" + getHostIp(dockerExec) + ":5001");
			configMap.put("tag", "sut_" + dockerExec.getExecutionId() + "_tjobexec");

			logConfig.setConfig(configMap);

			dockerExec.getDockerClient().pullImageCmd(appImage).exec(new PullImageResultCallback()).awaitSuccess();

			dockerExec.setAppContainer(dockerExec.getDockerClient().createContainerCmd(appImage).withEnv(envVar)
					.withLogConfig(logConfig).withNetworkMode(dockerExec.getNetwork())
					.withName("sut_" + dockerExec.getExecutionId()).exec());

			sutExec.deployStatus(SutExecution.DeployStatusEnum.DEPLOYED);

			String appContainerId = dockerExec.getAppContainer().getId();
			dockerExec.setAppContainerId(appContainerId);

			dockerExec.getDockerClient().startContainerCmd(appContainerId).exec();

			String sutIP = getContainerIp(appContainerId, dockerExec);
			sutIP = sutIP.split("/")[0];
			sutIP = "http://" + sutIP + ":8080";
			sutExec.setUrl(sutIP);
		} catch (Exception e) {
			e.printStackTrace();
			sutExec.deployStatus(SutExecution.DeployStatusEnum.ERROR);
			endSutExec(dockerExec);
			removeNetwork(dockerExec);
		}
		dockerExec.setSutExec(sutExec);
	}

	public void startTest(String testImage, DockerExecution dockerExec) {
		try {
			System.out.println("Starting test " + dockerExec.getExecutionId());
			this.testImage = testImage;

			String envVar = "DOCKER_HOST=tcp://172.17.0.1:2376";
			String envVar2 = "APP_IP=" + (dockerExec.isWithSut() ? dockerExec.getSutExec().getUrl() : "0");
			String envVar3 = "NETWORK=" + dockerExec.getNetwork();
			ArrayList<String> envList = new ArrayList<>();
			envList.add(envVar);
			envList.add(envVar2);
			envList.add(envVar3);

			LogConfig logConfig = new LogConfig();
			logConfig.setType(LoggingType.SYSLOG);
			Map<String, String> configMap = new HashMap<String, String>();
			configMap.put("syslog-address", "tcp://" + getHostIp(dockerExec) + ":5000");
			configMap.put("tag", "test_" + dockerExec.getExecutionId() + "_tjobexec");

			logConfig.setConfig(configMap);

			dockerExec.getDockerClient().pullImageCmd(testImage).exec(new PullImageResultCallback()).awaitSuccess();

			dockerExec.setTestcontainer(dockerExec.getDockerClient().createContainerCmd(testImage).withEnv(envList)
					.withLogConfig(logConfig).withNetworkMode(dockerExec.getNetwork())
					.withName("test_" + dockerExec.getExecutionId()).exec());

			String testContainerId = dockerExec.getTestcontainer().getId();
			dockerExec.setTestContainerId(testContainerId);

			dockerExec.getDockerClient().startContainerCmd(testContainerId).exec();
			int code = dockerExec.getDockerClient().waitContainerCmd(testContainerId)
					.exec(new WaitContainerResultCallback()).awaitStatusCode();
			System.out.println("Test container ends with code " + code);

			// this.saveTestSuite();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				endAllExec(dockerExec);
			} catch (Exception e1) {}
		}
	}

	/* End execution methods */

	public void endAllExec(DockerExecution dockerExec) throws Exception {
		try {
			endTestExec(dockerExec);
			if (dockerExec.isWithSut()) {
				endSutExec(dockerExec);
			}
			removeNetwork(dockerExec);
		} catch (Exception e) {
			throw new Exception("end error"); //TODO Customize Exception
		}
	}

	public void endTestExec(DockerExecution dockerExec) {
		try {
			System.out.println("Ending test execution " + dockerExec.getExecutionId());
			try {
				dockerExec.getDockerClient().stopContainerCmd(dockerExec.getTestContainerId()).exec();
			} catch (Exception e) {
			}
			dockerExec.getDockerClient().removeContainerCmd(dockerExec.getTestContainerId()).exec();
			try {
				dockerExec.getDockerClient().removeImageCmd(testImage).withForce(true).exec();
			} catch (Exception e) {
				System.out.println("Remove image " + testImage + " failed. In use. " + dockerExec.getExecutionId());
			}
		} catch (Exception e) {
			System.out.println("Error on ending test execution  " + dockerExec.getExecutionId());

		}
	}

	public void endSutExec(DockerExecution dockerExec) {
		SutExecution sutExec = dockerExec.getSutExec();
		sutExec.deployStatus(SutExecution.DeployStatusEnum.UNDEPLOYING);
		try {
			System.out.println("Ending sut execution " + dockerExec.getExecutionId());
			try {
				dockerExec.getDockerClient().stopContainerCmd(dockerExec.getAppContainerId()).exec();
			} catch (Exception e) {
			}
			dockerExec.getDockerClient().removeContainerCmd(dockerExec.getAppContainerId()).exec();
			try {
				dockerExec.getDockerClient().removeImageCmd(appImage).withForce(true).exec();
			} catch (Exception e) {
				System.out.println("Remove image " + appImage + " failed. In use. " + dockerExec.getExecutionId());
			}
			sutExec.deployStatus(SutExecution.DeployStatusEnum.UNDEPLOYED);
		} catch (Exception e) {
			sutExec.deployStatus(SutExecution.DeployStatusEnum.ERROR);
			System.out.println("Error on ending Sut execution " + dockerExec.getExecutionId());
		}
		dockerExec.setSutExec(sutExec);
		sutService.modifySutExec(dockerExec.getSutExec());
	}

	public void removeNetwork(DockerExecution dockerExec) {
		System.out.println("Removing docker network... " + dockerExec.getExecutionId());
		dockerExec.getDockerClient().removeNetworkCmd(dockerExec.getNetwork()).exec();
	}

	/* Utils */

	public String getContainerIp(String containerId, DockerExecution dockerExec) {
		return dockerExec.getDockerClient().inspectContainerCmd(containerId).exec().getNetworkSettings().getNetworks()
				.get(dockerExec.getNetwork()).getIpAddress();
	}

	public String getHostIp(DockerExecution dockerExec) {
		return dockerExec.getDockerClient().inspectNetworkCmd().withNetworkId(dockerExec.getNetwork()).exec().getIpam()
				.getConfig().get(0).getGateway();
	}

	public boolean imageExist(String imageName, DockerExecution dockerExec) {
		return !dockerExec.getDockerClient().searchImagesCmd(imageName).exec().isEmpty();
	}

}