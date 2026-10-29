SELECT
DISTINCT --Cambiar mas adelante
 ConsultarCargosAbonos."IdCargoAbonoMovimiento" *10		AS "IdMovimiento"
,CASE ConsultarCargosAbonos."IdTipoMovimiento" 		
								WHEN 'MOVABN' 
 								THEN 'ABONO'
 								ELSE 'CARGO'  END		AS "TipoDocumento"				 
,ConsultarCargosAbonos."IdCargoAbonoMovimiento"			AS "Folio"
,YEAR(ConsultarCargosAbonos."FechaSistema") ||'-'|| 
 RIGHT('0' || MONTH(ConsultarCargosAbonos."FechaSistema"),2) ||'-'|| 
 RIGHT('0' || DAYOFMONTH(ConsultarCargosAbonos."FechaSistema"),2) 
 														AS "FechaDocumento"
,ConsultarCargosAbonos."IdEmpresa"						AS "IdEmpresa"                      	
,YEAR(ConsultarCargosAbonos."FechaSistema") ||'-'|| 
 RIGHT('0' || MONTH(ConsultarCargosAbonos."FechaSistema"),2) ||'-'|| 
 RIGHT('0' || DAYOFMONTH(ConsultarCargosAbonos."FechaSistema"),2) 
														AS "Fecha"
,LEFT(DocInd."DocumentoIdentidad",60)					AS "DocumentoIdentidad"
,LEFT(ConsultarPortafolio."CodigoPortafolio",60)		AS "Portafolio"
,ConsultarCargosAbonos."IdConcepto"						AS "Concepto"                                                    		
,LEFT(ConsultarConcepto."Nombre",60)					AS "NombreConcepto"                                                    		
,LEFT(CASE ConsultarPersona."Tipo" 
	WHEN 'N' THEN ConsultarPersona."Nombres" ||' '|| ConsultarPersona."ApellidoPaterno" || ' ' || ConsultarPersona."ApellidoMaterno" 
	ELSE ConsultarPersona."RazonSocial" END,60) 		AS "NombreCompleto"
,ConsultarCargosAbonos."IdMoneda"						AS "Moneda"                                                      
,ConsultarCargosAbonos."IdTipoProducto"					AS "Producto"
,ConsultarEjecutivo."CodigoEmpleado" 					AS "Agente"
,LEFT(ConsultarEjecutivo."NombreCompleto",60) 			AS "NombreAgente"
,ConsultarEjecutivo."CodigoSucursal" 					AS "SucursalAgente"
,LEFT(ConsultarEjecutivo."NombreSucursal",60) 			AS "NombreSucursalAgente"
,LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60)	AS "CentroCostoAgente"
,CASE WHEN (ConsultarSocioDeNegocio."IdContraparte" > 0 ) 
		THEN 'SI'
		ELSE 'NO' END 									AS "EsContraparte"
,CASE WHEN DocInd."DocumentoIdentidad" = Emp."DocumentoIdentidad"
					THEN 'CP'
	  	WHEN UPPER(ConsultarIndicadoresCliente."CodigoValorIndicador") = 'VINCULADO' 	
	  				THEN 'RE'
	  	ELSE		'NR'
	  END 												AS "Relacion"
,''														AS "Bolsa"
,ConsultarPersona."Tipo"								AS "TipoPersona"
,ConsultarCargosAbonos."NemotecnicoOriginal"			AS "Nemo"
,COALESCE(ConsultarCargosAbonos."IdTipoComprobante",'')	AS "IdTipoComprobante"
,CenCos."Business" 										AS "OpCentros"
,DocInd."DocumentoIdentidad" 							AS "DocumentoIdentidadCAVALI"
,COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') 	AS "ClienteBancario"
,COALESCE(spt."OperacionFicticia", 'N') 				AS "OperacionNoFacturable"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" 										AS ConsultarCargosAbonos
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion"			 								AS ConsultarCuentaInversion
ON ConsultarCuentaInversion."IdCuentaInversion" = ConsultarCargosAbonos."IdCuentaInversion" 
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"														AS ConsultarPortafolio
ON ConsultarPortafolio."IdPortafolio" = ConsultarCuentaInversion."IdPortafolio" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"														AS ConsultarCliente
ON ConsultarCliente."IdCliente" =  ConsultarPortafolio."IdCliente" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"														AS ConsultarPersona
ON ConsultarPersona."IdPersona" = ConsultarCliente."IdPersona"
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 										AS DocInd 
ON DocInd."IdPersona" 					= ConsultarCliente."IdPersona"   
AND DocInd."IdTipoDocumentoIdentidad" 	= 'R'			
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" 										AS ConsultarConcepto
ON ConsultarConcepto."IdConcepto" = ConsultarCargosAbonos."IdConcepto"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" 									AS ConsultarEjecutivo
ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCargosAbonos."IdEjecutivo"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" 							AS ConsultarIndicadoresEmpleado
ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 								AS ConsultarSocioDeNegocio
ON (ConsultarSocioDeNegocio."IdPersona" =  ConsultarPersona."IdPersona")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" 								AS ConsultarIndicadoresCliente
ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarCargosAbonos."IdCliente" AND --ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR 
	(ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"   AS IndClienteBancario
	ON IndClienteBancario."IdCliente" =  ConsultarCargosAbonos."IdCliente" AND IndClienteBancario."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 										AS Emp
ON (Emp."IdEmpresa" = ConsultarCargosAbonos."IdEmpresa" AND	
ConsultarCargosAbonos."DocumentoIdentidad" = Emp."DocumentoIdentidad")		
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.CentroCosto"			CenCos
	ON (CenCos."IdEmpresa" 	= ConsultarCargosAbonos."IdEmpresa" AND CenCos."ProductoConcepto" = ConsultarCargosAbonos."IdTipoProducto" AND
		CenCos."Relacionado"= CASE ConsultarCargosAbonos."DocumentoIdentidad" 
								WHEN Emp."DocumentoIdentidad" 	
								THEN 'CP' 
								ELSE 'CL' 
								END	AND 
		CenCos."IdBolsa" = 'BVL' AND
		CenCos."TipoCentro" = CASE 
								WHEN ConsultarCargosAbonos."IdTipoMovimiento" = 'MOVABN' 
									THEN 'INGRESO'	 
									ELSE 'COSTO' 
								END
		)		
LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" spt ON spt."IdOperacionSpot" = ConsultarCargosAbonos."IdOperacion" AND ConsultarCargosAbonos."IdTipoOperacion" IN ('COMSPT', 'VENSPT')
WHERE "EsIngresoManual" = 'S' 
AND CAST(ConsultarCargosAbonos."FechaSistema" AS DATE) = :FechaContable
AND ConsultarCargosAbonos."IdEstado" IN ('CRT');