package com.teachingeval;

import com.teachingeval.config.DataInitializer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.upload.root=target/test-uploads")
@AutoConfigureMockMvc
class TeachingSystemFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataInitializer dataInitializer;

    @BeforeEach
    void resetData() {
        FileSystemUtils.deleteRecursively(Paths.get("target/test-uploads").toFile());
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
                .andExpect(result -> assertThat(result.getResponse().getContentType())
                        .startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty());
    }

    @Test
    void uploadSubmissionStoresFileAndReturnsMetadata() throws Exception {
        String studentPageResponse = mockMvc.perform(get("/api/students/page")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer studentId = com.jayway.jsonpath.JsonPath.read(studentPageResponse, "$.content[0].id");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "real-upload.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "真实上传测试内容".getBytes(StandardCharsets.UTF_8)
        );

        String response = mockMvc.perform(multipart("/api/submissions/upload")
                        .file(file)
                        .param("studentId", String.valueOf(studentId))
                        .param("title", "真实文件上传作业")
                        .param("workType", "实验报告")
                        .param("remark", "验证真实文件上传"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentName").value("张三"))
                .andExpect(jsonPath("$.fileName").value("real-upload.docx"))
                .andExpect(jsonPath("$.fileSize").value(file.getSize()))
                .andExpect(jsonPath("$.contentType").value(file.getContentType()))
                .andExpect(jsonPath("$.preprocessStatus").value("SKIPPED"))
                .andExpect(jsonPath("$.preprocessMessage").value("Py预处理未启用"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer submissionId = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        String filePath = com.jayway.jsonpath.JsonPath.read(response, "$.filePath");
        assertThat(filePath).isEqualTo("target/test-uploads/submissions/" + submissionId + "/real-upload.docx");
        Path savedPath = Paths.get(filePath);
        assertThat(Files.exists(savedPath)).isTrue();
        assertThat(Files.readString(savedPath)).isEqualTo("真实上传测试内容");
    }

    @Test
    void uploadSubmissionRejectsUnsupportedFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "unsafe.exe",
                "application/octet-stream",
                "bad".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/submissions/upload")
                        .file(file)
                        .param("studentId", "1")
                        .param("title", "不支持的文件")
                        .param("workType", "实验报告"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("不支持的文件类型"));
    }

    @Test
    void uploadSubmissionCanForwardFileToPreprocessService() throws Exception {
        AtomicReference<String> capturedRequestBody = new AtomicReference<>("");
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(0), 0);
        server.createContext("/api/preprocess", exchange -> respondWithPreprocessResult(exchange, capturedRequestBody));
        server.start();

        try {
            System.setProperty("app.preprocess.enabled", "true");
            System.setProperty("app.preprocess.endpoint-url",
                    "http://localhost:" + server.getAddress().getPort() + "/api/preprocess");

            String studentPageResponse = mockMvc.perform(get("/api/students/page")
                            .param("page", "0")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            Integer studentId = com.jayway.jsonpath.JsonPath.read(studentPageResponse, "$.content[0].id");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "preprocess-upload.docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "传给 Py 的真实文件内容".getBytes(StandardCharsets.UTF_8)
            );

            mockMvc.perform(multipart("/api/submissions/upload")
                            .file(file)
                            .param("studentId", String.valueOf(studentId))
                            .param("title", "Java Py 联调作业")
                            .param("workType", "实验报告"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.preprocessStatus").value("SUCCESS"))
                    .andExpect(jsonPath("$.preprocessMessage").value("Py预处理完成"))
                    .andExpect(jsonPath("$.preprocessResult").value("{\"renderStatus\":\"ok\"}"));

            assertThat(capturedRequestBody.get()).contains("submissionId");
            assertThat(capturedRequestBody.get()).contains("studentId");
            assertThat(capturedRequestBody.get()).contains("Java Py 联调作业");
            assertThat(capturedRequestBody.get()).contains("preprocess-upload.docx");
            assertThat(capturedRequestBody.get()).contains("传给 Py 的真实文件内容");
        } finally {
            System.clearProperty("app.preprocess.enabled");
            System.clearProperty("app.preprocess.endpoint-url");
            server.stop(0);
        }
    }

    private void respondWithPreprocessResult(HttpExchange exchange,
                                             AtomicReference<String> capturedRequestBody) throws java.io.IOException {
        assertThat(exchange.getRequestMethod()).isEqualTo("POST");
        assertThat(exchange.getRequestHeaders().getFirst("Content-Type")).contains("multipart/form-data");
        capturedRequestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        byte[] response = "{\"renderStatus\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
