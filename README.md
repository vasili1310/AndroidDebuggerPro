# AndroidDebuggerPro 📱🛠️

**AndroidDebuggerPro** este o soluție desktop avansată, dezvoltată pentru a oferi o interfață grafică (GUI) puternică și intuitivă peste utilitarul adb (Android Debug Bridge). Acest instrument este esențial pentru dezvoltatorii Android, testerii QA și entuziaștii care doresc să monitorizeze, să depaneze și să gestioneze dispozitivele Android fără a depinde exclusiv de linia de comandă.
## 📖 Cuprins
 * Despre Proiect
 * Funcționalități Detaliate
 * Cerințe de Sistem
 * Instalare și Configurare
 * Ghid de Utilizare
 * Depanare (Troubleshooting)
 * Contribuții
 * Licență
## 💡 Despre Proiect
În mod tradițional, lucrul cu ADB necesită memorarea a zeci de comenzi complexe. **AndroidDebuggerPro** abstractizează aceste comenzi într-o interfață modernă, permițând vizualizarea simultană a log-urilor, gestionarea fișierelor și monitorizarea performanței sistemului într-un singur panou de control.
## ✨ Funcționalități Detaliate
### 1. Logcat Pro (Monitorizare Log-uri)
 * **Filtrare Multi-Nivel:** Filtrează mesajele după tag, proces ID (PID) sau nivel de prioritate (Verbose, Debug, Info, Warning, Error, Fatal).
 * **Căutare în Timp Real:** Identifică rapid excepțiile (NullPointerException, ANR) folosind regex sau cuvinte cheie.
 * **Auto-Scroll & Pause:** Îngheață fluxul de log-uri pentru a analiza o anumită secvență fără a pierde datele noi în fundal.
### 2. File Explorer (Gestionare Fișiere)
 * Interfață de tip "drag-and-drop" pentru transferul fișierelor între PC și dispozitiv.
 * Permite navigarea în partițiile de sistem (necesită drepturi de Root pentru anumite directoare).
 * Previzualizare rapidă pentru fișierele text și imagini direct din dispozitiv.
### 3. Manager de Aplicații
 * Listarea tuturor pachetelor instalate (sistem vs. utilizator).
 * Instalare de fișiere APK/APKS prin simplă glisare.
 * Dezinstalare forțată, ștergere cache și oprire procese (Force Stop) dintr-un singur clic.
### 4. Monitorizare Resurse & Screen Mirroring
 * **CPU & RAM:** Grafice live care indică impactul aplicației tale asupra resurselor dispozitivului.
 * **Screen Capture:** Realizează screenshot-uri de înaltă rezoluție și înregistrări video ale ecranului (utile pentru rapoarte de bug-uri).
 * **Control de la Distanță:** Simulează atingeri, gesturi de swipe și input de la tastatură direct de pe PC.
## 💻 Cerințe de Sistem
Pentru o funcționare optimă, asigurați-vă că mediul dumneavoastră îndeplinește:
 * **Sistem de Operare:** Windows 10/11, macOS (Intel/M1/M2) sau distribuții Linux populare (Ubuntu/Fedora).
 * **Java:** JRE/JDK 11 sau o versiune mai nouă instalată.
 * **ADB:** Trebuie să fie instalat și adăugat în variabila de mediu PATH.
 * **Hardware:** Un cablu USB de calitate (recomandat USB 3.0) pentru un flux de date stabil.
## 🛠 Instalare și Configurare
### Pasul 1: Pregătirea Dispozitivului
 1. Accesați **Settings** > **About Phone**.
 2. Apăsați de 7 ori pe **Build Number** pentru a activa "Developer Options".
 3. În meniul nou apărut, activați **USB Debugging**.
### Pasul 2: Instalarea Aplicației
Puteți descărca ultima versiune din secțiunea Releases sau o puteți compila manual:
```bash
# Clonează repository-ul
git clone https://github.com/vasili1310/AndroidDebuggerPro.git

# Navighează în director
cd AndroidDebuggerPro

# Compilează folosind Gradle (Windows)
gradlew.bat build

# Compilează folosind Gradle (Linux/macOS)
./gradlew build

```
## 🚀 Ghid de Utilizare
 1. **Conectare:** Lansați aplicația și conectați dispozitivul. Dacă dispozitivul cere permisiunea "Allow USB Debugging?", selectați "Always allow from this computer".
 2. **Selectare Dispozitiv:** Dacă aveți mai multe dispozitive conectate (sau emulatoare), selectați-l pe cel dorit din meniul dropdown.
 3. **Analiza Log-urilor:** Mergeți la tab-ul Logcat, apăsați Connect și scrieți numele pachetului aplicației voastre în câmpul de filtrare pentru a vedea doar mesajele relevante.
 4. **Transfer Fișiere:** Accesați File Manager, navigați la /sdcard/Download și trageți orice fișier de pe calculatorul dvs. în acea fereastră.
## 🔍 Depanare (Troubleshooting)
 * **Dispozitivul nu este detectat:** - Verificați cablul USB.
   * Rulează adb kill-server urmat de adb start-server într-un terminal.
   * Asigurați-vă că driverele OEM (Samsung, Huawei, Google etc.) sunt instalate corect.
 * **Aplicația se închide neașteptat:**
   * Verificați versiunea de Java instalată cu comanda java -version. Trebuie să fie minim 11.
 * **Erori de permisiuni (Permission Denied):**
   * Unele foldere de sistem necesită acces Root. Asigurați-vă că dispozitivul este rootat dacă aveți nevoie de acces complet.
## 🤝 Contribuții
Vrei să faci AndroidDebuggerPro mai bun? Suntem deschiși la contribuții!
 1. Fă un **Fork** proiectului.
 2. Creează un branch pentru feature-ul tău (git checkout -b feature/AmazingFeature).
 3. Dă **Commit** modificărilor (git commit -m 'Add some AmazingFeature').
 4. Dă **Push** pe branch (git push origin feature/AmazingFeature).
 5. Deschide un **Pull Request**.
