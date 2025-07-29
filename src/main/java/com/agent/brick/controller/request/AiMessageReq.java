package com.agent.brick.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @since 2025/6/7
 *
 * @author cKnight
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiMessageReq {

    private String role;

    private String content;

    private List<AiMediaReq> medias;
}
