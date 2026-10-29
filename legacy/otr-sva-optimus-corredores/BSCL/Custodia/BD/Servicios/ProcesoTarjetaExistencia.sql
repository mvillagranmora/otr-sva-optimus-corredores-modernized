SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::ProcesoTarjetaExistencia";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Proceso para la generación de datos asociado a la centralización de tarjeta existencia
--			   Definido para la cuenta propia sin diferenciar por portafolio
--			   
--  Autor: NRM
--  Fecha Creacion: 02/03/2017
--  Id Servicio : 
--  
"BSCL.Custodia.BD.Servicios::ProcesoTarjetaExistencia" (
	--+ Variables de entrada obligatorias
	 IN p_IdEmpresa						BIGINT
	,IN p_IdUsuario						BIGINT
	,IN p_IdModulo						NVARCHAR(3)	
	,IN p_FechaProceso					NVARCHAR(10)
	,IN p_UsaReglasNegocio				NVARCHAR(1)	-- S/N se usa en caso de servicios anidados.
	
	,OUT p_out_InformacionContador  	INT
	,OUT p_out_ErroresContador			INT
)   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS	   
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA  CONDITION;	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		IF (NOT IS_EMPTY("BSCL.Custodia.BD::GBL.ProcesoTarjetaExistencia_SRV_TablaCodigosMensajes")) THEN
			
			v_Mensajes = SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2" FROM "BSCL.Custodia.BD::GBL.ProcesoTarjetaExistencia_SRV_TablaCodigosMensajes";
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
			
			INSERT INTO "BSCL.Custodia.BD::GBL.ProcesoTarjetaExistencia_OUT_InformacionDetalle"							
			SELECT EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Custodia.BD::GBL.ProcesoTarjetaExistencia_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'OK';
			
			INSERT INTO "BSCL.Custodia.BD::GBL.ProcesoTarjetaExistencia_OUT_ErroresDetalle"
			SELECT EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Custodia.BD::GBL.ProcesoTarjetaExistencia_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'RECH';
		END IF;
		
		SELECT count(1) into p_out_InformacionContador FROM "BSCL.Custodia.BD::GBL.ProcesoTarjetaExistencia_OUT_InformacionDetalle";
		SELECT count(1) into p_out_ErroresContador FROM "BSCL.Custodia.BD::GBL.ProcesoTarjetaExistencia_OUT_ErroresDetalle";	
	END;
/*	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			INSERT INTO "BSCL.Custodia.BD::GBL.ProcesoTarjetaExistencia_OUT_ErroresDetalle" SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;
			
			SELECT count(1) into p_out_ErroresContador FROM "BSCL.Custodia.BD::GBL.ProcesoTarjetaExistencia_OUT_ErroresDetalle";										
		ELSE	
			RESIGNAL;
		END IF;
	END;
*/	
	p_out_InformacionContador	:= 0;
	p_out_ErroresContador		:= 0;

	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoValidacion 	NVARCHAR(40);
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoValidacion);
		IF :v_resultadoValidacion = 'RECH' THEN
			INSERT INTO "BSCL.Custodia.BD::GBL.ProcesoTarjetaExistencia_SRV_TablaCodigosMensajes"
			SELECT 0 as "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2",'RECH' as "Resultado" FROM :v_Errores;
			SIGNAL SALIDA_CONTROLADA;
		END IF;

	END; 
	END IF;
	
	
	--------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE v_FechaHoraGeneracion 		NVARCHAR(40);
		DECLARE v_FechaProcesoAnterior		NVARCHAR(10);
		DECLARE v_FechaProcesoAnteriorMas1	NVARCHAR(10);
		DECLARE v_NuevaFechaProceso			NVARCHAR(10);
		DECLARE v_NuevaFechaProcesoAnterior	NVARCHAR(10);
		DECLARE v_Index,v_DiasEntreFechas	BIGINT := 0;
		DECLARE v_IdClienteCuentaPropia		BIGINT;
		DECLARE v_PrecioSucio				NVARCHAR(1) := 'N';
		

-- Lee parámetro para obtencion de precio con o sin gastos    	
		SELECT COALESCE("Valor", 'N') INTO v_PrecioSucio
		FROM "BSCL.Custodia.BD::CUS.Configuracion"
		WHERE 	"IdEmpresa" 		= :p_IdEmpresa	AND
				"CodigoParametro"	= 'PrecioSucio';
	
		--Obtener cliente cuenta propia 174950
		SELECT CLI."IdCliente" INTO v_IdClienteCuentaPropia
		  FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" AS EMP
		 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdPersona" = EMP."IdPersona"
		 WHERE EMP."IdEmpresa" = :p_IdEmpresa;
	
		--Obtener fecha sistema	
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
			p_IdEmpresa 			=> :p_IdEmpresa,
			p_out_FechaHoraSistema	=> :v_FechaHoraGeneracion
		);
		
		--Obtener fecha ultima ejecucion proceso 
		SELECT coalesce(MAX("FechaProceso"),p_FechaProceso) INTO v_FechaProcesoAnterior
		  FROM "BSCL.Custodia.BD::CUS.TarjetaExistencia" 
		 WHERE "FechaProceso" < :p_FechaProceso;
	
			
--		IF (COALESCE(:v_FechaProcesoAnterior,'0001-01-01') = '0001-01-01') THEN
		IF (COALESCE(:v_FechaProcesoAnterior,'0001-01-01') = '0001-01-01') OR ((COALESCE(:v_FechaProcesoAnterior,'0001-01-01'))= p_FechaProceso)THEN
			SELECT COALESCE(MIN("FechaOperacion"),p_FechaProceso)  INTO v_FechaProcesoAnterior
			  FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
			 WHERE CAR."IdCliente" = :v_IdClienteCuentaPropia;
			 
			v_FechaProcesoAnteriorMas1 	:= v_FechaProcesoAnterior;
		ELSE
			v_FechaProcesoAnteriorMas1 	:= ADD_DAYS (TO_DATE (:v_FechaProcesoAnterior, 'YYYY-MM-DD'), 1);
		END IF;
		
		v_DiasEntreFechas	:= DAYS_BETWEEN (TO_DATE (:v_FechaProcesoAnterior, 'YYYY-MM-DD'), TO_DATE(:p_FechaProceso, 'YYYY-MM-DD'));



		/*************************** LECTURA MOVIMIENTOS DE CARTERA ********************************/
		v_MovimientosCartera =
		----------------------------------------------------------------Datos de la cartera del día del proceso 
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FechaOperacion"						AS "FechaOperacion"
				,CAR."IdCartera"
				,CAR."FolioCartera"
				,CAR."IdConcepto"	
				,CAR."IdClasificacionTipoInstrumento"
				,CAR."IdTipoInstrumento"
				,CAR."IdInstrumento"
				,CAR."IdMonedaSistema"						AS "IdMoneda"					
				,CAR."Cantidad"
				,CASE v_PrecioSucio WHEN 'N' THEN CAR."PrecioCompra" ELSE CAR."PrecioSucio" END AS "PrecioAdquisicion" 
				,CAR."MontoCompra"							AS "Monto"					
				,CAR."IdEstado"
				,CTE."Tipo"
				,CTE."Orden"
		  FROM "BSCL.Custodia.BD::CUS.ConfiguracionTarjetaExistencia" AS CTE
		 INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		 		 ON CAR."IdClasificacionTipoInstrumento"	= CTE."IdClasificacionTipoInstrumento"
		 		AND (CTE."IdConcepto" = '' OR  CAR."IdConcepto" = CTE."IdConcepto")
		 		AND CTE."Tipo"								= 'INGRESO'	 		
		 WHERE CAR."IdEstado" IN ('VIG','LIQ')
		   AND CAR."IdEmpresa"		= :p_IdEmpresa
		   AND CAR."IdCliente"		= :v_IdClienteCuentaPropia
		   AND CAR."FechaOperacion"	BETWEEN :v_FechaProcesoAnteriorMas1 AND :p_FechaProceso	   
		UNION ALL
		----------------------------------------------------------------Datos de la cartera del día del proceso
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."EGRFechaOperacion"					AS "FechaOperacion"
				,CAR."IdCartera"
				,CAR."FolioCartera"
				,CAR."EGRIdConcepto"						AS "IdConcepto"
				,CAR."IdClasificacionTipoInstrumento"
				,CAR."IdTipoInstrumento"
				,CAR."IdInstrumento"
				,CAR."IdMonedaInstrumento"		 			AS "IdMoneda"
				,CAR."EGRCantidad"				 			AS "Cantidad"
				,CASE v_PrecioSucio WHEN 'N' THEN CAR."EGRPrecio" ELSE CAR."EGRPrecioSucio" END AS "PrecioAdquisicion"
				,CAR."EGRMonto" 							AS "Monto"
				,CAR."IdEstado"
				,CTE."Tipo"
				,CTE."Orden"
		  FROM "BSCL.Custodia.BD::CUS.ConfiguracionTarjetaExistencia" AS CTE
		 INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		 		 ON CAR."IdClasificacionTipoInstrumento"	= CTE."IdClasificacionTipoInstrumento"
		 		AND (CTE."IdConcepto" = '' OR  CAR."EGRIdConcepto" = CTE."IdConcepto")		 		
		 		AND CTE."Tipo"								= 'EGRESO'	 					 		
		 WHERE CAR."IdEstado" IN ('RPA','LIQ')
		   AND CAR."IdEmpresa"			= :p_IdEmpresa
		   AND CAR."IdCliente"			= :v_IdClienteCuentaPropia
		   AND CAR."EGRFechaOperacion"	BETWEEN :v_FechaProcesoAnteriorMas1 AND :p_FechaProceso;

		   

		FOR v_Index IN 0 .. :v_DiasEntreFechas DO
			v_NuevaFechaProceso 		:= ADD_DAYS (TO_DATE (:v_FechaProcesoAnteriorMas1 , 'YYYY-MM-DD'), :v_Index);
			v_NuevaFechaProcesoAnterior	:= ADD_DAYS (TO_DATE (:v_NuevaFechaProceso , 'YYYY-MM-DD'), -1 );
			
			--/*************************** UNION DE TODOS LOS MOVIMIENTOS Y OBTENCION DE SALDOS ********************************
			v_MovimientosProceso =
			WITH
				MovimientoProcesoAnterior as (
					----------------------------------------------------------------Ultimo Ingreso o Egreso del proceso anterior
					SELECT   "IdCliente"
							,"IdInstrumento"
							,"SaldoCantidad"
							,"SaldoMontoAPrecioPromedioPonderado"
					  FROM "BSCL.Custodia.BD::CUS.TarjetaExistencia"
					 WHERE "FechaProceso" 	= :v_NuevaFechaProcesoAnterior
					   AND "UltimoRegistro"	= 'S'
					   AND "IdEstado"		= 'VIG'
				),
				MovimientosProceso as (
					SELECT 	"IdCliente"
							,"IdPortafolio"
							,"FechaOperacion"								
							,"FolioCartera"
							,"IdConcepto"	
							,"IdClasificacionTipoInstrumento"
							,"IdTipoInstrumento"
							,"IdInstrumento"
							,"IdMoneda"					
							,"Cantidad"
							,"PrecioAdquisicion"
							,"Monto"					
							,"IdEstado"
							,"Tipo"
							,"Orden"
					  FROM :v_MovimientosCartera
					 WHERE "FechaOperacion" = :v_NuevaFechaProceso
				)
			  SELECT  ROW_NUMBER() OVER 
		 			(ORDER BY MP."IdCliente",MP."FechaOperacion",MP."IdInstrumento",MP."Tipo" desc,"Orden") AS "OrdenRegistros"
			 		,MP."IdCliente"
					,MP."IdPortafolio"
					,MP."FechaOperacion"
					,MP."FolioCartera"
					,MP."IdConcepto"	
					,MP."IdClasificacionTipoInstrumento"
					,MP."IdTipoInstrumento"
					,MP."IdInstrumento"
					,MP."IdMoneda"
					,CASE WHEN MP."Tipo" = 'INGRESO' THEN MP."Cantidad" ELSE 0 END AS "CantidadIngreso"
					,CASE WHEN MP."Tipo" = 'EGRESO' THEN MP."Cantidad" ELSE 0 END AS "CantidadEgreso"
					,COALESCE(MPA."SaldoCantidad",0) +
					 SUM(CASE WHEN MP."Tipo" = 'INGRESO' THEN MP."Cantidad" ELSE -1*MP."Cantidad" END) 
						over (PARTITION BY MP."IdCliente",MP."FechaOperacion",MP."IdInstrumento"
								  ORDER BY MP."IdCliente",MP."FechaOperacion",MP."IdInstrumento",MP."Tipo" desc,MP."Orden"
								   ROWS BETWEEN unbounded preceding and current row)
					  AS "SaldoCantidad"
					,"PrecioAdquisicion"
					,CASE WHEN MP."Tipo" = 'INGRESO' THEN MP."Monto" ELSE 0 END AS "MontoIngreso"
					,CASE WHEN MP."Tipo" = 'EGRESO' THEN MP."Monto" ELSE 0 END AS "MontoEgreso"
					,COALESCE(MPA."SaldoMontoAPrecioPromedioPonderado",0) + 
					 SUM(CASE WHEN MP."Tipo" = 'INGRESO' THEN MP."Monto" ELSE -1*MP."Monto" END) 
						over (PARTITION BY MP."IdCliente",MP."FechaOperacion",MP."IdInstrumento"
								  ORDER BY MP."IdCliente",MP."FechaOperacion",MP."IdInstrumento",MP."Tipo" desc,MP."Orden"
								   ROWS BETWEEN unbounded preceding and current row)
					  AS "SaldoMontoPrecioAdquisicion"
					,MP."IdEstado"
					,MP."Tipo"
					,MP."Orden"
			   FROM MovimientosProceso AS MP
			   LEFT JOIN MovimientoProcesoAnterior AS MPA
			   		  ON MPA."IdCliente" 		= MP."IdCliente"
			   		 AND MPA."IdInstrumento" 	= MP."IdInstrumento";
			
			
			--/*************************** OBTENCION DE PRECIO PROMEDIO PONDERADO ********************************
			v_PrecioPromedioPonderado =
			WITH
				MaxIngreso as (
					SELECT "IdCliente","FechaOperacion","IdInstrumento",MAX("OrdenRegistros") as "OrdenRegistros"
					  FROM :v_MovimientosProceso
					 WHERE "Tipo" = 'INGRESO'
					 GROUP BY "IdCliente","FechaOperacion","IdInstrumento"
				)
		    SELECT MC."OrdenRegistros",
		    	   MC."IdCliente",
		    	   MC."FechaOperacion",
		    	   MC."IdInstrumento",
		    	   "BSCL.Plataforma.BD.Utilitarios::FormatearNumero"((MC."SaldoMontoPrecioAdquisicion"/MC."SaldoCantidad"),MF."PrecioDecimales",MF."PrecioTrucaRedondea") AS "PrecioPromedioPonderado"
		      FROM :v_MovimientosProceso AS MC     
		     INNER JOIN MaxIngreso AS MX
					 ON MX."IdCliente" 		= MC."IdCliente"
					AND MX."FechaOperacion" = MC."FechaOperacion"
					AND MX."IdInstrumento"	= MC."IdInstrumento"
					AND MX."OrdenRegistros" = MC."OrdenRegistros"					
			 INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
			 		 ON MF."IdMoneda" 			= MC."IdMoneda" 
			 		AND MF."IdUsoMonedaFormato" = 'GEN'
			 		AND MF."IdTipoInstrumento" 	= ''
			 		AND MF."IdTipoOperacion" 	= '';
			
			
			--/*************************** VALORIZACION DE EGRESOS POR NUEVO PRECIO PROMEDIO PONDERADO ********************************
			v_CarteraValorizadaPrecioPromedioPonderado =
			WITH
				UltimoRegistro as (
					SELECT "IdCliente","FechaOperacion","IdInstrumento",MAX("OrdenRegistros") as "OrdenRegistros"
					  FROM :v_MovimientosProceso
					 GROUP BY "IdCliente","FechaOperacion","IdInstrumento"
				),
				CalculoPerdidaGanancia as (
					SELECT 	 MC."OrdenRegistros"
							,MC."IdCliente"
							,MC."IdPortafolio"
							,MC."FechaOperacion"
							,MC."Tipo"
							,MC."FolioCartera"
							,MC."IdConcepto"	
							,MC."IdClasificacionTipoInstrumento"
							,MC."IdTipoInstrumento"
							,MC."IdInstrumento"
							,MC."IdMoneda"
							,MC."CantidadIngreso"
							,MC."CantidadEgreso"
							,MC."SaldoCantidad"
							,MC."PrecioAdquisicion"
							,MC."MontoIngreso"
							,MC."MontoEgreso"
							,MC."SaldoMontoPrecioAdquisicion"
							,PPP."PrecioPromedioPonderado" AS "UltimoPrecioPromedioPonderado"
							,CASE WHEN MC."Tipo" = 'INGRESO' THEN (MC."SaldoMontoPrecioAdquisicion"/MC."SaldoCantidad")
								  WHEN MC."Tipo" = 'EGRESO' THEN PPP."PrecioPromedioPonderado"
							  END "PrecioPromedioPonderado"
							,CASE WHEN MC."Tipo" = 'INGRESO' THEN MC."CantidadIngreso" * COALESCE(PPP."PrecioPromedioPonderado",0)
								  WHEN MC."Tipo" = 'EGRESO' THEN MC."CantidadEgreso" * COALESCE(PPP."PrecioPromedioPonderado",0)
							  END AS "MontoAPrecioPromedioPonderado"							  
							,"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(
							 	(MC."SaldoCantidad" * COALESCE(PPP."PrecioPromedioPonderado",0)),MF."MontoDecimales",MF."MontoTrucaRedondea") AS "SaldoMontoAPrecioPromedioPonderado"
--							,MC."SaldoMontoPrecioAdquisicion" - 
--							 "BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(
--							 	(MC."SaldoCantidad" * COALESCE(PPP."PrecioPromedioPonderado",0)),MF."MontoDecimales",MF."MontoTrucaRedondea") AS "PerdidaGarancia"
-- Error cálculo Utilidad / Perdida 
							, 
							 "BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(
							 	(MC."SaldoCantidad" * COALESCE(PPP."PrecioPromedioPonderado",0)),MF."MontoDecimales",MF."MontoTrucaRedondea") - MC."SaldoMontoPrecioAdquisicion" AS "PerdidaGarancia"

-- Error cálculo Utilidad / Perdida
							 	,CASE WHEN MC."FechaOperacion" <> :v_NuevaFechaProceso THEN 'N/A'
								  WHEN MC."Tipo" = 'INGRESO' AND MC."OrdenRegistros" = PPP."OrdenRegistros" THEN 'S' 
								  ELSE 'N' END "UltimoIngreso"
							,CASE WHEN MC."FechaOperacion" <> :v_NuevaFechaProceso THEN 'N/A'
								  WHEN MC."OrdenRegistros" = UR."OrdenRegistros" THEN 'S'						  
								  ELSE 'N' END "UltimoRegistro"
					  FROM :v_MovimientosProceso AS MC
					 INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
					 		 ON MF."IdMoneda" 			= MC."IdMoneda" 
					 		AND MF."IdUsoMonedaFormato" = 'GEN'
					 		AND MF."IdTipoInstrumento" 	= ''
					 		AND MF."IdTipoOperacion" 	= ''
					  LEFT JOIN :v_PrecioPromedioPonderado AS PPP
					 		 ON MC."IdCliente" 		= PPP."IdCliente"
							AND MC."IdInstrumento"	= PPP."IdInstrumento"
					  LEFT JOIN UltimoRegistro AS UR
					 		 ON MC."IdCliente" 		= UR."IdCliente"
							AND MC."FechaOperacion" = UR."FechaOperacion"
							AND MC."IdInstrumento"	= UR."IdInstrumento"
							AND MC."OrdenRegistros" = UR."OrdenRegistros"
				)
			SELECT 	 CPG."IdCliente"
					,CPG."IdPortafolio"
					,CPG."FechaOperacion"
					,CPG."Tipo"
					,CPG."FolioCartera"
					,CPG."IdConcepto"	
					,CPG."IdClasificacionTipoInstrumento"
					,CPG."IdTipoInstrumento"
					,CPG."IdInstrumento"
					,CPG."IdMoneda"
					,CPG."CantidadIngreso"
					,CPG."CantidadEgreso"
					,CPG."SaldoCantidad"
					,CPG."PrecioAdquisicion"
					,CPG."MontoIngreso"
					,CPG."MontoEgreso"
					,CPG."SaldoMontoPrecioAdquisicion"
					,CPG."PrecioPromedioPonderado"
					,CPG."MontoAPrecioPromedioPonderado"
					,CPG."SaldoMontoAPrecioPromedioPonderado"
					,CASE WHEN "PerdidaGarancia" < 0 THEN "PerdidaGarancia" ELSE 0 END AS "Perdida"					
					,CASE WHEN "PerdidaGarancia" > 0 THEN "PerdidaGarancia"  ELSE 0 END AS "Ganancia"
					,SUM(CPG."PerdidaGarancia") 
						over (PARTITION BY CPG."IdCliente",CPG."FechaOperacion"
								  ORDER BY CPG."IdCliente",CPG."FechaOperacion"
								   ROWS BETWEEN unbounded preceding and current row)
					  AS "Acumulado"			
					,CPG."UltimoIngreso"
					,CPG."UltimoRegistro"
					,'N' AS "RegistroDeArrastre"
			  FROM CalculoPerdidaGanancia AS CPG
			 UNION ALL
			 SELECT  "IdCliente"
					,"IdPortafolio"
					,"FechaOperacion"
					,"Tipo"
					,"FolioCartera"
					,"IdConcepto"
					,"IdClasificacionTipoInstrumento"
					,"IdTipoInstrumento"
					,"IdInstrumento"
					,"IdMoneda"
					,"CantidadIngreso"
					,"CantidadEgreso"
					,"SaldoCantidad"
					,"PrecioAdquisicion"
					,"MontoIngreso"
					,"MontoEgreso"
					,"SaldoMontoPrecioAdquisicion"
					,"PrecioPromedioPonderado"
					,"MontoAPrecioPromedioPonderado"
					,"SaldoMontoAPrecioPromedioPonderado"
					,"Perdida"
					,"Ganancia"
					,"Acumulado"
					,"UltimoIngreso"
					,"UltimoRegistro"
					,'S' AS "RegistroDeArrastre"
			  FROM "BSCL.Custodia.BD::CUS.TarjetaExistencia"
			 WHERE "FechaProceso" 	= :v_NuevaFechaProcesoAnterior
			   AND "UltimoRegistro"	= 'S'
			   AND "IdEstado"		= 'VIG'
			   AND "IdInstrumento" NOT IN (SELECT "IdInstrumento" FROM :v_MovimientosCartera WHERE "FechaOperacion" = :v_NuevaFechaProceso);
			
			
			--/*************************** REGISTRAR RESULTADO DEL PROCESO ********************************
			UPDATE "BSCL.Custodia.BD::CUS.TarjetaExistencia"
			   SET "IdEstado" = 'MOD'
			 WHERE "FechaProceso" = :v_NuevaFechaProceso;
		
			INSERT INTO "BSCL.Custodia.BD::CUS.TarjetaExistencia"(
				 "IdTarjetaExistencia"
				,"IdEmpresa"
				,"FechaProceso"
				,"FechaSistema"
				,"IdCliente"
				,"IdPortafolio"
				,"FechaOperacion"
				,"Tipo"
				,"FolioCartera"
				,"IdConcepto"
				,"IdClasificacionTipoInstrumento"
				,"IdTipoInstrumento"
				,"IdInstrumento"
				,"IdMoneda"
				,"CantidadIngreso"
				,"CantidadEgreso"
				,"SaldoCantidad"
				,"PrecioAdquisicion"
				,"MontoIngreso"
				,"MontoEgreso"
				,"SaldoMontoPrecioAdquisicion"
				,"PrecioPromedioPonderado"
				,"MontoAPrecioPromedioPonderado"
				,"SaldoMontoAPrecioPromedioPonderado"
				,"Perdida"
				,"Ganancia"
				,"Acumulado"
				,"UltimoIngreso"
				,"UltimoRegistro"
				,"RegistroDeArrastre"
				,"IdEstado"	
			)
			SELECT   "BSCL.Custodia.BD.Secuencias::TarjetaExistencia".NEXTVAL
					,:p_IdEmpresa 			AS "IdEmpresa"
					,:v_NuevaFechaProceso 	AS "FechaProceso"
					,:v_FechaHoraGeneracion AS "FechaSistema"
					,CVPP."IdCliente"
					,CVPP."IdPortafolio"
					,CVPP."FechaOperacion"
					,CVPP."Tipo"
					,CVPP."FolioCartera"			
					,CVPP."IdConcepto"
					,CVPP."IdClasificacionTipoInstrumento"
					,CVPP."IdTipoInstrumento"
					,CVPP."IdInstrumento"
					,CVPP."IdMoneda"					
					,CVPP."CantidadIngreso"
					,CVPP."CantidadEgreso"
					,"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(CVPP."SaldoCantidad",MF."CantidadDecimales",MF."CantidadTrucaRedondea") AS "SaldoCantidad"
					,CVPP."PrecioAdquisicion"
					,CVPP."MontoIngreso"
					,CVPP."MontoEgreso"
					,"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(COALESCE(CVPP."SaldoMontoPrecioAdquisicion",0),MF."MontoDecimales",MF."MontoTrucaRedondea") AS "SaldoMontoPrecioAdquisicion"					
					,"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(COALESCE(CVPP."PrecioPromedioPonderado",0),MF."PrecioDecimales",MF."PrecioTrucaRedondea") AS "PrecioPromedioPonderado"					
					,"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(COALESCE(CVPP."MontoAPrecioPromedioPonderado",0),MF."MontoDecimales",MF."MontoTrucaRedondea") AS "MontoAPrecioPromedioPonderado"
					,"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(COALESCE(CVPP."SaldoMontoAPrecioPromedioPonderado",0),MF."MontoDecimales",MF."MontoTrucaRedondea") AS "SaldoMontoAPrecioPromedioPonderado"
					,"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(ABS(CVPP."Perdida"),MF."MontoDecimales",MF."MontoTrucaRedondea") AS "Perdida"
					,"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(ABS(CVPP."Ganancia"),MF."MontoDecimales",MF."MontoTrucaRedondea") AS "Ganancia"

					,"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(CVPP."Acumulado",MF."MontoDecimales",MF."MontoTrucaRedondea") AS "Acumulado"
					,CVPP."UltimoIngreso"
					,CVPP."UltimoRegistro"
					,CVPP."RegistroDeArrastre"
					,'VIG'
			  FROM :v_CarteraValorizadaPrecioPromedioPonderado CVPP
			 INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
			 		 ON MF."IdMoneda" = CVPP."IdMoneda" 
			 		AND MF."IdUsoMonedaFormato" = 'GEN'
			 		AND MF."IdTipoInstrumento" = ''
			 		AND MF."IdTipoOperacion" = '';
		END FOR;
		
		---------------------------------------------------RESULTADO
		INSERT INTO "BSCL.Custodia.BD::GBL.ProcesoTarjetaExistencia_SRV_TablaCodigosMensajes"
		SELECT  0 as "IdInterno"
			   ,'UI_TARJETA_EXISTENCIA_EJECUTADO_EXITO' as "Codigo"
			   ,'FechaProceso' as "CodigoDato1"
			   ,:p_FechaProceso as "ValorDato1"
			   ,'' as "CodigoDato2"
			   ,'' as "ValorDato2"
			   ,'OK' as "Resultado"
		  FROM DUMMY;
		
		SIGNAL SALIDA_CONTROLADA;
	END;
END;
GO