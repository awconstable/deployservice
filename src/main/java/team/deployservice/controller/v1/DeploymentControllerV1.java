package team.deployservice.controller.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
@Api
public class DeploymentControllerV1
    {
    
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
            return deploymentService.store(deployment);
        }

        @GetMapping("/deployment")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "List all deployments", notes = "List all deployments without filtering", response = Deployment.class, responseContainer = "List")
        public List<Deployment> list(){
            return deploymentService.list();
        }

        @GetMapping("/deployment/{id}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Get a specific deployment specified by it's id", response = Deployment.class)
        public Optional<Deployment> show(@PathVariable @ApiParam(value = "The deployment id", required = true) String id){
            return deploymentService.get(id);
        }
    
        @DeleteMapping("/deployment/{id}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Delete a specific deployment specified by it's id", response = Deployment.class)
        public String delete(@PathVariable @ApiParam(value = "The deployment id", required = true) String id) { return deploymentService.delete(id); }

        @GetMapping("/deployment/application/{id}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "List all deployments for an application", notes = "List all deployments filtered by application id", response = Deployment.class, responseContainer = "List")
        public List<Deployment> listForApp(@PathVariable @ApiParam(value = "The application id", required = true) String id){
            return deploymentService.listAllForApplication(id);
        }

        @GetMapping("/deployment/hierarchy/{id}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "List all deployments for a hierarchy", notes = "List all deployments in a hierarchy starting at node with application id", response = Deployment.class, responseContainer = "List")
        public List<Deployment> listForHierarchy(@PathVariable @ApiParam(value = "The application id", required = true) String id){
            return deploymentService.listAllForHierarchy(id);
        }

        @GetMapping("/deployment/application/{id}/date/{date}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "List all deployments for an application and date combination", notes = "List all deployments filtered by application id and date", response = Deployment.class, responseContainer = "List")
        public List<Deployment> listForAppAndDate(@PathVariable @ApiParam(value = "The application id", required = true) String id, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "The deployment date in ISO Date format YYYY-MM-dd", required = true) LocalDate date){
            Date reportingDate = Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
            return deploymentService.listAllForApplication(id, reportingDate);
        }

        @GetMapping("/deployment/application/{id}/frequency")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Calculate deployment frequency for an application for the last 90 days", response = DeploymentFrequency.class)
        public DeploymentFrequency calculateDeployFreq(@PathVariable @ApiParam(value = "The application id", required = true) String id){
            ZonedDateTime date = LocalDate.now().minusDays(1).atStartOfDay(ZoneOffset.UTC);        
            Date reportingDate = Date.from(date.toInstant());
            return deploymentService.calculateDeployFreq(id, reportingDate);
        }
    
        @GetMapping("/deployment/application/{id}/frequency/{date}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Calculate deployment frequency for an application for the date specified", response = DeploymentFrequency.class)
        public DeploymentFrequency calculateDeployFreq(@PathVariable @ApiParam(value = "The application id", required = true) String id, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "The deployment date in ISO Date format YYYY-MM-dd", required = true) LocalDate date){
            Date reportingDate = Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
            return deploymentService.calculateDeployFreq(id, reportingDate);
        }

        @GetMapping("/deployment/application/{id}/lead_time")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Calculate lead time for an application", response = LeadTime.class)
        public LeadTime calculateLeadTime(@PathVariable @ApiParam(value = "The application id", required = true) String id){
            ZonedDateTime date = LocalDate.now().minusDays(1).atStartOfDay(ZoneOffset.UTC);
            Date reportingDate = Date.from(date.toInstant());
            return deploymentService.calculateLeadTime(id, reportingDate);
        }
    
        @GetMapping("/deployment/application/{id}/lead_time/{date}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Calculate lead time for an application for the date specified", response = LeadTime.class)
        public LeadTime calculateLeadTime(@PathVariable @ApiParam(value = "The application id", required = true) String id, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "The deployment date in ISO Date format YYYY-MM-dd", required = true) LocalDate date){
            Date reportingDate = Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
            return deploymentService.calculateLeadTime(id, reportingDate);
        }
    }
