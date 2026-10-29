	SELECT
			 ConsultarCartera."IdCartera" 								AS "IdMovimiento"		 
			,LEFT(ConsultarCartera."DocumentoIdentidad",60)				AS "DocumentoIdentidad"                                    
			,LEFT(ConsultarCliente."NombreCompleto",60)					AS "NombreCliente"                                       
			,LEFT(ConsultarCartera."CodigoPortafolio",60)				AS "Portafolio"
			,ConsultarCartera."IdMonedaInstrumento"						AS "Moneda"                                                      
			,'EsRelacionado' 											AS "EsRelacionado"
			,CASE ConsultarCartera."DocumentoIdentidad" 
					WHEN ConsultarEmpresa."DocumentoIdentidad" 
					THEN 'S' ELSE 'N' END								AS "EsCuentaPropia" 		                                           
			,ConsultarCartera."IdTipoProducto"							AS "Producto"
			,ConsultarCartera."IdClasificacionTipoInstrumento" 			AS "ClaseInstrumento"
			,ConsultarCartera."IdTipoInstrumento" 						AS "TipoInstrumento"
			,ConsultarCartera."Nemotecnico"								AS "Nemo"
			,ConsultarCartera."IdBoveda"								AS "Boveda"	
			,CAST(ConsultarCartera."IdCartera" AS NVARCHAR(60))			AS "IdCartera"		
			,ConsultarCartera."IdEmisor"								AS "Emisor"
			,ConsultarCartera."IdTipoReajuste"							AS "TipoReajuste"
			,ConsultarCartera."CodigoIFRS"								AS "CodigoIFRS"
			,'CUSTODIA'													AS "TipoCartera"
			,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
			(ConsultarCartera."IdEmisor", ConsultarCartera."IdClasificacionTipoInstrumento", 
	 		 ConsultarCartera."IdTipoInstrumento", ConsultarCartera."Nemotecnico", ConsultarCartera."IdTipoReajuste")
		 																AS "ClaseEmisor"
			,ConsultarEjecutivo."CodigoEmpleado" 		    			AS "Agente"
			,LEFT(ConsultarEjecutivo."NombreCompleto",60) 				AS "NombreAgente"
			,ConsultarEjecutivo."CodigoSucursal" 						AS "SucursalAgente"
			,LEFT(ConsultarEjecutivo."NombreSucursal",60) 				AS "NombreSucursalAgente"
			,LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60)		AS "CentroCostoAgente"
			,CASE WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
								THEN 'CP'
				  	WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 	
				  				THEN 'RE'
				  	ELSE	'NR'
				  END 													AS "Relacion"
			,ConsultarCartera."IdBolsa"									AS "Bolsa"
			,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaLiquidacion"
										ELSE ConsultarCartera."EGRFechaLiquidacion" END
																		AS "FechaLiquidacion"
			,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaOperacion"
										ELSE ConsultarCartera."EGRFechaOperacion" END
																		AS "FechaOperacion"
			,ConsultarCartera."IdConceptoGrilla"						AS "Concepto"
			,ConsultarCliente."Tipo"									AS "TipoPersona"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
				ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
				ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
		LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
				ON ConsultarPortafolio."IdPortafolio" = ConsultarCartera."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
				ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
				ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
				ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarCartera."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
		WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa 
			AND (
	        	("INGFechaOperacion" <= :FechaContable AND "EGRCantidad" = 0) OR("INGFechaOperacion" <= :FechaContable AND "EGRCantidad" <> 0 AND "INGFechaOperacion" > :FechaContable)
	        	)
			AND ConsultarCartera."IdTipoOperacion"				IN ('TDEPLA','TAUTDI')
	       	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	= 'IF' 	        
--	        AND ConsultarCartera."DocumentoIdentidad" 				= :DocumentoIdentidad
	        AND ConsultarCartera."IdEstado"							= 'VIG'
	        AND ConsultarCartera."INGMontoInteresCompraAnterior" 	<> 0;