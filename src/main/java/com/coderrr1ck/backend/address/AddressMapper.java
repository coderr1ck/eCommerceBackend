package com.coderrr1ck.backend.address;

import org.springframework.stereotype.Service;

@Service
public class AddressMapper {

    public Address toAddress(AddressRequest addressRequest) {
        Address address = new Address();
        address.setFullName(addressRequest.getFullName());
        address.setPhoneNumber(addressRequest.getPhoneNumber());
        address.setLine1(addressRequest.getLine1());
        address.setLine2(addressRequest.getLine2());
        address.setLandmark(addressRequest.getLandmark());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setCountry(addressRequest.getCountry());
        address.setPostalCode(addressRequest.getPostalCode());
        return address;
    }

    public AddressResponse toAddressResponse(Address address) {
        AddressResponse addressResponse = new AddressResponse();
        addressResponse.setId(address.getAddressId());
        addressResponse.setFullName(address.getFullName());
        addressResponse.setPhone(address.getPhoneNumber());
        addressResponse.setLine1(address.getLine1());
        addressResponse.setLine2(address.getLine2());
        addressResponse.setLandmark(address.getLandmark());
        addressResponse.setCity(address.getCity());
        addressResponse.setState(address.getState());
        addressResponse.setCountry(address.getCountry());
        addressResponse.setPincode(address.getPostalCode());
        return addressResponse;
    }

    public Address mapAddressRequestToAddress(AddressRequest addressRequest, Address address) {
        address.setFullName(addressRequest.getFullName());
        address.setPhoneNumber(addressRequest.getPhoneNumber());
        address.setLine1(addressRequest.getLine1());
        address.setLine2(addressRequest.getLine2());
        address.setLandmark(addressRequest.getLandmark());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setCountry(addressRequest.getCountry());
        address.setPostalCode(addressRequest.getPostalCode());
        return address;
    }
}
