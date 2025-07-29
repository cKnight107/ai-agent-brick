package com.agent.brick.controller;

import com.agent.brick.controller.request.AiChatReq;
import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;
import com.agent.brick.pojo.vo.AiChatVO;
import com.agent.brick.pojo.vo.JsonResult;
import com.agent.brick.pojo.vo.PageVO;
import com.agent.brick.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author autoCode
 * @since 2025-07-05
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class AiChatController {
    private final AiChatService aiChatService;

    @PostMapping("queryChatPage")
    public JsonResult<PageVO<AiChatVO>> queryChatPage(@RequestBody AiChatReq aiChatReq){
        return JsonResult.buildSuccess(aiChatService.queryChatPage(aiChatReq));
    }

    @GetMapping("queryChatRecordList")
    public JsonResult<List<ChatRecordMsgJsonDto>> queryChatRecordList(@RequestParam(value = "chatId")Long chatId){
        return JsonResult.buildSuccess(aiChatService.queryChatRecordList(chatId));
    }
}
