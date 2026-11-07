package com.example.resource;

import com.example.dto.PageResponse;
import com.example.dto.PsicologoDTO;
import com.example.service.PsicologoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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

@Path("/psicologos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMINISTRADOR", "ATENDENTE", "PSICOLOGO"})
public class PsicologoResource {

    @Inject
    PsicologoService psicologoService;

    @GET
    public Response getAll() {
        List<PsicologoDTO> psicologos = psicologoService.findAll();
        return Response.ok(psicologos).build();
    }

    @GET
    @Path("/paginado")
    public Response getPaginated(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        PageResponse<PsicologoDTO> response = psicologoService.findPaginated(page, pageSize);
        return Response.ok(response).build();
    }

    @GET
    @Path("/nome/{nome}")
    public Response getByNome(
            @PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        PageResponse<PsicologoDTO> response = psicologoService.findByNome(nome, page, pageSize);
        return Response.ok(response).build();
    }

    @GET
    @Path("/cpf/{cpf}")
    public Response getByCpf(@PathParam("cpf") String cpf) {
        PsicologoDTO psicologo = psicologoService.findByCpf(cpf);
        return Response.ok(psicologo).build();
    }

    @GET
    @Path("/crp/{crp}")
    public Response getByCrp(@PathParam("crp") String crp) {
        PsicologoDTO psicologo = psicologoService.findByCrp(crp);
        return Response.ok(psicologo).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        PsicologoDTO psicologo = psicologoService.findById(id);
        return Response.ok(psicologo).build();
    }

    @POST
    public Response create(@Valid PsicologoDTO dto) {
        PsicologoDTO created = psicologoService.create(dto);
        return Response.created(URI.create("/psicologos/" + created.id()))
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid PsicologoDTO dto) {
        PsicologoDTO updated = psicologoService.update(id, dto);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        psicologoService.delete(id);
        return Response.noContent().build();
    }
}
