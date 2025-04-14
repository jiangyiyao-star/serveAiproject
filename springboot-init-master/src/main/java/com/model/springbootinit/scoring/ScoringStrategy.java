package com.model.springbootinit.scoring;

import com.model.springbootinit.model.entity.App;
import com.model.springbootinit.model.entity.UserAnswer;

import java.util.List;

/**
 * 评分策略
 *
 */
public interface ScoringStrategy {

    /**
     * 执行评分
     *
     * @param choices    用户选择的答案
     * @param app       用户回答的题目
     * @return
     * @throws Exception
     */
    UserAnswer doScore(List<String> choices, App app) throws Exception;
}