SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaCircular1518";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaCircular1518"
AS
SELECT 
	CUADRATURA."IdCuadraturaCircular1518"
	, CUADRATURA."IdCarga"
	, CUADRATURA."FechaCuadratura"
	, CUADRATURA."Nemotecnico"
	, CUADRATURA."IdInstrumento"
	, CUADRATURA."IdClasificacionTipoInstrumento"
	, CUADRATURA."IdTipoInstrumento"
	, CUADRATURA."CantidadCustodia"
	, CUADRATURA."CantidadGarantia"
	, CUADRATURA."CantidadVentaPlazo"
	, CUADRATURA."CantidadCompraPlazo"
	, CUADRATURA."TotalCustodia"
	, CUADRATURA."CompraEnTransito"
	, CUADRATURA."VentaEnTransito"
	, CUADRATURA."TransitoIngreso"
	, CUADRATURA."TransitoEgreso"
	, CUADRATURA."CantidadDCVCuentaRelacionado"
	, CUADRATURA."CantidadDisponibleDCVCuentaRelacionado"
	, CUADRATURA."CantidadBloqueadaDCVCuentaRelacionado"
	, CUADRATURA."CantidadReventaDCVCuentaRelacionado"
	, CUADRATURA."CantidadRecompraDCVCuentaRelacionado"
	, CUADRATURA."CantidadValorizadaUFDCVCuentaRelacionado"
	, CUADRATURA."CantidadDCVCuentaPropia"
	, CUADRATURA."CantidadDisponibleDCVCuentaPropia"
	, CUADRATURA."CantidadBloqueadaDCVCuentaPropia"
	, CUADRATURA."CantidadReventaDCVCuentaPropia"
	, CUADRATURA."CantidadRecompraDCVCuentaPropia"
	, CUADRATURA."CantidadValorizadaUFDCVCuentaPropia"
	, CUADRATURA."CantidadDCVCuentaTercero"
	, CUADRATURA."CantidadDisponibleDCVCuentaTercero"
	, CUADRATURA."CantidadBloqueadaDCVCuentaTercero"
	, CUADRATURA."CantidadReventaDCVCuentaTercero"
	, CUADRATURA."CantidadRecompraDCVCuentaTercero"
	, CUADRATURA."CantidadValorizadaUFDCVCuentaTercero"
	, CUADRATURA."CantidadBCS"
	, CUADRATURA."CantidadBEC"
	, CUADRATURA."CantidadBOV"
	, CUADRATURA."CantidadEmisores"
	, CUADRATURA."CantidadSorteoLetras"
	, CUADRATURA."TotalCustodioEmisores"
	, CUADRATURA."Diferencia"
	, CUADRATURA."Precio"
	, CUADRATURA."Valorizacion"
	, CUADRATURA."DiferenciaValorizada"
	, CUADRATURA."IdEstado"
	,CLASIFICACION_TIPO_INSTRUMENTO."Nombre"	AS "ClasificacionTipoInstrumento"
	,TIPO_INSTRUMENTO."Nombre" 					AS "TipoInstrumento"
	FROM "BSCL.Custodia.BD::CUS.CuadraturaCircular1518" CUADRATURA
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLASIFICACION_TIPO_INSTRUMENTO 
		ON CLASIFICACION_TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento" = CUADRATURA."IdClasificacionTipoInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPO_INSTRUMENTO
		ON TIPO_INSTRUMENTO."IdTipoInstrumento" = CUADRATURA."IdTipoInstrumento"
	WHERE CUADRATURA."IdEstado" <> 'ANU';
	
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaCircular1518" TO BCS_DESARROLLADOR WITH GRANT OPTION;
