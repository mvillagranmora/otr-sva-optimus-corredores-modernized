SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.SocioNegocio.BD.Servicios::SOC.ConsultarSocioDeNegocioDetallado";

CREATE PROCEDURE
-- 
--  Proposito: vista para obtener el socio de negocio segun relacionado Destino, 
--  Autor: OCT - Cristián Flores O.
--  Fecha Creacion: 21/10/2015
--  
"BSCL.SocioNegocio.BD.Servicios::SOC.ConsultarSocioDeNegocioDetallado" ( 
	IN  p_IdEmpresa							BIGINT,
	IN  p_FiltroIdTipoDocumentoIdentidad	NVARCHAR(1),
	IN  p_FiltroDocumentoIdentidad			NVARCHAR(40),
	IN  p_FiltroRazonSocial					NVARCHAR(100),
	IN  p_FiltroEstado						NVARCHAR(3),
	IN  p_FiltroIdRelacionOrigen			NVARCHAR(6),

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	READS SQL DATA
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;

 

	BEGIN

	DECLARE v_FiltroIdTipoDocumentoIdentidad	NVARCHAR(1) 	:= NULL;
	DECLARE v_FiltroDocumentoIdentidad			NVARCHAR(40) 	:= NULL;
	DECLARE v_FiltroRazonSocial					NVARCHAR(100) 	:= NULL;
	DECLARE v_FiltroEstado						NVARCHAR(3) 	:= NULL;
	DECLARE v_FiltroIdRelacionOrigen			NVARCHAR(6) 	:= NULL;
	DECLARE v_MuestraRelacionDestino       		NVARCHAR(1) 	:= 'N';
	
	IF (:p_FiltroIdTipoDocumentoIdentidad != '') THEN 
		v_FiltroIdTipoDocumentoIdentidad := p_FiltroIdTipoDocumentoIdentidad;
	END IF;

	IF (:p_FiltroDocumentoIdentidad != '') THEN 
		v_FiltroDocumentoIdentidad := p_FiltroDocumentoIdentidad;
	END IF;
	
	IF (:p_FiltroRazonSocial != '') THEN 
		v_FiltroRazonSocial := p_FiltroRazonSocial;
	END IF;

	IF (:p_FiltroEstado != '') THEN 
		v_FiltroEstado := p_FiltroEstado;
	END IF;
		
	IF (IFNULL(:p_FiltroIdRelacionOrigen,'') != '') THEN 
		v_MuestraRelacionDestino :='S';
		v_FiltroIdRelacionOrigen := p_FiltroIdRelacionOrigen;
	END IF;
	
	
	--Validacion de datos de busqueda
	IF ((IFNULL(:p_FiltroRazonSocial,'') != '') AND
	   ( ((IFNULL(:p_FiltroIdTipoDocumentoIdentidad,'') != '') OR (IFNULL(:p_FiltroDocumentoIdentidad,'') != '') ) ))  THEN 
	   
	 		SIGNAL SALIDA_CONTROLADA; --Busqueda no realizada		
	END IF;

	

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;



	IF (:v_MuestraRelacionDestino = 'N') THEN
		SELECT
				 "Persona_$1"."IdPersona" ,
				 "SocioDeNegocio_$0"."IdSocioDeNegocio" ,
				-- "SocioDeNegocio_$0"."FechaIngreso" ,
				-- "SocioDeNegocio_$0"."FechaCreacion" ,
				 --"SocioDeNegocio_$0"."FechaModificacion" ,
				 "DocumentoIdentidad_$2"."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidadDefecto" ,
				 "TipoDocumentoIdentidad_$3"."NombreTipoDocumentoIdentidad" AS "NombreTipoDocumentoIdentidadDefecto" ,
				 "DocumentoIdentidad_$2"."DocumentoIdentidad" AS "DocumentoIdentidadDefecto" ,
				 
				 "DocumentoIdentidad_$5"."IdTipoDocumentoIdentidad" ,
				 "TipoDocumentoIdentidad_$6"."NombreTipoDocumentoIdentidad" ,
				 "DocumentoIdentidad_$5"."DocumentoIdentidad" ,
				 "Persona_$1"."Tipo" AS "CodigoTipoPersona" ,
				 "Constante_$8"."Descripcion" AS "TipoPersona" ,
				 "Persona_$1"."RazonSocial" ,
				 "Persona_$1"."IdPaisResidencia" ,
				 "Persona_$1"."FechaNacimiento" ,
				 "Pais_$7"."CodigoPais" AS "CodigoPaisResidencia" ,
				 "Pais_$7"."NombrePais" AS "NombrePaisResidencia" ,
				 CASE WHEN ( "SocioDeNegocio_$0"."IdCliente" > 0 ) 
					THEN 'UI_SI' 
					ELSE 'UI_NO' 
					END AS "EsCliente" ,
						 "SocioDeNegocio_$0"."IdCliente" ,
						 CASE WHEN ( "SocioDeNegocio_$0"."IdEmpleado" > 0 ) 
					THEN 'UI_SI' 
					ELSE 'UI_NO' 
					END AS "EsEmpleado" ,
						 "SocioDeNegocio_$0"."IdEmpleado" ,
						 CASE WHEN ( "SocioDeNegocio_$0"."IdOperador" > 0 ) 
					THEN 'UI_SI' 
					ELSE 'UI_NO' 
					END AS "EsOperador" ,
						 "SocioDeNegocio_$0"."IdOperador" ,
						 CASE WHEN ( "SocioDeNegocio_$0"."IdProveedor" > 0 ) 
					THEN 'UI_SI' 
					ELSE 'UI_NO' 
					END AS "EsProveedor" ,
				 "SocioDeNegocio_$0"."IdProveedor" 
			FROM ( ( ( ( ( ( ( ( "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS "SocioDeNegocio_$0" 
											LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona_$1" ON ( "Persona_$1"."IdPersona" = "SocioDeNegocio_$0"."IdPersona" ) ) 
										LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS "DocumentoIdentidad_$2" ON ( "DocumentoIdentidad_$2"."IdDocumentoIdentidad" = "Persona_$1"."IdDocumentoIdentidad" ) ) 
									LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad_$3" ON ( "TipoDocumentoIdentidad_$3"."IdTipoDocumentoIdentidad" = "DocumentoIdentidad_$2"."IdTipoDocumentoIdentidad" ) ) 
								LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS "PersonaDocumentoIdentidad_$4" ON ( "PersonaDocumentoIdentidad_$4"."IdPersona" = "Persona_$1"."IdPersona" ) ) 
							LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS "DocumentoIdentidad_$5" ON ( "DocumentoIdentidad_$5"."IdDocumentoIdentidad" = "PersonaDocumentoIdentidad_$4"."IdDocumentoIdentidad" ) ) 
						LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad_$6" ON ( "TipoDocumentoIdentidad_$6"."IdTipoDocumentoIdentidad" = "DocumentoIdentidad_$5"."IdTipoDocumentoIdentidad" ) ) 
					LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS "Pais_$7" ON ( "Pais_$7"."IdPais" = "Persona_$1"."IdPaisResidencia" ) ) 
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS "Constante_$8" ON  ( "Constante_$8"."IdTipoPersona" = "Persona_$1"."Tipo" ) )
				
		 	WHERE  (IFNULL(:v_FiltroRazonSocial,'0')='0' OR ("Persona_$1"."RazonSocial" like :v_FiltroRazonSocial || '%'))
		 	 AND (IFNULL(:v_FiltroIdTipoDocumentoIdentidad,'0')='0' OR ("DocumentoIdentidad_$2"."IdTipoDocumentoIdentidad"= :v_FiltroIdTipoDocumentoIdentidad))
		 	 AND (IFNULL(:v_FiltroDocumentoIdentidad,'0')='0' OR ( "DocumentoIdentidad_$2"."DocumentoIdentidad" = :v_FiltroDocumentoIdentidad));
		 	 
		 	 
	
	END IF;
	
	
	
	IF (:v_MuestraRelacionDestino = 'S') THEN
		SELECT
				 "Persona_$1"."IdPersona" ,
				 "Relacion"."IdTipoRelacion",
				 "SocioDeNegocio_$0"."IdSocioDeNegocio" ,
				 --"SocioDeNegocio_$0"."FechaIngreso" ,
				 --"SocioDeNegocio_$0"."FechaCreacion" ,
				 --"SocioDeNegocio_$0"."FechaModificacion" ,
				 "DocumentoIdentidad_$2"."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidadDefecto" ,
				 "TipoDocumentoIdentidad_$3"."NombreTipoDocumentoIdentidad" AS "NombreTipoDocumentoIdentidadDefecto" ,
				 "DocumentoIdentidad_$2"."DocumentoIdentidad" AS "DocumentoIdentidadDefecto" ,
				 
				 "DocumentoIdentidad_$5"."IdTipoDocumentoIdentidad" ,
				 "TipoDocumentoIdentidad_$6"."NombreTipoDocumentoIdentidad" ,
				 "DocumentoIdentidad_$5"."DocumentoIdentidad" ,
				 "Persona_$1"."Tipo" AS "CodigoTipoPersona" ,
				 "Constante_$8"."Descripcion" AS "TipoPersona" ,
				 "Persona_$1"."RazonSocial" ,
				 "Persona_$1"."IdPaisResidencia" ,
				 "Persona_$1"."FechaNacimiento" ,
				 "Pais_$7"."CodigoPais" AS "CodigoPaisResidencia" ,
				 "Pais_$7"."NombrePais" AS "NombrePaisResidencia" ,
				 CASE WHEN ( "SocioDeNegocio_$0"."IdCliente" > 0 ) 
					THEN 'UI_SI' 
					ELSE 'UI_NO' 
					END AS "EsCliente" ,
						 "SocioDeNegocio_$0"."IdCliente" ,
						 CASE WHEN ( "SocioDeNegocio_$0"."IdEmpleado" > 0 ) 
					THEN 'UI_SI' 
					ELSE 'UI_NO' 
					END AS "EsEmpleado" ,
						 "SocioDeNegocio_$0"."IdEmpleado" ,
						 CASE WHEN ( "SocioDeNegocio_$0"."IdOperador" > 0 ) 
					THEN 'UI_SI' 
					ELSE 'UI_NO' 
					END AS "EsOperador" ,
						 "SocioDeNegocio_$0"."IdOperador" ,
						 CASE WHEN ( "SocioDeNegocio_$0"."IdProveedor" > 0 ) 
					THEN 'UI_SI' 
					ELSE 'UI_NO' 
					END AS "EsProveedor" ,
				 "SocioDeNegocio_$0"."IdProveedor" 
			FROM ( ( (( ( ( ( ( ( "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS "SocioDeNegocio_$0" 
											LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona_$1" ON ( "Persona_$1"."IdPersona" = "SocioDeNegocio_$0"."IdPersona" ) ) 
										LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS "DocumentoIdentidad_$2" ON ( "DocumentoIdentidad_$2"."IdDocumentoIdentidad" = "Persona_$1"."IdDocumentoIdentidad" ) ) 
									LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad_$3" ON ( "TipoDocumentoIdentidad_$3"."IdTipoDocumentoIdentidad" = "DocumentoIdentidad_$2"."IdTipoDocumentoIdentidad" ) ) 
								LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS "PersonaDocumentoIdentidad_$4" ON ( "PersonaDocumentoIdentidad_$4"."IdPersona" = "Persona_$1"."IdPersona" ) ) 
							LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS "DocumentoIdentidad_$5" ON ( "DocumentoIdentidad_$5"."IdDocumentoIdentidad" = "PersonaDocumentoIdentidad_$4"."IdDocumentoIdentidad" ) ) 
						LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad_$6" ON ( "TipoDocumentoIdentidad_$6"."IdTipoDocumentoIdentidad" = "DocumentoIdentidad_$5"."IdTipoDocumentoIdentidad" ) ) 
					LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS "Pais_$7" ON ( "Pais_$7"."IdPais" = "Persona_$1"."IdPaisResidencia" ) ) 
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS "Constante_$8" ON  ( "Constante_$8"."IdTipoPersona" = "Persona_$1"."Tipo" ) )
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarPersonaPorTipoRelacion" AS "Relacion" 	ON ("Relacion"."IdPersona"="SocioDeNegocio_$0"."IdPersona" ))
		
		 	WHERE "Relacion"."IdTipoRelacion" = :p_FiltroIdRelacionOrigen
		 	
		 	 AND (IFNULL(:v_FiltroRazonSocial,'0')='0' OR ("Persona_$1"."RazonSocial" like :v_FiltroRazonSocial || '%'))
		 	 AND (IFNULL(:v_FiltroIdTipoDocumentoIdentidad,'0')='0' OR ("DocumentoIdentidad_$2"."IdTipoDocumentoIdentidad"= :v_FiltroIdTipoDocumentoIdentidad))
		 	 AND (IFNULL(:v_FiltroDocumentoIdentidad,'0')='0' OR ( "DocumentoIdentidad_$2"."DocumentoIdentidad" = :v_FiltroDocumentoIdentidad))
		 	 AND (IFNULL(:v_FiltroEstado,'0')='0' OR ("Relacion"."IdEstado"= :v_FiltroEstado));
		 	 
	
	END IF;
	
	END;

	
END;
