SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.ASIGNACION_RF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.ASIGNACION_RF" AS

SELECT
	TO_INT(ASIG."IdAsignacion") AS SEC_ASIGNACION, --	DECIMAL	10
	LEFT(TO_VARCHAR (TRA."FechaOperacion",'YYYY-MM-DD HH24:MI:SS'),26) AS FEC_TRANS, --	CHAR	26
	LPAD(LEFT(TRA."Folio",8),8)  AS FOLIO_TRANS, --	CHAR	8
	TRA."IdBolsa" AS IND_BOLSA, --	CHAR	3
	substr(TOPE."AccionOperacion",1,1) AS TIPO_OPERAC, --	CHAR	1
	TO_INT(ASIG."IdOrden") AS SOL_SEC, --	DECIMAL	10
	LEFT(TRA."Nemotecnico",16) AS NEMO, --	CHAR	16
	LEFT(coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( DOC."DocumentoIdentidad" AS VARCHAR(11))), ''),11) AS RUT_CLI, --	CHAR	11 documento
	TO_INT(COALESCE(POR."CodigoPortafolio",'-1')) AS CUENTA, --	INTEGER	10
	ASIG."CantidadAsignada" AS CAN_ASIGNAC, --	DECIMAL	14
	CASE WHEN CL."Codigo" = 'CN' THEN '' ELSE CL."Codigo" END AS IND_LIQUID, --	CHAR	'' 2
	'A' AS IND_TIPO_ASIGNAC, --	CHAR	1 PREGUNTAR
	TRA."Precio" AS PRECIO, --	DECIMAL	5
	TO_DECIMAL(ASIG."FolioCartera") AS SEC_CAR_INVER, --	DECIMAL	8
	0 AS NUM_GRUPO, --	DECIMAL	10
	0 AS SEC_DIST, --	DECIMAL	10
	ASIG."MontoAsignado" AS MONTO, --	DECIMAL	18
	'0000' AS COD_AGE_ATE, --	CHAR	4 PREGUNTAR
	TO_INT(ASIG."IdMovimientoComprobante") AS SEC_MOVTO_FAC, --	DECIMAL	10
	TRA."Tasa" AS TIR, --	DECIMAL	5 BRF
	TO_VARCHAR (CASE WHEN TRA."FechaVencimiento" < '1900-01-01' THEN '1900-01-01' ELSE TRA."FechaVencimiento" END ,'YYYY-MM-DD')  AS FEC_VCTO_TP, --	CHAR	10
	CASE WHEN TRA."EsAPlazo" = 'S' THEN 'TS' ELSE '' END  AS IND_SIMUL, --	CHAR	2
	TRA."EsCuentaPropia" AS IND_CUENTA_PROPIA, --	CHAR	1
	0 AS CODCATEGORIAIFRS, --	TINYINT	3
	'' AS RAZON_OD, --	VARCHAR	100
	coalesce (TRA."ValorParidad", 1 ) AS PARIDAD_MORIGEN, --	DECIMAL	15  BRF
	coalesce (TRA."Monto",0) AS CAPTACION_MORIGEN, --	DECIMAL	18  BRF
	coalesce (TRA."IdMonedaEmision",'CLP') AS MON_MORIGEN, --	CHAR	3  BRF
	TO_TIMESTAMP(TO_VARCHAR (CASE WHEN ASIG."FechaAsignacion" < '1900-01-01' THEN '1900-01-01' ELSE ASIG."FechaAsignacion" END ,'YYYY-MM-DD HH24:MI:SS.FF7'))   AS FECHA_HORA_ASIGNACION --	VARCHAR	30

FROM "BSCL.Orden.BD::ORD.Asignacion" AS ASIG 
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRA ON (ASIG."IdTransaccion" = TRA."IdTransaccion")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  TOPE ON  (TOPE."IdTipoOperacion" = TRA."IdTipoOperacion") 
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS ORD ON (ASIG."IdOrden" = ORD."IdOrden")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (ORD."IdPortafolioCliente" = POR."IdPortafolio" )
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (POR."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (CLI."IdPersona" =  PER."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad")
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CL ON  (CL."IdCondicionDeLiquidacion" = TRA."IdCondicionDeLiquidacion") 

WHERE TRA."IdClasificacionTipoInstrumento" = 'RF' AND ASIG."IdEstadoAsignacion" <> 'ANU'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.ASIGNACION_RF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
