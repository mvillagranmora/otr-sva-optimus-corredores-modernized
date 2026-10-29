SELECT
	 CAL."FolioInterno"
	,CAL."IdOperacion"
	,CAL."FolioCartera"
	,CAL."IdTipoOperacion"
	,CAL."IdMonedaPrincipal"
	,CAL."MontoOperacion"
	,CAL."MontoDisponible"
	,CAL."MontoCalce"
	,CUS."IdCartera"
	,CUS."IdOperacion" AS "IdOperacionCartera"
	,CUS."IdTipoInstrumento"
	,CUS."IdInstrumento"
	,CUS."Nemotecnico"
	,CUS."FechaLiquidacion"
	,TIN."IdClasificacionTipoInstrumento"
FROM "BSCL.Mesa.BD::FWD.CrearOperacionForward_ProcesoCalce" CAL
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CUS
	ON (CUS."FolioCartera" = CAL."FolioCartera" AND
		CUS."IdEstado" = 'VIG')
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIN
	ON (CUS."IdTipoInstrumento" = TIN."IdTipoInstrumento" AND
		TIN."IdEstado" = 'VIG')
WHERE CAL."FolioInterno" IN (:FolioInterno)
  AND CUS."IdEmpresa" = :IdEmpresa;