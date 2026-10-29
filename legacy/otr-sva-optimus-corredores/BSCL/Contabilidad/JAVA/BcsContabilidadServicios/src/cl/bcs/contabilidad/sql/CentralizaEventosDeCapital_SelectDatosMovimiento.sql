SELECT
     EventoCapitalCliente."IdEventoCapitalCliente"          AS "IdMovimiento"
    ,EventoCapitalCliente."IdTipoEventoCapital"             AS "TipoEvento"
    ,YEAR(EventoCapitalCliente."FechaPago") ||'-'|| RIGHT('0' || MONTH(EventoCapitalCliente."FechaPago"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(EventoCapitalCliente."FechaPago"),2)
                                                            AS "FechaLiquidacion"                                        
    ,LEFT(EventoCapitalCliente."DocumentoIdentidad",60)     AS "DocumentoIdentidad"                                    
    ,LEFT(EventoCapitalCliente."NombreCliente",60)          AS "NombreCliente"                                       
    ,LEFT(ConsultarPortafolio."CodigoPortafolio",60)        AS "Portafolio"
    ,EventoCapitalCliente."IdMoneda"                        AS "Moneda"                                                      
    ,'EsCuentaMandante'                                     AS "EsCuentaMandante"
    ,'EsCuentaDepositante'                                  AS "EsCuentaDepositante"                                        
    --,EventoCapitalCliente."TipoSocioNegocio"              AS "TipoPersona"
    ,ConsultarPersona."Tipo"                                AS "TipoPersona"
    ,'CUS'                                                  AS "Producto"
    ,EventoCapitalCliente."IdClasificacionTipoInstrumento"  AS "ClaseInstrumento"
    ,EventoCapitalCliente."IdTipoInstrumento"               AS "TipoInstrumento"
    ,EventoCapitalCliente."Nemo"                            AS "Nemo"
    ,EventoCapitalCliente."TipoCartera"                     AS "TipoCartera"
    ,EventoCapitalCliente."IdBoveda"                        AS "Boveda"
    --,EventoCapitalCliente."CodigoUsuarioAgente"           AS "Agente"
    ,LEFT(EventoCapitalCliente."NombreAgente",60)           AS "NombreAgente"
    ,CASE WHEN (ConsultarSocioDeNegocio."IdContraparte" > 0 ) 
            THEN 'SI'
            ELSE 'NO' END                                   AS "EsContraparte"
    ,CASE WHEN EventoCapitalCliente."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
                        THEN 'CP'
            WHEN UPPER(ConsultarIndicadoresCliente."CodigoValorIndicador") = 'VINCULADO'     
                        THEN 'RE'
            ELSE    'NR' END                                AS "Relacion"
    ,EventoCapitalCliente."IdBolsa"                         AS "Bolsa"
    --,EventoCapitalCliente."CodigoSucursal"                AS "SucursalAgente"
    --,LEFT(ConsultarEjecutivo."NombreSucursal",60)         AS "NombreSucursalAgente"
    --,EventoCapitalCliente."CentroCosto"   AS "CentroCostoAgente"
    
    ,LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60) AS "CentroCostoAgente"
    ,cargoabono."IdConcepto"								AS "Concepto"
    ,IFNULL(CenCos."Business",'')                           AS "OpCentros"
    ,DocInd."DocumentoIdentidad"                            AS "DocumentoIdentidadCAVALI"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
        EventoCapitalCliente."IdClasificacionTipoInstrumento",
        EventoCapitalCliente."IdTipoInstrumento", 
        ConsultarInstrumento."Nemotecnico",  
        ConsultarInstrumento."IdEmisor" )       AS "TipoEmisor"
	,CASE cargoabono."IdTipoMovimiento" WHEN 'MOVCAR' THEN 'CARGO'
	     								WHEN 'MOVABN' THEN 'ABONO'
	     								ELSE '' END 		AS "TipoDocumento"
   ,COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') 	AS "ClienteBancario"

FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente"                 AS EventoCapitalCliente 
		ON EventoCapitalCliente."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" 								AS cuentainversion 
		ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"                         AS ConsultarInstrumento
        ON ConsultarInstrumento."IdInstrumento" = EventoCapitalCliente."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"                             AS ConsultarEmpresa
        ON ConsultarEmpresa."IdEmpresa" = EventoCapitalCliente."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"                             AS ConsultarPortafolio 
        ON ConsultarPortafolio."IdPortafolio" = EventoCapitalCliente."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"                           AS ConsultarCliente 
        ON ConsultarCliente."IdCliente" =  ConsultarPortafolio."IdCliente" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"                           AS ConsultarPersona
    ON ConsultarPersona."IdPersona" = ConsultarCliente."IdPersona"
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"                          AS DocInd 
    ON DocInd."IdPersona"                   = ConsultarPersona."IdPersona"   
    AND DocInd."IdTipoDocumentoIdentidad"   = 'RUT'             
--LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
--      ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarComprobante."IdEmpleadoEjecutivoPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"                    AS ConsultarSocioDeNegocio
ON (ConsultarSocioDeNegocio."IdEmpresa" = ConsultarEmpresa."IdEmpresa"  AND
    ConsultarSocioDeNegocio."IdPersona" =  ConsultarPersona."IdPersona")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"                  AS ConsultarIndicadoresCliente
        ON ConsultarIndicadoresCliente."IdCliente" =  EventoCapitalCliente."IdCliente" AND --ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
        ((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR 
	(ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"   AS IndClienteBancario
	ON IndClienteBancario."IdCliente" =  EventoCapitalCliente."IdCliente" AND IndClienteBancario."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
--new
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"                         AS ConsultarEjecutivo
        ON ConsultarEjecutivo."IdEmpleadoEjecutivo" =EventoCapitalCliente."IdEmpleadoEjecutivoPortafolio" -- ConsultarCartera."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"                 AS ConsultarIndicadoresEmpleado
        ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
--Centro Costo
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.CentroCosto"                                       AS CenCos
        ON (CenCos."IdEmpresa"  = :p_IdEmpresa AND CenCos."ProductoConcepto" = '' AND
            CenCos."Relacionado"= CASE EventoCapitalCliente."DocumentoIdentidad" 
                                    WHEN :p_DocumentoIdentidadCorredora
                                    THEN 'CP' 
                                    ELSE 'CL' 
                                    END AND 
            CenCos."IdBolsa" = EventoCapitalCliente."IdBolsa" AND 
            CenCos."TipoCentro" = 'INGRESO')
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdTipoProducto" = 'EVCA'
AND cargoabono."IdEstado" = 'CRT'
AND EventoCapitalCliente."IdTipoEventoCapital" NOT IN ('CC', 'SL', 'DA')
AND cargoabono."IdEmpresa" = :p_IdEmpresa
AND cargoabono."IdModulo" = 'CUS';
