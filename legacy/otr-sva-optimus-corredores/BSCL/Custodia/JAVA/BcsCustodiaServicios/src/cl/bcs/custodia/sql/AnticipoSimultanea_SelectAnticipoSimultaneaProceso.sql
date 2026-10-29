
WITH DatosSimultanea AS
(
	SELECT  
		ROW_NUMBER() over (PARTITION BY SUB."FolioCarteraRelacionada" ORDER BY SUB."FolioCarteraRelacionada" DESC, SUB."IdSubyacente" DESC) AS "Posicion",
		
		PRO."EsSimultaneaEnviada",
		PRO."IdCartera",
		PRO."FolioCartera",
		PRO."FolioCarteraOD",
		PRO."IdCliente",
		PRO."IdPortafolio",
		PRO."IdInstrumento",
		PRO."IdTipoProducto",
		PRO."IdMonedaSistema",
		PRO."IdBoveda",
		PRO."IdBolsa",
		PRO."FechaOperacion",
		PRO."FolioExternoReferencia",
		PRO."IdConcepto",
		PRO."IdTipoOperacion",
		PRO."IndicadorOrdenDirecta",
		
		-- Datos del Subyacente
		SUB."IdInstrumento" AS "IdInstrumentoSubyacente",
		SUB."IdMonedaInstrumento" AS "IdMonedaInstrumentoSubyacente",
		SUB."IdTipoReajuste" AS "IdTipoReajusteSubyacente",
		SUB."IdEmisor" AS "IdEmisorSubyacente",
		SUB."Nemotecnico" AS "NemotecnicoSubyacente", 
		
		CAR."FechaVencimientoOperacion",
		CAR."FechaLiquidacion",
		CAR."INGCantidad",
		CAR."INGMontoAPlazo",
		CAR."IdEmisor",
		CAR."IdAgente",
		CAR."IdSucursal",
		CAR."IdTipoInstrumento",
		CAR."Nemotecnico",
		CAR."FolioComprobante" AS "IdComprobanteReferencia", 
		CAR."PrecioAPlazo"
		
	FROM "BSCL.Custodia.BD::GBL.AnticipoSimultanea_Proceso_Simultaneas" AS PRO
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (PRO."IdCartera" = CAR."IdCartera")
	INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB ON (SUB."FolioCarteraRelacionada" = PRO."FolioCartera" AND SUB."IdEstado" = 'VIG')
)

SELECT *
FROM DatosSimultanea
WHERE "Posicion" = 1;





															   
															   
