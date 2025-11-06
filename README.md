https://github.com/ZoeAcero/ZoeAcero_AlvaroMartin_DanielJoseGonzalez.git



🚀 Benchmark de Estrategias de Concurrencia en Java (Spring Boot)

Este proyecto implementa y compara el rendimiento de tres modelos de ejecución de tareas intensivas en CPU (cálculo de números primos) utilizando Spring Boot 3.x.

El frontend (servido desde index.html) permite configurar la prueba y visualizar métricas críticas como el Tiempo de Ejecución, la Aceleración (Speedup) y la Eficiencia de uso de hilos.

🎯 1. Objetivo Principal

Desarrollar una herramienta de Benchmarking para medir la eficiencia y la aceleración obtenida al pasar de la ejecución monohilo a la concurrencia gestionada manualmente (ExecutorService) y la concurrencia asistida por el framework (@Async).

⚙️ 2. Arquitectura y Tecnologías

Componente

Capa

Clase(s) Clave

Backend

API / Servicio

BenchmarkController, BenchmarkService

Tareas

Modelo

ComputationTask (Cálculo de Primos)

Configuración

Spring

TaskExecutorConfig, BenchmarkApplication

Frontend

Interfaz Web

index.html (HTML + Tailwind CSS + JavaScript)

2.1. Métricas de Rendimiento

Métrica

Fórmula

Descripción

Tiempo Total

T_concurrente

Tiempo en milisegundos para completar todas las tareas.

Aceleración (Speedup)

$\frac{T_{\text{Secuencial}}}{T_{\text{Concurrente}}}$

Factor de ganancia de velocidad respecto a la ejecución en un solo hilo.

Eficiencia

$\frac{\text{Speedup}}{\text{Número de Hilos (P)}}$

Mide el uso óptimo de los hilos. Valor ideal: cercano a 1.0 (100%).

🧪 3. Estrategias de Concurrencia

Modo

Descripción

Tipo de Implementación

SEQUENTIAL

Ejecución en el hilo principal de la petición HTTP.

Monohilo, base de tiempo.

EXECUTOR_SERVICE

Uso manual de un ExecutorService (FixedThreadPool).

Concurrencia explícita de Java.

SPRING_ASYNC

Uso del método anotado @Async, delegando la gestión del pool a ThreadPoolTaskExecutor de Spring.

Concurrencia idiomática de Spring.

🛠️ 4. Guía de Ejecución (Paso a Paso)

Para probar la aplicación, la máquina debe tener el JDK 21 (o superior) y Maven configurados.

4.1. 📥 Compilación e Inicio del Backend

Abre la terminal en la raíz del proyecto (donde está el pom.xml).

Ejecuta el comando para compilar y descargar las dependencias:

mvn clean install


Ejecuta la aplicación Spring Boot (mantén la terminal abierta y corriendo):

mvn spring-boot:run


Nota: Si las variables de entorno de mvn no están configuradas, usa la ruta absoluta que ya verificamos: C:\Users\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd spring-boot:run

4.2. 💻 Acceso al Frontend

Una vez que la aplicación muestre "Started BenchmarkApplication", abre tu navegador.

Accede a la interfaz de control:

http://localhost:8080/


4.3. Prueba del Benchmark

Introduce el Número de Tareas (N) (ej. 50) y los Hilos Máximos (P) (ej. 4).

Haz clic en "Iniciar Benchmark" para que el frontend envíe la petición POST /start al servidor y visualice los resultados.

🐛 5. Logros en la Estabilidad del Entorno

Durante el desarrollo, se corrigieron problemas críticos de compatibilidad, asegurando la estabilidad del proyecto en cualquier entorno:

Compatibilidad JDK/Spring: Se migró el entorno al JDK 21, necesario para las dependencias de Spring Boot 3.x.

Resolución de Dependencias: Se resolvió el fallo de Runtime (Bean Not Found / Error 500) que impedía la inyección del pool de hilos de Spring, asegurando el escaneo correcto con @ComponentScan y la Inyección por Constructor.

Estabilidad de la Prueba: Se redujo la carga de trabajo de la ComputationTask para evitar que la ejecución secuencial causara Timeouts del servidor HTTP.
