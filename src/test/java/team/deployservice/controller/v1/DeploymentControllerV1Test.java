package team.deployservice.controller.v1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import team.deployservice.model.*;
import team.deployservice.service.DeploymentService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DeploymentControllerV1.class)
class DeploymentControllerV1Test
    {
    
    @Autowired private MockMvc mockMvc;
    
    @MockBean private DeploymentService mockDeploymentService;
    
    @Test
    void store() throws Exception
        {
        mockMvc.perform(post("/api/v1/deployment").contentType(MediaType.APPLICATION_JSON)
            .content(
            "{\n" +
            "    \"deploymentId\": \"d1\",\n" +
            "    \"applicationId\": \"a1\",\n" +
            "    \"created\": \"2020-11-30T23:00:00.000+00:00\",\n" +
            "    \"source\": \"test\",\n" +
            "    \"changes\": [\n" +
            "      {\n" +
            "        \"id\": \"c123\",\n" +
            "        \"created\": \"2020-11-20T22:00:00.000+00:00\",\n" +
            "        \"source\": \"test\",\n" +
            "        \"eventType\": \"test\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": \"c234\",\n" +
            "        \"created\": \"2020-11-20T22:01:00.000+00:00\",\n" +
            "        \"source\": \"test\",\n" +
            "        \"eventType\": \"test\"\n" +
            "      }\n" +
            "    ]\n" +
            "}"
            ))
            .andExpect(status().isCreated());
        verify(mockDeploymentService, times(1)).store(any(Deployment.class));
        }

    @Test
    void list() throws Exception
        {
        //TODO expand this test to check the json output
        mockMvc.perform(get("/api/v1/deployment")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(mockDeploymentService, times(1)).list();
        }

    @Test
    void show() throws Exception
        {
        ZonedDateTime reportingDate = LocalDate.of(2020, 10, 10).atStartOfDay(ZoneId.of("UTC"));
        
        String id = "testId";
        Change c1 = new Change("c1", Date.from(reportingDate.toInstant()), "test", "test");
        HashSet<Change> set = new HashSet<>();
        set.add(c1);
        Deployment d1 = new Deployment("d1", "d1", "a1", Date.from(reportingDate.toInstant()), "test", set);
        when(mockDeploymentService.get(id)).thenReturn(Optional.of(d1));
        MvcResult result = mockMvc.perform(get("/api/v1/deployment/" + id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertThat(content, is(equalTo("{\"id\":null,\"deploymentId\":\"d1\",\"deploymentDesc\":\"d1\",\"applicationId\":\"a1\",\"created\":\"2020-10-10T00:00:00.000+00:00\",\"source\":\"test\",\"changes\":[{\"id\":\"c1\",\"created\":\"2020-10-10T00:00:00.000+00:00\",\"source\":\"test\",\"eventType\":\"test\",\"leadTimeSeconds\":0}],\"leadTimeSeconds\":0,\"leadTimePerfLevel\":null}")));
        verify(mockDeploymentService, times(1)).get(id);
        }

    @Test
    void listByApp() throws Exception
        {
        //TODO expand this test to check the json output
        String appId = "id123";
        mockMvc.perform(get("/api/v1/deployment/application/" + appId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(mockDeploymentService, times(1)).listAllForApplication(appId);
        }

    @Test
    void calcDeployFreq() throws Exception
        {
        LocalDateTime date = LocalDate.now().minusDays(1).atStartOfDay();
        Date startDate = Date.from(date.toInstant(ZoneOffset.UTC));
        String appId = "a1";
        DeploymentFrequency fr = new DeploymentFrequency(appId, startDate, 10, TimePeriod.MONTH, DORALevel.MEDIUM);
        when(mockDeploymentService.calculateDeployFreq(appId, startDate)).thenReturn(fr);
        
        MvcResult result = mockMvc.perform(get("/api/v1/deployment/application/" + appId + "/frequency")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();
        
        String content = result.getResponse().getContentAsString();
        String dateOut = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        assertThat(content, is(equalTo("{\"applicationId\":\"" + appId + "\",\"reportingDate\":\"" + dateOut + "\",\"deploymentCount\":10,\"timePeriod\":\"MONTH\",\"deployFreqLevel\":\"MEDIUM\"}")));
        verify(mockDeploymentService, times(1)).calculateDeployFreq(appId, startDate);
        }
    
    @Test
    void calcDeployFreqByDate() throws Exception
        {
        LocalDateTime date = LocalDate.of(2020, Month.OCTOBER, 3).atStartOfDay();
        Date startDate = Date.from(date.toInstant(ZoneOffset.UTC));
        String appId = "a1";
        DeploymentFrequency fr = new DeploymentFrequency(appId, startDate, 10, TimePeriod.MONTH, DORALevel.MEDIUM);
        when(mockDeploymentService.calculateDeployFreq(appId, startDate)).thenReturn(fr);
        String dateOut = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        assertThat(dateOut, is(equalTo("2020-10-03")));

        MvcResult result = mockMvc.perform(get("/api/v1/deployment/application/" + appId + "/frequency/" + dateOut)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, is(equalTo("{\"applicationId\":\"" + appId + "\",\"reportingDate\":\"" + dateOut + "\",\"deploymentCount\":10,\"timePeriod\":\"MONTH\",\"deployFreqLevel\":\"MEDIUM\"}")));
        verify(mockDeploymentService, times(1)).calculateDeployFreq(appId, startDate);
        }

    @Test
    void calcLeadTime() throws Exception
        {
        LocalDateTime date = LocalDate.now().minusDays(1).atStartOfDay();
        Date endDate = Date.from(date.toInstant(ZoneOffset.UTC));
        String appId = "a1";
        LeadTime lt = new LeadTime(appId, endDate, 120, DORALevel.ELITE);
        when(mockDeploymentService.calculateLeadTime(appId, endDate)).thenReturn(lt);

        MvcResult result = mockMvc.perform(get("/api/v1/deployment/application/" + appId + "/lead_time")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();

        String dateOut = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        String content = result.getResponse().getContentAsString();
        assertThat(content, is(equalTo("{\"applicationId\":\"" + appId + "\",\"reportingDate\":\"" + dateOut + "\",\"leadTimeSeconds\":120,\"leadTimePerfLevel\":\"ELITE\"}")));
        verify(mockDeploymentService, times(1)).calculateLeadTime(appId, endDate);
        }

    @Test
    void calcLeadTimeByDate() throws Exception
        {
        LocalDateTime date = LocalDate.of(2020, Month.JULY, 6).atStartOfDay();
        Date startDate = Date.from(date.toInstant(ZoneOffset.UTC));
        String appId = "a1";
        LeadTime lt = new LeadTime(appId, startDate, 120, DORALevel.ELITE);
        when(mockDeploymentService.calculateLeadTime(appId, startDate)).thenReturn(lt);
        String dateOut = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        assertThat(dateOut, is(equalTo("2020-07-06")));

        MvcResult result = mockMvc.perform(get("/api/v1/deployment/application/" + appId + "/lead_time/" + dateOut)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();

        
        String content = result.getResponse().getContentAsString();
        assertThat(content, is(equalTo("{\"applicationId\":\"" + appId + "\",\"reportingDate\":\"" + dateOut + "\",\"leadTimeSeconds\":120,\"leadTimePerfLevel\":\"ELITE\"}")));
        verify(mockDeploymentService, times(1)).calculateLeadTime(appId, startDate);
        }
    }