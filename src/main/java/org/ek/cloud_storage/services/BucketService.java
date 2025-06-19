package org.ek.cloud_storage.services;

import java.io.IOException;

public interface BucketService {

    /*
      ! Change all hardcoded to argumants!!
     */

    /*
    Ресурсы #
        *Получение информации о ресурсе
        *Удаление ресурса
        * Поиск
        * Скачивание ресурса
        * Аплоад
        * Переименование/перемещение ресурса
    * Папки
        * Создание пустой папки
        * Получение информации о содержимом папки
     */

    //**Resources

    void getResourceInfo(String path) throws IOException;

    void deleteResource(String path) throws IOException;

    void searchResource(String name) throws IOException;

    void downloadResource(String path) throws IOException;

    void uploadResource(String path) throws IOException;

    void renameResource(String oldName, String newName) throws IOException;

    //**Folders

    void createEmptyFolder(String path) throws IOException;

    void getFolderInfo(String path) throws IOException;

}
