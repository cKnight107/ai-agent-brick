package com.agent.brick.service.impl;

import com.agent.brick.ai.agent.AssistantAgent;
import com.agent.brick.ai.transformer.KeywordTransformer;
import com.agent.brick.base.BaseService;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.enums.EventEnums;
import com.agent.brick.pojo.vo.JsonResult;
import com.agent.brick.service.AiService;
import com.agent.brick.util.CommonUtils;
import com.agent.brick.util.RandomUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @since 2025/6/1
 *
 * @author cKnight
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiServiceImpl extends BaseService implements AiService {

    private final KeywordTransformer keywordTransformer;

    private final AssistantAgent assistantAgent;

    @Override
    public Flux<ServerSentEvent<String>> completions(AiReq req) {
        CommonUtils.checkArgs(req.getMessages(),req.getMessage().getContent());
        String id = RandomUtils.generateUUID();
        return assistantAgent
                .chatClient(req)
                .stream()
                .chatResponse()
                .map(obj -> CommonUtils.genMsg(id, EventEnums.MSG,obj));
    }

    @Override
    public JsonResult<Void> etlDocument(MultipartFile file) {
        //转换为resource
        InputStreamResource resource = new InputStreamResource(file);
        //1.读取文档
//        TikaDocumentReader reader = new TikaDocumentReader(resource);
//        List<Document> documents = reader.get();

        //pdf 目录读取
        ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader(resource,
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build());
        List<Document> documents = pdfReader.read();

        //2.转换文档 统一文档格式
        //2.1 文档分割
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        documents = textSplitter.apply(documents);

        //2.2 关键词提取
        keywordTransformer.doApply(documents,2);
//
//        //2.3 摘要提取
//        SummaryMetadataEnricher summaryMetadataEnricher = new SummaryMetadataEnricher(qwenPlusModel,
//                List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS,
//                        SummaryMetadataEnricher.SummaryType.CURRENT,
//                        SummaryMetadataEnricher.SummaryType.NEXT));
//        summaryMetadataEnricher.apply(documents);
//
//        //3. 放入向量数据库
//        milvusVectorStore.doAdd(documents);
        return null;
    }
}
