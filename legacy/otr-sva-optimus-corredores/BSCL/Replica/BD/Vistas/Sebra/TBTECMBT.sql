SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBTECMBT"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBTECMBT" AS

SELECT
RPAD(DI."DocumentoIdentidad",11) AS RUT_EMPRESA,
RPAD(CASE COM."IdMoneda" 
	WHEN 'CLP' THEN '$'
	WHEN 'EUR' THEN 'EU'
	WHEN 'USD' THEN 'US'
	WHEN 'GBP' THEN '?'
	ELSE ''
	END ,2) AS TIPO_MON,
RPAD(CASE 
	WHEN COM."IdTipoMovimiento" = 'MOVING' AND COM."IdTipoPersona" = 'CLI' THEN
		CASE COM."IdMoneda"
			WHEN 'CLP' THEN 'OI'
			WHEN 'USD' THEN 'ID'
			ELSE ''
		END
	WHEN COM."IdTipoMovimiento" = 'MOVEGR' AND COM."IdTipoPersona" = 'CLI' THEN
		CASE COM."IdMoneda"
			WHEN 'CLP' THEN 'OP'
			WHEN 'USD' THEN 'ED'
			ELSE ''
		END
	WHEN COM."IdTipoMovimiento" = 'MOVING' AND COM."IdTipoPersona" IN ('CNT','PER','PRO') AND COM."IdPersona"  != PE."IdPersona" THEN
		CASE COM."IdMoneda"
			WHEN 'CLP' THEN 'OI'
			WHEN 'USD' THEN 'ID'
			ELSE ''
		END
	WHEN COM."IdTipoMovimiento" = 'MOVING' AND COM."IdTipoPersona" IN ('CNT','PER','PRO','EMP') AND COM."IdPersona"  = PE."IdPersona" THEN 'OI'
	WHEN COM."IdTipoMovimiento" = 'MOVEGR' AND COM."IdTipoPersona" IN ('CNT','PER','PRO','EMP') AND COM."IdPersona"  != PE."IdPersona" THEN
		CASE COM."IdMoneda"
			WHEN 'CLP' THEN 'OP'
			WHEN 'USD' THEN 'ED'
			ELSE ''
		END
	WHEN COM."IdTipoMovimiento" = 'MOVEGR' AND COM."IdTipoPersona" IN ('CNT','PER','PRO','EMP') AND COM."IdPersona"  = PE."IdPersona" THEN 'OP'
END,2) AS TIPO_CMBTE,
CAST(COALESCE(COM."IdComprobante", 0)AS DECIMAL(8,0)) AS FOLIO_CMBTE,
CAST(COALESCE(COM."FechaLiquidacion", '0001-01-01') AS CHAR(10)) AS FEC_LIQUID,
'' AS COD_BCO_PAGO,
'' AS COD_CAJA,
CASE
	WHEN COM."IdTipoMovimiento" = 'MOVING' AND CONCEPTO."ConceptoAbonoReplica" IS NOT NULL AND CONCEPTO."ConceptoAbonoReplica" <> '' THEN CONCEPTO."ConceptoAbonoReplica"
	WHEN COM."IdTipoMovimiento" = 'MOVEGR' AND CONCEPTO."ConceptoCargoReplica" IS NOT NULL AND CONCEPTO."ConceptoCargoReplica" <> '' THEN CONCEPTO."ConceptoCargoReplica"
	ELSE CAST(COALESCE(COM."IdConcepto", '') AS CHAR(2))
END AS CONCEPTO_MVTO,
CAST(COALESCE(COM."NombrePersona", '') AS CHAR(40)) AS EMITIDO_A,
CASE COM."IdEstado"
	WHEN 'ANU' THEN 'A'
	WHEN 'LIQ' THEN 'C'
	ELSE 'I'
	END AS EST_CMBTE,
CAST(COALESCE(COM."FechaMovimiento", '0001-01-01') AS CHAR(10)) AS FEC_ING,
'' AS FORMA_PAGO,
CAST(COALESCE(COM."FechaIngresoRegistro", '0001-01-01') AS CHAR(26)) AS HORA_ING,
'' AS IND_CHEQUE,
CASE WHEN COM."IdTipoPersona" = 'CNT' THEN 
		'P' 
	 ELSE 'C'
END AS IND_DEST,
CASE COM."IdTipoMovimiento"
	WHEN 'MOVING' THEN 'I'
	WHEN 'MOVEGR' THEN 'E'
	ELSE ''
END AS IND_ING_EGR,
CAST('' AS CHAR(1)) AS IND_MARCA_ESP,
CAST(' ' AS CHAR(1)) AS IND_PAGO_COBRO,
TO_DECIMAL(COM."Monto",17,2) AS MONTO,
CAST(0 AS DECIMAL(17,2)) AS MONTO_ACUM,
'' AS OBSERVACION, --?
CAST(COM."DocumentoIdentidad" AS CHAR(11)) AS RUT_DEST, 
CAST(0 AS DECIMAL(8,0)) AS SEC_MOVTO,
CAST(0 AS DECIMAL(2,0)) AS SEC_RUT_DEST,
'' AS TIPO_CAJA,
'' AS UNIDAD_DES,
'' AS UNIDAD_SOL,
CAST(0 AS DECIMAL(7,0)) AS USUARIO_CER,
CAST(0 AS DECIMAL(7,0)) AS USUARIO_SOL,
'' AS TIPO_PROV,
'' AS IND_TRASPASO,
'' AS PORTAFOLIO,
'' AS FPA_ID
FROM "BSCL.Tesoreria.BD::TES.Comprobante" COM
JOIN "BSCL.Plataforma.BD::PLA.Empresa" EM ON COM."IdEmpresa" = EM."IdEmpresa"
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SDC ON EM."IdSocioDeNegocio" = SDC."IdSocioDeNegocio"
JOIN "BSCL.SocioNegocio.BD::PER.Persona" PE ON SDC."IdPersona" = PE."IdPersona"
JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON PE."IdDocumentoIdentidad" = DI."IdDocumentoIdentidad"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONCEPTO ON CONCEPTO."IdConcepto" = COM."IdConcepto"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBTECMBT" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
