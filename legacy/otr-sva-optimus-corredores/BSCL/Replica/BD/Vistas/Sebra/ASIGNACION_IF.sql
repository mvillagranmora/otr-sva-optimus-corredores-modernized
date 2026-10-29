SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.ASIGNACION_IF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.ASIGNACION_IF" AS

SELECT
	TO_INT(ASIG."IdAsignacion") AS SEC_ASIGNACION, --	DECIMAL	10
	LEFT(TO_VARCHAR (TRA."FechaOperacion",'YYYY-MM-DD HH24:MI:SS'),26) AS FEC_TRANS, --	CHAR	26
	LPAD(LEFT(TRA."Folio",8),8) AS FOLIO_TRANS, --	CHAR	8
	TRA."IdBolsa" AS IND_BOLSA, --	CHAR	3
	substr(TOPE."AccionOperacion",1,1) AS TIPO_OPERAC, --	CHAR	1
	TO_INT(ASIG."IdOrden") as SOL_SEC, --	DECIMAL	10
	TRA."IdEmisor" AS EMISOR, --	CHAR	16
	CASE WHEN left(TRA."IdTipoReajuste",2) = 'CL'THEN 'NR'ELSE left(TRA."IdTipoReajuste",2) END AS TIPO_REAJ, --	CHAR	2
	TO_VARCHAR (CASE WHEN TRA."FechaVencimiento" < '1900-01-01' THEN '1900-01-01' ELSE TRA."FechaVencimiento" END ,'YYYY-MM-DD') AS FEC_VCTO, --	VARCHAR	30
	LEFT(coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( DOC."DocumentoIdentidad" AS VARCHAR(11))), ''),11) AS RUT_CLI, --	CHAR	11 documento
	TO_INT(COALESCE(POR."CodigoPortafolio",'-1')) as  CUENTA, --	INTEGER	10
	ASIG."CantidadAsignada" CANTIDAD, --	DECIMAL	6 //preguntar 
	CASE WHEN CL."Codigo" = 'CN' THEN '' ELSE CL."Codigo" END AS IND_LIQUID, --	CHAR	2
	'A' AS IND_TIPO_ASIGNAC, --	CHAR	1
	LEFT(TRA."Nemotecnico",16) AS NEMO, --	CHAR	16
    TO_INT(ASIG."FolioCartera") AS SEC_CAR_INVER, --	DECIMAL	10
	ASIG."ValorRescateAsignado" VAL_RESC_ASIG, --	DECIMAL	18
	ASIG."MontoAsignado" MONTO, --	DECIMAL	18
	'0000' AS COD_AGE_ATEN, --	CHAR	4
	TRA."Tasa" TASA, --	DECIMAL	7
	TO_INT(ASIG."IdMovimientoComprobante") AS SEC_MOVTO_FAC, --	DECIMAL	10
	TO_VARCHAR (CASE WHEN TRA."FechaVencimiento" < '1900-01-01' THEN '1900-01-01' ELSE TRA."FechaVencimiento" END ,'YYYY-MM-DD') AS FEC_VCTO_TP, --	CHAR	10
	CASE WHEN TRA."EsAPlazo" = 'S' THEN 'TS' ELSE '' END  AS IND_SIMUL, --	CHAR	2
	TRA."Tasa" TASA_EMISION, --	DECIMAL	10
	0.0 AS SPREAD_TASA_EMISION, --	DECIMAL	10
	'' AS MODALIDAD_PAGO, --	CHAR	1
	'' AS PERIODICIDAD_PAGO, --	CHAR	1
	TO_VARCHAR (CASE WHEN TRA."FechaEmision" < '1900-01-01' THEN '1900-01-01' ELSE TRA."FechaEmision" END ,'YYYY-MM-DD') AS FECHA_EMISION, --	CHAR	10
	COALESCE(TRA."Precio",0) PRECIO, --	DECIMAL	5
	COALESCE(TRA."EsCuentaPropia",'N') AS IND_CUENTA_PROPIA, --	CHAR	1
	0 AS CODCATEGORIAIFRS, --	TINYINT	3
	COALESCE(TRA."ValorParidad",0) PARIDAD_MORIGEN, --	DECIMAL	15
	COALESCE(TRA."Monto",0) CAPTACION_MORIGEN, --	DECIMAL	18
	COALESCE(TRA."IdMonedaEmision",'CLP') AS MON_MORIGEN, --	CHAR	3
	'' AS RAZON_OD, --	VARCHAR	100
	TO_VARCHAR (CASE WHEN ASIG."FechaAsignacion" < '1900-01-01' THEN '1900-01-01' ELSE ASIG."FechaAsignacion" END ,'YYYY-MM-DD HH24:MI:SS.FF3') AS FECHA_HORA_ASIGNACION --	VARCHAR	30

FROM "BSCL.Orden.BD::ORD.Asignacion" AS ASIG 
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRA ON (ASIG."IdTransaccion" = TRA."IdTransaccion")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  TOPE ON  (TOPE."IdTipoOperacion" = TRA."IdTipoOperacion") 
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS ORD ON (ASIG."IdOrden" = ORD."IdOrden")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (ORD."IdPortafolioCliente" = POR."IdPortafolio" )
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (POR."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (CLI."IdPersona" =  PER."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad")
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CL ON  (CL."IdCondicionDeLiquidacion" = TRA."IdCondicionDeLiquidacion") 

WHERE TRA."IdClasificacionTipoInstrumento" = 'IF' AND ASIG."IdEstadoAsignacion" <> 'ANU'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.ASIGNACION_IF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
