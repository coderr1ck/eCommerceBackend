package com.coderrr1ck.backend.address;

import com.coderrr1ck.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findByUserAndActiveTrue(User user);
    Optional<Address> findByAddressIdAndUser(Integer addressId, User user);
}
