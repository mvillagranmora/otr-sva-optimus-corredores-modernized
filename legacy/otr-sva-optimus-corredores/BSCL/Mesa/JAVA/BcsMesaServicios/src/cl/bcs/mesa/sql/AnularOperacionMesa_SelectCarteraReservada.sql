SELECT 	RES."IdOperacion", RES."FolioCarteraCustodia" AS "FolioCarteraCP", RES."Nemotecnico", RES."IdEmisor"
		,CASE WHEN RES."IdClasificacionTipoInstrumento" ='IF' THEN RES."IngValorRescate" ELSE RES."IngCantidad" END  AS "CantidadCP"
		,COALESCE( CASE WHEN CAR."IdClasificacionTipoInstrumento" ='IF' THEN CAR."INGValorRescate" ELSE CAR."INGCantidad" END, 0) AS "CantidadCartera" 
		,COALESCE( CASE WHEN PEN."IdClasificacionTipoInstrumento" ='IF' THEN PEN."IngValorRescate" ELSE PEN."IngCantidad" END, 0) AS "CantidadReservada"
FROM 	"BSCL.Custodia.BD::CUS.Reserva" RES
		INNER JOIN	"BSCL.Mesa.BD::MES.Operacion" AS OPE
					ON OPE."IdOperacion" = RES."IdOperacion" 
					AND ( OPE."IdTipoOperacion" = 'COMPAC' OR OPE."IdTipoOperacion" = 'COMTER' ) 
					AND OPE."IdEstado" = 'VIG'
		LEFT JOIN 	"BSCL.Custodia.BD::CUS.Cartera" AS CAR 
					ON CAR."IdOperacion"= RES."IdReserva" AND CAR."IdEstado" = 'VIG'
		LEFT JOIN 	"BSCL.Custodia.BD::CUS.Reserva" AS PEN
					ON PEN."FolioCarteraOrigen"= CAR."FolioCartera" AND PEN."IdEstado" = 'VIG'  AND PEN."IdOperacion" NOT IN ( :IdOperacion )
WHERE 	RES."IdOperacion" IN ( :IdOperacion ) 
		AND RES."IdEstado" = 'LIQ';