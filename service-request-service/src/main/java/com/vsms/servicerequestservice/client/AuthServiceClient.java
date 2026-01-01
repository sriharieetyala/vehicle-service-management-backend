package com.vsms.servicerequestservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    // Update technician workload (INCREMENT or DECREMENT)
    @PutMapping("/api/technicians/{id}/workload")
    void updateWorkload(@PathVariable Integer id, @RequestParam String action);
}
