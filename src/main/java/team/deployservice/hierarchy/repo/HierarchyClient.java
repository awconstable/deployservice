package team.deployservice.hierarchy.repo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collection;

@FeignClient(value = "team-service")
public interface HierarchyClient
    {
        @RequestMapping(method = RequestMethod.GET, value = "/v2/hierarchy/children/application/ids/{slug}")
        Collection<String> findApplicationChildIds(@PathVariable("slug") String slug);
    }
