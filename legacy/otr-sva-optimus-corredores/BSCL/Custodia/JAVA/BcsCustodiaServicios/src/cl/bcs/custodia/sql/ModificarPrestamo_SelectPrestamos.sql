SELECT
	 PRT."IdCartera"
    ,PRT."IdEstado"
	,PRT."FolioCartera" 		AS "FolioPrestamo"
	,PRT."FechaOperacion" 		AS "FechaPrestamo"
	,PRT."FolioSistemaExterno"
	,PRT."TasaCompra"
	,PRT."IdTipoPrestamo"
	,PRT."IdCliente"
	,PRT."FechaVencimientoOperacion"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS PRT 

WHERE PRT."IdCartera" = :IdCartera
AND   PRT."IdEmpresa" = :IdEmpresa
AND   PRT."IdEstado" = 'VIG';