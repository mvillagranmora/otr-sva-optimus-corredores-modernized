SELECT   DISTINCT		 "PAC"."IdentServicio"
						,"PAC"."IdMandatoPAC"
						--,"DET"."IdCargaMasivaDetallePac"
						,'C' 	AS "EstadoProcesamiento"
						--,"DET"."MontoCargo" 						AS "MontoDeposito"
						,"PAC"."IdCliente"
						--,"DET"."Estado"
						--,"DET"."TipoArchivo"
						,"PAC"."IdPortafolio"
						,"PAC"."FolioCSC"
						,"PAC"."FolioSAAPV"
						,"PAC"."Estado" 							AS "EstadoPAC"
						,IFNULL("SAP"."IdEstado",'')				AS "EstadoSAAPV"
						,IFNULL("ESS"."Nombre", '')					AS "EstadoLargoSAAPV"
						,IFNULL("CSC"."Estado", '')					AS "EstadoCSC"
						,IFNULL("ESC"."Nombre", '')					AS "EstadoLargoCSC"
						,"CLI"."NombreCompleto"
						,"CLI"."DocumentoIdentidad"
						,"CLI"."NombreEstado" 						AS "EstadoCliente"
						,"POR"."IdEstado" 							AS "EstadoPortafolio"
						--,"DET"."EstadoCargo" 						AS "EstadoCargoBanco"
						,"REN"."Descripcion" 						AS "DescripcionEstadoCargo"
						,"PAC"."EstadoCargo" 						AS "EstadoCargo"
						,CASE WHEN (IFNULL("CSC"."MontoPesos",0) = 0) THEN IFNULL("CSC"."MontoUF",0) ELSE "CSC"."MontoPesos" END AS "MontoCSC"
						,CASE WHEN (IFNULL("POC"."MontoPesos",0) = 0) THEN IFNULL("POC"."MontoUF",0) ELSE "POC"."MontoPesos" END AS "MontoSAPV"
						,CASE WHEN ("CSC"."MontoPesos" = 0) THEN 'UF' 			ELSE 'CLP'				END AS "MonedaCSC"
						,CASE WHEN ("POC"."MontoPesos" = 0) THEN 'UF' 			ELSE 'CLP' 				END AS "MonedaSAPV"
						,"BAN"."NombreBanco"
						,"CUB"."NumeroCuentaBancaria" AS "CuentaBancaria"
						,"POR"."CodigoPortafolio"
						,COALESCE("CSC"."Fecha", "SAP"."FechaSuscripcion") AS "FechaSuscripcion"
						,COALESCE("CSC"."DiaDescuento", "SAP"."DiaDescuento") AS "DiaDescuento"
						,"PAC"."IdMandatoPAC" AS "IdMandatoPACUI"
	FROM  "BSCL.Tesoreria.BD::CAR.CargaMasivaDetallePAC" 					AS "DET"
		  LEFT JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC"  					AS "PAC" 		ON ("DET"."IdentServicio"		= "PAC"."IdentServicio")
		  LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas"		AS "CSC"		ON ("CSC"."FolioCSC" 			= "PAC"."FolioCSC"
																								AND "CSC"."Estado"	   NOT IN('ANU','RVC')
																								AND "CSC"."TipoPago" 		= 'PAC' AND "PAC"."FolioCSC" != '0')
	 	  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS "CLI" 		ON ("PAC"."IdCliente" 			= "CLI"."IdCliente")
	 	  LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" 					AS "POR" 		ON ("POR"."IdPortafolio" 		= "PAC"."IdPortafolio")
	 	  LEFT JOIN "BSCL.Tesoreria.BD::PAC.EstadoCargoRendicion" 			AS "REN" 		ON ("REN"."CodigoCargo" 		= "DET"."EstadoCargo")
	 	  LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" 				 		AS "SAP" 		ON ("SAP"."FolioContratoSAAPV" 	= CAST("PAC"."FolioSAAPV" AS VARCHAR)
																								AND "SAP"."IdEstado"	   NOT IN('ANU','RVC') AND CAST("PAC"."FolioSAAPV" AS VARCHAR) != '0')
	 	  LEFT JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" 				AS "POC"		ON ("POC"."IdContratoSAAPV" 	= "SAP"."IdContratoSAAPV")
	 	  LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado"					AS "ESC"		ON ("ESC"."IdEstado" 			= "CSC"."Estado")
		  LEFT JOIN "BSCL.APV.BD::APV.Estado"							AS "ESS"		ON ("SAP"."IdEstado" 			= "ESS"."Codigo" AND "ESS"."NombreEntidad" = 'CONTRATO')
		  LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria"			AS "CUB"		ON ("CUB"."IdCuentaBancaria" = "PAC"."IdCuentaBancaria")
		  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "BAN" ON ("CUB"."IdBanco" = "BAN"."IdBanco")
	WHERE 		"PAC"."IdentServicio" 	=  "DET"."IdentServicio" 
			AND "DET"."Estado" 			= 'ACT' 
			AND "PAC"."Estado"		   != 'ANU' 
			AND "DET"."TipoArchivo" 	= 'REN'
			AND "DET"."TipoRegistro" 	= 'D'
			AND "PAC"."EstadoCargo"     = 'ACT'
			AND "DET"."EstadoCargo" 	= '3';