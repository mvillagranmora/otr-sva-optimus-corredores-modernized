		SELECT
			 	ConsultarGarantias."IdGarantia" 						AS "IdMovimiento"		 
				,LEFT(ConsultarGarantias."DocumentoIdentidad",60)		AS "DocumentoIdentidad"                                    
				,LEFT(ConsultarCliente."NombreCompleto",60)				AS "NombreCliente"                                       
				,LEFT(ConsultarGarantias."CodigoPortafolio",60)			AS "Portafolio"
			    --,'EsRelacionado' 										AS "EsRelacionado"
				,ConsultarGarantias."IdTipoProductoOperacionOrigen"		AS "Producto"
				,ConsultarGarantias."IdClasificacionTipoInstrumento" 	AS "ClaseInstrumento"
				,ConsultarGarantias."IdTipoInstrumento" 				AS "TipoInstrumento"
				,ConsultarGarantias."IdTipoInstrumentoGarantizado" 		AS "IdTipoInstrumentoGarantizado"
				,ConsultarGarantias."IdOrigenGarantia"					AS "IdOrigenGarantia"
				,ConsultarGarantias."Nemotecnico"						AS "Nemo"
				,ConsultarGarantias."FolioGarantia"						AS "Folio"
				,ConsultarCliente."DocumentoIdentidadAdicional"         AS "DocumentoIdentidadCAVALI"
				,ConsultarGarantias."IdBovedaOperacionOrigen"			AS "Boveda"
				,COALESCE(ConsultarGarantias."IdGarantia",0)			AS "IdGarantia"
				,ConsultarGarantias."IdEmisor"							AS "Emisor"
				,ConsultarGarantias."IdTipoReajuste"					AS "TipoReajuste"
				,ConsultarGarantias."CodigoIFRS"						AS "CodigoIFRS"
				,'GARANTIA'												AS "TipoCartera"
				,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
				(ConsultarGarantias."IdEmisor", ConsultarGarantias."IdClasificacionTipoInstrumento", 
		 		 ConsultarGarantias."IdTipoInstrumento", ConsultarGarantias."Nemotecnico", ConsultarGarantias."IdTipoReajuste")
			 															AS "ClaseEmisor"
				,ConsultarEjecutivo."CodigoEmpleado" 		    		AS "Agente"
				,LEFT(ConsultarEjecutivo."NombreCompleto",60) 			AS "NombreAgente"
				,ConsultarEjecutivo."CodigoSucursal" 					AS "SucursalAgente"
				,LEFT(ConsultarEjecutivo."NombreSucursal",60) 			AS "NombreSucursalAgente"
				,LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60)	AS "CentroCostoAgente"
				
				--new obtener y agregar nuevos parametros
				,ConsultarGarantias."IdConcepto"						AS "Concepto"
				,ConsultarGarantias."NombreConcepto"					AS "NombreConcepto"
--				,ConsultarGarantias."IdTipoOperacionOperacionOrigen"	AS "TipoMovimiento"
				,CASE WHEN ConsultarGarantias."EGRCantidad" = 0 
					THEN 'INGRESOGTA'
					ELSE 'EGRESOGTA' 
				END														AS "TipoMovimiento"					
					
					
			    ,ConsultarGarantias."IdMonedaInstrumento"				AS "Moneda"
				,ConsultarGarantias."FechaValorizacionGarantia"		AS "FechaActual"
				--,'650'												AS "TasaCambio"
				,COALESCE(ConsultarGarantias."ValorTipoCambioCierre",0)	AS "TasaCambio"
				,CASE WHEN ConsultarGarantias."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
								THEN 'CP'
				  	WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 	
				  				THEN 'RE'
				  	ELSE	'NR'
				  END 													AS "Relacion"
				,ConsultarGarantias."IdBolsaOperacionOrigen"			AS "Bolsa"
				,CASE WHEN ConsultarGarantias."EGRCantidad" = 0 THEN ConsultarGarantias."INGFechaLiquidacion"
										ELSE ConsultarGarantias."EGRFechaLiquidacion" END
																		AS "FechaLiquidacion"
				,ConsultarCliente."Tipo"								AS "TipoPersona"
				,CASE WHEN ConsultarGarantias."EGRCantidad" = 0 THEN ConsultarGarantias."INGFechaOperacion"
										ELSE ConsultarGarantias."EGRFechaOperacion" END
																		AS "FechaOperacion"
		
		FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" ConsultarGarantias
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
				ON ConsultarCliente."IdCliente" = ConsultarGarantias."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
				ON ConsultarEmpresa."IdEmpresa" = ConsultarGarantias."IdEmpresa"
		LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
				ON ConsultarPortafolio."IdPortafolio" = ConsultarGarantias."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
				ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarGarantias."IdAgente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
				ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
				ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarGarantias."IdCliente" AND 
				
--				ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
				((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR 
	(ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
				
		WHERE ConsultarGarantias."IdEmpresa" = :IdEmpresa
			AND	(("INGFechaOperacion" = :FechaContable  AND "EGRCantidad" = 0) OR ("EGRFechaOperacion" = :FechaContable  AND "EGRCantidad" <> 0))
	        AND ConsultarGarantias."DocumentoIdentidad" = :DocumentoIdentidad;
	        
			
