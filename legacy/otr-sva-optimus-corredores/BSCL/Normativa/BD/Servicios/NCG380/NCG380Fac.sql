SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Fac";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Fac" (
-- Parametros de Entrada
 IN p_IdSolicitudCircular BIGINT DEFAULT 0,
--VARIABLES DE SALIDA 
 OUT p_out_TablaSalida "BSCL.Normativa.BD::NOR.SALIDA_NCG380FAC" ) LANGUAGE SQLSCRIPT SQL SECURITY INVOKER DEFAULT SCHEMA "BSCL" AS
BEGIN

DECLARE v_FechaInicio,v_FechaTermino DATE DEFAULT '0001-01-01';
DECLARE v_IdEmpresa INT := 0;
DECLARE v_Version,v_FechaVersion NVARCHAR(10) DEFAULT '';

--Se obtiene la fecha de inicio y fecha termino
SELECT CAST(MAX("Valor") AS INT)
INTO v_IdEmpresa
FROM "BSCL.Normativa.BD::NOR.SolicitudParametros"
WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular
	AND "TipoParametro" = 'EMP';

SELECT CAST(UTCTOLOCAL ("Valor", 'America/Santiago','platform') AS DATE)
INTO v_FechaInicio
FROM "BSCL.Normativa.BD::NOR.SolicitudParametros"
WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular
	AND "TipoParametro" = 'FINI';

SELECT  CAST(UTCTOLOCAL ("Valor", 'America/Santiago','platform') AS DATE)
INTO v_FechaTermino
FROM "BSCL.Normativa.BD::NOR.SolicitudParametros"
WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular
	AND "TipoParametro" = 'FTER';

--Se obtiene segun fecha 
IF (:v_FechaInicio = '' OR :v_FechaInicio = '0001-01-01') 
	THEN v_FechaVersion := TO_VARCHAR (CURRENT_UTCTIMESTAMP,'DD-MM-YYYY');
	ELSE v_FechaVersion := TO_VARCHAR (:v_FechaInicio,'DD-MM-YYYY');
END IF;

SELECT
	TOP 1 "P_OUT_VERSION" INTO
		v_Version
	FROM
		"BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"('NCG380FAC',
		:v_FechaVersion);

p_out_TablaSalida = SELECT
	c."FechaTransaccion" 		AS "FechaTransaccion",
	c."DocumentoIdentidad" 		AS "Rut",
	c."CodigoPortafolio" 		AS "Cuenta",
	c."NombreLargoCliente" 		AS "Nombre",
	c."IdBolsa" 				AS "Bolsa",
	c."IdTransaccion" 			AS "Folio",
	c."NemotecnicoInstrumento" 	AS "Instrumento",
	LEFT(IFNULL(c."IdTipoOperacion", ''),
	1) 							AS "TipoOperacion",
	c."IdOrden" 				AS "NumeroOrden",
	c."FolioComprobante" 		AS "NumeroFactura",
	c."Cantidad" 				AS "Cantidad",
	c."Precio" 					AS "Precio",
	c."Monto" 					AS "Monto",
	CASE
    	WHEN SUBSTRING(c."IdMoneda", 1, 3) = 'CLP' THEN '$$' || COALESCE(SUBSTRING(c."IdMoneda", 4), '')
        WHEN SUBSTRING(c."IdMoneda", 1, 3) = 'AUD' THEN 'DA' || COALESCE(SUBSTRING(c."IdMoneda", 4), '')
        ELSE COALESCE(c."IdMoneda",'')
    END 						AS "Moneda",
	c."COMMVTOCMBTE" 			AS "Comision",
	c."DERMVTOCMBTE" 			AS "DerechoBolsa",
 	c."GASMVTOCMBTE" 			AS "Gastos",
	c."IMPMVTOCMBTE" 			AS "Impuesto",
	c."FechaMovimientoComprobante" AS "FechaFactura",
	c."IdComprobanteReferencia" AS "FacturaReferencia",
	c."Anulacion"				AS "Anulacion",
	c."FolioAnulacion" 			AS "FolioAnulacion",
	c."NotaCredito" 			AS "NotaCredito",
	c."FolioNotaCredito" 		AS "FolioNotaCredito",
	c."TipoComprobante" 		AS "TipoComprobante",
	c."FechaLiquidacion" 		AS "FechaLiquidacion",
	c."FechaEmision" 			AS "FechaEmision",
	c."HoraEmision" 			AS "HoraEmision",
	c."InstrumentoNegociacion" 	AS "InstrumentoNegociacion"
FROM
(SELECT
		c."FechaTransaccion" ,
		c."DocumentoIdentidad" ,
		c."CodigoPortafolio",
		c."NombreLargoCliente" ,
		c."IdBolsa",
		c."IdTransaccion" ,
		c."NemotecnicoInstrumento" ,
		c."IdTipoOperacion",
		c."IdOrden" ,
		c."IdComprobante",
		c."Cantidad" ,
		c."Precio" ,
		c."Monto",
		c."IdMoneda",
		COALESCE( c."COMMVTOCMBTE" ,0) AS "COMMVTOCMBTE",
		COALESCE( c."GASMVTOCMBTE" ,0) AS "GASMVTOCMBTE",
		COALESCE( c."DERMVTOCMBTE" ,0) AS "DERMVTOCMBTE",
		COALESCE( c."IMPMVTOCMBTE" ,0) as "IMPMVTOCMBTE",
		c."FechaMovimientoComprobante",
		c."IdComprobanteReferencia" ,
		c."IdInstrumento",
		c."IdClasificacionTipoInstrumento",
		c."Anulacion",
		c."FolioAnulacion",
		c."NotaCredito",
		c."FolioNotaCredito",
		c."TipoComprobante",
		c."FechaLiquidacion",
		c."FechaEmision",
		c."HoraEmision",
		c."FolioComprobante",
		c."InstrumentoNegociacion"
	FROM
(
		SELECT
			TO_CHAR(UTCTOLOCAL(TRANS."FechaOperacion", 'America/Santiago', 'platform'), 'DD-MM-YYYY HH24:MI:SS') AS "FechaTransaccion",
			MVTOCMBTE."DocumentoIdentidad",
			MVTOCMBTE."IdCliente",
			POR."CodigoPortafolio",
			COMP."NombreLargoCliente",
			COMP."IdBolsa",
			COALESCE(NULLIF ( MVTOCMBTE."FolioTransaccion",''),'0') AS "IdTransaccion",
			CASE 
				WHEN MVTOCMBTE."IdTipoInstrumento" = 'SPT' THEN 'DOLAR'
				WHEN MVTOCMBTE."IdClasificacionTipoInstrumento" = 'IF' THEN MVTOCMBTE."NemoSVS" 
				WHEN MVTOCMBTE."NemotecnicoInstrumento" IN ('SIMULTÁNEA COMPRA','SIMULTÁNEA VENTA') THEN MVTOCMBTE."NemotecnicoSubyacente"
				ELSE MVTOCMBTE."NemotecnicoInstrumento"
			END AS "NemotecnicoInstrumento",
			CASE 
				WHEN MVTOCMBTE."IdTipoComprobante" IN ('NCR','NCE','NEO','NCO') THEN 'N'
				WHEN MVTOCMBTE."IdTipoComprobante" IN ('FOE') THEN 'F'
			ELSE MVTOCMBTE."IdTipoOperacion" END  AS "IdTipoOperacion",
			CASE 
				WHEN MVTOCMBTE."IdTipoInstrumento" = 'SPT' THEN COALESCE(MVTOCMBTE."IdOrden", ASIG."IdOrden", 0)
				ELSE COALESCE(MVTOCMBTE."IdOrden",ASIG."IdOrden", 0)
			END AS "IdOrden",
			MVTOCMBTE."IdComprobante",
			CASE
				WHEN MVTOCMBTE."IdClasificacionTipoInstrumento" = 'IF' THEN MVTOCMBTE."ValorRescate"
				WHEN MVTOCMBTE."IdTipoProducto" = 'MEME' THEN COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Cantidad",
				FormCant."PrecioDecimales",
				FormCant."PrecioTrucaRedondea"),
				MVTOCMBTE."Cantidad")
				ELSE MVTOCMBTE."Cantidad"
			END AS "Cantidad",
			MVTOCMBTE."Precio",
			COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto",
			FormMvto."MontoDecimales",
			FormMvto."MontoTrucaRedondea"),
			MVTOCMBTE."Monto") AS "Monto",
			MVTOCMBTE."IdMoneda",
			CASE 
				WHEN MVTOCMBTE."IdTipoInstrumento" = 'SPT' THEN COALESCE(TOTCOBROS."COMMVTOCMBTE", 0)
				ELSE TOTCOBROS."COMMVTOCMBTE"
			END AS "COMMVTOCMBTE",
			CASE 
				WHEN MVTOCMBTE."IdTipoInstrumento" = 'SPT' THEN COALESCE(TOTCOBROS."DERMVTOCMBTE", 0)
				ELSE TOTCOBROS."DERMVTOCMBTE"
			END AS "DERMVTOCMBTE",
			CASE 
				WHEN MVTOCMBTE."IdTipoInstrumento" = 'SPT' THEN COALESCE(TOTCOBROS."GASMVTO", 0)
				ELSE TOTCOBROS."GASMVTO"
			END AS "GASMVTOCMBTE",
			CASE 
				WHEN MVTOCMBTE."IdTipoInstrumento" = 'SPT' THEN COALESCE(TOTCOBROS."IMPMVTO", 0)
				ELSE TOTCOBROS."IMPMVTO"
			END AS "IMPMVTOCMBTE",
			MVTOCMBTE."FechaMovimientoComprobante",
			CASE WHEN (MVTOCMBTE."IdTipoComprobante"  in ('NCE', 'NCR', 'NEO') AND MVTOCMBTE."IdEstadoFacturacion" IN  ('FAC','ENV') AND MVTOCMBTE."IdComprobanteReferencia">0 )
				THEN COALESCE(MVTOCMBTE."FolioComprobanteReferencia",0) 
				ELSE 0
				END AS "IdComprobanteReferencia",
			MVTOCMBTE."IdInstrumento",
			MVTOCMBTE."IdClasificacionTipoInstrumento",
			CASE 
				WHEN COMP."IdEstadoComprobante" = 'ANU' THEN '1'
				WHEN COMP."IdComprobanteReferencia" <> 0 AND COMP."IdTipoComprobante" NOT IN ('NCR','NCE','NEO') THEN '1'
				ELSE '0'
			END AS "Anulacion",
			CASE WHEN (MVTOCMBTE."IdTipoComprobante"  in ('NCE', 'NCR', 'NEO') AND MVTOCMBTE."IdEstadoFacturacion" IN  ('FAC','ENV') AND MVTOCMBTE."IdComprobanteReferencia">0 )
				THEN CAST(COALESCE(MVTOCMBTE."IdComprobanteReferencia",0) as NVARCHAR(19)) 
				ELSE '0'
			END AS "FolioAnulacion",
			CASE 
				WHEN MVTOCMBTE."IdTipoComprobante"  in ('NCE', 'NCR', 'NEO') THEN '1'
				ELSE '0'
			END AS "NotaCredito",
		CASE
			WHEN (MVTOCMBTE."IdTipoComprobante"  in ('NCE', 'NCR', 'NEO') AND MVTOCMBTE."IdEstadoFacturacion" IN  ('FAC','ENV') AND MVTOCMBTE."IdComprobanteReferencia">0 )
			THEN MVTOCMBTE."IdComprobante"
			ELSE NULL
		END AS "FolioNotaCredito",
			TIPOCOMPROBANTE."Nombre" As "TipoComprobante",
			MVTOCMBTE."FechaLiquidacion" As "FechaLiquidacion",
			CASE 
				WHEN CAST(COMP."FechaEmision" as NVARCHAR(10)) NOT IN ('0001-01-01','') 
				THEN COALESCE( TO_CHAR(UTCTOLOCAL(COMP."FechaEmision", 'America/Santiago', 'platform'), 'YYYY-MM-DD'), TO_CHAR(UTCTOLOCAL(COMP."FechaIngresoRegistro", 'America/Santiago', 'platform'), 'YYYY-MM-DD')) 
				ELSE TO_CHAR(UTCTOLOCAL(COMP."FechaIngresoRegistro", 'America/Santiago', 'platform'), 'YYYY-MM-DD')
			END AS "FechaEmision",			
			CASE
				WHEN CAST(COMP."FechaEmision" as NVARCHAR(19)) NOT IN ('0001-01-01 00:00:00','')
				THEN COALESCE( TO_CHAR(UTCTOLOCAL(COMP."FechaEmision", 'America/Santiago','platform'), 'HH24:MI:SS'), TO_CHAR(UTCTOLOCAL(COMP."FechaIngresoRegistro", 'America/Santiago','platform'), 'HH24:MI:SS'))
				ELSE TO_CHAR(UTCTOLOCAL(COMP."FechaIngresoRegistro", 'America/Santiago','platform'), 'HH24:MI:SS')
			END AS "HoraEmision",
		COMP."FolioComprobante" ,
		COALESCE("Instrumento_Negociacion"."Nemotecnico",'') AS "InstrumentoNegociacion"
		FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 	MVTOCMBTE 
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" 		POR 							ON MVTOCMBTE."IdPortafolio" = POR."IdPortafolio"
		LEFT  JOIN "BSCL.Facturacion.BD::FAC.Comprobante" 		COMP 							ON MVTOCMBTE."IdComprobante" = COMP."IdComprobante"
		LEFT  JOIN "BSCL.Orden.BD::ORD.Transaccion" 			TRANS 							ON 	MVTOCMBTE."IdTransaccion" = TRANS."IdTransaccion"
		LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" 	AS 	"Instrumento_Negociacion" 		ON  ( "Instrumento_Negociacion"."IdInstrumento" = TRANS."IdInstrumentoNegociacion" )
		LEFT  JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosTotalizados" AS TOTCOBROS ON	TOTCOBROS."IdMovimientoComprobante" = MVTOCMBTE."IdMovimientoComprobante"
		LEFT  JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS TIPOCOMPROBANTE      		ON TIPOCOMPROBANTE."IdTipoComprobante" = MVTOCMBTE."IdTipoComprobante"
		LEFT  JOIN "BSCL.Orden.BD::ORD.Asignacion" 			AS 	ASIG 									ON (MVTOCMBTE."IdAsignacion" = ASIG."IdAsignacion" AND "IdEstadoAsignacion" <> 'ANU' )
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" 		AS CLIENTE 		ON CLIENTE."IdCliente" = MVTOCMBTE."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" 	AS 	FormCant 	
			ON 	FormCant."IdMoneda" 			= MVTOCMBTE."IdMonedaSpot"	AND FormCant."IdTipoOperacion" 		= ''
			AND FormCant."IdTipoInstrumento" 	= ''						AND FormCant."IdUsoMonedaFormato" 	= 'GEN'
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" 	AS FormMvto 	
			ON 	FormMvto."IdMoneda" 			= MVTOCMBTE."IdMoneda"		AND FormMvto."IdTipoOperacion" 		= ''
			AND FormMvto."IdTipoInstrumento" 	= ''						AND FormMvto."IdUsoMonedaFormato" 	= 'GEN'		
		WHERE CLIENTE."TipoEnrolamiento" = 'FULL'
		AND COMP."FolioComprobante" is not null 
		) AS c
	INNER JOIN ( SELECT "Valor", "IdSistema" FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" = 'RUT') AS r ON (r."Valor" IS NULL OR r."IdSistema" = c."IdCliente")
	INNER JOIN ( SELECT "Valor", "IdSistema" FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" = 'NEMO') AS n ON (n."Valor" IS NULL OR n."IdSistema" = c."IdInstrumento")
	INNER JOIN ( SELECT "Valor" FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" = 'CLAINS') AS m ON	(m."Valor" IS NULL OR m."Valor" = c."IdClasificacionTipoInstrumento") 
 )AS c

WHERE (TO_DATE(c."FechaMovimientoComprobante") BETWEEN TO_DATE(:v_FechaInicio) AND TO_DATE(:v_FechaTermino))
ORDER BY
	c."FechaMovimientoComprobante",
	c."IdTransaccion" ASC;
END;