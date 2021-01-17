package team.deployservice.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import team.deployservice.model.Deployment;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentRepo extends MongoRepository<Deployment, String>
    {
        Optional<Deployment> findByDeploymentId(String deploymentId);
        
        List<Deployment> findByApplicationId(String applicationId);
    }
