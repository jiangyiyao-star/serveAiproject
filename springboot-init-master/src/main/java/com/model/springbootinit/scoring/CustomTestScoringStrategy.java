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
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义评分_测评类_策略实现类
 */
public class CustomTestScoringStrategy implements ScoringStrategy{

    @Resource
    private QuestionService questionService;
    @Resource
    private AppService appService;
    @Resource
    private ScoringResultService scoringResultService;

    /**
     *
     * @param choices    用户选择的答案
     * @param app       用户回答的题目
     * @return
     * @throws Exception
     */
    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {

        Long appId = app.getId();
        // 1. 根据id查询题目和题目结果信息

        //使用lambda表达式查询题目信息   在question表里面查询 appId=app.getId() 的题目信息
        Question question = questionService.getOne( //返回单个对象
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        //使用lambda表达式查询题目结果信息   在scoring_result表里面查询 appId=app.getId() 的评分结果
        List<ScoringResult> scoringResultList = scoringResultService.list(  //返回多个对象
                Wrappers.lambdaQuery(ScoringResult.class).eq(ScoringResult::getAppId, appId)
        );


        // 2. 统计用户的每个选择的属性个数 如I=10, E=5;
        //初始化一个map集合，用来存储每个属性的个数
        Map<String,Integer> optionCount =new HashMap<>();
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
                        // 获取选项的result属性
                        String result = option.getResult();

                        // 如果result属性不在optionCount中，初始化为0
                        if (!optionCount.containsKey(result)) {
                            optionCount.put(result, 0);
                        }

                        // 在optionCount中增加计数
                        optionCount.put(result, optionCount.get(result) + 1);
                    }
                }
            }
        }

        // 3. 遍历每种评分结果，计算哪个结果的得分更高
        // 初始化最高分数和最高分数对应的评分结果
        int maxScore = 0;
        ScoringResult maxScoringResult = scoringResultList.get(0);

        // 遍历评分结果列表
        for (ScoringResult scoringResult : scoringResultList) {
            List<String> resultProp = JSONUtil.toList(scoringResult.getResultProp(), String.class);
            // 计算当前评分结果的分数，[I, E] => [10, 5] => 15
            int score = resultProp.stream()
                    .mapToInt(prop -> optionCount.getOrDefault(prop, 0))
                    .sum();

            // 如果分数高于当前最高分数，更新最高分数和最高分数对应的评分结果
            if (score > maxScore) {
                maxScore = score;
                maxScoringResult = scoringResult;
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
        return userAnswer;


    }
}
