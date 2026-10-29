SELECT DISTINCT
			 ConsultarOperacionSpot."IdOperacionUI" 						AS "IdMovimiento"		 
			,LEFT(ConsultarOperacionSpot."RutCliente",60)				AS "DocumentoIdentidad"
			,LEFT(ConsultarOperacionSpot."NombreCliente",60)			AS "NombreCliente"                                       
			,LEFT(ConsultarOperacionSpot."CodigoPropositoPortafolio",60) AS "Portafolio"
			,ConsultarOperacionSpot."IdMonedaPrincipal"					AS "Moneda"                                                                                     
			,ConsultarOperacionSpot."IdTipoOperacion"					AS "Producto"
			,'SP'														AS "ClaseInstrumento"
			,ConsultarOperacionSpot."IdTipoInstrumento" 				AS "TipoInstrumento"
			,ConsultarOperacionSpot."NombreInstrumento"					AS "Nemo"
			,'Boveda'													AS "Boveda"
			,COALESCE(ConsultarOperacionSpot."IdOperacion",0)				AS "IdCartera"
			,'Emisor'													AS "Emisor"
			,ConsultarOperacionSpot."IdMonedaPrincipal"					AS "TipoReajuste"
			,''															AS "CodigoIFRS"
			,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'') 		    AS "Agente"
			,LEFT(IFNULL(ConsultarEjecutivo."NombreCompleto",''),60) 	AS "NombreAgente"
			,IFNULL(ConsultarEjecutivo."CodigoSucursal",'')				AS "SucursalAgente"
			,LEFT(IFNULL(ConsultarEjecutivo."NombreSucursal",''),60) 	AS "NombreSucursalAgente"
			,IFNULL(ConsultarOperacionSpot."IdTipoCanalIngreso",'')		AS "Canal"
			,CASE WHEN ConsultarOperacionSpot."RutCliente" = ConsultarEmpresa."DocumentoIdentidad"
								THEN 'CP'
				  WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 	
				  				THEN 'RE'
				  ELSE	'NR'
					END 												AS "Relacion"
			,''															AS "Bolsa"
			,LEFT(INDEMP."ValorIndicador",60)							AS "CentroCostoAgente"
			,ConsultarOperacionSpot."TipoComprobanteFacturacion"		AS "TipoComprobante"
			,LEFT(ConsultarOperacionSpot."Accion",1)					AS "CompraVenta"
			,ConsultarCliente."Tipo"									AS "TipoPersona"
			,LEFT(ConsultarOperacionSpot."FechaOperacion",10)			AS "FechaOperacion"
			,COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') AS "ClienteBancario"
			,ConsultarOperacionSpot."OperacionFicticia"                 AS "OperacionNoFacturable"
		FROM  "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" ConsultarOperacionSpot
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
				ON ConsultarCliente."IdCliente" = ConsultarOperacionSpot."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
				ON ConsultarEmpresa."IdEmpresa" = ConsultarOperacionSpot."IdEmpresa"
		LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
				ON ConsultarPortafolio."IdPortafolio" = ConsultarOperacionSpot."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
				--ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarOperacionSpot."IdAgente"
				ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarOperacionSpot."Agente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
				ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarOperacionSpot."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"    AS IndClienteBancario	
				ON IndClienteBancario."IdCliente" =  ConsultarOperacionSpot."IdCliente" AND IndClienteBancario."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" AS INDEMP
			ON INDEMP."IdEmpleado" =  ConsultarPortafolio."IdEmpleadoEjecutivo" AND INDEMP."CodigoTipoIndicador"  = 'CENCOSTO'
		WHERE ConsultarOperacionSpot."IdEmpresa" = :IdEmpresa 
		AND LEFT(ConsultarOperacionSpot."FechaOperacion",10) = :FechaContable		
        AND ConsultarOperacionSpot."Estado" = 'VIG'
