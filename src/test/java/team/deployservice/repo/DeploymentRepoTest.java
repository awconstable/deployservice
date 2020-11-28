package team.deployservice.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import team.deployservice.model.Change;
import team.deployservice.model.Deployment;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@ExtendWith(SpringExtension.class)
@DataMongoTest
class DeploymentRepoTest
    {
    
    @Autowired
    DeploymentRepo repo;
    
    @BeforeEach
    void setUp()
        {
        Change c1 = new Change("c123", Date.from(Instant.now()), "test", "test");
        Change c2 = new Change("c234", Date.from(Instant.now()), "test", "test");
        Deployment d1 = new Deployment("d123", "a1", "comp1", Date.from(Instant.now()), "test", new HashSet<>(Arrays.asList(c1, c2)));
        repo.save(d1);
        }

    @AfterEach
    void tearDown()
        {
        repo.deleteAll();
        }
    
    @Test
    public void getWithDeploymentId()
        {
            Optional<Deployment> d123 = repo.findByDeploymentId("d123");
            assert(d123.isPresent());
            assertThat(d123.get().getDeploymentId(), is(equalTo("d123")));
        }
    }