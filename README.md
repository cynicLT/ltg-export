# LTG Export

## About
This application exports KR99 and KR99 data from LTG into user defined CSV format.

## Installation
Download latest version from [releases](https://github.com/cynicLT/ltg-export/releases) and install (Windows only version).

## Configuration
Default export contains two different exports for KR99 and KR52.

```yaml
export:
  - name: KR99_Default
    type: KR99
    fields:
      - index: 1
        label: Galinė stotis
        path: $.KR99Vaztarastis.GalineStotis
        transform: station.apply(value)
      - index: 2
        label: Siuntėjas
        path: $.KR99Vaztarastis.LietuvosSiuntejas.Pavadinimas
      - index: 3
        label: Gavėjas
        path: $.KR99Vaztarastis.LietuvosGavejas.Pavadinimas
      - index: 4
        label: Uosto ekspeditorius/Įgaliota įmonė
        path: $.KR99Vaztarastis.UostoEkspeditorius
      - index: 5
        label: Krovos kompanija
        path: $.KR99Vaztarastis.KrovosKompanija
      - index: 6
        label: Krovinys BKN
        path: $.KR99Vaztarastis.KR99Kroviniai.KrovinioPavLT
      - index: 7
        label: Masė, kg
        path: $.KR99Vaztarastis.KR99Kroviniai.MaseKrovinioKG
      - index: 8
        label: Išsiųstas
        path: $.KR99Vaztarastis.IsvykimoLaikas
      - index: 9
        label: Atvyko
        path: $.KR99Vaztarastis.AtvykimoLaikas
      - index: 10
        label: Išduotas
        path: $.KR99Vaztarastis.IsdavimoLaikas
      - index: 11
        label: Suma (bendra su PVM)
        path: $.KR99Vaztarastis.KR99Mokesciai[*].SumaSuPVMValiuta
      - index: 12
        label: Suma (su PVM)
        path: $.KR99Vaztarastis.KR99Mokesciai[*].SumaSuPVMValiuta
      - index: 13
        label: Valiuta
        path: $.KR99Vaztarastis.KR99Mokesciai[*].VezimoTarifoValiuta
  - name: KR52_Default
    type: KR52
    fields:
      - index: 1
        label: Siuntos Numeris
        path: $.KR52Vaztarastis.SiuntosNumeris
      - index: 2
        label: Periodas
        path: $.KR52Vaztarastis.Periodas
      - index: 3
        label: Pradinė stotis
        path: $.KR52Vaztarastis.PradineStotis
        transform: station.apply(value)
      - index: 4
        label: Galinė stotis
        path: $.KR52Vaztarastis.GalineStotis
        transform: station.apply(value)
      - index: 5
        label: Paraiškos nr
        path: $.KR52Vaztarastis.ParaiskosNr
      - index: 6
        label: Paraiškos pakodis
        path: $.KR52Vaztarastis.ParaiskosPakodis
      - index: 7
        label: Krovinio kodas (važtaraštis)
        path: $.KR52Vaztarastis.KrovinioKodas
      - index: 8
        label: Krovinio pavadinimas
        path: $.KR52Vaztarastis.KrovinioPav
      - index: 9
        label: Bendra krovinio mase (kg)
        path: $.KR52Vaztarastis.MaseKrovinioKG
      - index: 10
        label: Siuntėjo mokėsčiai tranzitu
        path: $.KR52Vaztarastis.SiuntejoMokesciaiTranzitu
      - index: 11
        label: Išvykimo laikas
        path: $.KR52Vaztarastis.IsvykimoLaikas
      - index: 12
        label: Bendras krovino kodas (ETSNG)
        path: $.KR52Vaztarastis.KrovinioKodasETSNG
      - index: 13
        label: Siuntėjas
        path: $.KR52Vaztarastis.Siuntejas.Pavadinimas
      - index: 14
        label: Gavėjas
        path: $.KR52Vaztarastis.Gavejas.Pavadinimas
      - index: 15
        label: Krovinio kodas (krovinys)
        path: $.KR52Vaztarastis.KR52Kroviniai.KrovinioKodas
      - index: 16
        label: Krovinio pavadinimas (LT)
        path: $.KR52Vaztarastis.KR52Kroviniai.KrovinioPavLT
      - index: 17
        label: Krovinio pavadinimas (RU)
        path: $.KR52Vaztarastis.KR52Kroviniai.KrovinioPavRU
      - index: 18
        label: Krovinio masė (kg)
        path: $.KR52Vaztarastis.KR52Kroviniai.MaseKrovinioKG
      - index: 19
        label: Krovino kodas (ETSNG)
        path: $.KR52Vaztarastis.KR52Kroviniai.KrovinioKodasETSNG
      - index: 20
        label: Vagono numeris
        path: $.KR52Vaztarastis.KR52Vagonai.Numeris
      - index: 21
        label: Savininikas
        path: $.KR52Vaztarastis.KR52Vagonai.Savininkas
      - index: 22
        label: Spaudo data
        path: $.KR52Vaztarastis.KR52PasienioStotis[*].SpaudasData
      - index: 23
        label: Ekspeditorius
        path: $.KR52Vaztarastis.KR52Ekspeditoriai[*].Ekspeditorius
      - index: 24
        label: Ekspeditoriaus kodas
        path: $.KR52Vaztarastis.KR52Ekspeditoriai[*].EkspeditoriusKodas
      - index: 25
        label: Ekspeditoriaus duomenys spausdinimui
        path: $.KR52Vaztarastis.KR52Ekspeditoriai[*].EkspeditoriausDuomenysSpausdinimui
      - index: 26
        label: Ne vežėjui skirta informacija
        path: $.KR52Vaztarastis.NeGelZymos
        filter: StringUtils.containsIgnoreCase(value, "БАЛТИК КАРГО АГЕНТ")
      - index: 27
        label: Pakodis
        path: $.KR52Vaztarastis.NeGelZymos
        filter: StringUtils.containsIgnoreCase(value, "БАЛТИК КАРГО АГЕНТ")
        transform: RegExUtils.dotAllMatcher("БАЛТИК\\s+КАРГО\\s+АГЕНТ.*ПОДКОД\s*-?\s*\\d+\\s*/\\s*(\\d+)", value).results().findFirst().map({ it.group(1) }).orElse("")
```

If there is need exports may be different. Just create `application.yaml` file in the root directory of installed application.

There are possibility to filter and/or transform values using `filter`\ `transform` expressions on field. Expressions are `groovy` based.
* Value of field has name `value`
* Apache Commons 3 following utils are imported `StringUtils`, `RegExUtils`
* for station code lookup to name use `station` function. Call example `station.apply(value)` Function covers majority of post USSR countries rail stations.

## Access LTG services
To access LTG services Your IP must be white listed by LTG. 
