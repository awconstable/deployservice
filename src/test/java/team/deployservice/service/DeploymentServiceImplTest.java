package team.deployservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import team.deployservice.model.Change;
import team.deployservice.model.DORALevel;
import team.deployservice.model.Deployment;
import team.deployservice.repo.DeploymentRepo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class DeploymentServiceImplTest
    {
    @Autowired
    private DeploymentService deploymentService;
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

    private Deployment d1;
    private Change c1;
    private Change c2;
    private Change c3;
    
    private void setup(int c1M, int c1D, int c1H, int c2M, int c2D, int c2H, int c3M, int c3D, int c3H)
        {
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
        d1 = new Deployment(
            "d1",
            "deployment d1",
            "a1",
            "c1",
            dateOf(2020, 3, 10, 10, 0, 0),
            "source", changes);
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
        setup(3, 3, 10, 3, 4, 10, 3, 5, 10);
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
        setup(3, 10, 10, 3, 10, 11, 3, 10, 12);
        when(mockdeploymentRepo.save(d1)).thenReturn(d1);
        Deployment storedDep = deploymentService.store(d1);
        assertThat(storedDep.getLeadTimePerfLevel(), equalTo(DORALevel.ELITE));
        }
    
    @Test
    void checkHighLeadTimePerfLevel()
        {
            setup(3, 3, 10, 3, 4, 10, 3, 5, 10);
            when(mockdeploymentRepo.save(d1)).thenReturn(d1);
            Deployment storedDep = deploymentService.store(d1);
            assertThat(storedDep.getLeadTimePerfLevel(), equalTo(DORALevel.HIGH));
        }
    
    @Test
    void checkMedLeadTimePerfLevel()
        {
        setup(2, 20, 10, 2, 29, 10, 3, 5, 10);
        when(mockdeploymentRepo.save(d1)).thenReturn(d1);
        Deployment storedDep = deploymentService.store(d1);
        assertThat(storedDep.getLeadTimePerfLevel(), equalTo(DORALevel.MEDIUM));
        }

    @Test
    void checkLowLeadTimePerfLevel()
        {
        setup(1, 10, 10, 2, 29, 10, 2, 5, 10);
        when(mockdeploymentRepo.save(d1)).thenReturn(d1);
        Deployment storedDep = deploymentService.store(d1);
        assertThat(storedDep.getLeadTimePerfLevel(), equalTo(DORALevel.LOW));
        }
    }