SET SCHEMA "BSCL";
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTDTCF";

CREATE VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTDTCF" AS
SELECT
	 CAR."Nemotecnico" AS NEMO										--char	16
	,COM."IdTipoComprobante" AS TIPO_COMP_FAC						--char	2
	,COM."FolioComprobante" AS FOLIO_COMP_FAC						--decimal	0
	, 0 AS LIN_COMP_FAC												--decimal	0
	,CAR."INGCantidad" AS CANTIDAD									--decimal	0
	,CASE WHEN CAR."IdTipoOperacion" = 'VENRF' THEN 'VIRF'
		  WHEN CAR."IdTipoOperacion" = 'COMRF' THEN 'CIRF'
		  WHEN CAR."IdTipoOperacion" = 'COMACN' THEN 'CIRV'
		  WHEN CAR."IdTipoOperacion" = 'VENACN' THEN 'VIRV'
		  ELSE ''
	  END AS COD_MOVTO												--char	4	  
	,CAR."IdEmisor" AS EMISOR										--char	10
	,CAR."FechaEmisionInstrumento" AS FEC_EMI						--char	10
	,CAR."FechaVencimientoInstrumento" AS FEC_VCTO					--char	10
	,'' AS IND_TIT_CUS												--char	1
	,'' AS MERCADO													--char	2
	,CAR."INGMontoCompraActualMonedaInstrumento" AS MONTO			--decimal	0
	,CAR."INGPrecioCompra" AS PRECIO								--decimal	0
	,CAR."FolioCartera" AS SEC_CAR_INVER							--decimal	0
	,CAR."INGTasaCompra" AS TASA									--decimal	0
	,CASE WHEN TOPE."Accion" = 'COMPRA' THEN 'C'
		  WHEN TOPE."Accion" = 'VENTA' THEN 'V'
		  ELSE ''
	  END AS TIPO_OPERAC											--char	1  compra o venta
	,CAR."IdTipoReajuste" AS TIPO_REAJ								--char	2
	, 0 AS VAL_RESC													--decimal	0
	,'' AS FOLIO_TRANS												--char	8
	,'' AS IND_OD													--char	2
	,'' AS IND_DEST													--char	1
	,'' AS IND_BOLSA												--char	3
	, 0 AS TASA_EMISION												--decimal	0
	, 0 AS SPREAD_TASA_EMISION										--decimal	0
	,'' AS MODALIDAD_PAGO											--char	1
	,'' AS PERIODICIDAD_PAGO										--char	1
	,'' AS FECHA_EMISION											--char	10
	, 0 AS PARIDAD_MORIGEN											--decimal	0
	, 0 AS CAPTACION_MORIGEN										--decimal	0
	,CASE WHEN CAR."CodigoIFRS" = 'CUS' THEN 1			
		  ELSE 0
	  END AS CODCATEGORIAIFRS										--tinyint	0
	,CAR."IdMonedaInstrumento" AS MON_MORIGEN						--char	3
	,'' AS RUEDA													--char	2
	, 0 AS PRECIO_PROM_COMP_ANT										--decimal	0
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COM ON COM."IdComprobante" = CAR."FolioComprobante"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPE ON TOPE."IdTipoOperacion" = COM."IdTipoOperacion"
WHERE COM."IdFormaCustodia" IN ('CUS','NOM')
  AND CAR."IdClasificacionTipoInstrumento" = 'RF'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaTBCTDTCF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
