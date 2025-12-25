package com.bankshield.api.mapper;

import com.bankshield.api.entity.Dept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {
}