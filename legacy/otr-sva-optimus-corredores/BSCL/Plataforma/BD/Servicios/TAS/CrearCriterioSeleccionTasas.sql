SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.CrearCriterioSeleccionTasas";

-- 
--  Proposito: Crear criterio de seleccion de Tasas
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 06/10/2016  -
-- 
  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.CrearCriterioSeleccionTasas" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_Prioridad							BIGINT			DEFAULT 0,
	IN p_TipoCartera						NVARCHAR(250)	DEFAULT '0',
	IN p_IdContraparte						BIGINT			DEFAULT 0,
	IN p_IdUso								NVARCHAR(40)	DEFAULT '',
	IN p_IdClasificacionTipoInstrumento		NVARCHAR(3) 	DEFAULT '',
	IN p_IdTipoInstrumento					NVARCHAR(3)		DEFAULT '0',
	IN p_DiasAntiguedad						BIGINT  		DEFAULT 0,
	IN p_IdTipoCriterioTasa					NVARCHAR(40)	DEFAULT '0',
	IN p_CodigoFuente						NVARCHAR(40)	DEFAULT '',
	IN p_IdTipoTasa							NVARCHAR(40)	DEFAULT '',
	IN p_IdTipoPrecio						NVARCHAR(40)	DEFAULT '',
	IN p_Interpolar							NVARCHAR(1)		DEFAULT 'N',
	IN p_Nemotecnico                        NVARCHAR(250)	DEFAULT '',
	
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
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;										
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
        
		DECLARE v_Count				BIGINT:=0;
		DECLARE v_FechaHoraCreacion	SECONDDATE;	-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdCriterio		BIGINT:=0;
		
		
		--------------------
		--Validaciones
		--------------------
		
		-- Verifica si Criterio existe
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas"
		WHERE ("TipoCartera" = p_TipoCartera OR p_TipoCartera = '0' OR "TipoCartera" = '0')
		  AND "IdUso"				= p_IdUso
		  AND "IdClasificacionTipoInstrumento" = p_IdClasificacionTipoInstrumento
		  AND ("IdTipoInstrumento"	= p_IdTipoInstrumento OR p_IdTipoInstrumento = '0' OR "IdTipoInstrumento" = '0')
		  --AND "IdTipoOperacion"		= p_IdTipoOperacion
		  AND "DiasAntiguedad"		= p_DiasAntiguedad
		  AND ("IdContraparte"		= p_IdContraparte OR p_IdContraparte = '0' OR "IdContraparte" = 0)
		  AND ("IdTipoCriterioTasa"	= p_IdTipoCriterioTasa OR p_IdTipoCriterioTasa = '0' OR "IdTipoCriterioTasa" = '0')
		  AND "CodigoFuente"		= p_CodigoFuente
		  AND "IdTipoTasa"			= p_IdTipoTasa
		  AND "IdTipoPrecio"		= p_IdTipoPrecio
		  AND "Interpolar"			= p_Interpolar
		  AND "Nemotecnico"		    = p_Nemotecnico
		  AND "IdEmpresa"			= p_IdEmpresa
		  AND "Borrado"				= 'N';
		
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle =  select 'CRITERIO EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		IF(p_IdClasificacionTipoInstrumento != 'IF') THEN
			-- Verifica si existe Criterio con mismas llaves Y Fuente
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas"
			WHERE ("TipoCartera" = p_TipoCartera OR p_TipoCartera = '0' OR "TipoCartera" = '0')
			  AND "IdUso"				= p_IdUso
			  AND ("IdContraparte"		= p_IdContraparte OR p_IdContraparte = '0' OR "IdContraparte" = 0)
			  AND "IdClasificacionTipoInstrumento" = p_IdClasificacionTipoInstrumento
			  AND ("IdTipoInstrumento"	= p_IdTipoInstrumento OR p_IdTipoInstrumento = '0' OR "IdTipoInstrumento" = '0')
			  AND ("IdTipoCriterioTasa"	= p_IdTipoCriterioTasa OR p_IdTipoCriterioTasa = '0' OR "IdTipoCriterioTasa" = '0')
			  AND "CodigoFuente"		= p_CodigoFuente
			  AND "IdEmpresa"			= p_IdEmpresa
			  AND "Borrado"				= 'N'
			  AND "IdTipoTasa" = p_IdTipoTasa
			  AND "Interpolar" = p_Interpolar;
			
			IF(v_Count > 0) THEN
				p_out_ErroresDetalle =  select 'EXISTE CRITERIO CON MISMOS VALORES (LLAVES Y FUENTE)' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END IF;
		
		IF(p_IdClasificacionTipoInstrumento = 'IF') THEN
			-- Verifica si existe Criterio con mismas llaves y prioridad.
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas"
			WHERE ("TipoCartera" = p_TipoCartera OR p_TipoCartera = '0' OR "TipoCartera" = '0')
			  AND "Prioridad" = p_Prioridad
			  AND "IdUso"				= p_IdUso
			  AND ("IdContraparte"		= p_IdContraparte OR p_IdContraparte = '0' OR "IdContraparte" = 0)
			  AND "IdClasificacionTipoInstrumento" = p_IdClasificacionTipoInstrumento
			  AND ("IdTipoInstrumento"	= p_IdTipoInstrumento OR p_IdTipoInstrumento = '0' OR "IdTipoInstrumento" = '0')
			  AND ("IdTipoCriterioTasa"	= p_IdTipoCriterioTasa OR p_IdTipoCriterioTasa = '0' OR "IdTipoCriterioTasa" = '0')
			  AND "IdEmpresa"			= p_IdEmpresa
			  AND "Borrado"				= 'N'
			  AND "Nemotecnico" = p_Nemotecnico;
			
			IF(v_Count > 0) THEN
				p_out_ErroresDetalle =  select 'EXISTE CRITERIO CON MISMOS VALORES (LLAVES Y PRIORIDAD)' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END IF;
		IF(p_IdClasificacionTipoInstrumento = 'IF') THEN
			-- Verifica si existe Criterio con mismas llaves, prioridad y exlusividad tipo instrumento.
			-- si ya hay un TODOS no puede agregar un PAG/
            -- si ya hay un pag/pac no puede agregar un TODOS.
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas"
			WHERE ("TipoCartera" = :p_TipoCartera OR :p_TipoCartera = '0' OR "TipoCartera" = '0')
			  AND "IdUso"				= :p_IdUso
			  AND ("IdContraparte"		= :p_IdContraparte OR :p_IdContraparte = '0' OR "IdContraparte" = 0)
			  AND "IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento
			  AND (((:p_IdTipoInstrumento = 'PAG' OR :p_IdTipoInstrumento = 'PAC') AND "IdTipoInstrumento" = '0')  OR (:p_IdTipoInstrumento = '0' AND  ("IdTipoInstrumento" = 'PAC' OR  "IdTipoInstrumento" = 'PAG')))
			  AND ("IdTipoCriterioTasa"	= :p_IdTipoCriterioTasa OR :p_IdTipoCriterioTasa = '0' OR "IdTipoCriterioTasa" = '0')
			  AND "IdEmpresa"			= :p_IdEmpresa
			  AND "Borrado"				= 'N';
			
			IF(v_Count > 0) THEN
				p_out_ErroresDetalle =  select 'EXISTE CRITERIO CON MISMOS VALORES (YA SE SELECCIONO TIPO INSTRUMENTO)' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END IF;
		-- Validación para tipo curva de que si o si debe ser interpolación
		IF(p_IdTipoTasa = 'CURVA' AND p_Interpolar = 'N') THEN
			p_out_ErroresDetalle =  select 'LA CURVA DEBE TENER INTERPOLACION' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		IF(p_IdClasificacionTipoInstrumento != 'IF') THEN
			-- Verifica si existe Criterio con misma prioridad y mismas llaves
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas"
			WHERE "Prioridad"			= p_Prioridad
			  AND ("TipoCartera" = p_TipoCartera OR p_TipoCartera = '0' OR "TipoCartera" = '0')
			  AND "IdUso"				= p_IdUso
			  AND ("IdContraparte"		= p_IdContraparte OR p_IdContraparte = '0' OR "IdContraparte" = 0)
			  AND "IdClasificacionTipoInstrumento" = p_IdClasificacionTipoInstrumento
			  AND ("IdTipoInstrumento"	= p_IdTipoInstrumento OR p_IdTipoInstrumento = '0' OR "IdTipoInstrumento" = '0')
			  AND ("IdTipoCriterioTasa"	= p_IdTipoCriterioTasa OR p_IdTipoCriterioTasa = '0' OR "IdTipoCriterioTasa" = '0')
			  AND "IdEmpresa"			= p_IdEmpresa
			  AND "Borrado"				= 'N';
		
			IF(v_Count > 0) THEN
				p_out_ErroresDetalle =  select 'EXISTE CRITERIO CON MISMOS VALORES (PRIORIDAD Y LLAVES)' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END IF;

		
		--------------------
		--Reglas Negocio
		--------------------
		
		--------------------
		--Lógica de Negocio 
		--------------------
				
		BEGIN
			p_out_RefId:=1;
			
		 	------------------------------------------
			--Crear 
			------------------------------------------
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaHoraCreacion);
			
			SELECT "BSCL.Plataforma.BD.Secuencias::TAS.IdCriterioSeleccionTasas".NEXTVAL INTO v_IdCriterio FROM DUMMY;
			p_out_RefId := v_IdCriterio;
			
			INSERT INTO "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas"(
				 "IdCriterioSeleccionTasas"
				,"Prioridad"
				,"TipoCartera"
				,"IdClasificacionTipoInstrumento"
				,"IdTipoInstrumento"
				--,"IdTipoOperacion"
				,"DiasAntiguedad"
				,"IdTipoCriterioTasa"
				,"CodigoFuente"
				,"IdTipoTasa"
				,"IdTipoPrecio"
				,"Interpolar"
				--,"IdEstado"
				,"IdEmpresa"
				,"IdUsuarioModificacion"
				,"FechaCreacion"
				,"FechaModificacion"
				,"IdUso"
				,"IdContraparte"
				,"Nemotecnico"
				)
			VALUES(
				 v_IdCriterio
				,p_Prioridad
				,p_TipoCartera
				,p_IdClasificacionTipoInstrumento
				,p_IdTipoInstrumento
				--,p_IdTipoOperacion
				,p_DiasAntiguedad
				,p_IdTipoCriterioTasa
				,p_CodigoFuente
				,p_IdTipoTasa
				,p_IdTipoPrecio
				,p_Interpolar
				--,p_IdEstado
				,p_IdEmpresa
				,p_IdUsuario
				,v_FechaHoraCreacion
				,CURRENT_UTCTIMESTAMP
				,p_IdUso
				,p_IdContraparte
				,p_Nemotecnico
			);
	
		END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
