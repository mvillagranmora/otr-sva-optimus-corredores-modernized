SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.PLANTILLA_COBRO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.PLANTILLA_COBRO" AS

	SELECT 
		* 
	FROM (
		SELECT 
			PC."IdPerfilCobros",
			CASE PC."IdPerfilCobros" 
				WHEN 'PLANTILLA MBI 2' THEN 'PLMBI2' 
				WHEN 'PLANTILLA MBI1' THEN 'PLMBI1' 
				WHEN 'GLOBAL ADVISOR 1' THEN 'GADV1' 
				WHEN 'GLOBAL ADVISOR 2' THEN 'GADV2' 
				
				ELSE LEFT(DPC."IdPerfilCobros",10) END AS PAR_TPC_ID -- 
			,LEFT(H1."ValorOrigen",2) AS MERCADO
			,RPAD(h2."ValorOrigen",4) AS PRO_ID
			,RPAD(h3."ValorOrigen",4) AS OPE_ID
			,RPAD(C."IdCobro",4) AS COB_ID -- 
			,'O' AS PCO_IND_AMB
			,'S' AS PCO_IND_DEFINIDO
			,'N/A' AS PCO_PERIODO
			, VC."ValorCobro" AS PCO_VAL_CARGO
			, CASE WHEN C."FormaCobro" = 'F' THEN VC."IdMoneda" ELSE '%' END AS PCO_UNI_CARGO
			, 'N/A' AS PCO_TIP_VAR
			, 0.0 AS PCO_VAL_VAR
			, 'CLP' AS PCO_MIN_MONEDA
			, 'N/A' AS PCO_ESP_SIMPLAZO
			, '0' AS PCO_ESP_VALPLAZO
			, 'N/A' AS PCO_ESP_EMISOR
			, 'DESCONOCID' AS par_ben_tipo
			, 0.0 AS pco_ben_meta
			, 'N/A' AS pco_ben_unidad
			, 'N/A' AS pco_ben_moneda
			, 0.0 AS pco_porcentaje_agresor
			, 0.0 AS pco_porcentaje_agredido
			, 0.0 AS pco_porcentaje_od
			,ROW_NUMBER() OVER (PARTITION BY DPC."IdPerfilCobros" , H1."ValorOrigen" , h2."ValorOrigen" ,h3."ValorOrigen",LEFT(C."IdCobro" ,2)
				ORDER BY DPC."IdPerfilCobros" , H1."ValorOrigen" , h2."ValorOrigen" ,
					h3."ValorOrigen",C."IdCobro" , DPC."IdTipoOperacion"  DESC, DPC."IdTipoProducto" DESC , DPC."IdCanalIngreso" desc)  as IdRow
		FROM "BSCL.Plataforma.BD::COM.ValorCobro" VC
		INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" C ON VC."IdCobro" = C."IdCobro"
		INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilCobros" DPC ON DPC."IdValorCobro" = VC."IdValorCobro"
		INNER JOIN "BSCL.Plataforma.BD::COM.PerfilCobros" PC ON PC."IdPerfilCobros" = DPC."IdPerfilCobros"
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIOP ON TIOP."IdTipoProducto" = VC."IdTipoProducto"
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" TIOPTINS ON TIOPTINS."IdTipoOperacion" = TIOP."IdTipoOperacion"
		INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" H1 ON H1."TipoValor" ='TipoInstrumentoSegunMercado' AND H1."ValorDestino" = TIOPTINS."IdTipoInstrumento"
		INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" H2 ON H2."ValorDestino" = VC."IdTipoProducto" AND H2."TipoValor" = 'PlantillaProducto'
		INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" H3 ON H3."TipoValor" = 'TIPO_OPERACION' AND H3."ValorDestino" = TIOPTINS."IdTipoOperacion"
		--JOIN REQUERIDO
		WHERE TIOP."AccionOrden" IS NOT NULL and PC."EsPerfilPersonalizado" = 'N'
	) AS b1
	WHERE IdRow = 1	


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.PLANTILLA_COBRO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
