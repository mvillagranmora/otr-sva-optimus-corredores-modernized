/****PLDTRS****/
WITH PACTOS AS ( -- Consulta de pactos
SELECT 
CASE WHEN OPE."IdTipoOperacion" = 'COMPAC' 
	THEN CONCAT('COMPRA','              ;') 
	ELSE CONCAT('VENTA','               ;') 
END 														AS "CodigoTransaccion",
'0;' 														AS "MedioPago",
TO_NVARCHAR(OPE."FechaOperacion", 'YYYY-MM-dd') 			AS "FechaTransaccion",
CASE WHEN MC."FechaLiquidacion" IS NULL THEN :Fecha 
	ELSE TO_NVARCHAR(MC."FechaLiquidacion", 'YYYY-MM-dd')
END																		AS "FechaCierre",
OPE."IdOperacion" 											AS "Folio",
DOC."DocumentoIdentidad"									AS "DocumentoIdentidad",
 TO_NVARCHAR(TO_DECIMAL(OPE."MontoOperacionMoneda" * (1 + (OPE."Tasa" * DAYS_BETWEEN(OPE."FechaOperacion", :Fecha) )/36000), 4, 4)) 	AS "MontoOriginal",
CASE WHEN OPE."IdMonedaPrincipal" = 'CLP' THEN '999;' 
     WHEN OPE."IdMonedaPrincipal" = 'USD' THEN '013;'
     WHEN OPE."IdMonedaPrincipal" = 'UF' THEN '998;'
     ELSE '000;'
END 														AS "CodigoMoneda",
CASE WHEN OPE."IdTipoOperacion" = 'COMPAC' 
	THEN CONCAT('CRCD','      ;') 
	ELSE CONCAT('ARVD','      ;') 
END 														AS "CodigoSubproducto",
CAR."FolioComprobante" ,
CAR."TipoCarteraCorredor",
OPE."IdOperacion",
OPE."IdTipoProducto" ,
OPE."IdTipoOperacion"  , OPE."Tasa" , OPE."MontoOperacionMoneda"  , OPBAN.*
FROM "BSCL.Mesa.BD::MES.Operacion" OPE 
INNER JOIN "BSCL.Mesa.BD::MES.Mesa" MES           		  ON OPE."IdMesaOrigen" = MES."IdMesa"
INNER JOIN "BSCL.Custodia.BD::CUS.Reserva"        AS RES  ON RES."IdOperacion" = OPE."IdOperacion"
INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente"     AS SUB  ON SUB."FolioSubyacente" = RES."FolioSubyacente" 
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera"         AS CAR  ON RES."IdCarteraOrigen" = CAR."IdCartera" -- detalle de compra
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI 		  ON CLI."IdCliente" = OPE."IdCliente" 
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER 		  ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdPersona" = DOC."IdPersona" 
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarMesas" mesOri 	  ON (OPE."IdMesaOrigen" = mesOri."IdMesa")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SN ON MES."IdCliente" = SN."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" CE 	ON SN."IdPersona" = CE."IdEmpresa" AND CE."IdEmpresa" = 1
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacionBanco" OPBAN ON OPBAN."IdOperacion"  = OPE."IdOperacion" AND OPBAN."RecibePaga" = 'P'
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MC ON OPE."FolioOperacion" = MC."FolioComprobante"
WHERE  
OPE."IdEstado" <> 'ANU'
AND  OPE."IdTipoOperacion" IN ('VENPAC','COMPAC')
AND (OPE."FechaLiquidacion" >= TO_TIMESTAMP(:Fecha, 'YYYY-MM-DD')   AND OPE."FechaLiquidacion" <  ADD_DAYS(TO_TIMESTAMP(:Fecha, 'YYYY-MM-DD'), 1))
AND (OPE."FechaVencimiento" >= TO_TIMESTAMP(:Fecha, 'YYYY-MM-DD')   AND OPE."FechaVencimiento" <  ADD_DAYS(TO_TIMESTAMP(:Fecha, 'YYYY-MM-DD'), 1))
AND OPE."IdMonedaPrincipal" = 'USD'
)

(SELECT  
CASE 
  WHEN AccionOperacion."Accion" = 'COMPRA' THEN 'COMPRA              ;'
  WHEN AccionOperacion."Accion" = 'VENTA'  THEN 'VENTA               ;'
ELSE '        ;' 		END AS "CodigoTransaccion",
CASE FOP."IdMedioDePago"
			WHEN 'CHE' THEN '3;'
			WHEN 'EFE' THEN '1;'
			ELSE '0;'
END AS "MedioPago",
TO_NVARCHAR(COM."FechaMovimientoComprobante", 'YYYY-MM-dd') AS "FechaTransaccion",
COM."IdComprobante" AS "Folio",
COM."DocumentoIdentidad",
CASE 
	WHEN (COM."IdClasificacionTipoInstrumento" = 'IF' AND COM."IdMoneda" = 'USD') THEN TO_NVARCHAR(TO_DECIMAL(COM."MontoMovimientoComprobante", 4, 4))
	WHEN COM."NemotecnicoInstrumento"   = 'PDBC' THEN TO_NVARCHAR(TO_DECIMAL(COM."Monto", 4, 4))
	WHEN (COM."IdTipoInstrumento" = 'ACN') AND (COM."Monto" < 1) AND COM."IdCondicionLiquidacion" = 'TP' THEN TO_NVARCHAR(TO_DECIMAL(COM."MontoAPlazo", 4, 4))
	ELSE TO_NVARCHAR(TO_DECIMAL(COM."Monto", 4, 4))
END AS "MontoOriginal",
'999;' AS "CodigoMoneda",
CASE 
	WHEN (COM."IdClasificacionTipoInstrumento" = 'IF') AND (COM."IdTipoComprobante" = 'FAC' OR COM."IdTipoComprobante" = 'FOC' OR COM."IdTipoComprobante" = 'FEE' OR COM."IdTipoComprobante" = 'FAE' OR COM."IdTipoComprobante" = 'FOE')
			THEN 'IIF       ;'
	WHEN (COM."IdClasificacionTipoInstrumento" = 'RF') AND (COM."IdTipoComprobante" = 'FAC' OR COM."IdTipoComprobante" = 'FOC' OR COM."IdTipoComprobante" = 'FEE' OR COM."IdTipoComprobante" = 'FAE' OR COM."IdTipoComprobante" = 'FOE')
			THEN 'RF        ;'
	WHEN ((COM."IdTipoComprobante" = 'FAC' OR COM."IdTipoComprobante" = 'FOC' OR COM."IdTipoComprobante" = 'FEE' OR COM."IdTipoComprobante" = 'FAE' OR COM."IdTipoComprobante" = 'FOE') AND ((COM."IdTipoInstrumento" = 'ETN') OR (COM."IdTipoInstrumento" = 'ETX'))) 
			THEN 'ETF       ;'
	WHEN ((COM."IdTipoComprobante" = 'FAC' OR COM."IdTipoComprobante" = 'FOC' OR COM."IdTipoComprobante" = 'FEE' OR COM."IdTipoComprobante" = 'FAE' OR COM."IdTipoComprobante" = 'FOE') AND ((COM."IdTipoInstrumento" <> 'ETN') AND (COM."IdTipoInstrumento" <> 'ETX') AND (COM."IdTipoInstrumento" = 'ACN'))) 
			THEN 'ACC       ;'
	WHEN COM."IdTipoInstrumento" = 'SIM' THEN 'ACC       ;' 
	ELSE    CONCAT(COM."IdTipoInstrumento",'       ;')  END AS "CodogoSubProducto"

FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" COM
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AccionOperacion ON COM."IdTipoOperacion"  = AccionOperacion."IdTipoOperacion" 
LEFT JOIN (
  SELECT TOP 1 *
  FROM "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
) FOP ON COM."IdComprobante" = FOP."IdComprobante"
WHERE  COM."FechaMovimientoComprobante" = :Fecha 
AND    COM."IdTipoComprobante" IN ('FAC', 'FOC', 'FEE', 'FAE', 'FOE', 'NEO', 'NCO') 
AND    (COM."IdTipoInstrumento" IN ('ACN') OR COM."IdClasificacionTipoInstrumento" IN ('RF', 'IF', 'RV')) 
AND    COM."IdEstadoFacturacion" <> 'ANU'
AND    COM."IdComprobante" NOT IN (
    SELECT "IdComprobanteReferencia" 
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" 
    WHERE "IdComprobanteReferencia" <> 0
)
AND (
    COM."IdTipoInstrumento" != 'SIM'
    OR COM."IdCondicionLiquidacion" = 'T0'
)
AND NOT  (COM."IdClasificacionTipoInstrumento" = 'RV' AND COM."IdTipoComprobante" = 'FEE' )
UNION ALL

SELECT 
CASE		
	WHEN (FFMM."TipoSolicitud" = 'APO') THEN 'COMPRA              ;'
	ELSE 'VENTA               ;'END 									AS "CodigoTransaccion",
CASE FFMM."IdMedioDePago"
	WHEN 'CHE' THEN '3;'
	WHEN 'EFE' THEN '1;'
ELSE '0;'		END 													AS "MedioPago",
TO_NVARCHAR(FFMM."FechaAceptacion", 'YYYY-MM-dd') 						AS "FechaTransaccion",
FFMM."IdSolicitudAporteRescate" 										AS "Folio",
CLI."DocumentoIdentidad",
TO_NVARCHAR(TO_DECIMAL(FFMM."Monto", 4, 4))	 							AS "MontoOriginal",
CASE 
	WHEN (FFMM."TipoSolicitud" = 'APO' OR FFMM."TipoSolicitud" = 'RES') AND (FFMM."IdMoneda" = 'USD') THEN  '013;'  
	ELSE '999;'END 														AS "CodigoMoneda",
'FFMM      ;' 															AS "CodigoSubProducto"
FROM 		"BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 	FFMM 
LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI 	ON FFMM."IdCliente" = CLI."IdCliente"
WHERE 	FFMM."FechaAceptacion" 	= :Fecha 
AND 	FFMM."Estado" 			= 'CRT' 
AND		(FFMM."TipoSolicitud" 	= 'APO' OR FFMM."TipoSolicitud" = 'RES') 
AND 	(FFMM."IdMoneda" 		= 'USD' OR FFMM."IdMoneda" 		= 'CLP') 

UNION ALL

SELECT  
CASE SPO."IdTipoOperacion"
	WHEN 'COMSPT' THEN 'COMPRA              ;'
	WHEN 'VENSPT' THEN 'VENTA               ;'
END 													AS "CodigoTransaccion",
'1;' 													AS "MedioPago",
TO_NVARCHAR(SPO."FechaOperacion", 'YYYY-MM-dd') 		AS "FechaTransaccion", 
SPO."IdOperacionSpot" 									AS "Folio",
MOV."RutCliente" ,
CASE 
	WHEN SPO."IdMonedaPrincipal" = 'CLP' THEN TO_NVARCHAR(TO_DECIMAL(SPO."MontoMonedaPrincipal", 4, 4)) 
	ELSE TO_NVARCHAR(TO_DECIMAL(SPO."MontoMonedaSecundaria", 4, 4)) END AS "MontoOriginal",
'013;' 													AS "CodigoMoneda",
'CAM       ;'											AS "CodigoSubProducto"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPO
LEFT JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" MOV ON SPO."IdOperacionSpot"  = MOV."IdOperacion" 
WHERE     SPO."FechaOperacion" BETWEEN 
    ADD_SECONDS(TO_TIMESTAMP(CONCAT(:Fecha, ' 00:00:00'), 'YYYY-MM-DD HH24:MI:SS'), -1 * (SELECT "Valor" FROM  "BSCL.Plataforma.BD::PLA.Configuracion" AS conf WHERE ( conf."IdEmpresa" = :IdEmpresa AND conf."CodigoParametro" = 'PLA.SegundosZonaHoraria'))) AND 
    ADD_SECONDS(TO_TIMESTAMP(CONCAT(:Fecha, ' 23:59:59'), 'YYYY-MM-DD HH24:MI:SS'), -1 * (SELECT "Valor" FROM  "BSCL.Plataforma.BD::PLA.Configuracion" AS conf WHERE ( conf."IdEmpresa" = :IdEmpresa AND conf."CodigoParametro" = 'PLA.SegundosZonaHoraria')))
AND SPO."Estado" = 'VIG' 
AND (SPO."IdTipoOperacion" = 'COMSPT' OR SPO."IdTipoOperacion" = 'VENSPT') 
)

UNION ALL

SELECT 
	CASE 
		WHEN (CAM."Monto" > 0) AND CAM."IdTipoMovimiento" = 'MOVCAR' THEN CONCAT('COMPRA','              ;')
		WHEN (CAM."Monto" > 0) AND CAM."IdTipoMovimiento" = 'MOVABN' THEN CONCAT('VENTA','               ;')
    END 												AS "CodigoTransaccion",
	'0;' 												AS "MedioPago",
	TO_NVARCHAR(CAM."FechaCertificacion", 'YYYY-MM-dd') AS "FechaTransaccion",	 
	CAM."IdCargoAbonoMovimiento" 						AS "Folio", 
	DOC."DocumentoIdentidad",
	TO_NVARCHAR(TO_DECIMAL(CAM."Monto", 4, 4)) 			AS "MontoOriginal",
	CASE WHEN CI."IdMoneda" = 'CLP' THEN '999;' 
		 WHEN CI."IdMoneda" = 'USD' THEN '013;'
		 ELSE '000;'
	END 												AS "CodigoMoneda",
	CASE
		WHEN CAM."IdTipoMovimiento" = 'MOVABN' THEN CONCAT(NOC."Valor",'      ;')
		WHEN CAM."IdTipoMovimiento" = 'MOVCAR' THEN CONCAT(NOC."Valor",'      ;')
	END  												AS "CodigoSubProducto"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" 			AS CAM
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" 			AS CI 	ON CAM."IdCuentaInversion" 	= CI."IdCuentaInversion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" 						AS CLI 	ON CLI."IdCliente" 			= CI."IdCliente" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 			AS DOC 	ON DOC."IdPersona" 			= CLI."IdPersona"
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" 	AS CCI 	ON CAM."IdConcepto" 		= CCI."IdConcepto"
LEFT JOIN "BSCL.Normativa.BD::NOR.Configuracion" 					AS NOC 	ON NOC."CodigoParametro" 	= CAM."IdTipoMovimiento" 
WHERE CAM."FechaCertificacion" 	LIKE CONCAT(:Fecha,'%')
AND (CAM."IdTipoMovimiento" 	= 'MOVABN' OR  CAM."IdTipoMovimiento" = 'MOVCAR') 
AND CAM."IdEstado" 				= 'CRT'
AND DOC."IdEstado" 				= 'ACT'
AND CCI."MarcaInterfazLavadoDeDinero" = 'S'
AND DOC."Predeterminado" = 'S'

UNION ALL 
 
SELECT
  CASE 
	WHEN OP."IdTipoOperacion"  = 'COMPAC' 	THEN CONCAT('COMPRA','              ;')
	WHEN OP."IdTipoOperacion"  = 'VENPAC'  	THEN CONCAT('VENTA','               ;')
	ELSE OP."IdTipoOperacion"
  END 														AS "CodigoTransaccion"
,'1;' 														AS "MedioPago"
,TO_NVARCHAR(OP."FechaOperacion", 'YYYY-MM-dd') 			AS "FechaTransaccion"
,OP."IdOperacion"											AS "Folio" 
,CLI."DocumentoIdentidad" 									AS "DocumentoIdentidad"
, CASE WHEN res."Nemotecnico"  = 'PDBC' THEN TO_NVARCHAR(TO_DECIMAL(res."MontoValorizado", 4, 4))
else TO_NVARCHAR(TO_DECIMAL(res."MontoValorizado", 4, 4)) END AS "MontoOriginal"	
, CASE WHEN OP."IdMoneda" 	= 'CLP' THEN '999;' 
	WHEN 	OP."IdMoneda" 	= 'USD' THEN '013;'
	ELSE '000;'
  END 														AS "CodigoMoneda"
, CASE WHEN res."IdTipoReajuste" 	= 'CLP' 		THEN 'VPP       ;'
	WHEN res."IdTipoReajuste" 		= 'USD'			THEN 'VPD       ;'
	WHEN res."IdTipoReajuste" 		= 'UF'			THEN 'VPU       ;'
	ELSE 'VPP       ;'	END 								AS "CodigoSubProducto" 	 
FROM "BSCL.Mesa.BD::MES.Operacion" OP 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS 	CLI 	ON OP."IdCliente" 	= CLI."IdCliente"
LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" 				AS 	PARAM 	ON CLI."IdEmpresa" 		= PARAM."IdEmpresa" AND PARAM."CodigoParametro" = 'CONFIRMA_OPERACIONES_MESARF'						
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" 				AS 	T_Ope 	ON OP."IdTipoProducto" 	= T_Ope."IdTipoProducto" AND OP."IdTipoOperacion" = T_Ope."IdTipoOperacion"
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesReserva" res
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 						AS carr ON (res."IdCarteraOrigen" = carr."IdCartera") ON (OP."IdOperacion" = res."IdOperacion")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 				AS INS 	ON INS."IdInstrumento" = OP."IdInstrumento"
WHERE INS."IdTipoInstrumento" = 'PAC'
AND OP."FechaOperacion" LIKE CONCAT(:Fecha,'%')
AND OP."IdEstado" <> 'ANU'
AND NOT (res."IdClasificacionTipoInstrumento" = 'RV' AND carr."IdTipoComprobante" = 'FEE')

UNION ALL 

--SELECT 
--CASE 
--	WHEN G."IdEstado"  = 	'VIG' 									THEN CONCAT('COMPRA','              ;')
--	WHEN G."IdEstado" <> 'VIG' OR G."EGRIdTipoOperacion" = 'RETGTA'	THEN CONCAT('VENTA','               ;')
--  END 																AS "CodigoTransaccion"
--  ,'0;' 															AS "MedioPago"
--,TO_NVARCHAR(G."FechaOperacion", 'YYYY-MM-dd') 						AS "FechaTransaccion"
--,G."FolioCarteraOrigen"												AS "Folio" 
--,CLI."DocumentoIdentidad" 											AS "DocumentoIdentidad"
--,TO_NVARCHAR(TO_DECIMAL(G."INGMontoATasaGarantiaActual" , 4, 4)) 	AS "MontoOriginal"
--, CASE WHEN G."IdMonedaInstrumento"  	= 'CLP' THEN '999;' 
--	WHEN 	G."IdMonedaInstrumento" 	= 'USD' THEN '013;'
--	ELSE '000;'
--  END 																AS "CodigoMoneda"
--, CASE WHEN G."IdEstado"  = 	'VIG'  	THEN 'IGRV      ;'
--	WHEN G."IdEstado"  	<> 'VIG'   		THEN 'RGRV      ;'
--	END 															AS "CodigoSubProducto" 	 
--FROM  "BSCL.Custodia.BD::GTA.Garantia" G
--LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON G."IdCliente" 	= CLI."IdCliente"
--WHERE G."FechaComprobante" 				LIKE CONCAT(:Fecha,'%')
--AND G."IdConcepto" 						IN 	 ('GARSIM')  
--AND G."IdClasificacionTipoInstrumento"  = 	'RV'
--AND G."IdTipoComprobante"  				= 	'CUS'


						-- garantias* 
SELECT  CASE 
	WHEN SIM."EGRFechaOperacion" LIKE 	CONCAT(:Fecha,'%') THEN CONCAT('VENTA','               ;') -- 8 caracteres + ;
	ELSE CONCAT('COMPRA','              ;')
  END 																AS "CodigoTransaccion"
,'0;' 																AS "MedioPago"
,CASE WHEN SIM."EGRFechaOperacion" LIKE  CONCAT(:Fecha,'%') THEN TO_NVARCHAR( SIM."EGRFechaOperacion", 'YYYY-MM-dd')				
 ELSE TO_NVARCHAR( SIM."FechaComprobante", 'YYYY-MM-dd') 
 END 																AS "FechaTransaccion"
,SIM."FolioComprobante" 											AS "Folio" 
, SIM."DocumentoIdentidad" 											AS "DocumentoIdentidad"
,TO_NVARCHAR(round (TO_DECIMAL(SIM."MontoLiquidacion" , 4, 4),0)) 	AS "MontoOriginal"
, CASE WHEN SIM."IdMonedaInstrumento"  	= 'CLP' THEN '999;' 
	WHEN 	SIM."IdMonedaInstrumento"	= 'USD' THEN '013;'
	ELSE '000;'
  END 																AS "CodigoMoneda"
,CASE WHEN 	SIM."EGRFechaOperacion" LIKE 	CONCAT(:Fecha,'%')  THEN 'RGRV      ;'
ELSE 'IGRV      ;'
END 															AS "CodigoSubProducto" 	  -- 10 caracteres totales
FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" SIM
WHERE  	(SIM."EGRFechaOperacion" 	LIKE 	CONCAT(:Fecha,'%') OR 	SIM."FechaComprobante"  	LIKE 	CONCAT(:Fecha,'%'))
AND  	SIM."IdEstado" 				NOT IN 	('ANU')

UNION ALL 
					-- Simultaneas
SELECT  CASE 
	WHEN "AccionOperacion" = 'COMPRA' 	THEN CONCAT('VENTA','               ;') -- 8 caracteres + ;
	WHEN "AccionOperacion" = 'VENTA' 	THEN CONCAT('COMPRA','              ;')
  END 																AS "CodigoTransaccion"
,'0;' 																AS "MedioPago"
,TO_NVARCHAR( SIM."EGRFechaOperacion", 'YYYY-MM-dd')				AS "FechaTransaccion"
,SIM."FolioComprobante" 											AS "Folio" 
, SIM."DocumentoIdentidad" 											AS "DocumentoIdentidad"
,TO_NVARCHAR(round (TO_DECIMAL(SIM."MontoLiquidacion" , 4, 4),0)) 	AS "MontoOriginal"
, CASE WHEN SIM."IdMonedaInstrumento"  	= 'CLP' THEN '999;' 
	WHEN 	SIM."IdMonedaInstrumento"	= 'USD' THEN '013;'
	ELSE '000;'
  END 																AS "CodigoMoneda"
,'CS        ;'														AS "CodigoSubProducto" 	  -- 10 caracteres totales
FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" SIM
WHERE  SIM."EGRFechaOperacion"  LIKE CONCAT(:Fecha,'%')
AND  SIM."IdEstado" NOT IN ('VIG','ANU')

UNION ALL 

SELECT "CodigoTransaccion","MedioPago" ,"FechaTransaccion" ,"Folio", "DocumentoIdentidad","MontoOriginal","CodigoMoneda","CodigoSubproducto"  FROM PACTOS


UNION ALL 
-- COMPRAS Y VENTAS A TERMINO 
	SELECT 
		CASE 
			WHEN OP."IdTipoOperacion"  = 'COMTER' 	THEN CONCAT('COMPRA','              ;')
			WHEN OP."IdTipoOperacion"  = 'VENTER'  	THEN CONCAT('VENTA','               ;')
			ELSE OP."IdTipoOperacion"
		  END 														AS "CodigoTransaccion"
		,'1;' 														AS "MedioPago"
		,TO_NVARCHAR(OP."FechaOperacion", 'YYYY-MM-dd') 			AS "FechaTransaccion"
		,OP."IdOperacion"											AS "Folio" 
		,CLI."DocumentoIdentidad" 									AS "DocumentoIdentidad"
		, CASE WHEN res."Nemotecnico"  = 'PDBC' THEN TO_NVARCHAR(TO_DECIMAL(res."MontoValorizado"/(res."IngValorRescate"/5000000), 4, 4))
		else TO_NVARCHAR(TO_DECIMAL(res."MontoValorizado", 4, 4)) END AS "MontoOriginal"	
		, CASE WHEN OP."IdMoneda" 	= 'CLP' THEN '999;' 
			WHEN 	OP."IdMoneda" 	= 'USD' THEN '013;'
			WHEN 	OP."IdMoneda" 	= 'UF' THEN '998;'
			ELSE '000;'
		  END 														AS "CodigoMoneda"
		, CASE WHEN res."IdTipoReajuste" 	= 'CLP' 		THEN 'VPP       ;'
			WHEN res."IdTipoReajuste" 		= 'USD'			THEN 'VPD       ;'
			WHEN res."IdTipoReajuste" 		= 'UF'			THEN 'VPU       ;'
			ELSE 'VPP       ;'	END 								AS "CodigoSubProducto" 	 
	FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OP
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS CLI 		ON OP."IdCliente" 	= CLI."IdCliente"
	LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" 				AS 	PARAM 	ON CLI."IdEmpresa" 		= PARAM."IdEmpresa" AND PARAM."CodigoParametro" = 'CONFIRMA_OPERACIONES_MESARF'						
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" 				AS 	T_Ope 	ON OP."IdTipoProducto" 	= T_Ope."IdTipoProducto" AND OP."IdTipoOperacion" = T_Ope."IdTipoOperacion"
	LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesReserva" res 	ON (OP."IdOperacion" = res."IdOperacion")
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 						AS carr 	ON (res."IdCarteraOrigen" = carr."IdCartera")  
	WHERE OP."IdTipoOperacion"  IN ('VENTER','COMTER')
	AND OP."FechaOperacion" LIKE CONCAT(:Fecha,'%')
	AND OP."IdEstado"  = 'VIG'
	AND NOT ( carr."IdTipoComprobante" = 'FEE' AND carr."IdClasificacionTipoInstrumento"  = 'RV')
		
UNION ALL
	
	SELECT
	CASE 
		WHEN ccb."AccionTipoOperacion"  = 'COMPRA' 	THEN CONCAT('COMPRA','              ;')
		WHEN ccb."AccionTipoOperacion"  = 'VENTA'	THEN CONCAT('VENTA','               ;')
		ELSE ccb."AccionTipoOperacion"
	END 						AS "CodigoTransaccion",
	'0;'						AS "MedioPago",
	TO_NVARCHAR(ccb."FechaLiquidacion", 'YYYY-MM-dd') AS "FechaTransaccion",
	ccb."FolioComprobante"		AS "Folio",
	ccb."DocumentoIdentidad"	AS "DocumentoIdentidad",
	TO_NVARCHAR(round (TO_DECIMAL(ccb."MontoLiquidacion" , 4, 4),0))  AS "MontoOriginal",
	CASE WHEN ccb."IdMonedaLiquidacion"	 = 'CLP'   THEN '999;'
		 WHEN ccb."IdMonedaLiquidacion"	 = 'USD'   THEN '013;'
		 WHEN ccb."IdMonedaLiquidacion"	 = 'UF'	   THEN '998;'
		 ELSE '000;'
	END 						AS "CodigoMoneda",
	CASE WHEN ccb."IdConcepto" = 'ABCAAC' AND ccb."AccionTipoOperacion" = 'VENTA' THEN 'ABNA      ;'
		 WHEN ccb."IdConcepto" = 'ABCAAC' AND ccb."AccionTipoOperacion" = 'COMPRA' THEN 'CANA      ;'
		 WHEN ccb."IdConcepto" = 'ABCAIN' AND ccb."AccionTipoOperacion" = 'VENTA' THEN 'ABNA      ;'
		 WHEN ccb."IdConcepto" = 'ABCAIN' AND ccb."AccionTipoOperacion" = 'COMPRA' THEN 'CANA      ;'
		 WHEN ccb."IdConcepto" = 'ABCA' AND ccb."AccionTipoOperacion" = 'VENTA' THEN 'ABNA      ;'
		 WHEN ccb."IdConcepto" = 'ABCA' AND ccb."AccionTipoOperacion" = 'COMPRA' THEN 'CANA      ;'
		 WHEN ccb."IdConcepto" = 'ABCAED' AND ccb."AccionTipoOperacion" = 'VENTA' THEN 'ABNA      ;'
		 WHEN ccb."IdConcepto" = 'ABCAED' AND ccb."AccionTipoOperacion" = 'COMPRA' THEN 'CANA      ;'
		 WHEN ccb."IdConcepto" = 'ABCADC' AND ccb."AccionTipoOperacion" = 'VENTA' THEN 'ABNA      ;'
		 WHEN ccb."IdConcepto" = 'ABCADC' AND ccb."AccionTipoOperacion" = 'COMPRA' THEN 'CANA      ;'
		 WHEN ccb."IdConcepto" = 'ABOW' AND ccb."AccionTipoOperacion" = 'VENTA' THEN 'ABNA      ;'
		 WHEN ccb."IdConcepto" = 'ABOW' AND ccb."AccionTipoOperacion" = 'COMPRA' THEN 'CANA      ;'
		 WHEN ccb."IdConcepto" = 'ABOWP' AND ccb."AccionTipoOperacion" = 'VENTA' THEN 'ABNA      ;'
		 WHEN ccb."IdConcepto" = 'ABOWP' AND ccb."AccionTipoOperacion" = 'COMPRA' THEN 'CANA      ;'
		 ELSE '          ;'
	END 						AS "CodigoSubProducto"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" AS ccb
	WHERE ccb."IdConcepto" IN('ABCAAC','ABCAIN','ABCA','ABCAED','ABCADC','ABOW','ABOWP')
	AND "AccionTipoOperacion" IN('COMPRA','VENTA')
	AND ccb."NombreEstado" <> 'ANULADO'
	AND ccb."FechaLiquidacion" = :Fecha
	