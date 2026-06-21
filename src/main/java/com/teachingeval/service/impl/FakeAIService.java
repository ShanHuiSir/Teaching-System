package com.teachingeval.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

import com.teachingeval.entity.EvaluationResult;
import org.springframework.stereotype.Service;

import com.teachingeval.dto.AIEvalRequest;

@Service
public class FakeAIService {

    private static final BigDecimal BASE_SCORE = new BigDecimal("82.50");
    private static final BigDecimal MAX_DELTA = new BigDecimal("15.00");

    private static final String[] ISSUE_POOLS = {
        """
                1. 结构不够清晰，建议优化段落层次
                2. 缺少核心论点支撑材料
                3. 格式规范性不足，标题层级需统一""",
        """
                1. 内容覆盖较全面，但深度有待加强
                2. 部分论据与主题关联度不足
                3. 引用格式不够规范""",
        """
                1. 逻辑连贯性良好，个别段落过渡略显生硬
                2. 案例选择恰当，但分析不够深入
                3. 结论部分可进一步提炼核心观点""",
        """
                1. 整体框架较好，细节处理有待完善
                2. 语言表达流畅，但部分专业术语使用不准确
                3. 缺少对前沿成果的引用和对比""",
    };

    private static final String[] COMMENT_POOLS = {
            "整体完成度较好，但在结构组织上还有提升空间，建议加强逻辑连贯性。",
            "选题有一定价值，内容涵盖主要知识点，建议进一步深化分析。",
            "基本完成了任务要求，表达清晰，但创新性方面稍显不足。",
            "工作态度认真，材料准备充分，希望后续能在思想深度上持续提升。",
    };

    private static final String[][] DIMENSION_POOLS = {
            {
                    "{\"name\":\"完成度\",\"score\":%d,\"comment\":\"内容较完整\"}",
                    "{\"name\":\"规范性\",\"score\":%d,\"comment\":\"格式可优化\"}",
                    "{\"name\":\"创新性\",\"score\":%d,\"comment\":\"有一定独立思考\"}",
            },
            {
                    "{\"name\":\"内容质量\",\"score\":%d,\"comment\":\"观点明确\"}",
                    "{\"name\":\"结构逻辑\",\"score\":%d,\"comment\":\"层次分明\"}",
                    "{\"name\":\"语言表达\",\"score\":%d,\"comment\":\"通顺流畅\"}",
            },
    };

    public EvaluationResult evaluate(AIEvalRequest request) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        double delta = (rng.nextDouble() * 2 - 1) * MAX_DELTA.doubleValue();
        BigDecimal score = BASE_SCORE.add(BigDecimal.valueOf(delta)).setScale(2, RoundingMode.HALF_UP);

        String issues = ISSUE_POOLS[rng.nextInt(ISSUE_POOLS.length)];
        String comment = COMMENT_POOLS[rng.nextInt(COMMENT_POOLS.length)];

        String[] dimTemplate = DIMENSION_POOLS[rng.nextInt(DIMENSION_POOLS.length)];
        int d1 = 75 + rng.nextInt(21);
        int d2 = 75 + rng.nextInt(21);
        int d3 = 75 + rng.nextInt(21);
        String dimensions = String.format("[%s,%s,%s]",
                String.format(dimTemplate[0], d1),
                String.format(dimTemplate[1], d2),
                String.format(dimTemplate[2], d3));

        EvaluationResult result = new EvaluationResult();
        result.setAiScore(score);
        result.setAiIssues(issues);
        result.setAiComment(comment);
        result.setDimensionScores(dimensions);
        result.setStatus(EvaluationResult.STATUS_AI_REVIEWED);
        result.setAiSource(EvaluationResult.AI_SOURCE_FAKE);
        return result;
    }
}
