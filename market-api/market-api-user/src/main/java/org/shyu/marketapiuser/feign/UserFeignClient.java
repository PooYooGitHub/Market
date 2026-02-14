package org.shyu.marketapiuser.feign;
import org.shyu.marketapiuser.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
@FeignClient(name = "market-service-core", path = "/api/user")
public interface UserFeignClient {
    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
    @GetMapping("/username")
    UserDTO getUserByUsername(@RequestParam("username") String username);
    @GetMapping("/phone")
    UserDTO getUserByPhone(@RequestParam("phone") String phone);
}
