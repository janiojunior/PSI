package com.example.resource;

import com.example.dto.PacienteDTO;
import com.example.dto.PageResponse;
import com.example.service.PacienteService;
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

@Path("/pacientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMINISTRADOR", "ATENDENTE", "PSICOLOGO"})
public class PacienteResource {

    @Inject
    PacienteService pacienteService;

    @GET
    public Response getAll() {
        List<PacienteDTO> pacientes = pacienteService.findAll();
        return Response.ok(pacientes).build();
    }

    @GET
    @Path("/paginado")
    public Response getPaginated(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        PageResponse<PacienteDTO> response = pacienteService.findPaginated(page, pageSize);
        return Response.ok(response).build();
    }

    @GET
    @Path("/nome/{nome}")
    public Response getByNome(
            @PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        PageResponse<PacienteDTO> response = pacienteService.findByNome(nome, page, pageSize);
        return Response.ok(response).build();
    }

    @GET
    @Path("/cpf/{cpf}")
    public Response getByCpf(@PathParam("cpf") String cpf) {
        PacienteDTO paciente = pacienteService.findByCpf(cpf);
        return Response.ok(paciente).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        PacienteDTO paciente = pacienteService.findById(id);
        return Response.ok(paciente).build();
    }

    @POST
    public Response create(@Valid PacienteDTO dto) {
        PacienteDTO created = pacienteService.create(dto);
        return Response.created(URI.create("/pacientes/" + created.id()))
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid PacienteDTO dto) {
        PacienteDTO updated = pacienteService.update(id, dto);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        pacienteService.delete(id);
        return Response.noContent().build();
    }
}
