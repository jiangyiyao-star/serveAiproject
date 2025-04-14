package com.model.springbootinit.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 审核需求
 */
@Data
public class ReviewRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 审核状态 0_待审核 1_审核通过 2_审核不通过
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    private static  final long serialVersionUID = 1L;
}
