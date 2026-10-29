WITH v_TabCarteras AS(
	SELECT
	     CVIFRF."IdCartera"                       	AS "IdMovimiento"
	    ,CVIFRF."IdEmpresa" 
	    ,CVIFRF."IdCliente"    
	    ,CVIFRF."IdPortafolio" 
	    ,CVIFRF."IdAgente" 
		,CVIFRF."IdMesa" 
	    ,LEFT(CP."DocumentoIdentidad",60)         	AS "DocumentoIdentidad"                                                   
	    ,LEFT(CP."CodigoPortafolio",60)           	AS "Portafolio"
	    ,CVIFRF."IdMonedaInstrumento"             	AS "Moneda"                                                                                                        
	    ,CVIFRF."IdTipoProducto"                   	AS "Producto"
	    ,CVIFRF."IdClasificacionTipoInstrumento"  	AS "ClaseInstrumento"
	    ,CVIFRF."IdTipoInstrumento"               	AS "TipoInstrumento"
	    ,CVIFRF."Nemotecnico"                     	AS "Nemo"
	    ,CVIFRF."IdBoveda"                        	AS "Boveda" 
	    ,CAST(CVIFRF."IdCartera" AS NVARCHAR(60)) 	AS "IdCartera"      
	    ,CVIFRF."Emisor"                          	AS "Emisor"
	    ,CVIFRF."IdTipoReajuste"                  	AS "TipoReajuste"
	    ,CVIFRF."CodigoIFRS"                      	AS "CodigoIFRS"
	    ,CASE WHEN CVIFRF."Diferenciador" = 'CD' THEN 'LIBRE' ELSE 'GARANTIA' END AS "TipoCartera"
	    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor"(CVIFRF."Emisor", CVIFRF."IdClasificacionTipoInstrumento", CVIFRF."IdTipoInstrumento" , CVIFRF."Nemotecnico", CVIFRF."IdTipoReajuste") AS "ClaseEmisor"
	    ,TEMI."CodigoTipoEmisor" 					AS "TipoEmisor"
	    ,CVIFRF."IdBolsa"                         	AS "Bolsa"
	    ,CVIFRF."FechaLiquidacion" 					AS "FechaLiquidacion"
	    ,CVIFRF."FechaOperacion" 					AS "FechaOperacion"
	    ,CVIFRF."IdConcepto"                      	AS "IdConcepto"
	FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF 
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CVIFRF."IdPortafolio"
	 LEFT JOIN "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" TEMI ON TEMI."IdTipoInstrumento" = CVIFRF."IdTipoInstrumento"
			AND CVIFRF."IdTipoInstrumento" = TEMI."IdTipoInstrumento"
			AND ( 
				TEMI."Nemotecnico" != '' AND (TRIM(TEMI."Nemotecnico") = CVIFRF."Nemotecnico")
										 AND ((TEMI."IdEmisor" != '' AND TEMI."IdEmisor" = CVIFRF."Emisor") OR (TEMI."IdEmisor" = ''))					
				OR
				TEMI."Nemotecnico" = '' AND ((TEMI."IdEmisor" = CVIFRF."Emisor") OR (TEMI."IdEmisor" = ''))
			)
	WHERE CVIFRF."IdInforme" = :p_IdInforme
	  AND CVIFRF."IdEmpresa" = :p_IdEmpresa
	  AND CVIFRF."IdClasificacionTipoInstrumento" in ('RF','IF')
)
SELECT 
	"IdMovimiento"        
    ,ConsultarCliente."DocumentoIdentidad"                                    
    ,LEFT(ConsultarCliente."NombreCompleto",60) AS "NombreCliente"                                       
    ,"Portafolio"
    ,"Moneda"                                                                                                        
    ,"Producto"
    ,"ClaseInstrumento"
    ,"TipoInstrumento"
    ,"Nemo"
    ,"Boveda" 
    ,"IdCartera"      
    ,"Emisor"
    ,"TipoReajuste"
    ,ConsultarCartera."CodigoIFRS"
    ,"TipoCartera"
    ,"ClaseEmisor"
    ,COALESCE(ConsultarEjecutivo."CodigoEmpleado",'') AS "Agente"
    ,COALESCE(LEFT(ConsultarEjecutivo."NombreCompleto",60),'') AS "NombreAgente"
    ,COALESCE(ConsultarEjecutivo."CodigoSucursal",'') AS "SucursalAgente"
    ,COALESCE(LEFT(ConsultarEjecutivo."NombreSucursal",60),'') AS "NombreSucursalAgente"
    ,COALESCE(LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60),'') AS "CentroCostoAgente"
    ,CASE 
    	WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad" THEN 'CP'
        WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' THEN 'RE'
        ELSE 'NR'
     END AS "Relacion"
    ,"Bolsa"
    ,"FechaLiquidacion"
    ,ConsultarCliente."Tipo" AS "TipoPersona"
    ,"FechaOperacion"
    ,"IdConcepto"
    ,DocInd."DocumentoIdentidad" AS "DocumentoIdentidadCAVALI"
    ,"TipoEmisor"
    ,COALESCE(MES."Codigo",'')	AS "Mesa" 
FROM v_TabCarteras AS ConsultarCartera
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SoCNeg ON (ConsultarCartera."IdCliente" = SoCNeg."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocInd ON DocInd."IdPersona" = SoCNeg."IdPersona"   
	AND DocInd."IdTipoDocumentoIdentidad" = 'RUT'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio ON ConsultarPortafolio."IdPortafolio" = ConsultarCartera."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado ON ConsultarIndicadoresEmpleado."IdEmpleado" = ConsultarEjecutivo."IdEmpleadoEjecutivo" 
	AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente ON ConsultarIndicadoresCliente."IdCliente" = ConsultarCartera."IdCliente" 
	AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON ConsultarCartera."IdMesa" = MES."IdMesa"
