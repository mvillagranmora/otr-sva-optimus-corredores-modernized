WITH
	Cartera AS
	(
		SELECT 	
			CAR."FolioCartera"
			,CAR."IdPortafolio"
			,CAR."INGCantidad" AS "Cantidad"
			,CAR."IdEstado" 
			,CAR."FechaOperacion"
			,CAR."Nemotecnico"
			,CAR."INGIdMonedaPagoCupon" 
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		WHERE
	    CAR."IdEstado" = 'VIG'
	    AND CAR."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE CAR."IdInstrumento" END
		AND CAR."IdPortafolio" = CASE WHEN IFNULL(:p_IdPortafolio,0) > 0 THEN :p_IdPortafolio ELSE CAR."IdPortafolio" END
		AND CAR."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE CAR."IdCliente" END
		UNION ALL
				
		SELECT 	 
			CAR."FolioCartera"
			,CAR."IdPortafolio"
			,GAR."INGCantidad"AS "Cantidad"
			,GAR."IdEstado"
			,CAR."FechaOperacion" 
			,CAR."Nemotecnico"
			,CAR."INGIdMonedaPagoCupon" 
		FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON GAR."IdCarteraOrigen" = CAR."IdCartera"
		WHERE 
		GAR."IdEstado" = 'VIG'
		AND CAR."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE CAR."IdInstrumento" END
		AND CAR."IdPortafolio" = CASE WHEN IFNULL(:p_IdPortafolio,0) > 0 THEN :p_IdPortafolio ELSE CAR."IdPortafolio" END
		AND CAR."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE CAR."IdCliente" END
)
SELECT 	 
	 POR."IdCliente"
	,EVC."IdPortafolio"
	,EVC."IdInstrumento"
	,SUM(CAR."Cantidad"*(EVC."Total"/EVC."CantidadDerecho"))	AS "Disponible"		
	,SUM(CAR."Cantidad"*(EVC."MontoInteres"/EVC."CantidadDerecho"))	AS "InteresesPagados"	
	,CAR."INGIdMonedaPagoCupon"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EVC
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = EVC."IdPortafolio"
INNER JOIN Cartera CAR
	ON EVC."IdPortafolio" = CAR."IdPortafolio"
	AND EVC."Nemo" = CAR."Nemotecnico"
WHERE 
EVC."IdClasificacionTipoInstrumento" = 'RF'
AND EVC."IdEstado" = 'CER'
AND CAR."IdEstado" = 'VIG'
AND CAST ("FechaPago" as DATE)>= CAR."FechaOperacion"
AND CAST ("FechaPago" as DATE)<=:p_FechaConsulta
AND EVC."IdTipoEventoCapital" IN ('CC','SL')
AND EVC."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE EVC."IdInstrumento" END
AND CAR."IdPortafolio" = CASE WHEN IFNULL(:p_IdPortafolio,0) > 0 THEN :p_IdPortafolio ELSE CAR."IdPortafolio" END
AND POR."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE POR."IdCliente" END
GROUP BY 
POR."IdCliente",EVC."IdPortafolio",EVC."IdInstrumento",CAR."INGIdMonedaPagoCupon" ;
