SELECT * FROM (
	SELECT
	        OPE."FolioOperacion",
	        TO_NVARCHAR(OPE."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion",
	        CASE WHEN Sub."IdClasificacionTipoInstrumento" = 'IF' THEN Sub."NemoSVS" ELSE Sub."Nemotecnico" END AS "Nemotecnico",
	        Sub."IdEmisor",
	        Sub."IdClasificacionTipoInstrumento" AS "Mercado",
	        Sub."IdTipoReajuste",
	        RES."Tasa" AS "Tasa",
	        Sub."FechaVencimientoInstrumento",
	        TO_NVARCHAR(Sub."FechaVencimientoInstrumento", 'YYYY-MM-DD') AS "FechaVencimientoInstrumentoCadena",
	        TO_NVARCHAR(OPE."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion",
	        CASE WHEN Sub."IdClasificacionTipoInstrumento" = 'IF' THEN Sub."ValorRescate" ELSE Sub."Cantidad" END AS "Cantidad",
	        Sub."MontoSubyacenteMonedaInstrumento" AS "MontoSubyacente",
	        RES."INGMontoAPlazo" AS "MontoCompromisoDetalle",
	        OPE."IdTipoOperacion",
	        OPE."PlazoEnDias",	
	        TO_NVARCHAR(OPE."FechaVencimiento", 'YYYY-MM-DD') AS "FechaVencimiento",
	        COALESCE(IND."ValorIndicador", 'N') AS "RelacionCorredora",
	        CASE
				WHEN res."FolioCarteraOrigen" = 0 THEN res."FolioCarteraCustodia"
				ELSE res."FolioCarteraOrigen"
			END AS "FolioCartera"
	    FROM "BSCL.Custodia.BD::CUS.Subyacente" Sub
	    LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR
	        ON Sub."FolioCarteraRelacionada" = CAR."FolioCartera" AND CAR."IdTipoInstrumento" = 'PAC'
	    LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE
	        ON CAR."FolioComprobante" = OPE."IdOperacion"
	    LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" RES
	        ON Sub."FolioSubyacente" = RES."FolioSubyacente"
	    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI 
	    	ON OPE."IdCliente" = CLI."IdCliente"
	    LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND
			ON (IND."IdCliente" = CLI."IdCliente" AND "IdTipoIndicador" = 56 AND "GrupoIndicador" = 'NORMATIVOS')
			WHERE OPE."IdEstado" NOT IN ('PEN', 'ANU')
	        AND OPE."IdTipoOperacion" IN ('COMPAC', 'VENPAC')
)
WHERE "IdEmisor" IN (:IdEmisor)
	AND "IdTipoOperacion" IS NOT NULL
	AND "FechaLiquidacion" IS NOT NULL
	AND "FechaVencimiento" IS NOT NULL
	AND "PlazoEnDias" IS NOT NULL
	AND "RelacionCorredora" IS NOT NULL 