SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarOrdenesTraspaso";
 
CREATE VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarOrdenesTraspaso"  AS 
SELECT "IdTraspaso"
	 , "RazonRetiro"
	 , "Regimen"
     , "IdOrden"
     , "Instrumento"
     , "CantidadCuotas"
     , "ValorCuota"
	 , "IdSolicitudAporteRescate"
	 , "MontoOrden"
	 , "MontoOrdenFFMM"
	 , "Estado"
	 , "IdPortafolio"
	 , "IdMatrizAgrupadorAPV"
FROM (	 
		SELECT DISTINCT OrdenTraspaso."IdTraspaso"				AS "IdTraspaso"
			 , COALESCE(Traspaso."RazonRetiro",'')				AS "RazonRetiro"
			 , COALESCE(Portafolio."CodigoPortafolio",'')		AS "Regimen"
		     , OrdenTraspaso."IdOrden"							AS "IdOrden"
		     , Orden."Instrumento"								AS "Instrumento"
		     , Orden."CantidadOrden"							AS "CantidadCuotas"
		     , Orden."PrecioOrden"								AS "ValorCuota"
			 , OrdenTraspaso."IdSolicitudAporteRescate"			AS "IdSolicitudAporteRescate"
			 , Orden."MontoLiquidacion"							AS "MontoOrden"
			 , 0												AS "MontoOrdenFFMM" 
			 , Orden."IdPortafolio"								AS "IdPortafolio"
			 , Portafolio."IdMatrizAgrupadorAPV"				AS "IdMatrizAgrupadorAPV"
			 , Orden."IdEstadoOrden"							AS "Estado"
		FROM "BSCL.APV.BD::TRA.OrdenTraspaso" AS OrdenTraspaso
		INNER JOIN "BSCL.APV.BD::TRA.Traspaso" AS Traspaso
				ON Traspaso."IdTraspaso" = OrdenTraspaso."IdTraspaso"
		INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarOrden" AS Orden
				ON Orden."IdOrden" = OrdenTraspaso."IdSolicitudAporteRescate"
			  --AND Orden."IdEstadoOrden" = 'EJE'
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				ON Portafolio."IdPortafolio" = Orden."IdPortafolio"	
		WHERE Portafolio."IdEstado" <> 'INA'   
		UNION ALL
		SELECT DISTINCT OrdenTraspaso."IdTraspaso"				AS "IdTraspaso"
			 , COALESCE(Traspaso."RazonRetiro",'')				AS "RazonRetiro"
			 , COALESCE(Portafolio."CodigoPortafolio",'')		AS "Regimen"
		     , 0												AS "IdOrden"
		     , OrdenFondosMutuos."IdSerieFondo"					AS "Instrumento"
		     , OrdenFondosMutuos."NumeroCuotas"					AS "CantidadCuotas"
		     , OrdenFondosMutuos."ValorCuota"					AS "ValorCuota"
			 , OrdenTraspaso."IdSolicitudAporteRescate"			AS "IdSolicitudAporteRescate"
			 , 0												AS "MontoOrden"
			 , OrdenFondosMutuos."Monto"						AS "MontoOrdenFFMM" 
			 , OrdenFondosMutuos."IdPortafolio"					AS "IdPortafolio"
			 , Portafolio."IdMatrizAgrupadorAPV"				AS "IdMatrizAgrupadorAPV"
			 , OrdenFondosMutuos."EstadoSolicitud"				AS "Estado"
		FROM "BSCL.APV.BD::TRA.OrdenTraspaso" AS OrdenTraspaso
		INNER JOIN "BSCL.APV.BD::TRA.Traspaso" AS Traspaso
				ON Traspaso."IdTraspaso" = OrdenTraspaso."IdTraspaso"
		INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateUI" AS OrdenFondosMutuos
			    ON OrdenFondosMutuos."IdSolicitudAporteRescate" = OrdenTraspaso."IdSolicitudAporteRescate"
			   --AND OrdenFondosMutuos."EstadoSolicitud" = 'CRT'
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				ON Portafolio."IdPortafolio" = OrdenFondosMutuos."IdPortafolio"	
		WHERE Portafolio."IdEstado" <> 'INA' 
) AS OrdenTraspaso;

GRANT SELECT ON "BSCL.APV.BD.Vistas::TRA.ConsultarOrdenesTraspaso" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

