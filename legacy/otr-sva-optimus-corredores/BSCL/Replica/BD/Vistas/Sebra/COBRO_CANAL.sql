SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.COBRO_CANAL"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.COBRO_CANAL" AS

Select DISTINCT
            
				DPC."IdPerfilCobros" AS PAR_TPC_ID -- 
				,case 
					WHEN TIOPTINS."IdTipoInstrumento"  = 'CFI' THEN 'CF' 
					WHEN TIOPTINS."IdTipoInstrumento"  = 'CFX' THEN 'CO' 
					WHEN TIOPTINS."IdTipoInstrumento"  = 'ACN' THEN 'AC' 
					WHEN TIOPTINS."IdTipoInstrumento"  = 'ACX' THEN 'AO' 
					WHEN TIOPTINS."IdTipoInstrumento"  = 'ETN' THEN 'EN' 
					WHEN TIOPTINS."IdTipoInstrumento"  = 'ETX' THEN 'ET'  
					WHEN TIOPTINS."IdTipoInstrumento"  = 'MIL' THEN 'ST'
					WHEN CLA."IdClasificacionTipoInstrumento" = 'RF' THEN 'RF'
					WHEN CLA."IdClasificacionTipoInstrumento" = 'IF' THEN 'IF'
					ELSE '' END
					AS MERCADO
				,h2."ValorOrigen" AS PRO_ID
				,h3."ValorOrigen" AS OPE_ID
				, C."IdCobro" AS COB_ID -- 
				,'O' AS PCO_IND_AMB
				, 'N/A' AS PCO_ESP_SIMPLAZO
				, '0' AS PCO_ESP_VALPLAZO
				, 'N/A' AS PCO_ESP_EMISOR
				, dpc."IdCanalIngreso" AS CCA_CODIGO
				, VC."ValorCobro" AS COC_VAL_CARGO
				, 0.0 AS COC_VAL_MIN
			FROM "BSCL.Plataforma.BD::COM.ValorCobro" VC
			INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" C ON VC."IdCobro" = C."IdCobro"
			INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilCobros" DPC ON DPC."IdValorCobro" = VC."IdValorCobro"
			INNER JOIN "BSCL.Plataforma.BD::COM.PerfilCobros" PC ON PC."IdPerfilCobros" = DPC."IdPerfilCobros"
			INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIOP ON TIOP."IdTipoProducto" = VC."IdTipoProducto"
			INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" TIOPTINS ON TIOPTINS."IdTipoOperacion" = TIOP."IdTipoOperacion"
			INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS ON TINS."IdTipoInstrumento" = TIOPTINS."IdTipoInstrumento"
			INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA ON TINS."IdClasificacionTipoInstrumento" = CLA."IdClasificacionTipoInstrumento"
			INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" H2 ON H2."ValorDestino" = VC."IdTipoProducto" AND H2."TipoValor" = 'PlantillaProducto'
			INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" H3 ON H3."TipoValor" = 'TIPO_OPERACION' AND H3."ValorDestino" = TIOPTINS."IdTipoOperacion"
			WHERE DPC."IdBolsa" = 'BCS' AND LENGTH(DPC."IdPerfilCobros") <= 10 AND dpc."IdCanalIngreso" != '0' and TIOPTINS."IdTipoInstrumento" not in ('SIM','PRT') AND "Borrado" = 'N'
				AND CASE WHEN TINS."IdClasificacionTipoInstrumento" = 'RF' THEN 
					CASE WHEN TIOPTINS."IdTipoInstrumento" = 'BEM'THEN 1 ELSE 0 END
					ELSE 1
					END = 1
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.COBRO_CANAL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
