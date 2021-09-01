package io.qiot.manufacturing.datacenter.plantmanager.rest;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import io.qiot.manufacturing.datacenter.plantmanager.domain.dto.MachineryDTO;
import io.qiot.manufacturing.datacenter.plantmanager.service.MachineryService;

/**
 * Validation through hibernate validator:
 * https://quarkus.io/guides/validation#rest-end-point-validation
 * 
 * @author andreabattaglia
 *
 */
@Path("/station")
public class MachineryResource {

    @Inject
    Logger LOGGER;

    @Inject
    MachineryService service;

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/all")
    public List<MachineryDTO> getAll() {
        return service.getAllStations();
    }

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/id/{id}")
    public MachineryDTO getById(@PathParam("id") @NotNull UUID id) {
        return service.getById(id);
    }

    @Transactional
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String add(@QueryParam("serial") @NotNull String serial,
            @QueryParam("name") @NotNull String name,
            @QueryParam("factoryId") UUID factoryId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("register(String) - start");
        }

        String id = service.add(serial, name, factoryId);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("register(String) - end");
        }
        return id;
    }

}