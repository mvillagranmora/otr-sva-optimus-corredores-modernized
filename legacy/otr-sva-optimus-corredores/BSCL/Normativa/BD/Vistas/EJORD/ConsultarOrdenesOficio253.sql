SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Normativa.BD.Vistas::EJORD.ConsultarOrdenesOficio253";

CREATE VIEW "BSCL.Normativa.BD.Vistas::EJORD.ConsultarOrdenesOficio253" AS 
 
SELECT 
 EO."FechaEjecucion"
,CLI."DocumentoIdentidad"
,POR."CodigoPortafolio"
,COALESCE(OPER."Nombre",'') AS "TipoOperacion"	
,EO."IdOrden"
,EO."Cantidad"
,COALESCE(INSTR."Nemotecnico",'') AS "Instrumento"	
,EO."IdCondicionDeLiquidacion"
,EO."Precio"
,EO."Monto"
,EO."IdCliente"
,EO."IdPortafolio"
,EO."FolioComprobanteFacturacion"
,CLI."NombreCompleto" 	
,EO."IdClasificacionTipoInstrumento"
,EO."IdTipoInstrumento"
,EO."IdTipoOperacion"
,EO."IdInstrumento"
,EO."IdMoneda"
,EO."IdBolsa"
,EO."PrecioEjecucion"
,EO."MontoTransado"
,EO."CantidadTransada"
,EO."MejorPrecioOtraBolsa"
,EO."CantidadMejorPrecioOtraBolsa"
,EO."MejorPrecioMercado"
,EO."PrecioMinimoMercado"
,EO."HoraPrecioMinimoMercado"
,EO."IdBolsaPrecioMinimoMercado"
,EO."PrecioMaximoMercado"
,EO."HoraPrecioMaximoMercado"
,EO."IdBolsaPrecioMaximoMercado"
,EO."MayorPrecioOperacionCorredor"
,EO."HoraPrecioMayorCorredor"
,EO."MenorPrecioOperacionCorredor"
,EO."HoraPrecioMenorCorredor"
FROM "BSCL.Normativa.BD.Vistas::EJORD.ConsultarEjecucionOrdenes" AS EO
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
     	ON POR."IdPortafolio" = EO."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
	ON (CLI."IdCliente"=EO."IdCliente")     	
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPER 
     	ON OPER."IdTipoOperacion" = EO."IdTipoOperacion"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTR 
     	ON INSTR."IdInstrumento" = EO."IdInstrumento"
LEFT OUTER JOIN  "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" REL
		ON REL."CodigoTipoIndicador"='1064PROP' AND REL."Codigo" =EO."CodigoRelacion";
		
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::EJORD.ConsultarOrdenesOficio253"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
