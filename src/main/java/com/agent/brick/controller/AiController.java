package com.agent.brick.controller;

import com.agent.brick.base.BaseVO;
import com.agent.brick.compant.AuthComponent;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.pojo.dto.InterceptorDto;
import com.agent.brick.pojo.vo.JsonResult;
import com.agent.brick.service.AiService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * @since 2025/6/1
 *
 * @author cKnight
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;

    private final AuthComponent authComponent;

    /**
     * 聊天
     * @param aiReq 入参
     * @return str
     */
    @PostMapping(value = "/completions",produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public ResponseEntity<Flux<ServerSentEvent<String>>> completions(@RequestBody AiReq aiReq, HttpServletRequest request){
        InterceptorDto interceptorDto = authComponent.checkToken(request);
        if (Objects.isNull(interceptorDto)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }else {
            aiReq.setSysCacheUserDto(authComponent.getAdminUserInfo(interceptorDto));
        }
        return ResponseEntity.ok(aiService.completions(aiReq));
    }

    /**
     * 对文档进行ETL
     * @param file 文档
     * @return void
     */
    @PostMapping("/etlDocument")
    public JsonResult<Void> etlDocument(@RequestParam(value = "file") MultipartFile file){
        return aiService.etlDocument(file);
    }

    /**
     * 前端获取雪花id
     * @return id
     */
    @GetMapping("/getId")
    public JsonResult<BaseVO> getId(){
        BaseVO baseVO = new BaseVO();
        baseVO.setId(IdWorker.getId());
        return JsonResult.buildSuccess(baseVO);
    }
}
