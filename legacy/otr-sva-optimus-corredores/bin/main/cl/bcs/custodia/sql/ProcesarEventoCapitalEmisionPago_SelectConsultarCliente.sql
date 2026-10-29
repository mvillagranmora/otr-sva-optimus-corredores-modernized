SELECT 	CL."IdCliente"										AS "IdCliente",
		SOC."IdSocioDeNegocio"			 					AS "IdSocioDeNegocio",
		CASE 
			WHEN SOC."IdCliente" > 0 		THEN 'CLI'
			WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
			WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
			WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
		END													AS "TipoSocioNegocio",
		IFNULL(CL."TipoDocumentoIdentidad",'')				AS "TipoDocumentoIdentidad",
		IFNULL(CL."DocumentoIdentidad",'')					AS "DocumentoIdentidad",			
		IFNULL(CL."NombreCompleto",'')						AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"					AS CL
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 	AS SOC
		ON (CL."IdCliente" = SOC."IdCliente")
WHERE CL."IdCliente" IN (:IdCliente)
  AND IFNULL(SOC."IdSocioDeNegocio", -1) <> -1;