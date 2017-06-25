## Reactive Drawing Application

Cette application permet de se familiariser avec différentes fonctionnalités de Spring 5.
Ce fichier présente les différents exercices qui permettent de reconstruire l'application.
Les dossiers `nc-spring-webflux` et `nc-spring-webmvc` contiennent un exemple de résultat recherché.

## L'application

L'application est une interface WEB de construction et partage de dessins.
Lorsque l'utilisateur se connecte, son nom lui est demandé pour démarrer un dessin.
Via une palette, l'utilisateur dispose de différents outils pour dessiner.
Une fois son dessin terminé, il peut ajouter son dessin à une liste contenant les dessins de tous les utilisateurs connectés à l'application.
Dès qu'un dessin est ajouté, il apparaît dans la liste des dessins de tous les utilisateurs.

## Architecture

Le schema d'architecture peut se présenter ainsi :

```
                     +-------------+
                     |             |
                +--->|   MongoDB   |<---+
                |    |             |    |
 Tailable cursor|    +-------------+    | Load all images
                |                       |
                |                       |
   Push new +-------------+  +-------------+
   image ID |             |  |             |
    +-------|   WebFlux   |  |    WebMvc   |<---+
    |       |             |  |             |    |
    |       +-------------+  +-------------+    |
    |                ^            ^             |
    |     Add image  |            |HTTP/2       |
    |     Open SSE   |            |Load statics |
    |     connection |            |Load images  |       
    |                |            |             |
    |               +---------------+           |
    |               |               |HTTP/2 push|
    +-------------> |    Browser    |-----------+
                    |               |
                    +---------------+
```

* Les images sont stockées dans `MongoDB` en base 64, elles seront chargées par les modules WEB
* Un premier module est basé sur `WebFlux` correspond à l'API de l'application:
    * Le navigateur y ouvre une connexion SSE pour recevoir les images
    * Afin d'envoyer les URLs des images, le module `WebFlux` [crée un tailable cursor](https://docs.mongodb.com/manual/core/tailable-cursors/)
    * Un service WEB permet l'ajout d'une nouvelle image qui sera naturellement reçue via le `tailable cursor` dès son enregistrement
* Un second module basé sur `WebMvc` permet de servir les statics:
    * Un service WEB permet de charger une image en fonction de son ID.
    * Le navigateur s'y connecte en HTTP/2
    * Lorsque la page HTML est retournée, le module utiliser le HTTP/2 `server-push` pour envoyer le contenu des images existantes

## Pré-requis

* Diposer d'un JDK 8
* Avoir un accès à une instance MongoDB 3
* Avoir une connexion internet
* Utiliser Eclispe ou IntelliJ de préférence

## Resources utiles

Les ressources suivantes contiennent différents exemples qui permettent de réaliser les exercices.
Elles sont citées dans un ordre cohérent avec celui des exercices.

* Spring initalizr: https://start.spring.io/
* Reactor Getting started guide: https://github.com/reactor/reactor-core#getting-started
* Spring WebFlux: https://spring.io/blog/2016/07/28/reactive-programming-with-spring-5-0-m1
* WebTestClient: https://spring.io/blog/2017/02/23/spring-framework-5-0-m5-update
* Reactive Spring Data: https://spring.io/blog/2016/11/28/going-reactive-with-spring-data
* Reactive Mongo Template: http://docs.spring.io/spring-data/data-mongo/docs/2.0.0.M4/reference/html/#mongo.reactive.template
* Exemple HTTP/2: https://github.com/bclozel/http2-experiments
* Web functional framework: https://spring.io/blog/2016/09/22/new-in-spring-5-functional-web-framework
* Kotlin support: https://spring.io/blog/2017/01/04/introducing-kotlin-support-in-spring-framework-5-0

## Exercices

### Le module WebFlux

Dans cet exercice, nous allons déclarer le contrat d'interface de l'API, les mapings et tests unitaires.
Pour initialiser le module, allez sur `spring initializr` et sélectionnez `Spring Boot 2`.
Ajoutez les modules et `Reactive Web` et `Reactive Mongo`, conservez `Maven`.
Téléchargez l'archive ZIP et ouvrez le projet dans votre IDE.

Créer les objets du domaine:
* Une classe `DrawingInfo` annotée `@Document(collection = "drawings")` qui contient deux attributs de type String `id` et `author`
* Une classe `Drawing` qui étend `DrawingInfo` avec un attribut de type String `base64Image`

Créez l'interface `ReactiveDrawingController` avec les méthodes suivantes:

* `Mono<String> add(Mono<Drawing> drawing)`
* `Flux<DrawingInfo> getDrawings()`

Il faut maintenant déclarer les mappings.
Ils se font traditionnellement avec les annotations `GetMapping`, `RequestMapping`, etc.
Nous allons plutôt utiliser un bean de type `RouterFunction` pour câbler les méthodes du contrôleur avec les URIs.

Créez une classe de configuration `@Configuration` avec une méthode `@Bean public RouterFunction<ServerResponse> routingFunction(ReactiveDrawingController)`.
Utilisez les méthodes statiques de `RouterFunctions` pour créer deux mappings:

* Un premier qui accepte une requête en `GET` sur `/drawings` et qui envoit en réponse `ReactiveDrawingController#getDrawings()` et `MediaType.TEXT_EVENT_STREAM` comme `Content-Type`.
* Un second qui accepte une requête en `POST` sur `/drawing` qui envoit en réponse `ReactiveDrawingController#add(Mono<Drawing>)`. La requête et la réponse sont en `JSON`.

Nous allons maintenant créer des tests unitaires.
Créez une classe de test `@SpringBootTest`.

Déclarez une classe interne statique `@Configuration` dans laquelle vous créez un bean de type `ReactiveDrawingController`.
Appuyez-vous sur `Mockito` pour retourner un `Flux` lors de l'appel de `getDrawings()`.
Pensez à importer votre classe de configuration qui créee le `RouterFunctions` via `@Import`.

Avant d'écrire votre premier test, il vous faut un `WebTestClient` en tant qu'attribut.
Regardez la [Javadoc](https://docs.spring.io/spring/docs/5.0.x/javadoc-api/org/springframework/test/web/reactive/server/WebTestClient.html) pour trouver comment créer une instance de `WebTestClient` qui tient compte de votre `RouterFunction`.

Ecrire une méthode `@Test` qui utilise le `WebTestClient` pour envoyer une requête sur `/drawings`.
Manipulez l'API pour vérifier que le statut de la réponse est `ok` et qu'il contient bien les éléments du `Flux` dans votre `mock`.

### Le module WebMvc

Dans cet exercice, nous allons créer le module web qui va fournir les `statics`.

Pour initialiser le module, allez sur `spring initializr` et sélectionnez `Spring Boot 2`.
Ajoutez les modules et `Web` et `Reactive Mongo`, conservez `Maven`.
Télécharger l'archive ZIP et ouvrez le projet dans votre IDE.

Créez les classes du domaine `Drawing` et `DrawingInfo` comme dans `WebFlux` (on s'autorisera un peu de duplication plutôt que de créer un module commun).

Créer maintenant une classe `DrawingController` et injectez dans le constructeur un `ReactiveMongoTemplate`.

Implémenter un service web sur l'URI `/drawing/{id}` qui:
* Prend en paramètre l'ID du dessin (type String)
* Retourne un tableau de `byte` (precisez dans `@GetMapping` l'attribut `produces=MediaType.IMAGE_PNG_VALUE` et ajoutez l'annotation `ResponseBody`)
* Utilise `ReactiveMongoTemplate#findById()` pour charger un dessin de type `Drawing` et le map en tableau de `byte`

Implémentez une seconde méthode avec un mapping sur `/`:
* Elle prend en paramètre le `PushBuilder` l'API servlet 4
* Utilisez `ReactiveMongoTemplate#find()` pour charger la liste des dessins via le type `DrawingInfo` et effectuez un `server-push`  sur chacune des URLs d'image obtenues à partir des ID des dessins.
* Retourne la String `drawing.html`

Enfin créer une classe `@Configuration` dans laquelle vous:
* ajoutez la vue `drawing`: https://github.com/gdrouet/nitghtclazz-spring5/blob/master/nc-spring-webmvc/src/main/java/com.zenika/WebmvcConfig.java#L22-L25
* configurez le client `MongoDB`: https://github.com/gdrouet/nitghtclazz-spring5/blob/master/nc-spring-webmvc/src/main/java/com.zenika/WebmvcConfig.java#L43-L46

Dans `src/main/resources`, décompressez le contenu de cette archive: https://minhaskamal.github.io/DownGit/#/home?url=https://github.com/gdrouet/nitghtclazz-spring5/tree/master/nc-spring-webmvc/src/main/resources
Vous y trouverez:

* Un répertoire `static` dont le contenu sera exposé par `Spring Boot`. C'est la partie `front` de l'application.
* Un `application.yml` servant de base à la configuration HTTPS
* Un `keystore` référencé dans `applicationn.yml`

`HTTP/2` ne fonctionne qu'en HTTPs. 
C'est pour cela qu'il faut avoir les éléments de configuration nécessaires à l'activation de HTTPs.

A noter également que l'application envoi des requêtes vers l'API du module `WebFlux`.
Si la page est chargée en HTTPs, alors `WebFlux` doit aussi écouter en HTTPs.
Nous allons donc également configurer HTTPs pour `WebFlux`.

### Activer HTTP/2 et HTTPs

Téléchargez et décompressez dans le répertoire `src/main/resources` du module `WebFlux` le contenu de cette archive: https://minhaskamal.github.io/DownGit/#/home?url=https://github.com/gdrouet/nitghtclazz-spring5/tree/master/nc-spring-webflux/src/main/resources 
Vous y trouverez des informations de même nature que celles concernant HTTPs dans le répertoire du module `WebMvc`.

Par défaut, `Spring Boot` doit pouvoir automatiquement configurer HTTPs avec les informations présentes, mais:
* Avec `WebMvc` cela sera en `HTTP/1.1` alors que nous avons besoin de HTTP/2.
* Avec `WebFlux`, l'auto-configuration en HTTPs n'est pas encore implémentée.

Il faut donc substituer un bean de configuration spécifique qui accède à l'API native du conteneur de servlet.
Nous allons utiliser `Jetty` dans les deux modules.

Modifier le `pom.xml` de `WebFlux` comme ceci:

```
    <properties>
        <jetty.version>10.0.0-SNAPSHOT</jetty.version>
    </properties>

    ...

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-netty</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jetty</artifactId>
    </dependency>
    
    ...
    
    <repositories>
        <repository>
            <id>jetty-snapshots</id>
            <name>Jetty Snapshots</name>
            <url>https://oss.sonatype.org/content/repositories/jetty-snapshots</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
```

Modifier le `pom.xml` de `WebMvc` comme ceci (il faut plus de modules pour `HTTP/2`):

```
    <properties>
        <jetty.version>10.0.0-SNAPSHOT</jetty.version>
        <!-- https://www.eclipse.org/jetty/documentation/current/alpn-chapter.html#alpn-versions -->
        <alpn-version>8.1.11.v20170118</alpn-version>
    </properties>

    ...

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-tomcat</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <version>4.0.0-b05</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jetty</artifactId>
    </dependency>
    <dependency>
        <groupId>org.eclipse.jetty.http2</groupId>
        <artifactId>http2-server</artifactId>
        <version>${jetty.version}</version>
    </dependency>
    <dependency>
        <groupId>org.eclipse.jetty</groupId>
        <artifactId>jetty-alpn-server</artifactId>
        <version>${jetty.version}</version>
    </dependency>
    <dependency>
        <groupId>org.eclipse.jetty</groupId>
        <artifactId>jetty-jndi</artifactId>
        <version>${jetty.version}</version>
    </dependency>
    <dependency>
        <groupId>org.mortbay.jetty.alpn</groupId>
        <artifactId>alpn-boot</artifactId>
        <version>${alpn-version}</version>
        <scope>provided</scope>
    </dependency>
    
    ...
    
    <repositories>
        <repository>
            <id>jetty-snapshots</id>
            <name>Jetty Snapshots</name>
            <url>https://oss.sonatype.org/content/repositories/jetty-snapshots</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
```

Notez que nous utilisons la version 10 de `Jetty`, toujours en `snapshot`.
Cela nous permettra d'exploiter l'API servlet 4 afin de faire du `server-push` avec `HTTP/2`.
Regardez également la version utilisée pour `ALPN` et assurez-vous qu'elle est compatible avec votre JDK.

Il faut maintenant déclarer les beans de configuration.
Pour `WebFlux`, il faut configurer un `ReactiveWebServerFactory`:
* https://github.com/gdrouet/nitghtclazz-spring5/blob/master/nc-spring-webflux/src/main/java/com/zenika/config/ReactiveWebServerFactoryConfig.java
* https://github.com/gdrouet/nitghtclazz-spring5/blob/master/nc-spring-webflux/src/main/java/com/zenika/config/CustomJettyReactiveWebServerFactory.java

Pour `WebMvc`, on peut passer par un `WebServerFactoryCustomizer`:
* https://github.com/gdrouet/nitghtclazz-spring5/blob/master/nc-spring-webmvc/src/main/java/com.zenika/JettyHttp2Customizer.java
* https://github.com/gdrouet/nitghtclazz-spring5/blob/master/nc-spring-webmvc/src/main/java/com.zenika/WebmvcConfig.java#L33-L36

### Lancer WebMvc

Nous allons maintenant configurer les pramaètres de lancement de `WebMvc`.
Dans votre IDE, ajustez les options de la JVM avec le chemin du JAR ALPN:
`-Xbootclasspath/p:<<MAVEN_LCOAL_REPO>>/org/mortbay/jetty/alpn/alpn-boot/<<version>>/alpn-boot-<<version>>.jar`

Nous allons également passer en argument les informations nécessaires pour écouter sur le port `8443` et se connecter à `MongoDB`.
* `--spring.data.mongodb.uri=mongodb://<<user>>:<<password>>@<<host>>:<<port>>/<<database>>`
* `--server.port=8443`

A ce stade, vous devriez pouvoir démarrer le module `WebMvc`.
Néanmoins, l'application doit s'appuyer sur le module `WebFlux`.
Nous allons maintenant le démarrer.

### Lancer WebFlux

Le module est correctement configuré pour écouter en HTTPs.
Nous avons néanmoins deux problèmes que nous allons traiter:

* `ReactiveDrawingController` n'est pas implémenté (il a été bouchonné pour les tests unitaires)
* Les requêtes envoyées par le navigateur viennent d'un port différent, il faut configurer le `CORS`
 
Créez une classe `ReactiveDrawingControllerImpl`.
Assurez-vous qu'elle est bien injectée dans le contexte Spring tout en étant ignorée dans les tests unitaires.

Pour récupérer les dessins dans un flux continu qui alimentera la connexion SSE, utilisez `ReactiveMongoTemplate#tail`.
Pour la sauvegarde, vous disposez d'un `Mono<Drawing>` en paramètre qui peut être utilisé dans un `ReactiveCrudRepository`.
Ce dernier peut être spécialisé pour les dessins.
Pour ce faire, créez une interface `DrawingRepository` qui étend `ReactiveCrudRepository`.
Référez-vous à la Javadoc afin de savoir comment configurer vos generics.

Il faut à présent configurer le `CORS` pour que des requêtes depuis `https://localhost:8443` soit acceptées.
Dans votre bean de configuration des routes, faites en sorte que toutes les réponses envoient le header `Access-Control-Allow-Origin` avec la valeur `https://localhost:8443`.
Le protocole HTTP prévoit aussi d'envoyer une requête `OPTIONS` afin de savoir quelles requêtes seront acceptées.
Faites en sorte que toute requête `OPTIONS` envoyée sur `/drawing*` retourne une réponse avec comme header:
* `Access-Control-Allow-Origin` avec la valeur `https://localhost:8443`
* `Access-Control-Allow-Headers` avec la valeur `Content-Type`
* `Access-Control-Allow-Methods` avec la valeur `POST, GET`

Il ne reste plus qu'à lancer le module `WebFlux`.
Configurer votre IDE pour passer les arguments suivants;
* `--spring.data.mongodb.uri=mongodb://<<user>>:<<password>>@<<host>>:<<port>>/<<database>>`
* `--server.port=9443`

Connectez-vous à présent sur https://localhost:8443 et testez l'application!

### Kotlin

Nous allons maintenant utiliser le language Kotlin pour déclarer nos routes de façon plus concise.
Du code Java et Kotlin peuvent tout à fait cohabiter ensemble.

Dans le `pom.xml` du module `WebFlux`, déclarez les éléments suivants:

```
<properties>
    <kotin.version>1.1.2-5</kotin.version>
</properties>

...

<dependencies>
    <dependency>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-stdlib-jre8</artifactId>
        <version>${kotlin.version}</version>
    </dependency>
    <dependency>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-test</artifactId>
        <version>${kotlin.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>

...

<build>
    <sourceDirectory>${project.basedir}/src/main/kotlin</sourceDirectory>
    <plugins>
        <plugin>
            <artifactId>kotlin-maven-plugin</artifactId>
            <executions>
                <execution>
                    <id>compile</id>
                    <phase>compile</phase>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                    <configuration>
                        <sourceDirs>
                            <source>src/main/java</source>
                            <source>src/main/kotlin</source>
                        </sourceDirs>
                    </configuration>
                </execution>
                <execution>
                    <id>test-compile</id>
                    <phase>test-compile</phase>
                    <goals>
                        <goal>test-compile</goal>
                    </goals>
                </execution>
            </executions>
            <groupId>org.jetbrains.kotlin</groupId>
            <version>${kotlin.version}</version>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <executions>
                <execution>
                    <id>default-compile</id>
                    <phase>none</phase>
                </execution>
                <execution>
                    <id>default-testCompile</id>
                    <phase>none</phase>
                </execution>
                <execution>
                    <id>compile</id>
                    <phase>compile</phase>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                </execution>
                <execution>
                    <id>testCompile</id>
                    <phase>test-compile</phase>
                    <goals>
                        <goal>testCompile</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Commentez votre classe `RouteConfig` et ajoutez une classe `RouteConfig.kt` dans `/src/main/kotlin`.
Essayez de réécrire les différentes méthodes de votre classe de configuration Java en Kotlin.
Inspirez-vous des ressources citées plus haut afin de trouver des élémens syntaxiques qui pourront vous aider.

Si vous disposez de `IntelliJ`, un support avancé du language est proposé.
Lorsque vous copier/coller du code Java dans une classe Kotlin, l'IDE peut vous convertire relatvement efficacement le code.

Une fois terminé testez de nouveau l'application.