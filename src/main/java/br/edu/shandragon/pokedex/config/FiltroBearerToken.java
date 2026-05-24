package br.edu.shandragon.pokedex.config;

import br.edu.shandragon.pokedex.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class FiltroBearerToken extends OncePerRequestFilter {

    private final String tokenAdmin;
    private final JwtService jwtService;

    public FiltroBearerToken(String tokenAdmin, JwtService jwtService) {
        this.tokenAdmin = tokenAdmin;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            String token = cabecalho.substring(7);
            if (tokenAdmin.equals(token)) {
                var autenticacao = new UsernamePasswordAuthenticationToken("admin", null, List.of());
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            } else if (jwtService.validar(token)) {
                String sub = jwtService.extrairSub(token);
                var autenticacao = new UsernamePasswordAuthenticationToken(sub, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
                request.setAttribute("jwtAtual", token);
                String novoToken = jwtService.gerar(UUID.fromString(sub));
                response.setHeader("X-Token-Renovado", novoToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
