package com.example.aiservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ticket")
public class Ticket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String summary;     // AI 生成的摘要

    private String category;    // AI 生成的分类

    private String priority;    // AI 生成的优先级

    private String status;      // PENDING / PROCESSING / RESOLVED / CLOSED

    private Long creatorId;

    private Long handlerId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}