package com.example.resource;

import com.example.service.RegiaoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/regioes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMINISTRADOR", "ATENDENTE", "PSICOLOGO", "PACIENTE"})
public class RegiaoResource {

    @Inject
    RegiaoService regiaoService;

    @GET
    public Response getAll() {
        return Response.ok(regiaoService.findAll()).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(regiaoService.findById(id)).build();
    }
}
