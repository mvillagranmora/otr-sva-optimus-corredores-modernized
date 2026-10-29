/**
 * Proposito: Obtiene datos del cliente y su portafolio por defecto para la CONSULTA y TRANSACCION BCP
 *            El criterio de búsqueda es una llave compuesta: IdEmpresa + TipoDocumentoIdentidad + DocumentoIdentidad (Ej.: '1DNI12969673', '1RUC20101031340')
 * Autor: JVILLAGRAN - BCS
 * Fecha Creacion: 18/09/2017
 */ 


SELECT 
	"LlaveCliente", "IdPersona", "IdCliente", "NombreCompleto", "IdEstadoPersona", 
	"IdEstadoCliente", "IdTipoDocumentoIdentidad", "DocumentoIdentidad", "IdPortafolioDefecto",
	"IdTipoSocioDeNegocio"
FROM 
(
	SELECT
		(SOC."IdEmpresa" || DI."IdTipoDocumentoIdentidad" || DI."DocumentoIdentidad") AS "LlaveCliente",
		PER."IdPersona", 
		COALESCE(CLI."IdCliente",0) AS "IdCliente",
		CASE "PER"."Tipo"
			WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno"
			ELSE "PER"."RazonSocial"
		END  As "NombreCompleto",
		PER."IdEstado" AS "IdEstadoPersona",		
		
		DI."IdTipoDocumentoIdentidad",
		DI."DocumentoIdentidad",	
		
		-- Datos del Cliente(Si es que lo es)
		COALESCE(CLI."IdPortafolioDefecto",0) AS "IdPortafolioDefecto",
		COALESCE(CLI."IdEstado",'') AS "IdEstadoCliente",
		(CASE 
			WHEN (COALESCE(CLI."IdCliente",0) <> 0) THEN 'CLI' -- La persona es cliente
			ELSE ''                                            -- La persona no está activa como cliente
		END) AS "IdTipoSocioDeNegocio"
		
	FROM       "BSCL.SocioNegocio.BD::PER.Persona" AS PER
	INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdPersona" = PER."IdPersona")	
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON (DI."IdPersona" = PER."IdPersona")		
	LEFT JOIN  "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (PER."IdPersona" = CLI."IdPersona")
	WHERE SOC."Borrado"	= 'N'
)
WHERE "LlaveCliente" IN (:llavesClientes);  -- Llave: IdEmpresa + TipoDocumentoIdentidad + DocumentoIdentidad  (Ej.: '1DNI12969673', '1RUC20101031340')
--WHERE "LlaveCliente" IN ('1DNI12969673');

