SELECT
0		AS "IdPrecioTasa",
CAST(0 AS BIGINT) AS "IdInstrumento",
''		AS "Nemotecnico",
:p_IdClasificacionTipoInstrumento	AS "IdClasificacionTipoInstrumento",
''		AS "IdTipoInstrumento",
DTA."Tasa",
0		AS "TasaMod",
DTA."TasaEstimada",
0		AS "TasaEstimadaMod",
DTA."Plazo",
0		AS "PlazoMod",
'CAR'	AS "IdEstado",
					MON."IdMoneda"
				FROM "BSCL.Plataforma.BD::GBL.CargarArchivoTasas_Entrada_CargarArchivoTasas" DTA
				INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON
					ON (MON."IdEstado" = 'ACT' AND
						(:p_TipoArchivo = 'XML' AND DTA."IdMoneda" = MON."CodigoNumerico") OR
						(:p_TipoArchivo = 'CSV' AND DTA."IdMoneda" = MON."IdMoneda"));