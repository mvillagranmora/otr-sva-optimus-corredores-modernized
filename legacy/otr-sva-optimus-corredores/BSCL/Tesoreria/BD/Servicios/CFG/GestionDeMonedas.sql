SET SCHEMA "BSCL";   
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CFG::GestionDeMonedas";
-- 
--  Proposito: Realizar configuración de las monedas permitidas para operar en tesorería
--  Autor: NROJAS
--  Fecha Creacion: 22/09/2016
--

CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.CFG::GestionDeMonedas"(
	-- Parametros de Validacion                                                                                            
	IN p_IdEmpresa                     	BIGINT,
	IN p_IdUsuario                     	BIGINT,
	IN p_IdModulo                      	NVARCHAR(3),
	IN p_UsaReglasNegocio              	NVARCHAR(1),
	IN p_FechaMovimiento			    NVARCHAR(10),
   	IN p_Monedas						TABLE("IdMoneda"  NVARCHAR(3), 
   											  "TipoEjecucion" NVARCHAR(3)), -- ACT 	: Ingresar una moneda a tesorería
																			-- INA	: Eliminar moneda de tesoreria
											  
	
	--Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador  		INT,
	OUT p_out_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador      INT,
	OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador           INT,   
	OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY
) 
   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
	
BEGIN  
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_EXITOSA					CONDITION;
	DECLARE SALIDA_CONTROLADA 				CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 		CONDITION;
	DECLARE v_resultadoRegla 			  	NVARCHAR(40);
	DECLARE v_TablaCodigosMensajes			TABLE("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
				        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_TablaCodigosMensajes= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);		
		p_out_ErroresDetalle =  SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_EXITOSA     
	BEGIN
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);
		p_out_InformacionDetalle = SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT("Codigo") INTO p_out_InformacionContador FROM :p_out_InformacionDetalle;		
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL;
		END IF;
	END;

	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	v_Monedas =
	 SELECT ROW_NUMBER() over (ORDER BY "IdMoneda","TipoEjecucion") as "IdInterno","IdMoneda","TipoEjecucion"
	   FROM :p_Monedas;
	--------------------------------------------
	-- VALIDACIONES
	-------------------------------------------- 
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		---Validaciones datos de entrada			
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA; 	
		END IF;

		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGestionDeMonedas"(
			p_IdEmpresa		=>	:p_IdEmpresa,
		    p_Monedas		=>	:v_Monedas,
		    p_out_Resultado	=>	v_Resultado
		);
		
		--Se eliminan los registros rechazados
		v_Monedas = 
		 SELECT M."IdInterno",M."IdMoneda",M."TipoEjecucion"
		   FROM :v_Monedas as M
		  INNER JOIN (SELECT DISTINCT "IdInterno" FROM :v_Resultado WHERE "Resultado" = 'OK'
					  EXCEPT 
					  SELECT DISTINCT "IdInterno" FROM :v_Resultado WHERE "Resultado" = 'RECH') as R
				  ON R."IdInterno" = M."IdInterno";
				  
		
		--Se Consulta diccionario
		v_TablaCodigosMensajes =
		SELECT DISTINCT "IdInterno"
				,"Codigo"
				,'IdMoneda' as "CodigoDato1","IdMoneda" as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
		  FROM :v_Resultado
		 WHERE "Resultado" <> 'OK';		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);
		
		p_out_ErroresDetalle =  SELECT E."Codigo",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_Resultado as R ON R."IdInterno" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'RECH';
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
		
		p_out_AdvertenciasDetalle =  SELECT E."Codigo",'' as "RequiereAutorizacion",E."Descripcion" FROM :v_erroroutmsg as E INNER JOIN :v_Resultado as R ON R."IdInterno" = E."IdInterno" AND R."Codigo" = E."Codigo" AND R."Resultado" = 'ADVE';
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END; 
	END IF;


	--SIGNAL SALIDA_EXITOSA;

	--------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE v_InformacionContador,v_AdvertenciasContador,v_ErroresContador	INT	:= 0;
		DECLARE FechaIngreso	TIMESTAMP;
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
		FechaIngreso := :p_FechaMovimiento || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7');
		
		--1) Activar o Desactivar monedas 
		INSERT INTO "BSCL.Tesoreria.BD::TES.MonedaTesoreria"
		SELECT   NMon."IdMoneda"
				,:p_IdEmpresa as "IdEmpresa"
				,:p_IdUsuario as "IdUsuario"
				,:FechaIngreso as "FechaIngresoRegistro"
				,'ACT' as "IdEstado" 
		  FROM :v_Monedas as NMon
		 INNER JOIN (SELECT "IdMoneda"
		 			   FROM :v_Monedas
		 			  WHERE "TipoEjecucion" = 'ACT'
		 			 EXCEPT
		 			 SELECT "IdMoneda"
		 			   FROM "BSCL.Tesoreria.BD::TES.MonedaTesoreria"
		 			) as Mon
		 		 ON Mon."IdMoneda" = NMon."IdMoneda";
		   		
		UPDATE MT
		   SET MT."IdEstado" = NMon."TipoEjecucion"
		  FROM "BSCL.Tesoreria.BD::TES.MonedaTesoreria" as MT
		 INNER JOIN :v_Monedas as NMon ON NMon."IdMoneda" = MT."IdMoneda";
		
		
		--3) Desactivar Cuentas Inversion (Si no se puede modificar las Cuentas de inversión no impide continuar con el proceso de gestión de monedas de tesoreria)
		v_CuentaInversion = 
		 SELECT CTA."IdCliente",
				CTA."IdPortafolio",
				CTA."IdMoneda",
				CTA."IdCuentaInversion",
				null as "Nombre",
				null as "CuentaInversionPrincipal",
				CASE WHEN Mon."TipoEjecucion" = 'INA' AND CTA."BloqueadaEdicion" = 'N' THEN 'D' 
					 WHEN Mon."TipoEjecucion" = 'ACT' AND CTA."BloqueadaEdicion" = 'N' THEN 'A' 
				 END as"Accion"
		   FROM :v_Monedas as Mon
		  INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CTA ON CTA."IdMoneda" = Mon."IdMoneda";
		  		
		IF NOT IS_EMPTY(:v_CuentaInversion) THEN
			CALL "BSCL.CuentaInversion.BD.Servicios::GestionCuentaInversion" (
				 p_IdEmpresa				=>  :p_IdEmpresa
			   	,p_IdUsuario				=>	:p_IdUsuario
			   	,p_IdModulo					=>	:p_IdModulo
				,p_UsaReglasNegocio			=>	'S'
				,p_CuentaInversion			=>	:v_CuentaInversion
			    ,p_out_ResultadoProceso		=>	v_ResultadoProceso
			    ,p_out_InformacionContador	=>	v_InformacionContador
			    ,p_out_InformacionDetalle	=>	v_InformacionDetalle
			    ,p_out_AdvertenciasContador	=>	v_AdvertenciasContador
				,p_out_AdvertenciasDetalle	=>	v_AdvertenciasDetalle
				,p_out_ErroresContador		=>	v_ErroresContador
				,p_out_ErroresDetalle		=>	v_ErroresDetalle
			);
		END IF;
		
		--2) Log de proceso
		v_Log =
		 SELECT  ROW_NUMBER() over (order by MT."IdMoneda",MT."IdEmpresa") as "IdCabeceraLog"
				,'EDICION' 		as "IdTipoLog"
				,p_IdModulo 	as "IdModulo"
				,p_IdUsuario 	as "IdUsuario"
				,p_IdEmpresa 	as "IdEmpresa"
				,CAST(p_FechaMovimiento as DATE) as "FechaSistemaLog"
				,p_IdUsuario 	as "IdUsuarioAtendido"
				,0  as "IdPersona"
				,0 	as "IdCliente"
				,'' as "EstadoCliente"
				,'' as "NombreCliente"
				,'' as "IdTipoDocumentoIdentidad"
				,'' as "IdDocumentoIdentidad"
				,0  as "IdPortafolio"
				,'' as "EstadoPortafolio"
				,'GESTION MONEDA TESORERIA' as "IdTipoRegistro"
				,'' as "IdRegistro"
				,'' as "EstadoRegistro"
				,'' as "IdTipoOperacion"
				,0	as "Folio"
				,0  as "Cantidad"
				,0  as "PrecioTasa"
				,0  as "Monto"
				,'' as "IdMonedaOrigen"
				,'' as "IdMonedaLiquidacion"
				,'' as "TipoCambio"
				,'' as "Nemo"
				,0  as "IdProceso"
				,'' as "EstadoProceso"
				,'' as "NombreProceso"
		   FROM "BSCL.Tesoreria.BD::TES.MonedaTesoreria" as MT
		  INNER JOIN :v_Monedas as NMon ON NMon."IdMoneda" = MT."IdMoneda"
		  WHERE "FechaIngresoRegistro" <> :FechaIngreso;
				
		v_LogDetalle =
		 SELECT  ROW_NUMBER() over (order by MT."IdMoneda",MT."IdEmpresa") as "IdCabeceraLog"
		 		,'IdMoneda' as "SubRegistro"
		 		,'IdEstado' as "CampoRegistro"
		 		,CASE WHEN NMon."TipoEjecucion" = 'ACT' THEN 'INA' ELSE 'ACT' END as "ValorAntiguo"
		 		,NMon."TipoEjecucion" as "ValorNuevo" 
		   FROM "BSCL.Tesoreria.BD::TES.MonedaTesoreria" as MT
		  INNER JOIN :v_Monedas as NMon ON NMon."IdMoneda" = MT."IdMoneda"
		  WHERE "FechaIngresoRegistro" <> :FechaIngreso;
				
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo"(
			p_Log			=> :v_Log,
			p_LogDetalle	=> :v_LogDetalle
		);
		   
		   
		--3) Mensaje de exito
		v_TablaCodigosMensajes =
		SELECT  ROW_NUMBER() over (partition by "IdMoneda" order by "IdMoneda") AS "IdInterno"
			   ,CASE WHEN "TipoEjecucion" = 'ACT' THEN 'UI_MONEDA_TESORERIA_ACTIVA'
			   		 WHEN "TipoEjecucion" = 'INA' THEN 'UI_MONEDA_TESORERIA_INACTIVA'
			   	 END as "Codigo"
			   ,'IdMoneda' as "CodigoDato1"
			   ,"IdMoneda" as "ValorDato1"
			   ,'' as "CodigoDato2"
			   ,'' as "ValorDato2" 
		  FROM :v_Monedas;
		SIGNAL SALIDA_EXITOSA;
    END;
END;

