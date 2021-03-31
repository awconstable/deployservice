package team.deployservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.deployservice.model.*;
import team.deployservice.repo.DeploymentRepo;
import team.deployservice.hierarchy.repo.HierarchyClient;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class DeploymentServiceImpl implements DeploymentService
    {

    private final DeploymentRepo deploymentRepo;
    private final HierarchyClient hierarchyClient;

    @Autowired
    public DeploymentServiceImpl(DeploymentRepo deploymentRepo, HierarchyClient hierarchyClient)
        {
        this.deploymentRepo = deploymentRepo;
        this.hierarchyClient = hierarchyClient;
        }

    private static long findAverageUsingStream(Long[] array) {
        return Math.round(Arrays.stream(array).mapToLong(Long::longValue).average().orElse(Double.NaN));
    }
    
    private DORALevel findDORAPerfLevel(long leadTimeSecs){
        if(leadTimeSecs == 0){
            return DORALevel.LOW;
        }
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
    public String delete(String id)
        {
        Optional<Deployment> deployment = get(id);
        deployment.ifPresent(deploymentRepo::delete);
        return id;
        }

    @Override
    public List<Deployment> listAllForApplication(String applicationId)
        {
        return deploymentRepo.findByApplicationId(applicationId);
        }

    @Override
    public List<Deployment> listAllForApplication(String applicationId, Date reportingDate)
        {
        return deploymentRepo.findByApplicationIdAndCreatedBetweenOrderByCreated(applicationId, getStartDate(reportingDate, 0), getEndDate(reportingDate));
        }

    private Date getStartDate(Date reportingDate, Integer minusDays){
        ZonedDateTime startDate = ZonedDateTime.ofInstant(reportingDate.toInstant(), ZoneOffset.UTC).minusDays(minusDays);
        return Date.from(startDate.toInstant());
    }
    
    private Date getEndDate(Date reportingDate){
        return Date.from(ZonedDateTime.ofInstant(reportingDate.toInstant(), ZoneOffset.UTC).plusDays(1).toInstant());
    }
    
    @Override
    public DeploymentFrequency calculateDeployFreq(String applicationId, Date reportingDate)
        {
        Collection<String> appIds = hierarchyClient.findApplicationChildIds(applicationId);
        Date endDate = getEndDate(reportingDate);
        //check for elite performance
        List<Deployment> eliteDeploys = deploymentRepo
            .findByApplicationIdInAndCreatedBetweenOrderByCreated(appIds, getStartDate(reportingDate, 0), endDate);
        if(eliteDeploys.size() > 1){
            return new DeploymentFrequency(applicationId, reportingDate, eliteDeploys.size(), TimePeriod.DAY, DORALevel.ELITE);
        }
        //check for high performance
        List<Deployment> highDeploys = deploymentRepo
            .findByApplicationIdInAndCreatedBetweenOrderByCreated(appIds, getStartDate(reportingDate, 6), endDate);
        if(highDeploys.size() >= 1){
            return new DeploymentFrequency(applicationId, reportingDate, highDeploys.size(), TimePeriod.WEEK, DORALevel.HIGH);
        }
        //check for medium performance
        List<Deployment> medDeploys = deploymentRepo
            .findByApplicationIdInAndCreatedBetweenOrderByCreated(appIds, getStartDate(reportingDate, 29), endDate);
        if(medDeploys.size() >= 1){
            return new DeploymentFrequency(applicationId, reportingDate, medDeploys.size(), TimePeriod.MONTH, DORALevel.MEDIUM);
        }
        //check for low performance
        List<Deployment> lowDeploys = deploymentRepo
            .findByApplicationIdInAndCreatedBetweenOrderByCreated(appIds, getStartDate(reportingDate, 364), endDate);
        if(lowDeploys.size() >= 1) {
            return new DeploymentFrequency(applicationId, reportingDate, lowDeploys.size(), TimePeriod.YEAR, DORALevel.LOW);
        }
        //No data, return unknown performance level    
        return new DeploymentFrequency(applicationId, reportingDate, 0, TimePeriod.YEAR, DORALevel.UNKNOWN);
        }

    @Override
    public LeadTime calculateLeadTime(String applicationId, Date reportingDate)
        {
        Collection<String> appIds = hierarchyClient.findApplicationChildIds(applicationId);
        Date startDate = getStartDate(reportingDate, 89);
        Date endDate = getEndDate(reportingDate);
        List<Deployment> deploys = deploymentRepo
            .findByApplicationIdInAndCreatedBetweenOrderByCreated(appIds, startDate, endDate);
        //No data, return unknown performance level
        if(deploys.size() == 0){
            return new LeadTime(applicationId, reportingDate, 0, DORALevel.UNKNOWN);
        }
        ArrayList<Long> cLeadTimes = new ArrayList<>();
        deploys.forEach(
            deployment -> deployment.getChanges().forEach(
                c -> cLeadTimes.add(c.getLeadTimeSeconds())
            )
        );
        long leadTimeSecs = findAverageUsingStream(cLeadTimes.toArray(new Long[0]));
        DORALevel leadTimePerfLevel = findDORAPerfLevel(leadTimeSecs);
        return new LeadTime(applicationId, reportingDate, leadTimeSecs, leadTimePerfLevel);
        }
    }
