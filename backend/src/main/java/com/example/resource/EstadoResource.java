package com.example.resource;

import com.example.dto.EstadoDTO;
import com.example.service.EstadoService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/estados")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EstadoResource {

    @Inject
    EstadoService estadoService;

    @GET
    public Response getAll() {
        List<EstadoDTO> estados = estadoService.findAll();
        return Response.ok(estados).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        EstadoDTO estado = estadoService.findById(id);
        return Response.ok(estado).build();
    }

    @POST
    public Response create(@Valid EstadoDTO dto) {
        EstadoDTO created = estadoService.create(dto);
        return Response.created(URI.create("/estados/" + created.id()))
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid EstadoDTO dto) {
        EstadoDTO updated = estadoService.update(id, dto);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        estadoService.delete(id);
        return Response.noContent().build();
    }
}
