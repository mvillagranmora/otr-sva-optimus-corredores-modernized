SELECT count(*) as "Existe"
FROM "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro"
WHERE "IdOpcionCobro"=:IdOpcionCobro OR ("OpcionCobro"=:OpcionCobro
AND "PortafolioAPV"=:PortafolioAPV
AND "IdClasificacionTipoInstrumento"=:IdClasificacionTipoInstrumento);