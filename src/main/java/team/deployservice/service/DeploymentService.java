package team.deployservice.service;

import team.deployservice.model.Deployment;
import team.deployservice.model.DeploymentFrequency;
import team.deployservice.model.LeadTime;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DeploymentService
    {
        Deployment store(Deployment deployment);

        Optional<Deployment> get(String id);

        List<Deployment> list();
        
        String delete(String id);
        
        List<Deployment> listAllForApplication(String applicationId);
        
        List<Deployment> listAllForHierarchy(String applicationId);

        List<Deployment> listAllForApplication(String applicationId, Date reportingDate);
        
        DeploymentFrequency calculateDeployFreq(String applicationId, Date reportingDate);
        
        LeadTime calculateLeadTime(String applicationId, Date reportingDate);
    }
