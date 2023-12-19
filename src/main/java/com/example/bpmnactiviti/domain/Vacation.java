package com.example.bpmnactiviti.domain;

import lombok.Data;

import java.util.Date;

/**
 * @Author yanbl
 * @Date 2023/12/19 14:25
 * @Description ...
 */
@Data
public class Vacation {

    /**
     * 请假原因
     */
    private String reason;
    /**
     * 请假天数
     */
    private Integer days;

    /**
     * 申请人
     */
    private String applyUser;

    /**
     * 申请时间
     */
    private Date applyTime;
    /**
     * 申请状态
     */
    private String applyStatus;
    private String result;
}
