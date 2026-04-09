package org.shyu.marketserviceuser.service;

import org.shyu.marketserviceuser.dto.NotificationRequest;
import org.shyu.marketserviceuser.dto.SystemConfigRequest;
import org.shyu.marketserviceuser.vo.PlatformOverviewVO;
import org.shyu.marketserviceuser.vo.SystemStatisticsVO;

import java.util.List;
import java.util.Map;

/**
 * 系统管理服务接口
 */
public interface SystemManagementService {

    /**
     * 获取平台总览数据
     */
    PlatformOverviewVO getPlatformOverview();

    /**
     * 获取系统统计数据
     */
    SystemStatisticsVO getSystemStatistics();

    /**
     * 获取系统配置
     */
    Map<String, Object> getSystemConfigs();

    /**
     * 更新系统配置
     */
    void updateSystemConfig(SystemConfigRequest request);

    /**
     * 批量更新系统配置
     */
    void batchUpdateSystemConfigs(List<SystemConfigRequest> requests);

    /**
     * 发送系统通知
     */
    void sendSystemNotification(NotificationRequest request);

    /**
     * 获取系统日志
     */
    Map<String, Object> getSystemLogs(Integer pageNum, Integer pageSize, String level, String startDate, String endDate);

    /**
     * 清理系统缓存
     */
    void clearSystemCache(String cacheType);

    /**
     * 获取系统健康状态
     */
    Map<String, Object> getSystemHealth();

    /**
     * 获取系统性能指标
     */
    Map<String, Object> getSystemPerformance();

    /**
     * 获取数据库性能指标
     */
    Map<String, Object> getDatabasePerformance();

    /**
     * 系统维护模式切换
     */
    void toggleMaintenanceMode(Boolean enabled, String reason);

    /**
     * 获取系统版本信息
     */
    Map<String, Object> getSystemVersion();

    /**
     * 重启系统服务
     */
    void restartSystemService(String serviceName);
}