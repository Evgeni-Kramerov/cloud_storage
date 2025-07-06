package org.ek.cloud_storage.minio.services;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.ek.cloud_storage.auth.domain.User;
import org.ek.cloud_storage.auth.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PathService {

    private final UserRepository userRepository;

    private static final Pattern SAFE_PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9/_\\-]+/?$");

    public String fullPathForUser(Principal principal, String path) {

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(()->new UsernameNotFoundException("username not found"));

        long id = currentUser.getId();

        return "user-" + id + "-files/" + path;
    }

    public void validatePath(String path) throws IllegalArgumentException {

        if(path == null || path.isEmpty()){
            throw new IllegalArgumentException("path is null or empty");
        }

        if (path.contains("..") || path.contains("\\")) {
            throw new IllegalArgumentException("path is not valid");
        }

        if(!SAFE_PATH_PATTERN.matcher(path).matches()){
            throw new IllegalArgumentException("path is not valid");
        }

    }
}
