# KcTapeTool
Laden und Speichern von KC85/X Programmen auf und vom PC

... in Arbeit!

## Bauen

Es sollte ein aktuelles JDK (>=17) im Pfad liegen oder die `JAVA_HOME` Umgebungsvariable sollte auf ein aktuelles JDK zeigen.

Im Hauptverzeichnis von KcTapeTools einmal 

```
  gradlew build
```

ausführen. Damit wird das Projekt compiliert und unter build/distributions ein Tar und ein Zip mit dem kompilierten Programm abgelegt. Das kann an 
beliebiger Stelle entpackt werde. Das KCTapeTool wird mit einem der Skripte im `bin/` Ordner gestartet.

## Starten

Nach dem Bauen oder dem herunterladen von [Releases](https://github.com/Hojoe42/KcTapeTool/releases) muss das jeweilige Archiv entpackt werden. 
Danach kann das Programm je nach Betriebssystem mit einem der Skripte im `bin/` Ordner gestartet werden. Auch hier gilt Java muss im Pfad liegen 
oder die Umgebungsvariable `JAVA_HOME` muss passend gesetzt sein.

Hier einige Möglichkeiten:

- `KcTapeTool.bat --help` zeigt die Hilfe:

```
D:\Programme\KcTapeTool-0.1.0>bin\KcTapeTool.bat --help
Usage: KcTapeTool [-hlV] [-d=<Datei | Soundausgang>]
                  [--directory=<Verzeichnis>] [-s=<Datei | Soundeingang>]
                  [--wait=<timeout>]
  -d, --destination=<Datei | Soundausgang>
                         Das Ausgabe Ziel zu dem geschrieben wird. Kann eine
                           Datei oder ein Sound Ausgang sein. Kann entfallen,
                           wenn sich der Dateiname aus den gelesenen Daten
                           ergibt
      --directory=<Verzeichnis>
                         Arbeitsverzeichnis in das Dateien geschrieben oder von
                           dem Dateien gelesen werden. Wenn nicht angegeben,
                           wird das aktuelle Verzeichnis verwendet
  -h, --help             Zeigt diese Hilfe und beendet das Programm
  -l, --list             Gibt die Sound Eingabe- und Ausgabekanäle aus.
  -s, --source=<Datei | Soundeingang>
                         Die Quelle, daraus wird gelesen. Kann eine Datei oder
                           ein Sound Eingang sein.
  -V, --version          Zeigt die Versioninfo an und beendet das Programm
      --wait=<timeout>   Definiert die Dauer der Wartezeit beim Lesen von einem
                           Soundeingang in Sekunden.
```

- `KcTapeTool.bat -l` listet die möglichen Sound Ein- und Ausgänge auf. Bei mir sind das zum Beispiel:

```
D:\Programme\KcTapeTool-0.1.0>bin\KcTapeTool.bat -l
Verfügbare Sound Eingabe Geräte (* default):
  (*) Primärer Soundaufnahmetreiber
      Kopfhörermikrofon (Razer Audio
      Eingang (Realtek(R) Audio)
      Mikrofon (C922 Pro Stream Webca
Verfügbare Sound Ausgabe Geräte (* default):
  (*) Primärer Soundtreiber
      Kopfhörer (Razer Audio Controller - Chat)
      Lautsprecher (Razer Audio Controller - Game)
      BenQ PD3200U (NVIDIA High Definition Audio)
```

- `KcTapeTool.bat -s Eingang`  wählt den Realtek Audio Line In Eingang aus und wartet auf ein eingehendes Signal. Der Name des Eingangs 
muss nicht vollständig angegeben sein, er muss nur eindeutig sein. Mit `Strg + C` kann das Warten abgebrochen werden. Alternativ, wenn 60 Sekunden 
(der Default) lang kein Signal erkannt wurde beendet sich das Programm.
- `KcTapeTool.bat -s NameEinerWaveDate.wav` wird anstelle der Soundkarte eine Wave Datei als Eingang verwendet und eine passende *.KCC Datei produziert
- `KcTapeTool.bat -s SCHOCKY2.KCC` nimmt die Datei `SCHOCKY2.KCC` und gibt diese auf dem default Ausgabekanal der Soundkarte aus.
- `KcTapeTool.bat -s SCHOCKY2.KCC -d Laut` gibt die Datei `SCHOCKY2.KCC` auf dem Lautsprecher aus.
 