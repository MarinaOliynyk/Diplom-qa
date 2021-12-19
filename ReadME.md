# Порядок запуска автотестов

***

#### Перед запуском авто-тестов, необходимо:
* Уcтановить программы "Intellij IDEA Ultimate", "Docker", "Docker-compose", для работы с контейнерами "MySQL",
  "PostgreSQL", "Node-app"

* Проверить наличие установленных версий библиотек в файле "build.gradle",
  необходимых для запуска авто-тестов

* Запустить контейнеры "MySQL", "PostgreSQL", "Node-app" в "Docker-compose"

* Запустить SUT для "MySQL" или "PostgreSQL"

1. Для запуска контейнеров "MySQL", "PostgreSQL", "Node-app", ввести в
   терминал следующую команду: ```docker-compose up -d --force-recreate```
2. Запустить SUT командой
* Для MySQL: ```java -Dspring.datasource.url=jdbc:mysql://localhost:3306/app -jar artifacts/aqa-shop.jar```
* Для PostgreSQL: ```java -Dspring.datasource.url=jdbc:postgresql://localhost:5432/app -jar artifacts/aqa-shop.jar```
3. Запустить тесты командой:
* Для MySQL: ```./gradlew clean test -Ddb.url=jdbc:mysql://localhost:3306/app -Dlogin=app -Dpassword=pass -Dapp.url=http://localhost:8080```
* Для PostgreSQL: ```./gradlew clean test -Ddb.url=jdbc:postgresql://localhost:5432/postgres -Dlogin=app -Dpassword=pass -Dapp.url=http://localhost:8080```
4. Для запуска и просмотра отчета "Allure" по результатам тестирования выполнить команду:
   ```./gradlew allureReport```, затем ```./gradlew allureServe```

[![Build status](https://ci.appveyor.com/api/projects/status/yujhh53u0nujgp0x?svg=true)](https://ci.appveyor.com/project/MarinaOliynyk/diplom-qa)