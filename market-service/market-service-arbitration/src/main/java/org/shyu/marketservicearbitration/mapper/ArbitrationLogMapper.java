package org.shyu.marketservicearbitration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.shyu.marketservicearbitration.entity.ArbitrationLogEntity;

/**
 * 仲裁日志 Mapper 接口
 * @author shyu
 * @since 2026-04-01
 */
@Mapper
public interface ArbitrationLogMapper extends BaseMapper<ArbitrationLogEntity> {

}