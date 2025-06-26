package org.ek.cloud_storage.services;

import java.io.IOException;

public interface BucketService {

    //**Resources

    void getResourceInfo(String path) throws IOException;

    void deleteResource(String path) throws IOException;

    void searchResource(String name) throws IOException;

    void downloadResource(String pathCloudObject, String pathLocalObject) throws IOException;

    void uploadResource(String pathWhereToUpload, String pathToLocalObject) throws IOException;

    void renameResource(String oldPath, String newPath) throws IOException;

    //**Folders

    void createEmptyFolder(String path) throws IOException;

    void getFolderInfo(String path) throws IOException;

}
