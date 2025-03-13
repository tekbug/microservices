package com.athena.v2.teachers.utils;

import com.athena.v2.libraries.dtos.requests.AddressRequestDTO;
import com.athena.v2.libraries.dtos.requests.PhoneNumberRequestDTO;
import com.athena.v2.libraries.dtos.requests.UserRequestDTO;
import com.athena.v2.libraries.dtos.responses.AddressResponseDTO;
import com.athena.v2.libraries.dtos.responses.PhoneNumberResponseDTO;
import com.athena.v2.libraries.dtos.responses.UserResponseDTO;
import com.athena.v2.libraries.enums.UserStatus;
import com.athena.v2.libraries.utils.IdGenerator;
import com.athena.v2.users.models.Address;
import com.athena.v2.users.models.PhoneNumber;
import com.athena.v2.users.models.Users;
import com.athena.v2.users.repositories.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ObjectMappers {

    private final UsersRepository usersRepository;

    @Transactional
    public Users mapUsersToDatabase(UserRequestDTO requestDTO) {
        Users user = new Users();
        String prefix = switch (requestDTO.userRoles()) {
            case STUDENT -> "STU";
            case TEACHER -> "TEA";
            case ADMINISTRATOR -> "ADM";
            case DEAN -> "DEA";
            case DEPARTMENT_HEAD -> "DEP";
            case PROGRAM_COORDINATOR -> "PGC";
        };
        user.setUserId(IdGenerator.generateUserId(prefix));
        user.setUsername(requestDTO.username());
        user.setEmail(requestDTO.email());
        user.setFirstName(requestDTO.firstName());
        user.setMiddleName(requestDTO.middleName());
        user.setLastName(requestDTO.lastName());
        user.setUserRoles(requestDTO.userRoles());
        user.setUserStatus(UserStatus.PENDING);
        user.setPhoneNumbers(mapPhoneNumbersToDatabase(requestDTO.phoneNumbers()));
        user.setAddressList(mapAddressesToDatabase(requestDTO.addresses()));
        usersRepository.saveAndFlush(user);
        log.info("User has been successfully created. {}", user);
        return user;
    }

    public List<PhoneNumber> mapPhoneNumbersToDatabase(List<PhoneNumberRequestDTO> phoneNumbers) {
        return phoneNumbers.stream()
                .map(dto -> {
                    PhoneNumber phoneNumber = new PhoneNumber();
                    phoneNumber.setPhoneNumber(dto.phoneNumber());
                    return phoneNumber;
                })
                .collect(Collectors.toList());
    }

    public List<Address> mapAddressesToDatabase(List<AddressRequestDTO> addresses) {
        return addresses.stream()
                .map(addressRequestDTO -> {
                    Address address = new Address();
                    address.setCity(addressRequestDTO.city());
                    address.setSubCity(addressRequestDTO.subCity());
                    address.setPostalCode(addressRequestDTO.postalCode());
                    address.setStreetName(addressRequestDTO.streetName());
                    address.setHouseNo(addressRequestDTO.houseNo());
                    return address;
                })
                .collect(Collectors.toList());
    }

    public UserResponseDTO mapUserFromDatabase(Users user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .userRoles(user.getUserRoles())
                .userStatus(user.getUserStatus())
                .phoneNumbers(user.getPhoneNumbers()
                        .stream()
                        .map(phoneNumber ->
                                new PhoneNumberResponseDTO(phoneNumber.getPhoneNumber()))
                        .collect(Collectors.toList()))
                .addresses(user.getAddressList().stream()
                        .map(address -> new AddressResponseDTO(
                                address.getCity(),
                                address.getSubCity(),
                                address.getPostalCode(),
                                address.getStreetName(),
                                address.getHouseNo()
                        ))
                        .collect(Collectors.toList())
                )
                .build();
    }
}
