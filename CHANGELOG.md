# Changelog

## 8.2.0

### Features

- PW-109 - Seiteneinstellungen - Farbe
- PW-131 - Soundkarte auswählen
- PW-132 - Fading
- PW-134 - Kacheln Drag & Drop
- PW-146 - Tastatur-Mapping
- PW-147 - MIDI-Mapping
- PW-158 - Erweiterte Audio Playback Control Methoden
- PW-160 - Wiedergabegeschwindigkeit einstellen
- PW-161 - EoF Warnungsanimation soll bei Loop nicht angezeigt werden
- PW-166 - Autosave
- PW-168 - Neues Loop Icon
- PW-173 - EoF Animation soll leicht faden statt hart blinken
- PW-174 - Medien ersetzen - Ganzen Ordner auswählbar machen
- PW-175 - Medien ersetzen - Toast automatisch schließen wenn keine fehlenden Medien mehr
- PW-176 - Launchdialog stylen
- PW-182 - Linux Build

### Bugfixes

- PW-148 - Toasts sind nach dem Resizing des Hauptfensters verschoben
- PW-163 - Log Viewer lässt sich in der gebauten Version nicht öffnen
- PW-164 - Stopp-Fade-Out wird auch bei Pausezustand ausgeführt
- PW-165 - Projektname nicht mehr an mehreren Stellen speichern
- PW-167 - Debug Logging in der gebauten Version aktivierbar machen
- PW-177 - Kachel wird nicht mit Fehler markiert, wenn die verknüpfte Datei gelöscht wird
- PW-179 - Dialog für Verbindungsfehler stylen
- PW-199 - Liste des Mappingtabs refreshed sich nicht
- PW-202 - ScrollView Verhalten von Mapping View

## 8.1.1

### Bugfixes

- PW-129 - Kachel Play Color funktioniert nicht nach Speichern der Kacheleinstellungen
- PW-130 - Fehlende Mediendatei: Ordner-Button anzeigen

## 8.1.0

### Features

- PW-77 - Ramp-In / Intro
- PW-78 - End-of-File Warnhinweis
- PW-79 - Mediendateien per Drag & Drop hinzufügen
- PW-80 - Projektverwaltung
- PW-96 - Programmeinstellungen - Basics
- PW-99 - macOS Anwendung signieren
- PW-100 - Behandlung fehlender Mediendateien
- PW-110 - Seiten Import/Export aus anderen Projekten
- PW-119 - Remote Logging Server und zentrale View
- PW-121 - Scenic View integrieren
- PW-123 - Styling überarbeiten
- PW-125 - Suche in Projektverwaltung

### Bugfixes

- PW-103 - Alerts: Text im Header von Standarddialogen ist nicht deutsch
- PW-105 - Server System Tray: Portnummer der verbundenen Clients wird mit tausender Trennzeichen angezeigt
- PW-106 - Bei zu vielen Seiten ist die Menüleiste nicht mehr erreichbar
- PW-108 - Projekteinstellungen: Anzahl Pad pro Zeile/Spalte - bei Undo/Redo wird kein Text angezeigt
- PW-111 - Seiten anordnen - Dropzone flackert
- PW-112 - Kachel Fortschrittsanzeige: "verstrichene / Gesamtzeit" passt nicht von der Breite
- PW-114 - Windows: NativeAudio dll ist blockiert, wenn der Server über das SystemTray beendet wird
- PW-116 - Speichern der Kacheleinstellungen umgeht die globale Lautstärke
- PW-120 - Undo Operationen von einem geschlossenen Projekt möglich
- PW-128 - Langer Titel verdrängt ProgressBar

## 8.0.0

### Features

- PW-7 - Kachel Basics
- PW-9 - Kacheleinstellungen - Basics
- PW-12 - "Neues Projekt" anlegen
- PW-13 - Wiedergabefortschritt
- PW-18 - Projekteinstellungen Basics
- PW-52 - Undo / Redo
- PW-53 - Programmlautstärke
- PW-54 - Seitenverwaltung
- PW-60 - Kachelfarben

### Bugfixes

- PW-17 - RAM läuft voll bei Audio Wiedergabe
- PW-33 - Text ist in Dialogen abgeschnitten
- PW-45 - Beenden des Clients stoppt nicht die Audiowiedergabe
- PW-46 - loop funktioniert nur bei neu abspielen des Pads
- PW-51 - Fehlermeldungen vom Server werden im Client nicht mehr angezeigt
- PW-64 - "Kachel Zeit" bleibt nach löschen stehen
- PW-71 - PadNewMedia Undo: Kachel Titel wird nicht wiederhergestellt
