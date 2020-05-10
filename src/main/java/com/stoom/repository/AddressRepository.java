package com.stoom.repository;

import com.stoom.model.Address;
import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AddressRepository implements PanacheMongoRepository<Address> {
}
