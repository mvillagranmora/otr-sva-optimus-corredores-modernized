SELECT 
 CCOC."PortafolioAPV"
, CCOC."IdClasificacionTipoInstrumento"
, TO_NVARCHAR(CCOC."OpcionCobro") AS "OpcionCobro"
, CCOC."PorcentajeCobro"
, TO_NVARCHAR(CCOC."FechaVigencia", 'YYYY-MM-DD') AS "FechaVigencia"
FROM "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" AS CCOC 
WHERE CCOC."IdEstado" = 'ACT';