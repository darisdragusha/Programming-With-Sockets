# UDP Client-Server Aplikacioni 
##### Ky është një projekt i punuar nga studentë të vitit të trete të Universitetit "Hasan Prishtina"-Fakulteti i Inxhinierisë Elektrike dhe Kompjuterike në Lëndën "Rrjeta Kompjuterike"-Prof.Blerim Rexha dhe Asc.Mergim Hoti.

## Përshkrimi i projektit:
##### Ky aplikacion implementon një sistem klient-server duke përdorur protokollin UDP. Aplikacioni mundëson menaxhimin e file-ave në server, me kontroll të qasjes për klientët dhe logging të të gjitha veprimeve.

## Struktura e projektit:
##### Projekti përmban skedarët e mëposhtëm:

##### - Server.java: Implementon logjikën e serverit, duke përfshirë:
  - Menaxhimin e kërkesave të klientëve
  - Kontrollin e qasjes
  - Operacionet me file (lexim, shkrim, krijim, fshirje)
  - Logging të komunikimit

##### - Client.java: Implementon klientin, duke përfshirë:
  - Lidhjen me server
  - Dërgimin e kërkesave të enkriptuara
  - Ndërveprimin me përdoruesin

## Funksionalitetet kryesore:
##### 1. Menaxhimi i qasjes:
- REQUEST_FULL_ACCESS: Kërkesë për qasje të plotë
- RELEASE_FULL_ACCESS: Lëshimi i qasjes së plotë

##### 2. Operacionet me file:
- READ: Leximi i përmbajtjes së file-it
- WRITE: Shkruarja në file
- LIST: Listimi i file-ave
- CREATE: Krijimi i file-it të ri
- DELETE: Fshirja e file-it
- EXECUTE: Ekzekutimi i file-it

## Si të përdorim këtë projekt:

### Kërkesat paraprake:
- Java Development Kit (JDK) 8 ose më i ri
- Java Runtime Environment (JRE)

### Kompilimi i projektit:
bash
javac src/Server.java
javac src/Client.java

### Ekzekutimi:
1. Fillimisht startoni serverin:
bash
java Server <IP> <Port>
2. Pastaj startoni klientin:
bash
java Client <Server IP> <Server Port>

### Komandat e disponueshme:
- REQUEST_FULL_ACCESS: Kërkon qasje të plotë në server
- READ filename: Lexon përmbajtjen e file-it
- LIST: Liston të gjithë file-at në server
- WRITE filename content: Shkruan në file (kërkon qasje të plotë)
- CREATE filename: Krijon file të ri (kërkon qasje të plotë)
- DELETE filename: Fshin file-in (kërkon qasje të plotë)
- EXECUTE filename: Ekzekuton file-in (kërkon qasje të plotë)

## Siguria:
- Implementon sistem të kontrollit të qasjes
- Mban log të të gjitha veprimeve në server

## Rezultatet e pritshme nga ky projekt:
##### Ky projekt ofron një platformë të sigurt për komunikim klient-server dhe menaxhim të file-ave, duke demonstruar konceptet e rëndësishme të sigurisë së të dhënave, kontrollit të qasjes dhe komunikimit në rrjet.

## Kontribuesit në këtë projekt janë:
##### - [Dea Llapatinca](https://github.com/username)
##### - [Daris Dragusha](https://github.com/darisdr)
##### - [Dion Gashi](https://github.com/username3)
