WITH MovimientosIIF AS (
	SELECT MAX("IdEmisor") AS "IdEmisor",  MvtoCmbteDet."IdComprobanteDetalle"
	FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MvtoCmbte
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" As MvtoCmbteDet
		ON MvtoCmbte."IdMovimientoComprobante" = MvtoCmbteDet."IdMovimientoComprobante"
	WHERE
		MvtoCmbte."IdEmpresa" 	= :IdEmpresa 		AND
		MvtoCmbte."IdClasificacionTipoInstrumento" = 'IF' AND
		TO_DATE(MvtoCmbte."FechaSistema")  = :FechaContable 	AND 
		MvtoCmbte."DocumentoIdentidad" = :DocumentoIdentidad 
	GROUP BY MvtoCmbteDet."IdComprobanteDetalle"
)
SELECT 
 ConsultarComprobanteDetalle."IdComprobanteDetalle"		AS "IdMovimiento"
,'C' || ConsultarComprobante."IdTipoComprobante" 		AS "TipoDocumento"
,COALESCE(ConsultarComprobante."IdComprobante",0)		AS "Folio"
,COALESCE(ConsultarComprobante."FolioComprobante",0)	AS "FolioDTE"
,YEAR(ConsultarComprobante."FechaSistema") ||'-'|| RIGHT('0' || MONTH(ConsultarComprobante."FechaSistema"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(ConsultarComprobante."FechaSistema"),2) AS "FechaDocumento"
,LEFT(ConsultarComprobante."AccionTipoOperacion",1)		AS "CompraVenta"
,ConsultarComprobante."IdTipoOperacion" 				AS "Operacion"
,ConsultarComprobante."CodigoTipoProducto"				AS "Producto"
,ConsultarComprobante."IdClasificacionTipoInstrumento" 	AS "ClaseInstrumento"
,ConsultarInstrumentoNemotecnico."IdTipoInstrumento" 	AS "TipoInstrumento"
,ConsultarComprobante."IdCondicionLiquidacion" 			AS "CondicionLiquidacion"
,ConsultarComprobante."IdMonedaComprobante" 			AS "MonedaComprobante"
,ConsultarComprobante."IdBolsa" 						AS "Bolsa"
,ConsultarComprobante."DocumentoIdentidad" 				AS "DocumentoIdentidad"
,LEFT(CASE Persona."Tipo" 
			WHEN 'N' THEN Persona."Nombres" ||' '|| Persona."ApellidoPaterno" || ' ' ||Persona."ApellidoMaterno" 
			ELSE Persona."RazonSocial"
		END,60) 										AS "NombreCliente"
,LEFT(ConsultarPortafolio."CodigoPortafolio",60)		AS "Portafolio"
,ConsultarComprobante."IdConcepto"						AS "Concepto"
,LEFT(ConsultarConcepto."Nombre",60) 					AS "NombreConcepto"
,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'') 					AS "Agente"
,LEFT(IFNULL(ConsultarEjecutivo."NombreCompleto",''),60) 			AS "NombreAgente"
,IFNULL(ConsultarEjecutivo."CodigoSucursal",'') 					AS "SucursalAgente"
,LEFT(IFNULL(ConsultarEjecutivo."NombreSucursal",''),60) 			AS "NombreSucursalAgente"
,LEFT(IFNULL(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)	AS "CentroCostoAgente"
,"IdEstadoSII" 											AS "EstadoSII"
,CASE "EsExento" WHEN 'N' THEN 'NO' ELSE 'SI' END		AS "EsExento"
,ConsultarComprobante."IdMoneda" 						AS "Moneda"
,ConsultarComprobante."IdMonedaLiquidacion" 			AS "MonedaLiquidacion"
,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
((CASE ConsultarComprobante."IdClasificacionTipoInstrumento" WHEN 'IF' 
	THEN COALESCE(MOVIIF."IdEmisor",'') 
	ELSE ConsultarInstrumento."IdEmisor" END), ConsultarComprobante."IdClasificacionTipoInstrumento", 
 ConsultarInstrumentoNemotecnico."IdTipoInstrumento", ConsultarComprobanteDetalle."NemotecnicoInstrumento", ConsultarInstrumento."IdTipoReajuste")
	 													AS "ClaseEmisor"
,CASE ConsultarComprobante."IdClasificacionTipoInstrumento" WHEN 'IF' 
	THEN COALESCE(MOVIIF."IdEmisor",'') 
	ELSE ConsultarInstrumento."IdEmisor" END				AS "Emisor"
,ConsultarComprobanteDetalle."NemotecnicoInstrumento"	AS "Nemotecnico"
,CASE WHEN ConsultarComprobante."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
						THEN 'CP'
		  	WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 	
		  				THEN 'RE'
		  	ELSE	'NR'
		  END 											AS "Relacion"
,Persona."Tipo"								AS "TipoPersona"
,ConsultarComprobante."CodigoTipoComprobante"			AS "CodigoTipoComprobante"
,ConsultarComprobante."FechaLiquidacion"				AS "FechaLiquidacion"
,ConsultarComprobante."IdTipoComprobante"				AS "TipoComprobante"
,ConsultarComprobante."FechaComprobante"				AS "FechaComprobante"				
,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
	 ConsultarComprobante."IdClasificacionTipoInstrumento"
	,ConsultarComprobanteDetalle."IdTipoInstrumento"
	,ConsultarComprobanteDetalle."NemotecnicoInstrumento"
	,(CASE ConsultarComprobante."IdClasificacionTipoInstrumento" WHEN 'IF' 
		THEN COALESCE(MOVIIF."IdEmisor",'') 
		ELSE ConsultarInstrumento."IdEmisor" END))				AS "TipoEmisor"
,TRIM(ifnull(ConsultarComprobante."CodigoIFRS",''))				AS "CodigoIFRS"		
,COALESCE(ConsultarComprobanteDetalle."IndicadorOrdenDirecta",'')	AS "IndicadorOrdenDirecta"
,ConsultarInstrumento."IdTipoReajuste" as "TipoReajuste"
,area."Descripcion" AS "AreaCliente"
,COALESCE(ConsultarComprobante."FolioComprobante",0)	AS "FolioDTE"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" ConsultarComprobante
	LEFT JOIN  "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" ConsultarComprobanteDetalle
		ON ConsultarComprobanteDetalle."IdComprobante" = ConsultarComprobante."IdComprobante"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ConsultarInstrumento
		ON ConsultarInstrumento."IdInstrumento" = ConsultarComprobanteDetalle."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ConsultarInstrumentoNemotecnico
		ON ConsultarInstrumentoNemotecnico."Nemotecnico" = ConsultarComprobanteDetalle."NemotecnicoInstrumento"	 
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
		ON ConsultarEmpresa."IdEmpresa" = ConsultarComprobante."IdEmpresa"
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" Cliente
	ON Cliente."IdCliente" =  ConsultarComprobante."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" Persona 
	ON Persona."IdPersona" =  Cliente."IdPersona"
	LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio" ConsultarPortafolio 
		ON ConsultarPortafolio."IdPortafolio" = ConsultarComprobante."IdPortafolio" 
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" ConsultarConcepto
		ON ConsultarConcepto."IdConcepto" = ConsultarComprobante."IdConcepto"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
		ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarComprobante."IdEmpleadoEjecutivoPortafolio"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
		ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
		ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarComprobante."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
	LEFT JOIN MovimientosIIF MOVIIF ON MOVIIF."IdComprobanteDetalle" = ConsultarComprobanteDetalle."IdComprobanteDetalle"
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Area" AS area ON ConsultarPortafolio."AreaCliente" = area."Codigo"
WHERE
	ConsultarComprobante."IdEmpresa" 	= :IdEmpresa 		AND
	ConsultarComprobante."FechaSistema" = :FechaContable 	AND 
	"IdEstadoDTE"  IN ('PEN', 'ENV', 'REC') 				AND 
	ConsultarComprobante."DocumentoIdentidad" = :DocumentoIdentidad AND
	ConsultarComprobante."IdEstadoComprobante" <> 'ANU'