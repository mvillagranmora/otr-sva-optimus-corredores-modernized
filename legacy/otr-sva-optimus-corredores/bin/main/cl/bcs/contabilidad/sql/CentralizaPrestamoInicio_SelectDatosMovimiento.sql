SELECT
		 Prestamo."FolioPrestamo" 															AS "IdMovimiento"		 
		,LEFT(Prestamo."DocumentoIdentidad",60)												AS "DocumentoIdentidad"                                    
		,LEFT(ConsultarCliente."NombreCompleto",60)											AS "NombreCliente"                                       
		,LEFT(Prestamo."CodigoPortafolio",60)												AS "Portafolio"
		,LEFT(Prestamo."DocumentoIdentidad",60)												AS "DocumentoIdentidad"                                    
		,LEFT(ConsultarCliente."NombreCompleto",60)											AS "NombreCliente"                                       
		,LEFT(Prestamo."CodigoPortafolio",60)												AS "Portafolio"
		,Prestamo."IdMonedaInstrumento"														AS "Moneda"                                                      
		,Prestamo."IdTipoProducto"															AS "Producto"
		,Prestamo."IdClasificacionTipoInstrumento" 											AS "ClaseInstrumento"
		,Prestamo."IdTipoInstrumento" 														AS "TipoInstrumento"
		,Prestamo."NemoSubyacente"															AS "Nemo"
		,Prestamo."IdBoveda"																AS "Boveda"	
		,CAST(Prestamo."IdCartera" AS NVARCHAR(60))											AS "IdCartera"		
		,Prestamo."IdEmisor"																AS "Emisor"
		,Prestamo."IdTipoReajuste"															AS "TipoReajuste"
		,Prestamo."CodigoIFRS"																AS "CodigoIFRS"
		,'CUSTODIA'																			AS "TipoCartera"
		,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
			(Prestamo."IdEmisor", Prestamo."IdClasificacionTipoInstrumento", 
	 		 Prestamo."IdTipoInstrumento", Prestamo."NemoSubyacente", Prestamo."IdTipoReajuste")		
	 		 																				AS "ClaseEmisor"
		,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'')    									AS "Agente"
		,LEFT(IFNULL(ConsultarEjecutivo."NombreCompleto",''),60) 							AS "NombreAgente"
		,IFNULL(ConsultarEjecutivo."CodigoSucursal",'') 									AS "SucursalAgente"
		,LEFT(IFNULL(ConsultarEjecutivo."NombreSucursal",''),60) 							AS "NombreSucursalAgente"
		,LEFT(IFNULL(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)					AS "CentroCostoAgente"
		,CASE 
			WHEN Prestamo."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"  THEN 'CP'
			WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S'						THEN 'RE'
			ELSE																			'NR'
		END 																				AS "Relacion"
		,Prestamo."IdBolsa"																	AS "Bolsa"
			
		,TO_VARCHAR("FechaLiquidacion", 'YYYY-MM-DD') 										AS "FechaLiquidacion" 
		,ConsultarCliente."Tipo"															AS "TipoPersona"
		,TO_VARCHAR(Prestamo."FechaPrestamo", 'YYYY-MM-DD')									AS "FechaOperacion" 
		,Prestamo."IdConcepto"																AS "IdConcepto"
		,Prestamo."IdCondicionLiquidacion"													AS "CondicionLiquidacion"
		,CASE 
			WHEN Prestamo."IdTipoPrestamo" IN ('PRTVTC','PRVCRF')		 		THEN 'VENTA CORTA'
			WHEN Prestamo."IdTipoPrestamo" IN ('PRTADM','PRADRF')				THEN 'ADMINISTRATIVO'
			WHEN Prestamo."IdTipoPrestamo" IN ('PRTFIN','PRFGRF')				THEN 'FINES GENERALES'
			ELSE																	'??'
		END																					AS "TipoPrestamo"
		,ConsultarCliente."NombreSegmentoCliente"											AS "TipoPrestatario"
		,CASE
           	WHEN  ConsultarCliente."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad" THEN 'SI' 
         	ELSE 'NO' 
        END                              						 							AS "EsCuentaPropia"
        ,CASE  
			WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'N' THEN 'NO' 
			ELSE 'SI' 
		END																					AS "EsRelacionado"

FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" Prestamo
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente ON ConsultarCliente."IdCliente" = Prestamo."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa ON ConsultarEmpresa."IdEmpresa" = Prestamo."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio ON ConsultarPortafolio."IdPortafolio" = Prestamo."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = Prestamo."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" 			ConsultarIndicadoresCliente ON ConsultarIndicadoresCliente."IdCliente" =  Prestamo."IdCliente" AND ((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR (ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
WHERE
    	Prestamo."IdEmpresa"						= :IdEmpresa		AND 
    	Prestamo."FechaLiquidacion"					= :FechaContable 	AND
    	Prestamo."IdClasificacionTipoInstrumento"	= :IdClasificacionTipoInstrumento
GROUP BY
		 Prestamo."FolioPrestamo"
		,Prestamo."DocumentoIdentidad"
		,ConsultarCliente."NombreCompleto"
		,Prestamo."CodigoPortafolio"
		,Prestamo."IdMonedaInstrumento"
		,Prestamo."IdTipoProducto"
		,Prestamo."IdClasificacionTipoInstrumento"
		,Prestamo."IdTipoInstrumento"
		,Prestamo."NemoSubyacente"
		,Prestamo."IdBoveda"
		,Prestamo."IdCartera"
		,Prestamo."IdEmisor"
		,Prestamo."IdTipoReajuste"
		,Prestamo."CodigoIFRS"
		,Prestamo."IdEmisor"
		,Prestamo."IdClasificacionTipoInstrumento"
	 	,Prestamo."IdTipoInstrumento"
	 	,Prestamo."IdTipoReajuste"
		,ConsultarEjecutivo."CodigoEmpleado"
		,ConsultarEjecutivo."NombreCompleto"
		,ConsultarEjecutivo."CodigoSucursal"
		,ConsultarEjecutivo."NombreSucursal"
		,ConsultarIndicadoresEmpleado."ValorIndicador"
		,ConsultarIndicadoresCliente."ValorIndicador"
		,Prestamo."IdBolsa"
		,Prestamo."FechaLiquidacion"
		,ConsultarCliente."Tipo"
		,Prestamo."FechaPrestamo"
		,Prestamo."IdConcepto"
		,Prestamo."IdCondicionLiquidacion"
		,Prestamo."IdTipoPrestamo"
		,ConsultarCliente."NombreSegmentoCliente"
		,ConsultarEmpresa."DocumentoIdentidad"
		,ConsultarCliente."DocumentoIdentidad";