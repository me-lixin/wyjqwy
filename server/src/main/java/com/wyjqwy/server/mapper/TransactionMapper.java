package com.wyjqwy.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wyjqwy.server.model.entity.TransactionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionMapper extends BaseMapper<TransactionEntity> {
}
