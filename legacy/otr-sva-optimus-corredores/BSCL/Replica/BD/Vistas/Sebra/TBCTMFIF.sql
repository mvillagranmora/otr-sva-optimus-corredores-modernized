SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTMFIF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTMFIF" AS

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
	TO_DECIMAL(MC."IdMovimientoComprobante",8,0) AS SEC_MOVTO_FAC,
	CAST(COALESCE(MC."FechaSistema", '1900-01-01') AS CHAR(10)) AS FEC_MVTO,
	CAST(COALESCE(SUC."CodigoSucursal", '') AS VARCHAR(50)) AS COD_SUC,
	CAST(COALESCE(COD_EMP_TRUN4."CodigoEmpleado", '') AS CHAR(4)) AS COD_AGENTE,
	TO_DECIMAL(COALESCE(MC."IdOrden", 0),8,0) AS SEC_ORDEN,
	CAST(1 AS DECIMAL(2,0)) AS LIN_ORDEN,
    CAST(COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(MC."DocumentoIdentidad"),'') as char(11)) AS RUT_DEST, -- CHAR(11)
	TO_DECIMAL(POR."CodigoPortafolio",2,0) AS SEC_RUT_DEST,
	RPAD(LEFT(coalesce(MC."IdEmisor",''),10),10) as EMISOR,
	coalesce(CASE MC."IdTipoReajuste" WHEN 'CLP' THEN 'NR' WHEN 'USD' THEN 'US' WHEN 'UF' THEN 'UF' ELSE '' END ,'') AS TIPO_REAJ,
	coalesce(left(MC."FechaVencimiento",10),'1900-01-01') AS FEC_VCTO,
	CAST(REPLACE(LEFT(MC."FechaTransaccion",19),' ',':') AS CHAR(26)) AS FEC_TRANS,
	COALESCE(LPAD(LEFT(MC."FolioTransaccion",8),8) ,'') AS FOLIO_TRANS, --char](8) NOT NULL, *********
	LEFT(MC."IdBolsa",3) AS IND_BOLSA,
	CASE MC."IdTipoOperacion"
			WHEN 'COMIIF' THEN 'C'
			WHEN 'ANCIIF' THEN 'C'
			WHEN 'VENIIF' THEN 'V'
			WHEN 'ANVIIF' THEN 'V'
			ELSE ''
		END  AS TIPO_OPERAC,
	CAST(COALESCE("BSCL.Replica.BD.Servicios::MapeoTIPO_COMP_FAC"(MC."IdTipoComprobante") , '') AS CHAR(2)) AS TIPO_COMP_FAC,
	CAST(COALESCE(C."FolioComprobante", 0) AS DECIMAL(8,0)) AS FOLIO_COMP_FAC,
	COALESCE(DENSE_RANK() OVER (PARTITION BY CD."IdComprobante" ORDER BY MCD."IdComprobanteDetalle"), 0) AS  LIN_COMP_FAC,
	1 AS CANTIDAD,
	CAST(MC."IdBoveda" as char(3)) AS COD_BOVEDA,
	CAST(COALESCE(CORR."CodigoCorredora",0) AS DECIMAL(3,0)) AS COD_CORR_CONTRA,
	CASE
			WHEN MC."IdTipoOperacion" = 'COMIIF' THEN 'CIIF'
			WHEN MC."IdTipoOperacion" = 'VENIIF' THEN 'VIIF'
			WHEN MC."IdTipoOperacion" = 'ANCIIF' THEN 'NCIF'
			WHEN MC."IdTipoOperacion" = 'ANVIIF' THEN 'NVIF'
			ELSE ''
		END AS COD_MOVTO,
	TO_DECIMAL(COALESCE(CMCT."Comision", 0),14,4) AS COMISION,
	TO_DECIMAL(COALESCE(CMCT."Derechos",0),14,4) AS DERECHOS,
	COALESCE(CASE MC."IdEstadoFacturacion"
			WHEN 'ENV' THEN 'E' --'FACTURADO' ESTADO TRANSITORIO ENVIADO A FACTURAR
			WHEN 'FAC' THEN 'E' --'EMITIDO' ESTADO FINAL
			WHEN 'ANU' THEN 'A'
			WHEN 'ING' THEN 'I' --'INGRESADO' ESTADO INICIAL
			ELSE ''
		END, '') AS EST_MOVTO_FAC,
	CAST(COALESCE(MC."FechaEmision", '1900-01-01') AS CHAR(10)) AS FEC_EMI,	
	CAST(COALESCE(MC."FechaSistema", '1900-01-01') AS CHAR(10)) AS FEC_ING_MVTO,
	CAST(CASE WHEN LEFT(COALESCE(MC."FechaOrden", '1900-01-01'),10) = '0001-01-01' THEN '1900-01-01' ELSE COALESCE(MC."FechaOrden", '1900-01-01') END AS CHAR(10)) AS FEC_ORDEN,
	CAST(COALESCE(C."FolioComprobanteReferencia", 0) AS DECIMAL(8,0)) AS FOLIO_COMP_ASOC,
	CAST(0 AS DECIMAL(8,0)) FOLIO_PET_SUC,
	TO_DECIMAL(COALESCE(CMCT."Gastos",0),14,4) AS GASTOS,
	'N' AS IND_AFEC_COMIS,
	'N' AS IND_AFEC_DERES,
	'N' AS IND_AFEC_GASTO,
	'S' AS IND_AFEC_IVA,
	'N' AS IND_CHEQUE,
	'N' AS IND_COMP_INDIV,
	'N' AS IND_CTA_PRO,
	'' AS IND_DEST,
	'' AS IND_FACTURA,
	LEFT(COALESCE(LIQ."Codigo",''),2) AS IND_LIQUID,
	MC."IndicadorOrdenDirecta" AS IND_OD,
	'' AS IND_PAGO_COBRO,
	CASE WHEN MC."IdFormaCustodia" = 'NOM' THEN 'N' ELSE 'C' END AS IND_TIT_CUS,
	TO_DECIMAL(COALESCE(MC."MontoMovimientoComprobante",0),17,2) AS MONTO,
	CAST(COALESCE(MC."NemotecnicoInstrumento", '') AS CHAR(16)) AS NEMO,
	'' AS ORIGEN_ORDEN,
	'INIF' AS PRODUCTO,
	'' AS RUEDA,
	CAST(MC."FolioCartera" AS DECIMAL(8,0)) AS SEC_CAR_INVER,
	CAST(CASE WHEN MC."IdEstadoFacturacion" IN ('FAC','ENV') THEN MC."SecuenciaMovimiento" ELSE 0 END AS DECIMAL(8,0)) AS SEC_MOVTO,
	TO_DECIMAL(MC."Tasa",10,6) AS TASA,
	TO_DECIMAL(MC."ValorRescate",15,4) AS VAL_RESC,
	'' AS IND_CONV,
	'' AS FEC_VCTO_TP,
	'' AS IND_SIMUL,
	CAST(0 AS DECIMAL(10,6)) AS TASA_SIMUL,
	CAST(0 AS DECIMAL(10,6)) AS TASA_EMISION,
	CAST(0 AS DECIMAL(10,6)) AS SPREAD_TASA_EMISION,
	'' AS MODALIDAD_PAGO,
	'' AS PERIODICIDAD_PAGO,
	CAST('' AS CHAR(10)) AS FECHA_EMISION,
	0 AS CODCATEGORIAIFRS,
	CAST(0 AS DECIMAL(15,6)) AS PARIDAD_MORIGEN,
	TO_DECIMAL(COALESCE(MC."Monto", 0),18,2) AS CAPTACION_MORIGEN,
	MC."IdMoneda" AS MON_MORIGEN
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MC
LEFT JOIN v_comisiones CMCT ON CMCT."IdMovimientoComprobante" = MC."IdMovimientoComprobante"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR on MC."IdPortafolio" = POR."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" LIQ on LIQ."IdCondicionDeLiquidacion" = MC."IdCondicionLiquidacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUC on SUC."IdSucursal" = MC."IdSucursalEmpleadoEjecutivoPortafolio"
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" MCD ON MCD."IdMovimientoComprobante" = MC."IdMovimientoComprobante"
LEFT JOIN (
		SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
			SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
		) WHERE RANK < 10
	) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = MC."IdEmpleadoDigitador"
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" CD ON MCD."IdComprobanteDetalle" = CD."IdComprobanteDetalle"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" C ON MC."IdComprobante" = C."IdComprobante"
LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON CORR."IdCorredora" = MC."IdCorredoraContraparte"  AND MC."IdBolsa"  = CORR."IdBolsa"
WHERE MC."IdClasificacionTipoInstrumento" IN ('IF') AND POR."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R')
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTMFIF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
