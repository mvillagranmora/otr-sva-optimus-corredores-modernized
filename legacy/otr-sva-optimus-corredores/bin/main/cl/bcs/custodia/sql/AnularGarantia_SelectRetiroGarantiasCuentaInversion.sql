SELECT
	MOV."IdMovimientoCarteraEnTransito",
	COALESCE(RET."IdMovimientoCarteraEnTransito", GAR."IdMovimientoCarteraEnTransito") AS "IdMovimientoCarteraEnTransitoOrigen",
	COALESCE(GAR."IdGarantia", 0) AS "IdGarantiaOrigen",
	COALESCE(RET."IdGarantia", 0) AS "IdGarantia",
	COALESCE(VIG."IdGarantia", GAR."IdGarantia", 0) AS "IdGarantiaVigente",
	COALESCE(RET."MontoGarantia", GAR."EGRMontoMonedaInstrumento") AS "MontoGarantia",
	COALESCE(RET."INGCantidad", 0.0) AS "INGCantidad",
	COALESCE(RET."IdMonedaInstrumento", GAR."IdMonedaInstrumento") AS "IdMonedaInstrumento",
	COALESCE(VIG."IdEstado",'') AS "IdEstado",
	COALESCE(RET."IdOrigenGarantia", GAR."IdOrigenGarantia") AS "IdOrigenGarantia",
	COALESCE(RET."IdPortafolio", GAR."IdPortafolio") AS "IdPortafolio",
	COALESCE(RET."FolioGarantia", GAR."FolioGarantia")AS "FolioGarantia",
	TO_NVARCHAR(COALESCE(MOV."FechaOperacion", RET."FechaOperacion", GAR."FechaOperacion"), 'YYYY-MM-DD') AS "FechaOperacion",
	TO_NVARCHAR(COALESCE(RET."FechaEmisionInstrumento", GAR."FechaEmisionInstrumento"), 'YYYY-MM-DD') AS "FechaEmisionInstrumento", 
	TO_NVARCHAR(COALESCE(RET."FechaVencimientoInstrumento", GAR."FechaEmisionInstrumento"), 'YYYY-MM-DD') AS "FechaVencimientoInstrumento",
	COALESCE(RET."IdMonedaEmision", GAR."IdMonedaEmision") AS "IdMonedaEmision",
	COALESCE(RET."IdTipoReajuste", GAR."IdTipoReajuste") AS "IdTipoReajuste",
	COALESCE(RET."IdEmisor", GAR."IdEmisor") AS "IdEmisor",
	MOVING."PlazoEnDiasOperacion",
	COALESCE(RET."EsFungible", GAR."EsFungible") AS "EsFungible",
	COALESCE(RET."IdCliente", GAR."IdCliente") AS "IdCliente",
	COALESCE(RET."IdAgente", GAR."IdAgente") AS "IdAgente",
	MOVING."IdBolsa",
	MOV."IdConcepto",
	COALESCE(RET."IdContraparteGarantia", GAR."IdContraparteGarantia") AS "IdContraparteGarantia",
	COALESCE(RET."PrecioGarantia", GAR."PrecioGarantia") AS "Precio",
	COALESCE(RET."TasaGarantia", GAR."TasaGarantia") AS "Tasa",
	MOV."IdOperacion",
	COALESCE(RET."CorteMinimo", GAR."CorteMinimo") AS "CorteMinimo",
	MOV."MontoCompraMonedaInstrumento",
	MOV."Cantidad" AS "MovimientoCantidad",
	COALESCE(CAM."IdCargoAbonoMovimiento",0) AS "IdCargoAbonoMovimiento",
	MOV."IdTipoOperacion",
	COALESCE(VIG."INGCantidad", 0.0) AS "CantidadVigente"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM ON CAM."IdMovimientoAsociado" = MOV."IdMovimientoCarteraEnTransito" AND CAM."IdTipoOperacion" = 'RETGTA'
LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS RET ON RET."IdGarantia" = CAM."IdComprobante"
LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS GAR ON (GAR."EGRIdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito")
LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS VIG ON VIG."FolioGarantia" IN (RET."FolioGarantia" , CAM."IdOperacion") AND VIG."IdEstado" IN ('VIG','LIQ')
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOVING ON COALESCE(RET."IdMovimientoCarteraEnTransito", VIG."IdMovimientoCarteraEnTransito")  = MOVING."IdMovimientoCarteraEnTransito"
WHERE MOV."IdMovimientoCarteraEnTransito" IN (:idsMovimientoCarteraTransito);
--LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM ON CAM."IdMovimientoAsociado" = MOV."IdMovimientoCarteraEnTransito" AND CAM."IdTipoOperacion" = 'RETGTA'
--LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS RET ON RET."IdGarantia" = CAM."IdComprobante"
--LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS GAR ON (GAR."EGRIdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito")
--LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS VIG ON RET."FolioGarantia" = VIG."FolioGarantia" AND VIG."IdEstado" IN ('VIG','LIQ')
--LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOVING ON COALESCE(RET."IdMovimientoCarteraEnTransito", VIG."IdMovimientoCarteraEnTransito")  = MOVING."IdMovimientoCarteraEnTransito"
--WHERE MOV."IdMovimientoCarteraEnTransito" IN (:idsMovimientoCarteraTransito);

--SELECT
--	MOV."IdMovimientoCarteraEnTransito",
--	COALESCE(RET."IdMovimientoCarteraEnTransito", GAR."IdMovimientoCarteraEnTransito") AS "IdMovimientoCarteraEnTransitoOrigen",
--	COALESCE(GAR."IdGarantia", 0) AS "IdGarantiaOrigen",
--	COALESCE(RET."IdGarantia", 0) AS "IdGarantia",
--	COALESCE(RET."MontoGarantia", GAR."EGRMontoMonedaInstrumento") AS "MontoGarantia",
--	COALESCE(RET."INGCantidad", 0.0) AS "INGCantidad",
--	COALESCE(RET."IdMonedaInstrumento", GAR."IdMonedaInstrumento") AS "IdMonedaInstrumento",
--	COALESCE(RET."IdEstado", GAR."IdEstado" ,'') AS "IdEstado",
--	COALESCE(RET."IdOrigenGarantia", GAR."IdOrigenGarantia") AS "IdOrigenGarantia",
--	COALESCE(RET."IdPortafolio", GAR."IdPortafolio") AS "IdPortafolio",
--	COALESCE(RET."FolioGarantia", GAR."FolioGarantia")AS "FolioGarantia",
--	TO_NVARCHAR(COALESCE(MOV."FechaOperacion", RET."FechaOperacion", GAR."FechaOperacion"), 'YYYY-MM-DD') AS "FechaOperacion",
--	TO_NVARCHAR(COALESCE(RET."FechaEmisionInstrumento", GAR."FechaEmisionInstrumento"), 'YYYY-MM-DD') AS "FechaEmisionInstrumento", 
--	TO_NVARCHAR(COALESCE(RET."FechaVencimientoInstrumento", GAR."FechaEmisionInstrumento"), 'YYYY-MM-DD') AS "FechaVencimientoInstrumento",
--	COALESCE(RET."FolioCarteraOrigen", GAR."FolioCarteraOrigen") AS "FolioCarteraOrigen",
--	COALESCE(RET."NemoSVS", GAR."NemoSVS" ) AS "NemoSVS",
--	COALESCE(RET."IdInstrumento", GAR."IdInstrumento") AS "IdInstrumento",
--	COALESCE(RET."IdMonedaEmision", GAR."IdMonedaEmision") AS "IdMonedaEmision",
--	COALESCE(RET."INGValorRescate",GAR."INGValorRescate") AS "ValorRescate",
--	COALESCE(RET."IdClasificacionTipoInstrumento", GAR."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
--	COALESCE(RET."IdTipoReajuste", GAR."IdTipoReajuste") AS "IdTipoReajuste",
--	COALESCE(RET."IdEmisor", GAR."IdEmisor") AS "IdEmisor",
--	MOVING."PlazoEnDiasOperacion",
--	COALESCE(RET."EsFungible", GAR."EsFungible") AS "EsFungible",
--	COALESCE(RET."IdCliente", GAR."IdCliente") AS "IdCliente",
--	COALESCE(RET."IdAgente", GAR."IdAgente") AS "IdAgente",
--	COALESCE(RET."IdTipoInstrumento", GAR."IdTipoInstrumento") AS "IdTipoInstrumento",
--	MOVING."IdBolsa",
--	GAR."IdInstrumentoCarteraGarantizada" AS "IdInstrumentoGarantizado",
--	MOV."IdConcepto",
--	COALESCE(RET."IdContraparteGarantia", GAR."IdContraparteGarantia") AS "IdContraparteGarantia",
--	COALESCE(RET."PrecioGarantia", GAR."PrecioGarantia") AS "Precio",
--	COALESCE(RET."TasaGarantia", GAR."TasaGarantia") AS "Tasa",
--	MOV."IdOperacion",
--	COALESCE(RET."CorteMinimo", GAR."CorteMinimo") AS "CorteMinimo",
--	MOV."MontoCompraMonedaInstrumento",
--	MOV."Cantidad" AS "MovimientoCantidad",
--	MOV."ValorRescate" AS "MovimientoValorRescate",
--	COALESCE(CAM."IdCargoAbonoMovimiento",0) AS "IdCargoAbonoMovimiento",
--	MOV."IdTipoOperacion"
--FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
--INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" AS GAR ON (GAR."EGRIdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito" AND MOV."IdTipoOperacion" = 'RETGTA')
--LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS RET ON GAR."FolioGarantia" = RET."FolioGarantia" AND (RET."IdEstado" IN ('VIG','LIQ'))
--LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOVING ON COALESCE(RET."IdMovimientoCarteraEnTransito", GAR."IdMovimientoCarteraEnTransito")  = MOVING."IdMovimientoCarteraEnTransito"
--LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM ON RET."IdGarantia" = CAM."IdComprobante" AND CAm."FolioComprobante" = RET."FolioGarantia" AND CAM."IdTipoOperacion" = 'RETGTA'
--WHERE MOV."IdMovimientoCarteraEnTransito" IN (:idsMovimientoCarteraTransito);