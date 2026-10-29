SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ConsultarCartolaDeMovimiento";
GO

CREATE PROCEDURE
-- 
--  Proposito: Realizar consulta de cartola de movimientos 
--  Autor: NRM - BCS
--  Fecha Creacion: 21/08/2015
--  
-- TABLAS DE SALIDA
-- P_OUT_ERRORESDETALLE = BSCL.CuentaInversion.BD::CTA.ConsultarCartolaDeMovimiento_OUT_ErroresDetalle
--
"BSCL.CuentaInversion.BD.Servicios::ConsultarCartolaDeMovimiento" (
	-- Parametros entrada comunes en todos los servicios publicos
    IN p_IdEmpresa						BIGINT,
    IN p_IdUsuario						BIGINT,
    IN p_IdModulo						NVARCHAR(3),
	IN p_UsaReglasNegocio				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	
    -- Parametros entrada del servicio publico
	IN p_IdCuentaInversion				BIGINT,
	IN p_IdCliente						BIGINT,
    IN p_IdPortafolio					BIGINT,
    IN p_IdMoneda						NVARCHAR(3),
    IN p_IdEstado						NVARCHAR(3),
	IN p_FechaDesde						NVARCHAR(10),
    IN p_FechaHasta						NVARCHAR(10),
    IN p_SaldoPorFechaEfectiva			NVARCHAR(1) 	DEFAULT 'S',	-- S: Retorna saldo según fecha efectiva; N: Retorna saldo según fecha movimiento
    IN p_Validaciones					NVARCHAR(1) 	DEFAULT 'S',	-- S: Pasara por el procesos de validacion; N: No pasara por el procesos de validacion
    IN p_IdProductoAgrupador			BIGINT			DEFAULT NULL,
	IN p_IdMatrizAgrupador              BIGINT          DEFAULT NULL,
	IN p_EsADC                          NVARCHAR(1)     DEFAULT NULL,
	-- Parametros salida del servicio publico
    OUT p_out_CartolaDeMovimiento		"BSCL.CuentaInversion.BD.Tipos::CTA.CartolaDeMovimientos" DEFAULT EMPTY,
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_ErroresContador			INT
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
		
		IF (NOT IS_EMPTY("BSCL.CuentaInversion.BD::CTA.ConsultarCartolaDeMovimiento_SRV_TablaCodigosMensajes")) THEN
			
			v_Mensajes = SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2" FROM "BSCL.CuentaInversion.BD::CTA.ConsultarCartolaDeMovimiento_SRV_TablaCodigosMensajes";
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
			
			INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConsultarCartolaDeMovimiento_OUT_ErroresDetalle"
			SELECT TCM."IdInterno",EM."Codigo",EM."Descripcion"
			  FROM :v_erroroutmsg EM
			 INNER JOIN "BSCL.CuentaInversion.BD::CTA.ConsultarCartolaDeMovimiento_SRV_TablaCodigosMensajes" TCM
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'RECH';
			
		END IF;
		
		SELECT count(1) into p_out_ErroresContador FROM "BSCL.CuentaInversion.BD::CTA.ConsultarCartolaDeMovimiento_OUT_ErroresDetalle";
		
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		
		DECLARE v_errorExt 	NVARCHAR(40);
		
		IF ::SQL_ERROR_CODE > 10000 THEN
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConsultarCartolaDeMovimiento_OUT_ErroresDetalle" SELECT 0 as "IdInterno",v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;
			SIGNAL SALIDA_CONTROLADA;										
		ELSE
			RESIGNAL;
		END IF;
		
	END;
	
	p_out_ErroresContador := 0;
	
	-----------------------------------------------------
	--Validaciones básicas
	-----------------------------------------------------
	BEGIN
		
		DECLARE v_resultadoRegla       NVARCHAR(40);
		DECLARE v_erroroutmsg_Existe   NVARCHAR(520);
		
		IF :p_Validaciones = 'S' THEN
		
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
			IF :v_resultadoRegla = 'RECH' THEN
				INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConsultarCartolaDeMovimiento_SRV_TablaCodigosMensajes"
				SELECT 0 as "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2",'RECH' as "Resultado" FROM :v_Errores;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			CALL "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoConsultarCartolaDeMovimiento"(1,p_IdEmpresa,p_IdCuentaInversion,p_IdCliente,p_IdPortafolio,p_IdMoneda,p_IdEstado,p_FechaDesde,p_FechaHasta);
			INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConsultarCartolaDeMovimiento_SRV_TablaCodigosMensajes"
			SELECT R."IdInterno",R."Codigo",R."CodigoDato1",R."ValorDato1",R."CodigoDato2",R."ValorDato2",R."Resultado"
			  FROM "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoConsultarCartolaDeMovimiento_OUT_Resultado" as R WHERE "Resultado" <> 'OK';
			
			IF (NOT IS_EMPTY("BSCL.CuentaInversion.BD::CTA.ConsultarCartolaDeMovimiento_SRV_TablaCodigosMensajes")) THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		
		END IF;
		
	END;
	
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
		
		DECLARE v_Saldo_Inicial         DECIMAL(26,8);
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN		
			
			INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConsultarCartolaDeMovimiento_SRV_TablaCodigosMensajes"
			SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2",'RECH' as "Resultado" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
			
		END;
		
        --) Recuperar Id de la cuenta inversion	y busca Saldo Inicial desde la Cuenta Inversion
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_IN_CuentaInversionAConsultar"
		SELECT DISTINCT
			CuentaInversion."IdPortafolio"
		   ,CuentaInversion."IdMoneda"
		FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CuentaInversion
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CuentaInversion."IdPortafolio" AND Portafolio."IdCliente" = CuentaInversion."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		WHERE 
			(:p_IdCuentaInversion IS NULL OR CuentaInversion."IdCuentaInversion" = :p_IdCuentaInversion)
		AND (
					(:p_IdCliente			IS NULL OR CuentaInversion."IdCliente"    = :p_IdCliente)
				AND (:p_IdPortafolio		IS NULL OR CuentaInversion."IdPortafolio" = :p_IdPortafolio)
				AND (:p_IdMoneda			IS NULL OR CuentaInversion."IdMoneda"     = :p_IdMoneda)
				AND (:p_IdProductoAgrupador	IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
			);
		
		CALL "BSCL.CuentaInversion.BD.Servicios::SaldoCuentaInversion" (
			 p_IdEmpresa					=> p_IdEmpresa
			,p_FechaDesde					=> p_FechaDesde
		    ,p_FechaHasta					=> p_FechaHasta
		    ,p_SoloSaldoNegativo			=> 'N'
		);
		
		SELECT MAX (CASE WHEN p_SaldoPorFechaEfectiva = 'S' THEN "SaldoEfectivoInicial" ELSE "SaldoMovimientoInicial" END)
		INTO v_Saldo_Inicial
		FROM "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_OUT_SaldoInicialFinal";
		
		--) Retornar Cartola de movimiento
		v_CartolaDeMovimiento =
		SELECT
			CM."IdCargoAbonoMovimiento"
		   ,CM."FechaSistema"
		   ,CM."FechaIngresoRegistro"
		   ,CM."IdCuentaInversion"
		   ,CM."CodigoCuentaInversion"
		   ,CM."NombreCuentaInversion"
		   ,CM."IdCliente"
		   ,CM."NombreCliente"
		   ,CM."IdPortafolio"
		   ,CM."IdMoneda"
		   ,CM."CodigoMoneda"
		   ,CM."NombreMoneda"
		   ,CM."SimboloMoneda"
		   ,CM."DecimalesMoneda"
		   ,CM."IdTipoMovimiento"
		   ,CM."TipoMovimiento"
		   ,CM."NombreTipoMovimiento"
		   ,CM."IdConcepto"
		   ,CM."NombreConcepto"
		   ,CM."OrigenConcepto"
		   ,CM."Descripcion"
		   ,CM."IdCondicionDeLiquidacion"
		   ,CM."CodigoCondicionDeLiquidacion"
		   ,CM."NombreCondicionDeLiquidacion"
		   ,CM."PlazoCondicionDeLiquidacion"
		   ,CM."FechaEfectivaMovimiento"
		   ,CM."Monto"
		   ,CM."SaldoPorFechaEfectiva"
		   ,CM."SaldoPorFechaMovimiento"
		   ,CM."IdModulo"
		   ,CM."IdTipoProducto"
		   ,CM."CodigoTipoProducto"
		   ,CM."NombreTipoProducto"
		   ,CM."IdTipoOperacion"
		   ,CM."CodigoTipoOperacion"
		   ,CM."NombreTipoOperacion"
		   ,CM."AccionTipoOperacion"
		   ,CM."IdOperacion"
		   ,CM."FechaOperacion"
		   ,CM."FechaLiquidacionOperacion"
		   ,CM."FechaVencimientoOperacion"
		   ,CM."IdMonedaOperacion"
		   ,CM."MontoOperacion"
		   ,CM."TipoCambioOperacion"
		   ,CM."IdTipoInstrumento"
		   ,CM."NombreTipoInstrumento"
		   ,CM."IdClasificacionTipoInstrumento"
		   ,CM."NombreClasificacionTipoInstrumento"
		   ,CM."IdInstrumento"
		   ,CM."Cantidad"
		   ,CM."NemotecnicoOriginal"
		   ,CM."EsCargoAbonoPorTraspaso"
		   ,CM."EsCargoAbonoPorTraspasoTraducido"
		   ,CM."IdCargoAbonoRelacionadoTraspaso"
		   ,CM."NombreClienteRelacionado"
		   ,CM."CodigoPortafolioRelacionado"
		   ,CM."IdTipoComprobante"
		   ,CM."CodigoTipoComprobante"
		   ,CM."NombreTipoComprobante"
		   ,CM."IdComprobante"
		   ,CM."FolioComprobante"
		   ,CM."EsVentaCorta"
		   ,CM."FechaComprobante"
		   ,CM."IdComprobanteDetalle"
		   ,CM."IdPersonaPagoCobro"
		   ,CM."EsCertificadoAutomatico"
		   ,CM."IdUsuarioCertificador"
		   ,CM."FechaCertificacion"
		   ,CM."IdContraparte"
		   ,CM."EsIndicadorBilateral"
		   ,CM."IdEmpresa"
		   ,CM."CodigoEmpresa"
		   ,CM."NombreEmpresa"
		   ,CM."EsIngresoManual"
		   ,CM."IdEvento"
		   ,CM."IdUsuario"
		   ,CM."NombreUsuario"
		   ,CM."IdEstado"
		   ,CM."NombreEstado"
		   ,CM."NombreUsuarioCert"
		   ,CASE
		   		WHEN CM."IdTipoMovimiento" = 'MOVCAR' THEN "Monto"
		   		WHEN CM."IdTipoMovimiento" = 'MOVABN' THEN 0
			END AS "Cargo"
		   ,CASE
		   		WHEN CM."IdTipoMovimiento" = 'MOVCAR' THEN 0
		   		WHEN CM."IdTipoMovimiento" = 'MOVABN' THEN "Monto"
			END AS "Abono"
		   ,CAST(COALESCE(:v_Saldo_Inicial,0) + 
		   		SUM(CASE
		   				WHEN CM."IdEstado" = 'ANU' THEN 0
		   				ELSE CASE
		   						WHEN CM."IdTipoMovimiento" = 'MOVCAR' THEN (-1 * "Monto")
				   	     		WHEN CM."IdTipoMovimiento" = 'MOVABN' THEN "Monto"
				   	     	 END		     	    
			       	 END) over (partition by CM."IdCuentaInversion" 
			       	 			order by (CASE WHEN p_SaldoPorFechaEfectiva = 'S' THEN CM."FechaEfectivaMovimiento" ELSE CM."FechaSistema" END),
			       	 					 (CASE WHEN p_SaldoPorFechaEfectiva = 'S' THEN CM."FechaCertificacion" ELSE CM."FechaIngresoRegistro" END), 
										 CM."IdCargoAbonoMovimiento"
			       	            rows between unbounded preceding and current row) as DECIMAL(26,8)) as "SaldoSimulado"
		    ,Portafolio."IdMatrizAgrupador"
		    ,Portafolio."EsADC"
		    ,CM."IdEstadoSII"
		    ,CM."IdRespuestaDTE"
		FROM "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_IN_CuentaInversionAConsultar" as CIC
		INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" as CM
				ON CM."IdPortafolio" = CIC."IdPortafolio"
			   AND CM."IdMoneda" = CIC."IdMoneda"
	    LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio ON Portafolio."IdPortafolio" = CIC."IdPortafolio"
		WHERE (CASE WHEN p_SaldoPorFechaEfectiva = 'S' THEN CM."FechaEfectivaMovimiento" ELSE CM."FechaSistema" END) BETWEEN :p_FechaDesde and :p_FechaHasta
		AND Portafolio."IdMatrizAgrupador" = CASE WHEN :p_IdMatrizAgrupador IS NOT NULL THEN :p_IdMatrizAgrupador ELSE Portafolio."IdMatrizAgrupador" END
		AND Portafolio."EsADC" = CASE WHEN :p_EsADC IS NOT NULL THEN :p_EsADC ELSE Portafolio."EsADC" END;
		
		p_out_CartolaDeMovimiento =
		SELECT
			"IdCargoAbonoMovimiento"
		   ,"FechaSistema"
		   ,"FechaIngresoRegistro"
		   ,"IdCuentaInversion"
		   ,"CodigoCuentaInversion"
		   ,"NombreCuentaInversion"
		   ,"IdCliente"
		   ,"NombreCliente"
		   ,"IdPortafolio"
		   ,"IdMoneda"
		   ,"CodigoMoneda"
		   ,"NombreMoneda"
		   ,"SimboloMoneda"
		   ,"DecimalesMoneda"
		   ,"IdTipoMovimiento"
		   ,"TipoMovimiento"
		   ,"NombreTipoMovimiento"
		   ,"IdConcepto"
		   ,"NombreConcepto"
		   ,"OrigenConcepto"
		   ,"Descripcion"
		   ,"IdCondicionDeLiquidacion"
		   ,"CodigoCondicionDeLiquidacion"
		   ,"NombreCondicionDeLiquidacion"
		   ,"PlazoCondicionDeLiquidacion"
		   ,"FechaEfectivaMovimiento"
		   ,"Monto"
		   ,"SaldoPorFechaEfectiva"
		   ,"SaldoPorFechaMovimiento"
		   ,"IdModulo"
		   ,"IdTipoProducto"
		   ,"CodigoTipoProducto"
		   ,"NombreTipoProducto"
		   ,"IdTipoOperacion"
		   ,"CodigoTipoOperacion"
		   ,"NombreTipoOperacion"
		   ,"AccionTipoOperacion"
		   ,"IdOperacion"
		   ,"FechaOperacion"
		   ,"FechaLiquidacionOperacion"
		   ,"FechaVencimientoOperacion"
		   ,"IdMonedaOperacion"
		   ,"MontoOperacion"
		   ,"TipoCambioOperacion"
		   ,"IdTipoInstrumento"
		   ,"NombreTipoInstrumento"
		   ,"IdClasificacionTipoInstrumento"
		   ,"NombreClasificacionTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Cantidad"
		   ,"NemotecnicoOriginal"
		   ,"EsCargoAbonoPorTraspaso"
		   ,"EsCargoAbonoPorTraspasoTraducido"
		   ,"IdCargoAbonoRelacionadoTraspaso"
		   ,"NombreClienteRelacionado"
		   ,"CodigoPortafolioRelacionado"
		   ,"IdTipoComprobante"
		   ,"CodigoTipoComprobante"
		   ,"NombreTipoComprobante"
		   ,"IdComprobante"
		   ,COALESCE("FolioComprobante",'0') AS "FolioComprobante"
		   ,"EsVentaCorta"
		   ,"FechaComprobante"
		   ,"IdComprobanteDetalle"
		   ,"IdPersonaPagoCobro"
		   ,"EsCertificadoAutomatico"
		   ,"IdUsuarioCertificador"
		   ,"FechaCertificacion"
		   ,"IdContraparte"
		   ,"EsIndicadorBilateral"
		   ,"IdEmpresa"
		   ,"CodigoEmpresa"
		   ,"NombreEmpresa"
		   ,"EsIngresoManual"
		   ,"IdEvento"
		   ,"IdUsuario"
		   ,"NombreUsuario"
		   ,"IdEstado"
		   ,"NombreEstado"
		   ,"Cargo"
		   ,"Abono"
		   ,"SaldoSimulado"
		   ,"IdMatrizAgrupador"
		   ,"EsADC"
		   ,"NombreUsuarioCert"
		   ,"IdEstadoSII"
		   ,"IdRespuestaDTE"
		FROM :v_CartolaDeMovimiento
		WHERE "IdEstado" = COALESCE(p_IdEstado, "IdEstado")
		ORDER BY
			-- El orden depende si la consulta es por Fecha Adquisicion  o por Fecha Movimiento
			
			(CASE WHEN :p_SaldoPorFechaEfectiva = 'S' THEN "FechaEfectivaMovimiento" ELSE "FechaSistema" END),
			
		
			
			(CASE WHEN :p_SaldoPorFechaEfectiva = 'S' THEN "FechaCertificacion" ELSE "FechaSistema" END),
			
			
			"IdCargoAbonoMovimiento" ASC;
		
	END;
	
END;