SET SCHEMA "BSCL";
GO
DROP PROCEDURE   "BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaDocumentoIdentidad";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de documentos de identidad.
--  Autor: - CFLORES
--  Fecha Creacion: 15/12/2015 
--  
 "BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaDocumentoIdentidad" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  		--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1),	-- S/N se usa en caso de servicios anidados.
	IN	p_IdPersona 							BIGINT,
		  
	-- Parametros entrada del servicio publico
	IN	p_IdTipoDocumentoIdentidad				NVARCHAR(3)	,
	IN	p_DocumentoIdentidad				    NVARCHAR(40),
	IN	p_NumeroSerie							NVARCHAR(40)	DEFAULT '',
	IN	p_FechaEmision							NVARCHAR(27),
	IN	p_FechaVencimiento						NVARCHAR(27)	DEFAULT '0001-01-01',
	--IN	p_IdEstado								NVARCHAR(3),
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
		DECLARE v_IdDocumentoIdentidad	 		bigint:=0;
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdConcepto	       			NVARCHAR(6):='NUEDOC';
		DECLARE v_RequiereAutorizacion			NVARCHAR(1):='';
		DECLARE v_IdEstadoAutorizacion			NVARCHAR(3):='PEN';
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
					
		DECLARE v_countDocumento	 		   	bigint:=0;
		DECLARE v_countTipoDocumentoVigente		bigint:=0;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
		
		--------------------
		--Validaciones
		--------------------

		BEGIN

			---Verifica si el documento de identidad no esta registrado en otra entidad 
			SELECT COUNT(1) INTO v_countDocumento
			FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" --ya considera el campo borrado
			WHERE "DocumentoIdentidad" = :p_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :p_IdTipoDocumentoIdentidad;
			IF(v_countDocumento>0) THEN
			    v_hash= select 'UI_DOCUMENTO_IDENTIDAD_EXISTE' as "Codigo", '' as "Valor" FROM DUMMY;
                CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_DOCUMENTO_IDENTIDAD_EXISTE',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'UI_DOCUMENTO_IDENTIDAD_EXISTE' as "Codigo", :v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;		
			
			---Verifica que solo exista un tipo de documento de identidad del mismo tipo por persona
			SELECT COUNT(1) INTO v_countTipoDocumentoVigente
			FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 
			WHERE "IdPersona" = :p_IdPersona AND "IdTipoDocumentoIdentidad" = :p_IdTipoDocumentoIdentidad;
			
			IF(v_countTipoDocumentoVigente>0) THEN
			    v_hash= select 'UI_TIPO_DOCUMENTO_IDENTIDAD_EXISTE' as "Codigo", '' as "Valor" FROM DUMMY;
                CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_TIPO_DOCUMENTO_IDENTIDAD_EXISTE',:v_hash,:v_erroroutmsg);
                p_out_ErroresDetalle =  select 'UI_TIPO_DOCUMENTO_IDENTIDAD_EXISTE' as "Codigo", :v_erroroutmsg as "Descripcion" from dummy;
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
			--CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			
			-- recuperamos numero de secuencia que usaremos para el id del documento
		    SELECT "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad".NEXTVAL INTO v_IdDocumentoIdentidad FROM DUMMY;
	
			p_out_RefId:=v_IdDocumentoIdentidad;
			
		 	------------------------------------------
			--Crear Documentos de identidad
			------------------------------------------


			insert into "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
			(
				"IdPersona",
				"IdDocumentoIdentidad",
				"DocumentoIdentidad",
				"IdTipoDocumentoIdentidad",
				"NumeroSerie",
				"FechaEmision",
				"FechaVencimiento",
				"IdEstado",
				"FechaIngreso",
				"FechaCreacion",
				"FechaModificacion",
				"IdEstadoAutorizacion",
				"IdUsuarioCreador" 
			) VALUES (
				p_IdPersona,
				v_IdDocumentoIdentidad, 
				p_DocumentoIdentidad,
				p_IdTipoDocumentoIdentidad,
				p_NumeroSerie,
				p_FechaEmision,
				p_FechaVencimiento,
				'ACT',
				v_FechaIngreso,--TO_TIMESTAMP(p_FechaIngreso,'YYYY-MM-DD HH24:MI:SS.FF7') 					
				CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
				CURRENT_UTCTIMESTAMP, --p_FechaModificacion); 
				v_IdEstadoAutorizacion,
				p_IdUsuario --IdUsuarioCreador
			); 
			------------------------------------------
			--Pregunta Si requiere autorizacion
			------------------------------------------
			V_RegistroSolicitudCambio =SELECT  * FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" WHERE 1=2;
			
			call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:V_RegistroSolicitudCambio,
				:v_IdConcepto,
				:v_RequiereAutorizacion
			);	
			
			/*
			call "BSCL.SocioNegocio.BD.Servicios::PER.RequiereAutorizacionPersonaDocumentosIdentidad" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:V_RegistroSolicitudCambio,
				:v_IdConcepto,
				:v_RequiereAutorizacion
			); */
						
		if v_RequiereAutorizacion ='S' and p_UsaReglasNegocio != 'N' then
			------------------------------------------
			--Genera Evento de Autorizacion
			------------------------------------------

				DECLARE	 v_Glosa		NVARCHAR(520);
				DECLARE v_out_idAuth 		bigint;
				DECLARE v_out_AuthAdvertenciasContador 	int;
				DECLARE v_out_AuthErroresContador 		int;
				v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
				
				SELECT "DocumentoIdentidad" || ' / ' || "NombreCompleto" || ' / NuevoDoc: ' || p_DocumentoIdentidad  into  v_Glosa 
				FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona" = p_IdPersona;		
								
					
						CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
							p_IdEmpresa 				=> p_IdEmpresa,
							p_IdUsuario 				=> p_IdUsuario,
							p_IdModulo 					=> p_IdModulo,
							p_UsaReglasNegocio 			=> 'N',								
							p_IdTipoEvento 				=> v_IdConcepto,--'NUEDOC',
							p_IdModuloSolicita 			=> p_IdModulo,
							p_IdUsuarioSolicita 		=> p_IdUsuario,
							p_Fecha 					=> TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD'), 
							p_IdConcepto 				=> v_IdConcepto,
							p_NumeroOperacionConcepto 	=>  CAST (v_IdDocumentoIdentidad AS NVARCHAR(40)),--v_secuencia_IdSolicitudCambio,
							p_FechaOperacionConcepto 	=> TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'),
							p_IdCliente 				=> p_IdPersona, --v_IdCliente,
							p_Glosa 					=> v_Glosa,
							--salida
							p_out_IdEvento 				=> v_out_idAuth,
							p_out_AdvertenciasContador 	=> v_out_AuthAdvertenciasContador,
							p_out_AdvertenciasDetalle 	=>  v_out_AuthAdvertenciasDetalle,
							p_out_ErroresContador 		=>  v_out_AuthErroresContador,
							p_out_ErroresDetalle 		=>  v_out_AuthErroresDetalle);		
					
			 p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AuthAdvertenciasContador;	
					  			 
			 p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
			                             From :v_out_AuthAdvertenciasDetalle Union All 
			                             Select "Codigo","RequiereAutorizacion", "Descripcion"
			                             From :p_out_AdvertenciasDetalle;  
					                                          
					                                          
			 p_out_ErroresContador := p_out_ErroresContador + v_out_AuthErroresContador;
					                                         
			 p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
			                        From :v_out_AuthErroresDetalle Union All 
			                        Select "Codigo", "Descripcion" 
			                        From :p_out_ErroresDetalle; 
				                                     
			  if 	p_out_ErroresContador > 0 then
						SIGNAL SALIDA_CONTROLADA;
						
					else
						--Mensaje de Salida
						p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;	
						v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
						CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
						p_out_AdvertenciasDetalle =  select 'REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
												     select * from :p_out_AdvertenciasDetalle;
						SIGNAL SALIDA_CONTROLADA;
			  end if;								
			              		                                     
			else	--Confirma la creacion y cambia el estado del documento

				
				
				-- Se realiza insert en tabla LogGenerarContratos para un posterior procesamiento y comprobacion de datos obligatorios que pudiera gatillar la regeneracion de sus documentos.				
				INSERT INTO "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
				("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
				SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'DOCUMENTO_IDENTIDAD_', 'Creado', :p_IdEmpresa, 0, :p_IdPersona, :p_IdUsuario, LEFT(:v_FechaIngreso,10) , 0, 'PEN', 'Cliente - 	CrearPersonaDocumentosIdentidad.sql'
				FROM dummy;
			
				v_IdEstadoAutorizacion:='ING';
				CALL "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarCrearPersonaDocumentosIdentidad"(
					p_IdEmpresa,
					p_IdUsuario,
					p_IdModulo,
					p_UsaReglasNegocio,
					v_IdDocumentoIdentidad,
					v_IdEstadoAutorizacion,
					v_out_AdvertenciasContador,
					v_out_AdvertenciasDetalle,
					v_out_ErroresContador,
					v_out_ErroresDetalle);

				 p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContador;	
					  			 
				 p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
				                             From :v_out_AdvertenciasDetalle Union All 
				                             Select "Codigo","RequiereAutorizacion", "Descripcion"
				                             From :p_out_AdvertenciasDetalle;  
				                                         
					                                          
				 p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador;
					                                         
				 p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
				                             From :v_out_ErroresDetalle Union All 
				                             Select "Codigo", "Descripcion" 
				                             From :p_out_ErroresDetalle; 
							
							
			end if;
	
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;