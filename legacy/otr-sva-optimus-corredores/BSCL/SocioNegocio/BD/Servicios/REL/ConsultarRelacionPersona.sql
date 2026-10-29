SET SCHEMA "BSCL";
 GO
DROP PROCEDURE "BSCL.SocioNegocio.BD.Servicios::REL.ConsultarRelacionPersona";
 GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para consultar personas relacionadas a una entidad.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 04/02/2016

"BSCL.SocioNegocio.BD.Servicios::REL.ConsultarRelacionPersona" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,

    --VARIABLES DEL PROCESO
	--Parametros de Entidad principal
	--Socio de Negocio
	IN	p_IdSocioDeNegocio						BIGINT 			DEFAULT 0,
	--Cliente
	IN	p_IdCliente 							BIGINT 			DEFAULT 0,
	--Persona
	IN	p_IdPersona 							BIGINT 			DEFAULT 0, --*
	IN	p_IdTipoDocumentoIdentidad				NVARCHAR(1)		DEFAULT '',
	IN	p_DocumentoIdentidad				    NVARCHAR(40) 	DEFAULT '',
	--Parametros de Entidad destino	
	--Relacionados
	IN  p_IdTipoRelacion						NVARCHAR(6)	DEFAULT '',
	IN	p_IdPersonaDestino 						BIGINT 			DEFAULT 0,
  

    --VARIABLES DE SALIDA OBLIGATORIAS

	OUT p_out_PersonasRelacionadas   table ("IdPersonaOrigen"			BIGINT,
										 "IdRelacionPersona"		BIGINT, 
										 "Administracion" 			NVARCHAR(40),
   										 "IdTipoRelacion" 			NVARCHAR(6),
    									 "NombreTipoRelacion"		NVARCHAR(520),
    									 "FechaInicioRelacion" 		SECONDDATE,
    									 "FechaFinRelacion" 		SECONDDATE,
    									 "IdEstado" 				NVARCHAR(3),
    									 "NombreEstado" 			NVARCHAR(100),
    								   	 "IdPersona" 				BIGINT, 
										 "DocumentoIdentidadDefecto" NVARCHAR(40),
										 "NombreCompleto"			NVARCHAR(400),
										 "RazonSocial" 				NVARCHAR(100),
										 "Alias" 					NVARCHAR(100),    
										 "Nombres" 					NVARCHAR(100),
										 "ApellidoPaterno"			NVARCHAR(100),
										 "ApellidoMaterno" 			NVARCHAR(100),    
										 "FechaNacimiento" 			DAYDATE,
										 "IdPaisResidencia" 		BIGINT,
										 "IdTipoDocumentoIdentidadDefecto"				NVARCHAR(1),
										 "IdEmpresa" 				BIGINT,
										 "CodigoTipoPersona"						NVARCHAR(2),
										 "TipoPersona" 				NVARCHAR(300),
										 "NombreTipoDocumentoIdentidadDefecto" 			NVARCHAR(100))
 
	
	--"BSCL.SocioNegocio.BD::REL.PersonasRelacionadas"
	
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	READS SQL DATA
    AS
BEGIN

v_PersonasConsulta = SELECT "IdPersona"
				FROM "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio"
				WHERE (p_IdSocioDeNegocio			= 0 OR  "IdSocioDeNegocio"  				=  p_IdSocioDeNegocio)
	  			AND   (p_IdCliente		 			= 0 OR  "IdCliente" 						=  p_IdCliente)
	  			AND   (p_IdPersona		 			= 0 OR  "IdPersona" 						=  p_IdPersona)
	  			AND   (p_IdTipoDocumentoIdentidad	= '' OR  "IdTipoDocumentoIdentidadDefecto" 	=  p_IdTipoDocumentoIdentidad)
	  			AND   (p_DocumentoIdentidad			= '' OR  "DocumentoIdentidadDefecto" 		=  p_DocumentoIdentidad);	  				  			


	p_out_PersonasRelacionadas = SELECT	DISTINCT REL."IdPersonaOrigen",
									 REL."IdRelacionPersona", 
									 REL."Administracion",
									 REL."IdTipoRelacion",
									 REL."NombreTipoRelacion",
									 REL."FechaInicioRelacion",
									 REL."FechaFinRelacion",
									 REL."IdEstado",
									 REL."NombreEstado",
									 REL."IdPersonaDestino" AS "IdPersona", ---DESTINO
									 CPER."DocumentoIdentidad" AS "DocumentoIdentidadDefecto",
									 CPER."NombreCompleto",
									 CPER."RazonSocial",
									 CPER."Alias",    
									 CPER."Nombres",
									 CPER."ApellidoPaterno",
									 CPER."ApellidoMaterno",    
									 CPER."FechaNacimiento",
									 CPER."IdPaisResidencia",
									 CPER."TipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidadDefecto",
									 REL."IdEmpresa",
									 CPER."Tipo" AS "CodigoTipoPersona",
									 TPER."Descripcion"	AS "TipoPersona",
									 TIDEN."NombreAbrTipoDocumentoIdentidad" AS "NombreTipoDocumentoIdentidadDefecto"
            FROM 
            	"BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" REL
            	INNER JOIN :v_PersonasConsulta PER ON PER."IdPersona" IN (REL."IdPersonaDestino") --, REL."IdPersonaDestino")
            	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS CPER ON REL."IdPersonaOrigen" = CPER."IdPersona"
            	--INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON (DI."IdPersona" = PER."IdPersona" AND DI."IdDocumentoIdentidad" = CPER."IdDocumentoIdentidad")
            	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" as TPER ON TPER."IdTipoPersona" = CPER."Tipo"
            	INNER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" as TIDEN ON  REL."IdTipoDocumentoIdentidadDefecto" = TIDEN."IdTipoDocumentoIdentidad" 
					WHERE  (p_IdPersonaDestino			= 0  OR  REL."IdPersonaDestino" 		=  p_IdPersonaDestino)	  			
					AND    (p_IdTipoRelacion			= ''  OR  REL."IdTipoRelacion" 	=  p_IdTipoRelacion)
					AND  REL."IdEmpresa"=p_IdEmpresa
			ORDER BY 
				REL."IdPersonaDestino",REL."Administracion";
END; 