package team.deployservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import team.deployservice.model.*;
import team.deployservice.repo.DeploymentRepo;
import team.deployservice.hierarchy.repo.HierarchyClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class DeploymentServiceImplTest
    {
    @Autowired
    private DeploymentService deploymentService;
    @Autowired
    private DeploymentRepo mockdeploymentRepo;
    @Autowired
    private HierarchyClient mockHierarchyClient;

    @TestConfiguration
    static class DeploymentServiceImplTestContextConfiguration
        {
        @MockBean
        private DeploymentRepo mockdeploymentRepo;
        @MockBean
        private HierarchyClient mockHierarchyClient;
        @Bean
        public DeploymentService deploymentService()
            {
            return new DeploymentServiceImpl(mockdeploymentRepo, mockHierarchyClient);
            }
        }
    
    private Change c1;
    private Change c2;
    private Change c3;
    
    private Deployment setupDeployment(String appId, int c1M, int c1D, int c1H, int c2M, int c2D, int c2H, int c3M, int c3D, int c3H, int dM, int dD)
        {
        String rfcId = "rfc123";
        HashSet<Change> changes = new HashSet<>();
        c1 = new Change(
            "c1",
            dateOf(2020, c1M, c1D, c1H, 0, 0),
            "source",
            "event");
        changes.add(c1);
        // lead time seconds = 7 * 24 * 60 * 60 = 604800
        c2 = new Change(
            "c2",
            dateOf(2020, c2M, c2D, c2H, 0, 0),
            "source",
            "event");
        changes.add(c2);
        // lead time seconds = 6 * 24 * 60 * 60 = 518400
        c3 = new Change(
            "c3",
            dateOf(2020, c3M, c3D, c3H, 0, 0),
            "source",
            "event");
        // lead time seconds = 5 * 24 * 60 * 60 =  432000
        changes.add(c3);
        Deployment d1 = new Deployment(
            "d1",
            "deployment d1",
            appId,
            rfcId,
            dateOf(2020, dM, dD, 10, 0, 0),
            "source", changes);
        return d1;
        }
    
    static Date dateOf(int year,  int month, int dayOfMonth, int hour, int minute, int second){
        return Date.from(
            LocalDateTime.of(
                year, 
                month, 
                dayOfMonth, 
                hour, 
                minute, 
                second)
                .toInstant(ZoneOffset.UTC));
    }

    @Test
    void checkCommitLeadTimeCalc()
        {
        Deployment d1 = setupDeployment("a1", 3, 3, 10, 3, 4, 10, 3, 5, 10, 3, 10);
        when(mockdeploymentRepo.save(d1)).thenReturn(d1);
        Deployment storedDep = deploymentService.store(d1);
        assertThat(storedDep.getChanges(), hasItems(c1, c2, c3));
        storedDep.getChanges().forEach(
            change -> { 
            switch(change.getId()){
                case "c1":
                    assertThat(change.getLeadTimeSeconds(), equalTo(604800L));
                    break;
                case "c2":
                    assertThat(change.getLeadTimeSeconds(), equalTo(518400L));
                    break;
                case "c3":
                    assertThat(change.getLeadTimeSeconds(), equalTo(432000L));
                    break;
                }
            }
        );
        assertThat(storedDep.getLeadTimeSeconds(), equalTo(518400L));
        }

    @Test
    void checkEliteLeadTimePerfLevel()
        {
        Deployment d1 = setupDeployment("a1", 3, 10, 10, 3, 10, 11, 3, 10, 12, 3, 10);
        when(mockdeploymentRepo.save(d1)).thenReturn(d1);
        Deployment storedDep = deploymentService.store(d1);
        assertThat(storedDep.getLeadTimePerfLevel(), equalTo(DORALevel.ELITE));
        }
    
    @Test
    void checkHighLeadTimePerfLevel()
        {
        Deployment d1 = setupDeployment("a1", 3, 3, 10, 3, 4, 10, 3, 5, 10, 3, 10);
        when(mockdeploymentRepo.save(d1)).thenReturn(d1);
        Deployment storedDep = deploymentService.store(d1);
        assertThat(storedDep.getLeadTimePerfLevel(), equalTo(DORALevel.HIGH));
        }
    
    @Test
    void checkMedLeadTimePerfLevel()
        {
        Deployment d1 = setupDeployment("a1", 2, 20, 10, 2, 29, 10, 3, 5, 10, 3, 10);
        when(mockdeploymentRepo.save(d1)).thenReturn(d1);
        Deployment storedDep = deploymentService.store(d1);
        assertThat(storedDep.getLeadTimePerfLevel(), equalTo(DORALevel.MEDIUM));
        }

    @Test
    void checkLowLeadTimePerfLevel()
        {
        Deployment d1 = setupDeployment("a1", 1, 10, 10, 2, 29, 10, 2, 5, 10, 3, 10);
        when(mockdeploymentRepo.save(d1)).thenReturn(d1);
        Deployment storedDep = deploymentService.store(d1);
        assertThat(storedDep.getLeadTimePerfLevel(), equalTo(DORALevel.LOW));
        }
    
    @Test
    void checkEliteDeployFreqLevel()
        {
        String appId = "a1";
        Deployment d1 =  setupDeployment(appId, 1, 10, 10, 2, 29, 10, 2, 5, 10, 3, 10);
        Deployment d2 =  setupDeployment(appId, 1, 10, 10, 2, 29, 10, 2, 5, 10, 3, 10);
        List<Deployment> deploys = new ArrayList<>();
        deploys.add(d1);
        deploys.add(d2);
        when(mockdeploymentRepo.findByApplicationIdInAndCreatedBetweenOrderByCreated
            (Collections.singletonList(appId), 
                dateOf(2020, 3, 10, 0, 0, 0), 
                dateOf(2020, 3, 11, 0, 0, 0)))
            .thenReturn(deploys);
        when(mockHierarchyClient.findApplicationChildIds(appId)).thenReturn(Collections.singletonList(appId));
        DeploymentFrequency freq = deploymentService.calculateDeployFreq(appId, dateOf(2020, 3, 10, 0, 0, 0));
        assertThat(freq.getDeployFreqLevel(), equalTo(DORALevel.ELITE));
        assertThat(freq.getDeploymentCount(), equalTo(2));
        assertThat(freq.getTimePeriod(), equalTo(TimePeriod.DAY));
        }

    @Test
    void checkHighDeployFreqLevel()
        {
        String appId = "a1";
        Deployment d1 =  setupDeployment(appId, 1, 10, 10, 2, 29, 10, 2, 5, 10, 3, 10);
        Deployment d2 =  setupDeployment(appId, 1, 10, 10, 2, 29, 10, 2, 5, 10, 3, 7);
        List<Deployment> deploys = new ArrayList<>();
        deploys.add(d1);
        deploys.add(d2);
        when(mockdeploymentRepo.findByApplicationIdInAndCreatedBetweenOrderByCreated
            (Collections.singletonList(appId),
                dateOf(2020, 3, 4, 0, 0, 0),
                dateOf(2020, 3, 11, 0, 0, 0)))
            .thenReturn(deploys);
        when(mockHierarchyClient.findApplicationChildIds(appId)).thenReturn(Collections.singletonList(appId));
        DeploymentFrequency freq = deploymentService.calculateDeployFreq(appId, dateOf(2020, 3, 10, 0, 0, 0));
        assertThat(freq.getDeployFreqLevel(), equalTo(DORALevel.HIGH));
        assertThat(freq.getDeploymentCount(), equalTo(2));
        assertThat(freq.getTimePeriod(), equalTo(TimePeriod.WEEK));
        }

    @Test
    void checkMedDeployFreqLevel()
        {
        String appId = "a1";
        Deployment d1 =  setupDeployment(appId, 1, 10, 10, 2, 29, 10, 2, 5, 10, 3, 10);
        Deployment d2 =  setupDeployment(appId, 1, 10, 10, 2, 29, 10, 2, 5, 10, 2, 15);
        List<Deployment> deploys = new ArrayList<>();
        deploys.add(d1);
        deploys.add(d2);
        when(mockdeploymentRepo.findByApplicationIdInAndCreatedBetweenOrderByCreated
            (Collections.singletonList(appId),
                dateOf(2020, 2, 10, 0, 0, 0),
                dateOf(2020, 3, 11, 0, 0, 0)))
            .thenReturn(deploys);
        when(mockHierarchyClient.findApplicationChildIds(appId)).thenReturn(Collections.singletonList(appId));
        DeploymentFrequency freq = deploymentService.calculateDeployFreq(appId, dateOf(2020, 3, 10, 0, 0, 0));
        assertThat(freq.getDeployFreqLevel(), equalTo(DORALevel.MEDIUM));
        assertThat(freq.getDeploymentCount(), equalTo(2));
        assertThat(freq.getTimePeriod(), equalTo(TimePeriod.MONTH));
        }

    @Test
    void checkLowDeployFreqLevel()
        {
        String appId = "a1";
        Deployment d1 =  setupDeployment(appId, 1, 10, 10, 2, 29, 10, 2, 5, 10, 3, 10);
        Deployment d2 =  setupDeployment(appId, 1, 10, 10, 2, 29, 10, 2, 5, 10, 1, 15);
        List<Deployment> deploys = new ArrayList<>();
        deploys.add(d1);
        deploys.add(d2);
        when(mockdeploymentRepo.findByApplicationIdInAndCreatedBetweenOrderByCreated
            (Collections.singletonList(appId),
                dateOf(2019, 3, 12, 0, 0, 0),
                dateOf(2020, 3, 11, 0, 0, 0)))
            .thenReturn(deploys);
        when(mockHierarchyClient.findApplicationChildIds(appId)).thenReturn(Collections.singletonList(appId));
        DeploymentFrequency freq = deploymentService.calculateDeployFreq(appId, dateOf(2020, 3, 10, 0, 0, 0));
        assertThat(freq.getDeployFreqLevel(), equalTo(DORALevel.LOW));
        assertThat(freq.getDeploymentCount(), equalTo(2));
        assertThat(freq.getTimePeriod(), equalTo(TimePeriod.YEAR));
        }

    @Test
    void checkUnknownDeployFreqLevel()
        {
        String appId = "a1";
        when(mockdeploymentRepo.findByApplicationIdInAndCreatedBetweenOrderByCreated
            (Collections.singletonList(appId),
                dateOf(2019, 12, 12, 0, 0, 0),
                dateOf(2020, 3, 11, 0, 0, 0)))
            .thenReturn(Collections.emptyList());
        when(mockHierarchyClient.findApplicationChildIds(appId)).thenReturn(Collections.singletonList(appId));
        DeploymentFrequency freq = deploymentService.calculateDeployFreq(appId, dateOf(2020, 3, 10, 0, 0, 0));
        assertThat(freq.getDeployFreqLevel(), equalTo(DORALevel.UNKNOWN));
        assertThat(freq.getDeploymentCount(), equalTo(0));
        assertThat(freq.getTimePeriod(), equalTo(TimePeriod.YEAR));
        }

    @Test
    void checkEliteLeadTimeLevel()
        {
        String appId = "a1";
        Deployment d1 =  setupDeployment(appId,1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        Deployment d2 =  setupDeployment(appId, 1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        d1.getChanges().forEach(change -> {change.setLeadTimeSeconds(100);});
        d2.getChanges().forEach(change -> {change.setLeadTimeSeconds(100);});
        List<Deployment> deploys = new ArrayList<>();
        deploys.add(d1);
        deploys.add(d2);
        when(mockdeploymentRepo.findByApplicationIdInAndCreatedBetweenOrderByCreated
            (Collections.singletonList(appId),
                dateOf(2019, 12, 12, 0, 0, 0),
                dateOf(2020, 3, 11, 0, 0, 0)))
            .thenReturn(deploys);
        when(mockHierarchyClient.findApplicationChildIds(appId)).thenReturn(Collections.singletonList(appId));
        LeadTime leadTime = deploymentService.calculateLeadTime(appId, dateOf(2020, 3, 10, 0, 0, 0));
        assertThat(leadTime.getLeadTimePerfLevel(), equalTo(DORALevel.ELITE));
        assertThat(leadTime.getLeadTimeSeconds(), equalTo(100L));
        }

    @Test
    void checkHighLeadTimeLevel()
        {
        String appId = "a1";
        Deployment d1 =  setupDeployment(appId, 1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        Deployment d2 =  setupDeployment(appId, 1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        d1.getChanges().forEach(change -> {change.setLeadTimeSeconds(DORALevel.DAY);});
        d2.getChanges().forEach(change -> {change.setLeadTimeSeconds(DORALevel.DAY);});
        List<Deployment> deploys = new ArrayList<>();
        deploys.add(d1);
        deploys.add(d2);
        when(mockdeploymentRepo.findByApplicationIdInAndCreatedBetweenOrderByCreated
            (Collections.singletonList(appId),
                dateOf(2019, 12, 12, 0, 0, 0),
                dateOf(2020, 3, 11, 0, 0, 0)))
            .thenReturn(deploys);
        when(mockHierarchyClient.findApplicationChildIds(appId)).thenReturn(Collections.singletonList(appId));
        LeadTime leadTime = deploymentService.calculateLeadTime(appId, dateOf(2020, 3, 10, 0, 0, 0));
        assertThat(leadTime.getLeadTimePerfLevel(), equalTo(DORALevel.HIGH));
        assertThat(leadTime.getLeadTimeSeconds(), equalTo(DORALevel.DAY));
        }

    @Test
    void checkMedLeadTimeLevel()
        {
        String appId = "a1";
        Deployment d1 =  setupDeployment(appId,1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        Deployment d2 =  setupDeployment(appId,1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        d1.getChanges().forEach(change -> {change.setLeadTimeSeconds(DORALevel.WEEK);});
        d2.getChanges().forEach(change -> {change.setLeadTimeSeconds(DORALevel.WEEK);});
        List<Deployment> deploys = new ArrayList<>();
        deploys.add(d1);
        deploys.add(d2);
        when(mockdeploymentRepo.findByApplicationIdInAndCreatedBetweenOrderByCreated
            (Collections.singletonList(appId),
                dateOf(2019, 12, 12, 0, 0, 0),
                dateOf(2020, 3, 11, 0, 0, 0)))
            .thenReturn(deploys);
        when(mockHierarchyClient.findApplicationChildIds(appId)).thenReturn(Collections.singletonList(appId));
        LeadTime leadTime = deploymentService.calculateLeadTime(appId, dateOf(2020, 3, 10, 0, 0, 0));
        assertThat(leadTime.getLeadTimePerfLevel(), equalTo(DORALevel.MEDIUM));
        assertThat(leadTime.getLeadTimeSeconds(), equalTo(DORALevel.WEEK));
        }

    @Test
    void checkLowLeadTimeLevel()
        {
        String appId = "a1";
        Deployment d1 =  setupDeployment(appId,1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        Deployment d2 =  setupDeployment(appId,1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        d1.getChanges().forEach(change -> {change.setLeadTimeSeconds(DORALevel.MONTH);});
        d2.getChanges().forEach(change -> {change.setLeadTimeSeconds(DORALevel.MONTH);});
        List<Deployment> deploys = new ArrayList<>();
        deploys.add(d1);
        deploys.add(d2);
        when(mockdeploymentRepo.findByApplicationIdInAndCreatedBetweenOrderByCreated
            (Collections.singletonList(appId),
                dateOf(2019, 12, 12, 0, 0, 0),
                dateOf(2020, 3, 11, 0, 0, 0)))
            .thenReturn(deploys);
        when(mockHierarchyClient.findApplicationChildIds(appId)).thenReturn(Collections.singletonList(appId));
        LeadTime leadTime = deploymentService.calculateLeadTime(appId, dateOf(2020, 3, 10, 0, 0, 0));
        assertThat(leadTime.getLeadTimePerfLevel(), equalTo(DORALevel.LOW));
        assertThat(leadTime.getLeadTimeSeconds(), equalTo(DORALevel.MONTH));
        }

    @Test
    void checkUnknownLeadTimeLevel()
        {
        String appId = "a1";
        when(mockdeploymentRepo.findByApplicationIdInAndCreatedBetweenOrderByCreated
            (Collections.singletonList(appId),
                dateOf(2019, 12, 12, 0, 0, 0),
                dateOf(2020, 3, 11, 0, 0, 0)))
            .thenReturn(Collections.emptyList());
        when(mockHierarchyClient.findApplicationChildIds(appId)).thenReturn(Collections.singletonList(appId));
        LeadTime leadTime = deploymentService.calculateLeadTime(appId, dateOf(2020, 3, 10, 0, 0, 0));
        assertThat(leadTime.getLeadTimePerfLevel(), equalTo(DORALevel.UNKNOWN));
        assertThat(leadTime.getLeadTimeSeconds(), equalTo(0L));
        }

    @Test
    void checkListAll()
        {
        String appId = "a1";
        Deployment d1 =  setupDeployment(appId,1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        Deployment d2 =  setupDeployment(appId,1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        d1.getChanges().forEach(change -> {change.setLeadTimeSeconds(DORALevel.MONTH);});
        d2.getChanges().forEach(change -> {change.setLeadTimeSeconds(DORALevel.MONTH);});
        List<Deployment> deploys = new ArrayList<>();
        deploys.add(d1);
        deploys.add(d2);
        when(mockdeploymentRepo.findByApplicationId(appId))
            .thenReturn(deploys);
        
        List<Deployment> deployList = deploymentService.listAllForApplication(appId);
        
        assertThat(deployList.size(), equalTo(2));
        }

    @Test
    void checkListHierarchy()
        {
        Deployment d1 =  setupDeployment("a1",1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        Deployment d2 =  setupDeployment("a2",1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        d1.getChanges().forEach(change -> {change.setLeadTimeSeconds(DORALevel.MONTH);});
        d2.getChanges().forEach(change -> {change.setLeadTimeSeconds(DORALevel.MONTH);});
        List<Deployment> deploys = new ArrayList<>();
        deploys.add(d1);
        deploys.add(d2);
        when(mockHierarchyClient.findChildIds("a1")).thenReturn(Arrays.asList("a1", "a2"));
        when(mockdeploymentRepo.findByApplicationIdInOrderByCreatedDesc(anyCollection()))
            .thenReturn(deploys);

        List<Deployment> deployList = deploymentService.listAllForHierarchy("a1");

        assertThat(deployList.size(), equalTo(2));
        }

    @Test
    void checkListAllWithDate()
        {
        String appId = "a1";
        Deployment d1 =  setupDeployment(appId,1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        Deployment d2 =  setupDeployment(appId,1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        d1.getChanges().forEach(change -> {change.setLeadTimeSeconds(DORALevel.MONTH);});
        d2.getChanges().forEach(change -> {change.setLeadTimeSeconds(DORALevel.MONTH);});
        List<Deployment> deploys = new ArrayList<>();
        deploys.add(d1);
        deploys.add(d2);
        when(mockdeploymentRepo.findByApplicationIdAndCreatedBetweenOrderByCreated
            (appId,
                dateOf(2020, 3, 10, 0, 0, 0),
                dateOf(2020, 3, 11, 0, 0, 0)))
            .thenReturn(deploys);
        List<Deployment> deployList = deploymentService.listAllForApplication(appId, dateOf(2020, 3, 10, 0, 0, 0));

        assertThat(deployList.size(), equalTo(2));
        }
    
    @Test
    void checkDelete()
        {
        String appId = "a1";
        Deployment d1 =  setupDeployment(appId, 1, 10, 10, 3, 10, 10, 3, 10, 10, 3, 10);
        when(mockdeploymentRepo.findById("id123"))
            .thenReturn(Optional.of(d1));
        String id = deploymentService.delete("id123");
        assertThat(id, is(equalTo("id123")));
        verify(mockdeploymentRepo, times(1)).findById("id123");
        verify(mockdeploymentRepo, times(1)).delete(d1);
        }
    }