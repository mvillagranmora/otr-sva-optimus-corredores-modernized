	SELECT
		DISTINCT
		 ConsultarCartera."IdCartera" 																				AS "IdMovimiento"		 
		,LEFT(ConsultarCartera."DocumentoIdentidad",60)																AS "DocumentoIdentidad"                                    
		,LEFT(ConsultarCliente."NombreCompleto",60)																	AS "NombreCliente"                                       
		,LEFT(ConsultarCartera."CodigoPortafolio",60)																AS "Portafolio"
		,ConsultarCartera."IdMonedaInstrumento"																		AS "Moneda"                                                      
		,CASE 
		    WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
		                THEN 'CP'
		    WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S'  
		                THEN 'RE'
		    ELSE    'NR'
		  END                                                  														AS "Relacion"
		,ConsultarCartera."IdTipoProducto"																			AS "Producto"
		,ConsultarCartera."IdClasificacionTipoInstrumento" 															AS "ClaseInstrumento"
		,ConsultarCartera."IdTipoInstrumento" 																		AS "TipoInstrumento"
		,ConsultarCarteraSubyacenteBasica."Nemotecnico"																AS "Nemo"
		,ConsultarCartera."IdBoveda"																				AS "Boveda"	
		,CAST(ConsultarCartera."IdCartera" AS NVARCHAR(60))															AS "IdCartera"		
		,ConsultarCartera."IdEmisor"																				AS "Emisor"
		,ConsultarCartera."IdTipoReajuste"																			AS "TipoReajuste"
		,ConsultarCartera."CodigoIFRS"																				AS "CodigoIFRS"
		,'CUSTODIA'																									AS "TipoCartera"
		,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
		(ConsultarCartera."IdEmisor", ConsultarCartera."IdClasificacionTipoInstrumento", 
 		 ConsultarCartera."IdTipoInstrumento", ConsultarCartera."Nemotecnico", ConsultarCartera."IdTipoReajuste")
	 																												AS "ClaseEmisor"
		,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'')    															AS "Agente"
		,LEFT(IFNULL(ConsultarEjecutivo."NombreCompleto",''),60) 													AS "NombreAgente"
		,IFNULL(ConsultarEjecutivo."CodigoSucursal",'') 															AS "SucursalAgente"
		,LEFT(IFNULL(ConsultarEjecutivo."NombreSucursal",''),60) 													AS "NombreSucursalAgente"
		,LEFT(IFNULL(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)											AS "CentroCostoAgente"
		,CASE WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
				THEN 'CP'
			  WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 	
			  	THEN 'RE'
			  ELSE	'NR'
			  END 											AS "Relacion"
		,ConsultarCartera."IdBolsa"							AS "Bolsa"
		,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaLiquidacion"
									ELSE ConsultarCartera."EGRFechaLiquidacion" END
																													AS "FechaLiquidacion"
		,ConsultarCliente."Tipo"																					AS "TipoPersona"
		,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaOperacion"
									ELSE ConsultarCartera."EGRFechaOperacion" END
																													AS "FechaOperacion"
		,ConsultarCartera."IdConcepto"																				AS "IdConcepto"
		,CASE ConsultarCartera."DocumentoIdentidad" 
            		WHEN ConsultarEmpresa."DocumentoIdentidad" 
            		THEN 'SI' ELSE 'NO' END                              						 					AS "EsCuentaPropia"
		,COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') 											AS "ClienteBancario" 
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
	LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" ConsultarCarteraSubyacenteBasica
			ON ConsultarCarteraSubyacenteBasica."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
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
			ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarCartera."IdCliente" AND --ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
			((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR 
			(ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"   AS IndClienteBancario
					ON IndClienteBancario."IdCliente" =  ConsultarCartera."IdCliente" AND IndClienteBancario."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
	
	WHERE ConsultarCartera."IdEmpresa" 				= :IdEmpresa 
		AND ConsultarCartera."EGRFechaOperacion" 	= :FechaContable
		AND ConsultarCartera."EGRIdConcepto"		IN ('VENPRT','VENEMP','ANTPRT','ANTEMP')
        AND ConsultarCartera."IdClasificacionTipoInstrumento" 	= 'RV' 
        AND ConsultarCartera."IdTipoInstrumento" 	IN ('PRT','EMP');
        