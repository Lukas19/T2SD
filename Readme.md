## Tarea 2 Sistemas Distribuidos 

Ignacio Valenzuela 201473
Lukas Zamora 201430047-6

Para ejecutar un nodo cualquiera se debe ejecutar:

```bash
java Process <ID1> <ID2,ID5,ID6> <false> 
java -Djava.security.manager -Djava.security.policy=my.policy Process <ID1> <ID2,ID5,ID6> <true> <ruta> <ip> 10.10.2.214
```
 * Notar que cuando se lance el nodo representante se debe usar la segunda línea descrita anteriormente.

 1) Se ocupó algoritmo eco tanto para reconocer al representante (coordinador) como para enviar el mensaje descifrado a los demás procesos, aunque este último con una pequeña variación.
    - El algoritmo eco de elección funciona principalmente tomando a un proceso candidato que comienza el algoritmo, mandando mensajes explorer a los demás. Los procesos al recibir el mensaje, tomarán a ese proceso como el "parent" y enviarán mas exploradores a sus vecinos. Cuando un proceso no tenga mas vecinos que el parent, reenvia un mensaje de OK para indicar que esta listo. Esto termina hasta que el proceso candidato recibe todos los OK de sus vecinos.
    - Para la transmisión del mensaje se usó la misma implementación eco, pero en este caso los procesos no esperan que los demás respondan con un OK, es decir, los procesos asumen que el mensaje fue recibido por sus vecinos. Además, cuando un proceso recibe un mensaje, una variable local se hace 1 para evitar que, si otro proceso le envía también el mensaje se repita el procedimiento, y quizás imprimiendo varias veces el mismo mensaje.
