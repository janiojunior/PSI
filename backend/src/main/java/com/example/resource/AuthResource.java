package com.example.resource;

import com.example.dto.AuthRequestDTO;
import com.example.dto.AuthResponseDTO;
import com.example.dto.UsuarioDTO;
import com.example.service.AuthService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@NotNull @Valid AuthRequestDTO dto) {
        AuthResponseDTO authResponse = authService.autenticar(dto);
        return Response.ok(authResponse)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authResponse.token())
                .build();
    }

    @GET
    @Path("/me")
    @Authenticated
    public Response me() {
        String login = jwt != null ? jwt.getName() : null;
        UsuarioDTO usuario = authService.buscarUsuarioAutenticado(login);
        return Response.ok(usuario).build();
    }
}
