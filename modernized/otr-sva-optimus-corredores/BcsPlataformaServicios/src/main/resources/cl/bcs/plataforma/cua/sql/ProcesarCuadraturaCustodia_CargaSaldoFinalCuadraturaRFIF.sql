UPDATE "BSCL.Plataforma.BD::CUA.SaldosCuadraturaRF" scuad 
SET scuad."SaldoFinalSistema" = (
 SELECT SUM(CASE WHEN cart."IdClasificacionTipoInstrumento"='IF' THEN cart."INGValorRescate" 
ELSE cart."INGCantidad" END) AS INGCantidad FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraUI" cart
WHERE cart."IdClasificacionTipoInstrumento" in ('RF') AND cart."IdEstado"  NOT IN ('ANU','RPA') AND cart."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC', 'FWD','RRV','RRF') 
AND (cart."FechaOperacion" <= :p_FechaFin AND cart."EGRCantidad" = 0 AND cart."IdEstado" = 'VIG')
AND (cart."DocumentoIdentidad"=scuad."DocumentoIdentidad" AND scuad."IdPortafolio"=cart."IdPortafolio" AND scuad."IdInstrumento"=cart."IdInstrumento" AND scuad."CodigoPortafolio"=cart."CodigoPortafolio" AND scuad."Nemotecnico"=cart."Nemotecnico")
GROUP BY cart."DocumentoIdentidad",cart."IdPortafolio",cart."IdInstrumento"
 )  WHERE scuad."IdClasificacionTipoInstrumento"='RF';