SELECT			
	PRT."IdCartera"		 														AS "IdMovimiento"		 
	,LEFT(PRT."DocumentoIdentidad",60)											AS "DocumentoIdentidad"                                    
	,LEFT(ConsultarCliente."NombreCompleto",60)									AS "NombreCliente"                                       
	,LEFT(PRT."CodigoPortafolio",60)											AS "Portafolio"
	,LEFT(PRT."DocumentoIdentidad",60)											AS "DocumentoIdentidad"                                    
	,LEFT(ConsultarCliente."NombreCompleto",60)									AS "NombreCliente"                                       
	,LEFT(PRT."CodigoPortafolio",60)											AS "Portafolio"
	,PRT."IdMonedaInstrumento"													AS "Moneda"                                                      
	,PRT."IdTipoProducto"														AS "Producto"
	,PRT."IdClasificacionTipoInstrumento" 										AS "ClaseInstrumento"
	,PRT."IdTipoInstrumento" 													AS "TipoInstrumento"
	,PRT."NemoSubyacente"														AS "Nemo"
	,PRT."IdBoveda"																AS "Boveda"	
	,CAST(PRT."IdCartera" AS NVARCHAR(60))										AS "IdCartera"		
	,PRT."IdEmisor"																AS "Emisor"
	,PRT."IdTipoReajuste"														AS "TipoReajuste"
	,PRT."CodigoIFRS"															AS "CodigoIFRS"
	,'CUSTODIA'																	AS "TipoCartera"
	,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
	(PRT."IdEmisor", PRT."IdClasificacionTipoInstrumento", 
	 PRT."IdTipoInstrumento", PRT."NemoSubyacente", PRT."IdTipoReajuste")       AS "ClaseEmisor"
	,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'')    							AS "Agente"
	,LEFT(IFNULL(ConsultarEjecutivo."NombreCompleto",''),60) 					AS "NombreAgente"
	,IFNULL(ConsultarEjecutivo."CodigoSucursal",'') 							AS "SucursalAgente"
	,LEFT(IFNULL(ConsultarEjecutivo."NombreSucursal",''),60) 					AS "NombreSucursalAgente"
	,LEFT(IFNULL(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)			AS "CentroCostoAgente"
	,CASE WHEN PRT."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad" THEN 'CP'
		  WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 				THEN 'RE'
		  ELSE																		 'NR'
			END 																AS "Relacion"
	,PRT."IdBolsa"																AS "Bolsa"
	
	,CAST(PRT."FechaLiquidacion" AS NVARCHAR(10))								AS "FechaLiquidacion"
	,ConsultarCliente."Tipo"													AS "TipoPersona"
	,CAST(PRT."EGRFechaOperacion" AS NVARCHAR(10))								AS "FechaOperacion"
	,PRT."IdConcepto"															AS "IdConcepto"
	,PRT."IdCondicionLiquidacion"												AS "CondicionLiquidacion"

	,CASE 
		WHEN PRT."IdTipoPrestamo" IN ('PRTVTC','PRVCRF')		 		THEN 'VENTA CORTA'
		WHEN PRT."IdTipoPrestamo" IN ('PRTADM','PRADRF')				THEN 'ADMINISTRATIVO'
		WHEN PRT."IdTipoPrestamo" IN ('PRTFIN','PRFGRF')				THEN 'FINES GENERALES'
		ELSE																	'ERROR'
	END																			AS "TipoPrestamo"
	,ConsultarCliente."NombreSegmentoCliente"									AS "TipoPrestatario"
	,CASE ConsultarCliente."DocumentoIdentidad" 
		WHEN ConsultarCliente."DocumentoIdentidad" 						THEN 'SI' 
		ELSE 																 'NO' 
	END 																		AS "EsCuentaPropia"
	,CASE
		WHEN COALESCE(ConsultarIndicadoresCliente."ValorIndicador",'N') ='S'	THEN 'SI' 
		ELSE 																	 	 'NO' 
	END 																		AS "EsRelacionado"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoHistorico" PRT
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente ON ConsultarCliente."IdCliente" = PRT."IdCliente"
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa ON ConsultarEmpresa."IdEmpresa" = PRT."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio ON ConsultarPortafolio."IdPortafolio" = PRT."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = PRT."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente ON ConsultarIndicadoresCliente."IdCliente" =  PRT."IdCliente" AND ((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR (ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))

WHERE PRT."IdEmpresa" 		= :IdEmpresa		AND 
	PRT."EGRFechaOperacion" = :FechaContable 	AND
	PRT."IdEstado"			in ('VCD', 'ANT')   AND
	PRT."IdClasificacionTipoInstrumento"= :ClasificacionTipoInstrumento
	
GROUP BY
	PRT."FolioPrestamo"	 
	,PRT."DocumentoIdentidad"                                 
	,ConsultarCliente."NombreCompleto"                                     
	,PRT."CodigoPortafolio"
	,PRT."IdMonedaInstrumento"                                                  
	,PRT."IdTipoProducto"
	,PRT."IdClasificacionTipoInstrumento"
	,PRT."IdTipoInstrumento"
	,PRT."NemoSubyacente"
	,PRT."IdBoveda"
	,PRT."IdCartera"	
	,PRT."IdEmisor"
	,PRT."IdTipoReajuste"
	,PRT."CodigoIFRS"
	,ConsultarEjecutivo."CodigoEmpleado"
	,ConsultarEjecutivo."NombreCompleto"
	,ConsultarEjecutivo."CodigoSucursal"
	,ConsultarEjecutivo."NombreSucursal"
	,ConsultarIndicadoresEmpleado."ValorIndicador"
	,ConsultarEmpresa."DocumentoIdentidad"
	,ConsultarIndicadoresCliente."ValorIndicador" 
	,PRT."IdBolsa"	
	,PRT."FechaLiquidacion"
	,ConsultarCliente."Tipo"
	,PRT."EGRFechaOperacion"
	,PRT."IdConcepto"
	,PRT."IdCondicionLiquidacion" 
	,PRT."IdTipoPrestamo"
	,ConsultarCliente."NombreSegmentoCliente"
	,ConsultarCliente."DocumentoIdentidad";