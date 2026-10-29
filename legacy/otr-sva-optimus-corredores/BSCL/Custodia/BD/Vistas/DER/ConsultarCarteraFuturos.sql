SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::DER.ConsultarCarteraFuturos";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::DER.ConsultarCarteraFuturos" 
AS 
SELECT 
	CAR."IdCartera"																AS "NumeroCargoAbono",
	CAR."IdCliente"																AS "IdCliente",
	CAR."DocumentoIdentidad"													AS "DocumentoIdentidad",
	CAR."IdPortafolio"															AS "IdPortafolio",		
	CAR."CodigoPortafolio"														AS "CodigoPortafolio",
	CAR."RazonSocial"															AS "RazonSocial",
   	CAR."IdCondicionLiquidacion"												AS "IdCondicionDeLiquidacion",	
	TO_VARCHAR(CAR."FechaLiquidacion",'YYYY-MM-DD')								AS "FechaCondicionDeLiquidacion",
	CAR."INGMontoATasaMercadoActual"											AS "MontoATasaMercadoActual",
	CAR."INGMontoATasaMercadoAnterior"											AS "MontoATasaMercadoAnterior",
	TO_BIGINT(0)																AS "PrecioSubyacente",
	CAR."INGPrecioMercadoAnterior"												AS "PrecioMercadoAnterior",
	CAR."Cantidad"																AS "Cantidad",
	INS."Tamano"																AS "Tamano",
	TO_VARCHAR(CAR."FechaOperacion",'YYYY-MM-DD')								AS "FechaOperacion",			
	TO_VARCHAR(CAR."FechaLiquidacion",'YYYY-MM-DD')								AS "FechaLiquidacionOperacion",	
	TO_VARCHAR(CAR."FechaVencimientoOperacion",'YYYY-MM-DD')					AS "FechaVencimientoOperacion",
	CAR."IdTipoComprobante"														AS "IdTipoComprobante",	
	TO_VARCHAR(CAR."FolioComprobante")											AS "FolioComprobante",	
	COALESCE(INS."EsPutEsCall",'') 												AS "TipoMovimientoAsociado",
	TO_VARCHAR(CAR."FechaComprobante",'YYYY-MM-DD')								AS "FechaComprobante",	
	OPE."Accion"																AS "InteresAbierto",
	INS."IdInstrumento"															AS "IdInstrumento",				
	INS."IdTipoInstrumento"														AS "IdTipoInstrumento",
	CAR."Nemotecnico"															AS "Nemotecnico",
	INS."NombreTipoInstrumento"													AS "NombreTipoInstrumento"

FROM 	  "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 				AS CAR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		AS INS ON (INS."Nemotecnico" 		= CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" 		AS OPE ON (OPE."IdTipoOperacion" 	= CAR."IdTipoOperacion")

WHERE 
		CAR."IdEstado" = 'VIG'
	AND CAR."IdClasificacionTipoInstrumento" = 'DV'
	AND OPE."IdTipoInstrumento" IN ('FDO','FIP', 'FBC', 'FIC','FUF', 'FBO')	
	

WITH READ ONLY;

GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::DER.ConsultarCarteraFuturos" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO