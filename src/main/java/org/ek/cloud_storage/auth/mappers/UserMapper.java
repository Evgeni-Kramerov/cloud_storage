package org.ek.cloud_storage.auth.mappers;

import org.ek.cloud_storage.auth.domain.UserDetailsRequestDTO;
import org.ek.cloud_storage.auth.domain.UserResponseDTO;
import org.ek.cloud_storage.auth.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    User registrationRequesttoUser(UserDetailsRequestDTO userDetailsRequestDTO);

    UserResponseDTO usertoUserResponseDTO(User user);
}
