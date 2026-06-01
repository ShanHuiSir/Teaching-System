package com.teachingeval.service;

import java.time.Duration;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class PreprocessClient {

    private static final Logger log = LoggerFactory.getLogger(PreprocessClient.class);

    private final RestTemplateBuilder restTemplateBuilder;
    private final Environment environment;

    public PreprocessClient(RestTemplateBuilder restTemplateBuilder,
                            Environment environment) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.environment = environment;
    }

    public PreprocessResult submit(Long submissionId,
                                   Long studentId,
                                   String title,
                                   String workType,
                                   String originalFilename,
                                   String contentType,
                                   Path filePath) {
        boolean enabled = environment.getProperty("app.preprocess.enabled", Boolean.class, false);
        if (!enabled) {
            return PreprocessResult.skipped("Py预处理未启用");
        }
        String endpointUrl = environment.getProperty(
                "app.preprocess.endpoint-url",
                "http://localhost:8000/api/preprocess"
        );
        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(getLongProperty("app.preprocess.connect-timeout-ms", 3000L)))
                .setReadTimeout(Duration.ofMillis(getLongProperty("app.preprocess.read-timeout-ms", 10000L)))
                .build();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("submissionId", String.valueOf(submissionId));
        body.add("studentId", String.valueOf(studentId));
        body.add("title", title);
        body.add("workType", workType);
        body.add("originalFilename", originalFilename);
        body.add("contentType", contentType);
        body.add("file", new FileSystemResource(filePath));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endpointUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                return PreprocessResult.success(response.getBody());
            }
            return PreprocessResult.failed("Py预处理返回异常状态：" + response.getStatusCode().value(), response.getBody());
        } catch (RestClientException exception) {
            log.warn("调用 Py 预处理服务失败，submissionId={}", submissionId, exception);
            return PreprocessResult.failed("Py预处理服务调用失败：" + exception.getMessage(), null);
        }
    }

    private long getLongProperty(String propertyName, long defaultValue) {
        return environment.getProperty(propertyName, Long.class, defaultValue);
    }
}
