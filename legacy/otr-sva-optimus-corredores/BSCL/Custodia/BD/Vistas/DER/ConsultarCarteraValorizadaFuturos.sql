SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::DER.ConsultarCarteraValorizadaFuturos";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::DER.ConsultarCarteraValorizadaFuturos" 
AS

SELECT 
	 CONCAT (PER."Nombres",CONCAT(' ',CONCAT(PER."ApellidoPaterno",CONCAT(' ',PER."ApellidoMaterno")))) AS "NombreCliente"
	,CAR."Nemotecnico" 				 AS "Instrumento"
	,TO_VARCHAR(CAR."FechaVencimientoOperacion")  AS "FechaVencimiento"
	,TOPT."Accion"					 AS "InteresAbierto"
	,"INGCantidad" 					 AS "Cantidad" 
	,CAR."INGPrecioMercadoAnterior"  AS "PrecioAnterior"
	,CAR."INGPrecioMercadoActual"    AS "PrecioBase"
	,CAR."INGMontoATasaMercadoActual"-CAR."INGMontoATasaMercadoAnterior" AS "ResultadoDiario"
	,CAR."INGMontoATasaMercadoActual"- CAR."MontoCompra"		 AS "ResultadoAcumulado"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = CAR."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPT ON TOPT."IdTipoOperacion"= CAR."IdTipoOperacion" 
WHERE CAR."IdEstado" = 'VIG'
AND   CAR."IdClasificacionTipoInstrumento" ='DV'
AND   CAR."IdTipoInstrumento" IN ('FDO','FIP','FBC','FIC','FUF','FBO')
WITH READ ONLY;

GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::DER.ConsultarCarteraValorizadaFuturos" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO
