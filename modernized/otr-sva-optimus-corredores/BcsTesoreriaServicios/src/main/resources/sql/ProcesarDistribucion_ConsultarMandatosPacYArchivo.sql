SELECT  DISTINCT IFNULL("PAC"."IdentServicio",'N')				AS "Observacion" --Si es N, quiere decir que no hay mandato asociado
				,"DET"."IdentServicio"
				,'C' 					AS "EstadoProcesamiento"
				,"DET"."MontoCargo" 						AS "MontoDeposito"
				,CASE WHEN "PAC"."Estado" != 'ING' OR "PAC"."EstadoCargo" != 'ACT' THEN 'INA'
																				   ELSE "DET"."Estado"
				END 					AS "EstadoCargo"
				,"DET"."TipoArchivo"
				,''						AS "EstadoSAAPV"
				,''						AS "EstadoLargoSAAPV"
				,''						AS "EstadoCSC"
				,''						AS "EstadoLargoCSC"
				,"CLI"."NombreCompleto"
				,"CLI"."DocumentoIdentidad"
				,"CLI"."NombreEstado" 						AS "EstadoCliente"
				,"DET"."EstadoCargo" 	AS "EstadoCargoBanco"
				,0.0					AS "MontoCSC"
				,0.0					AS "MontoSAPV"
				,'' 					AS "MonedaCSC"
				,'' 					AS "MonedaSAPV"
FROM   	"BSCL.Tesoreria.BD::CAR.CargaMasivaDetallePAC"	AS "DET" 
LEFT OUTER JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC" 	AS "PAC"		ON ("DET"."IdentServicio" = "PAC"."IdentServicio" AND "PAC"."EstadoCargo"='ACT')
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS "CLI" 		ON ("PAC"."IdCliente" 			= "CLI"."IdCliente")

WHERE	"DET"."Estado" 			= 'ACT'  AND
		"DET"."TipoArchivo" 	= 'REN' AND
		"DET"."TipoRegistro" 	= 'D'	AND
		"DET"."EstadoCargo"		= '3';