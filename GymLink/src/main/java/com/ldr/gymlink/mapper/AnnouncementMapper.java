package com.ldr.gymlink.mapper;

import com.ldr.gymlink.model.entity.Announcement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 木子宸
 * @description 针对表【announcement(系统公告表)】的数据库操作Mapper
 * @createDate 2025-12-02 20:39:04
 * @Entity com.ldr.gymlink.model.entity.Announcement
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

}
