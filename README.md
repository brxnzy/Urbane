<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9-blueviolet?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-UI-green?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Supabase-Backend-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white" alt="Supabase"/>
  <img src="https://img.shields.io/badge/Firebase-Notifications-orange?style=for-the-badge&logo=firebase&logoColor=white" alt="Firebase"/>
  <img src="https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white" alt="Android Studio"/>
</p>

<h1 align="center">🏙️ Urbane – Residential Management App</h1>

<p align="center">
  Plataforma móvil para la gestión integral de residenciales, pagos, incidencias y comunicación entre residentes y administradores.<br/>
  <strong>Android (Kotlin + Jetpack Compose) • Supabase • Firebase</strong>
</p>

<br/>

## 📌 ¿Qué es Urbane?

Urbane es una aplicación móvil diseñada para **modernizar la administración de condominios, residenciales y propiedades**. Permite controlar desde una sola app:

- 💳 Pagos de alquiler y cuotas
- 🏠 Propiedades y residentes
- 🚨 Incidencias y reportes
- 🔔 Notificaciones en tiempo real
- 📊 Información financiera y administrativa

Todo con una arquitectura limpia, escalable y segura.

<br/>

## 🚀 Tecnologías usadas

| Tecnología              | Uso principal                              |
|-------------------------|--------------------------------------------|
| **Kotlin**              | Lenguaje principal                         |
| **Jetpack Compose**     | Interfaz moderna y reactiva                |
| **Supabase**            | Backend (Auth, Database, Storage, Functions) |
| **PostgreSQL**          | Base de datos (vía Supabase)               |
| **Firebase FCM**        | Notificaciones push en tiempo real         |
| **MVI**                | Arquitectura principal                     |
| **Coroutines & Flow**   | Manejo de datos asíncronos y reactivos     |
| **Material 3**          | Sistema de diseño moderno                  |

<br/>

## 🧠 Arquitectura limpia

- Separación clara de responsabilidades  
- Fácil mantenimiento
- Escalabilidad futura  
- Excelente manejo de estado

<br/>

## 👥 Sistema de Roles

| Rol          | Función principal                              |
|--------------|------------------------------------------------|
| 👨‍💼 Administrador | Control total del sistema                     |
| 🧑‍💻 Residente     | Pagar cuotas, reportar incidencias            |


<br/>

## ✨ Funcionalidades principales

### 🏠 Gestión de Propiedades
- Registro de casas, apartamentos, locales, villas, terrenos
- Asignación de residentes
- Gestión de contratos (fechas, montos, condiciones)

### 💳 Gestión de Pagos
- Pagos mensuales, trimestrales, anuales
- Abonos y pagos parciales
- Cálculo automático de mora e intereses
- Facturas digitales descargables
- Historial completo por residente/propiedad
- Servicios adicionales (agua, luz, mantenimiento, etc.)

### 🚨 Incidencias y Reportes
- Reporte rápido con descripción y fotos
- Seguimiento de estado (pendiente, en proceso, resuelto)
- Comunicación directa con administración

### 📊 Panel Administrativo
- Ingresos vs egresos
- Pagos pendientes y morosos
- Tasa de ocupación
- Reportes financieros exportables
- Registro de multas y penalizaciones

<br/>

## 🔐 Seguridad

- Autenticación robusta con **Supabase Auth**
- Control estricto basado en roles (RBAC)
- Auditoría de acciones importantes
- Protección de datos sensibles
- Validaciones en backend + frontend

<br/>

## 🛠️ Instalación y configuración

1. Clona el repositorio

```bash
git clone https://github.com/brxnzy/Urbane.git


```

Abre el proyecto en Android Studio
Configura las claves:
Supabase: Agrega la URL y la Anon Key en local.properties (recomendado) o directamente en el build.gradleproperties# local.properties
SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Firebase: Descarga el archivo google-services.json desde la consola de Firebase y colócalo en la carpeta app/



2. Dependencias

```bash
    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation(platform("io.github.jan-tennert.supabase:bom:3.2.4"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:functions-kt")
    implementation("io.ktor:ktor-client-android:3.3.1")
    implementation("io.ktor:ktor-client-cio:3.3.1")
    implementation("io.ktor:ktor-client-websockets:3.3.1")
    implementation("androidx.datastore:datastore-core-android:1.1.0")
    implementation("androidx.datastore:datastore-preferences:1.1.0")
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.2.20")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.0")
    implementation("io.ktor:ktor-client-core:2.3.3")
    implementation("io.ktor:ktor-client-cio:2.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-messaging")

```
Sincroniza el proyecto (Sync Project with Gradle Files) y ejecuta 🚀



## 🧪 Estado actual del proyecto

🟡 **En desarrollo activo**

Funcionalidades ya implementadas:

- 🔐 Autenticación y sistema de roles
- 💳 Gestión completa de pagos (incluyendo mora, abonos y facturas)
- 🔔 Sistema de notificaciones push con Firebase
- 🚨 Reporte y seguimiento de incidencias
- 🔄 Integración total con Supabase (Auth, Database, Storage)
- 🎨 Interfaz moderna con Jetpack Compose + Material 3

<br/>

## 📈 Visión a futuro

- 🌐 Web dashboard administrativo
- 🏬 Soporte multi-residencial
- 💳 Integración con pasarelas de pago (Stripe, PayPal, locales)
- ✍️ Firma digital de contratos
- 📊 Reportes avanzados y analíticas
- 📱 Versión iOS (posible con Kotlin Multiplatform)

<br/>

## 👨‍💻 Autores

**Bryan Flores**  
**Sebastian Gutierrez**  

GitHub: [@brxnzy](https://github.com/brxnzy)

¡Cualquier **estrella** ⭐, fork o sugerencia es súper bienvenida!  
Construyendo algo útil para la República Dominicana y más allá 🇩🇴
