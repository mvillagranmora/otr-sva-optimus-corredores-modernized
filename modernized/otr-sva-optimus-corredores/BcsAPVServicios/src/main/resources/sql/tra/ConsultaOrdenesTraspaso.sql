SELECT "IdTraspaso"
	 , "IdOrdenTraspaso"
	 , "RazonRetiro"
	 , "Regimen"
     , "IdOrden"
	 , "IdSolicitudAporteRescate"
	 , "MontoOrden"
	 , "MontoOrdenFFMM" 
	 , "IdPortafolio"
	 , "IdMatrizAgrupadorAPV"
	 , "IdRegimenTributarioMatriz"
FROM (	 
		SELECT DISTINCT OrdenTraspaso."IdTraspaso"				AS "IdTraspaso"
			 , OrdenTraspaso."IdOrdenTraspaso"					AS "IdOrdenTraspaso"
			 , COALESCE(Traspaso."RazonRetiro",'')				AS "RazonRetiro"
			 , COALESCE(Portafolio."CodigoPortafolio",'')		AS "Regimen"
		     , OrdenTraspaso."IdOrden"							AS "IdOrden"
			 , 0												AS "IdSolicitudAporteRescate"
			 , Orden."MontoLiquidacion"							AS "MontoOrden"
			 , 0												AS "MontoOrdenFFMM" 
			 , Orden."IdPortafolio"								AS "IdPortafolio"
			 , Portafolio."IdMatrizAgrupadorAPV"				AS "IdMatrizAgrupadorAPV"
			 , Matriz."IdRegimenTributario"						AS "IdRegimenTributarioMatriz"
		FROM "BSCL.APV.BD::TRA.OrdenTraspaso" AS OrdenTraspaso
		INNER JOIN "BSCL.APV.BD::TRA.Traspaso" AS Traspaso
				ON Traspaso."IdTraspaso" = OrdenTraspaso."IdTraspaso"
		INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarOrden" AS Orden
			ON Orden."IdOrden" = OrdenTraspaso."IdSolicitudAporteRescate"
		INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" AS ASG
			ON OrdenTraspaso."IdSolicitudAporteRescate" = ASG."IdOrden"
		INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MC
			ON ASG."IdAsignacion" = "MC"."IdAsignacion"
			AND MC."IdEstadoFacturacion" = 'FAC'
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				ON Portafolio."IdPortafolio" = Orden."IdPortafolio"	   
		 LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" Matriz
			    ON Portafolio."IdMatrizAgrupadorAPV" = Matriz."IdMatrizAgrupadorAPV"
		WHERE Portafolio."IdEstado" <> 'INA'
		UNION ALL
		SELECT DISTINCT OrdenTraspaso."IdTraspaso"				AS "IdTraspaso"
			 , OrdenTraspaso."IdOrdenTraspaso"					AS "IdOrdenTraspaso"
			 , COALESCE(Traspaso."RazonRetiro",'')				AS "RazonRetiro"
			 , COALESCE(Portafolio."CodigoPortafolio",'')		AS "Regimen"
		     , 0												AS "IdOrden"
			 , OrdenTraspaso."IdSolicitudAporteRescate"			AS "IdSolicitudAporteRescate"
			 , 0												AS "MontoOrden"
			 , OrdenFondosMutuos."Monto"						AS "MontoOrdenFFMM" 
			 , OrdenFondosMutuos."IdPortafolio"					AS "IdPortafolio"
			 , Portafolio."IdMatrizAgrupadorAPV"				AS "IdMatrizAgrupadorAPV"
			 , Matriz."IdRegimenTributario"						AS "IdRegimenTributarioMatriz"
		FROM "BSCL.APV.BD::TRA.OrdenTraspaso" AS OrdenTraspaso
		INNER JOIN "BSCL.APV.BD::TRA.Traspaso" AS Traspaso
				ON Traspaso."IdTraspaso" = OrdenTraspaso."IdTraspaso"
		INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateUI" AS OrdenFondosMutuos
			    ON OrdenFondosMutuos."IdSolicitudAporteRescate" = OrdenTraspaso."IdSolicitudAporteRescate"
			   AND OrdenFondosMutuos."EstadoSolicitud" IN  ('CRT', 'EJE')
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				ON Portafolio."IdPortafolio" = OrdenFondosMutuos."IdPortafolio"
		 LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" Matriz
			    ON Portafolio."IdMatrizAgrupadorAPV" = Matriz."IdMatrizAgrupadorAPV"
		WHERE Portafolio."IdEstado" <> 'INA'
) AS OrdenTraspaso		
WHERE OrdenTraspaso."IdTraspaso" = :IdTraspaso;