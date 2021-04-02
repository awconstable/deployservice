package team.deployservice.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import team.deployservice.model.Deployment;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentRepo extends MongoRepository<Deployment, String>
    {
        Optional<Deployment> findByDeploymentId(String deploymentId);
        
        List<Deployment> findByApplicationId(String applicationId);

        List<Deployment> findByApplicationIdInOrderByCreatedDesc(Collection<String> applicationIds);

        List<Deployment> findByApplicationIdAndCreatedBetweenOrderByCreated(String applicationId, Date start, Date end);

        List<Deployment> findByApplicationIdInAndCreatedBetweenOrderByCreated(Collection<String> applicationIds, Date start, Date end);
    }
