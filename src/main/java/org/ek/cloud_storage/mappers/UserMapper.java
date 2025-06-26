package org.ek.cloud_storage.mappers;

import org.ek.cloud_storage.domain.dto.UserDetailsRequestDTO;
import org.ek.cloud_storage.domain.dto.UserResponseDTO;
import org.ek.cloud_storage.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    User registrationRequesttoUser(UserDetailsRequestDTO userDetailsRequestDTO);

    UserResponseDTO usertoUserResponseDTO(User user);
}
