# theFreya
Freya the game

###
Подключение библиотек:
1) Открыть проект нужной библиотеки, внести правки;
2) Выполнить команды Maven:

   0) [ ] dependency:purge-local-repository
   0) [ ] source:jar-no-fork install
   0) [ ] install:install-file -Dfile=H:\JavaProj\FoxLibrary\FoxGUI\target\fox-gui-6.2.82.jar -DgroupId=FoxLib39 -DartifactId=fox-gui -Dversion=6.2.82 -Dpackaging=jar -DcreateChecksum=true 

3) Готово.

Теперь в основном проекте остаётся прописать зависимость типа:
```
<dependency>
    <groupId>FoxLib39</groupId>
    <artifactId>fox-gui</artifactId>
    <version>6.2.82</version>
</dependency>```
