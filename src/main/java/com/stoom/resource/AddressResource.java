package com.stoom.resource;

import com.google.common.collect.Lists;
import com.stoom.model.Address;
import com.stoom.model.google.GoogleGeoCode;
import com.stoom.model.google.GoogleGeoResult;
import com.stoom.service.AddressService;
import com.stoom.service.GoogleMapsService;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Path("/address")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AddressResource {

    @Inject
    AddressService addressService;

    @Inject
    GoogleMapsService googleMapsService;

    @Context
    UriInfo uriInfo;

    @GET
    public Response getAddress() {
        List<Address> list = addressService.listAll();
        return Response.ok(list).build();
    }

    @GET
    @Path("{id}")
    public Response getAddress(@PathParam("id") final String id) {
        Address address = addressService.findById(new ObjectId(id));
        if(address != null ){

            try {
                GoogleGeoCode geoCode = googleMapsService.getGeoCode(address);
                GoogleGeoResult[] results = geoCode.getResults();
                List<GoogleGeoResult> googleGeoResults = Lists.newArrayList(results);
                googleGeoResults.stream().forEach(googleGeoResult -> {
                    System.out.println(googleGeoResult.getGeometry().getLocation().getLat());
                    System.out.println(googleGeoResult.getGeometry().getLocation().getLng());
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Response.ok(address).build();
    }

    @POST
    public Response saveAddress(@Valid final Address address) {
        addressService.save(address);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path("{id}")
                .resolveTemplate("id", address.getId())
                .build();
        return Response.created(location).entity(address).build();
    }

    @PUT
    @Path("{id}")
    public Response updateAddress(@PathParam("id") final String id, @Valid Address address) {
        Address update = addressService.update(new ObjectId(id), address);
        return Response.ok(update).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteAddress(@PathParam("id") final String id) {
        addressService.deleteById(new ObjectId(id));
        return Response.noContent().build();
    }
}
