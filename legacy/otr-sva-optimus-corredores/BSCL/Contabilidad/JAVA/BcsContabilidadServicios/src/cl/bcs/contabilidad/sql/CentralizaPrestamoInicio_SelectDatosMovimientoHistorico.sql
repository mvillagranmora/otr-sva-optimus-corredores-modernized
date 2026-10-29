SELECT		
		Prestamo."FolioCartera" 															AS "IdMovimiento"		 
		,LEFT(ConsultarCliente."DocumentoIdentidad",60)										AS "DocumentoIdentidad"                                    
		,LEFT(ConsultarCliente."NombreCompleto",60)											AS "NombreCliente"                                       
		,LEFT(ConsultarPortafolio."CodigoPortafolio",60)									AS "Portafolio"
		,LEFT(ConsultarCliente."DocumentoIdentidad",60)										AS "DocumentoIdentidad"                                    
		,LEFT(ConsultarCliente."NombreCompleto",60)											AS "NombreCliente"                                       
		,LEFT(ConsultarPortafolio."CodigoPortafolio",60)									AS "Portafolio"
		,Prestamo."IdMonedaInstrumento"														AS "Moneda"                                                      
		,Prestamo."IdTipoProducto"															AS "Producto"
		,Prestamo."IdClasificacionTipoInstrumento" 											AS "ClaseInstrumento"
		,Prestamo."IdTipoInstrumento" 														AS "TipoInstrumento"
		,Subyacente."Nemotecnico"															AS "Nemo"
		,Prestamo."IdBoveda"																AS "Boveda"	
		,CAST(Prestamo."IdCartera" AS NVARCHAR(60))											AS "IdCartera"		
		,Prestamo."IdEmisor"																AS "Emisor"
		,Prestamo."IdTipoReajuste"															AS "TipoReajuste"
		,Prestamo."CodigoIFRS"																AS "CodigoIFRS"
		,'CUSTODIA'																			AS "TipoCartera"
		,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
			(Prestamo."IdEmisor", Prestamo."IdClasificacionTipoInstrumento", 
	 	 	Prestamo."IdTipoInstrumento", Subyacente."Nemotecnico", Prestamo."IdTipoReajuste")
		 																					AS "ClaseEmisor"
		,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'')    									AS "Agente"
		,LEFT(IFNULL(ConsultarEjecutivo."NombreCompleto",''),60) 							AS "NombreAgente"
		,IFNULL(ConsultarEjecutivo."CodigoSucursal",'') 									AS "SucursalAgente"
		,LEFT(IFNULL(ConsultarEjecutivo."NombreSucursal",''),60) 							AS "NombreSucursalAgente"
		,LEFT(IFNULL(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)					AS "CentroCostoAgente"
		,CASE 
			WHEN ConsultarCliente."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"	THEN 'CP'
			WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 		  					THEN 'RE'
			ELSE																				'NR'
		END 																				AS "Relacion"
		,Prestamo."IdBolsa"																	AS "Bolsa"
		,CAST(Prestamo."FechaLiquidacion" AS NVARCHAR(10))									AS "FechaLiquidacion"
		,ConsultarCliente."Tipo"															AS "TipoPersona"
		,CAST(Prestamo."FechaOperacion" AS NVARCHAR(10))									AS "FechaOperacion"
		,Prestamo."IdConcepto"																AS "IdConcepto"
		,Prestamo."IdCondicionLiquidacion"													AS "CondicionLiquidacion"
		,CASE 
			WHEN TipoPrestamo."IdTipoPrestamo" IN ('PRTVTC','PRVCRF')		 		THEN 'VENTA CORTA'
			WHEN TipoPrestamo."IdTipoPrestamo" IN ('PRTADM','PRADRF')				THEN 'ADMINISTRATIVO'
			WHEN TipoPrestamo."IdTipoPrestamo" IN ('PRTFIN','PRFGRF')				THEN 'FINES GENERALES'
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
			END																				AS "EsRelacionado"
FROM "BSCL.Custodia.BD::HIS.Cartera" Prestamo
LEFT JOIN "BSCL.Custodia.BD::HIS.Subyacente" Subyacente ON Subyacente."FolioCarteraRelacionada" = Prestamo."FolioCartera"
LEFT JOIN "BSCL.Custodia.BD::PRT.TipoPrestamo" TipoPrestamo ON TipoPrestamo."IdInstrumentoPrestamo" = Prestamo."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente ON ConsultarCliente."IdCliente" = Prestamo."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa ON ConsultarEmpresa."IdEmpresa" = Prestamo."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio ON ConsultarPortafolio."IdPortafolio" = Prestamo."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = Prestamo."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" 			ConsultarIndicadoresCliente ON ConsultarIndicadoresCliente."IdCliente" =  Prestamo."IdCliente" AND ((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR (ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
WHERE
    	Prestamo."IdEmpresa"						= :IdEmpresa				AND 
    	Prestamo."FechaLiquidacion"					= :FechaContable 			AND
    	Prestamo."FechaRespaldo"					= :FechaContable			AND
    	Subyacente."FechaRespaldo"					= :FechaContable			AND
    	Prestamo."IdEstado"							= 'VIG'						AND
    	Prestamo."IdTipoInstrumento"				IN ('PRT','PRF')			AND
    	Prestamo."IdClasificacionTipoInstrumento"	= :IdClasificacionTipoInstrumento
GROUP BY
		 Prestamo."FolioCartera"
		,ConsultarCliente."DocumentoIdentidad"
		,ConsultarCliente."NombreCompleto"
		,ConsultarPortafolio."CodigoPortafolio"
		,Prestamo."IdMonedaInstrumento"
		,Prestamo."IdTipoProducto"
		,Prestamo."IdClasificacionTipoInstrumento"
		,Prestamo."IdTipoInstrumento"
		,Subyacente."Nemotecnico"
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
		,Prestamo."FechaOperacion"
		,Prestamo."IdConcepto"
		,Prestamo."IdCondicionLiquidacion"
		,TipoPrestamo."IdTipoPrestamo"
		,ConsultarCliente."NombreSegmentoCliente"
		,ConsultarEmpresa."DocumentoIdentidad"
		,ConsultarCliente."DocumentoIdentidad";