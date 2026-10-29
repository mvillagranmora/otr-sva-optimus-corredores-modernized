SELECT		
		Emprestito."FolioEmprestito"	 													AS "IdMovimiento"		 
		,LEFT(Emprestito."DocumentoIdentidad",60)											AS "DocumentoIdentidad"                                    
		,LEFT(ConsultarCliente."NombreCompleto",60)											AS "NombreCliente"                                       
		,LEFT(Emprestito."CodigoPortafolio",60)												AS "Portafolio"
		,LEFT(Emprestito."DocumentoIdentidad",60)											AS "DocumentoIdentidad"                                    
		,LEFT(ConsultarCliente."NombreCompleto",60)											AS "NombreCliente"                                       
		,LEFT(Emprestito."CodigoPortafolio",60)												AS "Portafolio"
		,Emprestito."IdMonedaInstrumento"													AS "Moneda"                                                      
		,Emprestito."IdTipoProducto"														AS "Producto"
		,Emprestito."IdClasificacionTipoInstrumento" 										AS "ClaseInstrumento"
		,Emprestito."IdTipoInstrumento" 													AS "TipoInstrumento"
		,Emprestito."NemoSubyacente"														AS "Nemo"
		,Emprestito."IdBoveda"																AS "Boveda"	
		,CAST(Emprestito."IdCartera" AS NVARCHAR(60))										AS "IdCartera"		
		,Emprestito."IdEmisor"																AS "Emisor"
		,Emprestito."IdTipoReajuste"														AS "TipoReajuste"
		,Emprestito."CodigoIFRS"															AS "CodigoIFRS"
		,'CUSTODIA'																			AS "TipoCartera"
		,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
			(Emprestito."IdEmisor", Emprestito."IdClasificacionTipoInstrumento", 
	 		 Emprestito."IdTipoInstrumento", Emprestito."NemoSubyacente", Emprestito."IdTipoReajuste")
																							AS "ClaseEmisor"
		,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'')    									AS "Agente"
		,LEFT(IFNULL(ConsultarEjecutivo."NombreCompleto",''),60) 							AS "NombreAgente"
		,IFNULL(ConsultarEjecutivo."CodigoSucursal",'') 									AS "SucursalAgente"
		,LEFT(IFNULL(ConsultarEjecutivo."NombreSucursal",''),60) 							AS "NombreSucursalAgente"
		,LEFT(IFNULL(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)					AS "CentroCostoAgente"
		,CASE 
			WHEN Emprestito."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"	THEN 'CP'
			WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 						THEN 'RE'
			ELSE																				'NR'
		END 																				AS "Relacion"
		,Emprestito."IdBolsa"																AS "Bolsa"
		,CAST(Emprestito."FechaLiquidacion" AS NVARCHAR(10))								AS "FechaLiquidacion"
		,ConsultarCliente."Tipo"															AS "TipoPersona"
		,CAST(Emprestito."FechaEmprestito" AS NVARCHAR(10))									AS "FechaOperacion"
		,Emprestito."IdConcepto"															AS "IdConcepto"
		,Emprestito."IdCondicionLiquidacion"												AS "CondicionLiquidacion"
		,CASE 
				WHEN Emprestito."IdTipoPrestamoPrestamo" IN ('PRTVTC','PRVCRF')		 	THEN 'VENTA CORTA'
				WHEN Emprestito."IdTipoPrestamoPrestamo" IN ('PRTADM','PRADRF')			THEN 'ADMINISTRATIVO'
				WHEN Emprestito."IdTipoPrestamoPrestamo" IN ('PRTFIN','PRFGRF')			THEN 'FINES GENERALES'
				ELSE																	'??'
		END	
		,ConsultarCliente."NombreSegmentoCliente"													AS "TipoPrestatario"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" Emprestito
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente ON ConsultarCliente."IdCliente" = Emprestito."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa ON ConsultarEmpresa."IdEmpresa" = Emprestito."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio ON ConsultarPortafolio."IdPortafolio" = Emprestito."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = Emprestito."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente ON ConsultarIndicadoresCliente."IdCliente" =  Emprestito."IdCliente" AND ((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR (ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
WHERE
    	Emprestito."IdEmpresa" 			= :IdEmpresa		AND 
    	Emprestito."FechaLiquidacion" 	= :FechaContable 	AND
    	Emprestito."IdEstado"			= 'VIG'	 			AND
    	Emprestito."IdClasificacionTipoInstrumento"= :IdClasificacionTipoInstrumento
    	
GROUP BY 
    	 Emprestito."FolioEmprestito"
    	,Emprestito."DocumentoIdentidad"
    	,ConsultarCliente."NombreCompleto"
    	,Emprestito."CodigoPortafolio"
    	,ConsultarCliente."DocumentoIdentidad"
    	,ConsultarPortafolio."CodigoPortafolio"
    	,Emprestito."IdMonedaInstrumento"
    	,Emprestito."IdTipoProducto"
    	,Emprestito."IdClasificacionTipoInstrumento"
    	,Emprestito."IdTipoInstrumento"
    	,Emprestito."NemoSubyacente"
    	,Emprestito."IdBoveda"
    	,Emprestito."IdCartera"
    	,Emprestito."IdEmisor"
    	,Emprestito."IdTipoReajuste"
    	,Emprestito."CodigoIFRS" 
		,Emprestito."IdEmisor"
		,Emprestito."IdClasificacionTipoInstrumento"
		,Emprestito."IdTipoInstrumento"
		,Emprestito."IdTipoReajuste"
		,ConsultarEjecutivo."CodigoEmpleado"
		,ConsultarEjecutivo."NombreCompleto"
		,ConsultarEjecutivo."CodigoSucursal"
		,ConsultarEjecutivo."NombreSucursal"
		,ConsultarIndicadoresEmpleado."ValorIndicador"
		,ConsultarIndicadoresCliente."ValorIndicador"
		,Emprestito."IdBolsa"
		,Emprestito."FechaLiquidacion"
		,ConsultarCliente."Tipo"
		,Emprestito."FechaEmprestito"
		,Emprestito."IdConcepto"
		,Emprestito."IdCondicionLiquidacion"
		,Emprestito."IdTipoPrestamoPrestamo"											
		,ConsultarCliente."NombreSegmentoCliente"
		,ConsultarEmpresa."DocumentoIdentidad";