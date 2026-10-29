SELECT
     CVIFRF."IdValorizacion"                           		AS "IdMovimiento"        
    ,LEFT(:p_DocumentoIdentidad,60)         			AS "DocumentoIdentidad"                                    
    ,LEFT(ConsultarCliente."NombreCompleto",60)			AS "NombreCliente"                                       
    ,LEFT(ConsultarPortafolio."CodigoPortafolio",60)    AS "Portafolio"
    ,CVIFRF."IdMonedaInstrumento"                 		AS "Moneda"
    ,CVIFRF."IdTipoProducto"   							AS "Producto"
    ,CVIFRF."IdClasificacionTipoInstrumento"      		AS "ClaseInstrumento"
    ,CVIFRF."IdTipoInstrumento"                   		AS "TipoInstrumento"
    ,CVIFRF."Nemotecnico"    					AS "Nemo"
    ,CVIFRF."IdBoveda"                            		AS "Boveda" 
    ,CAST(CVIFRF."IdCartera" AS NVARCHAR(60))    	 	AS "IdCartera"      
    ,CVIFRF."Emisor"                            		AS "Emisor"
    ,CVIFRF."CodigoIFRS"                          		AS "CodigoIFRS"
    ,'SIMULTANEA'                                       AS "TipoCartera"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" (CVIFRF."Emisor", CVIFRF."IdClasificacionTipoInstrumento", CVIFRF."IdTipoInstrumento", CVIFRF."Nemotecnico", CVIFRF."IdTipoReajuste") AS "ClaseEmisor"
    ,ConsultarEjecutivo."CodigoEmpleado"                    AS "Agente"
    ,LEFT(ConsultarEjecutivo."NombreCompleto",60)           AS "NombreAgente"
    ,ConsultarEjecutivo."CodigoSucursal"                    AS "SucursalAgente"
    ,LEFT(ConsultarEjecutivo."NombreSucursal",60)           AS "NombreSucursalAgente"
    ,LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60) AS "CentroCostoAgente"
    ,CASE WHEN ConsultarEmpresa."DocumentoIdentidad" = :p_DocumentoIdentidad
            THEN 'CP'
          WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S'   
            THEN 'RE'
          ELSE  'NR'
          END                                           AS "Relacion"
    ,CVIFRF."IdBolsa"                   		AS "Bolsa"
    ,CVIFRF."FechaLiquidacion"                    		AS "FechaLiquidacion"
    ,ConsultarCliente."Tipo"                            AS "TipoPersona"
    ,CVIFRF."FechaOperacion"                   			AS "FechaOperacion"
    ,CVIFRF."IdConcepto"          			AS "IdConcepto"
    ,COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') 	AS "ClienteBancario"
FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" ConsultarCarteraSubyacenteBasica
        ON ConsultarCarteraSubyacenteBasica."FolioCarteraRelacionada" = CVIFRF."FolioCartera"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
        ON ConsultarCliente."IdCliente" = CVIFRF."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
        ON ConsultarEmpresa."IdEmpresa" = CVIFRF."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
        ON ConsultarPortafolio."IdPortafolio" = CVIFRF."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
        ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = CVIFRF."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
        ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
        ON ConsultarIndicadoresCliente."IdCliente" =  CVIFRF."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"   AS IndClienteBancario
	ON IndClienteBancario."IdCliente" =  CVIFRF."IdCliente" AND IndClienteBancario."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
WHERE CVIFRF."IdEmpresa" = :p_IdEmpresa 
    AND CVIFRF."IdInforme" = :p_IdInforme;