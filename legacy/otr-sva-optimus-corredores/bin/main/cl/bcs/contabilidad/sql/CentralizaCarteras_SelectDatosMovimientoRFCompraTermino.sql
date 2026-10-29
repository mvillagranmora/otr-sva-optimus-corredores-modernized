		SELECT
			 ConsultarCartera."IdCartera" 							AS "IdMovimiento"
			,LEFT(ConsultarCartera."DocumentoIdentidad",60)			AS "DocumentoIdentidad"                                    
			,LEFT(ConsultarCliente."NombreCompleto",60)				AS "NombreCliente"                                       
			,LEFT(ConsultarCartera."CodigoPortafolio",60)			AS "Portafolio"
			,ConsultarCartera."IdMonedaInstrumento"					AS "Moneda"                                                      
			,ConsultarCartera."IdTipoProducto"						AS "Producto"
			,ConsultarCartera."IdClasificacionTipoInstrumento" 		AS "ClaseInstrumento"
			,ConsultarCartera."IdTipoInstrumento" 					AS "TipoInstrumento"
			,ConsultarCartera."IdTipoOperacion" 					AS "TipoOperacion"
			,ConsultarCartera."Nemotecnico"							AS "Nemo"
			,ConsultarCartera."IdBoveda"							AS "Boveda"	
			,COALESCE(ConsultarCartera."IdCartera",0)				AS "IdCartera"		
			,ConsultarCartera."IdEmisor"							AS "Emisor"
			,ConsultarCartera."IdTipoReajuste"						AS "TipoReajuste"
			,ConsultarCartera."CodigoIFRS"							AS "CodigoIFRS"
			,'CUSTODIA'															AS "TipoCartera"
			,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
			(ConsultarCartera."IdEmisor", ConsultarCartera."IdClasificacionTipoInstrumento", 
	 		 ConsultarCartera."IdTipoInstrumento", ConsultarCartera."Nemotecnico", ConsultarCartera."IdTipoReajuste")
		 																		AS "ClaseEmisor"
			,ConsultarEjecutivo."CodigoEmpleado" 		    		AS "Agente"
			,LEFT(ConsultarEjecutivo."NombreCompleto",60) 			AS "NombreAgente"
			,ConsultarEjecutivo."CodigoSucursal" 					AS "SucursalAgente"
			,LEFT(ConsultarEjecutivo."NombreSucursal",60) 			AS "NombreSucursalAgente"
			,LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60)	AS "CentroCostoAgente"
			
			,CASE WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
								THEN 'CP'
				  	WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 	
				  				THEN 'RE'
				  	ELSE	'NR'
				  END 												AS "Relacion"
			,ConsultarCartera."IdBolsa"								AS "Bolsa"
			,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaLiquidacion"
										ELSE ConsultarCartera."EGRFechaLiquidacion" END
																	AS "FechaLiquidacion"
			,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaOperacion"
										ELSE ConsultarCartera."EGRFechaOperacion" END
																	AS "FechaOperacion"
			,ConsultarCartera."IdConceptoGrilla"					AS "Concepto"
			,ConsultarCliente."Tipo"								AS "TipoPersona"
			,DocInd."DocumentoIdentidad" 							AS "DocumentoIdentidadCAVALI"
			,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
						 	 ConsultarCartera."IdClasificacionTipoInstrumento"
							,ConsultarCartera."IdTipoInstrumento"
							,ConsultarCartera."Nemotecnico"
							,ConsultarCartera."IdEmisor")											AS "TipoEmisor"					
			,COALESCE(MES."Codigo",'')	AS "Mesa"
			,COALESCE(OPE."FolioOperacion",TO_BIGINT(0)) AS  "FolioOperacion"
			,COALESCE(IndCli2."NombreValorIndicador",'NO BANCARIO') AS "ClienteBancario" 
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 					AS ConsultarCartera

		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			AS ConsultarCliente
				ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"	AS SoCNeg
		ON (ConsultarCartera."IdCliente" = SoCNeg."IdCliente")
		
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 			AS DocInd 
		ON DocInd."IdPersona" 					= SoCNeg."IdPersona"   
		AND DocInd."IdTipoDocumentoIdentidad" 	= 'RUT'
				
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			AS ConsultarEmpresa
				ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
		LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 			AS ConsultarPortafolio 
				ON ConsultarPortafolio."IdPortafolio" = ConsultarCartera."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" 		AS ConsultarEjecutivo
				ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" AS ConsultarIndicadoresEmpleado
				ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS ConsultarIndicadoresCliente
				ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarCartera."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS IndCli2
				ON IndCli2."IdCliente" =  ConsultarCartera."IdCliente" AND IndCli2."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
		LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON ConsultarCartera."IdMesa" = MES."IdMesa"
		LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE ON OPE."IdOperacion" = ConsultarCartera."IdOperacion"
		WHERE ConsultarCartera."IdEmpresa"				= :IdEmpresa
			AND ConsultarCartera."IdTipoProducto" 		= 'MERF'
			AND ConsultarCartera."IdTipoOperacion" 		= 'COMTER'
			AND ConsultarCartera."INGFechaOperacion" 	= :FechaContable 
			AND ConsultarCartera."IdEstado"				!= 'ANU'
	        AND ConsultarCartera."DocumentoIdentidad" 	= :DocumentoIdentidad;
	        
