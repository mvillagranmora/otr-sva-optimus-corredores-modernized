WITH
	CarteraPortafolioPaso AS (
		SELECT
			CAR."IdPortafolio"
			,CAR."FolioCartera"
			,CAR."INGCantidad"
			,CAR."IdEstado"
			,CAR."FechaOperacion"
			,CAR."Nemotecnico"
			,CAR."IdBoveda"
			,CAR."INGFechaLiquidacion"
			,CAR."IdCartera"
			,CAR."FechaIngresoRegistro"
			,CAR."IdCondicionLiquidacion"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
		WHERE PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
	),
	Cartera AS
	(
		SELECT 	 
			CAR."IdPortafolio"
			,CAR."FolioCartera"
			,CAR."INGCantidad" AS "Cantidad"
			,CAR."IdEstado"
			,CAR."FechaOperacion"
			,CAR."Nemotecnico"
			,CAR."IdBoveda"
			,CAR."INGFechaLiquidacion"
			,CAR."IdCartera"
			,CAR."FechaIngresoRegistro"
			,CAR."IdCondicionLiquidacion"
		FROM CarteraPortafolioPaso CAR
		WHERE CAR."IdEstado" = 'VIG'
	     
		UNION ALL
				
		SELECT 	 
			CAR."IdPortafolio"
			,CAR."FolioCartera"
			,GAR."INGCantidad" AS "Cantidad"
			,GAR."IdEstado"
			,CAR."FechaOperacion" 
			,CAR."Nemotecnico"
			,CAR."IdBoveda"
			,CAR."INGFechaLiquidacion"
			,GAR."IdGarantia" AS "IdCartera"
			,CAR."FechaIngresoRegistro"
			,CAR."IdCondicionLiquidacion"
		FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
		INNER JOIN CarteraPortafolioPaso CAR ON GAR."IdCarteraOrigen" = CAR."IdCartera"
		WHERE GAR."IdEstado" = 'VIG'
	),
	CarteraObjetivo AS (
		SELECT DISTINCT CAR."FolioCartera"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente" EVC
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR
		ON EVC."IdPortafolio" = CAR."IdPortafolio"
		AND EVC."Nemo" = CAR."Nemotecnico"
		LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteCartera" EVCCAR 
		ON EVC."IdEventoCapitalCliente" = EVCCAR."IdEventoCapitalCliente" 
		AND EVCCAR."IdCartera" = CAR."IdCartera"
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
		WHERE PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
		AND CAR."IdEstado" = 'VIG'
)
SELECT 	 
	 EVC."IdCliente"
	,EVC."IdPortafolio"
	,CAR."IdBoveda"
	,EVC."IdClasificacionTipoInstrumento"
	,EVC."IdTipoInstrumento"
	,EVC."IdInstrumento"
	,EVC."Nemo"	AS "Nemotecnico"
	,SUM(
	CASE 
		WHEN cast("FechaCierre" as DATE)>= CAR."INGFechaLiquidacion" AND (EVCCAR."IdCartera" IS NOT NULL 
			OR CarteraObjetivo."FolioCartera" = CAR."FolioCartera"
			)
			THEN CAR."Cantidad"
		WHEN CAR."INGFechaLiquidacion"  BETWEEN (EVC."FechaCierre") AND (CCM."FechaCertificacion")
                        AND CAST(CCM."FechaCertificacion" as DATE) = CAST(EVC."FechaPago" as DATE)
                        AND CAR."IdCondicionLiquidacion" IN ('T1', 'T2')
                        THEN CAR."Cantidad"
		ELSE 0 END * (EVC."Total"/EVC."CantidadDerecho") - ( CASE WHEN EC."ISFUT"='S' THEN (EC."TasaEfectiva" * ((EC."Factor" * CAR."Cantidad")/100)) ELSE 0 END))	AS "Disponible"			
	,EVC."IdMoneda" AS "IdMonedaInstrumento"
	,to_char(CCM."FechaEfectivaMovimiento",'YYYY-MM-DD') AS "FechaEfectivaMovimiento"--Se incorpora la fecha del movimiento para poder modificar valores de carteras en otras monedas 
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente" EVC
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" EC ON EC."IdEventoCapital"=EVC."IdEventoCapital" AND (EC."IdTipoEventoCapital"='DV' OR EC."IdTipoEventoCapital" = 'RC')
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CCM ON CCM."IdTipoProducto"='EVCA' AND  CCM."IdOperacion"=EVC."IdEventoCapitalCliente" 
INNER JOIN Cartera CAR
	ON EVC."IdPortafolio" = CAR."IdPortafolio"
	AND EVC."Nemo" = CAR."Nemotecnico"
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
LEFT JOIN CarteraObjetivo 
	ON CarteraObjetivo."FolioCartera" = CAR."FolioCartera" 
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteCartera" EVCCAR ON 
EVC."IdEventoCapitalCliente" =EVCCAR."IdEventoCapitalCliente"  AND COALESCE(EVCCAR."IdCartera",-1)=CAR."IdCartera"
WHERE 
PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND EVC."IdClasificacionTipoInstrumento" = 'RV'
AND EVC."IdEmpresa" = :IdEmpresa
AND EVC."IdEstado" = 'CER'
AND CCM."IdEstado" != 'ANU'
AND CAR."IdEstado" = 'VIG'
AND
(
    CAST (EVC."FechaCierre" as DATE) >= CAR."INGFechaLiquidacion" 
    OR
    (
            CCM."IdTipoProducto"='EVCA'
            AND  CCM."IdOperacion"= EVC."IdEventoCapitalCliente"
            AND  CCM."IdTipoOperacion" = 'DIVDIN'
            AND  CAR."INGFechaLiquidacion"  BETWEEN (EVC."FechaCierre") AND (CCM."FechaCertificacion")
            AND  CAST (CCM."FechaCertificacion" as DATE) = CAST (EVC."FechaPago" as DATE)
            AND  CAR."IdCondicionLiquidacion" IN ('T1','T2')
    )
)
AND CAST (EVC."FechaPago" as DATE) <= :FechaHasta
AND (EVC."IdTipoEventoCapital" = 'DV' OR EVC."IdTipoEventoCapital" = 'RC')
GROUP BY 
EVC."IdCliente",EVC."IdPortafolio",CAR."IdBoveda",EVC."IdClasificacionTipoInstrumento",
EVC."IdTipoInstrumento",EVC."IdInstrumento",EVC."Nemo",EVC."IdMoneda"
,CCM."FechaEfectivaMovimiento";