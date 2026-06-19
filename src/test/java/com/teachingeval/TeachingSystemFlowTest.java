package com.teachingeval;

import com.teachingeval.config.DataInitializer;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionRepository;
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
import org.springframework.core.io.ClassPathResource;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.upload.root=target/test-uploads",
        "app.auth.enabled=false"
})
@AutoConfigureMockMvc
class TeachingSystemFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @BeforeEach
    void resetData() {
        FileSystemUtils.deleteRecursively(Paths.get("target/test-uploads").toFile());
        dataInitializer.resetDemoData();
    }

    private MockMultipartFile loadTestFile(String resourcePath,
                                            String uploadName,
                                            String contentType) throws Exception {
        var resource = new ClassPathResource(resourcePath);
        byte[] bytes = resource.getInputStream().readAllBytes();
        return new MockMultipartFile("file", uploadName, contentType, bytes);
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
                .andExpect(jsonPath("$.totalElements").value(18))
                .andExpect(jsonPath("$.totalPages").value(6))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.content[0].classId", greaterThan(0)))
                .andExpect(jsonPath("$.content[0].className").value("软件 1 班"));

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
    void classAndAssignmentModelsExposeStableRelationships() throws Exception {
        mockMvc.perform(get("/api/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].name").value("软件 1 班"))
                .andExpect(jsonPath("$[0].grade").value("2026"));

        String assignmentResponse = mockMvc.perform(get("/api/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(9)))
                .andExpect(jsonPath("$[0].classId", greaterThan(0)))
                .andExpect(jsonPath("$[0].classIds", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].classNames", hasSize(greaterThan(0))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer classId = com.jayway.jsonpath.JsonPath.read(assignmentResponse, "$[0].classId");
        mockMvc.perform(get("/api/assignments")
                        .param("classId", String.valueOf(classId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].classId").value(classId));
    }

    @Test
    void assignmentCanTargetMultipleClasses() throws Exception {
        String classResponse = mockMvc.perform(get("/api/classes"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer firstClassId = com.jayway.jsonpath.JsonPath.read(classResponse, "$[0].id");
        Integer secondClassId = com.jayway.jsonpath.JsonPath.read(classResponse, "$[1].id");
        String firstClassName = com.jayway.jsonpath.JsonPath.read(classResponse, "$[0].name");
        String secondClassName = com.jayway.jsonpath.JsonPath.read(classResponse, "$[1].name");

        String assignmentResponse = mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "多班级受理作业",
                                  "description": "覆盖多班级保存和筛选",
                                  "workType": "实验报告",
                                  "classIds": [%d, %d]
                                }
                                """.formatted(firstClassId, secondClassId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("多班级受理作业"))
                .andExpect(jsonPath("$.classId").value(firstClassId))
                .andExpect(jsonPath("$.classIds", hasSize(2)))
                .andExpect(jsonPath("$.classNames[0]").value(firstClassName))
                .andExpect(jsonPath("$.classNames[1]").value(secondClassName))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer assignmentId = com.jayway.jsonpath.JsonPath.read(assignmentResponse, "$.id");

        mockMvc.perform(get("/api/assignments")
                        .param("classId", String.valueOf(secondClassId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)]".formatted(assignmentId), hasSize(1)));
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
        Integer classId = com.jayway.jsonpath.JsonPath.read(studentPageResponse, "$.content[0].classId");

        String assignmentResponse = mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "自动化测试作业",
                                  "description": "主流程联调作业",
                                  "workType": "代码压缩包",
                                  "classId": %d
                                }
                                """.formatted(classId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("自动化测试作业"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer assignmentId = com.jayway.jsonpath.JsonPath.read(assignmentResponse, "$.id");

        String submissionResponse = mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": %d,
                                  "assignmentId": %d,
                                  "title": "自动化测试作业",
                                  "fileName": "auto-test.zip",
                                  "workType": "代码压缩包",
                                  "remark": "覆盖最小主流程"
                                }
                                """.formatted(studentId, assignmentId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", greaterThan(0)))
                .andExpect(jsonPath("$.studentName").value("张三"))
                .andExpect(jsonPath("$.assignmentId").value(assignmentId))
                .andExpect(jsonPath("$.assignmentTitle").value("自动化测试作业"))
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
                .andExpect(jsonPath("$.studentCount").value(18))
                .andExpect(jsonPath("$.submissionCount").value(19))
                .andExpect(jsonPath("$.aiEvaluatedCount").value(6))
                .andExpect(jsonPath("$.teacherConfirmedCount").value(3))
                .andExpect(jsonPath("$.averageTeacherScore").value(89.00));
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

        MockMultipartFile file = loadTestFile(
                "test-files/sample-code.cpp",
                "sample-code.cpp",
                "text/x-c++src"
        );

        String response = mockMvc.perform(multipart("/api/submissions/upload")
                        .file(file)
                        .param("studentId", String.valueOf(studentId))
                        .param("title", "真实文件上传作业")
                        .param("workType", "代码作业")
                        .param("remark", "验证真实文件上传"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentName").value("张三"))
                .andExpect(jsonPath("$.fileName").value("sample-code.cpp"))
                .andExpect(jsonPath("$.fileSize").value(file.getSize()))
                .andExpect(jsonPath("$.contentType").value(file.getContentType()))
                .andExpect(jsonPath("$.preprocessStatus").value("SKIPPED"))
                .andExpect(jsonPath("$.preprocessMessage").value("Py预处理未启用"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer submissionId = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        String filePath = com.jayway.jsonpath.JsonPath.read(response, "$.filePath");
        assertThat(filePath).isEqualTo("target/test-uploads/submissions/" + submissionId + "/sample-code.cpp");
        Path savedPath = Paths.get(filePath);
        assertThat(Files.exists(savedPath)).isTrue();
        String savedContent = Files.readString(savedPath);
        assertThat(savedContent).contains("Binary Search Implementation");

        mockMvc.perform(get("/api/submissions/{submissionId}/files", submissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].submissionId").value(submissionId))
                .andExpect(jsonPath("$[0].fileName").value("sample-code.cpp"))
                .andExpect(jsonPath("$[0].filePath").value(filePath))
                .andExpect(jsonPath("$[0].primaryFile").value(true));

        mockMvc.perform(get("/api/submissions/{submissionId}/file", submissionId))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentType())
                        .startsWith(file.getContentType()))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("sample-code.cpp")))
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .contains("Binary Search Implementation"));
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

            MockMultipartFile file = loadTestFile(
                    "test-files/sample-report.txt",
                    "preprocess-upload.txt",
                    "text/plain"
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
            assertThat(capturedRequestBody.get()).contains("preprocess-upload.txt");
            assertThat(capturedRequestBody.get()).contains("软件工程实训项目总结报告");
        } finally {
            System.clearProperty("app.preprocess.enabled");
            System.clearProperty("app.preprocess.endpoint-url");
            server.stop(0);
        }
    }

    @Test
    void evaluateSubmissionCanCallRealAiServiceWhenEnabled() throws Exception {
        AtomicReference<String> capturedRequestBody = new AtomicReference<>("");
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(0), 0);
        server.createContext("/api/evaluate/real", exchange -> respondWithRealAiResult(exchange, capturedRequestBody));
        server.start();

        try {
            System.setProperty("app.ai.real.enabled", "true");
            System.setProperty("app.ai.real.endpoint-url",
                    "http://localhost:" + server.getAddress().getPort() + "/api/evaluate/real");

            String studentPageResponse = mockMvc.perform(get("/api/students/page")
                            .param("page", "0")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            Integer studentId = com.jayway.jsonpath.JsonPath.read(studentPageResponse, "$.content[0].id");

            MockMultipartFile file = loadTestFile(
                    "test-files/sample-code.cpp",
                    "real-ai-upload.cpp",
                    "text/x-c++src"
            );

            String uploadResponse = mockMvc.perform(multipart("/api/submissions/upload")
                            .file(file)
                            .param("studentId", String.valueOf(studentId))
                            .param("title", "真实 AI 联调作业")
                            .param("workType", "实验报告"))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            Integer submissionId = com.jayway.jsonpath.JsonPath.read(uploadResponse, "$.id");

            mockMvc.perform(post("/api/submissions/{submissionId}/evaluate", submissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "studentName": "张三",
                                      "fileName": "real-ai-upload.cpp"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.submissionId").value(submissionId))
                    .andExpect(jsonPath("$.aiScore").value(88.00))
                    .andExpect(jsonPath("$.aiIssues").value("1. Mock Py 返回的问题"))
                    .andExpect(jsonPath("$.aiComment").value("Mock Py 真实 AI 评价完成。"))
                    .andExpect(jsonPath("$.dimensionScores[0].name").value("代码质量"))
                    .andExpect(jsonPath("$.dimensionScores[0].score").value(90))
                    .andExpect(jsonPath("$.status").value(1));

            assertThat(capturedRequestBody.get()).contains("studentName");
            assertThat(capturedRequestBody.get()).contains("张三");
            assertThat(capturedRequestBody.get()).contains("real-ai-upload.cpp");
            assertThat(capturedRequestBody.get()).contains("binarySearchIterative");
        } finally {
            System.clearProperty("app.ai.real.enabled");
            System.clearProperty("app.ai.real.endpoint-url");
            server.stop(0);
        }
    }

    @Test
    void evaluateSubmissionWithRealPythonService() throws Exception {
        String studentPageResponse = mockMvc.perform(get("/api/students/page")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer studentId = com.jayway.jsonpath.JsonPath.read(studentPageResponse, "$.content[0].id");

        MockMultipartFile file = loadTestFile(
                "test-files/sample-code.cpp",
                "real-eval.cpp",
                "text/x-c++src"
        );

        String uploadResponse = mockMvc.perform(multipart("/api/submissions/upload")
                        .file(file)
                        .param("studentId", String.valueOf(studentId))
                        .param("title", "真实 AI 评价测试作业")
                        .param("workType", "代码作业"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer submissionId = com.jayway.jsonpath.JsonPath.read(uploadResponse, "$.id");

        mockMvc.perform(post("/api/submissions/{submissionId}/evaluate", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentName": "张三",
                                  "fileName": "real-eval.cpp",
                                  "subjectType": "code"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissionId").value(submissionId))
                .andExpect(jsonPath("$.aiScore").isNumber())
                .andExpect(jsonPath("$.aiIssues").isString())
                .andExpect(jsonPath("$.aiComment").isString())
                .andExpect(jsonPath("$.dimensionScores").isArray())
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    void assignmentUpdatePreservesClassRelationships() throws Exception {
        Integer classId = com.jayway.jsonpath.JsonPath.read(
                mockMvc.perform(get("/api/classes"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$[0].id"
        );

        String created = mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "更新测试作业",
                                  "description": "验证同班级更新不触发约束冲突",
                                  "workType": "实验报告",
                                  "classIds": [%d]
                                }
                                """.formatted(classId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer assignmentId = com.jayway.jsonpath.JsonPath.read(created, "$.id");

        mockMvc.perform(put("/api/assignments/{id}", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "更新测试作业（已修改）",
                                  "description": "同一班级更新",
                                  "workType": "实验报告",
                                  "classIds": [%d]
                                }
                                """.formatted(classId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(assignmentId))
                .andExpect(jsonPath("$.title").value("更新测试作业（已修改）"))
                .andExpect(jsonPath("$.classIds[0]").value(classId));
    }

    @Test
    void invalidPathVariableReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/assignments/null")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "测试",
                                  "workType": "实验报告"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("无效的参数值：null"));

        mockMvc.perform(put("/api/classes/null")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "测试班级",
                                  "grade": "2026"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("无效的参数值：null"));
    }

    @Test
    void submissionFileDownloadBlocksUnauthorizedStudent() throws Exception {
        Student student = studentRepository.findAll().get(0);
        Long previousClassId = student.getClassId();
        student.setClassId(null);
        studentRepository.saveAndFlush(student);

        try {
            String submissionResp = mockMvc.perform(post("/api/submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "studentId": %d,
                                      "title": "无班级归属测试",
                                      "fileName": "orphan.txt",
                                      "workType": "实验报告"
                                    }
                                    """.formatted(student.getId())))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            Integer submissionId = com.jayway.jsonpath.JsonPath.read(submissionResp, "$.id");

            mockMvc.perform(get("/api/submissions/{id}/file", submissionId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("无权访问此提交"));
        } finally {
            student.setClassId(previousClassId);
            studentRepository.saveAndFlush(student);
        }
    }

    @Test
    void responseStatusExceptionPreservesStatusCode() throws Exception {
        mockMvc.perform(get("/api/submissions/{id}/file", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("提交记录不存在"));
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

    private void respondWithRealAiResult(HttpExchange exchange,
                                         AtomicReference<String> capturedRequestBody) throws java.io.IOException {
        assertThat(exchange.getRequestMethod()).isEqualTo("POST");
        assertThat(exchange.getRequestHeaders().getFirst("Content-Type")).contains("multipart/form-data");
        capturedRequestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        byte[] response = """
                {
                  "aiScore": 88.00,
                  "aiIssues": "1. Mock Py 返回的问题",
                  "aiComment": "Mock Py 真实 AI 评价完成。",
                  "dimensionScores": [
                    {"name": "代码质量", "score": 90, "comment": "结构良好"},
                    {"name": "功能完整性", "score": 86, "comment": "核心功能完整"}
                  ],
                  "status": 1
                }
                """.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
