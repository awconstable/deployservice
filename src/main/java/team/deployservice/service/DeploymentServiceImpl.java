package team.deployservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.deployservice.model.Deployment;
import team.deployservice.repo.DeploymentRepo;

import java.util.List;
import java.util.Optional;

@Service
public class DeploymentServiceImpl implements DeploymentService
    {

    private final DeploymentRepo deploymentRepo;

    @Autowired
    public DeploymentServiceImpl(DeploymentRepo deploymentRepo)
        {
        this.deploymentRepo = deploymentRepo;
        }
    
    @Override
    public Deployment store(Deployment deployment)
        {
        return deploymentRepo.save(deployment);
        }

    @Override
    public Optional<Deployment> get(String id)
        {
        return deploymentRepo.findById(id);
        }

    @Override
    public List<Deployment> list()
        {
        return deploymentRepo.findAll();
        }
    }
