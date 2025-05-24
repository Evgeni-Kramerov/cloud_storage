package org.ek.cloud_storage.services;

import java.io.IOException;

public interface BucketService {

    /*
    ! All with resources
    ! Update to Folders
     */

    /*
    Ресурсы #
        *Получение информации о ресурсе
        *Удаление ресурса
        Поиск
        Скачивание ресурса
        Переименование/перемещение ресурса
        Аплоад
    * Папки
        * Создание пустой папки
        * Получение информации о содержимом папки
     */

    //**Resources

    void getResourceInfo(String path) throws IOException;

    void deleteResource(String path) throws IOException;

    void searchResource(String name) throws IOException;


    //**Folders

    void createEmptyFolder(String path) throws IOException;

    void getFolderInfo(String path) throws IOException;

}
