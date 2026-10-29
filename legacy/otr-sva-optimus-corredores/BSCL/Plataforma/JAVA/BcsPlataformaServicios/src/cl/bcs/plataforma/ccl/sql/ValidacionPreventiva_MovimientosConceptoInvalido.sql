SELECT distinct car."IdConcepto" as "Valor1"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEgresosAnalitica" car
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" con on car."IdConcepto" = con."IdConcepto"
where con."IdConcepto" is null
union all
SELECT distinct car."IdConcepto"  as "Valor1"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraIngresosAnalitica" car
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" con on car."IdConcepto" = con."IdConcepto"
where con."IdConcepto" is null
union all
SELECT distinct car."IdConcepto"  as "Valor1"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversionAnalitica" car
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" con on car."IdConcepto" = con."IdConcepto"
where con."IdConcepto" is null
union all
SELECT distinct car."IdConcepto"  as "Valor1"
FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaAnalitica" car
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" con on car."IdConcepto" = con."IdConcepto"
where con."IdConcepto" is null
union all
SELECT distinct car."EGRIdConcepto"  as "Valor1"
FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaAnalitica" car
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" con on car."IdConcepto" = con."IdConcepto"
where con."IdConcepto" is null
