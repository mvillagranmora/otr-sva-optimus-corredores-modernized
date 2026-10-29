SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Orden.BD.Servicios::RutearOrdenes";

CREATE PROCEDURE
-- 
--  Proposito: inscribe ordenes para ser enviadas a negociacions 
--  Autor: TEAM ORDENES
--  Fecha Creacion: 28/12/2015
--   
"BSCL.Orden.BD.Servicios::RutearOrdenes" (

	IN p_IdEmpresa	BIGINT,
	
	IN p_Ordenes TABLE (
		"IdOrden"					BIGINT,
		--"IdTipoCanalDistribucion"	NVARCHAR(3), -- RUTEO AGENTE RUA, RUTEO RETAIL RUR, RUTEO TRADER RUT
		--"CodigoOperador"			NVARCHAR(40),
		"IdBolsaDistribucion" 		NVARCHAR(3) --OPCIONAL
	
	),
	
	OUT p_out_SalidaDetalle 		"BSCL.Orden.BD.Tipos::SalidaDetalle",
	OUT p_out_SalidaResumen		"BSCL.Orden.BD.Tipos::SalidaResumen"
	
	 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		
		p_out_SalidaResumen = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:p_out_SalidaDetalle);
		
		--DROP TABLE #ORD;
	
	END;
	
	BEGIN
		
		DECLARE v_RuteoUsaFechaSistema		NVARCHAR(40);
		DECLARE v_SegundosZonaHoraria		BIGINT;
		DECLARE v_FechaSistemaSeconddate	SECONDDATE;
		
		/*CREATE LOCAL TEMPORARY TABLE #ORD (
			"IdOrden"				BIGINT,
			"IdOrdenReal"				BIGINT,
			"IdTipoInstrumento"		NVARCHAR(3),
			"FechaOperacionOrden"	NVARCHAR(10),
			"Nemotecnico"			NVARCHAR(40),
			"IdMonedaOrden"			NVARCHAR(3),
			"Cantidad"				DECIMAL(26,8),
			"PrecioOrden"			DECIMAL(26,8),
			"AccionOperacion"		NVARCHAR(40),
			"IdTipoVigenciaOrden"	NVARCHAR(2),
			"FechaVigencia"			NVARCHAR(10),
			"IdBolsa"				NVARCHAR(3),
			"IdBolsaDistribucion"	NVARCHAR(3),	
			"IdTipoCanalDistribucion"	NVARCHAR(3),	
			"IdTipoCanalIngreso"		NVARCHAR(3),
			"IdCondicionDeLiquidacion"	NVARCHAR(2),
			--"CodigoOperador"		NVARCHAR(40),
			"Cumple" 					NVARCHAR(1)
			
			
		);*/
		
		SELECT COALESCE("RuteoUsaFechaSistema",'S') into v_RuteoUsaFechaSistema 
		FROM "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenesPivot" WHERE "IdEmpresa" = p_IdEmpresa;
		
		SELECT "Valor" INTO v_SegundosZonaHoraria FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.SegundosZonaHoraria';
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,:v_FechaSistemaSeconddate);
		
		/*INSERT INTO #ORD("IdOrden","IdOrdenReal","IdTipoInstrumento","FechaOperacionOrden","Nemotecnico","IdMonedaOrden","Cantidad","PrecioOrden","AccionOperacion","IdTipoVigenciaOrden","FechaVigencia","IdBolsa",
						"IdBolsaDistribucion","IdCondicionDeLiquidacion","IdTipoCanalDistribucion","IdTipoCanalIngreso","Cumple")*/
		v_Ordenes1 = SELECT ord."IdOrden",o1."IdOrden" as "IdOrdenReal",ins."IdTipoInstrumento",
						--CASE WHEN :v_RuteoUsaFechaSistema = 'S' THEN TO_NVARCHAR(ADD_SECONDS(o1."FechaOperacion",:v_SegundosZonaHoraria), 'YYYY-MM-DD') ELSE TO_NVARCHAR(ADD_SECONDS("FechaIngresoRegistro",:v_SegundosZonaHoraria), 'YYYY-MM-DD') END as "FechaOperacionOrden",
						TO_NVARCHAR(ADD_SECONDS(CASE WHEN :v_RuteoUsaFechaSistema = 'S' THEN o1."FechaOperacion" ELSE "FechaIngresoRegistro" END,:v_SegundosZonaHoraria), 'YYYY-MM-DD HH24:MI:SS') as "FechaOperacionOrden",
						o1."Nemotecnico",o1."IdMonedaOrden",
						o1."Cantidad",o1."PrecioOrden",tip."AccionOperacion","IdTipoVigenciaOrden","FechaVigencia",o1."IdBolsa",
						COALESCE(ord."IdBolsaDistribucion",o1."IdBolsa") as "IdBolsaDistribucion",o1."IdCondicionDeLiquidacion",o1."IdTipoCanalDistribucion",
						o1."IdTipoCanalIngreso",TO_INT(COALESCE(visi."Porcentaje",100)*o1."Cantidad" / 100) as "CantidadVisible",'S' as "Cumple"
						FROM :p_Ordenes ord 
						LEFT JOIN "BSCL.Orden.BD::ORD.Orden" o1 on ord."IdOrden" = o1."IdOrden"
						LEFT JOIN "BSCL.Orden.BD::ORD.PorcentajeVisibilidad" visi on visi."IdPorcentajeVisibilidad" = o1."IdPorcentajeVisibilidad"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."IdInstrumento" = o1."IdInstrumento"
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tip on tip."IdTipoOperacion" = o1."IdTipoOperacion";
		
		--SELECT 'v_Ordenes1',* FROM :v_Ordenes1;
		
		/*UPDATE #ORD
		SET "Cumple" = 'N'
		WHERE "IdOrdenReal" is null;*/
		
		v_Ordenes2 = SELECT "IdOrden","IdOrdenReal","IdTipoInstrumento","FechaOperacionOrden","Nemotecnico","IdMonedaOrden","Cantidad","PrecioOrden","AccionOperacion","IdTipoVigenciaOrden","FechaVigencia","IdBolsa",
						"IdBolsaDistribucion","IdCondicionDeLiquidacion","IdTipoCanalDistribucion","IdTipoCanalIngreso","CantidadVisible",CASE WHEN "IdOrdenReal" IS NULL THEN 'N' ELSE 'S' END AS "Cumple"
						FROM :v_Ordenes1;
		
		--SELECT 'v_Ordenes2',* FROM :v_Ordenes2;
		
		p_out_SalidaDetalle = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'RECHAZO' AS "Resultado",'ORDEN_NO_EXISTE' AS "Codigo",'IdOrden' as "CodigoDato1", CAST("IdOrden" AS NVARCHAR(100)) as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
						FROM :v_Ordenes2 WHERE "Cumple" = 'N';
			
		--TIPO CANAL DE DISTRIBUCION NO ACTIVADO PARA BOLSA,TIPO INSTRUMENTO,TIPO CANAL INGRESO
		v_CanalDistribucionPermitido = select ord."IdOrden",tcdb."IdTipoCanalDistribucion"
										FROM :v_Ordenes2 ord
										LEFT JOIN "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion" tcdb on 
											tcdb."IdEmpresa" = :p_IdEmpresa and
											tcdb."IdTipoCanalDistribucion" = ord."IdTipoCanalDistribucion" and 
											tcdb."IdBolsa" = ord."IdBolsa" and
											tcdb."IdTipoCanalIngreso" = ord."IdTipoCanalIngreso" and
											tcdb."IdTipoInstrumento" =	ord."IdTipoInstrumento"
										WHERE "Cumple" = 'S';
		
		--SELECT 'v_CanalDistribucionPermitido',* FROM :v_CanalDistribucionPermitido;
		
		/*UPDATE ord SET "Cumple" = 'N' 
		FROM #ORD ord
		INNER JOIN :v_CanalDistribucionPermitido vvv on vvv."IdOrden" = ord."IdOrden";*/
		v_Ordenes3 = SELECT ord."IdOrden","IdOrdenReal","IdTipoInstrumento","FechaOperacionOrden","Nemotecnico","IdMonedaOrden","Cantidad","PrecioOrden","AccionOperacion","IdTipoVigenciaOrden","FechaVigencia","IdBolsa",
						"IdBolsaDistribucion","IdCondicionDeLiquidacion",ord."IdTipoCanalDistribucion","IdTipoCanalIngreso","CantidadVisible",CASE WHEN cdis."IdTipoCanalDistribucion" IS NULL THEN 'N' ELSE 'S' END AS "Cumple"
						FROM :v_Ordenes2 ord
						LEFT JOIN :v_CanalDistribucionPermitido cdis on cdis."IdOrden" = ord."IdOrden";
		
		--SELECT 'v_Ordenes3',* FROM :v_Ordenes3;
		
		p_out_SalidaDetalle = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'RECHAZO' AS "Resultado",'BOLSA_NO_PERMITE_CANAL_DISTRIBUCION_SELECCIONADO' AS "Codigo",'IdOrden' as "CodigoDato1", CAST("IdOrden" AS NVARCHAR(100)) as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
					FROM :v_CanalDistribucionPermitido where "IdTipoCanalDistribucion" IS NULL
					UNION ALL SELECT * FROM :p_out_SalidaDetalle;
	
		
		---CAMBIO ESTADO
		v_OrdenesValidasCambioEstado = SELECT "IdOrden" as "IdInterno","IdOrden" FROM :v_Ordenes3 WHERE "Cumple" = 'S';
		
    	CALL "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
    		p_Ordenes				=> :v_OrdenesValidasCambioEstado,	
    		p_NuevoEstado			=> 'ENR',
			p_out_SalidaDetalle		=> v_out_Resultado,
			p_out_SalidaResumen		=> v_out_SalidaResumen
		);
		
		p_out_SalidaDetalle = SELECT * FROM :v_out_Resultado UNION ALL SELECT * FROM :p_out_SalidaDetalle;

		/*UPDATE ord
		SET "Cumple" = 'N'
		FROM #ORD ord
		INNER JOIN :v_out_Resultado res on ord."IdOrden" = res."IdOperacion" and "Resultado" = 'N';*/
		
		v_Ordenes4 = SELECT ord."IdOrden","IdOrdenReal","IdTipoInstrumento","FechaOperacionOrden","Nemotecnico","IdMonedaOrden","Cantidad","PrecioOrden","AccionOperacion","IdTipoVigenciaOrden","FechaVigencia","IdBolsa",
						"IdBolsaDistribucion","IdCondicionDeLiquidacion",ord."IdTipoCanalDistribucion","IdTipoCanalIngreso","CantidadVisible",CASE WHEN res."Resultado" = 'N' THEN 'N' ELSE 'S' END AS "Cumple"
						FROM :v_Ordenes3 ord
						INNER JOIN :v_out_Resultado res on ord."IdOrden" = res."IdOperacion";
		
		
		UPDATE ordtabla 
		SET "IdBolsaDistribucion" = ord."IdBolsaDistribucion"
		FROM "BSCL.Orden.BD::ORD.Orden" ordtabla 
		INNER JOIN :v_Ordenes4 ord on ord."IdOrden" = ordtabla."IdOrden" and "Cumple" = 'S';
		
		SELECT 'v_Ordenes4',* FROM :v_Ordenes4;
		
		insert into "BSCL.Orden.BD::DMA.TBSOLICITUDES"("SECUENCIA","IN_RUTEO","OUT_RUTEO","PROCESADO","SEC_ORDEN","RUT_CLI","NEMO",
		"CANTIDAD","CANTIDAD_VISIBLE","PRECIO","TIPO_OPERAC","FEC_ING_ORDEN","IND_VALIDEZ",
		"FEC_VCTO_VALIDEZ","CAN_ASIG_ACUM","ESTADO","MONEDA","BOLSA",
		"CONDICION_LIQUIDACION","SPONSORING_FIRM","MERCADO","SUB_MERCADO","PREPAGO","TASA_MAXIMA","TASA_PROPIA","ID_REFERENCIA","FECHA_VENCIMIENTO","DIVISIBLE","OD","SEC_ORDEN_OD_COMPRA","SEC_ORDEN_OD_VENTA",
		"OP_INTERNO","SEC_ORDEN_2","ID_CLIENTE")
		SELECT "IdOrden" as "SECUENCIA", "FechaOperacionOrden" as "IN_RUTEO", "FechaOperacionOrden" as "OUT_RUTEO",'N' as "PROCESADO", "IdOrden" as "SEC_ORDEN",NULL AS "RUT_CLI","Nemotecnico" AS "NEMO",
		"Cantidad" as "CANTIDAD","CantidadVisible" as "CANTIDAD_VISIBLE",
		"PrecioOrden" as "PRECIO",CASE "AccionOperacion" WHEN 'COMPRA' THEN 'C' WHEN 'VENTA' THEN 'V' ELSE '' END AS "TIPO_OPERAC","FechaOperacionOrden" AS "FEC_ING_ORDEN",
		CASE "IdTipoVigenciaOrden" WHEN 'SH' THEN 'D' WHEN 'SE' THEN 'P' WHEN 'OP' THEN 'P' WHEN 'FF' THEN 'P' ELSE '' END as "IND_VALIDEZ",
		CASE "IdTipoVigenciaOrden" WHEN 'OP' THEN '' WHEN 'SH' THEN "FechaOperacionOrden" ELSE "FechaVigencia" END as "FEC_VCTO_VALIDEZ",NULL AS "CAN_ASIG_ACUM",'VT' AS "ESTADO","IdMonedaOrden" as "MONEDA",
		CASE "IdBolsa" WHEN 'BCS' THEN 'XSGO' WHEN 'BVL' THEN 'XLIM' WHEN 'BVC' THEN 'XBOG' ELSE '' END AS "BOLSA",
		CASE "IdCondicionDeLiquidacion" WHEN 'T0' THEN 'PH' WHEN 'T1' THEN 'PM' WHEN 'T2' THEN 'CN' WHEN 'T3' THEN 'T3' ELSE '' END as "CONDICION_LIQUIDACION",
		' ' AS "SPONSORING_FIRM", CASE "IdTipoInstrumento" WHEN 'ACN' THEN 'AC' WHEN 'ACX' THEN 'AO' WHEN 'CFI' THEN 'CI' WHEN 'CFX' THEN 'CO' WHEN 'MON' THEN 'MO' ELSE '' END AS "MERCADO",
		NULL as "SUBMERCADO",NULL as "PREPAGO",NULL AS "TASA_MAXIMA",
		NULL AS "TASA_PROPIA",NULL AS "ID_REFERENCIA",NULL AS "FECHA_VENCIMIENTO",NULL AS "DIVISIBLE",'N' AS "OD",0 AS "SEC_ORDEN_OD_COMPRA",0 AS "SEC_ORDEN_OD_VENTA",
		' ' as "OP_INTERNO",' ' AS "SEC_ORDEN_2",' ' AS "ID_CLIENTE"
		FROM :v_Ordenes4 WHERE "Cumple" = 'S' AND "IdTipoCanalDistribucion" = 'RUA';
		
		insert into "BSCL.Orden.BD::DMA.TBSOLICITUDES_2"("SECUENCIA","IN_RUTEO","OUT_RUTEO","PROCESADO","SEC_ORDEN","RUT_CLI","NEMO",
		"CANTIDAD","CANTIDAD_VISIBLE","PRECIO","TIPO_OPERAC","FEC_ING_ORDEN","IND_VALIDEZ",
		"FEC_VCTO_VALIDEZ","CAN_ASIG_ACUM","ESTADO","MONEDA","BOLSA",
		"CONDICION_LIQUIDACION","SPONSORING_FIRM","MERCADO","SUB_MERCADO","PREPAGO","TASA_MAXIMA","TASA_PROPIA","ID_REFERENCIA","FECHA_VENCIMIENTO","DIVISIBLE","OD","SEC_ORDEN_OD_COMPRA","SEC_ORDEN_OD_VENTA",
		"OP_INTERNO","SEC_ORDEN_2","ID_CLIENTE")
		SELECT "IdOrden" as "SECUENCIA", "FechaOperacionOrden" as "IN_RUTEO", "FechaOperacionOrden" as "OUT_RUTEO",'N' as "PROCESADO", "IdOrden" as "SEC_ORDEN",NULL AS "RUT_CLI","Nemotecnico" AS "NEMO",
		"Cantidad" as "CANTIDAD","CantidadVisible" as "CANTIDAD_VISIBLE",
		"PrecioOrden" as "PRECIO",CASE "AccionOperacion" WHEN 'COMPRA' THEN 'C' WHEN 'VENTA' THEN 'V' ELSE '' END AS "TIPO_OPERAC","FechaOperacionOrden" AS "FEC_ING_ORDEN",
		CASE "IdTipoVigenciaOrden" WHEN 'SH' THEN 'D' WHEN 'SE' THEN 'P' WHEN 'OP' THEN 'P' WHEN 'FF' THEN 'P' ELSE '' END as "IND_VALIDEZ",
		CASE "IdTipoVigenciaOrden" WHEN 'OP' THEN '' WHEN 'SH' THEN "FechaOperacionOrden" ELSE "FechaVigencia" END as "FEC_VCTO_VALIDEZ",NULL AS "CAN_ASIG_ACUM",'VT' AS "ESTADO","IdMonedaOrden" as "MONEDA",
		CASE "IdBolsa" WHEN 'BCS' THEN 'XSGO' WHEN 'BVL' THEN 'XLIM' WHEN 'BVC' THEN 'XBOG' ELSE '' END AS "BOLSA",
		CASE "IdCondicionDeLiquidacion" WHEN 'T0' THEN 'PH' WHEN 'T1' THEN 'PM' WHEN 'T2' THEN 'CN' WHEN 'T3' THEN 'T3' ELSE '' END as "CONDICION_LIQUIDACION",
		' ' AS "SPONSORING_FIRM", CASE "IdTipoInstrumento" WHEN 'ACN' THEN 'AC' WHEN 'ACX' THEN 'AO' WHEN 'CFI' THEN 'CI' WHEN 'CFX' THEN 'CO' WHEN 'MON' THEN 'MO' ELSE '' END AS "MERCADO",
		NULL as "SUBMERCADO",NULL as "PREPAGO",NULL AS "TASA_MAXIMA",
		NULL AS "TASA_PROPIA",NULL AS "ID_REFERENCIA",NULL AS "FECHA_VENCIMIENTO",NULL AS "DIVISIBLE",'N' AS "OD",0 AS "SEC_ORDEN_OD_COMPRA",0 AS "SEC_ORDEN_OD_VENTA",
		' ' as "OP_INTERNO",' ' AS "SEC_ORDEN_2",' ' AS "ID_CLIENTE"
		FROM :v_Ordenes4 WHERE "Cumple" = 'S' AND "IdTipoCanalDistribucion" = 'RUR';
		
		v_Registros =	SELECT 'ORDEN' as "NombreEntidad","IdOrden" as "IdEntidad","IdOrden",0 as "IdAsignacion",0 as "IdTransaccion",'' AS "MensajeEventoBitacora",'ENVIADA_NEGOCIACION' as "IdTipoEventoBitacora"
						FROM :v_Ordenes4 WHERE "Cumple" = 'S'; 
		
		CALL "BSCL.Orden.BD.Servicios::AgregarBitacoras" (:p_IdEmpresa,:v_Registros);
		
		p_out_SalidaDetalle = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'OK' AS "Resultado",'ORDEN_RUTEADA' AS "Codigo",'IdOrden' as "CodigoDato1", CAST("IdOrden" AS NVARCHAR(100)) as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
					FROM :v_Ordenes4 WHERE "Cumple" = 'S'
					UNION ALL SELECT * FROM :p_out_SalidaDetalle;
		
		SIGNAL SALIDA_CONTROLADA;
	END;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	DECLARE v_RuteoUsaFechaSistema		NVARCHAR(40);
		DECLARE v_SegundosZonaHoraria		BIGINT;
		DECLARE v_FechaSistemaSeconddate	SECONDDATE;
		
		CREATE LOCAL TEMPORARY TABLE #ORD (
			"IdOrden"				BIGINT,
			"IdOrdenReal"				BIGINT,
			"IdTipoInstrumento"		NVARCHAR(3),
			"FechaOperacionOrden"	NVARCHAR(10),
			"Nemotecnico"			NVARCHAR(40),
			"IdMonedaOrden"			NVARCHAR(3),
			"Cantidad"				DECIMAL(26,8),
			"PrecioOrden"			DECIMAL(26,8),
			"AccionOperacion"		NVARCHAR(40),
			"IdTipoVigenciaOrden"	NVARCHAR(2),
			"FechaVigencia"			NVARCHAR(10),
			"IdBolsa"				NVARCHAR(3),
			"IdBolsaDistribucion"	NVARCHAR(3),	
			"IdTipoCanalDistribucion"	NVARCHAR(3),	
			"IdTipoCanalIngreso"		NVARCHAR(3),
			"IdCondicionDeLiquidacion"	NVARCHAR(2),
			--"CodigoOperador"		NVARCHAR(40),
			"Cumple" 					NVARCHAR(1)
			
			
		);
		
		SELECT COALESCE("RuteoUsaFechaSistema",'S') into v_RuteoUsaFechaSistema 
		FROM "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenesPivot" WHERE "IdEmpresa" = p_IdEmpresa;
		
		SELECT "Valor" INTO v_SegundosZonaHoraria FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.SegundosZonaHoraria';
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,:v_FechaSistemaSeconddate);
		
		INSERT INTO #ORD("IdOrden","IdOrdenReal","IdTipoInstrumento","FechaOperacionOrden","Nemotecnico","IdMonedaOrden","Cantidad","PrecioOrden","AccionOperacion","IdTipoVigenciaOrden","FechaVigencia","IdBolsa",
						"IdBolsaDistribucion","IdCondicionDeLiquidacion","IdTipoCanalDistribucion","IdTipoCanalIngreso","Cumple")
		SELECT ord."IdOrden",o1."IdOrden" as "IdOrdenReal",ins."IdTipoInstrumento",
						CASE WHEN :v_RuteoUsaFechaSistema = 'S' THEN TO_NVARCHAR(ADD_SECONDS(o1."FechaOperacion",:v_SegundosZonaHoraria), 'YYYY-MM-DD') ELSE TO_NVARCHAR(ADD_SECONDS("FechaIngresoRegistro",:v_SegundosZonaHoraria), 'YYYY-MM-DD') END as "FechaOperacionOrden",
						o1."Nemotecnico",o1."IdMonedaOrden",
						o1."Cantidad",o1."PrecioOrden",tip."AccionOperacion","IdTipoVigenciaOrden","FechaVigencia",o1."IdBolsa",
						COALESCE(ord."IdBolsaDistribucion",o1."IdBolsa") as "IdBolsaDistribucion",o1."IdCondicionDeLiquidacion",o1."IdTipoCanalDistribucion",
						o1."IdTipoCanalIngreso",'S' as "Cumple"
						FROM :p_Ordenes ord 
						LEFT JOIN "BSCL.Orden.BD::ORD.Orden" o1 on ord."IdOrden" = o1."IdOrden"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."IdInstrumento" = o1."IdInstrumento"
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tip on tip."IdTipoOperacion" = o1."IdTipoOperacion";
		
		UPDATE #ORD
		SET "Cumple" = 'N'
		WHERE "IdOrdenReal" is null;
		
		p_out_SalidaDetalle = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'RECHAZO' AS "Resultado",'ORDEN_NO_EXISTE' AS "Codigo",'IdOrden' as "CodigoDato1", CAST("IdOrden" AS NVARCHAR(100)) as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
						FROM #ORD WHERE "Cumple" = 'N';
			
		--TIPO CANAL DE DISTRIBUCION NO ACTIVADO PARA BOLSA,TIPO INSTRUMENTO,TIPO CANAL INGRESO
		v_CanalDistribucionNoPermitido = select ord."IdOrden",tcdb."IdTipoCanalDistribucion"
										FROM #ORD ord
										LEFT JOIN "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion" tcdb on 
											tcdb."IdEmpresa" = :p_IdEmpresa and
											tcdb."IdTipoCanalDistribucion" = ord."IdTipoCanalDistribucion" and 
											tcdb."IdBolsa" = ord."IdBolsa" and
											tcdb."IdTipoCanalIngreso" = ord."IdTipoCanalIngreso" and
											tcdb."IdTipoInstrumento" =	ord."IdTipoInstrumento"
										WHERE "Cumple" = 'S' AND tcdb."IdTipoCanalDistribucion" IS NULL;
		
		UPDATE ord SET "Cumple" = 'N' 
		FROM #ORD ord
		INNER JOIN :v_CanalDistribucionNoPermitido vvv on vvv."IdOrden" = ord."IdOrden";
		
		p_out_SalidaDetalle = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'RECHAZO' AS "Resultado",'BOLSA_ORDEN_NO_PERMITE_RUTEO' AS "Codigo",'IdOrden' as "CodigoDato1", CAST("IdOrden" AS NVARCHAR(100)) as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
					FROM :v_CanalDistribucionNoPermitido
					UNION ALL SELECT * FROM :p_out_SalidaDetalle;
	
		
		---CAMBIO ESTADO
		v_OrdenesValidasCambioEstado = SELECT "IdOrden" FROM #ORD WHERE "Cumple" = 'S';
		
    	CALL "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
    		p_Ordenes				=> :v_OrdenesValidasCambioEstado,	
    		p_NuevoEstado			=> 'ENR',
			p_out_SalidaDetalle		=> v_out_Resultado,
			p_out_SalidaResumen		=> v_out_SalidaResumen
		);
		
		p_out_SalidaDetalle = SELECT * FROM :v_out_Resultado UNION ALL SELECT * FROM :p_out_SalidaDetalle;

		UPDATE ord
		SET "Cumple" = 'N'
		FROM #ORD ord
		INNER JOIN :v_out_Resultado res on ord."IdOrden" = res."IdOperacion" and "Resultado" = 'N';
		
		UPDATE ordtabla 
		SET "IdBolsaDistribucion" = ord."IdBolsaDistribucion"
		FROM "BSCL.Orden.BD::ORD.Orden" ordtabla 
		INNER JOIN #ORD ord on ord."IdOrden" = ordtabla."IdOrden" and "Cumple" = 'S';
		
		insert into "BSCL.Orden.BD::DMA.TBSOLICITUDES"("SECUENCIA","IN_RUTEO","OUT_RUTEO","PROCESADO","SEC_ORDEN","RUT_CLI","NEMO",
		"CANTIDAD","PRECIO","TIPO_OPERAC","FEC_ING_ORDEN","IND_VALIDEZ",
		"FEC_VCTO_VALIDEZ","CAN_ASIG_ACUM","ESTADO","MONEDA","BOLSA",
		"CONDICION_LIQUIDACION","SPONSORING_FIRM","MERCADO","SUB_MERCADO","PREPAGO","TASA_MAXIMA","TASA_PROPIA","ID_REFERENCIA","FECHA_VENCIMIENTO","DIVISIBLE","OD","SEC_ORDEN_OD_COMPRA","SEC_ORDEN_OD_VENTA",
		"OP_INTERNO","SEC_ORDEN_2","ID_CLIENTE")
		SELECT "IdOrden" as "SECUENCIA", "FechaOperacionOrden" as "IN_RUTEO", "FechaOperacionOrden" as "OUT_RUTEO",'N' as "PROCESADO", "IdOrden" as "SEC_ORDEN",NULL AS "RUT_CLI","Nemotecnico" AS "NEMO","Cantidad" as "CANTIDAD",
		"PrecioOrden" as "PRECIO",CASE "AccionOperacion" WHEN 'COMPRA' THEN 'C' WHEN 'VENTA' THEN 'V' ELSE '' END AS "TIPO_OPERAC","FechaOperacionOrden" AS "FEC_ING_ORDEN",
		CASE "IdTipoVigenciaOrden" WHEN 'SH' THEN 'D' WHEN 'SE' THEN 'P' WHEN 'OP' THEN 'P' WHEN 'FF' THEN 'P' ELSE '' END as "IND_VALIDEZ",
		CASE "IdTipoVigenciaOrden" WHEN 'OP' THEN '' WHEN 'SH' THEN "FechaOperacionOrden" ELSE "FechaVigencia" END as "FEC_VCTO_VALIDEZ",NULL AS "CAN_ASIG_ACUM",'VT' AS "ESTADO","IdMonedaOrden" as "MONEDA",
		CASE "IdBolsa" WHEN 'BCS' THEN 'XSGO' WHEN 'BVL' THEN 'XLIM' WHEN 'BVC' THEN 'XBOG' ELSE '' END AS "BOLSA",
		CASE "IdCondicionDeLiquidacion" WHEN 'T0' THEN 'PH' WHEN 'T1' THEN 'PM' WHEN 'T2' THEN 'CN' WHEN 'T3' THEN 'T3' ELSE '' END as "CONDICION_LIQUIDACION",
		' ' AS "SPONSORING_FIRM", CASE "IdTipoInstrumento" WHEN 'ACN' THEN 'AC' WHEN 'ACX' THEN 'AO' WHEN 'CFI' THEN 'CI' WHEN 'CFX' THEN 'CO' WHEN 'MON' THEN 'MO' ELSE '' END AS "MERCADO",
		NULL as "SUBMERCADO",NULL as "PREPAGO",NULL AS "TASA_MAXIMA",
		NULL AS "TASA_PROPIA",NULL AS "ID_REFERENCIA",NULL AS "FECHA_VENCIMIENTO",NULL AS "DIVISIBLE",'N' AS "OD",0 AS "SEC_ORDEN_OD_COMPRA",0 AS "SEC_ORDEN_OD_VENTA",
		' ' as "OP_INTERNO",' ' AS "SEC_ORDEN_2",' ' AS "ID_CLIENTE"
		FROM #ORD WHERE "Cumple" = 'S' AND "IdTipoCanalDistribucion" = 'RUA';
		
		insert into "BSCL.Orden.BD::DMA.TBSOLICITUDES_2"("SECUENCIA","IN_RUTEO","OUT_RUTEO","PROCESADO","SEC_ORDEN","RUT_CLI","NEMO",
		"CANTIDAD","PRECIO","TIPO_OPERAC","FEC_ING_ORDEN","IND_VALIDEZ",
		"FEC_VCTO_VALIDEZ","CAN_ASIG_ACUM","ESTADO","MONEDA","BOLSA",
		"CONDICION_LIQUIDACION","SPONSORING_FIRM","MERCADO","SUB_MERCADO","PREPAGO","TASA_MAXIMA","TASA_PROPIA","ID_REFERENCIA","FECHA_VENCIMIENTO","DIVISIBLE","OD","SEC_ORDEN_OD_COMPRA","SEC_ORDEN_OD_VENTA",
		"OP_INTERNO","SEC_ORDEN_2","ID_CLIENTE")
		SELECT "IdOrden" as "SECUENCIA", "FechaOperacionOrden" as "IN_RUTEO", "FechaOperacionOrden" as "OUT_RUTEO",'N' as "PROCESADO", "IdOrden" as "SEC_ORDEN",NULL AS "RUT_CLI","Nemotecnico" AS "NEMO","Cantidad" as "CANTIDAD",
		"PrecioOrden" as "PRECIO",CASE "AccionOperacion" WHEN 'COMPRA' THEN 'C' WHEN 'VENTA' THEN 'V' ELSE '' END AS "TIPO_OPERAC","FechaOperacionOrden" AS "FEC_ING_ORDEN",
		CASE "IdTipoVigenciaOrden" WHEN 'SH' THEN 'D' WHEN 'SE' THEN 'P' WHEN 'OP' THEN 'P' WHEN 'FF' THEN 'P' ELSE '' END as "IND_VALIDEZ",
		CASE "IdTipoVigenciaOrden" WHEN 'OP' THEN '' WHEN 'SH' THEN "FechaOperacionOrden" ELSE "FechaVigencia" END as "FEC_VCTO_VALIDEZ",NULL AS "CAN_ASIG_ACUM",'VT' AS "ESTADO","IdMonedaOrden" as "MONEDA",
		CASE "IdBolsa" WHEN 'BCS' THEN 'XSGO' WHEN 'BVL' THEN 'XLIM' WHEN 'BVC' THEN 'XBOG' ELSE '' END AS "BOLSA",
		CASE "IdCondicionDeLiquidacion" WHEN 'T0' THEN 'PH' WHEN 'T1' THEN 'PM' WHEN 'T2' THEN 'CN' WHEN 'T3' THEN 'T3' ELSE '' END as "CONDICION_LIQUIDACION",
		' ' AS "SPONSORING_FIRM", CASE "IdTipoInstrumento" WHEN 'ACN' THEN 'AC' WHEN 'ACX' THEN 'AO' WHEN 'CFI' THEN 'CI' WHEN 'CFX' THEN 'CO' WHEN 'MON' THEN 'MO' ELSE '' END AS "MERCADO",
		NULL as "SUBMERCADO",NULL as "PREPAGO",NULL AS "TASA_MAXIMA",
		NULL AS "TASA_PROPIA",NULL AS "ID_REFERENCIA",NULL AS "FECHA_VENCIMIENTO",NULL AS "DIVISIBLE",'N' AS "OD",0 AS "SEC_ORDEN_OD_COMPRA",0 AS "SEC_ORDEN_OD_VENTA",
		' ' as "OP_INTERNO",' ' AS "SEC_ORDEN_2",' ' AS "ID_CLIENTE"
		FROM #ORD WHERE "Cumple" = 'S' AND "IdTipoCanalDistribucion" = 'RUR';
		
		v_Registros =	SELECT 'ORDEN' as "NombreEntidad","IdOrden" as "IdEntidad","IdOrden",0 as "IdAsignacion",0 as "IdTransaccion",'' AS "MensajeEventoBitacora",'ENVIADA_NEGOCIACION' as "IdTipoEventoBitacora"
						FROM #ORD WHERE "Cumple" = 'S'; 
		
		CALL "BSCL.Orden.BD.Servicios::AgregarBitacoras" (:p_IdEmpresa,:v_Registros);
		
		p_out_SalidaDetalle = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'OK' AS "Resultado",'ORDEN_RUTEADA' AS "Codigo",'IdOrden' as "CodigoDato1", CAST("IdOrden" AS NVARCHAR(100)) as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
					FROM #ORD WHERE "Cumple" = 'S'
					UNION ALL SELECT * FROM :p_out_SalidaDetalle;
		
		SIGNAL SALIDA_CONTROLADA;
	END;*/
   

END