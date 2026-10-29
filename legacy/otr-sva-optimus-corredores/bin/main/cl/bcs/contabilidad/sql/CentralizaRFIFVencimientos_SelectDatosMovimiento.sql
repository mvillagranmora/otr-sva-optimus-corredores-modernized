SELECT   
			 ConsultarCartera."IdCartera" 							AS "IdMovimiento"		 
			,LEFT(ConsultarCartera."DocumentoIdentidad",60)			AS "DocumentoIdentidad"
			,LEFT(ConsultarCartera."CodigoPortafolio",60)			AS "Portafolio"
			,LEFT(IFNULL(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)	AS "CentroCosto"
			,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'')    		AS "CodigoAgente"
			,ConsultarCartera."IdBolsa"								AS "CodigoBolsa"
			,ConsultarCartera."CodigoIFRS"							AS "CodigoIFRS"
			,ConsultarCartera."IdClasificacionTipoInstrumento"		AS "ClaseInstrumento"
			,ConsultarCartera."IdClasificacionTipoInstrumento"		AS "TipoInstrumento"
			,IFNULL(ConsultarEjecutivo."CodigoSucursal",'') 					AS "CodigoSucursal"
			,ConsultarCartera."IdTipoOperacion"						AS "CodigoTipoOperacion"
			,ConsultarCartera."IdSecuenciaCarteraMigracion"			AS "CodSecuencia"

			,CASE ConsultarCartera."DocumentoIdentidad" 
					WHEN ConsultarEmpresa."DocumentoIdentidad" 
					THEN 'S' ELSE 'N' END							AS "DiasLiquidacion"
			, CASE 	WHEN	ConsultarCartera."EGRIdCondicionLiquidacion"	='T0' THEN '0'
					WHEN	ConsultarCartera."EGRIdCondicionLiquidacion"	='T1' THEN '1'
					WHEN	ConsultarCartera."EGRIdCondicionLiquidacion"	='T2' THEN '2'
					ELSE	''	END									AS "DiasVencido"	
			,TO_BIGINT(DAYS_BETWEEN(ConsultarCartera."FechaLiquidacion",ConsultarCartera."FechaVencimientoInstrumento"))	AS "DiasVencimiento" 
			,ConsultarCartera."IdEmisor"							AS "EmisorInstrumento"
			,''														AS "EstadoMovimiento"
			,ConsultarCartera."IdTipoInstrumento"					AS "FamiliaInstrumento"
			,ConsultarCartera."EGRFechaComprobante"					AS "FechaEmisionDocumento"
			,CASE WHEN ConsultarCartera."EGRFechaLiquidacion" = '0001-01-01' THEN 
					CASE WHEN ConsultarCartera."EGRIdTipoOperacion"		='VCDTIT' THEN ConsultarCartera."EGRFechaOperacion" 	   				
					ELSE  ConsultarCartera."FechaVencimientoInstrumento" 	END 
			ELSE ConsultarCartera."EGRFechaLiquidacion" END 	AS "FechaLiquidacionDocumento"
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
			--,ConsultarCartera."IdEmisor"						AS "TipoEmisor"
			,ConsultarCartera."IdTipoInstrumento"				AS "FamiliaTipoInstrumento"----			
			,CASE 
			    WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
			                THEN 'CP'
			    WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S'  
			                THEN 'RE'
			    ELSE    'NR'
			  END                                                  	AS "Relacion"
			
	  	
				  	
			, "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor"
				(ConsultarCartera."IdEmisor", ConsultarCartera."IdClasificacionTipoInstrumento", 
	 			 ConsultarCartera."IdTipoInstrumento", ConsultarCartera."Nemotecnico", ConsultarCartera."IdTipoReajuste")
	 		 													AS "ClaseEmisor"
			,CASE WHEN ConsultarCartera."IdTipoInstrumento" IN ('BBC','BEM','BH','LTH','BCD','BCP','BCU','BTP','BTU','PAG','PRC','PCX','PRD') THEN 'Libre'	
				  WHEN ConsultarCartera."IdTipoInstrumento" = 'PAC' THEN 'VENDIDA CON PACTO'
				  ELSE '?' END
				  --emprestito	no hay para rf
				  												AS "TipoCartera"				
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
			,ConsultarCartera."IdTipoComprobante"				AS "TipoDocumento"			
			
			,'N'												AS "VentaCorta"				
			,''													AS "TipoCmbteCont" --?						
			,''													AS "FolioCmbteCont"--?					
			,ConsultarCartera."EGRIdConcepto"					AS "Concepto"							
			,ConsultarCartera."EGRGlosa"						AS "Glosa"								
			,ConsultarCartera."FechaVencimientoOperacion"		AS "FechaDeVencimiento"				
			,ConsultarCartera."INGFechaValorizacionAnterior"	AS "FechaAnterior"
			,ConsultarCartera."INGFechaValorizacionCartera"		AS "FechaActual"					
			,ConsultarCartera."INGTasaMercadoActual"			AS "TasaMercadoActual"	
			,ConsultarCartera."INGPrecioMercadoActual"			AS "PrecioMercadoActual"				
			,ConsultarCartera."INGTasaMercadoAnterior"			AS "TasaMercadoAnterior"		
			,ConsultarCartera."INGPrecioMercadoAnterior"		AS "PrecioMercadoAnterior"				
			,TO_DECIMAL(0)										AS "TasaEmision"						
			,ConsultarCartera."EGRTasa"							AS "TasaCompra"						
			,ConsultarCartera."EGRPrecio"	 					AS "PrecioCompra"	
			,ConsultarCartera."IdMonedaSistema"					AS "Moneda"
			,ConsultarCartera."ValorTipoCambioCierre"			AS "TasaCambio"
			,CASE WHEN ConsultarCartera."EGRFechaLiquidacion" = '0001-01-01' THEN 
					CASE WHEN ConsultarCartera."EGRIdTipoOperacion"		='VCDTIT' THEN ConsultarCartera."EGRFechaOperacion" 	   				
					ELSE  ConsultarCartera."FechaVencimientoInstrumento" 	END 
			ELSE ConsultarCartera."EGRFechaLiquidacion" END 
			AS "FechaLiquidacion"
			,ConsultarCliente."Tipo"							AS "TipoPersona"
			,ConsultarCartera."EGRFechaOperacion" 				AS "FechaOperacion"
			,LEFT(ConsultarCliente."NombreCompleto",60)			AS "NombreCliente"
			,CenCos."Business"									AS "OpCentros"
			,DocInd."DocumentoIdentidad" 							AS "DocumentoIdentidadCAVALI"
			,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
				ConsultarCartera."IdClasificacionTipoInstrumento",
				ConsultarCartera."IdTipoInstrumento", 
				ConsultarCartera."Nemotecnico",  
				ConsultarCartera."IdEmisor" )		AS "TipoEmisor"
			,COALESCE(MES."Codigo",'')	AS "Mesa"
			,COALESCE(IndCli2."NombreValorIndicador",'NO BANCARIO') AS "ClienteBancario" 
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 									AS ConsultarCartera
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 							AS ConsultarCliente
				ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"					AS SoCNeg
		ON (ConsultarCartera."IdCliente" = SoCNeg."IdCliente")
		
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 							AS DocInd 
		ON DocInd."IdPersona" 					= SoCNeg."IdPersona"   
		AND DocInd."IdTipoDocumentoIdentidad" 	= 'RUT'
				
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 							AS ConsultarEmpresa
				ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
		LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
				ON ConsultarPortafolio."IdPortafolio" = ConsultarCartera."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" 						AS ConsultarEjecutivo
				ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" 				AS ConsultarIndicadoresEmpleado
				ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" 					AS ConsultarIndicadoresCliente
				ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarCartera."IdCliente" AND --ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
				((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR 
	(ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))

		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"   AS IndCli2
					ON IndCli2."IdCliente" =  ConsultarCartera."IdCliente" AND IndCli2."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
		LEFT JOIN "BSCL.Contabilidad.BD::ITZ.CentroCosto"			CenCos
				ON (CenCos."IdEmpresa" 	= ConsultarCartera."IdEmpresa" AND CenCos."ProductoConcepto" = 'INRF' AND	-- Correo Eduardo 08-02-2018
					CenCos."Relacionado"= CASE ConsultarCartera."DocumentoIdentidad" 
											WHEN :DocumentoIdentidad 	
											THEN 'CP' 
											ELSE 'CL' 
											END	AND 
					CenCos."IdBolsa" = ConsultarCartera."IdBolsa" AND 
					CenCos."TipoCentro" = 'INGRESO'/*CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
												CASE Cmbte."AccionTipoOperacion"	WHEN 'COMPRA' THEN 'INGRESO' WHEN 'VENTA'  THEN 'COSTO' END -- FALTA DETERMINAR SI ES INGRESO O COSTO 
												ELSE CASE Cmbte."AccionTipoOperacion"
														WHEN 'COMPRA' THEN 'INGRESO' WHEN 'VENTA'  THEN 'COSTO' END
											END	*/)
											
		LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON ConsultarCartera."IdMesa" = MES."IdMesa"						
		WHERE 
			ConsultarCartera."IdEmpresa" = :IdEmpresa 
			AND
			ConsultarCartera."IdEstado"='VCD'
			AND (
					(ConsultarCartera."EGRIdTipoOperacion"		='VCDTIT' AND ConsultarCartera."EGRFechaOperacion" 	   				= :FechaContable)
				OR  (ConsultarCartera."EGRIdTipoOperacion"		='VCDPAC' AND ConsultarCartera."FechaVencimientoInstrumento" 	   	= :FechaContable)	
				)
	        AND ConsultarCartera."IdClasificacionTipoInstrumento" 	IN ('RF','IF');