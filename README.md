# FinMojarra — fase 1: Login (modo claro)

App Android con Kotlin + Jetpack Compose + Material 3. Esta fase incluye las pantallas de
acceso del diseño de Figma "App Moviles App FinMojarra", en **modo claro**.

## Cómo abrirlo
1. Android Studio (versión reciente, compatible con AGP 8.12): **File ▸ Open** y elige la carpeta `FinMojarra`.
2. Espera a que termine **Gradle Sync** (necesita internet la primera vez).
3. Ejecuta en un emulador o dispositivo con Android 8.0 (API 26) o superior.

Namespace: `com.finmojarra.app` · applicationId: `com.finmojarra.app` · minSdk 26 · compileSdk/targetSdk 36 · Kotlin 2.0.21.
Si Android Studio ofrece actualizar el plugin de Gradle o el SDK, acepta.

## Pantallas (todas en `ui/auth`)
| Frame de Figma | Archivo |
|---|---|
| Inicio App | `InicioScreen.kt` |
| Login | `LoginScreen.kt` |
| Registrar | `RegisterScreen.kt` |
| Recuperar contraseña | `RecoverScreen.kt` |
| Usuario/Contraseña incorrecta · error (demasiados intentos) · Login Error conexión · Login Error base de datos | `ui/components/AuthErrorDialog.kt` |
| Pantalla de carga | `LoadingOverlay` en `ui/components/AuthScreenFrame.kt` |

Cada pantalla tiene `@Preview` (Login: normal, credenciales incorrectas y cargando).

## Firebase
La app usa **Firebase Authentication** mediante `data/auth/FirebaseAuthRepository.kt`
(se conecta en `di/AppContainer.kt`).

- `app/google-services.json` debe tener el mismo paquete que `applicationId` (`com.finmojarra.app`).
- Correo/contraseña: **Authentication ▸ Método de acceso ▸ Correo electrónico/contraseña ▸ Habilitar**.
- Login con correo (el usuario sin "@" se rechaza). El teléfono del registro queda pendiente hasta
  agregar una base de datos (Firestore).

### Continuar con Google
Botón de `InicioScreen` -> `GoogleTokenProvider` (Credential Manager) -> `FirebaseAuthRepository.signInWithGoogle`.
Para que funcione hay que configurar Firebase (el código ya está listo):
1. **Authentication ▸ Método de acceso ▸ Google ▸ Habilitar** (elige un correo de asistencia) ▸ Guardar.
2. Obtén la huella **SHA-1** de depuración: en Android Studio, panel Gradle ▸ `app` ▸ Tasks ▸ android ▸
   `signingReport` (o `gradlew signingReport`). Cópiala de la variante `debug`.
3. **Configuración del proyecto ▸ Tus apps ▸ FinMojarra ▸ Agregar huella digital** y pega la SHA-1.
4. Descarga el `google-services.json` nuevo (ahora trae `oauth_client`), reemplaza `app/google-services.json`
   y haz **Sync**. Sin ese `oauth_client`, el botón muestra un error con el código `GOOGLE_CLIENT_ID_MISSING`.
5. El emulador o celular debe tener una cuenta de Google agregada (Ajustes ▸ Cuentas).

## Perfil (fase 2)
`ProfileScreen` muestra la foto, el nombre y una tarjeta por apartado. Cada tarjeta abre su pantalla
(todas en `ui/profile`, archivo `ProfileDetailScreens.kt`), excepto **Correo electrónico**, que solo se muestra.

| Tarjeta | Pantalla | Qué hace |
|---|---|---|
| Editar foto | `EditPhotoScreen` | Tomar foto (cámara), seleccionar de la galería, eliminar foto |
| TÚ · Info demográfica | `DemographicsScreen` | Edad, género, ocupación, nivel educativo |
| Número Celular | `PhoneScreen` | Cambiar número (con confirmación) |
| Dirección | `AddressScreen` | País, departamento, ciudad y dirección (tipo de vía + vía + placa) |
| Núm. Documento | `DocumentScreen` | Tipo y número de documento |

- Los datos extra (teléfono, dirección, documento, info demográfica y foto) se guardan **en Firestore**
  (`data/profile/FirestoreProfileDetailsRepository.kt`), así que son los mismos en cualquier dispositivo.
  `LocalProfileDetailsRepository` (solo en el teléfono) queda como alternativa en `di/AppContainer.kt`.
- La foto se reduce a ~480 px y se guarda dentro del documento de Firestore (sin Storage). Una copia queda en el almacenamiento privado de la app. La cámara usa un `FileProvider`
  (`res/xml/file_paths.xml` + `<provider>` en el manifest) y no necesita permiso `CAMERA`.
- El teléfono escrito en el registro se guarda como teléfono del perfil.

## Estructura
```
app/src/main/java/com/finmojarra/app/
├── MainActivity.kt, FinMojarraApp.kt
├── di/AppContainer.kt            ← dependencias (aquí se cambia el repositorio)
├── data/auth/                    ← modelos, AuthRepository, FirebaseAuthRepository, GoogleTokenProvider
├── navigation/FinMojarraNavHost  ← inicio · login · registro · recuperar · home
└── ui/
    ├── theme/                    ← Color, Type (Inter), Theme
    ├── components/               ← campo de texto, botones, diálogos, nubes, marco
    ├── auth/                     ← pantallas y ViewModels
    └── home/HomeScreen.kt        ← provisional
```

## Valores del diseño usados
Fondo `#F5FAFA` · teal `#00B9B3` (nubes) · botones negros, 40 dp, radio 8 · campos blancos,
borde `#E0E0E0`, 40 dp, radio 8 · diálogos blancos, borde negro, radio 20, padding 32 ·
botón "Salir" `#FF6347` · fuente Inter (400/500/600/700, incluida en `res/font`).

## Pendiente / decisiones abiertas
- **Logo:** `res/drawable-nodpi/logo_finmojarra.png` es de 135×139 px (la exportación que
  permitió Figma). Reemplázalo por una exportación @3x o un SVG para que se vea nítido y
  también para el ícono de la app.
- **Nubes:** se dibujan con círculos (`CloudDecoration.kt`), aproximando el diseño. Si
  exportas las nubes como SVG se pueden cambiar por un vector.
- **Botón de Google:** muestra un aviso; falta el logo oficial y la integración.
- **Frames "error" y "Pantalla de carga" en claro:** solo se pudieron ver en oscuro; se
  adaptaron al estilo claro de los demás diálogos. Compáralos con Figma.
- **Ver/ocultar contraseña:** se añadió (no está en el diseño).
- **Longitud mínima de contraseña:** 8 (`Validators.MIN_PASSWORD_LENGTH`); el diseño no la define.
- **Registro sin "usuario":** el login acepta usuario o correo pero el registro no pide usuario.
- **"Salir"** cierra la app (`finishAffinity`); confirma que es lo que quieres.
- **Modo oscuro:** existe en Figma pero aún no está implementado (`Theme.kt` tiene solo el claro).
- No se pudo compilar en el entorno donde se generó: si Gradle muestra algún error al sincronizar, envíalo y se corrige.

## Perfil de usuario
- Tras iniciar sesión, `HomeScreen` solo muestra el icono de perfil (arriba a la derecha) que abre `ProfileScreen`.
- Funciones: ver/editar nombre, correo (solo lectura), cambiar contraseña (con reautenticación), enviar correo de restablecimiento, cerrar sesión (`FirebaseAuth.signOut()`) y eliminar cuenta.
- Código: `data/profile/*` (contrato + implementación Firebase Auth), `ui/profile/*`, ruta `Routes.PERFIL`.
- Solo usa Firebase Authentication: teléfono y foto requieren Firestore/Storage (pendiente).


## Firestore (datos del perfil)
1. Consola de Firebase ▸ **Build ▸ Firestore Database ▸ Crear base de datos** (edición Standard, ubicación cercana
   como `southamerica-east1`, modo **producción**). La ubicación no se puede cambiar después.
2. Pestaña **Reglas**: pega esto y pulsa **Publicar**.
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```
3. Los datos se ven en **Firestore Database ▸ Datos ▸ users ▸ (UID del usuario)**.
   Si las reglas no están publicadas, Logcat (filtro `FinMojarra`) muestra `PERMISSION_DENIED`.
