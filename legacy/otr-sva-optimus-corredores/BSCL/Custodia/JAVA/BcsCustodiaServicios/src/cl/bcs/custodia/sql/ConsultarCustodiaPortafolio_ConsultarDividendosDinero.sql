WITH
	Cartera AS
	(
		SELECT 	 
			CAR."IdPortafolio"
			,CAR."Cantidad"
			,CAR."IdEstado"
			,CAR."FechaOperacion"
			,CAR."Nemotecnico"
			,CAR."INGFechaLiquidacion"
			,CAR."IdCartera"
			,CAR."FechaIngresoRegistro"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		WHERE CAR."IdEstado" = 'VIG'
		AND CAR."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE CAR."IdInstrumento" END
		AND CAR."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE CAR."Nemotecnico" END
		AND CAR."IdPortafolio" = CASE WHEN IFNULL(:p_IdPortafolio,0) > 0 THEN :p_IdPortafolio ELSE CAR."IdPortafolio" END
		AND CAR."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE CAR."IdCliente" END
	     
		UNION ALL
				
		SELECT 	 
			CAR."IdPortafolio"
			,GAR."INGCantidad"AS "Cantidad"
			,GAR."IdEstado"
			,CAR."FechaOperacion" 
			,CAR."Nemotecnico"
			,CAR."INGFechaLiquidacion"
			,CAR."IdCartera"
			,CAR."FechaIngresoRegistro"		
		FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON GAR."IdCarteraOrigen" = CAR."IdCartera"
		WHERE GAR."IdEstado" = 'VIG'
		AND CAR."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE CAR."IdInstrumento" END
		AND CAR."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE CAR."Nemotecnico" END
		AND CAR."IdPortafolio" = CASE WHEN IFNULL(:p_IdPortafolio,0) > 0 THEN :p_IdPortafolio ELSE CAR."IdPortafolio" END
		AND CAR."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE CAR."IdCliente" END
)
SELECT 	 
	 EVC."IdCliente"
	,EVC."IdPortafolio"
	,EVC."IdInstrumento"
	,SUM(
	CASE 
		WHEN cast("FechaCierre" as DATE)>= CAR."INGFechaLiquidacion" AND (EVCCAR."IdCartera" IS NOT NULL )
			THEN CAR."Cantidad"
		ELSE 0 END * (EVC."Total"/EVC."CantidadDerecho") - ( CASE WHEN EVC."ISFUT"='S' THEN (EC."TasaEfectiva" * ((EC."Factor" * CAR."Cantidad")/100)) ELSE 0 END))	AS "Disponible"
	,EVC."IdMoneda"
	,TO_CHAR(MIN(CCM."FechaEfectivaMovimiento"),'YYYY-MM-DD') AS "FechaEfectivaMovimiento"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente" EVC
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" EC ON EC."IdEventoCapital"=EVC."IdEventoCapital" AND (EC."IdTipoEventoCapital"='DV'  OR EC."IdTipoEventoCapital" = 'RC')
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CCM ON CCM."IdTipoProducto"='EVCA' AND  CCM."IdOperacion"=EVC."IdEventoCapitalCliente" 
INNER JOIN Cartera CAR
	ON EVC."IdPortafolio" = CAR."IdPortafolio"
	AND EVC."Nemo" = CAR."Nemotecnico"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteCartera" EVCCAR ON 
EVC."IdEventoCapitalCliente" =EVCCAR."IdEventoCapitalCliente"  AND COALESCE(EVCCAR."IdCartera",-1) = CAR."IdCartera"
WHERE 
EVC."IdClasificacionTipoInstrumento" = 'RV'
AND (EVC."IdTipoEventoCapital" = 'DV' OR EVC."IdTipoEventoCapital" = 'RC')
AND EVC."IdEstado" = 'CER'
AND EVC."CantidadDerecho" > 0
AND CAR."IdEstado" = 'VIG'
AND CAST (EVC."FechaCierre" as DATE) >= CAR."INGFechaLiquidacion"
AND CAST (EVC."FechaPago" as DATE) <=:p_FechaConsulta
AND EVC."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE EVC."IdInstrumento" END
AND EVC."Nemo" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE EVC."Nemo" END
AND EVC."IdPortafolio" = CASE WHEN IFNULL(:p_IdPortafolio,0) > 0 THEN :p_IdPortafolio ELSE EVC."IdPortafolio" END
AND EVC."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE EVC."IdCliente" END
GROUP BY EVC."IdCliente",EVC."IdPortafolio",EVC."IdInstrumento",EVC."IdMoneda";