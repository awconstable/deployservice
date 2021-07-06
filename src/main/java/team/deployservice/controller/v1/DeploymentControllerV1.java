package team.deployservice.controller.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import team.deployservice.model.Deployment;
import team.deployservice.model.DeploymentFrequency;
import team.deployservice.model.LeadTime;
import team.deployservice.service.DeploymentService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
@Api
public class DeploymentControllerV1
    {

    private static final Logger log = LoggerFactory.getLogger(DeploymentControllerV1.class);
    
    private final DeploymentService deploymentService;

    @Autowired
    public DeploymentControllerV1(DeploymentService deploymentService)
        {
        this.deploymentService = deploymentService;
        }

        @PostMapping("/deployment")
        @ResponseStatus(HttpStatus.CREATED)
        @ApiOperation(value = "Store a deployment", notes = "Store a single deployment", response = Deployment.class)
        public Deployment store(@Valid @RequestBody Deployment deployment){
            log.info("Store deployment with id {}", deployment.getDeploymentId());
            return deploymentService.store(deployment);
        }

        @GetMapping("/deployment")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "List all deployments", notes = "List all deployments without filtering", response = Deployment.class, responseContainer = "List")
        public List<Deployment> list(){
            log.info("List all deployments");
            return deploymentService.list();
        }

        @GetMapping("/deployment/{id}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Get a specific deployment specified by it's id", response = Deployment.class)
        public Optional<Deployment> show(@PathVariable @ApiParam(value = "The deployment id", required = true) String id){
            log.info("Show deployment with id {}", id);
            return deploymentService.get(id);
        }
    
        @DeleteMapping("/deployment/{id}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Delete a specific deployment specified by it's id", response = Deployment.class)
        public String delete(@PathVariable @ApiParam(value = "The deployment id", required = true) String id) {
            log.info("Delete deployment with id {}", id);
            return deploymentService.delete(id); 
        }

        @GetMapping("/deployment/application/{id}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "List all deployments for an application", notes = "List all deployments filtered by application id", response = Deployment.class, responseContainer = "List")
        public List<Deployment> listForApp(@PathVariable @ApiParam(value = "The application id", required = true) String id){
            log.info("List deployments for application with id {}", id);
            return deploymentService.listAllForApplication(id);
        }

        @GetMapping("/deployment/hierarchy/{id}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "List all deployments for a hierarchy", notes = "List all deployments in a hierarchy starting at node with application id", response = Deployment.class, responseContainer = "List")
        public List<Deployment> listForHierarchy(@PathVariable @ApiParam(value = "The application id", required = true) String id){
            log.info("List all deployments for a team hierarchy starting at application with id {}", id);
            return deploymentService.listAllForHierarchy(id);
        }

        @GetMapping("/deployment/application/{id}/date/{date}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "List all deployments for an application and date combination", notes = "List all deployments filtered by application id and date", response = Deployment.class, responseContainer = "List")
        public List<Deployment> listForAppAndDate(@PathVariable @ApiParam(value = "The application id", required = true) String id, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "The deployment date in ISO Date format YYYY-MM-dd", required = true) LocalDate date){
            ZonedDateTime reportingDate = date.atStartOfDay(ZoneOffset.UTC);
            Date rDate = Date.from(reportingDate.toInstant());
            log.info("List all deployments with application id {} for date {}", id, DateTimeFormatter.ISO_LOCAL_DATE.format(reportingDate));
            return deploymentService.listAllForApplication(id, rDate);
        }

        @GetMapping("/deployment/application/{id}/frequency")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Calculate deployment frequency for an application for the last 90 days", response = DeploymentFrequency.class)
        public DeploymentFrequency calculateDeployFreq(@PathVariable @ApiParam(value = "The application id", required = true) String id){
            ZonedDateTime date = LocalDate.now().minusDays(1).atStartOfDay(ZoneOffset.UTC);        
            Date reportingDate = Date.from(date.toInstant());
            log.info("Calculate deployment frequency for application id {} from date {}", id, DateTimeFormatter.ISO_LOCAL_DATE.format(date));
            return deploymentService.calculateDeployFreq(id, reportingDate);
        }
    
        @GetMapping("/deployment/application/{id}/frequency/{date}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Calculate deployment frequency for an application for the date specified", response = DeploymentFrequency.class)
        public DeploymentFrequency calculateDeployFreq(@PathVariable @ApiParam(value = "The application id", required = true) String id, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "The deployment date in ISO Date format YYYY-MM-dd", required = true) LocalDate date){
            ZonedDateTime reportingDate = date.atStartOfDay(ZoneOffset.UTC);        
            Date rDate = Date.from(reportingDate.toInstant());
            log.info("Calculate deployment frequency for application id {} from date {}", id, DateTimeFormatter.ISO_LOCAL_DATE.format(reportingDate));
            return deploymentService.calculateDeployFreq(id, rDate);
        }

        @GetMapping("/deployment/application/{id}/lead_time")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Calculate lead time for an application", response = LeadTime.class)
        public LeadTime calculateLeadTime(@PathVariable @ApiParam(value = "The application id", required = true) String id){
            ZonedDateTime date = LocalDate.now().minusDays(1).atStartOfDay(ZoneOffset.UTC);
            Date reportingDate = Date.from(date.toInstant());
            log.info("Calculate lead time for application id {} from date {}", id, DateTimeFormatter.ISO_LOCAL_DATE.format(date));
            return deploymentService.calculateLeadTime(id, reportingDate);
        }
    
        @GetMapping("/deployment/application/{id}/lead_time/{date}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Calculate lead time for an application for the date specified", response = LeadTime.class)
        public LeadTime calculateLeadTime(@PathVariable @ApiParam(value = "The application id", required = true) String id, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "The deployment date in ISO Date format YYYY-MM-dd", required = true) LocalDate date){
            ZonedDateTime reportingDate = date.atStartOfDay(ZoneOffset.UTC);    
            Date rDate = Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
            log.info("Calculate lead time for application id {} from date {}", id, DateTimeFormatter.ISO_LOCAL_DATE.format(reportingDate));
            return deploymentService.calculateLeadTime(id, rDate);
        }
    }
