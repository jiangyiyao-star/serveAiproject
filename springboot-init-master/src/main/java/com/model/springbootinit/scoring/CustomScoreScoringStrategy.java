package com.model.springbootinit.scoring;

import com.model.springbootinit.model.entity.App;
import com.model.springbootinit.model.entity.UserAnswer;

import java.util.List;


/**
 * 自定义评分_得分类_策略实现类
 */
public class CustomScoreScoringStrategy implements ScoringStrategy{
    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        return null;
    }
}
