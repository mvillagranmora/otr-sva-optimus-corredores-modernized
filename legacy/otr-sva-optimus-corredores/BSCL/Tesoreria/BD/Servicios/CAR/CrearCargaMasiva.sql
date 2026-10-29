SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CAR::CrearCargaMasiva";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Graba los datos de una carga masiva con su detalle.
--             Este servicio no procesa los registros(no crea mov de tesoreria) solo guarda la carga para posteriormente ser procesada
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 30/09/2016
--
"BSCL.Tesoreria.BD.Servicios.CAR::CrearCargaMasiva"(

    -- Parametros de Validacion                                                                                            
	IN p_IdEmpresa					BIGINT,
   	IN p_IdUsuario					BIGINT,
   	IN p_IdModulo					NVARCHAR(3),                                                                       
   	IN p_UsaReglasNegocio			NVARCHAR(1),	 -- S/N se usa en caso de servicios anidados.
	IN p_FechaCarga                 NVARCHAR(10),    -- Fecha de Sistema
	IN p_CargaMasiva                TABLE (
											"IdInterno"					BIGINT,           -- Solo Referencial(para relacionar maestro con detalle)
											"CodigoTipoCargaMasiva" 	NVARCHAR(40), 	  -- Ej. CARGA_RECAUDACION_SERVIPAG / CARGA_GENERICA_MOVIMIENTOS / CONCILIACION_RECAUDACION_BCP
											"NombreArchivo"         	NVARCHAR(100)     -- Nombre del archivo fisico(txt/csv)
										  ),
										  
	IN p_CargaMasivaDetalle			TABLE (  -- Parametros estandar para todo tipo de carma masiva
											"IdInterno"				BIGINT,               -- Solo Referencial(para relacionar maestro con detalle)
											"IdRegistro"			BIGINT,               -- Indicador del registro para trasponer
											"NombreCampo"			NVARCHAR(255), 		   
											"ValorCampo"			NVARCHAR(520)		  
									      ),

   	-- Parametros de Salida
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
	DECLARE SALIDA_CONTROLADA 			CONDITION;
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_CargaMasivaValidar TABLE ("IdCargaMasiva"     BIGINT, "CodigoTipoCargaMasiva"     NVARCHAR(40));
	DECLARE v_cantSocioInexistente INT;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA
	BEGIN
		v_hash= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_hash,:v_erroroutmsg);		
		p_out_ErroresDetalle =  SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
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
	


	p_out_ErroresContador			:= 0;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;  
	v_CargaMasivaValidar            = SELECT 0 as "IdCargaMasiva",'' as "CodigoTipoCargaMasiva" FROM dummy DUMMY;

	--------------------
	-- VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN		
		-- Validaciones datos de entrada			
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA; 	
		END IF;		
	END; 
	END IF;



	--------------------
	--LOGICA
	--------------------
	BEGIN
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		

		----------------------------------------------------------------------------
		-- PROCESA DATOS PARA DETALLE CARGA MASIVA (para todos los tipos de carga)
		----------------------------------------------------------------------------
		v_CargaMasivaDetalle =								      
			SELECT
				-- DATOS GENERALES
				CM."IdInterno",
				CM."CodigoTipoCargaMasiva",
				CM."NombreArchivo",
				CMD."IdRegistro",
				CAST(MAX(CMD."Monto") 	AS DECIMAL(26,8)) AS "Monto",
				MAX(CMD."Glosa") 		AS "Glosa",
				MAX(CMD."NumeroCuenta") AS "NumeroCuenta",
				CASE 
					WHEN "CodigoTipoCargaMasiva" = 'EGRESOS_POR_ABONOS_BCP_PAC' 	THEN  CAST(MAX(CMD."TipoMoneda") AS NVARCHAR(1))
					WHEN "CodigoTipoCargaMasiva" = 'INGRESOS_POR_CARGOS_BCP_PAC' 	THEN  CAST(MAX(CMD."TipoMoneda") AS NVARCHAR(1))
					WHEN "CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP' 	THEN  MAX(CMD."CodigoMoneda")
					ELSE ''
				END	AS "Moneda",

				-- DATOS PARA CARGA MASIVA: "CARGA_GENERICA_MOVIMIENTOS"
				MAX(CMD."TipoMovimiento") 		AS "TipoMovimiento",
				MAX(CMD."DocumentoIdentidad") 	AS "DocumentoIdentidad",
				MAX(CMD."CodigoPortafolio") 	AS "CodigoPortafolio",
				MAX(CMD."PlanTipoInversion") 	AS "PlanTipoInversion",
				MAX(CMD."ProductoAgrupador") 	AS "ProductoAgrupador",
				MAX(CMD."RegimenTributario") 	AS "RegimenTributario",
				
				-- DATOS PARA CARGA MASIVA: "EGRESOS_POR_ABONOS_BCP_PAC", "INGRESOS_POR_CARGOS_BCP_PAC"
				MAX(CMD."TipoCuenta") 			AS "TipoCuenta",
				MAX(CMD."TipoIDC") 				AS "TipoIDC",
				MAX(CMD."NumeroIDC") 			AS "NumeroIDC",
				MAX(CMD."TipoMoneda") 			AS "TipoMoneda",
				
				-- DATOS PARA CARGA MASIVA: "CONCILIACION_RECAUDACION_BCP"
				MAX(CMD."CodigoCuenta") 		AS "CodigoCuenta",
				MAX(CMD."CodigoMoneda") 		AS "CodigoMoneda",
				MAX(CMD."IdentificacionDepositante") 	AS "IdentificacionDepositante",
				MAX(CMD."FechaTransaccion") 	AS "FechaTransaccion",
				MAX(CMD."NumeroOperacion") 		AS "NumeroOperacion",
				MAX(CMD."ReferenciaOperacion") 	AS "ReferenciaOperacion",
				MAX(CMD."IndicadorExtorno") 	AS "IndicadorExtorno",
				MAX(CMD."NumeroOperacionBD") 	AS "NumeroOperacionBD"	
			FROM 
			(
				SELECT  -- Se usa para transponer datos
					-- DATOS GENERALES
					"IdInterno" , "IdRegistro",
					CASE WHEN ("NombreCampo" = 'NumeroCuenta') 				THEN "ValorCampo" ELSE NULL END AS "NumeroCuenta",
					CASE WHEN ("NombreCampo" = 'Monto') 					THEN "ValorCampo" ELSE NULL END AS "Monto",

					-- DATOS PARA CARGA MASIVA: "EGRESOS_POR_ABONOS_BCP_PAC"
					CASE WHEN ("NombreCampo" = 'TipoMovimiento') 	 		THEN "ValorCampo" ELSE NULL END AS "TipoMovimiento",
					CASE WHEN ("NombreCampo" = 'DocumentoIdentidad') 		THEN "ValorCampo" ELSE NULL END AS "DocumentoIdentidad",
					CASE WHEN ("NombreCampo" = 'CodigoPortafolio') 	 		THEN "ValorCampo" ELSE NULL END AS "CodigoPortafolio",
					
					-- DATOS PARA CARGA MASIVA: "EGRESOS_POR_ABONOS_BCP_PAC", "INGRESOS_POR_CARGOS_BCP_PAC"
					CASE WHEN ("NombreCampo" = 'TipoCuenta') 				THEN "ValorCampo" ELSE NULL END AS "TipoCuenta",
					CASE WHEN ("NombreCampo" = 'TipoIDC') 					THEN "ValorCampo" ELSE NULL END AS "TipoIDC",
					CASE WHEN ("NombreCampo" = 'NumeroIDC') 				THEN "ValorCampo" ELSE NULL END AS "NumeroIDC",
					CASE WHEN ("NombreCampo" = 'TipoMoneda') 				THEN "ValorCampo" ELSE NULL END AS "TipoMoneda",
					CASE WHEN ("NombreCampo" = 'Glosa') 					THEN "ValorCampo" ELSE NULL END AS "Glosa",
					
					-- DATOS PARA CARGA MASIVA: "CONCILIACION_RECAUDACION_BCP"
					CASE WHEN ("NombreCampo" = 'CodigoCuenta') 				THEN "ValorCampo" ELSE NULL END AS "CodigoCuenta",
					CASE WHEN ("NombreCampo" = 'CodigoMoneda') 				THEN "ValorCampo" ELSE NULL END AS "CodigoMoneda",
					CASE WHEN ("NombreCampo" = 'IdentificacionDepositante') THEN "ValorCampo" ELSE NULL END AS "IdentificacionDepositante",
					CASE WHEN ("NombreCampo" = 'FechaTransaccion') 			THEN "ValorCampo" ELSE NULL END AS "FechaTransaccion",
					CASE WHEN ("NombreCampo" = 'NumeroOperacion') 			THEN "ValorCampo" ELSE NULL END AS "NumeroOperacion",
					CASE WHEN ("NombreCampo" = 'ReferenciaOperacion') 		THEN "ValorCampo" ELSE NULL END AS "ReferenciaOperacion",
					CASE WHEN ("NombreCampo" = 'IndicadorExtorno') 			THEN "ValorCampo" ELSE NULL END AS "IndicadorExtorno",
					CASE WHEN ("NombreCampo" = 'NumeroOperacionBD') 		THEN "ValorCampo" ELSE NULL END AS "NumeroOperacionBD",
					
					-- CARGA_GENERICA_MOVIMIENTOS
					CASE WHEN ("NombreCampo" = 'PlanTipoInversion') 		THEN "ValorCampo" ELSE NULL END AS "PlanTipoInversion",
					CASE WHEN ("NombreCampo" = 'ProductoAgrupador') 		THEN "ValorCampo" ELSE NULL END AS "ProductoAgrupador",
					CASE WHEN ("NombreCampo" = 'RegimenTributario') 		THEN "ValorCampo" ELSE NULL END AS "RegimenTributario"
										
				FROM :p_CargaMasivaDetalle
			) AS CMD
			INNER JOIN :p_CargaMasiva AS CM ON (CM."IdInterno" = CMD."IdInterno")
			GROUP BY CM."IdInterno",CM."CodigoTipoCargaMasiva",CM."NombreArchivo",CMD."IdRegistro";
		
	
		-----------------------------------------------------------------
		-- PROCESA DATOS PARA ENCABEZADO CARGA MASIVA
		------------------------------------------------------------------			
		v_CargaMasiva = 
			SELECT
				"BSCL.Tesoreria.BD.Secuencias::CAR.CargaMasiva".NEXTVAL as "IdCargaMasiva", -- Nuevas secuencias
				"IdInterno",
				"CodigoTipoCargaMasiva",
				"NombreArchivo",
				"CantidadRegistros", 
				"MontoTotal",
				CASE 
					WHEN "CodigoTipoCargaMasiva" = 'EGRESOS_POR_ABONOS_BCP_PAC'  THEN CAST("Moneda" AS NVARCHAR(1))
					WHEN "CodigoTipoCargaMasiva" = 'INGRESOS_POR_CARGOS_BCP_PAC' THEN CAST("Moneda" AS NVARCHAR(1))
					ELSE "Moneda"
				END	AS "Moneda"
			FROM
			(
				SELECT "IdInterno", "CodigoTipoCargaMasiva", "NombreArchivo",				
					COUNT("IdInterno") AS "CantidadRegistros",
					CASE
						WHEN (COUNT(DISTINCT "Moneda") = 1 OR MAX("Moneda") = '') THEN SUM("Monto")
						WHEN COUNT(DISTINCT "Moneda") > 1 THEN 0   -- Multiples monedas no se pueden sumar montos
						ELSE 0
					END AS "MontoTotal",
					CASE
						WHEN COUNT(DISTINCT "Moneda") = 1 THEN MAX("Moneda")
						WHEN COUNT(DISTINCT "Moneda") > 1 THEN ''   -- Multiples monedas
						ELSE ''
					END AS "Moneda"
				FROM :v_CargaMasivaDetalle
				GROUP BY "IdInterno", "CodigoTipoCargaMasiva" ,"NombreArchivo"
			) AS CMD;
				
		--SELECT 'v_CargaMasiva', * FROM :v_CargaMasiva; -- REVISAR
			
		v_SocioNegocioInexistente = 
		    SELECT 
		        'SOCIO_NEGOCIO_NO_EXISTE' AS "Codigo", 
		        '' AS "RequiereAutorizacion", 
		        'Registro ' || TO_NVARCHAR("IdRegistro")  || 
		        ' Rut : ' || IFNULL("DocumentoIdentidad", 'N/A') || 
		        ' Cod Portafolio : ' || IFNULL("CodigoPortafolio", 'N/A') || 
		        ' Registro presenta error.' AS "Descripcion" 
		    FROM :v_CargaMasivaDetalle
		    WHERE "DocumentoIdentidad" NOT IN (
		    SELECT IDE."DocumentoIdentidad"		
		    FROM :v_CargaMasivaDetalle AS CMD 
				INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDE ON (IDE."DocumentoIdentidad" = CMD."DocumentoIdentidad") AND (IDE."Predeterminado" = 'S')
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (IDE."IdPersona" = PER."IdPersona")
				INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (PER."IdPersona" = CLI."IdPersona")
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (CLI."IdCliente" = POR."IdCliente" AND POR."CodigoPortafolio" = CMD."CodigoPortafolio")
		    
		    );

		-- Obtiene Homologacion de datos  	
		v_HomologacionCargaMasiva = 
			SELECT HOM."Campo", HOM."CodigoTipoCargaMasiva", HOM."ValorExterno", HOM."ValorOptimus"
			FROM :v_CargaMasiva AS CM
			INNER JOIN "BSCL.Tesoreria.BD::TES.Homologacion" AS HOM  ON (HOM."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva")
			WHERE HOM."NombreEntidad" = 'TES.CargaMasiva'
			AND   HOM."IdEmpresa" 	  = :p_IdEmpresa			
			AND   HOM."IdEstado" 	  = 'ACT';
		
		--SELECT 'HomologacionCargaMasiva', * FROM :v_HomologacionCargaMasiva; -- REVISAR
		
		--------------------------------------------   
		-- INSERTAR ENCABEZADO CARGA MASIVA
		--------------------------------------------
		
		INSERT INTO "BSCL.Tesoreria.BD::CAR.CargaMasiva"
		(
			"IdCargaMasiva",
			"CodigoTipoCargaMasiva",
			"IdEmpresa",
			"IdUsuario",
			"CantidadRegistros",
			"MontoTotal",
			"NombreArchivo",
		 	"FechaCarga",			
		 	"FechaProceso",
		  	"FechaIngresoRegistro",
			"IdEstado",
			"IdMoneda", 
			"IdConcepto"
		)
		SELECT 
			CM."IdCargaMasiva",
			CM."CodigoTipoCargaMasiva",
			:p_IdEmpresa,
			:p_IdUsuario,
			CM."CantidadRegistros",
			CM."MontoTotal",
			CM."NombreArchivo",
			:p_FechaCarga,
			'0001-01-01',  			-- FechaProceso null ya que aun no se procesa
			CURRENT_UTCTIMESTAMP, 	-- FechaIngresoRegistro
			'ING',  				-- Estado inicial ingresado
			COALESCE(HMO."ValorOptimus",'') AS "IdMoneda", 			
			'' AS "IdConcepto"  -- se llena en el complemento
		FROM :v_CargaMasiva AS CM
		LEFT JOIN :v_HomologacionCargaMasiva AS HMO ON (HMO."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva" AND HMO."Campo" = 'MONEDA' AND HMO."ValorExterno" = CM."Moneda");

	
		
		----------------------------------------------------------------------------------------
		-- INSERTAR DETALLE CARGA MASIVA SI ES "EGRESOS_POR_ABONOS_BCP_PAC" y "INGRESOS_POR_CARGOS_BCP_PAC"  (PARA PERU)
		----------------------------------------------------------------------------------------

		INSERT INTO "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle"
		(
			"IdCargaMasivaDetalle", "IdCargaMasiva", "IdTipoMovimiento", "DocumentoIdentidad", "Monto", "ObservacionValidacion", 
			"IdEstado", "IdPersona", "IdPortafolio", "CodigoPortafolio", "IdMoneda", "Glosa", 					
			-- Datos de origen de la entidad externa(archivo pac abonos bcp) 
			"Ori_TipoCuenta", "Ori_NumeroCuenta", "Ori_Moneda", "Ori_TipoDocumentoIdentidad", "Ori_DocumentoIdentidad"
		)
		SELECT 
			"BSCL.Tesoreria.BD.Secuencias::CAR.CargaMasivaDetalle".NEXTVAL AS "IdCargaMasivaDetalle",
			CM."IdCargaMasiva",
			CASE
				WHEN CM."CodigoTipoCargaMasiva" = 'EGRESOS_POR_ABONOS_BCP_PAC'  THEN 'MOVEGR'
				WHEN CM."CodigoTipoCargaMasiva" = 'INGRESOS_POR_CARGOS_BCP_PAC' THEN 'MOVING'
			END	 AS "IdTipoMovimiento",
			COALESCE(DI."DocumentoIdentidad",'') AS "DocumentoIdentidad",
			CMD."Monto",
			'OK',
			'ING',   -- Estado inicial Ingresado
			COALESCE(DI."IdPersona",0) AS "IdPersona",
			COALESCE(CLI."IdPortafolioDefecto",0) AS "IdPortafolio",
			'' AS "CodigoPortafolio", -- no viene en este archivo
			COALESCE(HMO."ValorOptimus",'') AS "IdMoneda",
			CASE
				WHEN CM."CodigoTipoCargaMasiva" = 'EGRESOS_POR_ABONOS_BCP_PAC'  THEN COALESCE(CMD."Glosa",'EGRESOS POR ABONOS BCP VIA PAC')
				WHEN CM."CodigoTipoCargaMasiva" = 'INGRESOS_POR_CARGOS_BCP_PAC' THEN COALESCE(CMD."Glosa",'INGRESOS POR CARGOS BCP VIA PAC')
			END	 AS "Glosa",
			CMD."TipoCuenta" AS "Ori_TipoCuenta",
			CMD."NumeroCuenta" AS "Ori_NumeroCuenta",
			CMD."TipoMoneda" AS "Ori_Moneda",
			CMD."TipoIDC" AS "Ori_TipoDocumentoIdentidad",
			CMD."NumeroIDC" AS "Ori_DocumentoIdentidad"					
		FROM :v_CargaMasivaDetalle AS CMD
		INNER JOIN :v_CargaMasiva AS CM ON (CM."IdInterno" = CMD."IdInterno")
		LEFT JOIN :v_HomologacionCargaMasiva AS HMO ON (HMO."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva" AND HMO."Campo" = 'MONEDA' AND HMO."ValorExterno"  = CMD."Moneda")
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON (
				DI."IdTipoDocumentoIdentidad" = (CASE 
													WHEN CMD."TipoIDC" = '1' THEN 'DNI' 
													WHEN CMD."TipoIDC" = '6' THEN 'RUC'  --- falta agregar carnet de extranjeria/Pasaporte
													ELSE ''
												END)
			AND DI."DocumentoIdentidad"       = LTRIM(RTRIM(CMD."NumeroIDC"))
		)
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdPersona" = DI."IdPersona")
		WHERE CMD."CodigoTipoCargaMasiva" IN ('EGRESOS_POR_ABONOS_BCP_PAC', 'INGRESOS_POR_CARGOS_BCP_PAC'); 



		--------------------------------------------------------------------------------------------
		-- INSERTAR DETALLE CARGA MASIVA SI ES CONCILIACION_RECAUDACION_BCP  (Solo para PERU)  
		--------------------------------------------------------------------------------------------		
		INSERT INTO "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle"
		(
			"IdCargaMasivaDetalle","IdCargaMasiva","IdTipoMovimiento","DocumentoIdentidad","Monto","ObservacionValidacion",
			"IdEstado","IdPersona","IdPortafolio","CodigoPortafolio", "IdMoneda","Glosa",
			-- Datos de origen de la entidad externa(archivo conciliacion bcp)
			"BCP_CON_CodigoCuenta",	"BCP_CON_CodigoMoneda",	"BCP_CON_NumeroCuenta",	"BCP_CON_IdentificacionDepositante",
			"BCP_CON_FechaTransaccion",	"BCP_CON_NumeroOperacion",	"BCP_CON_ReferenciaOperacion",	"BCP_CON_IndicadorExtorno",
			"BCP_CON_NumeroOperacionBD"
		)
		SELECT
			"BSCL.Tesoreria.BD.Secuencias::CAR.CargaMasivaDetalle".NEXTVAL AS "IdCargaMasivaDetalle",
			CM."IdCargaMasiva",
			(CASE
				WHEN CMD."IndicadorExtorno" = 'E' THEN 'MOVEGR' -- Si es extorno, genera un egreso
				ELSE 'MOVING'
			END) AS "IdTipoMovimiento",
			COALESCE(DI."DocumentoIdentidad",'') AS "DocumentoIdentidad",
			CMD."Monto",
			'OK',    -- ObservacionValidacion
			'ING',   -- Estado inicial Ingresado
			COALESCE(DI."IdPersona",0) AS "IdPersona",
			COALESCE(CLI."IdPortafolioDefecto",0) AS "IdPortafolio",
			'' AS "CodigoPortafolio",        -- no viene en este archivo
			COALESCE(HMO."ValorOptimus",'') AS "IdMoneda",
			'RECAUDACION CONCILIACION BCP POR CARGA BATCH' 	AS "Glosa",
			CMD."CodigoCuenta",
			CMD."CodigoMoneda",
			CMD."NumeroCuenta",
			CMD."IdentificacionDepositante",
			CMD."FechaTransaccion",
			CMD."NumeroOperacion",
			CMD."ReferenciaOperacion",
			CMD."IndicadorExtorno",
			CMD."NumeroOperacionBD"
		FROM :v_CargaMasivaDetalle AS CMD
		INNER JOIN :v_CargaMasiva AS CM ON (CM."IdInterno" = CMD."IdInterno")
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON (
				DI."IdTipoDocumentoIdentidad" = 'DNI' -- ?
			AND DI."DocumentoIdentidad" = CAST(CAST(CMD."IdentificacionDepositante" AS BIGINT) AS NVARCHAR)  -- Castea primero a Bigint para eliminar los ceros previos(00012969673) que viene en archivo
		)
		LEFT JOIN :v_HomologacionCargaMasiva AS HMO ON (HMO."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva" AND HMO."Campo" = 'MONEDA' AND HMO."ValorExterno" = CMD."Moneda")
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdPersona" = DI."IdPersona")
		WHERE CMD."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP';
		
		------------------------------------------------------------------
		-- INSERTAR DETALLE CARGA MASIVA SI ES CARGA_GENERICA_MOVIMIENTOS
		------------------------------------------------------------------
		INSERT INTO "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle"
		(
			"IdCargaMasivaDetalle",	"IdCargaMasiva", "IdTipoMovimiento", "DocumentoIdentidad", "Monto", "ObservacionValidacion",
			"IdEstado",	"IdPersona","IdPortafolio",	"CodigoPortafolio","IdMoneda","Glosa","PlanTipoInversion","ProductoAgrupador","RegimenTributario"
		)
		SELECT 
			"BSCL.Tesoreria.BD.Secuencias::CAR.CargaMasivaDetalle".NEXTVAL AS "IdCargaMasivaDetalle",
			CM."IdCargaMasiva",
			CMD."TipoMovimiento" AS "IdTipoMovimiento",
			CMD."DocumentoIdentidad",
			CMD."Monto",
			'OK',    -- ObservacionValidacion
			'ING',   -- Estado inicial Ingresado
			COALESCE(PER."IdPersona",0) AS "IdPersona",
			COALESCE(POR."IdPortafolio",0) AS "IdPortafolio",
			CMD."CodigoPortafolio" AS "CodigoPortafolio",
			CMD."TipoMoneda" AS "IdMoneda",  
			CMD."Glosa",
			CMD."PlanTipoInversion",
			CMD."ProductoAgrupador",
			CMD."RegimenTributario"
		FROM :v_CargaMasivaDetalle AS CMD
		INNER JOIN :v_CargaMasiva AS CM ON (CM."IdInterno" = CMD."IdInterno")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad" = CMD."DocumentoIdentidad")
		INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON (POR."IdPersona" = PER."IdPersona" AND RTRIM(LTRIM(UCASE(POR."CodigoPortafolio"))) = RTRIM(LTRIM(UCASE(CMD."CodigoPortafolio"))))
		LEFT JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS REGTRI ON REGTRI."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV" 
																				 AND REGTRI."IdProductoAgrupador"  = POR."IdProductoAgrupador"
		WHERE CMD."CodigoTipoCargaMasiva" = 'CARGA_GENERICA_MOVIMIENTOS'
		AND ((CMD."PlanTipoInversion" = '' ) OR (RTRIM(LTRIM(UCASE(REGTRI."CodigoPlan"))) = CMD."PlanTipoInversion"))
		AND ((CMD."ProductoAgrupador" = '' ) OR (RTRIM(LTRIM(UCASE(REGTRI."CodigoProducto"))) = CMD."ProductoAgrupador"))
		AND ((CMD."RegimenTributario" = '' ) OR (RTRIM(LTRIM(UCASE(REGTRI."Codigo"))) = CMD."RegimenTributario"));
			
		
		------------------------------------------------------------------
		-- INSERTAR DETALLE CARGA MASIVA SI ES "CARGA_RECAUDACION_SERVIPAG"
		------------------------------------------------------------------
		INSERT INTO "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle"
		(
			"IdCargaMasivaDetalle",	"IdCargaMasiva", "IdTipoMovimiento", "DocumentoIdentidad", "Monto", "ObservacionValidacion",
			"IdEstado",	"IdPersona","IdPortafolio",	"CodigoPortafolio","IdMoneda","Glosa"
		)
		SELECT				
			"BSCL.Tesoreria.BD.Secuencias::CAR.CargaMasivaDetalle".NEXTVAL AS "IdCargaMasivaDetalle",
			CM."IdCargaMasiva",
			CMD."TipoMovimiento" AS "IdTipoMovimiento",
			CMD."DocumentoIdentidad",
			CMD."Monto",
			'OK',    -- ObservacionValidacion
			'ING',   -- Estado inicial Ingresado
			COALESCE(PER."IdPersona",0) AS "IdPersona",
			COALESCE(CLI."IdPortafolioDefecto",0) AS "IdPortafolio",
			COALESCE(CLI."CodigoPortafolioDefecto",'') AS "CodigoPortafolio",
			'' AS "IdMoneda",  -- no viene en este archivo
			'RECAUDACION SERVIPAG - CARGA MASIVA' AS "Glosa"
		FROM :v_CargaMasivaDetalle AS CMD
		INNER JOIN :v_CargaMasiva AS CM ON (CM."IdInterno" = CMD."IdInterno")		
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad" = CMD."DocumentoIdentidad")
		LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."DocumentoIdentidad" = CMD."DocumentoIdentidad" AND CLI."TipoDocumentoIdentidad" = 'R')
		WHERE CMD."CodigoTipoCargaMasiva" = 'CARGA_RECAUDACION_SERVIPAG';

		
		
		
		--------------------------------------------------   
		-- VALIDA LA CM Y ACTUALIZA ESTADO DE INVALIDOS
		--------------------------------------------------

		-- Obtiene Id de las CM a validar
		v_CargaMasivaValidar = SELECT "IdCargaMasiva", "CodigoTipoCargaMasiva" FROM :v_CargaMasiva;
		CALL "BSCL.Tesoreria.BD.Servicios.CAR::ValidarCargaMasiva"
		(	 
			 p_CargaMasivaValidar  		=> :v_CargaMasivaValidar
			,p_out_CargaMasivaInvalida  => :v_CargaMasivaInvalida
			,p_out_ErroresValidacion    => :v_ErroresValidacion
		);


		-- Actualiza estado de CM que tienen registros invalidos
		UPDATE CM
		SET CM."IdEstado" = 'INV'
		FROM "BSCL.Tesoreria.BD::CAR.CargaMasiva" AS CM, :v_CargaMasivaInvalida AS CMI
		WHERE CM."IdCargaMasiva" = CMI."IdCargaMasiva";
		

		-- Actualiza estado de los registros de detalle invalidos
		UPDATE CMD
		SET  CMD."IdEstado"              = 'INV',
		     CMD."ObservacionValidacion" = EV."ObservacionValidacion"
		FROM "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle" AS CMD, :v_ErroresValidacion AS EV		
		WHERE CMD."IdCargaMasivaDetalle" = EV."IdCargaMasivaDetalle";

		SELECT COUNT(1) INTO v_cantSocioInexistente FROM :v_SocioNegocioInexistente;

		p_out_AdvertenciasDetalle = 
		    SELECT * FROM :p_out_AdvertenciasDetalle		    
		    UNION ALL
		    SELECT 
		        'HEADER' AS "Codigo", 
		        'N' AS "RequiereAutorizacion", 
		        'las siguientes operaciones no se ingresaron porque presentan errores' AS "Descripcion" 
		    FROM DUMMY 
		    WHERE :v_cantSocioInexistente > 0		    
		    UNION ALL
		    SELECT * FROM :v_SocioNegocioInexistente;

		SELECT COUNT(*) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
  	END;    
END;