SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Orden.BD.Servicios::RutearOrdenesUI";

CREATE PROCEDURE
-- 
--  Proposito: inscribe ordenes para ser enviadas a negociacions 
--  Autor: TEAM ORDENES
--  Fecha Creacion: 28/12/2015
--   
"BSCL.Orden.BD.Servicios::RutearOrdenesUI" (

	IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3), 
	
	IN p_Ordenes TABLE (
		"IdOrden"					BIGINT,
		--"IdTipoCanalDistribucion"	NVARCHAR(3), -- RUTEO AGENTE RUA, RUTEO RETAIL RUR, RUTEO TRADER RUT
		--"CodigoOperador"			NVARCHAR(40),
		"IdBolsaDistribucion" 		NVARCHAR(3) --OPCIONAL
	
	),
	
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
	
	DECLARE v_SalidaDetalle "BSCL.Orden.BD.Tipos::SalidaDetalle";
	
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
	
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle			=> :v_SalidaDetalle,
	
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
		

		v_out_SalidaResumen = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:v_SalidaDetalle);
		
		DROP TABLE #ORD;
	
	END;
	
	BEGIN
		
		DECLARE	v_resultadoRegla   NVARCHAR(40);
		DECLARE v_OperadorRuteoAgente	NVARCHAR(40);
		
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
		
		SELECT "Valor" into v_OperadorRuteoAgente FROM "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
		WHERE "CodigoParametro" = 'CodigoOperadorRuteoAgente';
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalleBasicos,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			v_SalidaDetalle = SELECT 0 as "IdInterno",0 as "IdOperacion",'RECHAZO' AS "Resultado","Codigo" AS "Codigo",'' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
					FROM :v_out_ErroresDetalleBasicos;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		INSERT INTO #ORD("IdOrden","IdOrdenReal","IdTipoInstrumento","FechaOperacionOrden","Nemotecnico","IdMonedaOrden","Cantidad","PrecioOrden","AccionOperacion","IdTipoVigenciaOrden","FechaVigencia","IdBolsa",
						"IdBolsaDistribucion","IdCondicionDeLiquidacion"/*,"CodigoOperador"*/,"IdTipoCanalDistribucion","IdTipoCanalIngreso","Cumple")
		SELECT ord."IdOrden",o1."IdOrden" as "IdOrdenReal",ins."IdTipoInstrumento",TO_NVARCHAR("FechaIngresoRegistro", 'YYYY-MM-DD') as "FechaOperacionOrden",o1."Nemotecnico",o1."IdMonedaOrden",
						o1."Cantidad",o1."PrecioOrden",tip."AccionOperacion","IdTipoVigenciaOrden","FechaVigencia",o1."IdBolsa",
						COALESCE(ord."IdBolsaDistribucion",o1."IdBolsa") as "IdBolsaDistribucion",o1."IdCondicionDeLiquidacion"/*,ord."CodigoOperador" as "CodigoOperador"*/,o1."IdTipoCanalDistribucion",
						o1."IdTipoCanalIngreso",'S' as "Cumple"
						FROM :p_Ordenes ord 
						LEFT JOIN "BSCL.Orden.BD::ORD.Orden" o1 on ord."IdOrden" = o1."IdOrden"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."IdInstrumento" = o1."IdInstrumento"
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tip on tip."IdTipoOperacion" = o1."IdTipoOperacion";
		
		UPDATE #ORD
		SET "Cumple" = 'N'
		WHERE "IdOrdenReal" is null;
		
		v_SalidaDetalle = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'RECHAZO' AS "Resultado",'ORDEN_NO_EXISTE' AS "Codigo",'IdOrden' as "CodigoDato1", CAST("IdOrden" AS NVARCHAR(100)) as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
						FROM #ORD WHERE "Cumple" = 'N';
		
		--NO PERMITE RUTEO, deberia reemplazarse con una tabla |tipo instrumento|bolsa|idtipocanaldistribucion
		/*v_OrdenesNoPermiteRuteo = select "IdOrden" from #ORD WHERE "IdTipoInstrumento" not in ('ACN','ACX','CFI','CFX','MON') AND "IdBolsa" not in ('BCS','BVL','BVC');
		
		UPDATE ord SET "Cumple" = 'N'
		FROM #ORD ord
		INNER JOIN :v_OrdenesNoPermiteRuteo vvv on vvv."IdOrden" = ord."IdOrden";
		
		v_SalidaDetalle = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'RECHAZO' AS "Resultado",'ORDEN_NO_PERMITE_RUTEO' AS "Codigo",'IdOrden' as "CodigoDato1", CAST("IdOrden" AS NVARCHAR(100)) as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
					FROM :v_OrdenesNoPermiteRuteo;*/
	
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
		
		v_SalidaDetalle = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'RECHAZO' AS "Resultado",'BOLSA_ORDEN_NO_PERMITE_RUTEO' AS "Codigo",'IdOrden' as "CodigoDato1", CAST("IdOrden" AS NVARCHAR(100)) as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
					FROM :v_CanalDistribucionNoPermitido
					UNION ALL SELECT * FROM :v_SalidaDetalle;
	
		
		---CAMBIO ESTADO
		v_OrdenesValidasCambioEstado = SELECT "IdOrden" AS "IdInterno","IdOrden" FROM #ORD WHERE "Cumple" = 'S';
		
    	CALL "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
    		p_Ordenes				=> :v_OrdenesValidasCambioEstado,	
    		p_NuevoEstado			=> 'OFE',
			p_out_SalidaDetalle		=> v_out_Resultado,
			p_out_SalidaResumen		=>v_out_SalidaResumen
		);
		--p_out_Resultado				TABLE("IdOrden" BIGINT, "Resultado" NVARCHAR(1), "Codigo" NVARCHAR(40))

		v_SalidaDetalle = SELECT * FROM :v_out_Resultado UNION ALL SELECT * FROM :v_SalidaDetalle;

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
		LEFT(:v_OperadorRuteoAgente,3) as "OP_INTERNO",' ' AS "SEC_ORDEN_2",' ' AS "ID_CLIENTE"
		FROM #ORD WHERE "Cumple" = 'S';
		
		INSERT INTO "BSCL.Orden.BD::ORD.EventoBitacora"("IdEventoBitacora","NombreEntidad","IdOrden","IdAsignacion","IdTransaccion",
		"MensajeEventoBitacora","FechaEventoBitacora","FechaIngresoRegistro","IdTipoEventoBitacora")
		SELECT "BSCL.Orden.BD.Secuencias::EventoBitacora".NEXTVAL as "IdEventoBitacora", 'ORDEN' as "NombreEntidad","IdOrden",0 as "IdAsignacion",0 as "IdTransaccion",
		'' AS "MensajeEventoBitacora",CURRENT_TIMESTAMP as "FechaEventoBitacora",CURRENT_TIMESTAMP as "FechaIngresoRegistro",'ENVIADA_NEGOCIACION' as "IdTipoEventoBitacora" 
		FROM #ORD WHERE "Cumple" = 'S';
		
		SIGNAL SALIDA_CONTROLADA;
	END;
   

END
