package team.deployservice.controller.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import team.deployservice.model.Deployment;
import team.deployservice.model.DeploymentFrequency;
import team.deployservice.service.DeploymentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
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
        public Deployment store(@RequestBody Deployment deployment){
            System.out.println(deployment);
            return deploymentService.store(deployment);
        }

        @GetMapping("/deployment")
        @ResponseStatus(HttpStatus.OK)
        public List<Deployment> list(){
            return deploymentService.list();
        }

        @GetMapping("/deployment/{id}")
        @ResponseStatus(HttpStatus.OK)
        public Optional<Deployment> show(@PathVariable String id){
            return deploymentService.get(id);
        }

        @GetMapping("/deployment/application/{id}")
        @ResponseStatus(HttpStatus.OK)
        public List<Deployment> listForApp(@PathVariable String id){
            return deploymentService.listAllForApplication(id);
        }

        @GetMapping("/deployment/application/{id}/frequency")
        @ResponseStatus(HttpStatus.OK)
        public DeploymentFrequency calculateDeployFreq(@PathVariable String id){
            LocalDateTime date = LocalDate.now().minusDays(1).atStartOfDay();        
            Date reportingDate = Date.from(date.toInstant(ZoneOffset.UTC));
            return deploymentService.calculateDeployFreq(id, reportingDate);
        }
    
        @GetMapping("/deployment/application/{id}/frequency/{date}")
        @ResponseStatus(HttpStatus.OK)
        public DeploymentFrequency calculateDeployFreq(@PathVariable String id, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date){
            return deploymentService.calculateDeployFreq(id, date);
        }
    }
