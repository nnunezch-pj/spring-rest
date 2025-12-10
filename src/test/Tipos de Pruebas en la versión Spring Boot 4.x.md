🎯 Tipos de Pruebas en Spring Boot 4.x
La filosofía se mantiene (aislamiento vs. contexto completo), pero las herramientas y configuraciones cambian debido a la eliminación de @WebMvcTest.

1. 🧪 Pruebas UNITARIAS de la Capa Web (Aislamiento del Controlador)
   Estas pruebas están diseñadas para verificar el Controller (Presentación) de manera aislada, asegurando que el mapeo de URLs, la validación de DTOs y el manejo de errores funcionen correctamente, sin invocar la lógica real del negocio (Service).

* Objetivo: Probar solo el Controller.
* Levantan: Un contexto mínimo de Spring que incluye el Controller, pero el Service es simulado.
* Servidor: NO levantan un servidor real.
* Herramienta: MockMvc.

| Técnica en Spring Boot 4.x | Configuración |
|----------------------------|---------------|
| **Simulación por Contexto Completo** | @SpringBootTest + Reemplazo de Beans con @TestConfiguration |
| **Propósito** | Reemplaza el rol de @WebMvcTest para simular dependencias (Servicios, Repositorios) y usar MockMvc. |
| **Mocks** | Se logra manualmente usando Mockito.mock() dentro de una clase @TestConfiguration para inyectar los Mocks en el @SpringBootTest. |

2. 🧩 Pruebas de INTEGRACIÓN (Contexto Completo Simulado)

Estas pruebas cargan toda la cadena de beans de la aplicación (Controller $\rightarrow$ Service $\rightarrow$ Repository), asegurando que la inyección de dependencias y la lógica de negocio funcionen juntas, pero sin necesidad de un endpoint HTTP real.

* Objetivo: Probar el flujo completo de la aplicación (RN1, RN3, RN4, etc.).
* Levantan: TODA la aplicación (todos los beans).
* Servidor: NO levantan un servidor web (webEnvironment = Mock).
* Herramienta: MockMvc o llamadas directas al Service.

| Técnica en Spring Boot 4.x | Configuración |
|----------------------------|---------------|
| **Anotación Principal** | @SpringBootTest (o @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)) |
| **Mocks** | Generalmente, se deja que los beans reales se inyecten, pero se pueden simular dependencias externas (ej. sistemas de pago) usando @MockBean. |

3. 🌐 Pruebas END-TO-END (Servidor Real)

Estas pruebas son las más completas, ya que levantan un servidor HTTP real en un puerto libre, probando la pila tecnológica de principio a fin, incluyendo serialización, manejo de filtros y routing.

* Objetivo: Probar la aplicación como un cliente real la ve.
* Levantan: TODA la aplicación y el servidor web embebido.
* Servidor: Levantan un servidor real en un puerto aleatorio.
* Herramienta: WebTestClient o TestRestTemplate.

| Técnica en Spring Boot 4.x | Configuración |
|----------------------------|---------------|
| **Anotación Principal** | @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) |
| **Uso** | Ideal para probar seguridad (filtros), caching, y el contracto real de la API. |