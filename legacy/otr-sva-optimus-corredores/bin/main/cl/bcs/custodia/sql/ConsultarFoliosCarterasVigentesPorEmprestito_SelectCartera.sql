WITH
	CarteraAReIngresar AS (
		SELECT 	TRA."IdClienteRelacionado" AS "IdCliente", 
				TRA."IdPortafolioRelacionado" AS "IdPortafolio", 
				SUB."IdInstrumento", 
				SUB."INGCantidad",
				CAR."FolioCartera" AS "FolioEmprestito"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
		INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS TRA ON CAR."IdMovimientoCarteraEnTransito" = TRA."IdMovimientoCarteraEnTransito"
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
		WHERE CAR."FolioCartera" in (:FolioEmprestito)
	)
SELECT 	CAR."IdCartera",
		CAR."FolioCartera", 
		CAR."IdCliente",
		CAR."IdPortafolio",
		CAR."IdInstrumento",
		CAR."IdEmisor",
		CARI."INGCantidad" AS "CantidadEmprestito",
		CAR."INGCantidad" AS "Cantidad", 
		CAR."INGValorRescate" AS "ValorRescate",
		CAR."FechaLiquidacion",
		CARI."FolioEmprestito"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN CarteraAReIngresar AS CARI ON CARI."IdPortafolio" = CAR."IdPortafolio" AND CARI."IdInstrumento" = CAR."IdInstrumento"
WHERE CAR."IdEstado" = 'VIG'
ORDER BY CAR."IdCartera"