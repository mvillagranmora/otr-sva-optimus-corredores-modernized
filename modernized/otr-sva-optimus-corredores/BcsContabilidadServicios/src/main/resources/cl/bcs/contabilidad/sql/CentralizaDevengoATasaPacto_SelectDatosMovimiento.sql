SELECT 
	 CVIFRF."IdValorizacion" AS "IdMovimiento"		 
	,LEFT(ConsultarCliente."DocumentoIdentidad",60) AS "DocumentoIdentidad" 
	,CASE CVIFRF."IdTipoOperacion" 
		WHEN 'VENPAC' THEN 'V'
		WHEN 'COMPAC' THEN 'C'
		ELSE '??????????'			
	END AS "CompraVenta"	
	,LEFT(ConsultarCliente."NombreCompleto",60) AS "NombreCliente"                                       
	,LEFT(ConsultarPortafolio."CodigoPortafolio",60) AS "Portafolio"
	,CVIFRF."IdMonedaInstrumento"	AS "Moneda"                                                      
	,CVIFRF."IdTipoProducto" AS "Producto"
	,CVIFRF."IdClasificacionTipoInstrumento" AS "ClaseInstrumento"
	,CVIFRF."IdTipoInstrumento" AS "TipoInstrumento"
	,CVIFRF."Nemotecnico" AS "Nemo"
	,CVIFRF."IdBoveda" AS "Boveda"	
	,COALESCE(CVIFRF."IdCartera",0) AS "IdCartera"		
	,CVIFRF."Emisor" AS "Emisor"
	,CVIFRF."IdTipoReajuste" AS "TipoReajuste"
	,CVIFRF."CodigoIFRS" AS "CodigoIFRS"
	,CASE
        WHEN CVIFRF."Diferenciador" = 'PC'            THEN 'VRC'
        WHEN CVIFRF."Diferenciador" = 'PP'            THEN 'VRC-CRV'
        WHEN CVIFRF."Diferenciador" = 'PD'            THEN 'CRV'
        ELSE '-'
	END AS "TipoCartera"
	,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor"(CVIFRF."Emisor", CVIFRF."IdClasificacionTipoInstrumento", CVIFRF."IdTipoInstrumento", CVIFRF."Nemotecnico", CVIFRF."IdTipoReajuste") AS "ClaseEmisor"
	,ConsultarEjecutivo."CodigoEmpleado" AS "Agente"
	,LEFT(ConsultarEjecutivo."NombreCompleto",60) AS "NombreAgente"
	,ConsultarEjecutivo."CodigoSucursal" AS "SucursalAgente"
	,LEFT(ConsultarEjecutivo."NombreSucursal",60) AS "NombreSucursalAgente"
	,LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60) AS "CentroCostoAgente"
	,CASE 
		WHEN ConsultarCliente."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad" THEN 'CP'
	  	WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' THEN 'RE'
	  	ELSE 'NR'
	 END AS "Relacion"
	,CVIFRF."IdBolsa" AS "Bolsa"
	,CVIFRF."FechaLiquidacion" AS "FechaLiquidacion"
	,CVIFRF."FechaOperacion" AS "FechaOperacion"
	,cvifrf."IdConcepto" AS "Concepto"
	,ConsultarCliente."Tipo" AS "TipoPersona"
	,'MESA_RENTA_FIJA' AS "Mesa"
	,CVIFRF."FolioOperacion" AS "FolioOperacion"
	,COALESCE(IndCli2."NombreValorIndicador",'NO BANCARIO') AS "ClienteBancario"
	,CASE WHEN CVIFRF."FechaVencimientoOperacion" <= TO_DATE( :p_FechaContable,'YYYY-MM-DD')  THEN 'SI' 
	  ELSE 'NO' END  AS "EsVencimiento"
	,COALESCE(OPE."IdTipoReajuste", CVIFRF."IdTipoReajuste") AS "TipoReajusteCabecera"
FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente ON ConsultarCliente."IdCliente" = CVIFRF."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa ON ConsultarEmpresa."IdEmpresa" = CVIFRF."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio ON ConsultarPortafolio."IdPortafolio" = CVIFRF."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = CVIFRF."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado 
	ON (ConsultarIndicadoresEmpleado."IdEmpleado" = ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador" = 'CENCOSTO')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente 
	ON (ConsultarIndicadoresCliente."IdCliente" =  ConsultarCliente."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS IndCli2 
	ON (IndCli2."IdCliente" =  ConsultarCliente."IdCliente" AND IndCli2."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO')
LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE 
	ON OPE."IdOperacion" = CVIFRF."IdOperacion" 
WHERE CVIFRF."IdInforme" = :p_IdInforme
AND CVIFRF."IdEmpresa" = :p_IdEmpresa 
AND CVIFRF."IdTipoOperacion" in ('COMPAC' , 'VENPAC');