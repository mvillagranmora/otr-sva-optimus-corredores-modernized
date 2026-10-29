SELECT
ConsultaSubyacente."IdSubyacente"															AS "IdMovimiento"
,LEFT(CCP."DocumentoIdentidad",60)															AS "DocumentoIdentidad"                                    
,LEFT(CCL."NombreCompleto",60)																AS "NombreCliente"                                       
,LEFT(CCP."CodigoPortafolio",60)															AS "Portafolio"
,CASE CCP."NemotecnicoPacto"
	WHEN 'PACTO VENTA'	
		THEN	'V'			
	WHEN 'PACTO COMPRA' 
		THEN  	'C'
	ELSE '????????????'			
	END																						AS "CompraVenta"--AS "TipoPacto"			

,CASE WHEN ConsultaSubyacente."IdMonedaInstrumento" <> '' THEN ConsultaSubyacente."IdMonedaInstrumento" ELSE CCP."IdMonedaSistema" END AS "Moneda"                                                      
,CCP."IdTipoProducto"																		AS "Producto"
,CCP."IdClasificacionTipoInstrumentoSubyacente" 											AS "ClaseInstrumento"
,CCP."IdTipoInstrumentoSubyacente" 															AS "TipoInstrumento"
,"Nemotecnico" 																				AS "Nemo"
,CCP."IdBoveda"																				AS "Boveda"	
,COALESCE(CCP."IdCartera",0)																AS "IdCartera"		
,ConsultaSubyacente."IdEmisor"																AS "Emisor"
,CCP."IdTipoReajusteSubyacente"																AS "TipoReajuste"
,ifnull(CCP."CodigoIFRSSubyacente",'')														AS "CodigoIFRS"
,'CUSTODIA'																					AS "TipoCartera"
,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" (ConsultaSubyacente."IdEmisor", ConsultaSubyacente."IdClasificacionTipoInstrumento", ConsultaSubyacente."IdTipoInstrumento", ConsultaSubyacente."Nemotecnico", CCP."IdTipoReajusteSubyacente") AS "ClaseEmisor"		 		 
,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'')    											AS "Agente"
,LEFT(IFNULL(ConsultarEjecutivo."NombreCompleto",''),60) 									AS "NombreAgente"
,IFNULL(ConsultarEjecutivo."CodigoSucursal",'')												AS "SucursalAgente"
,LEFT(IFNULL(ConsultarEjecutivo."NombreSucursal",''),60) 									AS "NombreSucursalAgente"
,LEFT(IFNULL(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)							AS "CentroCostoAgente"
,CASE WHEN CCP."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
		THEN 'CP'
	  WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 	
	  	THEN 'RE'
	  ELSE	'NR'
	  END 																					AS "Relacion"
,CCP."IdBolsa" 																				AS "Bolsa"
,CCL."Tipo"																					AS "TipoPersona"
,CCP."IdConcepto"	 																		AS "Concepto"
,CASE WHEN CCP."EGRCantidad" = 0 THEN CCP."INGFechaLiquidacion"
							ELSE CCP."EGRFechaLiquidacion" END
																							AS "FechaLiquidacion"
,CASE WHEN CCP."EGRCantidad" = 0 THEN CCP."INGFechaOperacion"
							ELSE CCP."EGRFechaOperacion" END
																							AS "FechaOperacion"
,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
 CCP."IdClasificacionTipoInstrumento"
,ConsultaSubyacente."IdTipoInstrumento"
,ConsultaSubyacente."Nemotecnico"
,ConsultaSubyacente."IdEmisor")																AS "TipoEmisor"																											
,COALESCE(MES."Codigo",'')	AS "Mesa"
,COALESCE(OPE."FolioOperacion",TO_BIGINT(0)) AS  "FolioOperacion"
,COALESCE(DET."Fase",0) AS "Fase"
FROM "BSCL.Custodia.BD.Vistas::MES.ConsultarCarteraPacto"		AS CCP		
INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"	ConsultaSubyacente
ON (CCP."FolioPacto" = ConsultaSubyacente."FolioCarteraRelacionada")

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS CCL
		ON CCL."IdCliente" = CCP."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
		ON ConsultarEmpresa."IdEmpresa" = CCP."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
		ON ConsultarPortafolio."IdPortafolio" = CCP."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
		ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = CCP."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
		ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
		ON ConsultarIndicadoresCliente."IdCliente" =  CCP."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON CCP."IdMesa" = MES."IdMesa" 
LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE ON OPE."IdOperacion" = CCP."IdOperacion"
INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = ConsultaSubyacente."IdEmisor" AND DET."IdEstado" = 'ACT'
WHERE CCP."IdEstado" = 'VIG' 
	AND CCP."FechaPacto" = :FechaContable
	AND CCP."IdTipoOperacion" IN ('COMPAC', 'VENPAC')
		AND DET."FlagDeterioro" = 'S'
		AND DET."PorcentajeDeterioro" > 0;