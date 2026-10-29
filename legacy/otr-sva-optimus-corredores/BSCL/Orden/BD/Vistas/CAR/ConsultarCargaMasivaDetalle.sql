SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Orden.BD.Vistas::CAR.ConsultarCargaMasivaDetalle";
GO
CREATE VIEW "BSCL.Orden.BD.Vistas::CAR.ConsultarCargaMasivaDetalle" AS
SELECT
	 CM."IdCargaMasiva"
	,TCM."IdTipoCargaMasiva"
	,TCM."CodigoTipoCargaMasiva"
	,CM."IdEmpresa"
	,CM."IdUsuario"
  	,CM."FechaCarga"   			-- Fecha Sistema
 	,CM."FechaProceso" 			-- Fecha en que se procesa CM
 	,CM."FechaIngresoRegistro"	-- Fecha de BD al ingreso
 	,CM."NombreArchivo"
	,CM."IdEstado" AS "IdEstadoCargaMasiva"
	,CMD."IdCargaMasivaDetalle"
	,CMD."ObservacionValidacion"
	,CMD."IdEstado" AS "IdEstadoCargaMasivaDetalle"
	,EST."Nombre" AS "NombreEstadoCargaMasivaDetalle"
	
	,CMD."DocumentoIdentidad"
	,CMD."CodigoPortafolio"
	,CMD."TipoOperacion"
	,CMD."IdMecanismoCaptacion"
	,CMD."Cantidad"
	,CMD."Nemotecnico"
	,CMD."Precio"
	,CMD."IdCondicionDeLiquidacion"
	,CMD."IdTipoVigenciaOrden"
	,CMD."FechaVigencia"
	,CMD."IdFormaCustodia"
	,CMD."IdBolsa"
	,CMD."IdBoveda"
	,CMD."IdTipoPrecioOrden"	
	,CMD."IdOperacionGenerada"
	
	,CMD."TipoInstruccion"
	,CMD."Emisor"
	,CMD."FechaVencimiento"
	,CMD."ValorRescate"
	,CMD."FechaTrade"
	,CMD."Intermediario"
	,CMD."FolioFactura"
	,CMD."MontoLiquidacion" 
	
	,CMD."IdTipoReajuste" 
	,CMD."TotalCobros" 
	,CMD."Tasa" 
	
FROM "BSCL.Orden.BD::CAR.CargaMasiva" AS CM
INNER JOIN "BSCL.Orden.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva")
INNER JOIN "BSCL.Orden.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = CMD."IdEstado")

WITH READ ONLY;

GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::CAR.ConsultarCargaMasivaDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;