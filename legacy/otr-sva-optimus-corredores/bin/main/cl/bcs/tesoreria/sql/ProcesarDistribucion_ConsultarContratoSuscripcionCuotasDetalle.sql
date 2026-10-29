SELECT "CSCD"."Serie", 
       "CSCD"."Regimen", 
       "CSCD"."Monto", 
       "CSC"."FolioCSC" AS "IdFolioCSC", 
       "CSC"."IdPortafolio", 
       "CSC"."Estado" AS "EstadoContrato", 
       "POR"."IdEstado" AS "EstadoPortafolio",
       case when "CSC"."MontoUF" = 0 then 'CLP' else 'UF' end as "Moneda",
       "FON"."IdMoneda" AS "IdMonedaInstrumento",
       "CSC"."Canal"
FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotasDetalle" AS "CSCD"
INNER JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" AS "CSC" ON "CSC"."IdFolioCSC" = "CSCD"."IdFolioCSC"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "POR" ON ("POR"."IdPortafolio" = "CSC"."IdPortafolio")
LEFT  JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" AS "FON" ON ("FON"."IdSerieFondo" = "CSCD"."Serie")
WHERE "CSC"."FolioCSC" IN (:folios);