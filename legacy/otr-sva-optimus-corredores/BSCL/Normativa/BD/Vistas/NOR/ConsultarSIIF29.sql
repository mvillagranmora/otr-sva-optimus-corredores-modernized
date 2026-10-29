	SET SCHEMA "BSCL";
	
	DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarSIIF29"; 
	 
	CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarSIIF29" AS 
			
		SELECT "DocumentoIdentidad"
			 , CtaInv."NombreCliente" AS "Nombre"
			 , "Fondo"
			 , "Serie"
			 , CASE WHEN "MontoOrden" = 0 THEN "MontoOrdenFFMM" ELSE "MontoOrden" END AS "MontoRetiro"
			 , "Monto" AS "MontoRetenido"
			 , CtaInv."FechaCertificacion"
			 , TO_CHAR( CtaInv."FechaCertificacion", 'YYYY-MM') AS "Periodo"
		FROM (	 
				SELECT DISTINCT OrdenTraspaso."IdTraspaso"				AS "IdTraspaso"
					 , OrdenTraspaso."IdOrdenTraspaso"					AS "IdOrdenTraspaso"
					 , Orden."DocumentoIdentidad"
					 , COALESCE(Traspaso."RazonRetiro",'')				AS "RazonRetiro"
					 , COALESCE(Portafolio."CodigoPortafolio",'')		AS "Regimen"
				     , OrdenTraspaso."IdOrden"							AS "IdOrden"
					 , 0												AS "IdSolicitudAporteRescate"
					 , Orden."MontoLiquidacion"							AS "MontoOrden"
					 , 0												AS "MontoOrdenFFMM" 
					 , Orden."IdPortafolio"								AS "IdPortafolio"
					 , Portafolio."IdMatrizAgrupadorAPV"				AS "IdMatrizAgrupadorAPV"
					 , "Instrumento" AS "Fondo"
					 , '' AS "Serie"
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
				UNION ALL
				SELECT DISTINCT OrdenTraspaso."IdTraspaso"				AS "IdTraspaso"
					 , OrdenTraspaso."IdOrdenTraspaso"					AS "IdOrdenTraspaso"
					 , "DocumentoIdentidad"
					 , COALESCE(Traspaso."RazonRetiro",'')				AS "RazonRetiro"
					 , COALESCE(Portafolio."CodigoPortafolio",'')		AS "Regimen"
				     , 0												AS "IdOrden"
					 , OrdenTraspaso."IdSolicitudAporteRescate"			AS "IdSolicitudAporteRescate"
					 , 0												AS "MontoOrden"
					 , OrdenFondosMutuos."Monto"						AS "MontoOrdenFFMM" 
					 , OrdenFondosMutuos."IdPortafolio"					AS "IdPortafolio"
					 , Portafolio."IdMatrizAgrupadorAPV"				AS "IdMatrizAgrupadorAPV"
					 , "NombreFondo" AS "Fondo"
					 , "NombreSerieFondo" AS "Serie"
				FROM "BSCL.APV.BD::TRA.OrdenTraspaso" AS OrdenTraspaso
				INNER JOIN "BSCL.APV.BD::TRA.Traspaso" AS Traspaso
						ON Traspaso."IdTraspaso" = OrdenTraspaso."IdTraspaso"
				INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateUI" AS OrdenFondosMutuos
					    ON OrdenFondosMutuos."IdSolicitudAporteRescate" = OrdenTraspaso."IdSolicitudAporteRescate"
					   AND OrdenFondosMutuos."EstadoSolicitud" = 'CRT'
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
						ON Portafolio."IdPortafolio" = OrdenFondosMutuos."IdPortafolio"	 
		) AS OrdenTraspaso		
		INNER JOIN  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CtaInv
		ON OrdenTraspaso."IdOrdenTraspaso" = CtaInv."IdOperacion" 
		AND "IdConcepto" = 'EGRRET'
		AND "IdTipoMovimiento" = 'MOVCAR';
		
			
	GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarSIIF29" TO  BCS_DESARROLLADOR WITH GRANT OPTION;