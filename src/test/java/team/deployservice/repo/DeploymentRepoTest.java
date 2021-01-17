package team.deployservice.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import team.deployservice.MongoDBContainerTest;
import team.deployservice.model.Change;
import team.deployservice.model.Deployment;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class DeploymentRepoTest extends MongoDBContainerTest
    {
    
    @Autowired
    DeploymentRepo repo;
    
    @BeforeEach
    void setUp()
        {
        Change c1 = new Change("c1", Date.from(Instant.now()), "test", "test");
        Change c2 = new Change("c2", Date.from(Instant.now()), "test", "test");
        Deployment d1 = new Deployment("d1", "deployment v1", "a1", "comp1", Date.from(Instant.now()), "test", new HashSet<>(Arrays.asList(c1, c2)));
        repo.save(d1);
        Deployment d2 = new Deployment("d2", "deployment v2", "a1", "comp1", Date.from(Instant.now()), "test", new HashSet<>(Arrays.asList(c1, c2)));
        repo.save(d2);
        Deployment d3 = new Deployment("d3", "deployment v3", "a2", "comp1", Date.from(Instant.now()), "test", new HashSet<>(Arrays.asList(c1, c2)));
        repo.save(d3);
        }

    @AfterEach
    void tearDown()
        {
        repo.deleteAll();
        }
    
    @Test
    public void getWithDeploymentId()
        {
            Optional<Deployment> d123 = repo.findByDeploymentId("d1");
            assert(d123.isPresent());
            assertThat(d123.get().getDeploymentId(), is(equalTo("d1")));
        }

    @Test
    public void getAllWithApplicationId()
        {
        List<Deployment> deploys = repo.findByApplicationId("a1");
        assertThat(deploys.size(), is(equalTo(2)));
        }
    }