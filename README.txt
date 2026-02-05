En este proyecto hemos realizado la migración completa de una aplicación CRUD con una base de datos relacional (SQL/MySQL) a una base de datos NoSQL (MongoDB).

El objetivo de la migración era mantener el comportamiento funcional de la aplicación, pero aprovechando las características de Mongo.

En SQL, los datos estaban repartidos en varias tablas (perfil, usuario, administrador, etc.).
En MongoDB hemos unificado esta estructura en una única colección users, manteniendo los datos de la pasada base de datos.

Hemos repartido el trabajo y cada uno ha creado nuevas consultas para MongoDB.

IMPORTANTE!!!!!

Para iniciar la aplicación es necesario importar users_manager.users.json de la carpeta CRUD_G1_MongoDB/mongoDB en una base de datos llamada users_manager dentro de una colección llamada users. 

Además se debe ejecutar en MongoDB shell las últimas líneas que se mencionan como obligatorias en BD_Reto_Crud.js. 

También se puede ejecutar todo consulta por consulta como se indica en el mismo BD_Reto_Crud.js

De esta manera la base de datos estará lista para abrir CRUD_Gi1_Proyecto\ADTi_DIN_Reto_CRUD en NetBeans y hacer run.