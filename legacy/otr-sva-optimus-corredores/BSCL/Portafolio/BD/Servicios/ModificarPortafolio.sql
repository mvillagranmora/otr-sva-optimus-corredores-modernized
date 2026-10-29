SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::ModificarPortafolio";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene y evalua informacion del portafolio a modificar, 
--  para ser creado como una solicitud de Cambio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 21-03-2016
--  
"BSCL.Portafolio.BD.Servicios::ModificarPortafolio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdCliente									BIGINT DEFAULT 0,			--ID del Cliente al que se le realiza el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdPortafolio						   	    BIGINT,	
	IN  p_IdCanal									NVARCHAR(3)  DEFAULT 'OPT',

	IN  p_RegistroCambio		  TABLE("CampoModificado"		NVARCHAR(255), -- parametro estandar
											"ValorNuevo"			NVARCHAR(255)),-- parametro estandar
	
	-- Parametros salida del servicio publico
--	OUT p_out_IdSecuencias table ("IdSecuencia" BIGINT)
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
		DECLARE v_Evento      									BIGINT:=1;
		DECLARE v_TolalEventos						      		BIGINT:=0;	
		DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
		DECLARE v_IdEntidadBase									BIGINT:=0;
		DECLARE v_CodigoPortafolio								NVARCHAR(100);
		DECLARE v_countEntidad			   					    BIGINT;
		DECLARE v_estadoAutorizacion							NVARCHAR(3);
		DECLARE v_RequiereAutorizacion							NVARCHAR(1);
		DECLARE v_secuencia_IdSolicitudCambio					BIGINT;
		DECLARE v_out_IdSecuencias 								table ("IdSecuencia" BIGINT);
		
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
   
		DECLARE v_fechaHoraSistema NVARCHAR(40);
		
		DECLARE v_IdEntidad				BIGINT:= :p_IdPortafolio;
		DECLARE v_TipoEntidadReplica	NVARCHAR(50):= 'PORTAFOLIO';
		DECLARE v_TipoOperacion			NVARCHAR(1):= 'U';
		DECLARE v_countIdPortafolio 	INT:=0;

					
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia 		
		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdPortafolio AS NVARCHAR(40));
			
			SELECT COUNT(1) INTO v_countEntidad FROM "BSCL.Portafolio.BD::POR.Portafolio" WHERE "IdPortafolio"=p_IdPortafolio;
			
			IF(v_countEntidad=0) THEN
				p_out_ErroresDetalle =  select 'ENTIDAD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
				ELSE
					SELECT "IdCliente","CodigoPortafolio" INTO v_IdEntidadBase, v_CodigoPortafolio FROM "BSCL.Portafolio.BD::POR.Portafolio" WHERE "IdPortafolio"=p_IdPortafolio;
			END IF;
										
		END;
		
		
		-- Verificar que no exista otro Portafolio del mismo Cliente, con una Plantilla Limites marcada Por Defecto
		BEGIN
			-- Si el ValorNuevo de la Plantilla Limites Base es una plantilla Por Defecto
			SELECT COUNT(1) INTO v_countEntidad
			FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarPlantillaLimites" PL
			INNER JOIN :p_RegistroCambio CAM ON (CAM."CampoModificado"='IdPerfilLimites' AND PL."IdPlantillaLimite"=CAM."ValorNuevo")
			WHERE PL."PorDefecto" = 'S';
			
			
			IF(v_countEntidad > 0) THEN
				-- Si Cliente ya tiene en uno de sus Portafolios una plantilla limites Por Defecto
				SELECT COUNT(1) INTO v_countEntidad
				FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla"
				WHERE "IdCliente" = v_IdEntidadBase
				  AND "IdPortafolio" <> p_IdPortafolio
				  AND "PorDefecto" = 'S';
				
				IF(v_countEntidad > 0) THEN
					p_out_ErroresDetalle =  select 'CLIENTE_YA_TIENE_PLANTILLA_LIMITE_POR_DEFECTO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
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
			v_tipoEntidad					:= 'PORTAFOLIO';
			v_IdConcepto					:= 'MODPOR';
			p_out_RefId						:=	0;
			
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);		
			-- 1) OBTENCION DE VALORES SEGUN CAMPOS ENVIADOS DESDE UI
			-- (Se obtiene: Tipo de Dato y Valor Anterior del campo modificado
			
			
			SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
		
				
			
			V_RegistroSolicitudCambio =SELECT	
					v_secuencia_IdSolicitudCambio 	AS "IdSolicitudCambio",	
 				 	0 								AS "IdSecuenciaCambio", --- Se crea interno
					v_tipoEntidad					AS "IdTipoEntidad",	
					v_IdEntidadOrigen				AS "IdEntidadOrigen",
					MOD."CampoModificado" 			as "CampoModificado",
					MOD."DATA_TYPE_NAME" 			AS "TipoDato",
					(CASE MOD."COLUMN_NAME" WHEN 'CodigoPortafolio'				THEN IFNULL(CAST (DOC."CodigoPortafolio" AS NVARCHAR(255)) ,'')
											WHEN 'DescripcionPortafolio'		THEN IFNULL(CAST (DOC."DescripcionPortafolio" AS NVARCHAR(255)) ,'')
											WHEN 'Proposito'					THEN IFNULL(CAST (DOC."Proposito" AS NVARCHAR(255)) ,'')
											WHEN 'Alias'						THEN IFNULL(CAST (DOC."Alias" AS NVARCHAR(255)) ,'')
											WHEN 'IdPersonaBrokerExtranjero'	THEN IFNULL(CAST (DOC."IdPersonaBrokerExtranjero" AS NVARCHAR(255)) ,'')
											WHEN 'IdPerfilComercialBaseOrigen'	THEN IFNULL(CAST (DOC."IdPerfilComercialBaseOrigen" AS NVARCHAR(255)) ,'')
											WHEN 'IdPerfilProductos'			THEN IFNULL(CAST (DOC."IdPerfilProductos" AS NVARCHAR(255)) ,'')
											WHEN 'IdPerfilProductosExtra'		THEN IFNULL(CAST (DOC."IdPerfilProductosExtra" AS NVARCHAR(255)) ,'')
											WHEN 'IdPerfilCobros'				THEN IFNULL(CAST (DOC."IdPerfilCobros" AS NVARCHAR(255)) ,'')
											WHEN 'IdPerfilCobrosExtra'			THEN IFNULL(CAST (DOC."IdPerfilCobrosExtra" AS NVARCHAR(255)) ,'')																						
											WHEN 'IdPerfilCobrosPeru'				THEN IFNULL(CAST (DOC."IdPerfilCobrosPeru" AS NVARCHAR(255)) ,'')
											WHEN 'IdPerfilCobrosPeruExtra'			THEN IFNULL(CAST (DOC."IdPerfilCobrosPeruExtra" AS NVARCHAR(255)) ,'')																						
											WHEN 'IdPerfilLimites'				THEN IFNULL(CAST (DOC."IdPerfilLimites" AS NVARCHAR(255)) ,'')
											WHEN 'IdPerfilLimitesExtra'			THEN IFNULL(CAST (DOC."IdPerfilLimitesExtra" AS NVARCHAR(255)) ,'')
											WHEN 'IdPerfilCondicionesOperativas'		THEN IFNULL(CAST (DOC."IdPerfilCondicionesOperativas" AS NVARCHAR(255)) ,'')											
											WHEN 'IdPerfilCondicionesOperativasExtra'	THEN IFNULL(CAST (DOC."IdPerfilCondicionesOperativasExtra" AS NVARCHAR(255)) ,'')		
											WHEN 'IdDireccionEnvioCorrespondencia'		THEN IFNULL(CAST (DOC."IdDireccionEnvioCorrespondencia" AS NVARCHAR(255)) ,'')
											WHEN 'IdTelefonoContacto'					THEN IFNULL(CAST (DOC."IdTelefonoContacto" AS NVARCHAR(255)) ,'')
											WHEN 'IdEmpleadoEjecutivo'					THEN IFNULL(CAST (DOC."IdEmpleadoEjecutivo" AS NVARCHAR(255)) ,'')
											WHEN 'NumeroFicha'					THEN IFNULL(CAST (DOC."NumeroFicha" AS NVARCHAR(255)) ,'')
											WHEN 'IdNombreFantasia'				THEN IFNULL(CAST (DOC."IdNombreFantasia" AS NVARCHAR(255)) ,'')
											WHEN 'AreaCliente'					THEN IFNULL(CAST (DOC."AreaCliente" AS NVARCHAR(255)) ,'')
											WHEN 'IdEmpleadoEjecutivoComercial'					THEN IFNULL(CAST (DOC."IdEmpleadoEjecutivoComercial" AS NVARCHAR(255)) ,'')
					END) AS "ValorAntiguo",
					MOD."ValorNuevo" AS "ValorNuevo",
					CURRENT_UTCTIMESTAMP AS "FechaSolicitud",--SE CREAN INTERNOS
					CURRENT_UTCTIMESTAMP AS "FechaRevision",--SE CREAN INTERNOS
					v_estadoAutorizacion AS "IdEstadoAutorizacion"
			FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC,
							(SELECT COL."DATA_TYPE_NAME",
									COL."COLUMN_NAME",
									CAM."CampoModificado",
									CAM."ValorNuevo"
							FROM SYS.TABLE_COLUMNS COL
							INNER JOIN :p_RegistroCambio CAM ON (COL."COLUMN_NAME"= CAM."CampoModificado")
							WHERE COL."TABLE_NAME"='BSCL.Portafolio.BD::POR.Portafolio' AND COL.SCHEMA_NAME='BSCL') MOD
			WHERE  DOC."IdPortafolio" =v_IdEntidadOrigen;

				
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
			V_RegistroSolicitudCambio = SELECT  * FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" WHERE 1=2;
			
			call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
			:p_IdEmpresa,
			:p_IdUsuario,
			:p_IdModulo,
			:V_RegistroSolicitudCambio,
			:v_IdConcepto,
			:v_RequiereAutorizacion
			);
			
				
		if v_RequiereAutorizacion='S' then
		------------------------------------------
		--Genera Evento de Autorizacion
		------------------------------------------
			DECLARE	 v_Glosa		NVARCHAR(255);
			declare v_out_idAuth 		bigint;
			declare v_out_AuthAdvertenciasContador 	int;
			declare v_out_AuthErroresContador 		int;
			v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
			
			--SELECT CAST("NombreCompleto" AS NVARCHAR(255))  into  v_Glosa from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" WHERE "IdCliente"=v_IdEntidadBase;
			
				select 
				CASE "Tipo" 
					WHEN 'J' THEN "DocumentoIdentidad" || ' / ' || "RazonSocial" || ' / CodPort:' || :v_CodigoPortafolio 
					ELSE "DocumentoIdentidad" || ' / ' || "Nombres" || ' ' || "ApellidoPaterno" || ' ' || "ApellidoMaterno" || ' / CodPort:' || :v_CodigoPortafolio 
				END AS "Nom" INTO v_Glosa 
				from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" WHERE "IdCliente"=v_IdEntidadBase;
			
					CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
						p_IdEmpresa => p_IdEmpresa,
						p_IdUsuario => p_IdUsuario,
						p_IdModulo => p_IdModulo,
						p_UsaReglasNegocio => 'N',								
						p_IdTipoEvento => v_IdConcepto,--'MODDOC',
						p_IdModuloSolicita => p_IdModulo,
						p_IdUsuarioSolicita => p_IdUsuario,
						p_Fecha => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'), 
						p_IdConcepto => v_IdConcepto,
						p_NumeroOperacionConcepto =>  v_IdEntidadOrigen,--v_secuencia_IdSolicitudCambio,
						p_FechaOperacionConcepto => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'),
						p_IdCliente => v_IdEntidadBase, --v_IdCliente,
						p_Glosa => v_Glosa,
						--salida
						p_out_IdEvento => v_out_idAuth,
						p_out_AdvertenciasContador => v_out_AuthAdvertenciasContador,
						p_out_AdvertenciasDetalle =>  v_out_AuthAdvertenciasDetalle,
						p_out_ErroresContador =>  v_out_AuthErroresContador,
						p_out_ErroresDetalle =>  v_out_AuthErroresDetalle);		
			
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
		
					-- Se realiza insert en tabla LogGenerarContratos para un posterior procesamiento y comprobacion de datos obligatorios que pudiera gatillar la regeneracion de sus documentos.
					INSERT INTO BSCL."BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
					("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
					SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'PORTAFOLIO_', "CampoModificado", :p_IdEmpresa, :p_IdCliente, 0, :p_IdUsuario, LEFT(:v_fechaHoraSistema,10) , 0, 'PEN', 'Cliente - ModificarPortafolio.sql'
					FROM :p_RegistroCambio;
			------------------------------------------
			-- Se confirma la modificacion y se realizan los cambios
			------------------------------------------
		
			call  "BSCL.Portafolio.BD.Servicios::ConfirmarModificarPortafolio" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:p_UsaReglasNegocio,
				:v_IdEntidadOrigen,
				'ING',
				:p_IdCanal,
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
			  
			--PRONETIT			
			
			SELECT COUNT("IdPortafolio") INTO v_countIdPortafolio
			FROM "BSCL.Portafolio.BD::POR.Portafolio"
			WHERE "IdPortafolio" = :p_IdPortafolio
			AND "Proposito" = 'PROPOSITOAPV';
			
			IF(v_countIdPortafolio > 0)THEN
				CALL "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV" (
					:p_IdEmpresa,
					:p_IdUsuario,
					:p_IdModulo,
					:p_UsaReglasNegocio,
					:p_IdPortafolio, 
					v_IdEntidad,
					v_TipoEntidadReplica, 
					v_TipoOperacion,
					v_out_AdvertenciasContador,
					v_out_AdvertenciasDetalle,
					v_out_ErroresContador,
					v_out_ErroresDetalle
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
	                
	            IF	p_out_ErroresContador > 0 then
					SIGNAL SALIDA_CONTROLADA;
			  	END if;
            END IF;                   	
		END if;
		
		
		p_out_RefId:=v_secuencia_IdSolicitudCambio;

	END;
					
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
