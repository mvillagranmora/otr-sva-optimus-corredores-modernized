SELECT 
	CAST(0 AS BIGINT) AS "IdDocumento",
    CONF."PKRUTCTRAPARTE1" AS "KRutCtraparte1",
    FWD."KIdContrato",
    TO_VARCHAR(FWD."KFechaContrato",'YYYY-MM-DD') AS "KFechaContrato",
    FWD."HoraContrato",
    CAST(0 AS BIGINT) AS "KIdOpeEstruc",
    ROW_NUMBER() OVER (PARTITION BY FWD."kPeriodoComercial", PAGO."IdOperacion" ORDER BY FWD."kPeriodoComercial") AS "KS5_CorrPago",
	CASE WHEN PAGO."RecibePaga" = 'P' THEN 'E' ELSE 'R' END AS "KS5_IdPago",
	TO_VARCHAR(PAGO."FechaLiquidacionComprobantePagoOperacion", 'YYYY-MM-DD') AS "S5_FecPago",
	PAGO."Monto" AS "S5_MontoPago",
	CONF."PTPAGOEFEC" AS "S5_TipoPago",
    CASE WHEN PAGO."IdMoneda"='UF' THEN 'CLF' ELSE PAGO."IdMoneda" END AS "S5_CodMoneda",
    'FWD' AS "kProducto",
    'FX' AS "kTipoArchivo",
    TO_VARCHAR(FWD."kPeriodoComercial",'YYYY-MM-DD') AS "kPeriodoComercial"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.ConsultarMovimientosFWDDJ1" FWD
INNER JOIN "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" PAGO
ON PAGO."IdOperacion" = FWD."IdCartera" AND PAGO."IdTipoProducto" = 'MEFW'  
CROSS JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1.Configuracion" CONF
WHERE FWD."EGRIdTipoOperacion"='VCDFWD' AND (
	(FWD."IdTipoOperacion"='COMFWD' AND SUBSTRING(TO_VARCHAR(FWD."FechaRecibimos",'YYYY/MM/DD'),1,7)=:periodo AND FWD."FechaRecibimos">=:FechaDesde AND FWD."FechaRecibimos"<=:FechaHasta)
	OR (FWD."IdTipoOperacion"='VENFWD' AND SUBSTRING(TO_VARCHAR(FWD."FechaEntregamos",'YYYY/MM/DD'),1,7)=:periodo AND FWD."FechaEntregamos">=:FechaDesde AND FWD."FechaEntregamos"<=:FechaHasta)
);