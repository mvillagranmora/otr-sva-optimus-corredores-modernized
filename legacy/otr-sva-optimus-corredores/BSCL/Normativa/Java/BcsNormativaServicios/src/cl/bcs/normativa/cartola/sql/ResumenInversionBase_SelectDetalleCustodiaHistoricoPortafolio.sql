WITH Cartera AS	(
	SELECT	CAR."IdCliente"
		,CAR."IdPortafolio"
		,CAR."IdClasificacionTipoInstrumento"
		,CAR."IdTipoInstrumento"
		,CAR."IdInstrumento"
		,CAR."IdMonedaInstrumento" AS "IdMonedaOperacion"
		,CAR."INGCantidad" AS "Cantidad"
		,CAR."Nemotecnico"
		,TINS."Nombre" AS "Item"
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = CAR."IdTipoInstrumento"
		WHERE CAR."FechaRespaldo" = :FechaHasta
		AND	CAR."IdClasificacionTipoInstrumento" IN ('RV')
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		AND CAR."IdEstado" = 'VIG'
		AND CAR."IdEmpresa" = :IdEmpresa
		AND CAR."IdTipoOperacion" != 'VENPAC'
		AND CAR."INGFechaOperacion" <= (CASE WHEN CAR."IdTipoInstrumento" = 'FWD' THEN CAR."INGFechaLiquidacion" ELSE :FechaHasta END)
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		AND P."IdTrabajo" = :IdTrabajo
)

SELECT "IdCliente"
		,"IdPortafolio"
		,"IdClasificacionTipoInstrumento"
		,"IdTipoInstrumento"
		,"IdInstrumento"
		,"IdMonedaOperacion"
		,SUM("Cantidad") AS "TotalCantidad"
		,"Nemotecnico"
		,"Item"
		FROM Cartera
		GROUP BY "IdCliente", "IdPortafolio", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdMonedaOperacion", "Nemotecnico", "Item"