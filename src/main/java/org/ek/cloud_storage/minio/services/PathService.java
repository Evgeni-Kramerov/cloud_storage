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

    private final Pattern SAFE_PATH_PATTERN = Pattern.compile("^(?:[a-zA-Z0-9/_\\.\\-]+/?)?$");

    public String getUserPrefix(Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(()->new UsernameNotFoundException("username not found"));

        long id = currentUser.getId();

        return "user-" + id + "-files/";
    }

    public  String getFolderPath(String path) {

        return removeUserFolderFromPath(path.substring(0, path.lastIndexOf("/",path.length()-2) + 1));
    }

    public  String getResourceName(String path) {
        if(path.endsWith("/"))
            {
                return  path.substring(path.lastIndexOf("/",path.length()-2) + 1);
            }
        else
            {
                return path.substring(path.lastIndexOf("/") + 1);
            }
    }

    public  String removeUserFolderFromPath(String path) {

        //for folders
        if (path.endsWith("/"))
            {
                if (pathContainsMoreThenAmountSlashes(path,2)){
                    return path.substring(path.indexOf("/")+1,path.lastIndexOf("/"));
                }

                // for folders in home directory
                else {
                    return "/";
                }
            }

        //for files
        else
            {
                if (pathContainsMoreThenAmountSlashes(path,1)){
                    return path.substring(path.indexOf("/")+1, path.lastIndexOf("/")) + "/";
                }
                //for files in home directory - path only "/"
                else {
                    return "/";
                }
            }
    }

    private boolean pathContainsMoreThenAmountSlashes(String path, int slashQuantity) {
        int count = 0;
        for (char c : path.toCharArray()) {
            if (c == '/') {
                count++;
            }
        }
        return count > slashQuantity;
    }

    public String fullPathForUser(Principal principal, String path) {

        System.out.println("Path from service " + path);

        validatePath(path);

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(()->new UsernameNotFoundException("username not found"));

        long id = currentUser.getId();

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String fullPath = "user-" + id + "-files/" + path;


        return fullPath;
    }


    public void validatePath(String path) throws IllegalArgumentException {

        if (path.contains("..") || path.contains("\\")) {
            throw new IllegalArgumentException("path is not valid");
        }

        if(!SAFE_PATH_PATTERN.matcher(path).matches()){
            throw new IllegalArgumentException("path is not valid");
        }
    }

    public String getParentFolderPath(String path) {
        if (!path.contains("/") || path.indexOf("/") == path.lastIndexOf("/")) {
            return "/";
        }
        return path.substring(0, path.substring(0,path.length()-1).lastIndexOf("/"));

    }
}
