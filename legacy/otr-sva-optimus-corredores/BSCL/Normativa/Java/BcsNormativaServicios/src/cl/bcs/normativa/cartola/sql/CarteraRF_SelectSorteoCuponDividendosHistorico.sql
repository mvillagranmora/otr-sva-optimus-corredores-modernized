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
		
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		WHERE 
	
		CAR."IdEstado" = 'VIG'
	    AND CAR."FechaRespaldo" = :FechaHasta
	    
		UNION ALL
				
		SELECT 	 
			CAR."FolioCartera"
			,CAR."IdPortafolio"
			,GAR."INGCantidad" AS "Cantidad"
			,GAR."IdEstado"
			,CAR."FechaOperacion" 
			,CAR."Nemotecnico"
		
		FROM "BSCL.Custodia.BD::HIS.Garantia" GAR
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" CAR ON GAR."IdCarteraOrigen" = CAR."IdCartera"
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		WHERE 
		   GAR."IdEstado" = 'VIG'
		   AND GAR."FechaRespaldo" = :FechaHasta
		   AND CAR."FechaRespaldo" = :FechaHasta
)
SELECT 	 
	 CAR."FolioCartera" || '_' || EVC."IdCliente" || '_' || EVC."IdPortafolio" || '_' || EVC."IdInstrumento" || '_' || EVC."IdMoneda" AS "Id"
	,EVC."IdCliente"
	,EVC."IdPortafolio"
	,EVC."IdInstrumento"
	,SUM(CAR."Cantidad"*(EVC."Total"/EVC."CantidadDerecho"))	AS "Disponible"	
	,SUM(CAR."Cantidad"*(EVC."MontoInteres"/EVC."CantidadDerecho"))	AS "InteresesPagados"
	,EVC."IdMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente" EVC
INNER JOIN Cartera CAR
	ON EVC."IdPortafolio" = CAR."IdPortafolio"
	AND EVC."Nemo" = CAR."Nemotecnico"
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON EVC."IdPortafolio" = P."IdPortafolio"	
WHERE 

 EVC."IdClasificacionTipoInstrumento" = 'RF'
AND EVC."IdEmpresa" = :IdEmpresa
AND EVC."IdEstado" = 'CER'
AND CAR."IdEstado" = 'VIG'
AND CAST ("FechaPago" as DATE)>= CAR."FechaOperacion"
AND CAST ("FechaPago" as DATE)<=:FechaHasta
AND EVC."IdTipoEventoCapital" IN ('CC','SL')
GROUP BY 
CAR."FolioCartera", EVC."IdCliente",EVC."IdPortafolio",EVC."IdClasificacionTipoInstrumento",
EVC."IdTipoInstrumento",EVC."IdInstrumento",EVC."Nemo",EVC."IdMoneda";
