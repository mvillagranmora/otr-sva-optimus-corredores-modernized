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
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		WHERE
	    CAR."IdEstado" = 'VIG'
	    AND CAR."FolioCartera" IN (:p_FolioCarteras)
		
)
SELECT 	 
	 CAR."FolioCartera"
	,SUM(CAR."Cantidad"*(EVC."Total"/EVC."CantidadDerecho"))	AS "Disponible"		
	,SUM(CAR."Cantidad"*(EVC."MontoInteres"/EVC."CantidadDerecho"))	AS "InteresesPagados"
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
AND CAST ("FechaPago" as DATE)<= :p_FechaConsulta
AND EVC."IdTipoEventoCapital" IN ('CC','SL')
AND CAR."FolioCartera" IN (:p_FolioCarteras)
GROUP BY 
CAR."FolioCartera";
