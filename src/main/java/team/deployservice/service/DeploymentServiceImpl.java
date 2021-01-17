package team.deployservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.deployservice.model.DORALevel;
import team.deployservice.model.Deployment;
import team.deployservice.model.DeploymentFrequency;
import team.deployservice.model.TimePeriod;
import team.deployservice.repo.DeploymentRepo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

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

    private Date getStartDate(Date reportingDate, Integer minusDays){
        LocalDateTime startDate = LocalDateTime.ofInstant(reportingDate.toInstant(), ZoneOffset.UTC).minusDays(minusDays);
        return Date.from(startDate.toInstant(ZoneOffset.UTC));
    }
    
    @Override
    public DeploymentFrequency calculateDeployFreq(String applicationId, Date reportingDate)
        {
        Date endDate = Date.from(LocalDateTime.ofInstant(reportingDate.toInstant(), ZoneOffset.UTC).plusDays(1).toInstant(ZoneOffset.UTC));
        //check for elite performance
        List<Deployment> eliteDeploys = deploymentRepo
            .findByApplicationIdAndCreatedBetweenOrderByCreated(applicationId, getStartDate(reportingDate, 1), endDate);
        if(eliteDeploys.size() > 1){
            return new DeploymentFrequency(applicationId, reportingDate, eliteDeploys.size(), TimePeriod.DAY, DORALevel.ELITE);
        }
        //check for high performance
        List<Deployment> highDeploys = deploymentRepo
            .findByApplicationIdAndCreatedBetweenOrderByCreated(applicationId, getStartDate(reportingDate, 7), endDate);
        if(highDeploys.size() >= 1){
            return new DeploymentFrequency(applicationId, reportingDate, highDeploys.size(), TimePeriod.WEEK, DORALevel.HIGH);
        }
        //check for medium performance
        List<Deployment> medDeploys = deploymentRepo
            .findByApplicationIdAndCreatedBetweenOrderByCreated(applicationId, getStartDate(reportingDate, 30), endDate);
        if(medDeploys.size() >= 1){
            return new DeploymentFrequency(applicationId, reportingDate, medDeploys.size(), TimePeriod.MONTH, DORALevel.MEDIUM);
        }
        //check for low performance
        List<Deployment> lowDeploys = deploymentRepo
            .findByApplicationIdAndCreatedBetweenOrderByCreated(applicationId, getStartDate(reportingDate, 365), endDate);
        return new DeploymentFrequency(applicationId, reportingDate, lowDeploys.size(), TimePeriod.YEAR, DORALevel.LOW);
        }
    }
