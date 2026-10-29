SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1871.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1871.Origen" (
    "IdEmpresa",
    "Numero",
    "RutCliente",
    "IdCliente",
    "FechaOperacion",
    "AnoAhorro",
    --Ahorros
    "AhorrosTrabajador",
    "AhorrosDeposito",
    "AhorrosCotizaciones",
    --Retiros
    "RetirosTrabajador",
    "RetirosDeposito",
    "RetirosCotizaciones",
    "EjecutadoComplementoIPC",
    "EjecutadoAjustarRescates"
) AS
	SELECT 
	  TO_BIGINT(sol."IdEmpresa") AS "IdEmpresa",
 		TO_BIGINT(sol."IdSolicitudAporteRescate") AS "Numero",
 		LPAD(cli."DocumentoIdentidad",11,' ') AS "RutCliente",
	   TO_BIGINT(sol."IdCliente") AS "IdCliente",
	   CASE WHEN YEAR(sol."FechaValorCuota") > YEAR("emp"."PeriodoPago" || '-01') 
	   THEN YEAR("emp"."PeriodoPago" || '-01') || '-12-30'
	   ELSE sol."FechaValorCuota" END AS "FechaOperacion",
	   substring("emp"."PeriodoPago",0,4) AS "AnoAhorro",
	   --Ahorros
	   0  AS "AhorrosTrabajador",
	   COALESCE(round(sol."MontoMonedaSistema" * (IPCACT."Valor"/IPCINV."Valor"),0),0) AS "AhorroAPV",
	   0 AS "AhorroCV",
	   --Retiros
	   0 AS "RetiroAPVC",
	   (SELECT COALESCE(round(SUM("EGRMonto"),0),0) 
			FROM 
				(	--	Para AT2022 solo este select servira*/
					SELECT (("EGRCantidad" * "MontoCompra") / "Cantidad") * (IPCACT."Valor"/IPCINV."Valor") AS "EGRMonto" , car."IdOperacion"	
					FROM "BSCL.Custodia.BD::CUS.Cartera" car 
					INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" solres ON solres."IdSolicitudAporteRescate" = "EGRIdOperacion"
					LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINV  ON (substring(ADD_MONTHS (TO_DATE (solres."FechaValorCuota", 'YYYY-MM-DD'), -1),0,7) = IPCINV."AgnoMes")  
		 																	AND IPCINV."IdParametroFinanciero" = 'IPC'
		 			LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACT  ON (SUBSTRING(COALESCE(solres."FechaValorCuota",'1900-01-01'),0,4) || '-11' = IPCACT."AgnoMes")  		
		 			 														AND IPCACT."IdParametroFinanciero" = 'IPC'
																			AND 	solres."IdMedioDePago" <> 'CIN'
					WHERE solres."FechaValorCuota" <> '0001-01-01'
					AND solres."Estado" = 'CRT'
					AND solres."IdTipoMovimientoFondoMutuo" IN ('RESNORM')
				)	car 
			WHERE 	car. "IdOperacion" = sol."IdSolicitudAporteRescate" --and car."EGRFechaOperacion" = sol."FechaValorCuota"
	    
		) AS "RetiroAPV",
		0 AS "RetirosCotizaciones",
			CAST('N' AS NVARCHAR(1)) AS "EjecutadoComplementoIPC",
       CAST('N' AS NVARCHAR(1)) AS "EjecutadoAjustarRescates"
	 FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol 
		 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = sol."IdCliente"
		 INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" saapv  ON saapv."FolioContratoSAAPV" = sol."FolioContratoOperacion"	 																		
		 INNER JOIN "BSCL.APV.BD::REC.Cliente" AS "cli"
     		ON "cli"."IdRecaudacionCliente" = sol."IdOrigenRecaCliente"
		 INNER JOIN "BSCL.APV.BD::REC.Recaudacion" AS "dep"
     		ON "dep"."IdRecaudacion"  = "cli"."IdRecaudacion"
		 INNER JOIN "BSCL.APV.BD::REC.Empleador" AS "emp"
     		ON "emp"."IdRecaudacion" = "dep"."IdRecaudacion"
     		AND "emp"."IdRecaudacion" = "cli"."IdRecaudacion" 
     		AND "emp"."IdRecaudacionEmpleador" = "cli"."IdRecaudacionEmpleador" 		 
		 LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINV   ON (substring(ADD_MONTHS (TO_DATE ("emp"."PeriodoPago", 'YYYY-MM'), -1),0,7) = IPCINV."AgnoMes")  
		 																		AND IPCINV."IdParametroFinanciero" = 'IPC'
		 LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACT  
		 		ON ( (CASE WHEN YEAR(sol."FechaValorCuota") > YEAR("emp"."PeriodoPago" || '-01') 
 						  THEN SUBSTRING(COALESCE("emp"."PeriodoPago",'1900-01-01'),0,4) || '-11'
 						  ELSE
 						  SUBSTRING(COALESCE(sol."FechaValorCuota",'1900-01-01'),0,4) || '-11'
 						  END) = IPCACT."AgnoMes") 
					AND IPCACT."IdParametroFinanciero" = 'IPC'
					
	 WHERE 	sol."TipoSolicitud" = 'APO'
	 AND 	sol."IdTipoMovimientoFondoMutuo" = 'APONORM'
	 AND 	sol."IdRegimenTributario" = 'APV-A'
	 --AND 	"emp"."PeriodoPago" BETWEEN  substring(sol."FechaValorCuota",0,4)|| '-01' AND substring(sol."FechaValorCuota",0,4)|| '-12'
	 AND 	sol."Estado" = 'CRT'
	 AND 	saapv."IdMedioRecaudacionFormaPago" = 2	--	Descuento Via Empleador
	 AND 	sol."FolioContratoOperacion" <> ''

	 UNION ALL 
	 
	 SELECT 
 		   TO_BIGINT(sol."IdEmpresa") AS "IdEmpresa",
 		TO_BIGINT(sol."IdSolicitudAporteRescate") AS "Numero",
 		LPAD(cli."DocumentoIdentidad",11,' ') AS "RutCliente",
	   TO_BIGINT(sol."IdCliente") AS "IdCliente",
	   substring(sol."FechaIngresoRegistro",1,10) AS "FechaOperacion",
		   (substring("FechaIngresoRegistro",0,4)) AS "AnoAhorro",
		   --Ahorros
		   0  AS "AhorrosTrabajador",
		   round(sol."MontoMonedaSistema" * (IPCACT."Valor"/IPCINV."Valor"),0) AS "AhorroAPV",
		   0 AS "AhorroCV",
		   --Retiros
		   0 AS "RetiroAPVC",
		   (SELECT COALESCE(round(SUM("EGRMonto"),0),0) 
				FROM 
					(	--	Para AT2022 solo este select servira
						SELECT (("EGRCantidad" * "MontoCompra") / "Cantidad") * (IPCACT."Valor"/IPCINV."Valor") AS "EGRMonto" , car."IdOperacion"	
						FROM "BSCL.Custodia.BD::CUS.Cartera" car 
						INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" solres ON solres."IdSolicitudAporteRescate" = "EGRIdOperacion"
					  LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINV  ON (substring(ADD_MONTHS (TO_DATE (solres."FechaValorCuota", 'YYYY-MM-DD'), -1),0,7) = IPCINV."AgnoMes")  
		 																		AND IPCINV."IdParametroFinanciero" = 'IPC'
		 			  LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACT  ON (SUBSTRING(COALESCE(solres."FechaValorCuota",'1900-01-01'),0,4) || '-11' = IPCACT."AgnoMes")  		
		 																		AND IPCACT."IdParametroFinanciero" = 'IPC'
																				AND 	solres."IdMedioDePago" <> 'CIN'
						WHERE solres."FechaValorCuota" <> '0001-01-01'
						AND solres."Estado" = 'CRT'
						AND solres."IdTipoMovimientoFondoMutuo" IN ('RESNORM')
					)	car 
				WHERE 	car. "IdOperacion" = sol."IdSolicitudAporteRescate"
				
				
			) AS "RetirosDeposito",
			0 AS "RetirosCotizaciones",
			CAST('N' AS NVARCHAR(1)) AS "EjecutadoComplementoIPC",
       CAST('N' AS NVARCHAR(1)) AS "EjecutadoAjustarRescates"
	 FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol 
		 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = sol."IdCliente"		
		 LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINV  ON (substring(ADD_MONTHS (TO_DATE (substring(sol."FechaIngresoRegistro",1,10), 'YYYY-MM-DD'), -1),0,7) = IPCINV."AgnoMes")  
		 																		AND IPCINV."IdParametroFinanciero" = 'IPC'
		 LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACT  ON (SUBSTRING(sol."FechaIngresoRegistro",0,4) || '-11' = IPCACT."AgnoMes")  		
		 																		AND IPCACT."IdParametroFinanciero" = 'IPC'
		 INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" saapv  ON saapv."FolioContratoSAAPV" = sol."FolioContratoOperacion"	 																		
	 WHERE 	sol."TipoSolicitud" = 'APO'
	 AND 	sol."IdTipoMovimientoFondoMutuo" = 'APONORM'
	 AND 	sol."IdRegimenTributario" = 'APV-A'
	 --AND 	substring(sol."FechaIngresoRegistro",1,10) BETWEEN '2020-01-01' AND '2020-12-31'
	 AND 	substring(sol."FechaIngresoRegistro",1,7) BETWEEN  substring(sol."FechaIngresoRegistro",0,4)|| '-01' AND substring(sol."FechaIngresoRegistro",0,4)|| '-12'
	 AND 	sol."Estado" = 'CRT'
	 AND 	saapv."IdMedioRecaudacionFormaPago" <> 2
	 AND 	sol."FolioContratoOperacion" <> ''

	 UNION ALL 
	 
	 
	 
	 SELECT 
	 	TO_BIGINT(sol."IdEmpresa") AS "IdEmpresa",
 		TO_BIGINT(sol."IdSolicitudAporteRescate") AS "Numero",
 		LPAD(cli."DocumentoIdentidad",11,' ') AS "RutCliente",
	   	TO_BIGINT(sol."IdCliente") AS "IdCliente",
	    substring(sol."FechaIngresoRegistro",1,10) AS "FechaOperacion",
	      (substring("FechaIngresoRegistro",0,4)) AS "AnoAhorro",
		   --Ahorros
		   0  AS "AhorrosTrabajador",
		   round(sol."MontoMonedaSistema" * (IPCACT."Valor"/IPCINV."Valor"),0) AS "AhorrosDeposito",
		   0 AS "AhorrosCotizaciones",
		   --Retiros
		   0 AS "RetirosTrabajador",
		   (SELECT COALESCE(round(SUM("EGRMonto"),0),0) 
				FROM 
					(--	Para AT2022 solo este select servira
						SELECT (("EGRCantidad" * "MontoCompra") / "Cantidad") * (IPCACT."Valor"/IPCINV."Valor") AS "EGRMonto" , car."IdOperacion"	
						FROM "BSCL.Custodia.BD::CUS.Cartera" car 
						INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" solres ON solres."IdSolicitudAporteRescate" = "EGRIdOperacion"
					  LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINV  ON (substring(ADD_MONTHS (TO_DATE (solres."FechaValorCuota", 'YYYY-MM-DD'), -1),0,7) = IPCINV."AgnoMes")  
		 																		AND IPCINV."IdParametroFinanciero" = 'IPC'
		 			  LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACT  ON (SUBSTRING(solres."FechaValorCuota",0,4) || '-11' = IPCACT."AgnoMes")   		
		 																		AND IPCACT."IdParametroFinanciero" = 'IPC'
																				AND 	solres."IdMedioDePago" <> 'CIN'
						WHERE solres."FechaValorCuota" <> '0001-01-01'
						AND solres."Estado" = 'CRT'
						AND solres."IdTipoMovimientoFondoMutuo" IN ('RESNORM')
					)	car 
				WHERE 	car. "IdOperacion" = sol."IdSolicitudAporteRescate"
				
			) AS "RetirosDeposito",
			0 AS "RetirosCotizaciones",
			CAST('N' AS NVARCHAR(1)) AS "EjecutadoComplementoIPC",
       CAST('N' AS NVARCHAR(1)) AS "EjecutadoAjustarRescates"
	 FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol 
		 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = sol."IdCliente"
		 LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINV  ON (substring(ADD_MONTHS (TO_DATE (substring(sol."FechaIngresoRegistro",1,10), 'YYYY-MM-DD'), -1),0,7) = IPCINV."AgnoMes")  
		 																		AND IPCINV."IdParametroFinanciero" = 'IPC'
		 LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACT  ON (SUBSTRING(sol."FechaIngresoRegistro",0,4) || '-11' = IPCACT."AgnoMes")  		
		 																		AND IPCACT."IdParametroFinanciero" = 'IPC'
	 WHERE 	sol."TipoSolicitud" = 'APO'
	 AND 	sol."IdTipoMovimientoFondoMutuo" = 'APONORM'
	 AND 	sol."IdRegimenTributario" = 'APV-A'
	 AND 	substring(sol."FechaIngresoRegistro",1,7) BETWEEN  substring(sol."FechaIngresoRegistro",0,4)|| '-01' AND substring(sol."FechaIngresoRegistro",0,4)|| '-12'
	 AND 	sol."Estado" = 'CRT'
	 AND 	sol."FolioContratoOperacion" = ''
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1871.Origen" TO bcs_desarrollador WITH GRANT OPTION;