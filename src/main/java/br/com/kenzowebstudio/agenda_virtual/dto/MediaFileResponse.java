package br.com.kenzowebstudio.agenda_virtual.dto;

import org.springframework.core.io.Resource;

public record MediaFileResponse(
        Resource resource,
        String contentType) {
}
