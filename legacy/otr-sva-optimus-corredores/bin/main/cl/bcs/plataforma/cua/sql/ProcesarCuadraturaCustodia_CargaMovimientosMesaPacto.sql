INSERT INTO "BSCL.Plataforma.BD::CUA.MovimientosCustodiaRF" ("Fecha","DocumentoIdentidad","TipoDocumentoIdentidad","IdPortafolio","CodigoPortafolio","IdCliente","Nemotecnico","IdInstrumento","IdTipoMovimiento","MontoOperacionFinal")
SELECT SUBSTRING (OP."FechaOperacion",0,10),Cli."DocumentoIdentidad",Cli."TipoDocumentoIdentidad",Port."IdPortafolio",Port."CodigoPortafolio",OP."IdCliente",
(CASE WHEN Cart."IdClasificacionTipoInstrumento" = 'IF' THEN Cart."NemoSVS" 
ELSE Cart."Nemotecnico" END) AS "Nemotecnico",
OP."IdInstrumento",OP."IdTipoOperacion",
CASE WHEN OP."IdTipoOperacion" IN ('COMPAC','COMTER')  THEN 
(CASE WHEN Cart."IdClasificacionTipoInstrumento"='IF' THEN Cart."INGValorRescate" 
ELSE Cart."INGCantidad" END)*(-1) ELSE (CASE WHEN Cart."IdClasificacionTipoInstrumento"='IF' THEN Cart."INGValorRescate" 
ELSE Cart."INGCantidad" END) END AS "INGCantidad"
FROM "BSCL.Mesa.BD::MES.Operacion" OP
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteUI" AS Cli ON Cli."IdCliente"= OP."IdCliente" 
LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS Cart ON Cart."IdOperacion"= OP."IdOperacion" AND Cart."FechaRespaldo"=(SELECT max("FechaRespaldo") FROM "BSCL.Custodia.BD::HIS.Cartera" WHERE "IdOperacion"=OP."IdOperacion")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as Port ON Port."IdPortafolio" = cart."IdPortafolio" 
WHERE OP."IdTipoProducto"='MERF' AND OP."IdEstado" IN ('VIG','ANT','LIQ') AND OP."FechaOperacion" >:p_FechaInicio AND OP."FechaOperacion" <=:p_FechaFin AND Cli."DocumentoIdentidad" IN (SELECT "DocumentoIdentidad" FROM "BSCL.Plataforma.BD::CUA.SaldosCuadraturaRF");