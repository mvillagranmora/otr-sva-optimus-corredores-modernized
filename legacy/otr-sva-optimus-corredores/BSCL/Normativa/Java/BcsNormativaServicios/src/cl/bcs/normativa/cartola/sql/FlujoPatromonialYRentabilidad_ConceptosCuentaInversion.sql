SELECT   CON."IdConcepto",CON."TipoConcepto",CTA."AfectaRentabilidad" 
FROM       "BSCL.Plataforma.BD::PLA.Concepto" as CON
INNER JOIN "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" CTA ON CON."IdConcepto" = CTA."IdConcepto"
