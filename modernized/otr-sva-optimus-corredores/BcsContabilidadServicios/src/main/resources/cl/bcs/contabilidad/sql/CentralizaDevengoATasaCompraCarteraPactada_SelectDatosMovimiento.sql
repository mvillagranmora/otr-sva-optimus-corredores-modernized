--IdTipoCentralizacion = 440: CodigoCentralizacion = 06; Informe = Pacto a Tasa Compra
SELECT
	 CVIFRF."IdValorizacion"							AS "IdMovimiento"  
	,LEFT(ConsultarCliente."DocumentoIdentidad",60) AS "DocumentoIdentidad"                                    
	,LEFT(ConsultarCliente."NombreCompleto",60) 	AS "NombreCliente"                                       
	,LEFT(ConsultarCliente."CodigoPortafolioDefecto",60) AS "Portafolio"
	,CVIFRF."IdMonedaInstrumento" 					AS "Moneda"                                                      
	,CASE CVIFRF."IdTipoOperacion"
		WHEN 'VENPAC' THEN 'V'
		WHEN 'COMPAC' THEN 'C'
		ELSE '??????????'			
	 END AS "CompraVenta"						
	,CVIFRF."IdTipoProducto"						AS "Producto"
	,CVIFRF."IdClasificacionTipoInstrumento" 		AS "ClaseInstrumento"
	,CVIFRF."IdTipoInstrumento" 					AS "TipoInstrumento"
	,CASE 
		WHEN CVIFRF."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC','RRV','RRF') THEN (SELECT Suby."Nemotecnico" 
	                                                                                    FROM (SELECT "Nemotecnico", row_number() over(order by (SELECT 1 from dummy) ) as "NroFila" 
                                                                                        	  FROM "BSCL.Custodia.BD::CUS.Subyacente" as miSub
	                                                                                          WHERE CVIFRF."FolioCartera" = miSub."FolioCarteraRelacionada") as Suby
	                                                                                    WHERE Suby."NroFila" = 1)
		WHEN CVIFRF."IdClasificacionTipoInstrumento" = 'IF' THEN CVIFRF."NemoSVS"
    	ELSE CVIFRF."Nemotecnico"
     END AS "Nemo"
	,CVIFRF."IdBoveda" 									AS "Boveda"	
	,CAST(CVIFRF."FolioCartera" AS NVARCHAR(60)) 		AS "IdSubyacente"		
	,CVIFRF."Emisor"
	,CVIFRF."IdTipoReajuste" 							AS "TipoReajuste"
	,CVIFRF."CodigoIFRS"
	,CASE
        WHEN CVIFRF."Diferenciador" = 'PC'            THEN 'VRC'
        WHEN CVIFRF."Diferenciador" = 'PP'            THEN 'VRC-CRV'
        WHEN CVIFRF."Diferenciador" = 'PD'            THEN 'CRV'
        ELSE '-'
	END AS "TipoCartera"
	,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor"(CVIFRF."Emisor", CVIFRF."IdClasificacionTipoInstrumento", CVIFRF."IdTipoInstrumento" , CVIFRF."Nemotecnico", CVIFRF."IdTipoReajuste") AS "ClaseEmisor"
	,TEMI."CodigoTipoEmisor"							AS "TipoEmisor"
	,ConsultarEjecutivo."CodigoEmpleado" 				AS "Agente"
	,LEFT(ConsultarEjecutivo."NombreCompleto",60) 		AS "NombreAgente"
	,ConsultarEjecutivo."CodigoSucursal" 				AS "SucursalAgente"
	,LEFT(ConsultarEjecutivo."NombreSucursal",60) 		AS "NombreSucursalAgente"
	,LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60) AS "CentroCostoAgente"
	,CASE 
		WHEN ConsultarCliente."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad" THEN 'CP'
		WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' THEN 'RE'
	 	ELSE 'NR'
	 END AS "Relacion"
	,CVIFRF."IdBolsa" 									AS "Bolsa"
	,ConsultarCliente."Tipo" 							AS "TipoPersona"
	,CVIFRF."IdConcepto" 								AS "Concepto"
	,CVIFRF."FechaLiquidacion" 							AS "FechaLiquidacion"
	,CVIFRF."FechaOperacion" 							AS "FechaOperacion"																						
	,'MESA_RENTA_FIJA'									AS "Mesa"
	,CVIFRF."FolioOperacion" AS "FolioOperacion"
	,CASE WHEN CVIFRF."FechaVencimientoOperacion" <= TO_DATE( :p_FechaContable,'YYYY-MM-DD')  THEN 'SI' 
	  ELSE 'NO' END  AS "EsVencimiento"
	,COALESCE(OPE."IdTipoReajuste", CVIFRF."IdTipoReajuste") AS "TipoReajusteCabecera"
FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa ON ConsultarEmpresa."IdEmpresa" = CVIFRF."IdEmpresa"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente ON ConsultarCliente."IdCliente" = CVIFRF."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = CVIFRF."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado ON (ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente ON (ConsultarIndicadoresCliente."IdCliente" =  CVIFRF."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR')
LEFT JOIN "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" TEMI ON TEMI."IdTipoInstrumento" = CVIFRF."IdTipoInstrumento"
			AND CVIFRF."IdTipoInstrumento" = TEMI."IdTipoInstrumento"
			AND ( 
				TEMI."Nemotecnico" != '' AND (TRIM(TEMI."Nemotecnico") = CVIFRF."Nemotecnico")
										 AND ((TEMI."IdEmisor" != '' AND TEMI."IdEmisor" = CVIFRF."Emisor") OR (TEMI."IdEmisor" = ''))					
				OR
				TEMI."Nemotecnico" = '' AND ((TEMI."IdEmisor" = CVIFRF."Emisor") OR (TEMI."IdEmisor" = ''))
			)
LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE 
	ON OPE."IdOperacion" = CVIFRF."IdOperacion"
WHERE CVIFRF."IdInforme" = :p_IdInforme	
AND CVIFRF."IdEmpresa" = :p_IdEmpresa;
