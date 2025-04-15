package com.model.springbootinit.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.model.springbootinit.model.dto.question.QuestionContentDTO;
import com.model.springbootinit.model.entity.App;
import com.model.springbootinit.model.entity.Question;
import com.model.springbootinit.model.entity.ScoringResult;
import com.model.springbootinit.model.entity.UserAnswer;
import com.model.springbootinit.model.vo.QuestionVO;
import com.model.springbootinit.service.AppService;
import com.model.springbootinit.service.QuestionService;
import com.model.springbootinit.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;


/**
 * 自定义评分_得分类_策略实现类
 */
@ScoringStrategyConfig(appType = 0, scoringStrategy = 0)
public class CustomScoreScoringStrategy implements ScoringStrategy {

    @Resource
    private QuestionService questionService;
    @Resource
    private ScoringResultService scoringResultService;

    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {

        // 1. 根据id查询题目和题目结果信息（按分数排序）
        Long appId = app.getId();

        //使用lambda表达式查询题目信息   在question表里面查询 appId=app.getId() 的题目信息
        Question question = questionService.getOne( //返回单个对象
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        //使用lambda表达式查询题目结果信息   在scoring_result表里面查询 appId=app.getId() 的评分结果
        List<ScoringResult> scoringResultList = scoringResultService.list(  //返回多个对象
                Wrappers.lambdaQuery(ScoringResult.class).eq(ScoringResult::getAppId, appId)
                        .orderByDesc(ScoringResult::getResultScoreRange)
        );
        // 2. 统计通过的总得分
        int totalScore = 0;
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();  //获得题目内容

        // 遍历题目列表
        for (QuestionContentDTO questionContentDTO : questionContent) {
            // 遍历答案列表
            for (String answer : choices) {
                // 遍历题目中的选项
                for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
                    // 如果答案和选项的key匹配
                    if (option.getKey().equals(answer)) {
                        //从 option.getScore() 中安全地获取值，如果为 null，就使用默认值 0。
                        int score = Optional.of(option.getScore()).orElse(0);
                        totalScore += score;
                    }
                }
            }
        }
        // 3. 遍历得分结果，找到第一个用户大于得分范围的结果 ，作为最最终结果
        ScoringResult maxScoringResult = scoringResultList.get(0);
        for (ScoringResult result : scoringResultList) {
            if (totalScore >= result.getResultScoreRange()) {
                maxScoringResult = result;
                break;
            }
        }
        // 4. 构造返回值，填充答案对象的属性
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxScoringResult.getId());
        userAnswer.setResultName(maxScoringResult.getResultName());
        userAnswer.setResultDesc(maxScoringResult.getResultDesc());
        userAnswer.setResultPicture(maxScoringResult.getResultPicture());
        userAnswer.setResultScore(totalScore);
        return userAnswer;

    }
}
