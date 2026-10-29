SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Orden.BD.Servicios::MIG.RutearOrdenes";
CREATE PROCEDURE
-- 
--  Proposito: inscribe ordenes para ser enviadas a negociacions 
--  Autor: TEAM ORDENES
--  Fecha Creacion: 28/12/2015
--   
"BSCL.Orden.BD.Servicios::MIG.RutearOrdenes" (

	IN p_IdEmpresa	BIGINT,
	
	/*IN p_Ordenes TABLE (
		"IdOrden"					BIGINT,
		"IdBolsaDistribucion" 		NVARCHAR(3) --OPCIONAL
	
	),
	*/
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
		DECLARE v_IntegracionMME			NVARCHAR(40);
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
		
		SELECT COALESCE("HabilitaIntegracionMME",'N') into v_IntegracionMME 
		FROM "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenesPivot" WHERE "IdEmpresa" = p_IdEmpresa;
		
		
		SELECT "Valor" INTO v_SegundosZonaHoraria FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.SegundosZonaHoraria';
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,:v_FechaSistemaSeconddate);
		
		/*INSERT INTO #ORD("IdOrden","IdOrdenReal","IdTipoInstrumento","FechaOperacionOrden","Nemotecnico","IdMonedaOrden","Cantidad","PrecioOrden","AccionOperacion","IdTipoVigenciaOrden","FechaVigencia","IdBolsa",
						"IdBolsaDistribucion","IdCondicionDeLiquidacion","IdTipoCanalDistribucion","IdTipoCanalIngreso","Cumple")*/
		v_Ordenes1 = SELECT ord."IdOrden",o1."IdOrden" as "IdOrdenReal",
						CASE WHEN ins."IdTipoInstrumento" = 'OSA' AND ins."IdTipoInstrumentoOrigen" = 'CFI' THEN 'CFI'
					     WHEN ins."IdTipoInstrumento" = 'OSA' AND ins."IdTipoInstrumentoOrigen" in ('ACN','') THEN 'ACN'
						ELSE ins."IdTipoInstrumento" END AS "IdTipoInstrumento",
						--CASE WHEN :v_RuteoUsaFechaSistema = 'S' THEN TO_NVARCHAR(ADD_SECONDS(o1."FechaOperacion",:v_SegundosZonaHoraria), 'YYYY-MM-DD') ELSE TO_NVARCHAR(ADD_SECONDS("FechaIngresoRegistro",:v_SegundosZonaHoraria), 'YYYY-MM-DD') END as "FechaOperacionOrden",
						TO_NVARCHAR(ADD_SECONDS(CASE WHEN :v_RuteoUsaFechaSistema = 'S' THEN o1."FechaOperacion" ELSE "FechaIngresoRegistro" END,:v_SegundosZonaHoraria), 'YYYY-MM-DD HH24:MI:SS') as "FechaOperacionOrden",
						o1."Nemotecnico",o1."IdMonedaOrden",
						o1."Cantidad",o1."PrecioOrden",tip."AccionOperacion","IdTipoVigenciaOrden","FechaVigencia",o1."IdBolsa",
						COALESCE(ord."IdBolsaDistribucion",o1."IdBolsa") as "IdBolsaDistribucion",o1."IdCondicionDeLiquidacion",o1."IdTipoCanalDistribucion",
						o1."IdTipoCanalIngreso",ROUND(COALESCE(visi."Porcentaje",100)*o1."Cantidad" / 100,0,ROUND_CEILING) as "CantidadVisible",'S' as "Cumple"
						, gTip."IdGrupoProducto", gTip."CodigoClasificacionNegociacion", insnegociacion."Nemotecnico" as "NemotecnicoNegociacion"
						--FROM :p_Ordenes ord 
						FROM "BSCL.Orden.BD::MIG.RutearOrdenes_Entrada" ord
						LEFT JOIN "BSCL.Orden.BD::ORD.Orden" o1 on ord."IdOrden" = o1."IdOrden"
						LEFT JOIN "BSCL.Orden.BD::ORD.PorcentajeVisibilidad" visi on visi."IdPorcentajeVisibilidad" = o1."IdPorcentajeVisibilidad"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."IdInstrumento" = o1."IdInstrumento"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" insnegociacion on insnegociacion."IdInstrumento" = o1."IdInstrumentoNegociacion"
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tip on tip."IdTipoOperacion" = o1."IdTipoOperacion"
						LEFT JOIN "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" gTip ON gTip."IdTipoInstrumento" = o1."IdTipoInstrumento";
		
		--SELECT 'v_Ordenes1',* FROM :v_Ordenes1;
		
		/*UPDATE #ORD
		SET "Cumple" = 'N'
		WHERE "IdOrdenReal" is null;*/
		
		v_Ordenes2 = SELECT "IdOrden","IdOrdenReal","IdTipoInstrumento","FechaOperacionOrden","Nemotecnico","IdMonedaOrden","Cantidad","PrecioOrden","AccionOperacion","IdTipoVigenciaOrden","FechaVigencia","IdBolsa",
						"IdBolsaDistribucion","IdCondicionDeLiquidacion","IdTipoCanalDistribucion","IdTipoCanalIngreso","CantidadVisible",CASE WHEN "IdOrdenReal" IS NULL THEN 'N' ELSE 'S' END AS "Cumple"
						,"IdGrupoProducto", "CodigoClasificacionNegociacion","NemotecnicoNegociacion"
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
						,"IdGrupoProducto", "CodigoClasificacionNegociacion","NemotecnicoNegociacion"
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
						,"IdGrupoProducto", "CodigoClasificacionNegociacion","NemotecnicoNegociacion",
						SUBSTRING(TO_NVARCHAR(LOCALTOUTC(TO_TIMESTAMP("FechaOperacionOrden"),'America/Santiago','platform')),0,23) as "FechaOperacionOrdenUTC"
						FROM :v_Ordenes3 ord
						INNER JOIN :v_out_Resultado res on ord."IdOrden" = res."IdOperacion";
		
		
		UPDATE ordtabla 
		SET "IdBolsaDistribucion" = ord."IdBolsaDistribucion"
		FROM "BSCL.Orden.BD::ORD.Orden" ordtabla 
		INNER JOIN :v_Ordenes4 ord on ord."IdOrden" = ordtabla."IdOrden" and "Cumple" = 'S';
		
		INSERT INTO "BSCL.Orden.BD::MME.TBSOLICITUDES_FIX5SP2"("SECUENCIA","IN_RUTEO","OUT_RUTEO","PROCESADO","SEC_ORDEN","NEMO","CANTIDAD","CANTIDAD_VISIBLE","PRECIO","TIPO_OPERAC","IND_VALIDEZ",
		"FEC_VCTO_VALIDEZ","TIPO_ORDEN","CUSTOMER_ACCOUNT","TEXTO","SEC_ORDEN_TO_CANCEL","FEC_PROGRAMADA")
		SELECT "IdOrden" as "SECUENCIA", "FechaOperacionOrden" AS "IN_RUTEO", "FechaOperacionOrden" AS "OUT_RUTEO", 'N' AS "PROCESADO", "IdOrden" AS "SEC_ORDEN", COALESCE("NemotecnicoNegociacion", "Nemotecnico") AS "NEMO", 
		"Cantidad" AS "CANTIDAD", "CantidadVisible" AS "CANTIDAD_VISIBLE", "PrecioOrden" AS "PRECIO",
		CASE "AccionOperacion" WHEN 'COMPRA' THEN 'C' WHEN 'VENTA' THEN 'V' ELSE '' END AS "TIPO_OPERAC",
		CASE "IdTipoVigenciaOrden" WHEN 'SH' THEN '0' WHEN 'SE' THEN '6' WHEN 'OP' THEN '6' WHEN 'FF' THEN '6' ELSE '' END AS "IND_VALIDEZ",
		CASE "IdTipoVigenciaOrden" WHEN 'OP' THEN "FechaVigencia"  WHEN 'SH' THEN "FechaOperacionOrden" ELSE "FechaVigencia" END AS "FEC_VCTO_VALIDEZ",
		'VT' AS "TIPO_ORDEN", '' AS "CUSTOMER_ACCOUNT", '' AS "TEXTO", NULL AS "SEC_ORDEN_TO_CANCEL",
		SUBSTRING("FechaOperacionOrdenUTC",0,10)||'T'||SUBSTRING("FechaOperacionOrdenUTC",12,23) AS "FEC_PROGRAMADA"
		FROM :v_Ordenes4 WHERE "Cumple" = 'S' AND v_IntegracionMME = 'S';
		
		v_Registros =	SELECT 'ORDEN' as "NombreEntidad","IdOrden" as "IdEntidad","IdOrden",0 as "IdAsignacion",0 as "IdTransaccion",'' AS "MensajeEventoBitacora",'ENVIADA_NEGOCIACION' as "IdTipoEventoBitacora"
						FROM :v_Ordenes4 WHERE "Cumple" = 'S'; 
		
		CALL "BSCL.Orden.BD.Servicios::AgregarBitacoras" (:p_IdEmpresa,:v_Registros);
		
		p_out_SalidaDetalle = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'OK' AS "Resultado",'ORDEN_RUTEADA' AS "Codigo",'IdOrden' as "CodigoDato1", CAST("IdOrden" AS NVARCHAR(100)) as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
					FROM :v_Ordenes4 WHERE "Cumple" = 'S'
					UNION ALL SELECT * FROM :p_out_SalidaDetalle;
		
		SIGNAL SALIDA_CONTROLADA;
	END;
	
END
