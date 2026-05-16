package com.coderrr1ck.backend.address;

import com.coderrr1ck.backend.user.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final AddressMapper mapper;

    public AddressResponse saveAddress(User user, AddressRequest addressRequest) {
        Address addresstoSave = mapper.toAddress(addressRequest);
        addresstoSave.setUser(user);
        return mapper.toAddressResponse(addressRepository.save(addresstoSave));
    }


    public AddressResponse updateAddress(Integer id, AddressRequest addressRequest, User user) {
        Address savedAddress = addressRepository.findByAddressIdAndUser(id,user)
                .orElseThrow(() -> new AddressNotFound("Address not found for id: " + id));
        if(!savedAddress.isActive()){
            throw new AddressNotFound("Address not found for id: " + id );
        }
        Address updatedAddress = mapper.mapAddressRequestToAddress(addressRequest,savedAddress);
        return mapper.toAddressResponse(addressRepository.save(updatedAddress));
    }

    public void deleteAddress(Integer id, User user) {
        Address savedAddress = addressRepository.findByAddressIdAndUser(id,user)
                .orElseThrow(() -> new AddressNotFound("Address not found for id: " + id));
        if(!savedAddress.isActive()){
            throw new AddressNotFound("Address not found for id: " + id );
        }
        savedAddress.setActive(false);
        addressRepository.save(savedAddress);
    }

    public AddressResponse getAddressById(Integer id, User user) {
        Address savedAddress = addressRepository.findByAddressIdAndUser(id, user)
                .orElseThrow(() -> new AddressNotFound("Address not found for id: " + id));
        if (!savedAddress.isActive()) {
            throw new AddressNotFound("Address not found for id: " + id);
        }
        return mapper.toAddressResponse(savedAddress);
    }

    public List<AddressResponse> getMyAddresses(User user) {
        List<Address> savedAddresses = addressRepository.findByUserAndActiveTrue(user);
        List<AddressResponse> responseList = savedAddresses.stream().map(mapper::toAddressResponse).toList();
        return responseList;
    }
}
