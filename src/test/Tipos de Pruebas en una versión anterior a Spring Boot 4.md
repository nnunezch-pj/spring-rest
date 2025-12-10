🎯 Hay 3 tipos de pruebas (y cada una usa una anotación distinta)

1. Pruebas UNITARIAS de la capa web (solo el controller)
   -----------------------------------------------------
   👉 NO levantan todo el contexto de Spring
   👉 NO levantan servidor
   👉 NO cargan servicios reales

   Se usa:
   @WebMvcTest(MiController.class) -- NO ES COMPATIBLE CON LA VERSION SPRING 4

   Para qué sirve:
   - Testear solo el controlador
   - Mockear servicios con @MockBean
   - Usar MockMvc

   Son las más rápidas.

2. Pruebas de INTEGRACIÓN (MOCK)
   -----------------------------
   👉 Carga TODA la app (beans, servicios, repositorios)
   👉 Pero NO levanta un servidor web
   👉 Todo corre en memoria con MockMvc

   Se usa:
   @SpringBootTest
   o explícito:
   @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)

   Para qué sirve:
    - Probar toda la aplicación sin servidor real
    - Llamar controllers con MockMvc
    - Verificar comportamiento de servicios + repositorios

   Es lo que estás usando actualmente.

3. Pruebas END-TO-END (servidor real)
   -----------------------------------------------------
   👉 Levanta un servidor real embebido
   👉 Usa un puerto aleatorio
   👉 Usa TestRestTemplate o WebTestClient
   👉 Simula llamadas HTTP reales

   Se usa:
   @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

   Para qué sirve:
   - Probar seguridad
   - CORS
   - Filtros
   - Serialización real
   - Integraciones reales

   Son las pruebas más lentas, pero más completas.