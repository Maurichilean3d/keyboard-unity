# Keyboard Unity 🎹🖱

Convierte tu teléfono Android en un **teclado Bluetooth + trackpad** para controlar tu PC sin drivers adicionales.

---

## Requisitos

| Requisito | Versión mínima |
|---|---|
| Android Studio | **Iguana 2023.2.1** o superior |
| Android SDK | **API 34** (Android 14) |
| Build Tools | **34.0.0** |
| Teléfono Android | **Android 10** (API 29) o superior |
| PC | Cualquier PC con Bluetooth |

---

## Cómo compilar el APK en Android Studio

### Paso 1 — Descarga el proyecto

1. Haz clic en el botón verde **`<> Code`** → **`Download ZIP`**
2. Descomprime el ZIP (ej: `C:\Proyectos\keyboard-unity-main`)

---

### Paso 2 — Instala los componentes del SDK *(paso crítico)*

> ⚠️ Si salteas este paso el proyecto **no compilará**

1. Abre Android Studio
2. Ve a **`Tools → SDK Manager`**
3. En la pestaña **SDK Platforms**:
   - ✅ Marca **Android 14.0 ("UpsideDownCake") API Level 34**
4. En la pestaña **SDK Tools**:
   - ✅ Marca **Android SDK Build-Tools 34.0.0**
5. Haz clic en **`Apply`** → **`OK`** y espera que descargue

---

### Paso 3 — Abre el proyecto

1. En Android Studio: **`File → Open`**
2. Selecciona la carpeta **`keyboard-unity-main`** (la que contiene `settings.gradle`)
3. Cuando pregunte *"Trust Project?"* → **`Trust Project`**
4. Espera que termine la barra **"Gradle sync"** (puede tardar 2-5 min la primera vez, necesita internet para descargar dependencias)

---

### Paso 4 — Genera el APK

1. **`Build → Build Bundle(s) / APK(s) → Build APK(s)`**
2. Espera que compile (1-3 min)
3. Cuando aparezca la notificación **"APK(s) generated"** → haz clic en **`locate`**

El APK estará en:
```
keyboard-unity-main\app\build\outputs\apk\debug\app-debug.apk
```

---

### Paso 5 — Instala en el teléfono

**Opción A — Cable USB (más fácil):**
```bash
adb install app-debug.apk
```

**Opción B — Manual:**
1. Copia `app-debug.apk` al teléfono
2. En el teléfono: **Ajustes → Seguridad → Instalar apps de fuentes desconocidas** → Activar
3. Abre el archivo APK en el teléfono e instala

---

## Cómo usar la app

1. Abre **Keyboard Unity** en el teléfono
2. Menú (⋮) → **"Make Discoverable"** (el teléfono aparece visible 5 min)
3. En el PC: **Configuración → Bluetooth → Agregar dispositivo** → busca **"Keyboard Unity"** → empareja
4. La barra superior mostrará **"● Connected"**
5. Usa las dos pestañas:
   - **⌨ Keyboard** — teclado QWERTY completo
   - **🖱 Trackpad** — 1 dedo mueve cursor · 2 dedos hacen scroll · botones L/M/R

---

## Errores comunes

| Error | Solución |
|---|---|
| `Failed to find target 'android-34'` | Instala **API 34** en SDK Manager (Paso 2) |
| `Build Tools 34.0.0 not found` | Instala **Build-Tools 34.0.0** en SDK Manager (Paso 2) |
| `Gradle sync failed` | Verifica conexión a internet y repite el sync |
| `SDK location not found` | Android Studio lo crea automáticamente al abrir el proyecto |
