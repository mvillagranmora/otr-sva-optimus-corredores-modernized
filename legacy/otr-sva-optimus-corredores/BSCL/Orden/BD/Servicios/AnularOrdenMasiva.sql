SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::AnularOrdenMasiva";

CREATE PROCEDURE
-- 
--  Proposito: Anular Ordenes
--  Autor: TEAM ORDENES
--  Fecha Creacion: 07/01/2016
--  
"BSCL.Orden.BD.Servicios::AnularOrdenMasiva" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS 
    IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3), 
	
	--ENUMERAR VARIABLES DE ENTRADA
	IN p_Ordenes						  table (
											"IdOrden" 			BIGINT,
											"MotivoAnulacion"	NVARCHAR(5000),
											"FechaHoraAnulacion" TIMESTAMP
											),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador int,
	OUT p_out_InformacionDetalle table ("IdInterno" BIGINT, "IdOperacion" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("IdInterno" BIGINT, "IdOperacion" BIGINT,"Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("IdInterno" BIGINT, "IdOperacion" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	
    
	)
	
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	DECLARE v_Errores 					"BSCL.Orden.BD.Tipos::SalidaDetalle";
	DECLARE v_Localizacion NVARCHAR(10);--Para Obtener Localizacion
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle			=> :v_Errores,
	
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
		

		v_out_SalidaResumen = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:v_Errores);
		p_out_ErroresContador = 0;
		
		DROP TABLE #RESULTADO_AnularOrdenMasiva;
		
	END;
	
	BEGIN
	
		DECLARE v_IntegracionMME			NVARCHAR(40);
		DECLARE v_SegundosZonaHoraria		BIGINT;
				
		CREATE LOCAL TEMPORARY TABLE #RESULTADO_AnularOrdenMasiva (
			"IdOrden" 					BIGINT,
			"FechaOperacion"			SECONDDATE,
			"FechaIngresoRegistro"		SECONDDATE,
			"IdCliente"					BIGINT,
			"IdEstadoCliente"			NVARCHAR(3),
			"IdPortafolio"				BIGINT,
			"IdMonedaLiquidacionOrden"	NVARCHAR(3),
			"IdEstadoOrden"				NVARCHAR(3),
			"IdBolsa"					NVARCHAR(3),
			"IdTipoOperacion"			NVARCHAR(6),
			"IdTipoInstrumento"			NVARCHAR(3),
			"IdCondicionDeLiquidacion"	NVARCHAR(2),
			"Cantidad"					DECIMAL(26,8),
			"PrecioOrden"				DECIMAL(26,8),
			"MontoOrden"				DECIMAL(26,8),
			"IdMonedaOrden"				NVARCHAR(3),
			"TipoCambio"				DECIMAL(26,8),
			"Nemotecnico"				NVARCHAR(40),
			"IdTipoCanalIngreso"		NVARCHAR(3),
			"IdTipoCanalDistribucion"	NVARCHAR(3),
			"NombreCompleto"			NVARCHAR(100),
			"TipoDocumentoIdentidad"	NVARCHAR(3),
			"DocumentoIdentidad"		NVARCHAR(40),
			"IdEstadoPortafolio"		NVARCHAR(3),
			"AccionOrden"				NVARCHAR(40),
			"CantidadAsignaciones"		BIGINT,
			"MotivoAnulacion"			NVARCHAR(5000),
			"Cumple"					NVARCHAR(1),
			"EnAnulacion"				NVARCHAR(10),
			"NemoNegociacion"			NVARCHAR(40),
			"FechaHoraAnulacion"		TIMESTAMP
		);
		
		SELECT COALESCE("HabilitaIntegracionMME",'N') into v_IntegracionMME 
		FROM "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenesPivot" WHERE "IdEmpresa" = p_IdEmpresa;
		
		
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;		
		
		INSERT INTO #RESULTADO_AnularOrdenMasiva ("IdOrden","FechaOperacion","FechaIngresoRegistro","IdCliente","IdEstadoCliente","IdPortafolio","IdMonedaLiquidacionOrden","IdEstadoOrden","IdBolsa","IdTipoOperacion","IdTipoCanalDistribucion","AccionOrden",
		"Cantidad","PrecioOrden","MontoOrden","IdMonedaOrden","IdTipoInstrumento","TipoCambio","Nemotecnico","IdTipoCanalIngreso","NombreCompleto","TipoDocumentoIdentidad","DocumentoIdentidad","IdEstadoPortafolio","IdCondicionDeLiquidacion","MotivoAnulacion","Cumple","EnAnulacion",
		"NemoNegociacion"
		,"FechaHoraAnulacion")
		SELECT ord."IdOrden",ord."FechaOperacion" as "FechaOperacion",ord."FechaIngresoRegistro",ord."IdCliente",cli."IdEstado" as "IdEstadoCliente",ord."IdPortafolioCliente" as "IdPortafolio",ord."IdMonedaLiquidacionOrden",ord."IdEstadoOrden",ord."IdBolsa",ord."IdTipoOperacion",ord."IdTipoCanalDistribucion",tipoope."AccionOrden" as "AccionOrden",
		ord."Cantidad",ord."PrecioOrden",ord."MontoOrden",ord."IdMonedaOrden",
		CASE WHEN ord."IdTipoInstrumento" = 'OSA' AND instrumento."IdTipoInstrumentoOrigen" = 'CFI' THEN 'CFI'
			 WHEN ord."IdTipoInstrumento" = 'OSA' AND instrumento."IdTipoInstrumentoOrigen" in ('ACN','') THEN 'ACN'
			 ELSE ord."IdTipoInstrumento"
		END AS "IdTipoInstrumento"
		,ord."TipoCambio",ord."Nemotecnico",ord."IdTipoCanalIngreso",cli."NombreCompleto",cli."TipoDocumentoIdentidad",cli."DocumentoIdentidad",port."IdEstado" as "IdEstadoPortafolio",ord."IdCondicionDeLiquidacion",p_ord."MotivoAnulacion",'S' as "Cumple",
		ord."EnAnulacion",insnegociacion."Nemotecnico" as "NemoNegociacion"
		,p_ord."FechaHoraAnulacion"
		FROM :p_Ordenes p_ord 
		INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord ON p_ord."IdOrden" = ord."IdOrden"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" instrumento on instrumento."IdInstrumento" = ord."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" insnegociacion on insnegociacion."IdInstrumento" = ord."IdInstrumentoNegociacion"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on tipoope."IdTipoOperacion" = ord."IdTipoOperacion"
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port on ord."IdPortafolioCliente" = port."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on port."IdCliente" = cli."IdCliente"	;
					
		UPDATE res_ord
		SET "CantidadAsignaciones" = COALESCE(TT."CantidadAsignaciones",0)
		FROM #RESULTADO_AnularOrdenMasiva res_ord 
		INNER JOIN (
			SELECT ord."IdOrden", SUM("CantidadAsignada")  as "CantidadAsignaciones"
			FROM #RESULTADO_AnularOrdenMasiva res_ord 
			INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord ON res_ord."IdOrden" = ord."IdOrden"
			LEFT OUTER JOIN "BSCL.Orden.BD::ORD.Asignacion" asig ON asig."IdOrden" = ord."IdOrden" and asig."IdEstadoAsignacion" != 'ANU' 
			GROUP BY ord."IdOrden") 
		AS TT ON tt."IdOrden" = res_ord."IdOrden";
		
		--Localizacion
		SELECT loc."Valor"
		INTO v_Localizacion
		FROM "BSCL.Plataforma.BD::PLA.Configuracion" loc
		WHERE loc."CodigoParametro" = 'PLA.LocalizacionSistema' AND loc."IdEmpresa" = :p_IdEmpresa;
		
		
		------------------orden cantada con algo asignado, no puedo anular----------------------------------------
		v_OrdenesOfertaManualConAsignaciones = SELECT "IdOrden","EnAnulacion"
									FROM #RESULTADO_AnularOrdenMasiva res
									WHERE "CantidadAsignaciones" > 0 AND "CantidadAsignaciones" != res."Cantidad"  AND  "IdTipoCanalDistribucion" = 'MAN' AND  :v_Localizacion = 'PE';
		
		v_Errores = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",(CASE WHEN "EnAnulacion" = 'S' THEN 'INFORMACION' ELSE 'ADVERTENCIA' END) AS "Resultado", (CASE WHEN "EnAnulacion" ='S' THEN 'SOLICITUD_PROCESADA' ELSE 'GENERADA_SOLICITUD_ANULACION' END) AS "Codigo",'IdOrden' as "CodigoDato1",CAST("IdOrden" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
					FROM :v_OrdenesOfertaManualConAsignaciones val WHERE :v_Localizacion = 'PE' ;

		UPDATE res
		SET "Cumple" = 'N'
		FROM #RESULTADO_AnularOrdenMasiva res
		INNER JOIN :v_OrdenesOfertaManualConAsignaciones a1 on res."IdOrden" = a1."IdOrden";
		------------------------------------------------------------------------------------------------------------
		---------------------Orden Cantada con Algo Asignado Envia Solicitud de Anulacion PERU----------------------
		v_OrdenesParaAnularPeruAsigParcial = SELECT "IdOrden"
									FROM #RESULTADO_AnularOrdenMasiva res
									WHERE "CantidadAsignaciones" > 0 AND "CantidadAsignaciones" != res."Cantidad" AND "IdTipoCanalDistribucion" = 'MAN' AND :v_Localizacion = 'PE';
		
		UPDATE ORD
		SET "EnAnulacion" = 'S'
		FROM "BSCL.Orden.BD::ORD.Orden" AS ORD
		INNER JOIN :v_OrdenesParaAnularPeruAsigParcial as Sol ON ORD."IdOrden" = Sol."IdOrden";
	
		--ANULACION DE ORDEN CANTADA PARCIALMENTE CHILE
		v_OrdenesParaAnularAsigParcial = SELECT res."IdOrden"
									FROM #RESULTADO_AnularOrdenMasiva res
									LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarOrdenesUI" ord ON ord."IdOrden" = res."IdOrden"
									WHERE res."CantidadAsignaciones" > 0 AND res."CantidadAsignaciones" != res."Cantidad" AND res."IdTipoCanalDistribucion" = 'MAN' AND ord."IdEstadoOrden" NOT IN ('EJP', 'ANP');
	
		UPDATE ORD
		SET "EnAnulacion" = 'N'
		FROM "BSCL.Orden.BD::ORD.Orden" AS ORD
		INNER JOIN :v_OrdenesParaAnularAsigParcial as Sol ON ORD."IdOrden" = Sol."IdOrden";
	
		UPDATE res
		SET "Cumple" = 'N'
		FROM #RESULTADO_AnularOrdenMasiva res
		INNER JOIN :v_OrdenesParaAnularAsigParcial a1 on res."IdOrden" = a1."IdOrden";
	
	
		v_Errores = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'RECHAZO' AS "Resultado",'ERROR_ANULAR_ORDEN_ESTADO_OFERTADA' AS "Codigo",'IdOrden' as "CodigoDato1",CAST("IdOrden" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
					FROM :v_OrdenesParaAnularAsigParcial val
					UNION ALL
					SELECT * FROM :v_Errores;
		
		--Estas Ordenes con Solicitud no deben ser procesadas desde gestion de ordenes--
		------------------------------------------------------------------------------------------------------------
		
		------------------orden ruteo asignada completamente,no puedo anular----------------------------------------
		v_OrdenesRuteoConAsignaciones = SELECT "IdOrden"
									FROM #RESULTADO_AnularOrdenMasiva res
									WHERE "CantidadAsignaciones" = res."Cantidad" AND "IdTipoCanalDistribucion" != 'MAN';
		
		v_Errores = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'RECHAZO' AS "Resultado",'ERROR_ANULAR_ORDEN_ASIGNADA_COMPLETA' AS "Codigo",'IdOrden' as "CodigoDato1",CAST("IdOrden" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
					FROM :v_OrdenesRuteoConAsignaciones val
					UNION ALL
					SELECT * FROM :v_Errores;		
		
		UPDATE res
		SET "Cumple" = 'N'
		FROM #RESULTADO_AnularOrdenMasiva res
		INNER JOIN :v_OrdenesRuteoConAsignaciones a1 on res."IdOrden" = a1."IdOrden";
		
		------------------orden rechazada, no puedo anular ----------------------------------------
		v_OrdenesRechazadas = SELECT "IdOrden"
									FROM #RESULTADO_AnularOrdenMasiva res
									WHERE "IdEstadoOrden" = 'RZD';
		
		v_Errores = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'RECHAZO' AS "Resultado",'ERROR_ANULAR_ORDEN_ESTADO_RECHAZADO' AS "Codigo",'IdOrden' as "CodigoDato1",CAST("IdOrden" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
					FROM :v_OrdenesRechazadas val
					UNION ALL
					SELECT * FROM :v_Errores;
		
		UPDATE res
		SET "Cumple" = 'N'
		FROM #RESULTADO_AnularOrdenMasiva res
		INNER JOIN :v_OrdenesRechazadas a1 on res."IdOrden" = a1."IdOrden";
		
		------------------orden anulada, no puedo anular----------------------------------------
		v_OrdenesAnuladas = SELECT "IdOrden"
									FROM #RESULTADO_AnularOrdenMasiva res
									WHERE "IdEstadoOrden" IN ('ANU','ANP');
		
		v_Errores = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'RECHAZO' AS "Resultado",'ERROR_ANULAR_ORDEN_ESTADO_ANULADO' AS "Codigo",'IdOrden' as "CodigoDato1",CAST("IdOrden" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
					FROM :v_OrdenesAnuladas val
					UNION ALL
					SELECT * FROM :v_Errores;
		
		UPDATE res
		SET "Cumple" = 'N'
		FROM #RESULTADO_AnularOrdenMasiva res 
		INNER JOIN :v_OrdenesAnuladas a1 on res."IdOrden" = a1."IdOrden";
		------------------------------------------------------------------------------------------
		
		------------------orden cantada, NO SE PUEDE ANULAR EXCEPTO SI ES POSTRERA----------------------------------------
		v_OrdenesPostreraOfertada = SELECT "IdOrden","EnAnulacion"
									FROM #RESULTADO_AnularOrdenMasiva res
									WHERE "IdEstadoOrden" = 'OFE' and "IdTipoCanalDistribucion" = 'MAN' 
									AND "IdTipoCanalIngreso" != 'TRA'
									AND ("CantidadAsignaciones" = 0 OR ("CantidadAsignaciones" > 0	AND "CantidadAsignaciones" = "Cantidad"	));

		
		v_Errores = SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'RECHAZO' AS "Resultado",'ERROR_ANULAR_ORDEN_ESTADO_OFERTADA' AS "Codigo",'IdOrden' as "CodigoDato1",CAST("IdOrden" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
					FROM :v_OrdenesPostreraOfertada val WHERE :v_Localizacion = 'CL'
					UNION ALL
					SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",(CASE WHEN "EnAnulacion" = 'S' THEN 'INFORMACION' ELSE 'ADVERTENCIA' END) AS "Resultado", (CASE WHEN "EnAnulacion" ='S' THEN 'SOLICITUD_PROCESADA' ELSE 'GENERADA_SOLICITUD_ANULACION' END) AS "Codigo",'IdOrden' as "CodigoDato1",CAST("IdOrden" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
					FROM :v_OrdenesPostreraOfertada val WHERE :v_Localizacion = 'PE'
					UNION ALL
					SELECT * FROM :v_Errores;
		
		UPDATE res
		SET "Cumple" = 'N'
		FROM #RESULTADO_AnularOrdenMasiva res
		INNER JOIN :v_OrdenesPostreraOfertada a1 on res."IdOrden" = a1."IdOrden";
		------------------------------------------------------------------------------------------
		-->Vamos a modificar las ordenes para que se muestren en negociacion como solicitud de ANULACION<-----	 
		v_OrdenesParaAnularPeru = SELECT "IdOrden"
								  FROM #RESULTADO_AnularOrdenMasiva res
								  WHERE "IdEstadoOrden" = 'OFE' AND "IdTipoCanalDistribucion" = 'MAN' AND "IdTipoCanalIngreso" != 'TRA' AND :v_Localizacion = 'PE' AND "CantidadAsignaciones" = 0;
		
		UPDATE ORD
		SET "EnAnulacion" = 'S'
		FROM "BSCL.Orden.BD::ORD.Orden" AS ORD
		INNER JOIN :v_OrdenesParaAnularPeru as Sol ON ORD."IdOrden" = Sol."IdOrden";

		------------------------------------------------------------------------------------------
		--FIN VALIDACIONES		
		SELECT '#RESULTADO_AnularOrdenMasiva',* FROM #RESULTADO_AnularOrdenMasiva;
		--ANULACION ORDENES MANUALES PARCIAL
		v_OrdenesAnularManualesParciales = 	SELECT "IdOrden"
											FROM #RESULTADO_AnularOrdenMasiva 
											WHERE "Cumple" = 'S' AND "IdTipoCanalDistribucion" = 'MAN'
											AND "CantidadAsignaciones" > 0 AND "CantidadAsignaciones" != "Cantidad";
									
		--ANULACION ORDENES MANUALES
		v_OrdenesAnularManuales = 	SELECT RES."IdOrden"
									FROM #RESULTADO_AnularOrdenMasiva RES
									WHERE RES."Cumple" = 'S' AND RES."IdTipoCanalDistribucion" = 'MAN'
									AND RES."IdOrden" NOT IN (SELECT RESP."IdOrden" FROM :v_OrdenesAnularManualesParciales RESP);
		
		--ANULACION ORDENES DE RUTEO NO PROCESADAS
		--SI LA ORDEN ESTA EN RUTEO AGENTE PERO AUN NO HA SIDO PROCESADA
		v_OrdenesNoProcesadasRuteoMME2 = 	SELECT "IdOrden" 
											FROM #RESULTADO_AnularOrdenMasiva res  
											INNER JOIN "BSCL.Orden.BD::MME.TBSOLICITUDES_FIX5SP2" SOL ON res."IdOrden" = SOL."SEC_ORDEN"
											WHERE "Cumple" = 'S' AND "IdEstadoOrden" = 'ENR' AND "PROCESADO" != 'S' AND v_IntegracionMME = 'S';
		
		
		v_RegistrosAnulacionNoProcesadas = 
						SELECT 'ORDEN' as "NombreEntidad","IdOrden" as "IdEntidad","IdOrden",0 as "IdAsignacion",0 as "IdTransaccion",'' AS "MensajeEventoBitacora",'ORDEN_ANULADA' as "IdTipoEventoBitacora"
						FROM :v_OrdenesNoProcesadasRuteoMME2;
									
		v_OrdenesParaAnulacion =	SELECT "IdOrden" AS "IdInterno","IdOrden" FROM :v_OrdenesAnularManuales;
		
		v_OrdenesParaAnulacionParciales =	SELECT "IdOrden" AS "IdInterno","IdOrden" FROM :v_OrdenesAnularManualesParciales;
									
		
		--SELECT 'v_OrdenesParaAnulacion',* FROM :v_OrdenesParaAnulacion;
		
		--ANULACION TOTAL
		call "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
			p_Ordenes 			=> :v_OrdenesParaAnulacion,
			p_NuevoEstado 		=> 'ANU',
			p_out_SalidaDetalle => v_ErroresCambiarEstadoOrdenMasivo,
			p_out_SalidaResumen => v_out_SalidaResumen);
		
		--SELECT 'v_out_SalidaResumen',* FROM :v_out_SalidaResumen;
		
		UPDATE res
		SET "Cumple" = 'N'
		FROM #RESULTADO_AnularOrdenMasiva res
		INNER JOIN :v_out_SalidaResumen a1 on res."IdOrden" = a1."IdOperacion" and "Resultado" = 'RECHAZO';
		
		UPDATE ord
		SET "MotivoAnulacion" = res."MotivoAnulacion"
		FROM "BSCL.Orden.BD::ORD.Orden" ord
		INNER JOIN #RESULTADO_AnularOrdenMasiva res on res."IdOrden" = ord."IdOrden"
		INNER JOIN :v_out_SalidaResumen a1 on ord."IdOrden" = a1."IdOperacion" and "Resultado" = 'OK';
		
		
		v_Errores = SELECT * FROM :v_Errores
					UNION ALL
					SELECT * FROM :v_ErroresCambiarEstadoOrdenMasivo
					UNION ALL
					SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'INFORMACION' AS "Resultado",'ORDEN_ANULADA' AS "Codigo",'IdOrden' as "CodigoDato1",CAST("IdOrden" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
					FROM #RESULTADO_AnularOrdenMasiva res
					INNER JOIN :v_out_SalidaResumen a1 on res."IdOrden" = a1."IdOperacion" and "Resultado" = 'OK';		
		
		--ANULACION PARCIAL
		call "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
			p_Ordenes 			=> :v_OrdenesParaAnulacionParciales,
			p_NuevoEstado 		=> 'ANP',
			p_out_SalidaDetalle => v_ErroresCambiarEstadoOrdenMasivo,
			p_out_SalidaResumen => v_out_SalidaResumen);
		
		--SELECT 'v_out_SalidaResumen',* FROM :v_out_SalidaResumen;
		
		UPDATE res
		SET "Cumple" = 'N'
		FROM #RESULTADO_AnularOrdenMasiva res
		INNER JOIN :v_out_SalidaResumen a1 on res."IdOrden" = a1."IdOperacion" and "Resultado" = 'RECHAZO';
		
		UPDATE ord
		SET "MotivoAnulacion" = res."MotivoAnulacion"
		FROM "BSCL.Orden.BD::ORD.Orden" ord
		INNER JOIN #RESULTADO_AnularOrdenMasiva res on res."IdOrden" = ord."IdOrden"
		INNER JOIN :v_out_SalidaResumen a1 on ord."IdOrden" = a1."IdOperacion" and "Resultado" = 'OK';
		
		
		v_Errores = SELECT * FROM :v_Errores
					UNION ALL
					SELECT * FROM :v_ErroresCambiarEstadoOrdenMasivo
					UNION ALL
					SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'INFORMACION' AS "Resultado",'ORDEN_ANULADA' AS "Codigo",'IdOrden' as "CodigoDato1",CAST("IdOrden" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
					FROM #RESULTADO_AnularOrdenMasiva res
					INNER JOIN :v_out_SalidaResumen a1 on res."IdOrden" = a1."IdOperacion" and "Resultado" = 'OK';		
		
		UPDATE SOL
		SET "PROCESADO" = 'S'
		FROM "BSCL.Orden.BD::MME.TBSOLICITUDES_FIX5SP2"  SOL 
		INNER JOIN #RESULTADO_AnularOrdenMasiva res on SOL."SEC_ORDEN" = res."IdOrden"
		WHERE "Cumple" = 'S'  AND v_IntegracionMME = 'S';
		

		SELECT "Valor" INTO v_SegundosZonaHoraria FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.SegundosZonaHoraria';
		--ORDENES DE RUTEO EN ESTADO OFERTADA
		--SI LA ORDEN ESTA OFERTADA Y ES DE RUTEO AGENTE
		v_FFF = SELECT "BSCL.Orden.BD.Secuencias::SecuenciaAnulacion".NEXTVAL as "SECUENCIA", "IdTipoCanalDistribucion"
						,TO_NVARCHAR(ADD_SECONDS("FechaHoraAnulacion",:v_SegundosZonaHoraria), 'YYYY-MM-DD HH24:MI:SS') as "IN_RUTEO"
						,TO_NVARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as "OUT_RUTEO",
						'N' as "PROCESADO", "IdOrden" as "SEC_ORDEN",NULL AS "RUT_CLI","Nemotecnico" AS "NEMO",0 as "CANTIDAD",
						0 as "PRECIO",CASE "AccionOrden" WHEN 'COMPRA' THEN 'C' WHEN 'VENTA' THEN 'V' ELSE '' END AS "TIPO_OPERAC",TO_NVARCHAR("FechaIngresoRegistro", 'YYYY-MM-DD') AS "FEC_ING_ORDEN",
						'' as "IND_VALIDEZ","NemoNegociacion",
						TO_NVARCHAR("FechaIngresoRegistro", 'YYYY-MM-DD') as "FEC_VCTO_VALIDEZ",NULL AS "CAN_ASIG_ACUM",'SA' AS "ESTADO","IdMonedaOrden" as "MONEDA",
						CASE 
							WHEN "IdBolsa" = 'BCS' AND "IdTipoInstrumento" = 'ACX' THEN 'OFS'
							WHEN "IdBolsa" = 'BCS' AND "IdTipoInstrumento" = 'CFX' THEN 'OFS'
							WHEN "IdBolsa" = 'BCS' AND "IdTipoInstrumento" = 'ETX' THEN 'OFS'
							WHEN "IdBolsa" = 'BCS' THEN 'XSGO'
							WHEN "IdBolsa" = 'BVL' THEN 'XLIM' 
							WHEN "IdBolsa" = 'BVC' THEN 'XBOG' 
						ELSE 'XSGO' END AS "BOLSA",
						CASE "IdCondicionDeLiquidacion" WHEN 'T0' THEN 'PH' WHEN 'T1' THEN 'PM' WHEN 'T2' THEN 'CN' WHEN 'T3' THEN 'T3' ELSE '' END as "CONDICION_LIQUIDACION",
						' ' AS "SPONSORING_FIRM", 
						CASE "IdTipoInstrumento" 
							WHEN 'ACN' THEN 'AC' 
							WHEN 'ACX' THEN 'AO' 
							WHEN 'ETN' THEN 'AC' 
							WHEN 'ETX' THEN 'AO' 
							WHEN 'CFI' THEN 'CF' 
							WHEN 'CFX' THEN 'CO' 
							WHEN 'MON' THEN 'MO' ELSE '' 
						END AS "MERCADO",
						NULL as "SUB_MERCADO",NULL as "PREPAGO",NULL AS "TASA_MAXIMA",
						NULL AS "TASA_PROPIA",NULL AS "ID_REFERENCIA",NULL AS "FECHA_VENCIMIENTO",NULL AS "DIVISIBLE",'N' AS "OD",0 AS "SEC_ORDEN_OD_COMPRA",0 AS "SEC_ORDEN_OD_VENTA",
						' ' as "OP_INTERNO",' ' AS "SEC_ORDEN_2",' ' AS "ID_CLIENTE",
						SUBSTRING(TO_NVARCHAR(LOCALTOUTC(TO_TIMESTAMP(TO_NVARCHAR(ADD_SECONDS("FechaHoraAnulacion",:v_SegundosZonaHoraria), 'YYYY-MM-DD HH24:MI:SS')),'America/Santiago','platform')),0,23) as "FechaOperacionOrdenUTC"
				FROM #RESULTADO_AnularOrdenMasiva 
				WHERE "Cumple" = 'S'  AND "IdEstadoOrden" = 'OFE';
		
		INSERT INTO "BSCL.Orden.BD::MME.TBSOLICITUDES_FIX5SP2"("SECUENCIA","IN_RUTEO","OUT_RUTEO","PROCESADO","SEC_ORDEN","NEMO","CANTIDAD",
		"CANTIDAD_VISIBLE","PRECIO","TIPO_OPERAC","IND_VALIDEZ","FEC_VCTO_VALIDEZ","TIPO_ORDEN","CUSTOMER_ACCOUNT","TEXTO","SEC_ORDEN_TO_CANCEL","FEC_PROGRAMADA")
		SELECT 	"SECUENCIA","IN_RUTEO","OUT_RUTEO","PROCESADO","SEC_ORDEN" * 10 AS "SEC_ORDEN","NemoNegociacion" AS "NEMO","CANTIDAD",
				0 AS "CANTIDAD_VISIBLE","PRECIO","TIPO_OPERAC","IND_VALIDEZ","FEC_VCTO_VALIDEZ","ESTADO" AS "TIPO_ORDEN",
				'' AS "CUSTOMER_ACCOUNT",'' AS "TEXTO","SEC_ORDEN" AS "SEC_ORDEN_TO_CANCEL",
				SUBSTRING("FechaOperacionOrdenUTC",0,10)||'T'||SUBSTRING("FechaOperacionOrdenUTC",12,23) AS "FEC_PROGRAMADA"
		FROM :v_FFF WHERE v_IntegracionMME = 'S';
		
		v_RegistrosAnulacionRuteoAgente =
						SELECT 'ORDEN' as "NombreEntidad","IdOrden" as "IdEntidad","IdOrden",0 as "IdAsignacion",0 as "IdTransaccion",'' AS "MensajeEventoBitacora",'ENVIADA_ANULAR' as "IdTipoEventoBitacora"
						FROM #RESULTADO_AnularOrdenMasiva 
						WHERE "Cumple" = 'S' AND "IdTipoCanalDistribucion" = 'RUA' AND "IdEstadoOrden" = 'OFE' AND v_IntegracionMME = 'N';
		
		v_RegistrosAnulacionRuteoRetail =
						SELECT 'ORDEN' as "NombreEntidad","IdOrden" as "IdEntidad","IdOrden",0 as "IdAsignacion",0 as "IdTransaccion",'' AS "MensajeEventoBitacora",'ENVIADA_ANULAR' as "IdTipoEventoBitacora"
						FROM #RESULTADO_AnularOrdenMasiva 
						WHERE "Cumple" = 'S' AND "IdTipoCanalDistribucion" = 'RUR' AND "IdEstadoOrden" = 'OFE' AND v_IntegracionMME = 'N';	
						
	   v_RegistrosAnulacionRuteoMME =
						SELECT 'ORDEN' as "NombreEntidad","IdOrden" as "IdEntidad","IdOrden",0 as "IdAsignacion",0 as "IdTransaccion",'' AS "MensajeEventoBitacora",'ENVIADA_ANULAR' as "IdTipoEventoBitacora"
						FROM #RESULTADO_AnularOrdenMasiva 
						WHERE "Cumple" = 'S' AND "IdEstadoOrden" = 'OFE' AND v_IntegracionMME = 'S';						
		
		v_Registros = SELECT * FROM :v_RegistrosAnulacionRuteoRetail
						UNION ALL 
						SELECT * FROM :v_RegistrosAnulacionRuteoAgente
						UNION ALL
						SELECT * FROM :v_RegistrosAnulacionRuteoMME
						UNION ALL
						SELECT * FROM :v_RegistrosAnulacionNoProcesadas;
					
		CALL "BSCL.Orden.BD.Servicios::AgregarBitacoras" (:p_IdEmpresa,:v_Registros);
						
		v_Errores = SELECT * FROM :v_Errores
					UNION ALL
					SELECT "IdOrden" as "IdInterno","IdOrden" as "IdOperacion",'INFORMACION' AS "Resultado",'ORDEN_EN_PROCESO_ANULACION' AS "Codigo",'IdOrden' as "CodigoDato1",CAST("IdOrden" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
					FROM #RESULTADO_AnularOrdenMasiva res WHERE "Cumple" = 'S' AND "IdEstadoOrden" = 'OFE' AND (
						(("IdTipoCanalDistribucion" = 'RUR' OR "IdTipoCanalDistribucion" = 'RUA')  AND v_IntegracionMME = 'N') OR v_IntegracionMME = 'S'
					);
		
		
	 	v_log = SELECT	"IdOrden" as "IdCabeceraLog",
				'ANULACION' AS "IdTipoLog",
				'ORD' as "IdModulo",
		 		:p_IdUsuario as "IdUsuario",
		 		:p_IdEmpresa as 	"IdEmpresa",
		 		TO_NVARCHAR("FechaOperacion", 'YYYY-MM-DD') as	"FechaSistemaLog",
		 		:p_IdUsuario as "IdUsuarioAtendido",
		 		0 AS "IdPersona",
		 		"IdCliente",
				"IdEstadoCliente" as "EstadoCliente",
				"NombreCompleto" as "NombreCliente",
				"TipoDocumentoIdentidad" as "IdTipoDocumentoIdentidad",
				"DocumentoIdentidad" as "IdDocumentoIdentidad",
				"IdPortafolio" as "IdPortafolio",
				"IdEstadoPortafolio" as "EstadoPortafolio",
				'ORDEN' as "IdTipoRegistro", 
				"IdOrden" as "IdRegistro",
				"IdEstadoOrden" as "EstadoRegistro",
				"IdTipoOperacion",
				"IdOrden" as "Folio",
				"Cantidad",
				"PrecioOrden" as "PrecioTasa",
				"MontoOrden" as "Monto",
				"IdMonedaOrden" as "IdMonedaOrigen",
				"IdMonedaLiquidacionOrden" as "IdMonedaLiquidacion",
				"TipoCambio",
				"Nemotecnico" as "Nemo",
				0 as "IdProceso",
				'' as "EstadoProceso",
				'' as "NombreProceso"
			FROM #RESULTADO_AnularOrdenMasiva WHERE "Cumple" = 'S';
			
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		SIGNAL SALIDA_CONTROLADA;

	END;
END;
