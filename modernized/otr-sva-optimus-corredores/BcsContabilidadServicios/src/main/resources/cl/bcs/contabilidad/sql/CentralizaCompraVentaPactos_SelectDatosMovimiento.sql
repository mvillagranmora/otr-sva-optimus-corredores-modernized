WITH consultaCarteraPacto AS (
SELECT
pac."FolioCartera" as "FolioPacto",
pac."IdCartera",
pac."IdBolsa",
pac."IdConcepto",
pac."FechaOperacion" as "FechaPacto",
pac."IdEstado",
pac."IdMonedaSistema",
pac."IdTipoOperacion",
pac."Nemotecnico" as "NemotecnicoPacto",
pac."IdEmpresa",
sby."IdEmisor" 				as "IdEmisorSubyacente",
Cli."DocumentoIdentidad",
pac."IdPortafolio",
pac."IdCliente" ,
pac."IdAgente",
pac."IdMesa" ,
pac."IdClasificacionTipoInstrumento",
pac."IdOperacion" ,
Port."CodigoPortafolio",
pac."IdTipoProducto",
sby."IdClasificacionTipoInstrumento"		as "IdClasificacionTipoInstrumentoSubyacente",
sby."IdTipoInstrumento"		as "IdTipoInstrumentoSubyacente",
pac."IdBoveda",
sby."IdTipoReajusteSubyacente", 
sby."CodigoIFRS" as "CodigoIFRSSubyacente",
pac."EGRCantidad",
pac."INGFechaOperacion",
pac."INGFechaLiquidacion",
pac."INGCantidad",
pac."INGValorRescate",
pac."EGRFechaLiquidacion",
pac."EGRFechaOperacion",
sby."Nemotecnico"			as "NemoSubyacente"
FROM "BSCL.Custodia.BD::CUS.Cartera" as pac
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (pac."FolioCartera" = ultSub."FolioCarteraRelacionada")
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente") 
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON (Port."IdPortafolio" = pac."IdPortafolio")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON (Cli."IdEmpresa" = pac."IdEmpresa" and Cli."IdCliente" = Port."IdCliente")
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipOpe ON (TipOpe."IdTipoOperacion" = pac."IdTipoOperacion")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON (pac."IdConcepto" = Concepto."IdConcepto")
WHERE pac."IdTipoInstrumento" = 'PAC' and pac."IdEstado"IN ('VIG','VCD','ANT')
)
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

,CCP."IdMonedaSistema"																		AS "Moneda"                                                      
,CCP."IdTipoProducto"																		AS "Producto"
,CCP."IdClasificacionTipoInstrumentoSubyacente" 											AS "ClaseInstrumento"
,CCP."IdTipoInstrumentoSubyacente" 															AS "TipoInstrumento"
,"Nemotecnico" 																				AS "Nemo"
,CCP."IdBoveda"																				AS "Boveda"	
,COALESCE(CCP."IdCartera",0)																AS "IdCartera"		
,ConsultaSubyacente."IdEmisor"																AS "Emisor"
,ConsultaSubyacente."IdTipoReajuste"														AS "TipoReajuste"
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
FROM consultaCarteraPacto		AS CCP		
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
WHERE CCP."IdEstado" IN ('VIG','VCD','ANT') AND CCP."FechaPacto" = :FechaContable
UNION ALL
SELECT
CCP."IdCartera"																				AS "IdMovimiento"
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

,CCP."IdMonedaSistema"																		AS "Moneda"                                                      
,CCP."IdTipoProducto"																		AS "Producto"
,CCP."IdClasificacionTipoInstrumentoSubyacente" 											AS "ClaseInstrumento"
,CCP."IdTipoInstrumentoSubyacente" 															AS "TipoInstrumento"
,CCP."NemotecnicoPacto" 																	AS "Nemo"
,CCP."IdBoveda"																				AS "Boveda"	
,COALESCE(CCP."IdCartera",0)																AS "IdCartera"		
,''																							AS "Emisor"
,CCP."IdTipoReajusteSubyacente"																AS "TipoReajuste"
,ifnull(CCP."CodigoIFRSSubyacente",'')														AS "CodigoIFRS"
,'CUSTODIA'																					AS "TipoCartera"
,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
(CCP."IdEmisorSubyacente", CCP."IdClasificacionTipoInstrumentoSubyacente", 
 		 CCP."IdTipoInstrumentoSubyacente", CCP."NemoSubyacente", CCP."IdTipoReajusteSubyacente")
 																								AS "ClaseEmisor"
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
,''																							AS "TipoEmisor"																											
,COALESCE(MES."Codigo",'')	AS "Mesa"
,COALESCE(OPE."FolioOperacion",TO_BIGINT(0)) AS  "FolioOperacion"
FROM consultaCarteraPacto		AS CCP		
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion ON (Operacion."FolioCartera" = CCP."FolioPacto")

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
WHERE CCP."IdEstado" IN ('VIG','VCD','ANT') AND CCP."FechaPacto" = :FechaContable;