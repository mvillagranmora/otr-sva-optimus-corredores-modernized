SET SCHEMA "BSCL";

DROP PROCEDURE   "BSCL.Plataforma.BD.Servicios::IND.AgregarTipoIndicador";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para agregar un Tipo de Indicador a la lista disponible en ficha 'Socio de negocio'.
--  Autor: - CFLORES
--  Fecha Creacion: 07/04/2016 
--  
"BSCL.Plataforma.BD.Servicios::IND.AgregarTipoIndicador" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  		--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1),	-- S/N se usa en caso de servicios anidados.
	
		  
	-- Parametros entrada del servicio publico
	
	IN	p_IdTipoEntidad      		NVARCHAR(40),
	IN	p_ValorIndicadorDefecto 	NVARCHAR(255), -- <TipoCliente>
	IN	p_ClaseDato         		NVARCHAR(40),
	IN	p_NombreTipoIndicador     	NVARCHAR(255),
	IN	p_CodigoTipoIndicador	 	NVARCHAR(40),
	IN	p_GrupoIndicador       		NVARCHAR(40),
	IN	p_IdEstado		      		NVARCHAR(3),
	IN	p_CantidadDecimales 		BIGINT DEFAULT 0,
	IN	p_LongitudMinima			BIGINT DEFAULT 0,
	IN	p_LongitudMaxima			BIGINT DEFAULT 0,
	IN	p_RequiereAutorizacion		NVARCHAR(1),
	IN  p_IdTipoPersona				NVARCHAR(1) DEFAULT 'A',
	IN  p_RequiereFechaVencimiento  NVARCHAR(1) DEFAULT 'N',
	
	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
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
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdTipoIndicador		 		bigint:=0;
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
     	DECLARE v_IdUso		NVARCHAR(3):='IND'; 
					
		DECLARE v_countAtributo	 		   bigint:=0;
		--------------------
		--Validaciones
		--------------------
		

	BEGIN

			---Verifica si el Atributo no este registrado
			SELECT COUNT(1) INTO v_countAtributo
			FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicador"
			WHERE "IdTipoEntidad" = :p_IdTipoEntidad 
				AND "CodigoTipoIndicador" = :p_CodigoTipoIndicador
				AND "IdEmpresa"= :p_IdEmpresa
				AND "IdUso" = :v_IdUso;
			IF(v_countAtributo>0) THEN
				p_out_ErroresDetalle =  select 'INDICADOR_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;							
	END;		

		
		
				
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------
				
		BEGIN
			
			    -- recuperamos numero de secuencia que usaremos para el id del documento
		    SELECT "BSCL.Plataforma.BD.Secuencias::TipoIndicador".NEXTVAL INTO v_IdTipoIndicador FROM DUMMY;
	
			p_out_RefId:=v_IdTipoIndicador;
			
		 	------------------------------------------
			--Crear Documentos de identidad
			------------------------------------------


					insert into "BSCL.Plataforma.BD::IND.TipoIndicador"
					(
					   "IdTipoIndicador",
					   "IdTipoEntidad",
					   "IdEmpresa",
					   "ValorIndicadorDefecto", -- <TipoCliente>
					   "ClaseDato",
					   "NombreTipoIndicador",  
					   "CodigoTipoIndicador", 
					   "GrupoIndicador",
					   "IdUso", -- IND: Indicador, REQ: Requisito
					   "IdEstado",
				       "CantidadDecimales",
					   "LongitudMinima",
					   "LongitudMaxima",
					   "RequiereAutorizacion",
					   "IdTipoPersona",
					   "RequiereFechaVencimiento"		
					) VALUES (
						v_IdTipoIndicador,
						p_IdTipoEntidad,
						p_IdEmpresa,
						p_ValorIndicadorDefecto,
						p_ClaseDato,
						p_NombreTipoIndicador,
						p_CodigoTipoIndicador,
						p_GrupoIndicador,
						v_IdUso,
						p_IdEstado,
						p_CantidadDecimales,
						p_LongitudMinima,
						p_LongitudMaxima,
						p_RequiereAutorizacion,
						p_IdTipoPersona,
						p_RequiereFechaVencimiento
						);
						
					IF :p_ClaseDato != 'CHECKBOX' AND :p_ClaseDato != 'MULTI' AND :v_IdUso != 'REQ' THEN
						INSERT INTO "BSCL.Plataforma.BD::FRM.Campo"
						(
							"CodigoCampo"
							,"CodigoCampoFrontend"
							,"CodigoValorFrontend"
							,"CodigoCampoBackend"
							,"NombreCampo"
							,"TipoDato"
							,"ValorDefecto"
							,"TipoCampoFrontend"
							,"ConfiguracionFrontend"
							,"IdEstado"
						)
						VALUES(
							'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
							,:p_CodigoTipoIndicador
							,'ValorIndicador'
							,'ValorIndicador'
							,:p_NombreTipoIndicador
							,'TEXT'
							,''
							,CASE
								WHEN :p_ClaseDato = 'NVARCHAR' THEN 'bcs-input-texto'
								WHEN :p_ClaseDato = 'DATE' THEN 'bcs-input-date'
								WHEN :p_ClaseDato = 'COMBO' THEN 'bcs-combobox-base'
								ELSE ''
							END
							,CASE
								WHEN :p_ClaseDato = 'COMBO' THEN '{"tipo":"combobox-detalle-tipo-atributo"}'
								ELSE ''
							END
							,'ACT'
						);
						
						IF :p_IdTipoEntidad = 'PERSONA' THEN
						
							IF :v_IdUso = 'IND' THEN
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'INDICADORES_PERSONA_FULL_JURIDICA'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'INDICADORES_PERSONA_FULL_NATURAL'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'INDICADORES_PERSONA_EXPRESS_JURIDICA'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'INDICADORES_PERSONA_EXPRESS_NATURAL'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
							END IF;
						END IF;
						
						IF :p_IdTipoEntidad = 'CLIENTE' THEN
						
							IF :v_IdUso = 'IND' THEN
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'INDICADORES_CLIENTE_FULL_JURIDICA'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'INDICADORES_CLIENTE_FULL_NATURAL'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'INDICADORES_CLIENTE_EXPRESS_JURIDICA'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'INDICADORES_CLIENTE_EXPRESS_NATURAL'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
							END IF;
							
							IF :v_IdUso = 'PRE' THEN
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'PREFERENCIAS_NEGOCIOS_CLIENTE_FULL_JURIDICA'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'PREFERENCIAS_NEGOCIOS_CLIENTE_FULL_NATURAL'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'PREFERENCIAS_NEGOCIOS_CLIENTE_EXPRESS_JURIDICA'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'PREFERENCIAS_NEGOCIOS_CLIENTE_EXPRESS_NATURAL'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
							END IF;
						END IF;
						
						IF :p_IdTipoEntidad = 'PORTAFOLIO' THEN
						
							IF :v_IdUso = 'IND' THEN
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'PORTAFOLIO_CLIENTE_INDICADORES_FULL_JURIDICA'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'PORTAFOLIO_CLIENTE_INDICADORES_FULL_NATURAL'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'PORTAFOLIO_CLIENTE_INDICADORES_EXPRESS_JURIDICA'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'PORTAFOLIO_CLIENTE_INDICADORES_EXPRESS_NATURAL'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
							END IF;
							
							IF :v_IdUso = 'PRE' THEN
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'PORTAFOLIO_CLIENTE_PREFERENCIA_NEGOCIO_FULL_JURIDICA'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'PORTAFOLIO_CLIENTE_PREFERENCIA_NEGOCIO_FULL_NATURAL'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'PORTAFOLIO_CLIENTE_PREFERENCIA_NEGOCIO_EXPRESS_JURIDICA'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
								
								INSERT INTO "BSCL.Plataforma.BD::FRM.CampoFormulario"
								(
									"CodigoFormulario"
									,"CodigoCampo"
									,"CantidadMinima"
									,"Requerido"
									,"IdTipoCanal"
								)
								SELECT
									'PORTAFOLIO_CLIENTE_PREFERENCIA_NEGOCIO_EXPRESS_NATURAL'
									,'INGRESAR_INDICADORES_' || UPPER(:p_CodigoTipoIndicador)
									,0
									,'N'
									,"IdTipoCanalIngreso"
								FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteEnrolamiento" = 'S';
							END IF;
						END IF;
					END IF;
					
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
