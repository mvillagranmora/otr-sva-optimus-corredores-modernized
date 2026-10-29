		SELECT
			TO_BIGINT(:IdTipoCentralizacion)                    		AS "IdTipoCentralizacion"
			,ConsultarCartera."IdCartera" 								AS "IdMovimiento"		 
			,LEFT(ConsultarCartera."DocumentoIdentidad",60)				AS "DocumentoIdentidad"                                    
			,LEFT(ConsultarCliente."NombreCompleto",60)					AS "NombreCliente"                                       
			,LEFT(ConsultarCartera."CodigoPortafolio",60)				AS "Portafolio"
			,ConsultarCartera."IdMonedaInstrumento"						AS "Moneda"                                                      
			,CASE 
			    WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
			                THEN 'CP'
			    WHEN IndCli."ValorIndicador" = 'S'  
			                THEN 'RE'
			    ELSE    'NR'
			  END                                                  		AS "Relacion"
			,ConsultarCartera."IdTipoProducto"							AS "Producto"
			,ConsultarCartera."IdClasificacionTipoInstrumento" 			AS "ClaseInstrumento"
			,ConsultarCartera."IdTipoInstrumento" 						AS "TipoInstrumento"
			,ConsultarCartera."NemotecnicoPrestamo"						AS "Nemo"
			,ConsultarCartera."IdBoveda"								AS "Boveda"	
			,CAST(ConsultarCartera."IdCartera" AS NVARCHAR(60))			AS "IdCartera"		
			,ConsultarCartera."IdEmisor"								AS "Emisor"
			,ConsultarCartera."IdTipoReajuste"							AS "TipoReajuste"
			,ConsultarCartera."CodigoIFRS"								AS "CodigoIFRS"
			,'CUSTODIA'													AS "TipoCartera"
			,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
			(ConsultarCartera."IdEmisor", ConsultarCartera."IdClasificacionTipoInstrumento", 
	 		 ConsultarCartera."IdTipoInstrumento", ConsultarCartera."NemotecnicoPrestamo", ConsultarCartera."IdTipoReajuste")
		 																AS "ClaseEmisor"
			,ifnull(ConsultarEjecutivo."CodigoEmpleado",'')    			AS "Agente"
			,LEFT(ifnull(ConsultarEjecutivo."NombreCompleto",''),60) 	AS "NombreAgente"
			,ifnull(ConsultarEjecutivo."CodigoSucursal",'')				AS "SucursalAgente"
			,LEFT(ifnull(ConsultarEjecutivo."NombreSucursal",''),60) 	AS "NombreSucursalAgente"
			,LEFT(ifnull(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)		AS "CentroCostoAgente"
			,ConsultarCartera."INGFechaValorizacionAnterior"            AS "FechaAnterior"
			,ConsultarCartera."INGFechaValorizacionActual"              AS "FechaActual"
			,ConsultarCartera."ValorTipoCambioCierre"                   AS "TipoCambio"
			,ConsultarCartera."INGPrecioMercadoAnterior"                AS "PrecioMercadoAnterior"
			,ConsultarCartera."INGPrecioMercadoInstrumento"             AS "PrecioMercadoActual"
			,ConsultarCliente."Tipo"									AS "TipoPersona"
			,CASE WHEN ConsultarCartera."EGRCantidadPrestamo" = 0 THEN ConsultarCartera."INGFechaLiquidacionPrestamo"
										ELSE ConsultarCartera."EGRFechaLiquidacionPrestamo" END
																		AS "FechaLiquidacion"
			,CASE WHEN ConsultarCartera."EGRCantidadPrestamo" = 0 THEN ConsultarCartera."INGFechaOperacion"
										ELSE ConsultarCartera."EGRFechaOperacion" END
																		AS "FechaOperacion"
				
			
		FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" ConsultarCartera
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

		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
		ON IndCli."IdCliente" =  ConsultarCartera."IdCliente" AND --IndCli."CodigoTipoIndicador"  = 'RELCORR'
		((IndCli."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR 
		(IndCli."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
				
				
		WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa
			AND "INGFechaOperacion" <= :FechaContable 
			AND "EGRCantidadPrestamo" = 0
	        AND ConsultarCartera."IdClasificacionTipoInstrumento" = 'RV'
	        AND ConsultarCartera."IdTipoInstrumento" IN ('PRT', 'EMP')
--	        AND ConsultarCartera."DocumentoIdentidad" = :DocumentoIdentidad
	        AND ConsultarCartera."IdEstado" = 'VIG'
	        
