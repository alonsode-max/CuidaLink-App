# CuidaLink 🩺

**CuidaLink** es una aplicación Android nativa para el **acompañamiento y cuidado de personas mayores**. Conecta a un **paciente** con su **cuidador** en tiempo real: ubicación, zonas seguras (geovallas), alertas SOS, calendario de medicación/citas, ejercicios cognitivos y contactos de emergencia.

Está construida 100 % en **Kotlin + Jetpack Compose (Material 3)** y usa **Supabase** (Postgres + Auth + Realtime + Storage) como backend, sin servidor intermedio propio.

---

## 📑 Tabla de contenidos

1. [Características](#-características)
2. [Roles](#-roles-paciente-vs-cuidador)
3. [Stack tecnológico](#-stack-tecnológico)
4. [Arquitectura](#-arquitectura)
5. [Estructura del proyecto](#-estructura-del-proyecto)
6. [Navegación](#-navegación)
7. [Backend Supabase](#-backend-supabase)
8. [Modelo de datos](#-modelo-de-datos)
9. [Flujos clave](#-flujos-clave)
10. [Puesta en marcha](#-puesta-en-marcha)
11. [Permisos](#-permisos-android)
12. [Convenciones de código](#-convenciones-de-código)

---

## ✨ Características

| Área | Descripción |
|------|-------------|
| **Autenticación** | Registro y login con Supabase Auth (email/contraseña). El rol se deduce de la tabla donde vive el `uid`. |
| **Vinculación** | El paciente genera un **código/QR**; el cuidador lo escanea para vincularse. |
| **Ubicación en tiempo real** | El paciente reporta su ubicación (FusedLocation) y el cuidador la ve en un mapa OpenStreetMap. |
| **Historial de ubicaciones** | Lista de las últimas posiciones registradas; cada una se abre en Google Maps. |
| **Zonas seguras (geovallas)** | El cuidador define **varias geovallas**; si el paciente sale de **todas**, se dispara una alerta. |
| **Alerta SOS** | El paciente pulsa SOS → el cuidador recibe una alerta a pantalla completa con su ubicación. |
| **Alerta de zona** | Salida de zona segura → alerta estilo SOS con ubicación actual (o última conocida). |
| **Llamar a casa** | Botón que marca el teléfono de emergencia capturado en el registro. |
| **Calendario** | Citas y recordatorios de medicación con alarmas exactas (foreground service + full-screen intent). |
| **Centro cognitivo** | Mini-juegos (parejas de emojis) para estimulación cognitiva, con reporte de actividad. |
| **Contactos de emergencia** | Gestión de contactos importantes. |
| **Fotos de perfil** | Paciente y cuidador suben su avatar a Supabase Storage. |
| **Accesibilidad** | Ajustes de tamaño de fuente, contraste y tema pensados para personas mayores. |

---

## 👥 Roles: paciente vs cuidador

La app tiene **dos experiencias completamente distintas** según el rol, resueltas con **dos grafos de navegación separados**:

- **PACIENTE** (`patient_graph`) — interfaz simple y grande: inicio, calendario, juegos, auxilio/SOS, perfil, contactos.
- **CUIDADOR** (`caregiver_graph`) — panel de monitoreo: ubicación del paciente, zonas seguras, historial, calendario del paciente, alertas.

El rol se determina en el login según en qué tabla de Supabase (`patients` o `caretakers`) se encuentra el `uid` autenticado.

---

## 🛠 Stack tecnológico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material 3, Navigation Compose
- **Imágenes:** Coil (`AsyncImage`)
- **Mapas:** osmdroid (OpenStreetMap, sin API key)
- **QR:** ZXing (core + embedded scanner)
- **Ubicación:** Google Play Services Location (FusedLocationProvider)
- **Backend:** Supabase-kt (BOM) → Auth, Postgrest, Realtime, Storage; Ktor (OkHttp) + kotlinx.serialization
- **Persistencia local:** DataStore Preferences (sesión + ajustes de accesibilidad)
- **Build:** Gradle KTS con version catalog (`gradle/libs.versions.toml`)

**Requisitos de build:** `compileSdk 36`, `minSdk 31`, `targetSdk 36`, Java 11.

---

## 🏗 Arquitectura

Patrón **MVVM** con capas claras y flujo unidireccional de datos:

```
┌─────────────────────────────────────────────┐
│  UI (Composables)  — ui/, ui/caregiver/      │  observa StateFlow
├─────────────────────────────────────────────┤
│  ViewModel         — viewmodel/              │  lógica de estado + StateFlow
├─────────────────────────────────────────────┤
│  Repository        — repository/             │  interfaz + impl (Result<T>)
├─────────────────────────────────────────────┤
│  Service           — network/                │  llamadas a Supabase (Postgrest/Auth/Storage)
├─────────────────────────────────────────────┤
│  Supabase          — SupabaseClient          │  Auth · Postgres · Realtime · Storage
└─────────────────────────────────────────────┘
```

- **Repositorios** devuelven `Result<T>` y ocultan el detalle de Supabase; la lógica de negocio depende de la interfaz, no de la implementación.
- **StateFlow** expone el estado inmutable a la UI; los ViewModel nunca mutan objetos en sitio (patrón `copy`).
- **Watchers de Realtime**: composables invisibles montados junto al `NavHost` que observan cambios en tiempo real y reaccionan (navegan + notifican). Ejemplos: `PatientSosWatcher`, `CaregiverSosWatcher`, `CaregiverZoneWatcher`.

---

## 📂 Estructura del proyecto

```
app/src/main/java/com/example/cuidalink/
├── MainActivity.kt              # Host de navegación, grafos por rol, watchers
├── data/                        # Persistencia local (DataStore)
│   ├── SessionStore.kt          #   sesión (uid, rol) persistida
│   └── AccessibilitySettingsStore.kt
├── model/
│   ├── Contact.kt, Event.kt, UserModels.kt
│   ├── remote/                  # DTOs del esquema Supabase (@Serializable)
│   │   ├── RemoteProfiles.kt    #   Patient, Caretaker, GeofenceZone, LocationHistory…
│   │   └── EventRow.kt
│   └── ui/ProfileUiModels.kt    # Modelos de UI (PatientProfileUi, CaregiverProfileUi…)
├── network/                     # Acceso a Supabase
│   ├── SupabaseClient.kt        #   config del cliente (URL + anon key)
│   ├── ProfileService.kt        #   perfiles, geovallas, ubicación, fotos, SOS
│   └── LinkService.kt           #   vinculación paciente↔cuidador
├── repository/                  # Interfaces + implementaciones
│   ├── ProfileRepository(+Impl).kt
│   ├── LinkRepository.kt, CalendarRepository.kt, RegistrationRepository.kt
├── viewmodel/                   # Un ViewModel por pantalla/feature (StateFlow)
├── ui/                          # Composables del paciente + compartidos
│   ├── caregiver/               #   pantallas exclusivas del cuidador
│   ├── components/              #   piezas reutilizables (mapa, QR, estados)
│   ├── icons/, theme/           #   iconografía y tema (color, tipografía, accesibilidad)
│   └── *Watcher.kt, OsmMap.kt, MapsIntent.kt, ZoneAlertNotifier.kt …
├── receiver/                    # AlarmReceiver + AlarmService (recordatorios)
├── service/                     # LocationTrackingService (foreground)
└── util/AlarmScheduler.kt       # Programación de alarmas exactas
```

---

## 🧭 Navegación

Un único `NavHost` con destinos de nivel raíz y **dos grafos anidados** por rol.

**Raíz:** `login` · `registro` · `paciente_codigo` · `cuidador_vincular`

**`patient_graph`** (start = `inicio`):
`inicio` · `entrenamiento` · `juego` · `juegoParejas` · `auxilio` · `calendario` · `ajustes` · `tema` · `accesibilidad` · `perfil` · `contactos`

**`caregiver_graph`** (start = `monitoreo`):
`monitoreo` · `cuidador_calendario` · `zonas` · `cuidador_ajustes` · `cuidador_historial` · `cuidador_perfil` · `cuidador_mi_perfil` · `cuidador_auxilio` · `cuidador_alerta_paciente` · `cuidador_alerta_zona`

Las rutas `cuidador_alerta_*` son pantallas de alerta a pantalla completa a las que navegan automáticamente los *watchers* de Realtime.

---

## 🔌 Backend Supabase

El backend **no es un servidor REST propio**: es Supabase directo (tablas Postgrest + Auth + Realtime + Storage). Ver [`INTEGRACION.md`](INTEGRACION.md) para el checklist completo.

**Configuración** (`network/SupabaseClient.kt`):

```kotlin
const val SUPABASE_URL = "https://XXXX.supabase.co"   // proyecto compartido
const val SUPABASE_KEY = "anon / publishable key"      // clave PÚBLICA (protegida por RLS)
```

> ⚠️ Usar siempre la clave `anon`/publishable (pública por diseño, protegida por Row Level Security). **Nunca** la `service_role` en la app.

El cliente instala `Auth`, `Postgrest`, `Realtime` y `Storage`, con `requestTimeout = 20s`.

### Scripts SQL

Ejecutar en **Supabase → SQL Editor** (todos son necesarios):

| Script | Qué crea |
|--------|----------|
| `supabase_rls_vinculacion.sql` | RLS para vincular paciente↔cuidador |
| `supabase_rls_calendario.sql` | RLS del calendario/eventos |
| `supabase_location_history.sql` | Tabla `location_history` + RLS |
| `supabase_storage_avatars.sql` | Bucket `avatars` (fotos de perfil) |
| `supabase_emergency_phone.sql` | Columna `emergency_phone` en `patients` |
| `supabase_geofences.sql` | Columna `geofences jsonb` en `patients` |

### Realtime — notas

- Cada canal usa un **ID único** (UUID) para evitar colisiones.
- Las tablas observadas necesitan `replica identity full` y estar en la publicación `supabase_realtime`.
- **Gotcha de RLS:** un `UPDATE` de PostgREST que no coincide con ninguna fila devuelve `200`/vacío (no-op silencioso). Verificar que las políticas permiten la fila afectada.

---

## 🗃 Modelo de datos

Tablas principales en Supabase:

- **`patients`** — `uid`, `name`, `email`, `age`, `blood_group`, `allergies`, `weight`, `height`, `profile_pic`, `patient_lat`, `patient_lng`, `emergency_phone`, **`geofences` (jsonb)**, `created_at`…
- **`caretakers`** — datos del cuidador + vínculo al paciente.
- **`location_history`** — histórico de posiciones del paciente (insertadas con throttling ~60 s).
- **`events`** — citas/recordatorios del calendario.

**Geovallas** (`patients.geofences`) es un **array JSON** de objetos, no una tabla aparte:

```kotlin
@Serializable
data class GeofenceZone(
    val lat: Double,
    val lng: Double,
    val radius: Float,
    val name: String? = null,
)
```

Se guardan en la fila del paciente para **reutilizar el Realtime y las políticas RLS existentes** sin plomería adicional. La detección de "fuera de zona" = el paciente tiene ≥1 geovalla **y** está fuera de **todas** ellas (`Location.distanceBetween`).

---

## 🔄 Flujos clave

### Vinculación
1. Paciente → `PatientShareCodeScreen` genera código + QR.
2. Cuidador → `CaretakerLinkScreen` escanea el QR → `LinkService` crea el vínculo.

### Ubicación
1. `LocationTrackingService` (foreground, `foregroundServiceType=location`) + `PatientLocationReporter` reportan la posición.
2. `PatientLocationViewModel` la sube a `patients` e inserta puntos en `location_history` (throttled).
3. El cuidador ve la posición vía Realtime en `CaregiverDashboardScreen` (mapa osmdroid).

### Zonas seguras
1. Cuidador en `CaregiverSafeZoneScreen`: ajusta radio, pulsa **"Añadir zona segura"** → se agrega al array `geofences`.
2. Lista **"Zonas seguras (N)"** con borrado por geovalla; el mapa dibuja todos los círculos.
3. Si no hay ninguna geovalla → banner "Sin zona configurada" en el dashboard.
4. `CaregiverZoneAlertViewModel` detecta la transición dentro→fuera y `CaregiverZoneWatcher` dispara la alerta a pantalla completa (`cuidador_alerta_zona`).

### SOS
1. Paciente pulsa SOS → `ProfileService.triggerPatientSos` marca la fila.
2. `CaregiverSosWatcher` recibe el cambio por Realtime → navega a `cuidador_alerta_paciente` (pantalla completa con ubicación + abrir en Maps).

### Recordatorios / alarmas
- `AlarmScheduler` programa alarmas exactas → `AlarmReceiver` lanza `AlarmService` (foreground) con full-screen intent, incluso con pantalla bloqueada.

---

## 🚀 Puesta en marcha

### Requisitos
- Android Studio (con Android SDK 36)
- JDK 11
- Un emulador o dispositivo con **Android 12+ (API 31)**

### Pasos
1. **Clonar** el repositorio y abrirlo en Android Studio.
2. **Configurar Supabase:** pegar `SUPABASE_URL` y `SUPABASE_KEY` en `network/SupabaseClient.kt`.
3. **Ejecutar los scripts SQL** listados arriba en el SQL Editor de Supabase.
4. **Compilar:**
   ```bash
   ./gradlew :app:compileDebugKotlin   # comprobación rápida de compilación
   ./gradlew :app:assembleDebug        # generar APK debug
   ```
5. **Instalar** en el emulador/dispositivo:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
   (con varios dispositivos, añadir `-s emulator-XXXX`).

---

## 🔐 Permisos Android

Declarados en `AndroidManifest.xml`:

- **Ubicación:** `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`, `FOREGROUND_SERVICE_LOCATION`
- **Alarmas / notificaciones:** `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `POST_NOTIFICATIONS`, `USE_FULL_SCREEN_INTENT`, `WAKE_LOCK`
- **Servicios en primer plano:** `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`
- **Otros:** `INTERNET`, `ACCESS_NETWORK_STATE`, `CAMERA` (escáner QR), `READ_CONTACTS`, `ACTIVITY_RECOGNITION`

Servicios registrados: `AlarmService` (`specialUse` → `alarm_clock`) y `LocationTrackingService` (`location`).

---

## 📐 Convenciones de código

- **Inmutabilidad:** nunca mutar objetos en sitio; usar `copy`.
- **Archivos pequeños y cohesivos** (~200–400 líneas), funciones enfocadas (<50 líneas).
- **Nombres:** `camelCase` (vars/funciones), `PascalCase` (tipos/composables), `UPPER_SNAKE_CASE` (constantes), estado con prefijos `is`/`has`/`should`.
- **Manejo de errores explícito** en cada capa; nunca tragar errores en silencio.
- **Sin secretos hardcodeados** salvo la `anon key` pública (protegida por RLS).
- Commits: `<tipo>: <descripción>` (`feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `perf`, `ci`).

---

## 📄 Documentos relacionados

- [`INTEGRACION.md`](INTEGRACION.md) — checklist de integración frontend ↔ Supabase.
- Scripts `supabase_*.sql` — esquema, RLS, storage y columnas.
