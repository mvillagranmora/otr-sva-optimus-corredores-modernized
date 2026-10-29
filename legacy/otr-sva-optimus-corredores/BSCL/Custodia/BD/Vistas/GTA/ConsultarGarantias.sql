SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias";
GO

CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" AS 
WITH 
InstrumentoDER AS (
  SELECT "Nemotecnico", "IdTipoInstrumento"
  FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"
  WHERE "IdTipoInstrumento" = 'DER'
),
InstrumentoFWD AS (
  SELECT "IdTipoInstrumento", "Nombre"
  FROM "BSCL.Plataforma.BD::INS.TipoInstrumento"
  WHERE "IdTipoInstrumento" = 'FWD'
)
SELECT 
	 GAR."IdGarantia"
    ,GAR."FolioGarantia"
    ,GAR."FolioCarteraOrigen"
    ,GAR."FolioCarteraGarantizada"
    ,GAR."EsEspecifica"
    ,GAR."IdInstrumentoCarteraGarantizada"
    ,CASE 
    	WHEN GAR."IdConcepto" IN ('GARDEV','GARDVA') 
			THEN InstrumentoDER."Nemotecnico"
		WHEN GAR."IdConcepto" = 'GTAFWD' 
			THEN InstrumentoFWD."IdTipoInstrumento" || ' - ' || InstrumentoFWD."Nombre"
		ELSE INSGARANT."Nemotecnico" 
   	END 																			AS "NemoGarantizado"
    ,CASE 
    	WHEN GAR."IdConcepto" IN ('GARDEV','GARDVA') 
			THEN InstrumentoDER."IdTipoInstrumento"
		WHEN GAR."IdConcepto" = 'GTAFWD' 
			THEN InstrumentoFWD."IdTipoInstrumento"
		ELSE INSGARANT."IdTipoInstrumento" 
 	END 																			AS "IdTipoInstrumentoGarantizado"
    ,GAR."IdContraparteGarantia"
    ,COALESCE(CNT."RazonSocial",'') 												AS "RazonSocialContraparte"
    ,GAR."IdOrigenGarantia"
    ,ORIGAR."Nombre" 																AS "NombreOrigenGarantia"
    ,GAR."IdEspecieGarantia"
    ,COALESCE(ESPGAR."Descripcion",'') 												AS "DescripcionEspecie"
    ,GAR."FechaIngresoRegistro"
    ,GAR."FechaModificacionRegistro"
    ,GAR."IdEmpresa"
    ,GAR."IdUsuario"
    ,GAR."IdModulo"
    ,GAR."IdCliente"
    ,GAR."IdPortafolio"
    ,CLI."NombreCompleto" 															AS "NombreCliente"
    ,CLI."DocumentoIdentidad"
    ,POR."CodigoPortafolio"
    ,GAR."IdEstado"
    ,EST."Nombre" 																	AS "NombreEstado"
    ,CAR."IdTipoProducto" AS "IdTipoProductoOperacionOrigen"
    ,GAR."IdTipoCanalIngreso"
    ,GAR."IdMonedaSistema"
    ,CONCAT(COALESCE(CONCAT (MONSIS."Codigo", ' - '),''), COALESCE(MONSIS."Nombre",''))	AS "NombreMonedaSistema"
    ,GAR."CodigoIFRS"
    ,GAR."IdConcepto"
    ,CON."Nombre" 																	AS "NombreConcepto"
    ,CAR."IdTipoOperacion" 															AS "IdTipoOperacionOperacionOrigen"
    ,GAR."FechaOperacion"
    ,GAR."FechaVencimientoOperacion" 
    ,GAR."PlazoEnDiasOperacion"
    ,GAR."FolioExternoReferencia"
    ,CAR."IdMesa" 																	AS "IdMesaOperacionOrigen"
    ,CAR."IdBolsa" 																	AS "IdBolsaOperacionOrigen"
--  ,J."Nombre" AS "NombreBolsa"
    ,GAR."IdSucursal"
    ,GAR."IdAgente"
    ,GAR."IdCondicionLiquidacion"
    ,GAR."FechaLiquidacion"
    ,GAR."IdClasificacionTipoInstrumento"
    ,CLATIPINS."Nombre" 															AS "ClasificacionTipoInstrumento"
    ,GAR."IdTipoInstrumento" 
    ,TIPINST."Nombre" 																AS "NombreTipoInstrumento"
    ,GAR."IdInstrumento"
--	,GAR."Nemotecnico"
    ,CASE 
        WHEN INSTAVAL."IdClasificacionTipoInstrumento" = 'IF' AND IFNULL(GAR."NemoSVS",'') <> '' 
        	THEN GAR."NemoSVS" ELSE GAR."Nemotecnico" 
    END 																			AS "Nemotecnico"
    ,GAR."IdMonedaEmision"
    ,CASE
        WHEN GAR."IdOrigenGarantia" = 'CUS' 
        	THEN COALESCE(INSTAVAL."Nemotecnico", '')
        WHEN GAR."IdOrigenGarantia" = 'CTA' 
        	THEN 'GARANTIA EN EFECTIVO'
        WHEN GAR."IdOrigenGarantia" = 'ESP' 
        	THEN 'GARANTIA EN ESPECIE'
    END 																			AS "NombreInstrumento"
    ,GAR."IdEmisor"
    ,GAR."NemoSVS"
    ,GAR."IdTipoReajuste"
    ,GAR."FechaEmisionInstrumento"
    ,GAR."FechaVencimientoInstrumento"
    ,GAR."EsFungible"
    ,COALESCE(CAR."IdBoveda",'')   													AS "IdBovedaOperacionOrigen"
    ,GAR."IdMonedaInstrumento"
    ,GAR."MontoGarantiaMonedaInstrumento"
    ,CONCAT(COALESCE(CONCAT (MONINS."Codigo", ' - '),''), COALESCE(MONINS."Nombre",'')) 	AS "NombreMonedaInstrumento"
    ,CAR."IdTipoComprobante" AS "IdTipoComprobanteOperacionOrigen"
    ,CAR."FolioComprobante" AS "FolioComprobanteOperacionOrigen"
    ,CAR."FechaComprobante" AS "FechaComprobanteOperacionOrigen"   
    ,CASE 
    	WHEN GAR."IdOrigenGarantia" = 'CUS' AND GAR."IdClasificacionTipoInstrumento" = 'IF' 
    		THEN GAR."ValorRescate" ELSE GAR."Cantidad" 
    END 																			AS "Cantidad"
    ,GAR."PrecioGarantia"
    ,GAR."TasaGarantia"
    ,GAR."MontoGarantia"
    ,GAR."CorteMinimo"
    ,GAR."ValorRescate"
    ,GAR."PrecioCompraOperacionOrigen"
    ,GAR."TasaCompraOperacionOrigen"
    ,GAR."PrecioPromedioCompraOperacionOrigen"
    ,GAR."MontoPrecioPromedioCompraOperacionOrigen"
    ,GAR."MontoPrecioPromedioMonedaInstrumentoOperacionOrigen"
    ,CAR."ValorTipoCambioCierre"
    ,GAR."INGFechaOperacion"
    ,GAR."INGFechaLiquidacion"
--  ,CASE 
--		WHEN GAR."IdOrigenGarantia" = 'CUS' AND GAR."IdClasificacionTipoInstrumento" = 'IF' 
--			THEN GAR."INGValorRescate" 
--		ELSE GAR."INGCantidad" 
--	END AS "INGCantidad"   
    ,GAR."INGCantidad"
    ,CASE 
    	WHEN GAR."IdOrigenGarantia" = 'CUS' 
        	THEN CASE 
	        		WHEN GAR."IdClasificacionTipoInstrumento" = 'IF' 
						THEN GAR."INGValorRescate" 
                  	ELSE GAR."INGCantidad" 
            END
    	WHEN GAR."IdOrigenGarantia" IN ('CTA','ESP') 
        	THEN 1
	 END 																			AS "CantidadGarantia" 
    ,GAR."INGPrecioGarantia"
    ,GAR."INGTasaGarantia"
    ,GAR."INGValorRescate"
    ,GAR."INGMontoATasaMercadoActual" 												AS "INGMonto"
    ,GAR."INGMontoATasaCompraActual"
    ,GAR."INGMontoCompraActualMonedaInstrumento"    
    ,GAR."INGMontoGarantiaActualMonedaInstrumento"
    ,GAR."INGMontoGarantiaProximoMonedaInstrumento"      
    ,GAR."INGMontoMercadoActualMonedaInstrumento"
    ,GAR."INGMontoMercadoProximoMonedaInstrumento"
    ,CASE GAR."IdOrigenGarantia"
        WHEN 'CUS' THEN GAR."INGMontoMercadoActualMonedaInstrumento"	-- Monto valorizado
        WHEN 'CTA' THEN GAR."INGMontoGarantiaActualMonedaInstrumento"   -- Para garantias en dinero no se valoriza
        WHEN 'ESP' THEN GAR."MontoGarantiaMonedaInstrumento"            -- Para garantias en especies no se valoriza
     END 																			AS "MontoValorizadoMonedaInstrumento"
    ,CASE GAR."IdOrigenGarantia"
        WHEN 'CUS' THEN GAR."INGMontoATasaMercadoActual"   -- Monto valorizado
        WHEN 'CTA' THEN GAR."INGMontoATasaGarantiaActual"  -- Para garantias en dinero no se valoriza
        WHEN 'ESP' THEN GAR."MontoGarantia"               -- Para garantias en especies no se valoriza
     END 																			AS "MontoValorizado"
    ,GAR."INGMontoATasaGarantiaActual"
    ,GAR."INGMontoATasaCompraAnterior"
--  ,GAR."INGPrecioMercadoActual"
  	,GAR."INGTasaMercadoActual"            											AS "INGTasaMercadoActualMayorMenorValor"
    ,CAR."INGPrecioMercadoActual"
    ,CAR."INGTasaMercadoActual"
    ,GAR."INGMontoATasaMercadoActual"
    ,GAR."INGMontoATasaMercadoAnterior"
    ,INSTAVAL."IdTipoInstrumento" 													AS "IdTipoInstrumentoAval"
    ,INSTAVAL."NombreTipoInstrumento" 												AS "NombreTipoInstrumentoAval"
    ,INSTAVAL."IdClasificacionTipoInstrumento" 										AS "IdClasificacionTipoInstrumentoAval"
    ,INSTAVAL."NombreClasificacionTipoInstrumento" 									AS "NombreClasificacionTipoInstrumentoAval"
    ,CASE 
    	WHEN GAR."IdClasificacionTipoInstrumento" = 'IF' 
    		THEN TRIM(SUBSTRING(CONCAT(IFNULL(EMI."ValorClasificacionRiesgoIIF",''),'   '),1,3)) 
    	ELSE INSCR."ValorClasificacionRiesgo" 
    END 																			AS "ValorClasificacionRiesgo" 
    ,INSCR."IdEntidadClasificadoraRiesgo" 											AS "IdEntidadClasificadoraRiesgo"
    ,TIPINSCR."IdTipoClasificacionRiesgo" 												AS "IdTipoClasificacionRiesgo"
    ,GAR."INGDurationCompraActual"
    ,GAR."INGPrecioMercadoProximo"
    ,GAR."INGTasaMercadoProximo"
    ,GAR."INGMontoATasaCompraProximo"
    ,GAR."INGMontoATasaMercadoProximo"
    ,GAR."INGDurationMercadoActual"
    ,GAR."INGMontoInteresCompraProximo"
    ,GAR."INGDurationCompraProximo"
    ,GAR."INGMontoReajusteCompraProximo"
    ,GAR."INGFuenteTasaPrecioMercadoActual" 											AS "FuenteTasaPrecioMercado"
    ,GAR."INGFechaValorizacionActual" 												AS "FechaValorizacionGarantia"
    ,GAR."INGPrecioMercadoAnterior"
    ,CASE
    	WHEN GAR."IdOrigenGarantia" = 'CUS' AND GAR."IdClasificacionTipoInstrumento" = 'IF'  
    		THEN GAR."EGRValorRescate" 
    	ELSE GAR."EGRCantidad" 
    END 																			AS "EGRCantidad"
    ,GAR."EGRMonto"
    ,GAR."EGRMontoMonedaInstrumento"
    ,GAR."EGRMontoMonedaAdicional"
    ,GAR."EGRFechaLiquidacion"
    ,GAR."EGRFechaOperacion"
    ,CAR."ISIN"
    ,GAR."INGMontoInteresCorridoGarantiaActual" 										AS "MontoInteresCorrido"
    ,GAR."INGMontoReajusteCorridoGarantiaActual" 										AS "MontoReajusteCorrido"
    ,GAR."INGMontoInteresCorridoMercadoActual" 											AS "MontoInteresCorridoMercado"
    ,GAR."INGMontoReajusteCorridoMercadoActual" 										AS "MontoReajusteCorridoMercado"
    ,GAR."INGFechaValorizacionAnterior"
    ,GAR."INGFechaValorizacionActual"
    ,(CAR."CorteMinimo" * GAR."INGCantidad") 											AS "ValorNominal"
    ,GAR."EGRIdConcepto"
    ,CAR."IdCuentaCustodioPortafolio"
    ,ENT."IdEntidadDestinoGarantia" 													AS "IdEntidadDestinoGarantiaEmpresa"
    ,CASE 
    	WHEN GAR."IdOrigenGarantia" = 'CTA' AND GAR."EsEspecifica" = 'S' 
    		THEN GAR."EGRIdOperacion"
        ELSE CAR."EGRIdOperacion" 
    END 																			AS "IdOperacionOrigenGarantia"
    ,CAR."TipoCarteraNormativa" 													AS "TipoCarteraNormativa"
    ,CONCUS."ConCertificadoAutomatico"
    ,CAR."IdRegimenFondoMutuo"
    ,GAR."INGMontoMercadoAnteriorMonedaInstrumento"
    ,GAR."INGMontoCompraAnteriorMonedaInstrumento"
--CELCONDOR-456
	,GAR."FolioComprobante"															AS "FolioComprobanteGarantia"     
FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen"
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" EMI ON GAR."IdEmisor" = EMI."IdEmisor"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" CON ON CON."IdConcepto" = GAR."IdConcepto"
LEFT JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia" CONCUS ON CONCUS."IdConcepto" = GAR."IdConcepto"  AND CONCUS."NombreEntidad" = 'CUS.GARANTIA'
LEFT JOIN "BSCL.Custodia.BD::GTA.EspecieEnGarantia" ESPGAR ON ESPGAR."IdEspecieGarantia" = GAR."IdEspecieGarantia"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLATIPINS ON CLATIPINS."IdClasificacionTipoInstrumento" = GAR."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INSTAVAL ON INSTAVAL."IdInstrumento" = GAR."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINST ON TIPINST."IdTipoInstrumento" = GAR."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" TIPINSCR ON TIPINSCR."IdTipoInstrumento" = INSTAVAL."IdTipoInstrumento" AND TIPINSCR."EntidadClasificadoraRiesgoPrincipal" = 'S'
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" INSCR ON INSCR."IdInstrumento" = INSTAVAL."IdInstrumento" AND INSCR."IdEntidadClasificadoraRiesgo" = TIPINSCR."IdEntidadClasificadoraRiesgo"  
LEFT JOIN "BSCL.Custodia.BD::GTA.OrigenGarantia" ORIGAR ON ORIGAR."IdOrigenGarantia" = GAR."IdOrigenGarantia"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON GAR."IdEmpresa" = EMP."IdEmpresa"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" CNT ON CNT."IdContraparte" = GAR."IdContraparteGarantia"
LEFT JOIN "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" ENT ON ENT."IdPersona" = EMP."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONSIS ON MONSIS."IdMoneda" = GAR."IdMonedaSistema"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONINS ON MONINS."IdMoneda" = GAR."IdMonedaInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON EST."IdEstado" = GAR."IdEstado" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INSGARANT ON INSGARANT."IdInstrumento" = GAR."IdInstrumentoCarteraGarantizada"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = GAR."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = GAR."IdPortafolio"
CROSS JOIN InstrumentoDER 
CROSS JOIN InstrumentoFWD 
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
