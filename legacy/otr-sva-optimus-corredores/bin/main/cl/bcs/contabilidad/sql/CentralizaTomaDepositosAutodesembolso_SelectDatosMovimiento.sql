		SELECT DISTINCT
			 ConsultarCartera."IdCartera" 							AS "IdMovimiento"		 
			,LEFT(ConsultarCartera."DocumentoIdentidad",60)			AS "DocumentoIdentidad"
			,LEFT(ConsultarCartera."CodigoPortafolio",60)			AS "Portafolio"
			,LEFT(IFNULL(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)	AS "CentroCosto"
			,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'')    		AS "CodigoAgente"
			,ConsultarCartera."IdBolsa"								AS "CodigoBolsa"
			,ConsultarCartera."CodigoIFRS"							AS "CodigoIFRS"
			,ConsultarCartera."IdClasificacionTipoInstrumento"		AS "TipoInstrumento"
			,IFNULL(ConsultarEjecutivo."CodigoSucursal",'') 		AS "CodigoSucursal"
			,ConsultarCartera."IdTipoOperacion"						AS "CodigoTipoOperacion"
			,ConsultarCartera."IdSecuenciaCarteraMigracion"			AS "CodSecuencia"
			,CASE ConsultarCartera."DocumentoIdentidad"
					WHEN ConsultarEmpresa."DocumentoIdentidad" 
					THEN 'S' ELSE 'N' END							AS "CuentaPropia"
			,CASE ConsultarCartera."DocumentoIdentidad" 
					WHEN ConsultarEmpresa."DocumentoIdentidad" 
					THEN 'S' ELSE 'N' END							AS "DiasLiquidacion"
			, CASE 	WHEN	ConsultarCartera."EGRIdCondicionLiquidacion"	='T0' THEN '0'
					WHEN	ConsultarCartera."EGRIdCondicionLiquidacion"	='T1' THEN '1'
					WHEN	ConsultarCartera."EGRIdCondicionLiquidacion"	='T2' THEN '2'
					ELSE	''	END									AS "DiasVencido"	
			,DAYS_BETWEEN(ConsultarCartera."FechaLiquidacion",ConsultarCartera."FechaVencimientoInstrumento") 
																	AS "DiasVencimiento" 
			,ConsultarCartera."IdEmisor"							AS "EmisorInstrumento"
			,''														AS "EstadoMovimiento"
			,ConsultarCartera."IdTipoInstrumento"					AS "FamiliaInstrumento"
			,ConsultarCartera."EGRFechaComprobante"					AS "FechaEmisionDocumento"
			,ConsultarCartera."EGRFechaLiquidacion"					AS "FechaLiquidacionDocumento"
			, CASE 	WHEN	ConsultarCartera."EGRIdCondicionLiquidacion"	='T0' THEN 'PH'
					WHEN	ConsultarCartera."EGRIdCondicionLiquidacion"	='T1' THEN 'PM'
					WHEN	ConsultarCartera."EGRIdCondicionLiquidacion"	='T2' THEN 'CN'
					ELSE	''	END								AS "IndicadorLiquidacion"
			,''													AS "IndicadorSimultanea" --?
			,ConsultarCartera."Nemotecnico"						AS "Nemo"
			,ConsultarCartera."IdTipoProducto"					AS "ProductoMovimiento"
			
			,CASE	ConsultarCartera."DocumentoIdentidad" 		 
					WHEN ConsultarEmpresa."DocumentoIdentidad" 
			 		THEN 'S' ELSE 'N' END 						AS "RelacionadoEmpresa"
			,CASE	ConsultarCartera."DocumentoIdentidad"		 
					WHEN ConsultarEmpresa."DocumentoIdentidad" 
			 		THEN 'S' ELSE 'N' END 						AS "RelacionCorredora"
			,ConsultarCartera."IdEmisor"						AS "TipoEmisor"
			,ConsultarCartera."IdTipoInstrumento"				AS "FamiliaTipoInstrumento"----			
			,CASE 
					 WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"					 
						THEN 'CP'					
					WHEN UPPER(ConsultarIndicadoresCliente."CodigoValorIndicador") = 'VINCULADO' 	
						THEN 'RE'					
				  	ELSE 'NR'		
			 END							AS "Relacion"
				  	
				  	
			, "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor"
				(ConsultarCartera."IdEmisor", ConsultarCartera."IdClasificacionTipoInstrumento", 
	 			 ConsultarCartera."IdTipoInstrumento", ConsultarCartera."Nemotecnico", ConsultarCartera."IdTipoReajuste")
	 		 													AS "ClaseEmisor"
			,/*CASE WHEN ConsultarCartera."IdTipoInstrumento" IN ('BBC','BEM','BH','LTH','BCD','BCP','BCU','BTP','BTU','PAG','PRC','PCX','PRD') THEN 'Libre'	
				  WHEN ConsultarCartera."IdTipoInstrumento" = 'PAC' THEN 'VENDIDA CON PACTO'
				  ELSE '?' END
				  --emprestito	no hay para rf
				  	*/''											AS "TipoCartera"				
			,CASE 	WHEN ConsultarCartera."IdTipoInstrumento" IN ('BBC')	THEN 'BONOS'--'BONOS BANCARIOS'
					WHEN ConsultarCartera."IdTipoInstrumento" IN ('BEM')	THEN 'BONOS'--'BONOS EMPRESA'
					WHEN ConsultarCartera."IdTipoInstrumento" IN ('LTH')	THEN 'LETRAS HIPOTECARIAS'
					WHEN ConsultarCartera."IdTipoInstrumento" IN ('BCX','BCD','BCP','BCU')	THEN 'CENTRALES'
					WHEN ConsultarCartera."IdTipoInstrumento" IN ('BTP','BTU')	THEN 'TESORERIA'
					--WHEN "IdTipoInstrumento" IN ()	THEN 'DEPOSITOS'
					--WHEN "IdTipoInstrumento" IN ()	THEN 'CENTRALES' --'CENTRALES IF'
					WHEN ConsultarCartera."IdTipoInstrumento" IN ('PAG')	THEN 'PAGARES'
					ELSE '?' END								AS "TipoFamilia"						
			,ConsultarCartera."IdTipoReajuste"					AS "TipoReajuste"
			,ConsultarCartera."IdCartera"						AS "IdCartera"						
			,CASE ConsultarCartera."IdTipoOperacion"					
				WHEN 'TDEPLA'	THEN 'TOMDE' 
				WHEN 'TAUTDI'	then 'AUTOD'
			END			AS "TipoDocumento"			
			
			,'N'												AS "VentaCorta"				
			,''													AS "TipoCmbteCont" --?						
			,''													AS "FolioCmbteCont"--?					
			,ConsultarCartera."IdConcepto"						AS "Concepto"							
			,ConsultarCartera."EGRGlosa"						AS "Glosa"								
			,ConsultarCartera."FechaVencimientoOperacion"		AS "FechaDeVencimiento"				
			,ConsultarCartera."INGFechaValorizacionAnterior"	AS "FechaAnterior"
			,ConsultarCartera."INGFechaValorizacionCartera"		AS "FechaActual"					
			,ConsultarCartera."INGTasaMercadoActual"			AS "TasaMercadoActual"	
			,ConsultarCartera."INGPrecioMercadoActual"			AS "PrecioMercadoActual"				
			,ConsultarCartera."INGTasaMercadoAnterior"			AS "TasaMercadoAnterior"		
			,ConsultarCartera."INGPrecioMercadoAnterior"		AS "PrecioMercadoAnterior"				
			,'0'												AS "TasaEmision"						
			,ConsultarCartera."EGRTasa"							AS "TasaCompra"						
			,ConsultarCartera."EGRPrecio"	 					AS "PrecioCompra"	
			,ConsultarCartera."IdMonedaSistema"					AS "Moneda"
			,ConsultarCartera."ValorTipoCambioCierre"			AS "TasaCambio"
			,ConsultarCartera."INGFechaLiquidacion" 			AS "FechaLiquidacion"
			,ConsultarCliente."Tipo"							AS "TipoPersona"
			,ConsultarCartera."EGRFechaOperacion" 				AS "FechaOperacion"
			,LEFT(ConsultarCliente."NombreCompleto",60)			AS "NombreCliente"
			,DocInd."DocumentoIdentidad" 						AS "DocumentoIdentidadCAVALI"
		
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 																AS ConsultarCartera
		LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" 									AS ConsultarCarteraSubyacenteBasica
				ON ConsultarCarteraSubyacenteBasica."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 														AS ConsultarCliente
				ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 														AS DocInd 
	    		ON DocInd."IdPersona" 					= ConsultarCliente."IdPersona"   
	   			AND DocInd."IdTipoDocumentoIdentidad" 	= 'RUT' 				
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 														AS ConsultarEmpresa
				ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
		LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 														AS ConsultarPortafolio 
				ON ConsultarPortafolio."IdPortafolio" = ConsultarCartera."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" 													AS ConsultarEjecutivo
				ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" 											AS ConsultarIndicadoresEmpleado
				ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" 												AS ConsultarIndicadoresCliente
				ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarCartera."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL'--'RELCORR'

								
		WHERE 
				ConsultarCartera."IdEmpresa" 					= 	:IdEmpresa 
			AND	ConsultarCartera."IdEstado"						=	'VIG'
			AND ConsultarCartera."FechaOperacion" 	   			= 	:FechaContable
			AND ConsultarCartera."IdTipoOperacion"				IN ('TDEPLA','TAUTDI')
	       	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	= 'IF'; 