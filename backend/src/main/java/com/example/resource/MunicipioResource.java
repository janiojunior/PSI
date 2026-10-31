package com.example.resource;

import com.example.dto.MunicipioDTO;
import com.example.service.MunicipioService;
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

@Path("/municipios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MunicipioResource {

    @Inject
    MunicipioService municipioService;

    @GET
    public Response getAll() {
        List<MunicipioDTO> municipios = municipioService.findAll();
        return Response.ok(municipios).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        MunicipioDTO municipio = municipioService.findById(id);
        return Response.ok(municipio).build();
    }

    @POST
    public Response create(@Valid MunicipioDTO dto) {
        MunicipioDTO created = municipioService.create(dto);
        return Response.created(URI.create("/municipios/" + created.id()))
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid MunicipioDTO dto) {
        MunicipioDTO updated = municipioService.update(id, dto);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        municipioService.delete(id);
        return Response.noContent().build();
    }
}
