		SELECT   
			DISTINCT 
				CA."IdCargoAbonoMovimiento" 							AS "IdMovimiento"
				,CASE CA."IdTipoMovimiento" 		
						WHEN 'MOVABN' THEN 'ABONO'
						ELSE 'CARGO'  
				  END													AS "TipoDocumento"
				,CA."IdCargoAbonoMovimiento"							AS "Folio"
				,YEAR(CA."FechaSistema") ||'-'|| 
				 RIGHT('0' || MONTH(CA."FechaSistema"),2) ||'-'|| 
				 RIGHT('0' || DAYOFMONTH(CA."FechaSistema"),2) 
				 														AS "FechaDocumento"
				,CA."IdEmpresa"											AS "IdEmpresa" 
				,YEAR(CA."FechaSistema") ||'-'|| 
			 	 RIGHT('0' || MONTH(CA."FechaSistema"),2) ||'-'|| 
			 	 RIGHT('0' || DAYOFMONTH(CA."FechaSistema"),2) 			AS "Fecha"
				--,LEFT(PER."DocumentoIdentidad",60)						AS "DocumentoIdentidad"
				,LEFT(DocIdentPer."DocumentoIdentidad",60)				AS "DocumentoIdentidad"
				,LEFT(POR."CodigoPortafolio",60)						AS "Portafolio"
				,CA."IdConcepto"										AS "Concepto"
				,LEFT(CPT."Nombre",60)									AS "NombreConcepto"                                                    		
				,LEFT(CLI."NombreCompleto",60)							AS "NombreCliente"                                       
				,CA."IdMoneda"											AS "Moneda"
				,EJE."CodigoEmpleado" 									AS "Agente"
				,LEFT(EJE."NombreCompleto",60) 							AS "NombreAgente"				
				,EJE."IdSucursal" 										AS "SucursalAgente"
--				,EJE."CodigoSucursal"                                   AS "SucursalAgente"
				,LEFT(EJE."NombreSucursal",60) 							AS "NombreSucursalAgente"
				,LEFT(IFNULL(INDEMP."ValorIndicador",''),60)			AS "CentroCostoAgente"
				
				,IFNULL(CA."IdTipoProducto",'')							AS "Producto"
				,CASE 
				    --WHEN PER."DocumentoIdentidad" = EMP."DocumentoIdentidad"
				    WHEN DocIdentPer."DocumentoIdentidad" = EMP."DocumentoIdentidad"
				                THEN 'CP'
				    WHEN UPPER(ConsultarIndicadoresCliente."CodigoValorIndicador") = 'VINCULADO'  
				                THEN 'RE'
				    ELSE    'NR'
  				END  													AS "Relacion"
				,''														AS "Bolsa"
				,CLI."Tipo"												AS "TipoPersona"
				,IFNULL(CA."NemotecnicoOriginal",'')					AS "Nemo"
				,IFNULL(DocInd."DocumentoIdentidad",'') 				AS "DocumentoIdentidadCAVALI"
				,TO_BIGINT(0)											AS "Dias"
				,IFNULL(CA."IdTipoComprobante",'')						AS "TipoCmbte"
				,COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') 	AS "ClienteBancario"
		  FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" 				AS CA
		  LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" 		AS CIV
				 ON CIV."IdCuentaInversion" = CA."IdCuentaInversion"
		  --LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 				AS POR
				 --ON POR."IdPortafolio"	= CIV."IdPortafolio"
		  LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"                            AS POR
		         ON POR."IdPortafolio"	= CIV."IdPortafolio"		  
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			AS CLI
				 ON CLI."IdCliente"		= POR."IdCliente" 
		  --LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" 			AS PER
				 --ON PER."IdPersona"		= CLI."IdPersona"
		  LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"                             AS PER
		         ON PER."IdPersona"		= CLI."IdPersona"		         
		  LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"                  AS DocIdentPer
		         ON DocIdentPer."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"		 
		  LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 			AS DocInd 
	    		ON DocInd."IdPersona" 					= CLI."IdPersona"   
	   			AND DocInd."IdTipoDocumentoIdentidad" 	= 'RUT' 
				 
		  LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" 								AS CPT
		  		 ON CPT."IdConcepto"	= CA."IdConcepto"	
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 				AS EMP
				 ON EMP."IdEmpresa" 	= CA."IdEmpresa"		  
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" AS INDEMP
				 ON INDEMP."IdEmpleado" =  POR."IdEmpleadoEjecutivo" AND INDEMP."CodigoTipoIndicador"  = 'CENCOSTO' AND INDEMP."IdEmpresa" = CA."IdEmpresa"
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" 			AS EJE
		  		 ON EJE."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
				ON ConsultarIndicadoresCliente."IdCliente" =  CA."IdCliente" AND --ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
				((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR 
				(ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"   AS IndClienteBancario
				ON IndClienteBancario."IdCliente" =  CA."IdCliente" AND IndClienteBancario."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
		
		 WHERE CA."FechaSistema" = :FechaContable
		   AND CA."SaldoLibroInventario" = 0
		   AND CA."IdEstado" IN ('CRT')

UNION ALL

		SELECT  DISTINCT  
				CA."IdCargoAbonoMovimiento" 							AS "IdMovimiento"
				,CASE CA."IdTipoMovimiento" 		
						WHEN 'MOVABN' THEN 'MOABONO'
						ELSE 'MOCARGO'  
				  END													AS "TipoDocumento"
				,CA."IdCargoAbonoMovimiento"							AS "Folio"
				,YEAR(:FechaContable) ||'-'|| 
				 RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
				 RIGHT('0' || DAYOFMONTH(:FechaContable),2) 
				 														AS "FechaDocumento"
				,:IdEmpresa												AS "IdEmpresa" 
				,YEAR(:FechaContable) ||'-'|| 
			 	 RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
			 	 RIGHT('0' || DAYOFMONTH(:FechaContable),2) 			AS "Fecha"
				,LEFT(PER."DocumentoIdentidad",60)						AS "DocumentoIdentidad"
				,LEFT(POR."CodigoPortafolio",60)						AS "Portafolio"
				,CA."IdConcepto"										AS "Concepto"
				,LEFT(CPT."Nombre",60)									AS "NombreConcepto"                                                    		
				,LEFT(CLI."NombreCompleto",60)							AS "NombreCliente"                                       
				,CA."IdMoneda"											AS "Moneda"
				,EJE."CodigoEmpleado" 									AS "Agente"
				,LEFT(EJE."NombreCompleto",60) 							AS "NombreAgente"				
				,EJE."IdSucursal" 										AS "SucursalAgente"
--				,EJE."CodigoSucursal"                                   AS "SucursalAgente"
				,LEFT(EJE."NombreSucursal",60) 							AS "NombreSucursalAgente"
				,LEFT(IFNULL(INDEMP."ValorIndicador",''),60)			AS "CentroCostoAgente"
				
				,IFNULL(CI."IdTipoProducto",'')							AS "Producto"
				,CASE WHEN PER."DocumentoIdentidad" = EMP."DocumentoIdentidad"
								THEN 'CP'
				  	WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 	
				  				THEN 'RE'
				  	ELSE		'NR'
				  END 													AS "Relacion"
				,''														AS "Bolsa"
				,CLI."Tipo"												AS "TipoPersona"
				--,IFNULL(CI."NemotecnicoOriginal",'')					AS "Nemo"
				,COALESCE (CI."Nemo" ,'' )                              AS "Nemo"
				,IFNULL(DocInd."DocumentoIdentidad",'') 				AS "DocumentoIdentidadCAVALI"
				,CA."AntiguedadDeuda"									AS "Dias"
				,IFNULL(CI."IdTipoComprobante",'')						AS "TipoCmbte"
				,COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') 	AS "ClienteBancario"
--		  FROM :v_TabCargos 														AS CA
FROM (	SELECT   	 
			CPC."IdCargoAbonoMovimiento"
			,CPC."IdTipoMovimiento"
			,CPC."IdConcepto"
			,CPC."NombreConcepto"
			,CPC."IdCuentaInversion"
			,CPC."TipoCliente"
			,CPC."IdCliente"
			,CPC."DocumentoIdentidadCliente"
			,CPC."NombreCliente"
			,CPC."RazonSocial"
			,CPC."IdPortafolio"
			,CPC."CodigoPortafolio"
			,CPC."Proposito"
			,CPC."CodigoProposito"
			,CPC."IdMoneda"
			,CPC."CodigoMoneda"
			,CPC."NombreMoneda"
			,CPC."CodigoNombreMoneda"
			,CPC."IdCondicionDeLiquidacion"
			,CPC."FechaEfectivaMovimiento"
			,CPC."Monto"
			,CPC."SaldoLibroInventario"
			,CPC."AntiguedadDeuda"
			,CPC."IdEstado"
		FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentasPorPagarCobrar" as CPC
		WHERE CPC."SaldoLibroInventario" > 0
		AND CPC."IdTipoMovimiento" = 'MOVCAR' 	   
		AND CPC."IdEstado" = 'CRT'
		AND CPC."FechaEfectivaMovimiento" 			= :FechaContable
		AND (NULL IS NULL OR CPC."IdCliente" 		= NULL)
		AND (NULL IS NULL OR CPC."IdPortafolio"	= NULL)
		) AS CA
--
		  --LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" 		AS CI
		  --ON (CA."IdCargoAbonoMovimiento" = CI."IdCargoAbonoMovimiento")
		  LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"             AS CI 
		         ON (CA."IdCargoAbonoMovimiento" = CI."IdCargoAbonoMovimiento")
		  LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" 		AS CIV
				 ON CIV."IdCuentaInversion" = CA."IdCuentaInversion"
		  LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 				AS POR
				 ON POR."IdPortafolio"	= CIV."IdPortafolio" 
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			AS CLI
				 ON CLI."IdCliente"		= POR."IdCliente" 
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" 			AS PER
				 ON PER."IdPersona"		= CLI."IdPersona"
		  LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 			AS DocInd 
	    		ON DocInd."IdPersona" 					= CLI."IdPersona"   
	   			AND DocInd."IdTipoDocumentoIdentidad" 	= 'RUT' 
				 
		  LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" 								AS CPT
		  		 ON CPT."IdConcepto"	= CA."IdConcepto"	
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 				AS EMP
				 ON EMP."IdEmpresa" 	= :IdEmpresa		  
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" AS INDEMP
				 ON INDEMP."IdEmpleado" =  POR."IdEmpleadoEjecutivo" AND INDEMP."CodigoTipoIndicador"  = 'CENCOSTO' AND INDEMP."IdEmpresa" = :IdEmpresa
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" 			AS EJE
		  		 ON EJE."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
				ON ConsultarIndicadoresCliente."IdCliente" =  CA."IdCliente" AND --ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
					((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR 
					(ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"   AS IndClienteBancario
				ON IndClienteBancario."IdCliente" =  CA."IdCliente" AND IndClienteBancario."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
				
		 WHERE --CA."FechaSistema" = :p_FechaContable
		   	   CA."AntiguedadDeuda" > 0
		   AND CA."IdEstado" IN ('CRT')	
UNION ALL

		SELECT  DISTINCT  
				CA."IdCargoAbonoMovimiento" 							AS "IdMovimiento"
				,CASE CA."IdTipoMovimiento" 		
						WHEN 'MOVABN' THEN 'MOABONO'
						ELSE 'MOCARGO'  
				  END													AS "TipoDocumento"
				,CA."IdCargoAbonoMovimiento"							AS "Folio"
				,YEAR(:FechaContable) ||'-'|| 
				 RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
				 RIGHT('0' || DAYOFMONTH(:FechaContable),2) 
				 														AS "FechaDocumento"
				,:IdEmpresa												AS "IdEmpresa" 
				,YEAR(:FechaContable) ||'-'|| 
			 	 RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
			 	 RIGHT('0' || DAYOFMONTH(:FechaContable),2) 			AS "Fecha"
				,LEFT(PER."DocumentoIdentidad",60)						AS "DocumentoIdentidad"
				,LEFT(POR."CodigoPortafolio",60)						AS "Portafolio"
				,CA."IdConcepto"										AS "Concepto"
				,LEFT(CPT."Nombre",60)									AS "NombreConcepto"                                                    		
				,LEFT(CLI."NombreCompleto",60)							AS "NombreCliente"                                       
				,CA."IdMoneda"											AS "Moneda"
				,EJE."CodigoEmpleado" 									AS "Agente"
				,LEFT(EJE."NombreCompleto",60) 							AS "NombreAgente"				
				,EJE."IdSucursal" 										AS "SucursalAgente"
--				,EJE."CodigoSucursal"                                   AS "SucursalAgente"
				,LEFT(EJE."NombreSucursal",60) 							AS "NombreSucursalAgente"
				,LEFT(IFNULL(INDEMP."ValorIndicador",''),60)			AS "CentroCostoAgente"
				
				,IFNULL(CI."IdTipoProducto",'')							AS "Producto"
				,CASE WHEN PER."DocumentoIdentidad" = EMP."DocumentoIdentidad"
								THEN 'CP'
				  	WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 	
				  				THEN 'RE'
				  	ELSE		'NR'
				  END 													AS "Relacion"
				,''														AS "Bolsa"
				,CLI."Tipo"												AS "TipoPersona"
				--,IFNULL(CI."NemotecnicoOriginal",'')					AS "Nemo"
				,COALESCE (CI."Nemo" ,'' )                              AS "Nemo"
				,IFNULL(DocInd."DocumentoIdentidad",'') 				AS "DocumentoIdentidadCAVALI"
				,CA."AntiguedadDeuda"									AS "Dias"
				,IFNULL(CI."IdTipoComprobante",'')						AS "TipoCmbte"
				,COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') 	AS "ClienteBancario"
--
--		  FROM :v_TabCargos 														AS CA
FROM (	SELECT   	 
			CPC."IdCargoAbonoMovimiento"
			,CPC."IdTipoMovimiento"
			,CPC."IdConcepto"
			,CPC."NombreConcepto"
			,CPC."IdCuentaInversion"
			,CPC."TipoCliente"
			,CPC."IdCliente"
			,CPC."DocumentoIdentidadCliente"
			,CPC."NombreCliente"
			,CPC."RazonSocial"
			,CPC."IdPortafolio"
			,CPC."CodigoPortafolio"
			,CPC."Proposito"
			,CPC."CodigoProposito"
			,CPC."IdMoneda"
			,CPC."CodigoMoneda"
			,CPC."NombreMoneda"
			,CPC."CodigoNombreMoneda"
			,CPC."IdCondicionDeLiquidacion"
			,CPC."FechaEfectivaMovimiento"
			,CPC."Monto"
			,CPC."SaldoLibroInventario"
			,CPC."AntiguedadDeuda"
			,CPC."IdEstado"
		FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentasPorPagarCobrar" as CPC
		WHERE CPC."SaldoLibroInventario" > 0
		AND CPC."IdTipoMovimiento" = 'MOVABN' 	   
		AND CPC."IdEstado" = 'CRT'
		AND CPC."FechaEfectivaMovimiento" 			= :FechaContable
		AND (NULL IS NULL OR CPC."IdCliente" 		= NULL)
		AND (NULL IS NULL OR CPC."IdPortafolio"	= NULL)
		) AS CA
--
--
		  --LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" 		AS CI
		  --ON (CA."IdCargoAbonoMovimiento" = CI."IdCargoAbonoMovimiento")
		  LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"             AS CI 
		    ON (CA."IdCargoAbonoMovimiento" = CI."IdCargoAbonoMovimiento")
		  LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" 		AS CIV
				 ON CIV."IdCuentaInversion" = CA."IdCuentaInversion"
		  LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 				AS POR
				 ON POR."IdPortafolio"	= CIV."IdPortafolio" 
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			AS CLI
				 ON CLI."IdCliente"		= POR."IdCliente" 
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" 			AS PER
				 ON PER."IdPersona"		= CLI."IdPersona"
		  LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 			AS DocInd 
	    		ON DocInd."IdPersona" 					= CLI."IdPersona"   
	   			AND DocInd."IdTipoDocumentoIdentidad" 	= 'RUT' 
				 
		  LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" 								AS CPT
		  		 ON CPT."IdConcepto"	= CA."IdConcepto"	
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 				AS EMP
				 ON EMP."IdEmpresa" 	= :IdEmpresa		  
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" AS INDEMP
				 ON INDEMP."IdEmpleado" =  POR."IdEmpleadoEjecutivo" AND INDEMP."CodigoTipoIndicador"  = 'CENCOSTO' AND INDEMP."IdEmpresa" = :IdEmpresa
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" 			AS EJE
		  		 ON EJE."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
				ON ConsultarIndicadoresCliente."IdCliente" =  CA."IdCliente" AND --ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
					((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR 
					(ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"   AS IndClienteBancario
				ON IndClienteBancario."IdCliente" =  CA."IdCliente" AND IndClienteBancario."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
		
		 WHERE --CA."FechaSistema" = :p_FechaContable
		   	   CA."AntiguedadDeuda" > 0
		   AND CA."IdEstado" IN ('CRT');