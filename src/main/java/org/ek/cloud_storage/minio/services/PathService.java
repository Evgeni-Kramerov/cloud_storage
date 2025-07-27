package org.ek.cloud_storage.minio.services;

import lombok.RequiredArgsConstructor;
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

    private final Pattern SAFE_PATH_PATTERN =
            Pattern.compile("^(?:[a-zA-Z0-9/_\\.\\- ]+/?)?$");

    private final String USER_FILES_PREFIX_FORMAT = "user-%d-files/";

    /**
     * Helper method to extract ID from authenticated user
     * @param principal
     * @return
     */
    private long getUserId(Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(()->new UsernameNotFoundException("username not found"));

        return currentUser.getId();
    }

    /**
     * Returnes user specific files prefix (e.g - "user-123-files/")
     * @param principal
     * @return
     */
    public String getUserFilesPrefix(Principal principal) {
        long userId = getUserId(principal);

        return String.format(USER_FILES_PREFIX_FORMAT, userId);
    }

    /**
     * Validates a given path. Throws exceptions otherwise
     * - Must not contain ".." or backslashes
     * - Must match SAFE_PATH_PATTERN
     */
    public void validatePath(String path) throws IllegalArgumentException {

        if (path.contains("..") || path.contains("\\")) {
            throw new IllegalArgumentException("Path is not valid");
        }

        if(!SAFE_PATH_PATTERN.matcher(path).matches()){
            throw new IllegalArgumentException("Path is not valid");
        }
    }

    /**
     * Returns resource name from resource path
     * File - "file.txt"
     * Folder - "foldername/"
     * @param path
     * @return
     */
    public  String getResourceName(String path) {

        if(path.endsWith("/")){
            int secondLastSlash = path.lastIndexOf("/",path.length() - 2);
            return path.substring(secondLastSlash + 1);
        }
        else {
            int lastSlash = path.lastIndexOf("/");
            return path.substring(lastSlash + 1);
        }
    }

    /**
     * Returns full path for specific user by adding user specific prefix
     * (e.g "folder1/file.txt" -> "user-12-files/folder1/file.txt"
     * @param principal
     * @param path
     * @return
     */
    public String getFullPathForUser(Principal principal, String path) {

        validatePath(path);

        long id = getUserId(principal);

        String trimmedPath = path.startsWith("/")
                ? path.substring(1)
                : path;

        return String.format("user-%d-files/%s", id, trimmedPath);
    }

    /**
     * Returns path for parent folder of resource
     * (e.g "folder1/folder2/file.txt" -> "folder1/folder2/"
     * @param path
     * @return
     */
    public String getParentFolderPath(String path) {


        String trimmed = path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;

        int lastSlash = trimmed.lastIndexOf('/');

        return lastSlash <= 0 ? "/" : trimmed.substring(0, lastSlash + 1);
    }

    /**
     * Retunes path for the folder (e.g "folder1/folder2" -> "folder1/"
     * @param path
     * @return
     */
    public  String getFolderPath(String path) {

        int slashPositionBeforeTheLastOne = path.lastIndexOf("/", path.length() - 2);

        return removeUserPrefixFromPath(
                path.substring(0, slashPositionBeforeTheLastOne + 1)
        );
    }

    /**
     * Removes user folder from resource path (e.g. "user-21-files/folder1/file.txt"
     * -> "folder1/file.txt"
     * @param path
     * @return
     */
    public  String removeUserPrefixFromPath(String path) {

        boolean isFolder = path.endsWith("/");
        int slashCount = countSlashes(path);

        return isFolder
                ? removeUserPrefixForDirectory(path, slashCount)
                : removeUserPrefixForFile(path, slashCount);
    }

    private String removeUserPrefixForFile(String path, int slashCount) {
        int firstSlashIndex = path.indexOf("/");
        int lastSlashIndex = path.lastIndexOf("/");
        return slashCount > 1
                ? path.substring(firstSlashIndex + 1, lastSlashIndex) + "/"
                : "/";
    }

    private String removeUserPrefixForDirectory(String path, int slashCount) {
        return slashCount > 2
                ? path.substring(path.indexOf("/") + 1,path.lastIndexOf("/"))
                : "/";
    }

    /**
     * Helper function that counts number of '/' in the path
     * @param path
     * @return
     */
    private int countSlashes(String path) {
        return (int) path.chars().filter(c -> c == '/').count();
    }

}
