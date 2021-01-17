package team.deployservice.service;

import team.deployservice.model.Deployment;

import java.util.List;
import java.util.Optional;

public interface DeploymentService
    {
        Deployment store(Deployment deployment);

        Optional<Deployment> get(String id);

        List<Deployment> list();
        
        List<Deployment> listAllForApplication(String applicationId);
    }
