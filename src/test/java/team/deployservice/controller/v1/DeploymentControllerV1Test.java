package team.deployservice.controller.v1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import team.deployservice.model.Deployment;
import team.deployservice.repo.DeploymentRepo;
import team.deployservice.service.DeploymentService;
import team.deployservice.service.DeploymentServiceImpl;

import java.time.Month;
import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DeploymentControllerV1.class)
class DeploymentControllerV1Test
    {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private DeploymentRepo mockdeploymentRepo;

    @TestConfiguration
    static class DeploymentServiceImplTestContextConfiguration
        {
        @MockBean
        private DeploymentRepo mockdeploymentRepo;
        @Bean
        public DeploymentService deploymentService()
            {
            return new DeploymentServiceImpl(mockdeploymentRepo);
            }
        }
    
    @Test
    void store() throws Exception
        {
        mockMvc.perform(post("/api/v1/deployment").contentType(MediaType.APPLICATION_JSON)
            .content(
            "{\n" +
            "    \"deploymentId\": \"d1\",\n" +
            "    \"applicationId\": \"a1\",\n" +
            "    \"componentId\": \"c1\",\n" +
            "    \"created\": \"2020-11-30 23:00:00\",\n" +
            "    \"source\": \"test\",\n" +
            "    \"changes\": [\n" +
            "      {\n" +
            "        \"id\": \"c123\",\n" +
            "        \"created\": \"2020-11-20 22:00:00\",\n" +
            "        \"source\": \"test\",\n" +
            "        \"eventType\": \"test\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": \"c234\",\n" +
            "        \"created\": \"2020-11-20 22:01:00\",\n" +
            "        \"source\": \"test\",\n" +
            "        \"eventType\": \"test\"\n" +
            "      }\n" +
            "    ]\n" +
            "}"
            ))
            .andExpect(status().isCreated());
        verify(mockdeploymentRepo, times(1)).save(any(Deployment.class));
        }

    @Test
    void list() throws Exception
        {
        mockMvc.perform(get("/api/v1/deployment")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(mockdeploymentRepo, times(1)).findAll();
        }

    @Test
    void show() throws Exception
        {
        String id = "testId";
        mockMvc.perform(get("/api/v1/deployment/" + id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(mockdeploymentRepo, times(1)).findById(id);
        }

    @Test
    void listByApp() throws Exception
        {
        String appId = "id123";
        mockMvc.perform(get("/api/v1/deployment/application/" + appId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(mockdeploymentRepo, times(1)).findByApplicationId(appId);
        }

    @Test
    void calcDeployFreq() throws Exception
        {
        LocalDateTime date = LocalDate.now().minusDays(1).atStartOfDay();
        Date reportingDate = Date.from(date.toInstant(ZoneOffset.UTC));
        String appId = "a1";
        mockMvc.perform(get("/api/v1/deployment/application/" + appId + "/frequency")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(mockdeploymentRepo, times(1)).findByApplicationIdAndCreatedBetweenOrderByCreated(appId, reportingDate, reportingDate);
        }
    
    @Test
    void calcDeployFreqByDate() throws Exception
        {
        LocalDateTime date = LocalDate.of(2020, Month.OCTOBER, 3).atStartOfDay();
        Date reportingDate = Date.from(date.toInstant(ZoneOffset.UTC));
        String appId = "a1";
        mockMvc.perform(get("/api/v1/deployment/application/" + appId + "/frequency/2020-10-03")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(mockdeploymentRepo, times(1)).findByApplicationIdAndCreatedBetweenOrderByCreated(appId, reportingDate, reportingDate);
        }
    }