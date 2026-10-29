SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTMFRV"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTMFRV" AS

WITH v_comisiones as (
		select COMP."IdMovimientoComprobante",
		COALESCE(MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN cob."MontoCobro" END),0) as "Comision",
		COALESCE(MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN cob."MontoCobro" END),0) as "Derechos",
		COALESCE(MAX(CASE WHEN "CodigoCobroClasificacion" IN('GASTO','IMPUESTO') THEN cob."MontoCobro" END),0) as "Gastos"
		from "BSCL.Facturacion.BD::FAC.MovimientoComprobante" comp
		left join (
			SELECT "IdMovimientoComprobante", CLASIF."CodigoCobroClasificacion" , SUM("MontoMovimientoComprobanteCobro") AS "MontoCobro"
			FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" COMCOBRO
			LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" COBRO ON COBRO."IdCobro" = COMCOBRO."IdCobro" 
			LEFT JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" CLASIF ON COBRO."IdCobroClasificacion" = CLASIF."IdCobroClasificacion"
			WHERE COMCOBRO."IdEstado" = 'VIG'
			group by "IdMovimientoComprobante", CLASIF."CodigoCobroClasificacion"
		) as cob on cob."IdMovimientoComprobante" = comp."IdMovimientoComprobante"
		group by comp."IdMovimientoComprobante"
	)
SELECT
	CAST(MC."IdMovimientoComprobante" AS DECIMAL(8,0)) AS SEC_MOVTO_FAC,
	CASE 
		WHEN MC."IdClasificacionTipoInstrumento" IN ('RF', 'IF', 'FM') THEN MC."IdClasificacionTipoInstrumento"
		WHEN MC."IdTipoInstrumento" IN ('OSA','SIM','ACN') THEN 'AC'
 	    WHEN MC."IdTipoInstrumento" IN ('ETN') THEN 'EN'
 	    WHEN MC."IdTipoInstrumento" IN ('ETX') THEN 'ET'
 	    WHEN MC."IdTipoInstrumento" IN ('MIL') THEN 'ST'
 	    WHEN MC."IdTipoInstrumento" IN ('ACX') THEN 'AO'
	    WHEN MC."IdTipoInstrumento" IN ('CFX') THEN 'CO'
	    WHEN MC."IdTipoInstrumento" IN ('CFM') THEN 'CM'
        WHEN MC."IdClasificacionTipoInstrumento" = 'SP' THEN 'MO'
        WHEN MC."IdTipoInstrumento" = 'FWD' THEN 'FW'
        WHEN MC."IdTipoInstrumento" = 'CFI' THEN 'CF'
        ELSE ''END AS "MERCADO", -- CHAR(2)
	CAST(COALESCE(MC."FechaSistema", '1900-01-01') AS CHAR(10)) AS FEC_MVTO,
	CAST(COALESCE(SUC."CodigoSucursal", '') AS VARCHAR(50)) AS COD_SUC,
	CAST(COALESCE(COD_EMP_TRUN4."CodigoEmpleado", '') AS CHAR(4)) AS COD_AGENTE,
	TO_DECIMAL(COALESCE(MC."IdOrden", 0),8,0) AS SEC_ORDEN,
	CAST(1 AS DECIMAL(2,0)) AS LIN_ORDEN,
	"BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(MC."DocumentoIdentidad") AS RUT_DEST,
	TO_DECIMAL(POR."CodigoPortafolio",2,0) AS SEC_RUT_DEST,
	CASE MC."IdTipoInstrumento" WHEN 'SIM' THEN  	CAST(COALESCE(MC."NemotecnicoSubyacente", '') AS CHAR(16))
	ELSE CAST(COALESCE(MC."NemotecnicoInstrumento", '') AS CHAR(16)) END AS NEMO,
	CAST(COALESCE(MC."FechaTransaccion", '1900-01-01')AS CHAR(26)) AS FEC_TRANS,
	CAST(LPAD(COALESCE(MC."FolioTransaccion", ''),8) AS CHAR(8)) AS FOLIO_TRANS,
	LEFT(MC."IdBolsa",3) AS IND_BOLSA,
	COALESCE(CASE LEFT(MC."IdTipoOperacion", 3)
			WHEN 'COM' THEN 'C'
			WHEN 'VEN' THEN 'V'
			ELSE ''
		END, '')                                                                     AS TIPO_OPERAC,
	CAST(COALESCE("BSCL.Replica.BD.Servicios::MapeoTIPO_COMP_FAC"(MC."IdTipoComprobante") , '') AS CHAR(2)) AS TIPO_COMP_FAC,
	CAST(COALESCE(C."FolioComprobante", 0) AS DECIMAL(8,0)) AS FOLIO_COMP_FAC,CD."IdComprobante",
	COALESCE(DENSE_RANK() OVER (PARTITION BY CD."IdComprobante" ORDER BY MCD."IdComprobanteDetalle"), 0) AS  LIN_COMP_FAC,
	TO_DECIMAL(COALESCE(MC."Cantidad" ,0),14,2) AS CANTIDAD,
	LEFT(MC."IdBoveda",3) AS COD_BOVEDA,
	CAST(COALESCE(CORCOD."CodigoCorredora",0) AS DECIMAL(3,0)) AS COD_CORR_CONTRA,
	COALESCE(CASE
			WHEN MC."IdTipoOperacion" = 'COMSPT' THEN 'CFRD'
			WHEN MC."IdTipoOperacion" = 'VENSPT' THEN 'VFRD'
			WHEN MC."IdTipoOperacion" = 'COMSIM' AND MC."EsAPlazo" = 'N' THEN 'CIRV'
			WHEN MC."IdTipoOperacion" = 'COMSIM' AND MC."EsAPlazo" = 'S' THEN 'VITP'
			WHEN MC."IdTipoOperacion" = 'VENSIM' AND MC."EsAPlazo" = 'N' THEN 'VIRV'
			WHEN MC."IdTipoOperacion" = 'VENSIM' AND MC."EsAPlazo" = 'S' THEN 'CITP'
			WHEN MC."IdTipoOperacion" LIKE '%COM%' AND MC."IdClasificacionTipoInstrumento" = 'RV' THEN 'CIRV'
			WHEN MC."IdTipoOperacion" LIKE '%ANC%' AND MC."IdClasificacionTipoInstrumento" = 'RV' THEN 'NCRV'
			WHEN MC."IdTipoOperacion" LIKE '%VEN%' AND MC."IdClasificacionTipoInstrumento" = 'RV' THEN 'VIRV'
			WHEN MC."IdTipoOperacion" LIKE '%ANV%' AND MC."IdClasificacionTipoInstrumento" = 'RV' THEN 'NVRV'
			ELSE ''
		END, '')  AS COD_MOVTO,
	TO_DECIMAL(COALESCE(CMCT."Comision", 0),14,4) AS COMISION,
	TO_DECIMAL(COALESCE(CMCT."Derechos",0),14,4) AS DERECHOS,
	COALESCE(CASE MC."IdEstadoFacturacion"
			WHEN 'ENV' THEN 'E' --'FACTURADO' ESTADO TRANSITORIO ENVIADO A FACTURAR
			WHEN 'FAC' THEN 'E' --'EMITIDO' ESTADO FINAL
			WHEN 'ANU' THEN 'A'
			WHEN 'ING' THEN 'I' --'INGRESADO' ESTADO INICIAL
			ELSE ''
		END, '') AS EST_MOVTO_FAC, 
	CAST(COALESCE(MC."FechaSistema", '1900-01-01') AS CHAR(10)) AS FEC_ING_MVTO,
	CASE WHEN CAST(COALESCE(MC."FechaOrden", '1900-01-01') AS CHAR(10)) = '0001-01-01' THEN '1900-01-01' ELSE CAST(COALESCE(MC."FechaOrden", '1900-01-01') AS CHAR(10)) END AS FEC_ORDEN,
	CAST(COALESCE(MC."FechaLiquidacion", '1900-01-01') AS CHAR(10)) AS FEC_VCTO_TP,
	CAST(COALESCE(C."FolioComprobanteReferencia", 0) AS DECIMAL(8,0)) AS FOLIO_COMP_ASOC,
	CAST(0 AS DECIMAL(8,0)) FOLIO_PET_SUC,
	TO_DECIMAL(COALESCE(CMCT."Gastos",0),14,4) AS GASTOS,
	'' AS IND_AFEC_COMIS,
	'' AS IND_AFEC_DERES,
	'' AS IND_AFEC_GASTO,
	'' AS IND_AFEC_IVA,
	'' AS IND_CHEQUE,
	'' AS IND_COMP_INDIV,
	'' AS IND_CTA_PRO,
	'' AS IND_DEST,
	'' AS IND_FACTURA,
	LEFT(COALESCE(LIQ."Codigo",''),2) AS IND_LIQUID,
	COALESCE(MC."IndicadorOrdenDirecta",'') AS IND_OD,
	'' AS IND_PAGO_COBRO,
	CASE MC."IdTipoOperacion" WHEN 'COMSIM' THEN 'TS' WHEN 'VENSIM' THEN 'TS' ELSE '' END AS IND_SIMUL,
	CASE WHEN MC."IdFormaCustodia" = 'NOM' THEN 'N' ELSE 'C' END AS IND_TIT_CUS,
	CASE WHEN MC."EsAPlazo" = 'N' THEN TO_DECIMAL(MC."Monto",17,2) ELSE 0 END AS MONTO,
	TO_DECIMAL(MC."MontoAPlazo",17,2) AS MONTO_APLAZO,
	'' AS ORIGEN_ORDEN,
	TO_DECIMAL(COALESCE(MC."Precio", 0),17,6) AS PRECIO,
	MC."IdTipoProducto" AS PRODUCTO,
	'' AS RUEDA,
	CAST(0 AS DECIMAL(8,0)) AS SEC_CAR_INVER,
	CAST(0 AS DECIMAL(8,0)) AS SEC_MOVTO,
	COALESCE(CASE WHEN MC."IdClasificacionTipoInstrumento" = 'SP' THEN MC."IdTransaccion" ELSE MC."IdMovimientoRelacionado" END,0) AS SEC_MOVTO_ASOC,
	CAST(0 AS DECIMAL(10,6)) AS SPREAD,
	TO_DECIMAL(MC."Tasa",10,6) AS TASA,
	CAST(0 AS DECIMAL(10,6)) AS TASA_AGE,
	CAST(COALESCE(MC."IdConceptoMovimiento", '') AS CHAR(2))AS CONCEPTO_MVTO,
	'' AS IND_CONV,
	'' AS IND_PRECIO_PRIMA,
	0 AS PLAZO_PRIMA,
	0 AS COD_OPE_EXT,
	0 AS OPE_INT,
	'' AS CCA_CODIGO,
	'' AS IND_RUTEO,
	0 AS CODCATEGORIAIFRS,
	'' AS PROCESADO_CIRCULAR,
	LEFT(MC."IdMoneda",3) AS MON_MORIGEN,
	TO_DECIMAL(COALESCE(MC."Monto", 0),18,2) AS CAPTACION_MORIGEN,
	CAST(0 AS DECIMAL(15,6)) AS PARIDAD_MORIGEN,
	'' AS COD_CORR_INTER,
	CAST(0 AS DECIMAL(10,6)) AS MONTO_SOLICITUD,
	'' AS IND_TIT_CCTE,
	'' AS OBSERVACION1985,
	0 AS FAC_PRE_PROM,
	'' AS RESUMEN_NEMO,
	'' AS MONTO_MODIF,
	'' AS REFERENCIA,
	'' AS NUM_CONTRATO,
	
	COALESCE(CAST(MC."PrecioRaiz" AS DECIMAL(17,6)), 0.0) AS "PRECIO_CONVERTIDO",
	COALESCE(CAST(MC."MontoRaiz" AS DECIMAL(17,2)), 0.0) AS "MONTO_CONVERTIDO",
	COALESCE(CAST(TRA."IdMonedaRaiz" AS CHAR(3)), '') AS "MONEDA_CONVERTIDA",
	--COALESCE(NULLIF(CAST(MC."Monto" AS DECIMAL(15,6)),0), 1)/ COALESCE(NULLIF(CAST(MC."MontoRaiz" AS DECIMAL(15,6)), 0), 1) AS "PARIDAD_CONVERTIDA",
	0 AS PARIDAD_CONVERTIDA,
	COALESCE(CAST(INS."Nemotecnico" AS CHAR(16)), '') AS "NEMO_NEGOCIACION",
	0.0 AS "PARIDAD_MX"
	
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MC
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR on MC."IdPortafolio" = POR."IdPortafolio"
LEFT JOIN v_comisiones CMCT ON CMCT."IdMovimientoComprobante" = MC."IdMovimientoComprobante"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUC on SUC."IdSucursal" = MC."IdSucursalEmpleadoEjecutivoPortafolio"
LEFT JOIN (
		SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
			SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
		) WHERE RANK < 10
	) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = MC."IdEmpleadoDigitador"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" LIQ on LIQ."IdCondicionDeLiquidacion" = MC."IdCondicionLiquidacion"
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" MCD ON MCD."IdMovimientoComprobante" = MC."IdMovimientoComprobante"
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" CD ON MCD."IdComprobanteDetalle" = CD."IdComprobanteDetalle"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" C ON MC."IdComprobante" = C."IdComprobante"
LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORCOD ON MC."IdCorredoraContraparte" = CORCOD."IdCorredora" AND CORCOD."IdBolsa" = MC."IdBolsa"
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON MC."IdTransaccion" = TRA."IdTransaccion" AND TRA."IdClasificacionTipoInstrumento" IN ('RV', 'RF', 'IF')
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON TRA."IdInstrumentoNegociacion" = INS."IdInstrumento"

WHERE MC."IdTipoProducto" NOT IN ('INIF','INRF')
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTMFRV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

