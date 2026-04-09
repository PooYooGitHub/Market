package org.shyu.marketserviceuser.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户角色分配请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class UserRoleAssignRequest {

    /**
     * 用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    /**
     * 角色ID列表
     */
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;

    /**
     * 操作类型：assign, remove, replace
     */
    @NotNull(message = "操作类型不能为空")
    private String operationType;

    /**
     * 操作原因
     */
    private String reason;
}