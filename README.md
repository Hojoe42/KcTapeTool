# KcTapeTool
Laden und Speichern von KC85/X Programmen auf und vom PC

... in Arbeit!

## Bauen

im Hauptverzeichnis einmal 

```
  gradlew build
```

ausführen. Damit wird das Projekt compiliert und unter build/distributions ein Tar und ein Zip mit dem kompilierten Programm abgelegt. Das kann an 
beliebiger Stelle entpackt werde. Das KCTapeTool wird mit einem der Skripte im `bin/` Ordner gestartet.

## Starten

Nach dem Entpacken kann das Programm je nach Betriebssystem mit einem der Skripte im `bin/` Ordner gestartet werden. Hier einige Möglichkeiten:

- `KcTapeTool.bat read help` zeigt die Hilfe für das 'read' Kommando.
- `KcTapeTool.bat read -l` listet die möglichen Sound Eingänge auf. Bei mir sind das zum Beispiel:

```
Verfügbare Input Geräte:
  Primärer Soundaufnahmetreiber
  Kopfhörermikrofon (Razer Audio
  Eingang (Realtek(R) Audio)
  Mikrofon (C922 Pro Stream Webca
```

- `KcTapeTool.bat read -i Eingang`  wählt den Realtek Audio Line In Eingang aus und wartet auf ein eingehendes Signal. Der Name muss nicht 
vollständig angegeben sein, er muss nur eindeutig sein. Mit `Strg + C` kann das Warten abgebrochen werden.
- `KcTapeTool.bat read -f NameEinerWaveDate.wav` wird anstelle der Soundkarte eine Wave Datei als Eingang verwendet.

Beispiel:

```
KcTapeTool-0.1.0>bin\KcTapeTool.bat read -f d:\tmp\kc\caos.wav
Starte Aufnahme (Abbruch mit Ctrl + C)
Name: CAOS_E
01> 02> 03> 04> FF>
CAOS_E.KCC wurde geschrieben
```