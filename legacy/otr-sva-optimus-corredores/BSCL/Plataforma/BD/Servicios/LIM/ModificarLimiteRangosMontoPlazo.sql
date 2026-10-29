SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::LIM.ModificarLimiteRangosMontoPlazo";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene y evalua informacion de los limites a modificar, 
--  para ser creado como una solicitud de Cambio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 24-05-2016
--  
"BSCL.Plataforma.BD.Servicios::LIM.ModificarLimiteRangosMontoPlazo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdRangosMontoPlazo		BIGINT,	
	IN  p_RegistroCambio TABLE("CampoModificado"	NVARCHAR(255), -- parametro estandar
									"ValorNuevo"	NVARCHAR(255)),-- parametro estandar
	
	-- Parametros salida del servicio publico
--	OUT p_out_IdSecuencias table ("IdSecuencia" BIGINT)
	OUT p_out_RefId					BIGINT,	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	int,
	OUT p_out_AdvertenciasDetalle	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador		int,
	OUT p_out_ErroresDetalle		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
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
		DECLARE v_tipoEntidad					NVARCHAR(40):='';
		DECLARE v_IdConcepto	       			NVARCHAR(6):='';
		DECLARE v_Evento      					BIGINT:=1;
		DECLARE v_TolalEventos					BIGINT:=0;	
		DECLARE v_IdEntidadOrigen				NVARCHAR(40):='';
		DECLARE v_IdEntidadBase					BIGINT:=0;
		DECLARE v_countEntidad					BIGINT:=0;
		DECLARE v_estadoAutorizacion			NVARCHAR(3):='';
		DECLARE v_RequiereAutorizacion			NVARCHAR(1):='';
		DECLARE v_secuencia_IdSolicitudCambio	BIGINT:=0;
		DECLARE v_out_IdSecuencias 				table ("IdSecuencia" BIGINT);
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
   
		DECLARE v_fechaHoraSistema 		NVARCHAR(40):='';
		DECLARE v_NombrePlantilla		NVARCHAR(100):='';
		DECLARE v_IdCliente		 	    bigint:=0;
		DECLARE v_NombreEmisor			NVARCHAR(100):='';
		DECLARE v_Count			 		bigint:=0;						
		DECLARE v_Tasa			 		bigint:=0;
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
		BEGIN
			-- validacion valores de entrada: PIB no deben ser menores a la Tasa
			SELECT COUNT(1) INTO v_Count
			FROM :p_RegistroCambio CAM1, :p_RegistroCambio CAM2
			WHERE CAM1."CampoModificado" = 'Tasa'
			  AND CAM2."CampoModificado" = 'PuntosTasaMenos'
			  AND CAM1."ValorNuevo" < CAM2."ValorNuevo";
			
			IF v_Count > 0 THEN
				p_out_ErroresDetalle = SELECT 'PIB_MENOS_NO_PERMITIDO' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			SELECT COUNT(1) INTO v_Count
			FROM :p_RegistroCambio CAM1, :p_RegistroCambio CAM2
			WHERE CAM1."CampoModificado" = 'Tasa'
			  AND CAM2."CampoModificado" = 'PuntosTasaMenosAgente'
			  AND CAM1."ValorNuevo" < CAM2."ValorNuevo";
			
			IF v_Count > 0 THEN
				p_out_ErroresDetalle = SELECT 'PIB_AGENTE_MENOS_NO_PERMITIDO' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			
			-- validacion PIB no deben ser menores a la Tasa que esta en la Tabla
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo" RAN, :p_RegistroCambio CAM
			WHERE "IdRangosMontoPlazo"=p_IdRangosMontoPlazo
			AND CAM."CampoModificado" = 'PuntosTasaMenos'
			AND RAN."Tasa" < CAM."ValorNuevo";
			
			IF v_Count > 0 THEN
				p_out_ErroresDetalle = SELECT 'PIB_MENOS_NO_PERMITIDO' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo" RAN, :p_RegistroCambio CAM
			WHERE "IdRangosMontoPlazo"=p_IdRangosMontoPlazo
			AND CAM."CampoModificado" = 'PuntosTasaMenosAgente'
			AND RAN."Tasa" < CAM."ValorNuevo";
			
			IF v_Count > 0 THEN
				p_out_ErroresDetalle = SELECT 'PIB_AGENTE_MENOS_NO_PERMITIDO' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			
			-- validacion cuando Activa un Rango que puede coincidir en montos y/o plazos con otro Rango
			SELECT COUNT(1) INTO v_Count FROM :p_RegistroCambio CAM WHERE CAM."CampoModificado" = 'IdEstado' AND CAM."ValorNuevo" = 'ACT';
			
			IF(v_Count > 0) THEN
				DECLARE v_IdLimite		BIGINT	:= 0;
				DECLARE v_MontoDesde	DECIMAL := 0;
				DECLARE v_MontoHasta	DECIMAL := 0;
				DECLARE v_PlazoDesde	INTEGER := 0;
				DECLARE v_PlazoHasta	INTEGER := 0;
   	
				SELECT "IdLimite", "MontoDesde", "MontoHasta", "PlazoDesde", "PlazoHasta" INTO v_IdLimite, v_MontoDesde, v_MontoHasta, v_PlazoDesde, v_PlazoHasta
				FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo" 
				WHERE "IdRangosMontoPlazo"=p_IdRangosMontoPlazo;
				
				--validacion entre rangos, no debe haber intersecciones entre ellos
				--Valida: valor nuevo existe entre rango antiguo
				SELECT COUNT(1) INTO v_Count
				FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo"
				WHERE "IdLimite" = v_IdLimite
				  AND "IdEstado" = 'ACT'
				  AND ( (v_MontoDesde between "MontoDesde" AND "MontoHasta") OR (v_MontoHasta between "MontoDesde" AND "MontoHasta") );
				 
				IF(v_Count > 0) THEN
					p_out_ErroresDetalle = select 'RANGO_MONTO_NO_PERMITIDO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA; 	
				END IF;
				
				SELECT COUNT(1) INTO v_Count
				FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo"
				WHERE "IdLimite" = v_IdLimite
				  AND "IdEstado" = 'ACT'
				  AND ( (v_PlazoDesde between "PlazoDesde" AND "PlazoHasta") OR (v_PlazoHasta between "PlazoDesde" AND "PlazoHasta") );
				 
				IF(v_Count > 0) THEN
					p_out_ErroresDetalle = select 'RANGO_PLAZO_NO_PERMITIDO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA; 	
				END IF;
				
				--Valida: valor antiguo existe entre rango nuevo
				SELECT COUNT(1) INTO v_Count
				FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo"
				WHERE "IdLimite" = v_IdLimite
				  AND "IdEstado" = 'ACT'
				  AND ( ("MontoDesde" between v_MontoDesde AND v_MontoHasta) OR ("MontoHasta" between v_MontoDesde AND v_MontoHasta) );
				 
				IF(v_Count > 0) THEN
					p_out_ErroresDetalle = select 'RANGO_MONTO_NO_PERMITIDO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA; 	
				END IF;
		
				SELECT COUNT(1) INTO v_Count
				FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo"
				WHERE "IdLimite" = v_IdLimite
				  AND "IdEstado" = 'ACT'
				  AND ( ("PlazoDesde" between v_PlazoDesde AND v_PlazoHasta) OR ("PlazoHasta" between v_PlazoDesde AND v_PlazoHasta) );
				 
				IF(v_Count > 0) THEN
					p_out_ErroresDetalle = select 'RANGO_PLAZO_NO_PERMITIDO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA; 	
				END IF;
			END IF;
			
			
			v_IdEntidadOrigen:=CAST (p_IdRangosMontoPlazo AS NVARCHAR(40));
			
			SELECT COUNT(1) INTO v_countEntidad 
			FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo" 
			WHERE "IdRangosMontoPlazo"=p_IdRangosMontoPlazo;
			
			IF(v_countEntidad=0) THEN
				p_out_ErroresDetalle =  select 'ENTIDAD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			
			SELECT COUNT(1) into v_Count
		    FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	   		INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
			WHERE EMP."IdEmpresa" = p_IdEmpresa
			AND CLI."IdEstado"='ACT';
			    		
			IF v_Count=0 THEN
				p_out_ErroresDetalle = SELECT 'UI_ENTIDAD_NO_ES_CUENTA_PROPIA' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
				SIGNAL SALIDA_CONTROLADA;
			ELSE
				SELECT CLI."IdPersona" INTO v_IdEntidadBase
				FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
				INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
				WHERE EMP."IdEmpresa" = p_IdEmpresa
				AND CLI."IdEstado"='ACT';
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
			v_tipoEntidad					:= 'LIMITERANGOSMONTOPLAZO';
			v_IdConcepto					:= 'MRLCTP';--Modificar Rangos Limite Cta Propia
			p_out_RefId						:=	0;
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			
			
			-- 1) OBTENCION DE VALORES SEGUN CAMPOS ENVIADOS DESDE UI
			-- (Se obtiene: Tipo de Dato y Valor Anterior del campo modificado
			
			SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
			
			V_RegistroSolicitudCambio =SELECT	
					v_secuencia_IdSolicitudCambio 	AS "IdSolicitudCambio",	
 				 	0 								AS "IdSecuenciaCambio", --- Se crea interno
					v_tipoEntidad					AS "IdTipoEntidad",	
					v_IdEntidadOrigen				AS "IdEntidadOrigen",
					MOD."CampoModificado" 			as "CampoModificado",
					MOD."DATA_TYPE_NAME" 			AS "TipoDato",
					(CASE MOD."COLUMN_NAME"	WHEN 'Tasa'					 THEN IFNULL(CAST (DOC."Tasa" AS NVARCHAR(255)),'')
											WHEN 'PuntosTasaMenos'		 THEN IFNULL(CAST (DOC."PuntosTasaMenos" AS NVARCHAR(255)),'')
											WHEN 'PuntosTasaMas'		 THEN IFNULL(CAST (DOC."PuntosTasaMas" AS NVARCHAR(255)),'')
											WHEN 'PuntosTasaMenosAgente' THEN IFNULL(CAST (DOC."PuntosTasaMenosAgente" AS NVARCHAR(255)),'')
											WHEN 'PuntosTasaMasAgente'	 THEN IFNULL(CAST (DOC."PuntosTasaMasAgente" AS NVARCHAR(255)),'')
											WHEN 'IdEstado'				 THEN IFNULL(CAST (DOC."IdEstado" AS NVARCHAR(255)),'')
					END) AS "ValorAntiguo",
					MOD."ValorNuevo" AS "ValorNuevo",
					CURRENT_UTCTIMESTAMP AS "FechaSolicitud",--SE CREAN INTERNOS
					CURRENT_UTCTIMESTAMP AS "FechaRevision",--SE CREAN INTERNOS
					v_estadoAutorizacion AS "IdEstadoAutorizacion"
			FROM "BSCL.Plataforma.BD::LIM.RangosMontoPlazo" DOC,
							(SELECT COL."DATA_TYPE_NAME",
									COL."COLUMN_NAME",
									CAM."CampoModificado",
									CAM."ValorNuevo"
							FROM SYS.TABLE_COLUMNS COL
							INNER JOIN :p_RegistroCambio CAM ON (COL."COLUMN_NAME"= CAM."CampoModificado")
							WHERE COL."TABLE_NAME"='BSCL.Plataforma.BD::LIM.RangosMontoPlazo' AND COL.SCHEMA_NAME='BSCL') MOD
			WHERE  DOC."IdRangosMontoPlazo" =p_IdRangosMontoPlazo;

				
			-- 2) ENVIO A SERVICIO CREACION SOLICITUD DE CAMBIO

			CALL  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio"
			(
				:V_RegistroSolicitudCambio,
				v_out_IdSecuencias
				
			);
			
							
			-- 3) DETERMINA SI LA MODIFICACION  REQUIERE DE UNA AUTORIZACION PREVIA
					
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

					
			if v_RequiereAutorizacion='S' then
				------------------------------------------
				--Genera Evento de Autorizacion
				------------------------------------------
				DECLARE	 v_Glosa		NVARCHAR(520);
				declare v_out_idAuth 		bigint;
				declare v_out_AuthAdvertenciasContador 	int;
				declare v_out_AuthErroresContador 		int;
				v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
				
				SELECT "DocumentoIdentidad" || ' / ' || "NombreCompleto"   into  v_Glosa 
				FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona" = v_IdEntidadBase;
						
				v_tablaEventos = SELECT
									v_IdConcepto AS "IdTipoEvento",              		 
								    p_IdModulo 	 AS "IdModuloSolicita",
								    p_IdUsuario	 AS "IdUsuarioSolicita",
								    TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') AS "Fecha",
								    '' AS "IdTipoOperacion",
								    v_IdConcepto AS	"IdConcepto",
								   v_IdEntidadOrigen AS "NumeroOperacionConcepto",
								    TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') AS "FechaOperacionConcepto",
								    0  AS "IdCliente",
								    0  AS "IdFuncionario",
								    0  AS "IdProveedor",
								    '' AS "IdTipoProducto",
									'' AS "IdClasificacionTipoInstrumento",
									'' AS "IdTipoInstrumento",
									0  AS "IdInstrumento",
									'' AS "Nemo",
								    0  AS "Cantidad",
								    0  AS "Monto",
								    0  AS "Precio",
								    0  AS "Tasa",
								    '' AS "IdMoneda",
								    v_Glosa AS "Glosa",
								    0 as "IdUnion" 
									FROM DUMMY;
			----------------------------------------------------------------------------------------------------------------							
	        v_tablaEventosDetalle = select  "IdEventoDetalle" as "IdUnion","NombreCampo","TipoDato","Valor","Clasificacion"
	   									from "BSCL.Plataforma.BD::AUT.EventoDetalle" 
	   									where "IdEventoDetalle" = 0;
	   		-----------------------------------------------------------------------------------------------------------------
				CALL"BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacionMasivo"(
						p_IdEmpresa => p_IdEmpresa,
						p_IdUsuario => p_IdUsuario,
						p_IdModulo => p_IdModulo,
						p_UsaReglasNegocio=> :p_UsaReglasNegocio,
					    p_simularOperacion=> 'N',
						p_tablaEventos => :v_tablaEventos,	
						p_tablaDetalle	=> :v_tablaEventosDetalle,
						--salida
						p_out_IdEvento => v_out_idAuthtable,
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
	            
	            if p_out_ErroresContador > 0 then
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
			v_estadoAutorizacion 			:= 'ING';	
			v_tablaIdLimite = SELECT p_IdRangosMontoPlazo AS "IdRangosMontoPlazo" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::LIM.ConfirmarModificarLimiteRangosMontoPlazo"(
					p_IdEmpresa,
					p_IdUsuario,
					p_IdModulo,
					p_UsaReglasNegocio,
					:v_tablaIdLimite,
					v_estadoAutorizacion,
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
	            
	            if p_out_ErroresContador > 0 then
	            	SIGNAL SALIDA_CONTROLADA;
	            end if;
				  
			end if;
			
			p_out_RefId:=v_secuencia_IdSolicitudCambio;
	
		END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;