# Definition of Done — RoadAssist

Een user story is **Done** wanneer aan alle onderstaande criteria is voldaan. Deze criteria gelden voor elke user story, ongeacht sprint of scope.

---

## 1. Functionele volledigheid

- [ ] Alle acceptatiecriteria van de user story zijn geïmplementeerd en handmatig getest.
- [ ] De functionaliteit werkt op zowel Android (API 26+) als Desktop (Windows/Linux), tenzij de story expliciet platformspecifiek is.
- [ ] Randgevallen die in de acceptatiecriteria worden beschreven zijn afgedekt.

## 2. Codekwaliteit

- [ ] De code volgt een gelaagde structuur: UI-logica staat in Composables, businesslogica in ViewModels, data-toegang in Repositories. (NF-04)
- [ ] Er zijn geen hardcoded waarden voor URLs, secrets of configuratie — deze staan in configuratiebestanden of omgevingsvariabelen.
- [ ] Publieke klassen en functies met niet-voor-de-hand-liggende gedrag zijn voorzien van een kort commentaar.
- [ ] Er zijn geen dode code, uitgecommentarieerde blokken of onopgeloste TODO-opmerkingen.
- [ ] De code is leesbaar en voorzien van commentaar waar nodig. (NF-06)

## 3. Testen

- [ ] Businesslogica (ViewModels, Repositories, Filters) is gedekt door ten minste één unit test die draait zonder emulator of apparaat. (NF-07)
- [ ] Alle bestaande tests slagen (`./gradlew :core:jvmTest :server:test :app:shared:jvmTest`).
- [ ] Nieuwe tests zijn toegevoegd voor elk niet-triviaal pad dat de story introduceert.

## 4. Platform en architectuur

De onderstaande criteria komen rechtstreeks uit de niet-functionele eisen van de casus (NF-01 t/m NF-08):

| ID | Criterium |
|----|-----------|
| NF-01 | De app werkt op Android (versie 8.0 of hoger) en op desktop (Windows en Linux). |
| NF-02 | De app heeft een eigen backend die alle meldingen en gebruikersdata beheert. De app communiceert alleen met die eigen backend. |
| NF-03 | Code die gedeeld kan worden tussen de backend en de app (zoals datamodellen) staat in een aparte gedeelde module. |
| NF-04 | De app volgt een duidelijke, gelaagde structuur waarbij de UI losstaat van de logica (MVVM of vergelijkbaar). |
| NF-05 | Het overzichtsscherm laadt binnen 2 seconden bij een normaal werkende verbinding. |
| NF-06 | De code is leesbaar en voorzien van commentaar waar nodig. Een README legt uit hoe het project gestart wordt. |
| NF-07 | De businesslogica is testbaar zonder dat er een apparaat of scherm voor nodig is. |
| NF-08 | Het project wordt bijgehouden in Git met duidelijke, regelmatige commits. |

## 5. Versiebeheer en oplevering

- [ ] De wijzigingen zijn gecommit met een beschrijvend commitbericht in het formaat `type(scope): omschrijving` (conventional commits). (NF-08)
- [ ] De commit staat op de juiste feature branch; er is niet rechtstreeks naar `master` of `staging` gecommit.
- [ ] De branch is samengevoegd via een Pull Request.
- [ ] Geen wachtwoorden, tokens of `.env`-bestanden zijn gecommit.
