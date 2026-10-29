WITH
	CarteraAReIngresar AS (
		SELECT 	CAR."IdCartera",
				CAR."FolioCartera",
				CAR."IdCliente",
				CAR."IdPortafolio",
				CAR."IdInstrumento",
				CAR."EGRCantidad" AS "Cantidad", 
				CAR."EGRValorRescate" AS "ValorRescate",
				'CarteraAReIngresar' AS "Glosa"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS TRAREL ON TRAREL."IdMovimientoCarteraEnTransito"  = CAR."EGRIdMovimientoCarteraEnTransito"
		INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS TRA ON TRAREL."IdMovimientoCarteraEnTransitoRelacionado"  = TRA."IdMovimientoCarteraEnTransito"
		WHERE TRA."IdMovimientoCarteraEnTransito" in (:IdMovimientoCarteraEnTransito)
	),
	CarteraVigente AS (
		SELECT 	CAR."IdCartera",
				CAR."FolioCartera", 
				CAR."IdCliente",
				CAR."IdPortafolio",
				CAR."IdInstrumento",
				CAR."INGCantidad" AS "Cantidad", 
				CAR."INGValorRescate" AS "ValorRescate",
				'CarteraVigente' AS "Glosa"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		INNER JOIN CarteraAReIngresar AS CARI ON CARI."FolioCartera" = CAR."FolioCartera"
		WHERE CAR."IdEstado" = 'VIG'
	),
	UnirCarteras AS (
		SELECT "IdCartera","FolioCartera","IdCliente","IdPortafolio", "IdInstrumento", "Cantidad", "ValorRescate", "Glosa"
		FROM CarteraAReIngresar
		UNION
		SELECT "IdCartera","FolioCartera","IdCliente","IdPortafolio", "IdInstrumento","Cantidad",  "ValorRescate", "Glosa"
		FROM CarteraVigente
	),
	SumaCarteraAIngresar AS (
		SELECT "FolioCartera","IdCliente","IdPortafolio", "IdInstrumento", SUM("Cantidad") AS "Cantidad",  SUM("ValorRescate") AS "ValorRescate"
		FROM UnirCarteras
		GROUP BY "FolioCartera","IdCliente","IdPortafolio", "IdInstrumento"
	),
	CarteraTotal AS	(
		SELECT 	CV."IdCartera", 
				ROW_NUMBER() OVER(PARTITION BY  CT."FolioCartera" ORDER BY CV."IdCartera" DESC) AS rankC,
				CT."FolioCartera", 
				CT."IdCliente", 
				CT."IdPortafolio",
				CT."IdInstrumento",
				CT."Cantidad",
				CT."ValorRescate"
		FROM SumaCarteraAIngresar AS CT
		LEFT JOIN CarteraAReIngresar AS CV ON CV."FolioCartera" = CT."FolioCartera"
	)
SELECT 	"IdCartera",
		"FolioCartera", 
		"IdCliente", 
		"IdPortafolio",
		"IdInstrumento",
		"Cantidad",
		"ValorRescate"
FROM CarteraTotal
WHERE rankC = 1	