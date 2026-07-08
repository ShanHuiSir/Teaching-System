# RoadMap — PreprocessSubmittedDocuments

> "不要高估学生提交作品的统一性"  
> 每个工具保持独立文件夹，统一通过 HTTP 被 Py 侧调度。

---

## 已完成

| 工具                        | 说明                                                    |
|---------------------------|-------------------------------------------------------|
| **DocxConv** v0.5.0       | DOCX → JSON / Images（双管线：LibreOffice + Pandoc+WeasyPrint/Playwright）/ 结构标注（text/blocks 模式）/ 内嵌图片提取 |
| **ScreenshotProc** v0.1.0 | 截图 OCR → 逐行文本 + bbox 坐标 JSON，多图时序排序，本地不做语义合并（EasyOCR） |
| **ArchiveProc** v0.1.0    | 压缩包解压 + 文件分类 + 纯文本读取，递归展开嵌套归档，不调用其他工具                 |

---

## 待填坑

| 优先级 | 任务                | 说明                                                                                                    |
|-----|-------------------|-------------------------------------------------------------------------------------------------------|
| P2  | **PPT 预处理**       | PPT 文件文本提取 + 图片导出                                                                                     |
| P2  | **Excel 预处理**     | Excel → JSON 结构化数据提取                                                                                  |
| P3  | **PDF 直接文本提取**    | 非扫描件 PDF 可直接提取文本，无需 OCR                                                                               |
| P3  | **视频文件预处理**       | 实训视频 → 关键帧抽取 + OCR                                                                                    |
| P4  | **手写体识别**         | 学生手写作业拍照 → OCR（难度高，需要专门模型）                                                                            |
| --  | --                | --                                                                                                    |
| P2  | **OCR 代码感知后处理**   | ScreenshotProc 符号识别弱（MapleMono `@` 等）。编辑距离 + Java/Python 词典纠错，如 `RRestControexer` → `@RestController` |
| P3  | **合成数据微调 OCR 模型** | 用 Cascadia Code / MapleMono / JetBrains Mono 等常见代码字体自动生成标注数据，微调 EasyOCR 识别模型                          |
