SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.ModificarCriterioSeleccionTasas";

-- 
--  Proposito: Crear criterio de seleccion de Tasas
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 05/10/2016  
-- 

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.ModificarCriterioSeleccionTasas" (
	-- Parametros entrada comunes
	IN p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio			NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_IdCriterioSeleccionTasas	BIGINT,
	IN p_Prioridad					BIGINT			DEFAULT 0,
	IN p_IdTipoInstrumento			NVARCHAR(3)		DEFAULT '',
	IN p_DiasAntiguedad				BIGINT  		DEFAULT 0,
	IN p_IdTipoCriterioTasa			NVARCHAR(40)	DEFAULT '0',
	IN p_IdContraparte				BIGINT  		DEFAULT 0,
	IN p_CodigoFuente				NVARCHAR(40)	DEFAULT '',
	IN p_IdTipoTasa					NVARCHAR(40)	DEFAULT '',
	IN p_IdTipoPrecio				NVARCHAR(40)	DEFAULT '',
	IN p_Interpolar					NVARCHAR(1)		DEFAULT 'N',
	IN p_Borrado					NVARCHAR(1)		DEFAULT 'N',
	IN p_Nemotecnico                NVARCHAR(250)	DEFAULT '',

	
	-- Parametro para registro en Log
	IN p_DetalleLog			TABLE (	"SubRegistro"	NVARCHAR(40),
									"CampoRegistro"	NVARCHAR(40),
									"ValorAntiguo"	NVARCHAR(250),
									"ValorNuevo"	NVARCHAR(250)),
	
	-- Parametros salida del servicio
    OUT p_out_RefId			BIGINT,
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
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
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
        
		DECLARE v_Count			BIGINT:=0;
		DECLARE v_FechaSistema	SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdTipoLog		NVARCHAR(40) := '';
		DECLARE v_NombreProceso	NVARCHAR(40) := '';
		DECLARE v_TipoCartera	NVARCHAR(250) := '';
		DECLARE v_IdUso         NVARCHAR(40) := '';
		DECLARE v_IdClasificacionTipoInstrumento NVARCHAR(3) := '';
		
		p_out_RefId := 0;
		
		--------------------
		--Validaciones
		--------------------
		BEGIN
			---Verifica Id de Criterio exista
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas"
			WHERE "IdCriterioSeleccionTasas" = :p_IdCriterioSeleccionTasas
			  AND "Borrado" = 'N';
			
			IF(v_Count = 0) THEN
				p_out_ErroresDetalle = select 'CRITERIO NO EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			
			SELECT CST."TipoCartera", CST."IdClasificacionTipoInstrumento", CST."IdUso" INTO v_TipoCartera, v_IdClasificacionTipoInstrumento, v_IdUso
			FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" CST
			WHERE "IdCriterioSeleccionTasas" = :p_IdCriterioSeleccionTasas
			  AND "Borrado" = 'N';
			
			-- Verifica si Criterio existe
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas"
			WHERE "IdCriterioSeleccionTasas" <> :p_IdCriterioSeleccionTasas
			  AND ("TipoCartera" = v_TipoCartera OR v_TipoCartera = '0' OR "TipoCartera" = '0')
			  AND "IdClasificacionTipoInstrumento" = v_IdClasificacionTipoInstrumento
			  AND ("IdTipoInstrumento"	= p_IdTipoInstrumento OR p_IdTipoInstrumento = '0' OR "IdTipoInstrumento" = '0')
			  AND "DiasAntiguedad"		= p_DiasAntiguedad
			  AND ("IdTipoCriterioTasa"	= p_IdTipoCriterioTasa OR p_IdTipoCriterioTasa = '0' OR "IdTipoCriterioTasa" = '0')
			  AND "CodigoFuente"		= p_CodigoFuente
			  AND "IdTipoTasa"			= p_IdTipoTasa
			  AND "IdTipoPrecio"		= p_IdTipoPrecio
			  AND "Interpolar"			= p_Interpolar
			  AND "IdUso"               = v_IdUso
			  AND ("IdContraparte"		= p_IdContraparte OR p_IdContraparte = '0' OR "IdContraparte" = 0)
			  --AND "IdEstado"			= p_IdEstado
			  AND "Nemotecnico"		    = p_Nemotecnico
			  AND "IdEmpresa"			= p_IdEmpresa
			  AND "Borrado"				= 'N';
			
			IF(v_Count > 0) THEN
				p_out_ErroresDetalle =  select 'CRITERIO EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			-- Validación para tipo curva de que si o si debe ser interpolación
			IF(p_IdTipoTasa = 'CURVA' AND p_Interpolar = 'N') THEN
				p_out_ErroresDetalle =  select 'LA CURVA DEBE TENER INTERPOLACION' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			IF(v_IdClasificacionTipoInstrumento != 'IF') THEN			
				-- Verifica si existe Criterio con misma Prioridad y valores
				SELECT COUNT(1) INTO v_Count
				FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" CST
				WHERE "IdCriterioSeleccionTasas" <> :p_IdCriterioSeleccionTasas
				  AND "Prioridad"			= p_Prioridad
				  AND ("TipoCartera" = v_TipoCartera OR v_TipoCartera = '0')
				  AND "IdClasificacionTipoInstrumento" = v_IdClasificacionTipoInstrumento
				  AND ("IdTipoInstrumento"	= p_IdTipoInstrumento OR p_IdTipoInstrumento = '0' OR "IdTipoInstrumento" = '0')
				  AND "DiasAntiguedad"		= p_DiasAntiguedad
				  AND ("IdTipoCriterioTasa"	= p_IdTipoCriterioTasa OR p_IdTipoCriterioTasa = '0')
				  AND "CodigoFuente"		= p_CodigoFuente
				  AND "IdTipoTasa"			= p_IdTipoTasa
				  AND "IdTipoPrecio"		= p_IdTipoPrecio
				  AND "Interpolar"			= p_Interpolar
				  AND "IdUso"               = v_IdUso
				  AND ("IdContraparte"		= p_IdContraparte OR p_IdContraparte = '0' OR "IdContraparte" = 0)
				  --AND "IdEstado"			= p_IdEstado
				  AND "IdEmpresa"			= p_IdEmpresa
				  AND "Borrado"				= 'N';
				
				IF(v_Count > 0) THEN
					p_out_ErroresDetalle =  select 'CRITERIO EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END IF;
			
			IF(v_IdClasificacionTipoInstrumento != 'IF') THEN			
				-- Verifica si existe Criterio con misma prioridad y mismas llaves
				SELECT COUNT(1) INTO v_Count
				FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas"
				WHERE "IdCriterioSeleccionTasas" <> :p_IdCriterioSeleccionTasas
				  AND "Prioridad"			= p_Prioridad
				  AND ("TipoCartera" = v_TipoCartera OR v_TipoCartera = '0' OR "TipoCartera" = '0')
				  AND "IdClasificacionTipoInstrumento" = v_IdClasificacionTipoInstrumento
				  AND ("IdTipoInstrumento"	= p_IdTipoInstrumento OR p_IdTipoInstrumento = '0' OR "IdTipoInstrumento" = '0')
				  AND ("IdTipoCriterioTasa"	= p_IdTipoCriterioTasa OR p_IdTipoCriterioTasa = '0' OR "IdTipoCriterioTasa" = '0')
				  AND "IdEmpresa"			= p_IdEmpresa
				  AND "IdUso"               = v_IdUso
				  AND ("IdContraparte"		= p_IdContraparte OR p_IdContraparte = '0' OR "IdContraparte" = 0)
				  AND "Borrado"				= 'N';
				
				IF(v_Count > 0 AND p_Borrado='N') THEN
					p_out_ErroresDetalle =  select 'EXISTE CRITERIO CON MISMOS VALORES (PRIORIDAD Y LLAVES)' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END IF;
			IF(v_IdClasificacionTipoInstrumento = 'IF') THEN
				-- Verifica si existe Criterio con mismas llaves y prioridad.
				SELECT COUNT(1) INTO v_Count
				FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas"
				WHERE "IdCriterioSeleccionTasas" <> :p_IdCriterioSeleccionTasas
				  AND ("TipoCartera" = v_TipoCartera OR v_TipoCartera = '0' OR "TipoCartera" = '0')
				  AND "Prioridad" = p_Prioridad
				  AND "IdUso"				= v_IdUso
				  AND ("IdContraparte"		= p_IdContraparte OR p_IdContraparte = '0' OR "IdContraparte" = 0)
				  AND "IdClasificacionTipoInstrumento" = v_IdClasificacionTipoInstrumento
				  AND ("IdTipoInstrumento"	= p_IdTipoInstrumento OR p_IdTipoInstrumento = '0' OR "IdTipoInstrumento" = '0')
				  AND ("IdTipoCriterioTasa"	= p_IdTipoCriterioTasa OR p_IdTipoCriterioTasa = '0' OR "IdTipoCriterioTasa" = '0')
				  AND "IdEmpresa"			= p_IdEmpresa
				  AND "Borrado"				= 'N'
				  AND "Nemotecnico" = p_Nemotecnico;
				
				IF(v_Count > 0 AND p_Borrado='N') THEN
					p_out_ErroresDetalle =  select 'EXISTE CRITERIO CON MISMOS VALORES (LLAVES Y PRIORIDAD)' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END IF;
		END;
		
		
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------
				
		BEGIN
			IF p_Borrado='S' THEN
				v_IdTipoLog		:= 'ELIMINACION';
				v_NombreProceso	:= 'ELIMINACION CRITERIO SELECCION TASAS';
				
				UPDATE CST
				SET 
					 CST."Borrado"				= 'S'
					,CST."IdUsuarioModificacion"= p_IdUsuario
					,CST."FechaModificacion"	= CURRENT_UTCTIMESTAMP
				FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" CST
				WHERE CST."IdCriterioSeleccionTasas" = p_IdCriterioSeleccionTasas
				  AND CST."IdEmpresa" = p_IdEmpresa;
			END IF;
			
			IF p_Borrado='N' THEN
				v_IdTipoLog		:= 'MODIFICACION';
				v_NombreProceso	:= 'MODIFICACION CRITERIO SELECCION TASAS';
				
				UPDATE CST
				SET
					 CST."Prioridad"			= p_Prioridad
					,CST."IdTipoInstrumento"	= p_IdTipoInstrumento
					,CST."DiasAntiguedad"		= p_DiasAntiguedad
					,CST."IdTipoCriterioTasa"	= p_IdTipoCriterioTasa
					,CST."CodigoFuente"			= p_CodigoFuente
					,CST."IdTipoTasa"			= p_IdTipoTasa
					,CST."IdContraparte"		= p_IdContraparte
					,CST."IdTipoPrecio"			= p_IdTipoPrecio
					,CST."Interpolar"			= p_Interpolar
					,CST."IdUsuarioModificacion"= p_IdUsuario
					,CST."FechaModificacion"	= CURRENT_UTCTIMESTAMP
				FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" CST
				WHERE CST."IdCriterioSeleccionTasas" = p_IdCriterioSeleccionTasas
				  AND CST."IdEmpresa" = p_IdEmpresa;
			END IF;
			
			p_out_RefId := 1;
			
			
			-- LOG DE AUDITORIA
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaSistema);
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog"(
				 p_IdTipoLog 		 => v_IdTipoLog
				,p_IdModulo 		 => :p_IdModulo
				,p_IdUsuario 		 => :p_IdUsuario
				,p_IdEmpresa 		 => :p_IdEmpresa
				,p_FechaSistemaLog 	 => TO_NVARCHAR(:v_FechaSistema,'YYYY-MM-DD')
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro 	 => 'CRITERIOSELECCIONTASAS'
				,p_NombreProceso 	 => v_NombreProceso
				,p_DetalleLog 		 => :p_DetalleLog
			);
			
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
