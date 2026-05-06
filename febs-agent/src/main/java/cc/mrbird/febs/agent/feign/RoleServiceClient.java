package cc.mrbird.febs.agent.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 角色服务Feign客户端
 * 用于调用FEBS-Server-System的角色相关API
 * 
 * @author mrbird
 */
@FeignClient(name = "FEBS-Server-System", path = "/role")
public interface RoleServiceClient {
    
    /**
     * 获取角色列表
     */
    @GetMapping("/roles")
    Map<String, Object> getRoleList(@RequestParam(required = false) String roleName,
                                    @RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size);
    
    /**
     * 获取角色详情
     */
    @GetMapping("/role")
    Map<String, Object> getRoleById(@RequestParam Long roleId);
    
    /**
     * 获取用户角色
     */
    @GetMapping("/user/roles")
    Map<String, Object> getUserRoles(@RequestParam Long userId);
}