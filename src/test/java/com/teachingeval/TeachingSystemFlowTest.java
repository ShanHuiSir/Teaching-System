package com.teachingeval;

import com.teachingeval.config.DataInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeachingSystemFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataInitializer dataInitializer;

    @BeforeEach
    void resetData() {
        dataInitializer.resetDemoData();
    }

    @Test
    void studentPageSupportsPaginationAndKeywordSearch() throws Exception {
        mockMvc.perform(get("/api/students/page")
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.totalElements").value(8))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.hasNext").value(true));

        mockMvc.perform(get("/api/students/page")
                        .param("page", "-1")
                        .param("size", "999")
                        .param("keyword", "张"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(200))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("张三"));
    }

    @Test
    void duplicateStudentNumberReturnsClearError() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentNo": "2026001",
                                  "name": "重复学生",
                                  "className": "软件 1 班"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("学号已存在"));
    }

    @Test
    void mainEvaluationFlowCreatesSubmissionEvaluatesReviewsAndUpdatesSummary() throws Exception {
        String studentPageResponse = mockMvc.perform(get("/api/students/page")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer studentId = com.jayway.jsonpath.JsonPath.read(studentPageResponse, "$.content[0].id");

        String submissionResponse = mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": %d,
                                  "title": "自动化测试作业",
                                  "fileName": "auto-test.zip",
                                  "workType": "代码压缩包",
                                  "remark": "覆盖最小主流程"
                                }
                                """.formatted(studentId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", greaterThan(0)))
                .andExpect(jsonPath("$.studentName").value("张三"))
                .andExpect(jsonPath("$.title").value("自动化测试作业"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer submissionId = com.jayway.jsonpath.JsonPath.read(submissionResponse, "$.id");

        mockMvc.perform(post("/api/submissions/{submissionId}/evaluate", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentName": "张三",
                                  "fileName": "auto-test.zip"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissionId").value(submissionId))
                .andExpect(jsonPath("$.aiScore").value(82.50))
                .andExpect(jsonPath("$.status").value(1));

        mockMvc.perform(post("/api/submissions/{submissionId}/teacher-review", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "teacherScore": 91,
                                  "teacherComment": "自动化测试复核通过。"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissionId").value(submissionId))
                .andExpect(jsonPath("$.teacherScore").value(91))
                .andExpect(jsonPath("$.status").value(2));

        mockMvc.perform(get("/api/statistics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentCount").value(8))
                .andExpect(jsonPath("$.submissionCount").value(7))
                .andExpect(jsonPath("$.aiEvaluatedCount").value(4))
                .andExpect(jsonPath("$.teacherConfirmedCount").value(2))
                .andExpect(jsonPath("$.averageTeacherScore").value(91.50));
    }

    @Test
    void exportEndpointReturnsXlsxStream() throws Exception {
        mockMvc.perform(post("/api/export/excel"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty());
    }
}
