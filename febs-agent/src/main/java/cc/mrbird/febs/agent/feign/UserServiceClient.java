package cc.mrbird.febs.agent.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 用户服务Feign客户端
 * 用于调用FEBS-Server-System的用户相关API
 * 
 * @author mrbird
 */
@FeignClient(name = "FEBS-Server-System", path = "/user")
public interface UserServiceClient {
    
    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    Map<String, Object> getUserList(@RequestParam(required = false) String username,
                                    @RequestParam(required = false) Integer deptId,
                                    @RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size);
    
    /**
     * 获取用户详情
     */
    @GetMapping("/user")
    Map<String, Object> getUserById(@RequestParam Long userId);
}