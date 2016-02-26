### Подготовка: 
**Java** (надеюсь есть у всех), сборщик проектов **maven**.

Установка для Mac OS (у кого homebrew):

```bash
brew install maven
```

### Запуск
```bash
cd LuceneServer
mvn clean
mvn install
mvn tomcat:run
```

### Запросы
* **Создание Индекса**: 
```
http://localhost:8080/LuceneServer/server/create_index?pathtocorpus=FOLDER_WITH_TEXTS&byline=READ_BY_LINE&similarity=TYPE_OF_SIMULARITY
```
  **FOLDER_WITH_TEXTS** = /Users/ermakovpetr/projects/kaggle/texts
  
  **READ_BY_LINE** = `yes` or `no`
  
  **TYPE_OF_SIMULARITY** = `bm25` or `classic` or `lmd` or `default`
  ```js
  {"status":"Ok"}
  ```
* **Закрытие Индекса**:
```
http://localhost:8080/LuceneServer/server/close_index 
```
  ```js
  {"status":"Ok"}
  ```
* **Закрытие Score по фразу/слову**:
```
http://localhost:8080/LuceneServer/server/get_score?query=QUERY&nresult=NUM&pathtocorpus=FOLDER_WITH_TEXTS&returndocs=RETURN_DOCS
```
  **FOLDER_WITH_TEXTS** = /Users/ermakovpetr/projects/kaggle/texts
  
  **QUERY** = blood%20pressure
  
  **NUM** = 10
  
  **RETURN_DOCS** = `yes` or `no`
  ```js
  {"scoresList":[2.2025597,2.2025597,1.9272398,1.6519198,1.6519198,1.6519198,1.6519198,1.5574449,1.3765998,1.3765998],"docList":[]}
  ```

### Доп. инфа:
* Индекс создается в папке с корпусом текстов в папке `_data/index`
* Индекс создается в N потоков, где N - количество ядер.
