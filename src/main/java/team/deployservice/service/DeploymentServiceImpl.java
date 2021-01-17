package team.deployservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.deployservice.model.DORALevel;
import team.deployservice.model.Deployment;
import team.deployservice.repo.DeploymentRepo;

import java.util.ArrayList;
import java.util.Arrays;
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

    private static long findAverageUsingStream(Long[] array) {
        return Math.round(Arrays.stream(array).mapToLong(Long::longValue).average().orElse(Double.NaN));
    }
    
    private DORALevel findDORAPerfLevel(long leadTimeSecs){
        if(leadTimeSecs < DORALevel.DAY){
            return DORALevel.ELITE;
        } else if(leadTimeSecs < DORALevel.WEEK){
            return DORALevel.HIGH;
        } else if(leadTimeSecs < DORALevel.MONTH){
            return DORALevel.MEDIUM;
        } else {
            return DORALevel.LOW;
        }
    }
    
    @Override
    public Deployment store(Deployment deployment)
        {
        long deployTime = deployment.getCreated().toInstant().getEpochSecond();
        ArrayList<Long> cLeadTimes = new ArrayList<>();
        deployment.getChanges().forEach(
            c -> { 
            long leadTimeSeconds = deployTime - c.getCreated().toInstant().getEpochSecond();
            c.setLeadTimeSeconds(leadTimeSeconds);
            cLeadTimes.add(leadTimeSeconds);
            }
        );
        long leadTimeSecs = findAverageUsingStream(cLeadTimes.toArray(new Long[0]));
        deployment.setLeadTimeSeconds(leadTimeSecs);
        DORALevel leadTimePerfLevel = findDORAPerfLevel(leadTimeSecs);
        deployment.setLeadTimePerfLevel(leadTimePerfLevel);
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

    @Override
    public List<Deployment> listAllForApplication(String applicationId)
        {
        return deploymentRepo.findByApplicationId(applicationId);
        }
    }
