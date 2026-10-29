set schema "BSCL";

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1891.Origen";

create view "BSCL.Normativa.BD.Vistas::TRI.DJ1891.Origen" (
    "IdEmpresa",
    "TipoDocumento",
    "NumeroDocumento",
    "FechaDocumento",
    "RutEmisor",
    "Nemotecnico",
    "RutCliente",
    "Cuenta",
    "Cantidad",
	"MontoCompras",
	"MontoVentas",
	"ValorAdquisicion",
    "TipoTransaccion",
    "IdTipoOperacion",
    "IdTipoProducto",
    "IdCliente",
    "IdPortafolio",
    "IdBaseDocumento",
    "LineaFactura"
) AS

-- ***********************************	FACTURAS 			 *************************************************************************************************

	SELECT 
	master."IdEmpresa"													AS "IdEmpresa",
    CASE
        WHEN master."IdTipoComprobante" IN ('NCE', 'NCR','NEE','NEO') THEN 'C'
        WHEN master."IdTipoComprobante" IN ('NDE', 'NDR') THEN 'D'
        WHEN master."IdTipoComprobante" IN ('FAC', 'FAE','FEE','FOE','FOX') THEN 'F'
        ELSE 'O'
    END 															AS "TipoDocumento",
    master."FolioComprobante"										AS "NumeroDocumento",
	TO_DATE(master."FechaComprobante") 								AS "FechaDocumento",    
	
	CASE 
		WHEN COALESCE(ins."IdTipoInstrumento",'') IN ('ACN','CFI') AND master."IdTipoComprobante" NOT IN ('FOE','NEO')
			THEN COALESCE(ins."DocumentoIdentidadEmisor",' ') 
		
		WHEN COALESCE(ins."IdTipoInstrumento",'') = 'SIM'
    		THEN COALESCE(ins_sim."DocumentoIdentidadEmisor",' ')
		ELSE '' 
	END  															AS "RutEmisor",
	--IFNULL(emi."DocumentoIdentidad",'') 							AS "RutEmisor"
	CASE WHEN COALESCE(ins."IdTipoInstrumento",'') IN ('ACN','CFI','SIM') AND master."IdTipoComprobante" NOT IN ('FOE','NEO') THEN detalle."NemotecnicoInstrumento" ELSE '' END AS "Nemo",
	LPAD(cli."DocumentoIdentidad",11,' ') 							AS "RutCliente",
	master."CodigoPortafolio"  										AS "Cuenta",
	CASE 
		WHEN COALESCE(ins."IdTipoInstrumento",'') IN ('ACN','CFI','SIM') AND master."IdTipoComprobante" NOT IN ('FOE','NEO')
			THEN detalle."Cantidad" 
			ELSE 0 
	END 															AS "Cantidad",
	CASE
		WHEN master."AccionTipoOperacion" = 'VENTA' AND COALESCE(ins."IdTipoInstrumento",'') = 'SPT' THEN
		  (CASE WHEN detalle."NemotecnicoInstrumento" IN ('DOLAR','EURO','ARBINT','DISTRI','INTERB') THEN detalle."MontoLiquidacion"
		        WHEN detalle."IdTipoProducto" IN ('INAX','INCX','INEX','INCI') THEN detalle."MontoComprobanteDetalle"
		  ELSE detalle."Monto" END )
		WHEN master."AccionTipoOperacion" = 'COMPRA' AND COALESCE(ins."IdTipoInstrumento",'') <> 'SPT' THEN
		  (CASE WHEN detalle."NemotecnicoInstrumento" IN ('DOLAR','EURO','ARBINT','DISTRI','INTERB') THEN detalle."MontoLiquidacion"
		        WHEN detalle."IdTipoProducto" IN ('INAX','INCX','INEX','INCI') THEN detalle."MontoComprobanteDetalle"
		  ELSE detalle."Monto" END )
	    ELSE 0
	END 															AS "MontoCompras",
	CASE		
	    WHEN master."AccionTipoOperacion" = 'COMPRA' AND COALESCE(ins."IdTipoInstrumento",'') = 'SPT' THEN
		  (CASE WHEN detalle."NemotecnicoInstrumento" IN ('DOLAR','EURO','ARBINT','DISTRI','INTERB') THEN detalle."MontoLiquidacion"		  
		        WHEN detalle."IdTipoProducto" IN ('INAX','INCX','INEX','INCI') THEN detalle."MontoComprobanteDetalle"
		  ELSE detalle."Monto" END )
	    WHEN master."AccionTipoOperacion" = 'VENTA' AND COALESCE(ins."IdTipoInstrumento",'') <> 'SPT' THEN	    
		  (CASE WHEN detalle."NemotecnicoInstrumento" IN ('DOLAR','EURO','ARBINT','DISTRI','INTERB') THEN detalle."MontoLiquidacion"
		        WHEN detalle."IdTipoProducto" IN ('INAX','INCX','INEX','INCI') THEN detalle."MontoComprobanteDetalle"
		  ELSE detalle."Monto" END )
	    ELSE 0 
	END 															AS "MontoVentas",	
	CASE
		WHEN master."AccionTipoOperacion" = 'VENTA' AND COALESCE(ins."IdTipoInstrumento",'') = 'ACN' AND master."IdTipoComprobante" NOT IN ('FOE','NEO') THEN
			CASE
				WHEN (SELECT (SUM((movVenta."PrecioCompra"*movVenta."EGRCantidad")*(IPCVNT."Valor"/CASE WHEN IPCCOM."Valor"=0 OR IPCCOM."Valor" IS NULL THEN 1 ELSE  IPCCOM."Valor" END))) FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS movCartera 
									RIGHT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS movVenta
									ON movVenta."EGRFolioComprobante" = master."IdComprobante"
									AND movVenta."EGRIdMovimientoCarteraEnTransito" = movCartera."IdMovimientoCarteraEnTransito"
									LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCCOM  ON
											((CASE
												WHEN movVenta."FechaLiquidacion" <> '0001-01-01' THEN
													substring(ADD_MONTHS (TO_DATE (movVenta."FechaLiquidacion", 'YYYY-MM-DD'), -1),0,7)
												ELSE
													substring(ADD_MONTHS (TO_DATE (movVenta."FechaComprobante", 'YYYY-MM-DD'), -1),0,7)
											 END) = IPCCOM."AgnoMes")  AND IPCCOM."IdParametroFinanciero" = 'IPC'
									LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCVNT  ON 
											(substring(ADD_MONTHS (TO_SECONDDATE (master."FechaComprobante", 'YYYY-MM-DD HH24:MI:SS'), -1),0,7) = IPCVNT."AgnoMes")  AND IPCVNT."IdParametroFinanciero" = 'IPC'
									WHERE movVenta."EGRIdMovimientoCarteraEnTransito" = movCartera."IdMovimientoCarteraEnTransito"
										AND movCartera."FolioComprobante" = master."IdComprobante"
										AND movCartera."IdTipoInstrumento" = 'ACN'
										AND movCartera."IdCliente" = master."IdCliente"
										AND movCartera."Nemotecnico" = detalle."NemotecnicoInstrumento"
										AND (movCartera."FolioExternoReferencia" = detalle."FolioTransaccion" OR movCartera."PrecioCompra" = detalle."Precio")
								) IS NULL THEN
									CASE
										WHEN (SELECT (SUM((movVenta."PrecioCompra"*movVenta."EGRCantidad")*(IPCVNT."Valor"/CASE WHEN IPCCOM."Valor"=0 OR IPCCOM."Valor" IS NULL THEN 1 ELSE  IPCCOM."Valor" END))) FROM "BSCL.Custodia.BD::CUS.Cartera" AS movVenta
													LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCCOM  ON
														((CASE
															WHEN movVenta."FechaLiquidacion" <> '0001-01-01' THEN
																substring(ADD_MONTHS (TO_DATE (movVenta."FechaLiquidacion", 'YYYY-MM-DD'), -1),0,7)
															ELSE
																substring(ADD_MONTHS (TO_DATE (movVenta."FechaComprobante", 'YYYY-MM-DD'), -1),0,7)
											 			END) = IPCCOM."AgnoMes")  AND IPCCOM."IdParametroFinanciero" = 'IPC'
													LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCVNT  ON 
															(substring(ADD_MONTHS (TO_SECONDDATE (master."FechaComprobante", 'YYYY-MM-DD HH24:MI:SS'), -1),0,7) = IPCVNT."AgnoMes")  AND IPCVNT."IdParametroFinanciero" = 'IPC'
													WHERE movVenta."EGRFolioComprobante" = master."IdComprobante"
														AND movVenta."IdTipoInstrumento" = 'ACN'
														AND movVenta."IdCliente" = master."IdCliente"
														AND movVenta."Nemotecnico" = detalle."NemotecnicoInstrumento"
														AND movVenta."EGRPrecio" = detalle."Precio"
														) IS NOT NULL THEN
														(SELECT (SUM((movVenta."PrecioCompra"*movVenta."EGRCantidad")*(IPCVNT."Valor"/CASE WHEN IPCCOM."Valor"=0 OR IPCCOM."Valor" IS NULL THEN 1 ELSE  IPCCOM."Valor" END))) FROM "BSCL.Custodia.BD::CUS.Cartera" AS movVenta
															LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCCOM  ON
																((CASE
																	WHEN movVenta."FechaLiquidacion" <> '0001-01-01' THEN
																		substring(ADD_MONTHS (TO_DATE (movVenta."FechaLiquidacion", 'YYYY-MM-DD'), -1),0,7)
																	ELSE
																		substring(ADD_MONTHS (TO_DATE (movVenta."FechaComprobante", 'YYYY-MM-DD'), -1),0,7)
																 END) = IPCCOM."AgnoMes")  AND IPCCOM."IdParametroFinanciero" = 'IPC'
															LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCVNT  ON 
																	(substring(ADD_MONTHS (TO_SECONDDATE (master."FechaComprobante", 'YYYY-MM-DD HH24:MI:SS'), -1),0,7) = IPCVNT."AgnoMes")  AND IPCVNT."IdParametroFinanciero" = 'IPC'
											 				WHERE movVenta."EGRFolioComprobante" = master."IdComprobante"
																AND movVenta."IdTipoInstrumento" = 'ACN'
																AND movVenta."IdCliente" = master."IdCliente"
																AND movVenta."Nemotecnico" = detalle."NemotecnicoInstrumento"
																AND movVenta."EGRPrecio" = detalle."Precio"
														)
														ELSE 0
										END
					
								ELSE
									(SELECT (SUM((movVenta."PrecioCompra"*movVenta."EGRCantidad")*(IPCVNT."Valor"/CASE WHEN IPCCOM."Valor"=0 OR IPCCOM."Valor" IS NULL THEN 1 ELSE  IPCCOM."Valor" END))) FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS movCartera 
										RIGHT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS movVenta
										ON movVenta."EGRFolioComprobante" = master."IdComprobante"
										AND movVenta."EGRIdMovimientoCarteraEnTransito" = movCartera."IdMovimientoCarteraEnTransito"
										LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCCOM  ON
											((CASE
												WHEN movVenta."FechaLiquidacion" <> '0001-01-01' THEN
													substring(ADD_MONTHS (TO_DATE (movVenta."FechaLiquidacion", 'YYYY-MM-DD'), -1),0,7)
												ELSE
													substring(ADD_MONTHS (TO_DATE (movVenta."FechaComprobante", 'YYYY-MM-DD'), -1),0,7)
											 END) = IPCCOM."AgnoMes")  AND IPCCOM."IdParametroFinanciero" = 'IPC'
										LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCVNT  ON 
											(substring(ADD_MONTHS (TO_SECONDDATE (master."FechaComprobante", 'YYYY-MM-DD HH24:MI:SS'), -1),0,7) = IPCVNT."AgnoMes")  AND IPCVNT."IdParametroFinanciero" = 'IPC'
										WHERE movVenta."EGRIdMovimientoCarteraEnTransito" = movCartera."IdMovimientoCarteraEnTransito"
											AND movCartera."FolioComprobante" = master."IdComprobante"
											AND movCartera."IdTipoInstrumento" = 'ACN'
											AND movCartera."IdCliente" = master."IdCliente"
											AND movCartera."Nemotecnico" = detalle."NemotecnicoInstrumento"
											AND (movCartera."FolioExternoReferencia" = detalle."FolioTransaccion" OR movCartera."PrecioCompra" = detalle."Precio")
									)
				END
					
		ELSE 0
	END														AS "ValorAdquisicion",

	CASE WHEN master."IdTipoComprobante" IN ('FOE','NEO') AND master."IdTipoOperacion" NOT IN('VENOTC','COMOTC','ANVOTC','ANCOTC' ) THEN '00' 					-- Comisiones
		 WHEN master."IdTipoComprobante" IN ('FOE','NEO') AND master."IdTipoOperacion" IN ('VENOTC','COMOTC','ANVOTC','ANCOTC') THEN '99' 
		
	ELSE 
		CASE WHEN COALESCE(ins."IdTipoInstrumento",'') = 'ACN' THEN '01'																-- Acciones
			 WHEN COALESCE(ins."IdTipoInstrumento",'') = 'SPT' THEN '02'																-- Dolares
			 
			 WHEN COALESCE(ins."IdTipoInstrumento",'') = 'MON'  THEN 
			 	CASE 
			 		WHEN ins."Nemotecnico" LIKE 'DOLAR%' THEN '02'														-- Dolar
			 		WHEN ins."Nemotecnico" LIKE 'ORO%'	 THEN '03'														-- Oro
			 		ELSE '09'																							-- Otro
			 	END
			 
			 WHEN COALESCE(ins."IdTipoInstrumento",'') IN ('FBC','FDO','FIC','FIP','FUF') then '04'									-- Futuro
			 WHEN COALESCE(ins."IdTipoInstrumento",'') IN ('OSA','IOA','IOE','IOI','IOD','IOB','IOC','IOU','IOM','IOF') THEN '05'	-- Opciones
			 WHEN ins."IdClasificacionTipoInstrumento" = 'RF' THEN '06'
			 
			 WHEN COALESCE(ins."IdTipoInstrumento",'') = 'CFI' THEN '08'																-- CFI
			 WHEN COALESCE(ins."IdTipoInstrumento",'') = ''    THEN '09'																-- Otros Instrumentos de inversion nacionales
			 WHEN COALESCE(ins."IdTipoInstrumento",'') = 'ETN' THEN '10'																-- ETF
			 WHEN COALESCE(ins."IdTipoInstrumento",'') = 'PAG' THEN '21'																-- DAP Endosable		
		     WHEN COALESCE(ins."IdTipoInstrumento",'') = 'SIM' THEN '51'																-- Simultaneas	 
		     WHEN COALESCE(ins."IdTipoInstrumento",'') = 'ACX' THEN '31'																-- ACX
		     WHEN COALESCE(ins."IdTipoInstrumento",'') = 'CFX' THEN '37'																-- CFX
		     WHEN COALESCE(ins."IdTipoInstrumento",'') = 'ETX' THEN '38'																-- ETX		     

	ELSE '09'
	END
	END 															AS "TipoTransac",
	CASE
        WHEN master."AccionTipoOperacion" = 'COMPRA' AND COALESCE(ins."IdTipoInstrumento",'') <> 'SPT' THEN 'CO'
        WHEN master."AccionTipoOperacion" = 'VENTA'  AND COALESCE(ins."IdTipoInstrumento",'') <> 'SPT' THEN 'VE'
        WHEN master."AccionTipoOperacion" = 'COMPRA' AND COALESCE(ins."IdTipoInstrumento",'')  = 'SPT' THEN 'VE' --SPT se invierte
        WHEN master."AccionTipoOperacion" = 'VENTA'  AND COALESCE(ins."IdTipoInstrumento",'')  = 'SPT' THEN 'CO'
        ELSE ''
    END 															AS "IdTipoOperacion",
    COALESCE(detalle."IdTipoProducto",'')							AS "IdTipoProducto",
	master."IdCliente" 												AS "IdCliente",
	master."IdPortafolio" 											AS "IdPortafolio",
	detalle."IdComprobanteDetalle" 									AS "IdBaseDocumento",
	detalle."NroLinea" 												AS "LineaFactura"

	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 						AS master
			INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle"  	AS detalle ON detalle."IdComprobante" 	= master."IdComprobante"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			AS cli ON cli."IdCliente" 				= master."IdCliente" 
			LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		AS ins ON ins."IdInstrumento"=detalle."IdInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS ins_sim ON ins_sim."Nemotecnico" = detalle."NemotecnicoInstrumento" AND ins."IdTipoInstrumento" = 'SIM'
			LEFT JOIN 	"BSCL.Plataforma.BD::MER.ParametroFinanciero"				AS dol ON dol."Fecha"					= master."FechaComprobante" and  dol."IdParametroFinanciero" ='DO'
			INNER JOIN  "BSCL.Normativa.BD::NOR.Configuracion"						AS par ON master."IdEmpresa" 			= par."IdEmpresa"


	WHERE 	(master."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') AND master."Proposito" NOT IN ('REGAPV'))
	AND 	master."IdEstadoSII"  IN ('ACP','REC')
	AND 	master."IdTipoComprobante" IN ('FAE','FEE','FOE','FOX','NCE','NEE','NEO')
	AND 	master."FolioComprobante" > 0
	AND  COALESCE(TRIM(detalle."NemotecnicoInstrumento"),'') NOT IN (SELECT TRIM("Instrumento") FROM "BSCL.Normativa.BD::TRI.InstrumentosArticulo104" WHERE "Periodo" >= 2019)
	AND (CASE WHEN detalle."NemotecnicoInstrumento" IN ('DOLAR','ARBINT','DISTRI','INTERB') THEN detalle."Cantidad"		
		  WHEN detalle."NemotecnicoInstrumento" IN ('EURO') THEN (detalle."MontoLiquidacion"/dol."Valor")
		  ELSE 1
		END ) < par."Valor"	
	
	AND par."CodigoParametro" = 'MontoMinimoTributarioDJ1870'
	
	UNION ALL
	
-- ***********************************	PACTOS Y COMPRA/VENTA A TERMINO 			 *************************************************************************************************
	SELECT
	"IdEmpresa" 													AS "IdEmpresa",
	'O' 															AS "TipoDocumento",
	"IdOperacion" 													AS "NumeroDocumento",
	"FechaOperacion" 												AS "FechaDocumento",
	'' 																AS "RutEmisor",
	'' 																AS "Nemotecnico",
	"DocumentoIdentidad" 											AS "RutCliente",
	"CodigoPortafolio" 												AS "Cuenta",
	0 																AS "Cantidad",
	CASE WHEN "IdTipoOperacion" IN ('VENPAC','VENTER') THEN  "MontoOperacion" ELSE "MontoOperacionFinalMoneda" END  AS "MontoCompras",
	CASE WHEN "IdTipoOperacion" IN ('VENPAC','VENTER') THEN  "MontoOperacionFinalMoneda" ELSE "MontoOperacion" END  AS "MontoVentas",
	0																AS "ValorAdquisicion",
	CASE
		WHEN "IdTipoOperacion" IN ('COMPAC','VENPAC') 
			THEN '11'
		WHEN "IdTipoOperacion" IN ('COMTER','VENTER') 
			THEN '09'
	END 															AS "TipoTransaccion" ,
	-- se debe dar vuelta el sentido porque la dj se informa desde
	-- la vista del cliente y la mesa desde la vista del corredor    
	CASE
		WHEN "IdTipoOperacion" = 'COMTER' THEN 'VT'
		WHEN "IdTipoOperacion" = 'VENTER' THEN 'CT'
		WHEN "IdTipoOperacion" = 'COMPAC' THEN 'VP'
		WHEN "IdTipoOperacion" = 'VENPAC' THEN 'CP'
	END 															AS "IdTipoOperacion",
	"IdTipoProducto"												AS "IdTipoProducto",
	"IdCliente" 													AS "IdCliente",
	"IdPortafolio" 													AS "IdPortafolio",
	'0'																AS "IdBaseDocumento",
	'1' 															AS "LineaFactura"	
	FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones"
	WHERE
		"IdTipoProducto" = 'MERF'
	AND "IdEstado" NOT IN ( 'ANU', 'PEN')
	
	UNION ALL 
	
	SELECT
		"IdEmpresa" 													AS "IdEmpresa",
		'O' 															AS "TipoDocumento",
		"FolioPrestamo" 												AS "NumeroDocumento",
		"FechaPrestamo" 												AS "FechaDocumento",
		'' 																AS "RutEmisor",
		"NemoSubyacente" 												AS "Nemotecnico",
		"DocumentoIdentidad" 											AS "RutCliente",
		"CodigoPortafolio" 												AS "Cuenta",
		"Cantidad" 														AS "Cantidad",
		0 																AS "MontoCompras",
		"INGMontoTotalPrestamo" 										AS "MontoVentas",
		0																AS "ValorAdquisicion",
		CASE
			WHEN "NemotecnicoPrestamo" = 'VENTA CORTA' 		THEN '61'
			WHEN "NemotecnicoPrestamo" = 'ADMINISTRATIVO' 	THEN '62'
			WHEN "NemotecnicoPrestamo" = 'FINES GENERALES'	THEN '63'
			WHEN "NemotecnicoPrestamo" = '' 				THEN '64'
			ELSE '64'
		END AS "TipoTransaccion",
		'PP' 															AS "IdTipoOperacion",
		"IdTipoProducto"												AS "IdTipoProducto",
		"IdCliente"														AS "IdCliente",
		"IdPortafolio"													AS "IdPortafolio",
		'0'																AS "IdBaseDocumento",
		'0' 															AS "LineaFactura"
	FROM
		"BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
		
		UNION ALL 
		
		SELECT
		EMP."IdEmpresa" 													AS "IdEmpresa",
		'O' 																AS "TipoDocumento",
		EMP."FolioPrestamo" 												AS "NumeroDocumento",
		EMP."FechaPrestamo" 												AS "FechaDocumento",
		'' 																	AS "RutEmisor",
		EMP."NemoSubyacente" 												AS "Nemotecnico",
		EMP."DocumentoIdentidad" 											AS "RutCliente",
		EMP."CodigoPortafolio" 												AS "Cuenta",
		EMP."Cantidad" 														AS "Cantidad",
		0 																	AS "MontoCompras",
		EMP."INGMontoTotalEmprestito" 										AS "MontoVentas",
		0																	AS "ValorAdquisicion",
		CASE
			WHEN PRE."NemotecnicoPrestamo" = 'VENTA CORTA' 		THEN '61'
			WHEN PRE."NemotecnicoPrestamo" = 'ADMINISTRATIVO' 	THEN '62'
			WHEN PRE."NemotecnicoPrestamo" = 'FINES GENERALES' 	THEN '63'
			WHEN PRE."NemotecnicoPrestamo" = '' 				THEN '64'
			ELSE '64'
		END 																AS "TipoTransaccion",
		'DP'																AS "IdTipoOperacion",
		PRE."IdTipoProducto"												AS "IdTipoProducto",
		EMP."IdCliente"														AS "IdCliente",
		EMP."IdPortafolio"													AS "IdPortafolio",
		'0'																	AS "IdBaseDocumento",
		'0'																	AS "LineaFactura" 
	FROM
		"BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" AS EMP LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" AS PRE
		ON (EMP."FolioPrestamo" = PRE."FolioPrestamo")

with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1891.Origen" to bcs_desarrollador with grant option;