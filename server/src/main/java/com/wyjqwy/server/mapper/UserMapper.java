package com.wyjqwy.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wyjqwy.server.model.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
