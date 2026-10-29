SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido"  AS 

	SELECT "E"."Codigo" 													AS "CodigoEstado"
		 , "E"."Nombre" 													AS "NombreEstado"
		--,TO_DATE("C"."FechaIngreso", 'YYYY-MM-DD')  AS "FechaDeposito"
		 , TO_DATE("CR"."FechaDeposito", 'YYYY-MM-DD')						AS "FechaDeposito"
		 , "CCR"."FechaIngreso"												AS "FechaIngreso"
		 , "CR"."IdRecaudacion"												AS "FolioRecaudacion"
		 , COALESCE("EM"."IdRecaudacionEmpleador",0)						AS "IdRecaudacionEmpleador"
		 , COALESCE("CCR"."IdRecaudacionCliente",0)							AS "IdRecaudacionCliente"
		 , "CR"."FolioCartola"												AS "FolioCartola"
		 , "CR"."LineaCartola"												AS "LineaCartola"
		 , COALESCE("CCR"."IdPersona",0)									AS "IdPersonaCliente"
		 , COALESCE("SN_CLI"."IdSocioDeNegocio",0)									AS "IdSocioNegocioCliente"
		 , COALESCE("CCR"."IdCliente",0)  									AS "IdCliente"
		 , COALESCE("CCR"."DocumentoIdentidad",'') 							AS "RutCliente"
		 , COALESCE("CCR"."NombreCompleto",'') 								AS "Nombre"
		 , COALESCE("CR"."IdTipoDeposito",'') 							    AS "IdTipoDeposito"
		 , COALESCE("CR"."NombreTipoDeposito",'') 							AS "NombreTipoDeposito"
		 , CASE 
		 	WHEN (COALESCE("CCR"."MontoDepositoConvenido",0) != 0) 
		 	THEN "CCR"."MontoDepositoConvenido" 
		 	ELSE 
		 		CASE 
		 			WHEN (COALESCE("CCR"."MontoDepositoAPV",0) != 0)		 
		 			THEN "CCR"."MontoDepositoAPV" 		 
		 			ELSE 0 
		 		END	
		 	END AS "RecMonto" --("E"."Codigo" !='DIS' AND "CCR"."MontoDepositoConvenido" IS NOT NULL) THEN "CCR"."MontoDepositoConvenido" ELSE 0 END AS "RecMontoDC" 
		 , CASE 
		 	WHEN (COALESCE("CCR"."MontoDepositoConvenido",0) != 0)
		 	THEN 'DC' 
		 	ELSE 
		 		CASE 
		 			WHEN (COALESCE("CCR"."MontoDepositoAPV",0) != 0)		 
		 			THEN 'APV'		 
		 			ELSE '' 
		 		END	
		 	END AS "TipoRegimen"
		 , CASE WHEN ("CAPV"."FolioContratoSAAPV" IS NOT NULL) THEN	"CAPV"."FolioContratoSAAPV" ELSE '' END AS "FolioContrato"
		 , CASE WHEN ("CCR"."TotalDigitado" IS NOT NULL) 		 THEN "CCR"."TotalDigitado" 		 ELSE 0 END AS "RecTotal" --("E"."Codigo" !='DIS' AND "CCR"."TotalDigitado" IS NOT NULL) 		 THEN "CCR"."TotalDigitado" 		 ELSE 0 END AS "RecTotal" 
		 , CASE WHEN ("CCR"."DiferenciaDigitado" IS NOT NULL) 	 THEN "CCR"."DiferenciaDigitado" 	 ELSE 0 END	AS "RecDiferencia" --("E"."Codigo" !='DIS' AND "CCR"."DiferenciaDigitado" IS NOT NULL) 	 THEN "CCR"."DiferenciaDigitado" 	 ELSE 0 END	AS "RecDiferencia" 
		 , CASE WHEN ("E"."Codigo"  ='DIS' AND "CCR"."MontoDepositoConvenido" IS NOT NULL) THEN "CCR"."MontoDepositoConvenido" ELSE 0 END AS "DisMontoDC" 
		 , CASE WHEN ("E"."Codigo"  ='DIS' AND "CCR"."MontoDepositoAPV" IS NOT NULL) 		 THEN "CCR"."MontoDepositoAPV" 		 ELSE 0 END AS "DisMontoAPV" 
		 , CASE WHEN ("E"."Codigo"  ='DIS' AND "CCR"."TotalDigitado" IS NOT NULL) 		 THEN "CCR"."TotalDigitado" 		 ELSE 0 END AS "DisTotal" 
		 , CASE WHEN ("E"."Codigo"  ='DIS' AND "CCR"."DiferenciaDigitado" IS NOT NULL) 	 THEN "CCR"."DiferenciaDigitado" 	 ELSE 0 END AS "DisDiferencia" 
		 , COALESCE("CR"."IdBanco",0)									    AS "IdBanco"
		 , COALESCE("B"."CodigoBanco",'')									AS "CodigoBanco"
		 , "CR"."CuentaCorriente" 											AS "CuentaCorriente"
		 , COALESCE("CCR"."IdRegimenTriburario",'')							AS "Regimen"
		 , COALESCE("EM"."IdPersona",0)									    AS "IdPersonaEmpleador"	
		 , COALESCE("SN_EMP"."IdSocioDeNegocio",0)									    AS "IdSocioNegocioEmpleador"
		 , COALESCE("CP"."DocumentoIdentidad",'')							AS "RutEmpleador"
		 , COALESCE("CP"."NombreCompleto",'')								AS "NombreEmpleador"
		 , COALESCE("ETRAN"."Observacion",'')								AS "Observacion"
		 , COALESCE("EM"."PeriodoPago",'')								AS "PeriodoPago"
	FROM "BSCL.APV.BD.Vistas::REC.ConsultarRecaudacion" AS "CR"
		LEFT JOIN "BSCL.APV.BD.Vistas::REC.ConsultarClienteRecaudacion" AS "CCR"
			ON "CR"."IdRecaudacion"	 =  "CCR"."IdRecaudacion"
		LEFT JOIN "BSCL.APV.BD::REC.Empleador" AS "EM" 
				ON "CCR"."IdRecaudacionEmpleador"	 =  "EM"."IdRecaudacionEmpleador" 
		INNER JOIN "BSCL.APV.BD::APV.Estado"  AS "E"
			ON "E"."Codigo" = "CCR"."IdEstado"  AND "NombreEntidad" = 'RECAUDACION'
		INNER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "B"
			ON "B"."IdBanco" =  "CR"."IdBanco"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"AS "CP"
			ON "CP"."IdPersona" = "EM"."IdPersona"	
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" "SN_CLI" 
			ON "CCR"."IdPersona" = "SN_CLI"."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" "SN_EMP" 
			ON "CP"."IdPersona" = "SN_EMP"."IdPersona"
	    LEFT JOIN (SELECT EstadoTransicion.* FROM  "BSCL.APV.BD.Vistas::REC.ConsultarEstadoTransicion" EstadoTransicion
						INNER JOIN (SELECT MAX("IdEstadoTransicion") AS "IdEstadoTransicion", "IdRegistro" 
									FROM  "BSCL.APV.BD.Vistas::REC.ConsultarEstadoTransicion" 
									GROUP BY "IdRegistro") UltimoEstado
					ON UltimoEstado."IdEstadoTransicion" = EstadoTransicion."IdEstadoTransicion"
			) "ETRAN"
	    	ON  "ETRAN"."IdRegistro" = "CCR"."IdRecaudacionCliente"
	    LEFT JOIN "BSCL.APV.BD::REC.AsignacionContratoRecaudacion" AS "ACR"
	    	ON "ACR"."IdRecaudacionCliente" = "CCR"."IdRecaudacionCliente"
	    LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS "CAPV"
	    	ON "ACR"."IdContratoSAAPV" = "CAPV"."IdContratoSAAPV";
	    	
GRANT SELECT ON "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido" TO  BCS_DESARROLLADOR WITH GRANT OPTION;