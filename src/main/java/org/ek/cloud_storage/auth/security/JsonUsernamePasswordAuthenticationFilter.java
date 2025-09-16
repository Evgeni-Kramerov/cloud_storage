package org.ek.cloud_storage.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ek.cloud_storage.domain.UserResponseDTO;
import org.ek.cloud_storage.exception.ErrorResponseDTO;
import org.ek.cloud_storage.domain.UserDetailsRequestDTO;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

public class JsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonUsernamePasswordAuthenticationFilter(String defaultFilterProcessesUrl) {
        super(new AntPathRequestMatcher(defaultFilterProcessesUrl, "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {

        try {
            if (!request.getContentType().equals("application/json")) {
                throw new AuthenticationServiceException("Unsupported content type");
            }

            UserDetailsRequestDTO loginRequest =
                    objectMapper.readValue(request.getInputStream(), UserDetailsRequestDTO.class);

            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                            loginRequest.getPassword());

            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to parse JSON request", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        Object principal = authResult.getPrincipal();

        Authentication sessionAuth;

        if (principal instanceof CloudUserDetails details) {
            CloudUserSession sessionPrincipal = new CloudUserSession(
                    details.getUser().getId(),
                    details.getUsername()
            );

            sessionAuth = new UsernamePasswordAuthenticationToken(
                    sessionPrincipal,
                    null,
                    sessionPrincipal.getAuthorities()
            );
        } else {
            sessionAuth = authResult;
        }

        // Set security context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(sessionAuth);
        SecurityContextHolder.setContext(context);

        // ✅ Save context to session
        HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.saveContext(context, request, response);

        // Send user response
        UserResponseDTO user = new UserResponseDTO(sessionAuth.getName());
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getOutputStream(), user);
    }

    //    @Override
//    protected void successfulAuthentication(HttpServletRequest request,
//                                            HttpServletResponse response, FilterChain chain,
//                                            Authentication authResult) throws IOException, ServletException {
//        // Set the authentication in the context
//        SecurityContextHolder.getContext().setAuthentication(authResult);
//
//        // Create a new session and bind it with the authentication
//        HttpSession session = request.getSession(true);
//        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
//                SecurityContextHolder.getContext());
//
//        UserResponseDTO user = new UserResponseDTO(authResult.getName());
//
//        response.setContentType("application/json");
//        response.setStatus(HttpServletResponse.SC_OK);
//
//        objectMapper.writeValue(response.getOutputStream(), user);
//
//    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print(objectMapper.writeValueAsString(new ErrorResponseDTO("Authentication failed")));

    }
}
