package br.com.kenzowebstudio.agenda_virtual.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class CsrfController {

    @GetMapping("/auth/csrf")
    @Operation(summary = "Obter token CSRF", description = "Retorna o token CSRF utilizado para proteger requisições que modificam dados.")
    public CsrfToken csrf(
            @Parameter(hidden = true) CsrfToken csrfToken) {

        return csrfToken;
    }
}
