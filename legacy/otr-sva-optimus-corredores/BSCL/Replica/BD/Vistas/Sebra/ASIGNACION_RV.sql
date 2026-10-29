SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.ASIGNACION_RV"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.ASIGNACION_RV" AS

SELECT 
	TO_INT(ASIG."IdAsignacion") AS SEC_ASIGNACION, --	DECIMAL	10
--	LEFT(COALESCE( hpr."ValorOrigen", TRA."IdTipoInstrumento"),2) AS MERCADO, --	CHAR	2 preguntar homologacion 
	
	LEFT(COALESCE( hpr."ValorOrigen", 
			case when TPRO."IdTipoProducto"  = 'INSI' then 'AC'
					when TPRO."IdTipoProducto"  = 'INFU' then 'FU'
					when ORD."IdProducto"  = 'INEN' then 'EN'
					when ORD."IdProducto"  = 'INEX' then 'ET'
				 else  TPRO."IdTipoProducto"  END)
			
			, 2) AS "MERCADO", -- TIPO :char
	
	
	LEFT(TO_VARCHAR (TRA."FechaOperacion",'YYYY-MM-DD HH24:MI:SS'),26) AS FEC_TRANS, --	CHAR	26
	LPAD(LEFT(TRA."Folio",8),8)  AS FOLIO_TRANS, --	CHAR	8 // PREGUNTAR 
	TRA."IdBolsa" AS IND_BOLSA, --	CHAR	3
	COALESCE(substr(TOPE."AccionOperacion",1,1),'') AS TIPO_OPERAC, --	CHAR	1
	TO_INT(ASIG."IdMovimientoComprobante") AS SEC_MOVTO_FAC, --	DECIMAL	10 preguntar 
	LEFT(TRA."Nemotecnico",16) AS NEMO, --	CHAR	16
	TO_INT(ASIG."IdOrden") AS SOL_SEC, --	DECIMAL	10 idorden
	LEFT(coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( DOC."DocumentoIdentidad" AS VARCHAR(11))), ''),11) AS RUT_CLI, --	CHAR	11 documento
	TO_INT(COALESCE(POR."CodigoPortafolio",'-1')) AS CUENTA, --	INTEGER	10 
	ASIG."CantidadAsignada" AS CAN_ASIGNAC, --	DECIMAL	17
	
	TO_TIMESTAMP(TO_VARCHAR (CASE WHEN TRA."FechaVencimiento" < '1900-01-01' THEN '1900-01-01' ELSE TRA."FechaVencimiento" END ,'YYYY-MM-DD')) AS FEC_VCTO_TP, --	VARCHAR	30
	
	CASE WHEN CL."Codigo" = 'CN' THEN '' ELSE CL."Codigo" END   AS IND_LIQUID, --	CHAR	2
	'A' AS IND_TIPO_ASIGNAC, --	CHAR	1
	CASE WHEN TRA."EsAPlazo" = 'S' THEN 'TS' ELSE '' END  AS IND_SIMUL, --	CHAR	2
	TRA."Precio" AS PRECIO, --	DECIMAL	15
	ASIG."MontoAsignado" AS MONTO, --	DECIMAL	18
	'0000' AS COD_AGE_ATEN, --	CHAR	4 sacar de orden 
	TRA."EsCuentaPropia" AS IND_CUENTA_PROPIA, --	CHAR	1
	0 AS CODCATEGORIAIFRS, --	TINYINT	3
	'' AS RAZON_OD, --	VARCHAR	100
	0.0 AS MONTO_SOLICITUD, --	DECIMAL	17
	TO_INT(ASIG."IdAsignacionGrupo") AS GRUPO_ID,	--INTEGER 10
	'S' AS IND_TIT_CCTE, --	CHAR	1
	'N' AS IND_REFACT, --	VARCHAR	3
	
	TO_TIMESTAMP(TO_VARCHAR (CASE WHEN ASIG."FechaAsignacion" < '1900-01-01' THEN '1900-01-01' ELSE ASIG."FechaAsignacion" END ,'YYYY-MM-DD HH24:MI:SS.FF7'))  AS FECHA_HORA_ASIGNACION --	VARCHAR	30

FROM "BSCL.Orden.BD::ORD.Asignacion" AS ASIG 
inner JOIN "BSCL.Orden.BD::ORD.Orden" AS ORD ON (ASIG."IdOrden" = ORD."IdOrden")
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRA ON (ASIG."IdTransaccion" = TRA."IdTransaccion")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  TOPE ON  (TOPE."IdTipoOperacion" = TRA."IdTipoOperacion") 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" TPRO ON (TOPE."IdTipoProducto" = TPRO."IdTipoProducto")
LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."ValorDestino" = ORD."IdTipoInstrumento"   AND hpr."ValorOrigen" NOT IN ('CM' , 'EN' , 'ET')   )

LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (ORD."IdPortafolioCliente" = POR."IdPortafolio" )
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (POR."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (CLI."IdPersona" =  PER."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad")
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CL ON  (CL."IdCondicionDeLiquidacion" = TRA."IdCondicionDeLiquidacion") 

WHERE TRA."IdClasificacionTipoInstrumento" = 'RV' AND ASIG."IdEstadoAsignacion" <> 'ANU'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.ASIGNACION_RV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
