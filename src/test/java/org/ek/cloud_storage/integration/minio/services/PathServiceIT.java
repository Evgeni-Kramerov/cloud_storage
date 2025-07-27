package org.ek.cloud_storage.integration.minio.services;

import org.ek.cloud_storage.auth.repositories.UserRepository;
import org.ek.cloud_storage.minio.services.PathService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;

@SpringBootTest(classes = PathService.class)
public class PathServiceIT {

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private PathService pathService;

    @Test
    void should_return_parent_folder_path_when_given_full_path() throws IOException {
        String path = "user-details/folder1/folder2/folder3/";

        String folderPath = pathService.getFolderPath(path);

        assertEquals("folder1/folder2/", folderPath);

    }

    @Test
    void should_return_folder_name_when_given_full_path() throws IOException {
        String path = "user-details/folder1/folder2/folder3/";

        String folderName = pathService.getResourceName(path);

        assertEquals("folder3", folderName);

    }

    @Test
    void should_return_path_without_userid_when_given_full_path() throws IOException {
        String path = "user-details/folder1/folder2/folder3/";

        String pathWithoutUserDetails = pathService.removeUserPrefixFromPath(path);

        assertEquals("folder1/folder2/folder3/", pathWithoutUserDetails);
    }


}
