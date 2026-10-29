-- Optimizacion: se reemplaza el LEFT JOIN a la vista "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica"
-- (~200 columnas, 7 joins, ORDER BY en la propia definicion de la vista) por una subconsulta acotada a las
-- columnas realmente consumidas por este servicio, sin ORDER BY y sin los joins que no aportan a esas columnas
-- (INS.TipoInstrumento, INS.TipoInstrumentoClasificacionRiesgo, INS.InstrumentoValorClasificacionRiesgo).
-- Se corrige ademas el join a PLA.Configuracion: la vista lo hace contra origen."IdEmpresa" (NULL cuando el
-- subyacente no tiene cartera de origen), aqui se usa :p_IdEmpresa directo.
-- Se expone "IdSubyacente" (en vez de "FolioCarteraRelacionada") porque este servicio la referencia por
-- IdSubyacente = ultSub."IdSubyacente".
WITH SUB_BASICA AS (
    SELECT
         suby."IdSubyacente"
        ,suby."IdClasificacionTipoInstrumento"
        ,suby."IdTipoInstrumento"
        ,suby."IdInstrumento"
        ,suby."Nemotecnico"
        ,suby."NemoSVS"
        ,CASE WHEN coalesce(suby."IdEmisor",Inst."IdEmisor",'') <> '' THEN coalesce(suby."IdEmisor",Inst."IdEmisor",'') ELSE Inst."IdEmisor" END AS "IdEmisor"
        ,suby."EsFungible"
        ,suby."IdCarteraRetirada"
        ,IFNULL(suby."IdTipoReajuste",'') AS "IdTipoReajusteSubyacente"
        ,CASE WHEN IFNULL(suby."IdMonedaInstrumento",'')<>'' THEN IFNULL(suby."IdMonedaInstrumento",'') ELSE COALESCE(Inst."IdMoneda",'') END AS "IdMonedaInstrumento"
        ,CASE WHEN IFNULL(suby."IdMonedaEmision",'')<>'' THEN IFNULL(suby."IdMonedaEmision",'') ELSE COALESCE(Inst."IdMoneda",'') END AS "IdMonedaEmision"
        ,CASE WHEN IFNULL(origen."IdMonedaSistema",'') <> '' THEN IFNULL(origen."IdMonedaSistema",'') ELSE IFNULL(conf."Valor",'CLP') END AS "IdMonedaSistema"
        ,suby."INGPrecioSubyacente"
        ,suby."INGMontoATasaSubyacenteActual"
        ,suby."INGMontoSubyacenteActualMonedaInstrumento"
        ,suby."INGPrecioMercadoActual"
        ,suby."INGTasaMercadoActual"
        ,suby."INGMontoMercadoActualMonedaInstrumento"
        ,suby."INGMontoATasaMercadoActual"
        ,suby."INGMontoInteresCorridoSubyacenteActual"
    FROM "BSCL.Custodia.BD::CUS.Subyacente" AS suby
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Inst ON suby."IdInstrumento" = Inst."IdInstrumento"
    LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS origen ON (origen."IdCartera" = suby."IdCarteraRetirada")
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf ON (conf."IdEmpresa" = :p_IdEmpresa AND conf."CodigoParametro" = 'PLA.MonedaSistema')
)
SELECT
"IdClasificacionTipoInstrumento"
   ,"IdTipoInstrumento"
   ,"IdInstrumento"
   ,"Nemotecnico"
   ,"IdEmisor"
   ,"EsFungible"
   ,"IdCliente"
   ,"IdPortafolio"
   ,"Producto"
   ,"IdBoveda"
   ,"IdCuentaCustodioPortafolio"
   ,"NumeroCuentaBoveda"
   ,"Cantidad"
   ,"PrecioCompra"
   ,"ValorCompra"
   ,"MontoCompra"
   ,"INGPrecioMercadoActual"
   ,"INGTasaMercadoActual"
   ,"ValorMercado"
   ,"MontoMercado"
   ,"IdMonedaInstrReajuste"
   ,"IdMonedaEmision"
   ,"IdMonedaPrincipal"
   ,"InteresCorrido"
   ,"IdMatrizAgrupador"
   ,"EsADC"
   ,"IdPersona"
   ,"RazonSocialInstrumento"
   , "EsCustodiaExterna"
FROM
(
SELECT
				sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,sby."IdInstrumento" as "IdInstrumento"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
			   ,sby."IdEmisor" as "IdEmisor"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."EsFungible" else 'N' end as "EsFungible"
			   ,EMP."IdCliente"
			   ,EMP."IdPortafolio"
			   ,'N' AS "EsCustodiaExterna"
			   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
			   ,CASE
			   		WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto
			   		WHEN ifnull(EMP."IdBoveda",'') <> '' THEN EMP."IdBoveda"
			   		WHEN ifnull(orig."IdBoveda",'') <> '' THEN orig."IdBoveda"
			   		ELSE :v_IdBovedaDefecto
			   	END AS "IdBoveda"
			   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
			   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"			   
			   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF'  then EMP."INGValorRescate" else EMP."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,COALESCE(sby."INGPrecioSubyacente",0) as "PrecioCompra"						
			   ,COALESCE(sby."INGMontoATasaSubyacenteActual",0) as "MontoCompra" 			
			   ,COALESCE(sby."INGMontoSubyacenteActualMonedaInstrumento",0) as "ValorCompra"	
			   ,COALESCE(sby."INGPrecioMercadoActual",0) as "INGPrecioMercadoActual" 			
			   ,COALESCE(sby."INGTasaMercadoActual",0)as "INGTasaMercadoActual" 				
			   ,COALESCE(sby."INGMontoMercadoActualMonedaInstrumento",0) as "ValorMercado"
			   ,COALESCE(sby."INGMontoATasaMercadoActual",0) as "MontoMercado"
			   ,CASE
			   		WHEN ifnull(sby."IdMonedaInstrumento",'') <> '' THEN sby."IdMonedaInstrumento"
			   		WHEN ifnull(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
			   		ELSE ifnull(sby."IdMonedaSistema",'')
			   	END AS "IdMonedaInstrReajuste"
			   ,sby."IdMonedaEmision"
			   ,sby."IdMonedaSistema" AS "IdMonedaPrincipal"
			   ,sby."INGMontoInteresCorridoSubyacenteActual" as "InteresCorrido"
			   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
               ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
               ,CLI."IdPersona"
               ,inst3."RazonSocial" AS "RazonSocialInstrumento"
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS EMP
			-- Optimizacion: se inlinean las vistas CUS.ConsultarUltimoMovimientoSubyacente y
			-- CUS.ConsultarUltimoMovimientoCartera (agregaciones MAX()/GROUP BY sin filtro sobre toda la
			-- tabla). ultPrt se acota por :p_IdEmpresa antes de agrupar (agregar un WHERE de igualdad antes
			-- del GROUP BY no cambia el resultado de los grupos que sobreviven, solo descarta grupos
			-- irrelevantes). ultSub no se acota: CUS.Subyacente no tiene columna "IdEmpresa" propia y
			-- unirla con Cartera para acotarla arriesgaba cambiar el agrupamiento del MAX(), asi que se
			-- preserva identica a la definicion original de la vista.
			INNER JOIN (
				SELECT "FolioCarteraRelacionada", MAX("IdSubyacente") as "IdSubyacente"
				FROM "BSCL.Custodia.BD::CUS.Subyacente"
				WHERE "IdEstado" <> 'ANU'
				GROUP BY "FolioCarteraRelacionada"
			) as ultSub on (EMP."FolioCartera" = ultSub."FolioCarteraRelacionada")
			INNER JOIN (
				SELECT "IdEmpresa", "FolioCartera", MAX("IdCartera") as "IdCartera"
				FROM "BSCL.Custodia.BD::CUS.Cartera"
				WHERE "IdEmpresa" = :p_IdEmpresa
				GROUP BY "IdEmpresa","FolioCartera"
			) as ultPrt on (EMP."IdEmpresa" = ultPrt."IdEmpresa" and EMP."FolioCarteraRelacionada" = ultPrt."FolioCartera")
			INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as prt on (prt."IdCartera" = ultPrt."IdCartera")
			LEFT JOIN SUB_BASICA as sby on sby."IdSubyacente" = ultSub."IdSubyacente"
			LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as orig on (orig."IdCartera" = sby."IdCarteraRetirada")
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =:p_IdEmpresa AND CLI."IdCliente" = EMP."IdCliente")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = EMP."IdPortafolio" AND Portafolio."IdCliente" = EMP."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = EMP."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC ON TPO_CC."CodigoTipoIndicador" = 'CC'
			LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" AND ATR_CC."IdTipoEntidad" = 'CLIENTE' AND ATR_CC."IdEntidad" = TO_VARCHAR(EMP."IdCliente")
			WHERE
				EMP."IdEmpresa" =:p_IdEmpresa
			AND EMP."IdEstado" = 'VIG'

			<<FECHACONSULTA.EMP.p_TipoFechaConsulta>>
			AND EMP."IdCliente" = :p_IdCliente
			<<CASE.EMP.p_IdPortafolio>>
			<<CASE.sby.p_IdClasificacionTipoInstrumento>>
			<<CASE.sby.p_IdTipoInstrumento>>
			<<CASE.sby.p_IdInstrumento>>
			<<CASE.sby.p_Nemotecnico>>

			AND EMP."IdTipoInstrumento" IN ('EMP','ERF')
			AND IFNULL(ATR_CC."ValorIndicador",'N') = 'S'
			
	)
