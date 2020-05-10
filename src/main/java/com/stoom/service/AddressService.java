package com.stoom.service;

import com.google.common.collect.Lists;
import com.stoom.model.Address;
import com.stoom.model.google.GoogleGeoCode;
import com.stoom.model.google.GoogleGeoResult;
import com.stoom.repository.AddressRepository;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import java.util.List;


@ApplicationScoped
public class AddressService {

    @Inject
    AddressRepository addressRepository;

    @Inject
    GoogleMapsService googleMapsService;

    public List<Address> listAll(){
        return Address.listAll();
    }

    public Address findById(ObjectId id){
        return addressRepository.findById(id);
    }

    @Transactional
    public void deleteById(ObjectId id) {
        Address address = addressRepository.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("Endereço nao encontrado",404));
        addressRepository.delete(address);
    }

    @Transactional
    public Address update(ObjectId id, Address address){
        return addressRepository.findByIdOptional(id)
                .map(existente -> {
                    existente.setStreetName(address.getStreetName());
                    existente.setNumber(address.getNumber());
                    existente.setComplement(address.getComplement());
                    existente.setNeighbourhood(address.getNeighbourhood());
                    existente.setCity(address.getCity());
                    existente.setCountry(address.getCountry());
                    existente.setState(address.getState());
                    existente.setZipcode(address.getZipcode());
                    if(StringUtils.isEmpty(address.getLatitude()) || StringUtils.isEmpty(address.getLongitude())) {
                        Address adr = setLatitudeLongitude(address);
                        existente.setLatitude(adr.getLatitude());
                        existente.setLongitude(adr.getLongitude());
                    } else {
                        existente.setLatitude(address.getLatitude());
                        existente.setLongitude(address.getLongitude());
                    }
                    addressRepository.update(existente);
                    return existente;
                })
                .orElseThrow(() -> new WebApplicationException("Endereço nao encontrado",404));
    }

    @Transactional
    public Address save(Address address) {
        if(StringUtils.isEmpty(address.getLatitude()) || StringUtils.isEmpty(address.getLongitude())) {
            address = setLatitudeLongitude(address);
        }
        addressRepository.persist(address);
        return address;
    }

    private Address setLatitudeLongitude(Address address) {
        try{
            GoogleGeoCode geoCode = googleMapsService.getGeoCode(address);
            GoogleGeoResult[] results = geoCode.getResults();
            List<GoogleGeoResult> googleGeoResults = Lists.newArrayList(results);
            googleGeoResults.stream().forEach(googleGeoResult -> {
                address.setLatitude(googleGeoResult.getGeometry().getLocation().getLat());
                address.setLongitude(googleGeoResult.getGeometry().getLocation().getLng());
            });
        }catch (Exception e) {
            System.out.println(e);
        }
        return address;
    }
}
