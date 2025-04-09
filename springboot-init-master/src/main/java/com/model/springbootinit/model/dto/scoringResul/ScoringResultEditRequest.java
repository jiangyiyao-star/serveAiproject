package com.model.springbootinit.model.dto.scoringResul;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑问题请求
 *
 */
@Data
public class ScoringResultEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}