WITH UltimaGarantia AS (
    SELECT "IdGarantiaVigente", "FolioGarantia", "IdEstado", "INGCantidad", "INGValorRescate"
    FROM (
        SELECT "IdGarantia" AS "IdGarantiaVigente",
    	   	   "FolioGarantia",
               "IdEstado",
    	       "INGCantidad", 
    	       "INGValorRescate",
               ROW_NUMBER() OVER (PARTITION BY "FolioGarantia" ORDER BY "IdGarantia" DESC) AS rn
        FROM "BSCL.Custodia.BD::GTA.Garantia"
        WHERE "FolioGarantia" IN (:foliosGarantia)
    ) AS sub
    WHERE sub.rn = 1
)
SELECT
	MOV."IdMovimientoCarteraEnTransito",
	COALESCE(RET."IdMovimientoCarteraEnTransito", GAR."IdMovimientoCarteraEnTransito") AS "IdMovimientoCarteraEnTransitoOrigen",
	COALESCE(GAR."IdGarantia", 0) AS "IdGarantiaOrigen",
	COALESCE(RET."IdGarantia", 0) AS "IdGarantia",
	COALESCE(VIG."IdGarantiaVigente", 0) AS "IdGarantiaVigente",
	COALESCE(RET."MontoGarantia", GAR."EGRMontoMonedaInstrumento") AS "MontoGarantia",
	COALESCE(VIG."INGCantidad", 0.0) AS "INGCantidad",
	COALESCE(RET."IdMonedaInstrumento", GAR."IdMonedaInstrumento") AS "IdMonedaInstrumento",
	COALESCE(VIG."IdEstado", '') AS "IdEstado",
	COALESCE(RET."IdOrigenGarantia", GAR."IdOrigenGarantia") AS "IdOrigenGarantia",
	COALESCE(RET."IdPortafolio", GAR."IdPortafolio") AS "IdPortafolio",
	COALESCE(RET."FolioGarantia", GAR."FolioGarantia",VIG."FolioGarantia")AS "FolioGarantia",
	TO_NVARCHAR(COALESCE(MOV."FechaOperacion", RET."FechaOperacion", GAR."FechaOperacion"), 'YYYY-MM-DD') AS "FechaOperacion",
	TO_NVARCHAR(COALESCE(RET."FechaEmisionInstrumento", GAR."FechaEmisionInstrumento"), 'YYYY-MM-DD') AS "FechaEmisionInstrumento", 
	TO_NVARCHAR(COALESCE(RET."FechaVencimientoInstrumento", GAR."FechaEmisionInstrumento"), 'YYYY-MM-DD') AS "FechaVencimientoInstrumento",
	COALESCE(RET."FolioCarteraOrigen", GAR."FolioCarteraOrigen") AS "FolioCarteraOrigen",
	COALESCE(RET."NemoSVS", GAR."NemoSVS" ) AS "NemoSVS",
	COALESCE(RET."IdInstrumento", GAR."IdInstrumento") AS "IdInstrumento",
	COALESCE(RET."IdMonedaEmision", GAR."IdMonedaEmision") AS "IdMonedaEmision",
	COALESCE(VIG."INGValorRescate",GAR."INGValorRescate") AS "ValorRescate",
	COALESCE(RET."IdClasificacionTipoInstrumento", GAR."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
	COALESCE(RET."IdTipoReajuste", GAR."IdTipoReajuste") AS "IdTipoReajuste",
	COALESCE(RET."IdEmisor", GAR."IdEmisor") AS "IdEmisor",
	MOVING."PlazoEnDiasOperacion",
	COALESCE(RET."EsFungible", GAR."EsFungible") AS "EsFungible",
	COALESCE(RET."IdCliente", GAR."IdCliente") AS "IdCliente",
	COALESCE(RET."IdAgente", GAR."IdAgente") AS "IdAgente",
	COALESCE(RET."IdTipoInstrumento", GAR."IdTipoInstrumento") AS "IdTipoInstrumento",
	MOVING."IdBolsa",
	GAR."IdInstrumentoCarteraGarantizada" AS "IdInstrumentoGarantizado",
	MOV."IdConcepto",
	COALESCE(RET."IdContraparteGarantia", GAR."IdContraparteGarantia") AS "IdContraparteGarantia",
	COALESCE(RET."PrecioGarantia", GAR."PrecioGarantia") AS "Precio",
	COALESCE(RET."TasaGarantia", GAR."TasaGarantia") AS "Tasa",
	MOV."IdOperacion",
	COALESCE(RET."CorteMinimo", GAR."CorteMinimo") AS "CorteMinimo",
	MOV."MontoCompraMonedaInstrumento",
	MOV."Cantidad" AS "MovimientoCantidad",
	MOV."ValorRescate" AS "MovimientoValorRescate",
	MOV."IdTipoOperacion",
	COALESCE(VIG."INGCantidad", 0.0) AS "CantidadVigente",
	VIG."FolioGarantia"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" AS GAR ON (GAR."EGRIdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito" AND MOV."IdTipoOperacion" = 'RETGTA')
LEFT JOIN UltimaGarantia AS VIG ON (VIG."FolioGarantia" = GAR."FolioGarantia")
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."EGRIdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito"
LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS RET ON RET."IdGarantia" = CAR."EGRIdOperacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOVING ON GAR."IdMovimientoCarteraEnTransito"  = MOVING."IdMovimientoCarteraEnTransito"
WHERE MOV."IdMovimientoCarteraEnTransito" IN (:idsMovimientoCarteraTransito);