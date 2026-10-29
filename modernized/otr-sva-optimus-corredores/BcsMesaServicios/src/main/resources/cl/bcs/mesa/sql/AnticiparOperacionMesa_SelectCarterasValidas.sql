/*
SELECT
 RES."IdOperacion"
,CASE WHEN CAR."IdClasificacionTipoInstrumento" ='IF' THEN CAR."INGValorRescate" ELSE CAR."INGCantidad" END  AS "CantidadCarteraDestinoDetalle" 
,CASE WHEN RES."IdClasificacionTipoInstrumento" ='IF' THEN RES."IngValorRescate" ELSE RES."IngCantidad" END  AS "CantidadDetalle"
FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES 
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."FolioCartera"= RES."FolioCarteraCustodia"
WHERE RES."IdOperacion" IN (:IdOperacion) AND CAR."IdEstado" IN ('VIG','LIQ')
*/ 

--* CHEQUEO QUE AL ANULAR UNA COMPRA VIGENTE, SUS CARTERAS NO ESTEN TOMADAS EN UNA VENTA VIGENTE O EN TRANSITO.
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
					ON CAR."FolioCartera"= RES."FolioCarteraCustodia" AND CAR."IdEstado" = 'VIG'
		LEFT JOIN 	"BSCL.Custodia.BD::CUS.Reserva" AS PEN
					ON PEN."FolioCarteraOrigen"= RES."FolioCarteraCustodia" AND PEN."IdEstado" = 'VIG' AND PEN."IdOperacion" NOT IN ( :IdOperacion ) 
WHERE 	RES."IdOperacion" IN ( :IdOperacion ) 
		AND RES."IdEstado" = 'LIQ'
		
