# Pausa (Android)

App para Android (Kotlin + Jetpack Compose, minSdk 26) que bloquea apps distractoras. Para abrir una app bloqueada, ganas minutos resolviendo un minijuego y girando una ruleta (5, 10 o 15 min). Al acabarse el tiempo, vuelve a bloquear.

## Cómo funciona
- Un **servicio de accesibilidad** (`BlockerService`) detecta qué app pasa a primer plano.
- Si está bloqueada y no hay minutos ganados, abre `BlockActivity` encima (minijuego -> ruleta).
- Al ganar se guarda `unlockUntil`; el servicio re-comprueba al vencer el tiempo y vuelve a bloquear.

## Cómo compilarla
1. Abre la carpeta `PausaAndroid` en **Android Studio** (Ladybug o más nuevo). Descargará Gradle y las dependencias solo.
2. Si te pide actualizar versiones de AGP/Kotlin/Compose BOM, acepta.
3. Ejecuta en un móvil real o emulador.
4. Al abrir la app, toca "Abrir ajustes de accesibilidad" y activa **Pausa**.
5. En la pestaña Apps elige qué bloquear y activa el bloqueo en Inicio.

## Importante
- No compilé el proyecto aquí (no hay Android SDK): puede requerir pequeños ajustes.
- **Google Play** restringe el uso de AccessibilityService; para publicar debes declararlo en el formulario de permisos y justificar que es una función central. Alternativas: UsageStatsManager + permiso de superposición (`SYSTEM_ALERT_WINDOW`), o Digital Wellbeing/Modo Enfoque (sin API pública).
- Xiaomi/Huawei/Samsung pueden matar el servicio: pide excluir la app del ahorro de batería.
- Los minutos son tiempo de reloj (no de uso) y no hay icono de lanzador personalizado todavía.

## Ideas siguientes
Horarios de bloqueo, más juegos, mascota, ligas con amigos (backend), suscripción con Google Play Billing.

## Obtener el APK
**Opción A — Android Studio:** Build > Build Bundle(s) / APK(s) > Build APK(s). Sale en `app/build/outputs/apk/debug/app-debug.apk`.

**Opción B — terminal** (con Android SDK instalado y `ANDROID_HOME` definido): `./gradlew assembleDebug`.

**Opción C — sin instalar nada (GitHub Actions):** sube esta carpeta a un repositorio de GitHub; en la pestaña *Actions* corre "Build APK" y descarga el artefacto `Pausa-debug-apk`.

Para instalarlo: copia el APK al móvil y permite "instalar apps de orígenes desconocidos".
