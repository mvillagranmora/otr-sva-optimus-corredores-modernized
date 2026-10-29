SELECT
	MOV."IdTipoReajuste",
	MOV."IdCliente",
	CON."TipoConcepto",
	CAST(0 AS DECIMAL) AS "MontoMercadoMonedaAdicional",
	MOV."IdPortafolio",
	TIPO_OP."Nombre" AS "NombreTipoOperacion",
	'INGRESO' AS "Accion",
	CAST(0 AS DECIMAL) AS "PrecioMercado",
	TO_CHAR("Cantidad") AS "CantidadString",
	CAST(0 AS DECIMAL) AS "TasaMercado",
	MOV."PrecioCompra",
	MOV."FolioComprobante",
	MOV."IdClasificacionTipoInstrumento",
	MOV."Nemotecnico",
	MOV."IdConcepto",
	MOV."MontoCompraMonedaInstrumento",
	MOV."IdMonedaInstrumento",
	MOV."IdEstado",
	to_nvarchar(MOV."FechaOperacion", 'DD-MM-YYYY') AS "FechaOperacion",
	CON."Nombre" AS "NombreConcepto",
	MOV."IdTipoComprobante",
	TIPO_OP."IdTipoOperacion",
	MOV."Cantidad",
	MOV."TasaCompra",
	CAST(0 AS DECIMAL) AS "MontoMercadoMonedaLocal",
	CAST(0 AS DECIMAL) AS "MontoMercadoMonedaInstrumento",
	MOV."IdEmisor" AS "Emisor",
	MOV."IdTipoInstrumento",
	CAST(0 AS BIGINT) AS "IdFolioCartera"
FROM
	"BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MOV
	INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON MOV."IdPortafolio" = P."IdPortafolio"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIPO_OP ON MOV."IdTipoOperacion" = TIPO_OP."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as CON ON CON."IdConcepto" = MOV."IdConcepto"
WHERE MOV."IdEmpresa" = :IdEmpresa
	AND MOV."EsNominal" = 'S'
	AND MOV."MueveCustodia" = 'N'
	AND MOV."IdClasificacionTipoInstrumento" = 'RV'
	AND MOV."IdEstado" = 'CRT'
	AND MOV."FechaOperacion" BETWEEN :FechaDesde
	AND :FechaHasta
	AND MOV."IdTipoInstrumento" NOT IN ('SIM', 'PRT', 'EMP', 'PAC', 'RRV', 'RRF')
	AND TIPO_OP."AccionOperacion" NOT IN ('VENTA', 'RETIRO')