# Lijst van User Stories — RoadAssist

Dit document bevat alle functionele eisen van RoadAssist, zoals vastgelegd in de casus, aangevuld met formele user stories en acceptatiecriteria per feature.

---

## Functionele eisen

De prioriteiten zijn gebaseerd op het MoSCoW-model (Must / Should / Could).

| ID | Rol | Omschrijving | Prioriteit |
|----|-----|--------------|------------|
| FR-01 | Beide | De gebruiker kan inloggen met een gebruikersnaam en wachtwoord. | Must |
| FR-02 | Beide | Na het inloggen ziet de gebruiker de weergave die bij zijn rol hoort. | Must |
| FR-03 | Weggebruiker | De gebruiker kan een nieuwe melding aanmaken met een beschrijving en categorie (bvb. pech, ongeluk, wegversperring). | Must |
| FR-04 | Weggebruiker | Bij het aanmaken van een melding wordt de huidige locatie automatisch opgehaald via de locatiefunctie van het apparaat. | Must |
| FR-05 | Weggebruiker | De gebruiker kan optioneel een foto toevoegen aan een melding via de camera van het apparaat. | Must |
| FR-06 | Weggebruiker | De gebruiker ziet een overzicht van zijn actieve meldingen met de huidige status. | Must |
| FR-07 | Weggebruiker | De gebruiker heeft een apart scherm met zijn meldingsgeschiedenis, inclusief afgehandelde meldingen. | Must |
| FR-08 | Weggebruiker | De gebruiker kan de details van een eigen melding bekijken. | Must |
| FR-09 | Dispatcher | De dispatcher ziet een overzicht van alle inkomende meldingen, gesorteerd op tijdstip. | Must |
| FR-10 | Dispatcher | De dispatcher kan de details van een melding bekijken, inclusief locatie, beschrijving en foto. | Must |
| FR-11 | Dispatcher | De dispatcher kan de status van een melding aanpassen. | Must |
| FR-12 | Beide | De app toont een melding als er geen verbinding is met de server. | Must |
| FR-13 | Beide | De gebruiker kan uitloggen. | Should |
| FR-14 | Dispatcher | De dispatcher kan meldingen filteren op status of categorie. | Should |
| FR-15 | Dispatcher | De dispatcher kan een opmerking toevoegen aan een melding (bvb. "Hulpverlener Jan is onderweg"). | Should |
| FR-16 | Weggebruiker | De gebruiker ontvangt een melding op het apparaat wanneer de status van zijn melding verandert. | Could |
| FR-17 | Dispatcher | De dispatcher ziet de locatie van een melding op een kaart. | Could |
| FR-18 | Beide | De app ondersteunt een webversie met dezelfde basisfunctionaliteit. | Could |

## User Stories

### Authenticatie en navigatie

#### US-01 — Inloggen
**Als** weggebruiker of dispatcher
**wil ik** kunnen inloggen met mijn gebruikersnaam en wachtwoord
**zodat** ik toegang krijg tot mijn persoonlijke weergave.

**Traceerbaarheid:** FR-01, FR-02

**Acceptatiecriteria:**
- Het inlogscherm toont velden voor gebruikersnaam en wachtwoord.
- Bij correcte inloggegevens wordt de gebruiker doorgestuurd naar de rol-specifieke startpagina.
- Bij onjuiste inloggegevens verschijnt een foutmelding.
- Tijdens het laden is een laadspinner zichtbaar.
- Het wachtwoordveld maskeert de invoer.

#### US-02 — Uitloggen
**Als** ingelogde gebruiker
**wil ik** kunnen uitloggen
**zodat** mijn sessie wordt beëindigd en mijn gegevens niet toegankelijk zijn voor anderen.

**Traceerbaarheid:** FR-13

**Acceptatiecriteria:**
- Na uitloggen is het token gewist en keert de back-knop niet terug naar een beveiligd scherm.
- De uitlogoptie is bereikbaar vanuit de navigatie.

### Meldingen aanmaken

#### US-03 — Melding indienen
**Als** weggebruiker
**wil ik** een nieuwe melding aanmaken met categorie en beschrijving
**zodat** een dispatcher op de hoogte is van mijn situatie op de weg.

**Traceerbaarheid:** FR-03, FR-04, FR-05

**Acceptatiecriteria:**
- Het formulier bevat een verplicht beschrijvingsveld en een verplichte categorieselectie (Pech, Ongeluk, Wegversperring, Overig).
- De locatie wordt automatisch opgehaald bij het openen van het scherm; op Desktop is handmatige invoer van coördinaten mogelijk.
- Optioneel kan een foto worden toegevoegd via de camera of galerij (Android) of een bestandsdialoog (Desktop).
- Indienen zonder beschrijving toont een validatiefout en verstuurt de melding niet.
- Na succesvolle indiening navigeert de app terug naar het overzicht van actieve meldingen.
- De knop om een melding in te dienen is niet zichtbaar wanneer de server onbereikbaar is.

### Meldingen volgen (weggebruiker)

#### US-04 — Actieve meldingen bekijken
**Als** weggebruiker
**wil ik** een overzicht zien van mijn actieve meldingen met de huidige status
**zodat** ik weet of mijn melding wordt behandeld.

**Traceerbaarheid:** FR-06

**Acceptatiecriteria:**
- Alleen niet-opgeloste meldingen van de ingelogde gebruiker worden weergegeven.
- De lijst is gesorteerd op aanmaakdatum (nieuwste bovenaan).
- Pull-to-refresh werkt op Android; een verversknop is beschikbaar op Desktop.
- Bij een lege lijst verschijnt een legestaat met de knop "Melding indienen" (alleen als de server bereikbaar is).

#### US-05 — Meldingsgeschiedenis bekijken
**Als** weggebruiker
**wil ik** een apart scherm hebben met mijn afgehandelde meldingen
**zodat** ik een overzicht heb van alle eerdere incidenten.

**Traceerbaarheid:** FR-07

**Acceptatiecriteria:**
- Alleen meldingen met status RESOLVED worden weergegeven.
- De lijst is gesorteerd op afrondingsdatum (nieuwste bovenaan).
- Tikken op een rij opent het detailscherm.

#### US-06 — Meldingsdetail bekijken (weggebruiker)
**Als** weggebruiker
**wil ik** de details van een eigen melding kunnen bekijken
**zodat** ik de volledige informatie en eventuele opmerkingen van de dispatcher zie.

**Traceerbaarheid:** FR-08

**Acceptatiecriteria:**
- Het scherm toont: categorie, beschrijving, locatie, statusbadge, foto (indien aanwezig) en dispatcher-opmerkingen.
- De foto laadt asynchroon; een placeholder is zichtbaar tijdens het laden.
- De fotosectie is verborgen als er geen foto is.
- De opmerkingenlijst is verborgen als er geen opmerkingen zijn.

### Dispatcher-operaties

#### US-07 — Alle meldingen bekijken
**Als** dispatcher
**wil ik** een overzicht zien van alle inkomende meldingen, gesorteerd op tijdstip
**zodat** ik snel kan reageren op nieuwe incidenten.

**Traceerbaarheid:** FR-09

**Acceptatiecriteria:**
- Alle meldingen van alle weggebruikers worden weergegeven.
- De lijst is gesorteerd op aanmaakdatum (nieuwste bovenaan).
- Realtime updates via SSE verschijnen zonder handmatige verversing.

#### US-08 — Meldingsdetail bekijken (dispatcher)
**Als** dispatcher
**wil ik** de details van een melding bekijken, inclusief locatie, beschrijving en foto
**zodat** ik een goed beeld heb van de situatie voor ik actie onderneem.

**Traceerbaarheid:** FR-10

**Acceptatiecriteria:**
- Het scherm toont: categorie, beschrijving, GPS-coördinaten, statusbadge, foto (indien aanwezig) en bestaande opmerkingen.
- De foto laadt asynchroon.

#### US-09 — Status van een melding aanpassen
**Als** dispatcher
**wil ik** de status van een melding kunnen aanpassen
**zodat** de weggebruiker en mijn collega's weten in welke fase de afhandeling zit.

**Traceerbaarheid:** FR-11

**Acceptatiecriteria:**
- De beschikbare statussen zijn: Nieuw, In behandeling, Onderweg, Afgehandeld.
- Na opslaan is de bijgewerkte status direct zichtbaar in het overzicht (optimistische update).
- Bij een API-fout wordt de vorige status hersteld en verschijnt een foutmelding.

#### US-10 — Meldingen filteren
**Als** dispatcher
**wil ik** meldingen kunnen filteren op status en/of categorie
**zodat** ik snel de relevante meldingen kan vinden.

**Traceerbaarheid:** FR-14

**Acceptatiecriteria:**
- Filters voor status en categorie zijn beschikbaar boven de lijst.
- Gecombineerde filters passen AND-logica toe.
- Filterinstellingen blijven behouden bij automatisch vernieuwen via SSE.

#### US-11 — Opmerking toevoegen aan een melding
**Als** dispatcher
**wil ik** een opmerking kunnen toevoegen aan een melding
**zodat** ik de weggebruiker kan informeren over de voortgang.

**Traceerbaarheid:** FR-15

**Acceptatiecriteria:**
- Een tekstveld voor opmerkingen is beschikbaar op het detailscherm van de dispatcher.
- Opmerkingen verschijnen bij de weggebruiker zodra de SSE-update binnenkomt.
- Een opmerking van meer dan 1000 tekens wordt geweigerd met een foutmelding.

### Connectiviteit en realtime

#### US-12 — Offline-melding
**Als** gebruiker
**wil ik** zien wanneer er geen verbinding is met de server
**zodat** ik weet dat de informatie mogelijk verouderd is.

**Traceerbaarheid:** FR-12

**Acceptatiecriteria:**
- Een offline banner verschijnt bovenin het scherm zodra de server onbereikbaar is.
- De banner verdwijnt automatisch zodra de verbinding hersteld is.
- De knop om een melding in te dienen is verborgen in de offline staat.
