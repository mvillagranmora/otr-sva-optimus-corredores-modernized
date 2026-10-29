SELECT 
				 Cmbte."IdComprobante" 									AS "IdMovimiento"
				,Cmbte."IdTipoComprobante" 								AS "TipoDocumento"
				,"FolioComprobante"  									AS "Folio"
				,YEAR(Cmbte."FechaLiquidacion") 		||'-'|| RIGHT('0' || 
				 MONTH(Cmbte."FechaLiquidacion"),2) 	||'-'|| RIGHT('0' || 
				 DAYOFMONTH(Cmbte."FechaLiquidacion"),2) 							AS "FechaDocumento"
		, CASE WHEN Cmbte."IdTipoComprobante" = 'NCE' OR Cmbte."IdTipoComprobante" = 'NCR' THEN
			CASE Cmbte."AccionTipoOperacion" WHEN 'COMPRA' THEN 'VENTA'	WHEN 'VENTA'  THEN 'COMPRA'	END 
			ELSE Cmbte."AccionTipoOperacion"
		 END 															AS "CompraVenta"
				,Cmbte."IdTipoOperacion" 								AS "Operacion"
				,Cmbte."CodigoTipoProducto"								AS "Producto"
				,Cmbte."IdClasificacionTipoInstrumento" 				AS "ClaseInstrumento"
				,Cmbte."IdTipoInstrumento" 								AS "TipoInstrumento"
				,Cmbte."IdCondicionLiquidacion" 						AS "CondicionLiquidacion"
				,Cmbte."IdMonedaComprobante" 							AS "MonedaComprobante"
				,Cmbte."IdBolsa" 										AS "Bolsa"
				,Cmbte."DocumentoIdentidad" 							AS "DocumentoIdentidad"
				,LEFT(Cli."NombreCompleto",60) 							AS "NombreCliente"
				,LEFT(Por."CodigoPortafolio",60)						AS "Portafolio"
				,Cmbte."IdConcepto"										AS "Concepto"
				,LEFT(Con."Nombre",60) 									AS "NombreConcepto"
				,CASE Cmbte."DocumentoIdentidad" 
					WHEN Emp."DocumentoIdentidad" 	
						THEN 'SI' 
						ELSE 'NO' 
				END 													AS "EsCuentaPropia"
				,CASE IndCli."ValorIndicador" 
					WHEN 'N' THEN 'NO' 
					ELSE 'SI' 
				 END													AS "EsRelacionado"
				,Eje."CodigoEmpleado" 									AS "Agente"
				,LEFT(Eje."NombreCompleto",60) 							AS "NombreAgente"
				,Eje."CodigoSucursal" 									AS "SucursalAgente"
				,LEFT(Eje."NombreSucursal",60) 							AS "NombreSucursalAgente"
				,LEFT(Indemp."ValorIndicador",60)						AS "CentroCostoAgente"
				,"IdEstadoSII" 											AS "EstadoSII"
				,"IdEstadoSII" 											AS "EstadoSUNAT"
				,CASE "EsExento" WHEN 'N' THEN 'NO' ELSE 'SI' END		AS "EsExento"
				,Cmbte."IdMoneda" 										AS "Moneda"
				,Cmbte."IdMonedaLiquidacion" 							AS "MonedaLiquidacion"
				,'N'													AS "EsContraparte"
				,CASE 
					WHEN Cmbte."DocumentoIdentidad" = Emp."DocumentoIdentidad"
								THEN 'CP'
				  	--WHEN IndCli."ValorIndicador" = 'S'
				 	WHEN UPPER(IndCli."CodigoValorIndicador") = 'VINCULADO'
				  				THEN 'RE'
				  	ELSE	'NR'
				  END 													AS "Relacion",
				Cli."Tipo"								 				AS "TipoPersona",


				"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
				(Ins."IdEmisor", Cmbte."IdClasificacionTipoInstrumento", 
		 		Cmbte."IdTipoInstrumento", CmbteDet."NemotecnicoInstrumento", Ins."IdTipoReajuste")
			 															AS "ClaseEmisor",
				Ins."IdEmisor"											AS "Emisor",
				Ins."Nemotecnico"										AS "Nemotecnico",
				Cmbte."DocumentoIdentidadCAVALI"						AS "DocumentoIdentidadCAVALI"	,
				TRIM(IFNULL(Cmbte."CodigoIFRS",''))                    AS "CodigoIFRS"

--				
		FROM 		"BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 				AS Cmbte
		LEFT JOIN  	"BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" 		AS CmbteDet
		ON 			CmbteDet."IdComprobante" = Cmbte."IdComprobante"

		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 				AS Emp
		ON Emp."IdEmpresa" = Cmbte."IdEmpresa"
		
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 				AS Cli 
		ON Cli."IdCliente" =  Cmbte."IdCliente"
		
		LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 				AS Por 
		ON Por."IdPortafolio" = Cmbte."IdPortafolio" 
		
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" 		AS Con 
		ON Con."IdConcepto" = Cmbte."IdConcepto"
		
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" 			AS Eje 
		ON Eje."IdEmpleadoEjecutivo" = Cmbte."IdEmpleadoEjecutivoPortafolio"
		
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" 	AS Indemp
--		ON Indemp."IdEmpleado" =  Eje."IdEmpleadoEjecutivo" AND Indemp."CodigoTipoIndicador"  = 'CENCOSTO'
		ON Indemp."IdEmpleado" =  Eje."IdEmpleadoSupervisor" AND Indemp."CodigoTipoIndicador"  = 'CENCOSTO'
		
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" 		AS IndCli
		ON IndCli."IdCliente" =  Cmbte."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'GENPRIMREL'--'RELCORR''RELCORR'
			
	
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"								AS Ins 
		ON Ins."IdInstrumento" = CmbteDet."IdInstrumento" 
								
--			
			
		WHERE 	Cmbte."FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND Cmbte."IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND "EsVentaCorta" = 'N';