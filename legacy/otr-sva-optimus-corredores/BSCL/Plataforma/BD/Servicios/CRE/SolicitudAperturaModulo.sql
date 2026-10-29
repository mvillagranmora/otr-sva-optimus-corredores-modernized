SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::CRE.SolicitudAperturaModulo";

CREATE PROCEDURE 
-- 
--  Proposito: Servicio que solicita Apertura de Modulo, Si el estado de solicitud es Aprobado se Abre el Modulo. 
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 15-03-2016
--  
"BSCL.Plataforma.BD.Servicios::CRE.SolicitudAperturaModulo"(
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_simularOperacion       					NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos) 
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdCREModuloEmpresa						BIGINT,	
	IN  p_IdEstado									NVARCHAR(3),
	IN  p_HoraApertura								NVARCHAR(40),
	IN  p_FechaProceso 								SECONDDATE,
	IN  p_TipoEstado								CHAR(1),
	
	-- Parametros salida del servicio publico
	OUT p_out_RefId 					BIGINT,
		
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador int,
	OUT p_out_InformacionDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(520))
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
	DECLARE v_Errores "BSCL.Orden.BD.Tipos::SalidaDetalle";
	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA

	BEGIN
		--salida/error/advertencia controlado por este servicio
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle			=> :v_Errores,
	
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
			 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select 0 AS "IdInterno" ,0 AS "IdOperacion", v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_InformacionContador	:= 0;
	p_out_InformacionDetalle	= select 0 AS "IdInterno" ,0 AS "IdOperacion", '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_AdvertenciasDetalle	= select 0 AS "IdInterno" ,0 AS "IdOperacion", '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select 0 AS "IdInterno" ,0 AS "IdOperacion", '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'' AS "Resultado",'' AS "Codigo",'' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" FROM DUMMY WHERE 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
			DECLARE v_tipoEntidad									NVARCHAR(40);
			DECLARE v_IdTipoEvento									NVARCHAR(6);
			DECLARE v_IdConcepto	       							NVARCHAR(6);									
			DECLARE v_countCREModuloEmpresa				 					BIGINT;
			DECLARE v_estadoAutorizacion							NVARCHAR(3);
			--DECLARE v_RequiereAutorizacion							NVARCHAR(1);
			DECLARE v_secuencia_IdSolicitudCambio					BIGINT;
			--DECLARE v_out_IdSecuencias table ("IdSecuencia" BIGINT);
			
			
		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			DECLARE v_fechaHoraSistema NVARCHAR(40);
			
			--------------------
		--Validaciones
		--------------------
		
		-- Existencia 		
		BEGIN
			
			SELECT COUNT("IdCREModuloEmpresa") INTO v_countCREModuloEmpresa FROM "BSCL.Plataforma.BD::CRE.ModuloEmpresa" WHERE "IdCREModuloEmpresa"=p_IdCREModuloEmpresa;
			IF(v_countCREModuloEmpresa=0) THEN
				v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'RECHAZO' AS "Resultado", 'MODULOEMPRESA_NO_EXISTE' AS "Codigo", '' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" FROM DUMMY;	
			END IF;
			
			/*Validar que los procesos dependientes hayan sido ejecutados*/
			BEGIN
				DECLARE v_fechaSistema NVARCHAR(10);
					
				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(
					p_IdEmpresa => p_IdEmpresa, 
					p_out_FechaSistema => v_fechaSistema
				);
				
				CALL "BSCL.Plataforma.BD.Servicios::CRE.ComprobarRequisitosAperturaCierre"(p_IdEmpresa, p_IdUsuario, p_IdModulo, p_IdCREModuloEmpresa, 'APEMOD', v_fechaSistema, :v_Errores);
			END;
			
			/*Validar que el modulo se encuentre cerrado, y no en estado pendiente*/
			BEGIN
				/* Modulo esta Cerrado*/
				IF (p_TipoEstado = 'C') THEN
					/* Volver a solicitar Apertura de Modulo*/
					IF (p_IdEstado = 'PEN') THEN
						v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'RECHAZO' AS "Resultado", 'PENDIENTE_AUTORIZACION' AS "Codigo", '' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" FROM DUMMY 
						UNION ALL SELECT * FROM :v_Errores;
						SIGNAL SALIDA_CONTROLADA; 
					/* Error Modulo ya se encuentra Abierto (no deberia pasar por acá)*/
					ELSEIF (p_IdEstado = 'ACT') THEN
						v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'RECHAZO' AS "Resultado", 'MODULO_ABIERTO' AS "Codigo", '' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" FROM DUMMY 
						UNION ALL SELECT * FROM :v_Errores;
						SIGNAL SALIDA_CONTROLADA; 					
					END IF;
				/* Error Modulo ya se encuentra ABIERTO*/
				ELSEIF (p_TipoEstado = 'A') THEN
					v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'RECHAZO' AS "Resultado", 'MODULO_ABIERTO' AS "Codigo", '' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" FROM DUMMY 
					UNION ALL SELECT * FROM :v_Errores; 
					SIGNAL SALIDA_CONTROLADA; 
				END IF;
			END;
						
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :v_Errores; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;	
			
			
		END;
		
		BEGIN
			/*Validar que los procesos dependientes hayan sido ejecutados*/
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
			v_tipoEntidad					:= 'APERTURA-MODULO';
			v_IdTipoEvento					:= 'APEMOD';
			v_IdConcepto					:= 'APERMO';			
			p_out_RefId						:=0;
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);			
			
			-- 1) OBTENCION DE VALORES SEGUN CAMPOS ENVIADOS DESDE UI
			-- (Se obtiene: Tipo de Dato y Valor Anterior del campo modificado
			
			--SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSecuenciaCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
			SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
			
			/* Modulo Cerrado, Solicitar Apertura*/
			IF (p_TipoEstado = 'C') THEN
				/* Volver a solicitar Apertura de Modulo*/
				IF (p_IdEstado = 'RZD' OR  p_IdEstado = 'CER') THEN 
					V_RegistroSolicitudCambio = SELECT
							v_secuencia_IdSolicitudCambio AS "IdSolicitudCambio",
							0 AS		"IdSecuenciaCambio", --- Se crea interno
							v_tipoEntidad	AS "IdTipoEntidad",
							CAST (p_IdCREModuloEmpresa AS NVARCHAR(40))		AS "IdEntidadOrigen",
							MOD."COLUMN_NAME" 		AS "CampoModificado",
							MOD."DATA_TYPE_NAME" 	AS "TipoDato",
							(CASE MOD."COLUMN_NAME" WHEN 'IdEstado'	THEN IFNULL(CAST (CRE."IdEstado" AS NVARCHAR(255)),'') END) AS "ValorAntiguo",
							(CASE MOD."COLUMN_NAME" WHEN 'IdEstado'	THEN 'PEN'	END)  AS "ValorNuevo",
							CURRENT_UTCTIMESTAMP AS "FechaSolicitud",--SE CREAN INTERNOS
							CURRENT_UTCTIMESTAMP AS "FechaRevision",--SE CREAN INTERNOS
							v_estadoAutorizacion AS "IdEstadoAutorizacion"
						FROM "BSCL.Plataforma.BD::CRE.ModuloEmpresa" CRE,
						(SELECT COL."DATA_TYPE_NAME", COL."COLUMN_NAME"
							FROM SYS.TABLE_COLUMNS COL
							WHERE COL."TABLE_NAME"='BSCL.Plataforma.BD::CRE.ModuloEmpresa' AND COL.SCHEMA_NAME='BSCL'
							AND COL."COLUMN_NAME"IN ('IdEstado')) MOD
						WHERE CRE."IdCREModuloEmpresa" = p_IdCREModuloEmpresa;
						
				/* Cambiar Estado a ACT y [A]. - Actualizar Horas.*/
				ELSEIF (p_IdEstado = 'AUT') THEN
					UPDATE "BSCL.Plataforma.BD::CRE.ModuloEmpresa"
					SET "IdEstado" = 'ACT', "TipoEstado" = 'A', "HoraApertura" = TO_VARCHAR(p_FechaProceso, 'HH24:MI:SS')
					WHERE "IdCREModuloEmpresa" = p_IdCREModuloEmpresa;
					/* Agregar LOG */
					
					SIGNAL SALIDA_CONTROLADA; 
				END IF;
			END IF;
			
			-- 2) ENVIO A SERVICIO CREACION SOLICITUD DE CAMBIO

			CALL  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio"
			(
				:V_RegistroSolicitudCambio,
				v_out_IdSecuencias
				
			);
			
			UPDATE "BSCL.Plataforma.BD::CRE.ModuloEmpresa"
			SET "IdEstado" = v_estadoAutorizacion
			WHERE "IdCREModuloEmpresa" = p_IdCREModuloEmpresa;
			------------------------------------------
			--Genera Evento de Autorizacion
			------------------------------------------
			BEGIN 
				DECLARE	 v_Glosa		NVARCHAR(255);
				declare v_out_idAuth 		bigint;
				declare v_out_AuthAdvertenciasContador 	int;
				declare v_out_AuthErroresContador 		int;
				v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
				SELECT CAST("NombreModuloEmpresa" AS NVARCHAR(255))  into  v_Glosa from "BSCL.Plataforma.BD::CRE.ModuloEmpresa" WHERE "IdCREModuloEmpresa"=p_IdCREModuloEmpresa;
							
				CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
					p_IdEmpresa => p_IdEmpresa,
					p_IdUsuario => p_IdUsuario,
					p_IdModulo => p_IdModulo,
					p_UsaReglasNegocio => 'N',							
					p_IdTipoEvento => v_IdTipoEvento,
					p_IdModuloSolicita => p_IdModulo,
					p_IdUsuarioSolicita => p_IdUsuario,
					p_Fecha => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'), 
					p_IdConcepto =>  v_IdConcepto,
					p_NumeroOperacionConcepto => CAST (p_IdCREModuloEmpresa AS NVARCHAR(40)), --v_secuencia_IdSolicitudCambio,
					p_FechaOperacionConcepto => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'),
					p_Glosa => v_Glosa,
					--salida
					p_out_IdEvento => v_out_idAuth,
					p_out_AdvertenciasContador => v_out_AuthAdvertenciasContador,
					p_out_AdvertenciasDetalle =>  v_out_AuthAdvertenciasDetalle,
					p_out_ErroresContador =>  v_out_AuthErroresContador,
					p_out_ErroresDetalle =>  v_out_AuthErroresDetalle);				
					
					v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'ADVERTENCIA' AS "Resultado", "Codigo" AS "Codigo", '' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
								FROM :v_out_AuthAdvertenciasDetalle UNION ALL SELECT * FROM :v_Errores;                             
	                                     
					v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'RECHAZO' AS "Resultado", "Codigo" AS "Codigo", '' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
								FROM :p_out_ErroresDetalle UNION ALL SELECT * FROM :v_Errores;                             
									                                     
				  if 	p_out_ErroresContador > 0 then
							SIGNAL SALIDA_CONTROLADA;
							
						else
							--Mensaje de Salida
							v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'ADVERTENCIA' AS "Resultado", 'REQUIERE_AUTORIZACION' AS "Codigo", '' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
										FROM DUMMY UNION ALL SELECT * FROM :v_Errores;                             
							SIGNAL SALIDA_CONTROLADA;
				  end if;
			  END;

		END;
					
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;