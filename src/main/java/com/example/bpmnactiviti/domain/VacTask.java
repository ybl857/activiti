package com.example.bpmnactiviti.domain;

import lombok.Data;

import java.util.Date;

/**
 * @Author yanbl
 * @Date 2023/12/19 14:31
 * @Description ...
 */
@Data
public class VacTask {

    private String id;
    private String name;
    private Vacation vac;
    private Date createTime;
}
