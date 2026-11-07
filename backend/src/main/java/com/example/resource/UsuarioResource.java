package com.example.resource;

import com.example.dto.PageResponse;
import com.example.dto.UsuarioDTO;
import com.example.dto.UsuarioRequestDTO;
import com.example.dto.UsuarioUpdateDTO;
import com.example.service.UsuarioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRADOR")
public class UsuarioResource {

    @Inject
    UsuarioService usuarioService;

    @GET
    public Response getAll() {
        List<UsuarioDTO> usuarios = usuarioService.findAll();
        return Response.ok(usuarios).build();
    }

    @GET
    @Path("/paginado")
    public Response getPaginated(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        PageResponse<UsuarioDTO> response = usuarioService.findPaginated(page, pageSize);
        return Response.ok(response).build();
    }

    @GET
    @Path("/nome/{nome}")
    public Response getByNome(
            @PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        PageResponse<UsuarioDTO> response = usuarioService.findByNome(nome, page, pageSize);
        return Response.ok(response).build();
    }

    @GET
    @Path("/cpf/{cpf}")
    public Response getByCpf(@PathParam("cpf") String cpf) {
        UsuarioDTO usuario = usuarioService.findByCpf(cpf);
        return Response.ok(usuario).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        UsuarioDTO usuario = usuarioService.findById(id);
        return Response.ok(usuario).build();
    }

    @POST
    public Response create(@NotNull @Valid UsuarioRequestDTO dto) {
        UsuarioDTO created = usuarioService.create(dto);
        return Response.created(URI.create("/usuarios/" + created.id()))
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @NotNull @Valid UsuarioUpdateDTO dto) {
        UsuarioDTO updated = usuarioService.update(id, dto);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        usuarioService.delete(id);
        return Response.noContent().build();
    }
}
