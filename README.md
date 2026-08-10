# FCM Calculator — Calculadora GRL para FC Mobile

Calculadora ligera y moderna para Android, diseñada específicamente para calcular el **GRL (Global Rating Level)** en **FC Mobile**.  
La aplicación combina una capa nativa en **Kotlin** con una interfaz renderizada mediante **WebView** (HTML, CSS y JavaScript).

---

## Características

- **Cálculo automático del GRL global** a partir de los 11 titulares (y hasta 7 suplentes).
- **Gestión de titulares y suplentes**: añade o elimina suplentes fácilmente.
- **Rango rápido**: establece el rango de todos los jugadores con un solo toque (0‑5).
- **Indicador de progreso**: muestra cuántos titulares has completado.
- **Información de progreso**: te dice cuántos puntos de GRL y de rango faltan para subir de nivel.
- **Interfaz oscura** (tema fijo, sin alternancia).
- **Diseño responsive** y optimizado para móviles.
- **Funciona offline** (sin conexión a Internet).
- **Icono personalizado** en el lanzador.

---

## Captura de pantalla

<p align="center">
  <img src="app/src/main/fcmcalc-playstore.png" alt="FCM Calculator" width="300">
</p>

---

## Stack tecnológico

| Tecnología | Versión / Uso |
|------------|---------------|
| **Kotlin** | Capa nativa de Android |
| **AndroidX** | `activity-ktx`, `appcompat`, `constraintlayout`, `core-ktx`, `material` |
| **WebView** | Renderizado de la interfaz HTML |
| **HTML5** | Estructura de la calculadora |
| **CSS3** | Estilos y diseño responsivo |
| **JavaScript (ES6)** | Lógica de cálculo e interacciones |
| **Gradle (Kotlin DSL)** | Sistema de compilación |

---

## Estructura del proyecto

```text
FCMCalculator/
├── app/
│   └── src/
│       └── main/
│           ├── assets/
│           │   └── index.html          # Interfaz completa (HTML+CSS+JS)
│           ├── java/
│           │   └── com/android/fcmcalculator/
│           │       └── MainActivity.kt # Actividad principal con WebView
│           ├── res/
│           │   ├── drawable/           # Recursos gráficos
│           │   ├── layout/             # (no usado, pero presente)
│           │   ├── mipmap-*/           # Iconos del launcher
│           │   ├── values/             # Colores, strings, temas
│           │   └── xml/                # Configuración de respaldo
│           └── AndroidManifest.xml
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

---

## Requisitos

- **Android Studio** (versión estable recomendada)
- **JDK 11** o **17** (compatible con Gradle)
- **Android SDK** (API 24+ para `minSdk`, API 37 para `targetSdk`)

---

## Cómo compilar

### Desde Android Studio

1. Clona el repositorio:
   ```bash
   git clone https://github.com/kveld9/FCMCalculator.git
   cd FCMCalculator
   ```
2. Abre el proyecto en Android Studio.
3. Espera a que Gradle sincronice las dependencias.
4. Ejecuta la app en un emulador o dispositivo físico.

### Desde línea de comandos

**Windows:**
```bash
gradlew.bat assembleDebug
```

**Linux / macOS:**
```bash
./gradlew assembleDebug
```

El APK de depuración se generará en:
```text
app/build/outputs/apk/debug/
```

---

## Cómo funciona la calculadora

La lógica está completamente en `index.html` (JavaScript).  
El cálculo se actualiza en tiempo real al modificar cualquier campo.

- **GRL (Global Rating Level)**:  
  Se calcula como la suma de `(GRL - Rango)` de todos los jugadores cargados (titulares + suplentes), dividida entre el número total de jugadores, redondeando al alza. Luego se suma el promedio de los rangos.

- **Progreso**:  
  Se muestran los puntos que faltan para que el promedio suba 1 punto, tanto en GRL base como en rango.

- **Titulares**: siempre 11 jugadores, campos obligatorios.
- **Suplentes**: hasta 7, se pueden añadir o eliminar.

---

## Desarrollo y personalización

- **Cambios en la interfaz**: edita `app/src/main/assets/index.html`.
- **Cambios en el comportamiento nativo**: edita `MainActivity.kt`.
- **Estilos**: modifica las variables CSS en el `index.html`.

Después de cada cambio, prueba en un emulador y en un dispositivo físico para verificar el comportamiento.

---

## Versiones

El proyecto sigue un versionado semántico simplificado: `MAJOR.MINOR` (ej. `1.0`, `1.1`, `2.0`).  
Los APKs estables se publican en **GitHub Releases**; no se almacenan en el repositorio.

---

## Licencia

Este proyecto está bajo la licencia **MIT**.  
Consulta el archivo [`LICENSE`](LICENSE) para más detalles.

---

## Autor

**Kveld** — [GitHub](https://github.com/kveld9)
