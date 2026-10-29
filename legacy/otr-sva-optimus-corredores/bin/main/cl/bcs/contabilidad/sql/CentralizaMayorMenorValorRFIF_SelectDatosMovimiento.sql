SELECT
     CVIFRF."IdValorizacion"                                 AS "IdMovimiento"
    ,CASE 
    	WHEN CVIFRF."Diferenciador" IN ('PP', 'PC', 'PD') 
            THEN CVIFRF."FolioOperacion"
    	ELSE CVIFRF."FolioCartera"
   	 END												AS "FolioOperacion"        
    ,LEFT(:p_DocumentoIdentidad,60)         			AS "DocumentoIdentidad"      
    ,LEFT(ConsultarPortafolio."CodigoPortafolio",60)    AS "Portafolio"
    ,CASE WHEN ConsultarEmpresa."DocumentoIdentidad" = :p_DocumentoIdentidad THEN 'SI' ELSE 'NO' END AS "EsCuentaPropia"
    ,CASE WHEN COALESCE(ConsultarIndicadoresCliente."ValorIndicador",'N') ='S' THEN 'SI' ELSE 'NO' END AS "EsRelacionado"
    ,CASE
        WHEN CVIFRF."Diferenciador" = 'CG'            THEN 'GARANTIA'
        WHEN CVIFRF."Diferenciador" = 'CD'            THEN 'LIBRE'
        WHEN CVIFRF."Diferenciador" = 'PC'            THEN 'VRC'
        WHEN CVIFRF."Diferenciador" = 'PP'            THEN 'VRC-CRV'
        WHEN CVIFRF."Diferenciador" = 'PD'            THEN 'CRV'
        WHEN CVIFRF."Diferenciador" = 'PR'          THEN 'PRESTAMO'
        WHEN CVIFRF."Diferenciador" = 'EM'          THEN 'EMPRESTITO'
        ELSE '-'
	END AS "TipoCartera"
    ,CVIFRF."FechaOperacion"                   		AS "FechaOperacion"
    ,CVIFRF."FechaVencimientoOperacion" 			AS "FechaVencimientoOperacion"
    ,CVIFRF."FechaLiquidacion"                   	AS "FechaLiquidacion"
    ,CVIFRF."IdClasificacionTipoInstrumento"      	AS "ClaseInstrumento"
    ,CVIFRF."IdTipoInstrumento"                   	AS "TipoInstrumento"
    ,CVIFRF."Nemotecnico" 							AS "Nemo"
    ,CVIFRF."IdBoveda"                            	AS "Boveda" 
    ,CVIFRF."Emisor"                            	AS "Emisor"
    ,TEMI."CodigoTipoEmisor" 						AS "TipoEmisor"
    ,CVIFRF."IdTipoReajuste"                      	AS "TipoReajuste"
    ,CVIFRF."FechaVencimientoInstrumento"
    ,CVIFRF."CodigoIFRS"                          	AS "CodigoIFRS"
    ,CVIFRF."IdMonedaInstrumento"                 	AS "Moneda"                                                                                                          
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" (CVIFRF."Emisor", CVIFRF."IdClasificacionTipoInstrumento",  CVIFRF."IdTipoInstrumento", CVIFRF."Nemotecnico", CVIFRF."IdTipoReajuste") AS "ClaseEmisor"
FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa ON ConsultarEmpresa."IdEmpresa" = CVIFRF."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio ON ConsultarPortafolio."IdPortafolio" = CVIFRF."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente  ON ConsultarIndicadoresCliente."IdCliente" =  CVIFRF."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
LEFT JOIN "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" TEMI ON TEMI."IdTipoInstrumento" = CVIFRF."IdTipoInstrumento"
			AND CVIFRF."IdTipoInstrumento" = TEMI."IdTipoInstrumento"
			AND ( 
				TEMI."Nemotecnico" != '' AND (TRIM(TEMI."Nemotecnico") = CVIFRF."Nemotecnico")
										 AND ((TEMI."IdEmisor" != '' AND TEMI."IdEmisor" = CVIFRF."Emisor") OR (TEMI."IdEmisor" = ''))					
				OR
				TEMI."Nemotecnico" = '' AND ((TEMI."IdEmisor" = CVIFRF."Emisor") OR (TEMI."IdEmisor" = ''))
			)
WHERE CVIFRF."IdEmpresa" = :p_IdEmpresa 
    AND CVIFRF."IdInforme" = :p_IdInforme;