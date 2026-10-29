SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaDatosGenerales";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene y evalua informacion de la persona a modificar, para ser creado 
--  como una solicitud de Cambio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 09-12-2015
--  
--  Proposito: Se incorporan definiciones Optimus Perú
--  Autor: JGARCES - BCS
--  Fecha Modificacion: 14/03/2017
--
"BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaDatosGenerales" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdPersona									BIGINT,	
	IN  p_RegistroCambio		  TABLE("CampoModificado"		NVARCHAR(255),
										"ValorNuevo"			NVARCHAR(255)),
	
	
	-- Parametros salida del servicio publico
	OUT p_out_RefId 					BIGINT,
		
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  			nvarchar(520);	        	
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
			DECLARE v_tipoEntidad									NVARCHAR(40);
			DECLARE v_IdConcepto	       							NVARCHAR(6);			
			DECLARE v_countPersona				 					BIGINT;
			DECLARE v_count   					 					BIGINT;
			DECLARE v_estadoAutorizacion							NVARCHAR(3);
			DECLARE v_RequiereAutorizacion							NVARCHAR(1);
			DECLARE v_secuencia_IdSolicitudCambio					BIGINT;
			DECLARE v_out_IdSecuencias table ("IdSecuencia" BIGINT);
			
			
		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			DECLARE v_fechaHoraSistema NVARCHAR(40);
			
			--------------------
		--Validaciones
		--------------------
		
		-- Existencia 		
		BEGIN
			
			SELECT COUNT("IdPersona") INTO v_countPersona FROM "BSCL.SocioNegocio.BD::PER.Persona" WHERE "IdPersona"=p_IdPersona;
			IF(v_countPersona=0) THEN
				p_out_ErroresDetalle =  select 'PERSONA_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
		END;		
		
		--Verifica y advierte para el caso en que se seleccione un Empleador y la persona ya sea Empleado de la Corredora.
		BEGIN
			
			SELECT COUNT("ValorNuevo")	INTO v_count  FROM :p_RegistroCambio WHERE "CampoModificado"='IdPersonaEmpleador' AND LENGTH("ValorNuevo")>0;
			IF(v_count>0) THEN
				v_count:=0;
				
				SELECT COUNT("IdPersona") INTO v_count FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"
						 WHERE "IdPersona"=p_IdPersona;-- AND "IdEstado"='ACT' ;
						 
				IF(v_count>0) THEN --Establece advertencia
						p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;	
					    v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
						CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('TIENE_EMPLEADOR_RELACIONADO',:v_hash,:v_erroroutmsg);
						p_out_AdvertenciasDetalle =  select 'TIENE_EMPLEADOR_RELACIONADO' as "Codigo",'' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
					    select * from :p_out_AdvertenciasDetalle;
				END IF;
			END IF;
		
		END;	
		
		
		
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			--Inicializacion de variables:
			v_secuencia_IdSolicitudCambio 	:= -1;
			
			v_estadoAutorizacion 			:= 'PEN';
			v_tipoEntidad					:= 'PERSONA';
			v_IdConcepto					:= 'MODPER';
			p_out_RefId						:=0;
			
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);			
			-- 1) OBTENCION DE VALORES SEGUN CAMPOS ENVIADOS DESDE UI
			-- (Se obtiene: Tipo de Dato y Valor Anterior del campo modificado
			
			--SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSecuenciaCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
			SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
			
						
			
			V_RegistroSolicitudCambio =SELECT	
					v_secuencia_IdSolicitudCambio AS "IdSolicitudCambio",	
					 0 AS		"IdSecuenciaCambio", --- Se crea interno
					v_tipoEntidad	AS "IdTipoEntidad",	
					p_IdPersona		AS "IdEntidadOrigen",
					MOD."CampoModificado" as "CampoModificado",
					MOD."DATA_TYPE_NAME" AS "TipoDato",
					(CASE MOD."COLUMN_NAME" WHEN 'Tipo'							THEN IFNULL(CAST (PER."Tipo" AS NVARCHAR(255)),'')
											WHEN 'Nombres'						THEN IFNULL(CAST (PER."Nombres" AS NVARCHAR(255)),'')
											WHEN 'ApellidoPaterno'				THEN IFNULL(CAST (PER."ApellidoPaterno" AS NVARCHAR(255)),'')
											WHEN 'ApellidoMaterno'				THEN IFNULL(CAST (PER."ApellidoMaterno" AS NVARCHAR(255)),'')
											WHEN 'RazonSocial'					THEN IFNULL(CAST (PER."RazonSocial" AS NVARCHAR(255)),'')
											WHEN 'IdGiroPrincipal'				THEN IFNULL(CAST (PER."IdGiroPrincipal" AS NVARCHAR(255)),'')
											WHEN 'IdPaisNacimiento'				THEN IFNULL(CAST (PER."IdPaisNacimiento" AS NVARCHAR(255)),'')
											WHEN 'IdPaisResidencia'				THEN IFNULL(CAST (PER."IdPaisResidencia" AS NVARCHAR(255)),'')
											WHEN 'FechaNacimiento'				THEN IFNULL(CAST (PER."FechaNacimiento" AS NVARCHAR(255)),'')
											WHEN 'FechaDefuncion'				THEN IFNULL(CAST (PER."FechaDefuncion" AS NVARCHAR(255)),'')
											WHEN 'Sexo'							THEN IFNULL(CAST (PER."Sexo" AS NVARCHAR(255)),'')
											WHEN 'EsExtranjero'					THEN IFNULL(CAST (PER."EsExtranjero" AS NVARCHAR(255)),'')
											WHEN 'EstadoCivil'					THEN IFNULL(CAST (PER."EstadoCivil" AS NVARCHAR(255)),'')
											WHEN 'RegimenConyugal'				THEN IFNULL(CAST (PER."RegimenConyugal" AS NVARCHAR(255)),'')
											--WHEN 'Cargo'						THEN IFNULL(CAST (PER."Cargo" AS NVARCHAR(255)),'')
											--WHEN 'Empleador'					THEN IFNULL(CAST (PER."Empleador" AS NVARCHAR(255)),'')											
											WHEN 'IdProfesion'					THEN IFNULL(CAST (PER."IdProfesion" AS NVARCHAR(255)),'')
											WHEN 'FechaInicioActividad'			THEN IFNULL(CAST (PER."FechaInicioActividad" AS NVARCHAR(255)),'')
											WHEN 'Alias'						THEN IFNULL(CAST (PER."Alias" AS NVARCHAR(255)),'')
											WHEN 'IdPersonaConyuge'				THEN IFNULL(CAST (PER."IdPersonaConyuge" AS NVARCHAR(255)),'')
											WHEN 'IdPersonaRepresentante'		THEN IFNULL(CAST (PER."IdPersonaRepresentante" AS NVARCHAR(255)),'')
											WHEN 'IdPaisNacionalidadPrincipal'	THEN IFNULL(CAST (PER."IdPaisNacionalidadPrincipal" AS NVARCHAR(255)),'')
											WHEN 'GlobalLegalEntity'			THEN IFNULL(CAST (PER."GlobalLegalEntity" AS NVARCHAR(255)),'')
											WHEN 'IdPersonaEmpleador'			THEN IFNULL(CAST (PER."IdPersonaEmpleador" AS NVARCHAR(255)),'')
											WHEN 'CodigoTipoCargoEmpleado'		THEN IFNULL(CAST (PER."CodigoTipoCargoEmpleado" AS NVARCHAR(255)),'')
											WHEN 'DetalleCargo'					THEN IFNULL(CAST (PER."DetalleCargo" AS NVARCHAR(255)),'')
											WHEN 'IdNombreFantasiaPrincipal'	THEN IFNULL(CAST (PER."IdNombreFantasiaPrincipal" AS NVARCHAR(255)),'')
											WHEN 'IdRangoPatrimonio'			THEN IFNULL(CAST (PER."IdRangoPatrimonio" AS NVARCHAR(255)),'')
											-- PERU
											WHEN 'IdGradoInstruccion'			THEN IFNULL(CAST (PER."IdGradoInstruccion" AS NVARCHAR(255)),'')
											WHEN 'AgnosServicio'				THEN IFNULL(CAST (PER."AgnosServicio" AS NVARCHAR(255)),'')
											WHEN 'IngresoPromedioMensual'		THEN IFNULL(CAST (PER."IngresoPromedioMensual" AS NVARCHAR(255)),'')
											WHEN 'IdGiroCIIU'					THEN IFNULL(CAST (PER."IdGiroCIIU" AS NVARCHAR(255)),'')
											WHEN 'IdPaisResidenciaSecundaria'	THEN IFNULL(CAST (PER."IdPaisResidenciaSecundaria" AS NVARCHAR(255)),'')
											WHEN 'IdPaisNacionalidadSecundaria'	THEN IFNULL(CAST (PER."IdPaisNacionalidadSecundaria" AS NVARCHAR(255)),'')
					END) AS "ValorAntiguo",
					MOD."ValorNuevo" AS "ValorNuevo",
					CURRENT_UTCTIMESTAMP AS "FechaSolicitud",--SE CREAN INTERNOS
					CURRENT_UTCTIMESTAMP AS "FechaRevision",--SE CREAN INTERNOS
					v_estadoAutorizacion AS "IdEstadoAutorizacion"
			FROM "BSCL.SocioNegocio.BD::PER.Persona" PER,
							(SELECT COL."DATA_TYPE_NAME",
									COL."COLUMN_NAME",
									CAM."CampoModificado",
									CAM."ValorNuevo"
							FROM SYS.TABLE_COLUMNS COL
							INNER JOIN :p_RegistroCambio CAM ON (COL."COLUMN_NAME"= CAM."CampoModificado")
							WHERE COL."TABLE_NAME"='BSCL.SocioNegocio.BD::PER.Persona' AND COL.SCHEMA_NAME='BSCL') MOD
			WHERE PER."IdPersona"=	p_IdPersona;

		
			--select "DATA_TYPE_NAME","LENGTH","COLUMN_NAME",* from SYS.TABLE_COLUMNS WHERE "TABLE_NAME"='BSCL.SocioNegocio.BD::CLI.Cliente'
							
			-- 2) ENVIO A SERVICIO CREACION SOLICITUD DE CAMBIO

			CALL  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio"
			(
				:V_RegistroSolicitudCambio,
				v_out_IdSecuencias
				
			);
											
						
							
			-- 2) DETERMINA SI LA MODIFICACION  REQUIERE DE UNA AUTORIZACION PREVIA
					
			------------------------------------------
			--Pregunta Si requiere autorizacion
			------------------------------------------
			call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:V_RegistroSolicitudCambio,
				:v_IdConcepto,
				:v_RequiereAutorizacion
			);

			/*
			call "BSCL.SocioNegocio.BD.Servicios::PER.RequiereAutorizacionPersonaDatosGenerales" (
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
			DECLARE	 v_Glosa		NVARCHAR(255);
			declare v_out_idAuth 		bigint;
			declare v_out_AuthAdvertenciasContador 	int;
			declare v_out_AuthErroresContador 		int;
			v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
			
			SELECT "DocumentoIdentidad" || ' / ' || "NombreCompleto"  into  v_Glosa 
			FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona" = p_IdPersona;	
						
			CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
				p_IdEmpresa 				=> p_IdEmpresa,
				p_IdUsuario 				=> p_IdUsuario,
				p_IdModulo 					=> p_IdModulo,
				p_UsaReglasNegocio 			=> 'N',								
				p_IdTipoEvento 				=> v_IdConcepto,
				p_IdModuloSolicita 			=> p_IdModulo,
				p_IdUsuarioSolicita 		=> p_IdUsuario,
				p_Fecha 					=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'), 
				p_IdConcepto 				=>  v_IdConcepto,
				p_NumeroOperacionConcepto 	=> CAST (p_IdPersona AS NVARCHAR(40)) , --v_secuencia_IdSolicitudCambio,
				p_FechaOperacionConcepto 	=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'),
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
			
				
                                         
				
		else
		
			------------------------------------------
			-- Se confirma la modificacion y se realizan los cambios
			------------------------------------------
				
				call  "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarPersonaDatosGenerales" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:p_UsaReglasNegocio,
				:p_IdPersona,
				'ING',
				V_out_AdvertenciasContador,
				v_out_AdvertenciasDetalle,	
				V_out_ErroresContador,
				V_out_ErroresDetalle
				);
				
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
                                     
                                     
			  if 	p_out_ErroresContador > 0 then
						SIGNAL SALIDA_CONTROLADA;
			  end if;								
		                                
		p_out_RefId:=v_secuencia_IdSolicitudCambio;				
		end if;
		
		


	END;
					
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;