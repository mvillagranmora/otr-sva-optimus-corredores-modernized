WITH v_TabCarteras AS(
	SELECT
	     CAR."IdCartera"                       AS "IdMovimiento"
	    ,Car."IdEmpresa" 
	    ,CAR."IdCliente"    
	    ,CAR."IdPortafolio" 
	    ,CAR."IdAgente" 
		,CAR."IdMesa" 
	    ,LEFT(CP."DocumentoIdentidad",60)     AS "DocumentoIdentidad"                                                   
	    ,LEFT(CP."CodigoPortafolio",60)       AS "Portafolio"
	    ,CAR."IdMonedaInstrumento"             AS "Moneda"                                                                                                        
	    ,CAR."IdTipoProducto"                  AS "Producto"
	    ,CAR."IdClasificacionTipoInstrumento"  AS "ClaseInstrumento"
	    ,CAR."IdTipoInstrumento"               AS "TipoInstrumento"
	    ,CAR."Nemotecnico"                     AS "Nemo"
	    ,CAR."IdBoveda"                        AS "Boveda" 
	    ,CAST(CAR."IdCartera" AS NVARCHAR(60)) AS "IdCartera"      
	    ,CAR."IdEmisor"                        AS "Emisor"
	    ,CAR."IdTipoReajuste"                  AS "TipoReajuste"
	    ,CAR."CodigoIFRS"                      AS "CodigoIFRS"
	    ,'CUSTODIA'                                         AS "TipoCartera"
	    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
	    (CAR."IdEmisor", CAR."IdClasificacionTipoInstrumento", 
	     CAR."IdTipoInstrumento", CAR."Nemotecnico", CAR."IdTipoReajuste")
	                                                        AS "ClaseEmisor"
	    ,CAR."IdBolsa"                         AS "Bolsa"
	    ,CASE WHEN CAR."EGRCantidad" = 0 THEN CAR."INGFechaLiquidacion"
	                                ELSE CAR."EGRFechaLiquidacion" END
	                                                                AS "FechaLiquidacion"
	    ,CASE WHEN CAR."EGRCantidad" = 0 THEN CAR."INGFechaOperacion"
	                                ELSE CAR."EGRFechaOperacion" END
	                                                                AS "FechaOperacion"
	    ,CAR."IdConcepto"                              AS "IdConcepto"
	    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
	        CAR."IdClasificacionTipoInstrumento",
	        CAR."IdTipoInstrumento", 
	        CAR."Nemotecnico",  
	        CAR."IdEmisor" )       AS "TipoEmisor"
	     ,COALESCE(DET."Fase",0) AS "Fase"
	FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio"
	INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = CAR."IdEmisor" AND DET."IdEstado" = 'ACT'
	WHERE CAR."IdEmpresa" = :p_IdEmpresa
	AND   CAR."IdEstado" = 'VIG'
	AND   CAR."IdTipoOperacion" IN ('COMRF','INGTIT')
	AND   CAR."IdTipoProducto" IN( 'INRF','CUST') 
	AND   CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
	AND   DET."FlagDeterioro" = 'S'
	AND   DET."PorcentajeDeterioro" > 0
	AND   CAR."FechaOperacion" <=:p_FechaContable
	AND   CP."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora

	UNION ALL 
	
	SELECT
	     CAR."IdCartera"                       AS "IdMovimiento"
	    ,Car."IdEmpresa" 
	    ,CAR."IdCliente"    
	    ,CAR."IdPortafolio" 
	    ,CAR."IdAgente" 
		,CAR."IdMesa" 
	    ,LEFT(CP."DocumentoIdentidad",60)     AS "DocumentoIdentidad"                                                   
	    ,LEFT(CP."CodigoPortafolio",60)       AS "Portafolio"
	    ,CAR."IdMonedaInstrumento"             AS "Moneda"                                                                                                        
	    ,CAR."IdTipoProducto"                  AS "Producto"
	    ,CAR."IdClasificacionTipoInstrumento"  AS "ClaseInstrumento"
	    ,CAR."IdTipoInstrumento"               AS "TipoInstrumento"
	    ,CAR."Nemotecnico"                     AS "Nemo"
	    ,CAR."IdBoveda"                        AS "Boveda" 
	    ,CAST(CAR."IdCartera" AS NVARCHAR(60)) AS "IdCartera"      
	    ,CAR."IdEmisor"                        AS "Emisor"
	    ,CAR."IdTipoReajuste"                  AS "TipoReajuste"
	    ,CAR."CodigoIFRS"                      AS "CodigoIFRS"
	    ,'CUSTODIA'                                         AS "TipoCartera"
	    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
	    (CAR."IdEmisor", CAR."IdClasificacionTipoInstrumento", 
	     CAR."IdTipoInstrumento", CAR."Nemotecnico", CAR."IdTipoReajuste")
	                                                        AS "ClaseEmisor"
	    ,CAR."IdBolsa"                         AS "Bolsa"
	    ,CASE WHEN CAR."EGRCantidad" = 0 THEN CAR."INGFechaLiquidacion"
	                                ELSE CAR."EGRFechaLiquidacion" END
	                                                                AS "FechaLiquidacion"
	    ,CASE WHEN CAR."EGRCantidad" = 0 THEN CAR."INGFechaOperacion"
	                                ELSE CAR."EGRFechaOperacion" END
	                                                                AS "FechaOperacion"
	    ,CAR."IdConcepto"                              AS "IdConcepto"
	    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
	        CAR."IdClasificacionTipoInstrumento",
	        CAR."IdTipoInstrumento", 
	        CAR."Nemotecnico",  
	        CAR."IdEmisor" )       AS "TipoEmisor"
	     ,COALESCE(DET."Fase",0) AS "Fase"
	FROM "BSCL.Mesa.BD::MES.Operacion" AS OPE
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."IdOperacion" = OPE."IdOperacion"
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = RES."IdReserva"
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio" 
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CPO ON CPO."IdPortafolio" = OPE."IdPortafolioCorredor" 
	INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = CAR."IdEmisor" AND DET."IdEstado" = 'ACT'
	WHERE CAR."IdEmpresa" = :p_IdEmpresa
	AND   OPE."IdEstado" = 'VIG'
	AND   CAR."IdEstado" = 'VIG'
	AND   CAR."IdTipoOperacion" IN ('COMTER')
	AND   CAR."IdTipoProducto" = 'MERF'
	AND   CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
	AND   CAR."IdTipoInstrumento" != 'PAC'
	AND   DET."FlagDeterioro" = 'S'
	AND   DET."PorcentajeDeterioro" > 0
	AND   CAR."FechaOperacion" <=:p_FechaContable
	AND   CPO."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
	
	UNION ALL
	
	SELECT
	     CAR."IdCartera"                       AS "IdMovimiento"
	    ,Car."IdEmpresa" 
	    ,CAR."IdCliente"    
	    ,CAR."IdPortafolio" 
	    ,CAR."IdAgente" 
		,CAR."IdMesa" 
	    ,LEFT(CP."DocumentoIdentidad",60)     AS "DocumentoIdentidad"                                                   
	    ,LEFT(CP."CodigoPortafolio",60)       AS "Portafolio"
	    ,CAR."IdMonedaInstrumento"             AS "Moneda"                                                                                                        
	    ,CAR."IdTipoProducto"                  AS "Producto"
	    ,CAR."IdClasificacionTipoInstrumento"  AS "ClaseInstrumento"
	    ,CAR."IdTipoInstrumento"               AS "TipoInstrumento"
	    ,CAR."Nemotecnico"                     AS "Nemo"
	    ,CAR."IdBoveda"                        AS "Boveda" 
	    ,CAST(CAR."IdCartera" AS NVARCHAR(60)) AS "IdCartera"      
	    ,CAR."IdEmisor"                        AS "Emisor"
	    ,CAR."IdTipoReajuste"                  AS "TipoReajuste"
	    ,CAR."CodigoIFRS"                      AS "CodigoIFRS"
	    ,'CUSTODIA'                                         AS "TipoCartera"
	    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
	    (CAR."IdEmisor", CAR."IdClasificacionTipoInstrumento", 
	     CAR."IdTipoInstrumento", CAR."Nemotecnico", CAR."IdTipoReajuste")
	                                                        AS "ClaseEmisor"
	    ,CAR."IdBolsa"                         AS "Bolsa"
	    ,CASE WHEN CAR."EGRCantidad" = 0 THEN CAR."INGFechaLiquidacion"
	                                ELSE CAR."EGRFechaLiquidacion" END
	                                                                AS "FechaLiquidacion"
	    ,CASE WHEN CAR."EGRCantidad" = 0 THEN CAR."INGFechaOperacion"
	                                ELSE CAR."EGRFechaOperacion" END
	                                                                AS "FechaOperacion"
	    ,CAR."IdConcepto"                              AS "IdConcepto"
	    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
	        CAR."IdClasificacionTipoInstrumento",
	        CAR."IdTipoInstrumento", 
	        CAR."Nemotecnico",  
	        CAR."IdEmisor" )       AS "TipoEmisor"
	    ,COALESCE(DET."Fase",0) AS "Fase"
	FROM "BSCL.Mesa.BD::MES.Operacion" AS OPE  
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."IdOperacion" = OPE."IdOperacion"
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = RES."IdReserva"
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio" 
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CPO ON CPO."IdPortafolio" = OPE."IdPortafolioCorredor" 
	INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = CAR."IdEmisor" AND DET."IdEstado" = 'ACT'
	WHERE CAR."IdEmpresa" = :p_IdEmpresa
	AND   OPE."IdEstado" = 'VIG'
	AND   CAR."IdEstado" IN ('VIG','RPA')
	AND   CAR."IdTipoOperacion" IN ('VENPAC')
	AND   CAR."IdTipoProducto" = 'MERF'
	AND   CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
	AND   CAR."IdTipoInstrumento" != 'PAC'
	AND   DET."FlagDeterioro" = 'S'
	AND   DET."PorcentajeDeterioro" > 0
	AND   CAR."FechaOperacion" <=:p_FechaContable
	AND   CPO."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora

	UNION ALL
	
	SELECT
	     GAR."IdGarantia"                       AS "IdMovimiento"
	    ,GAR."IdEmpresa" 
	    ,GAR."IdCliente"    
	    ,GAR."IdPortafolio" 
	    ,GAR."IdAgente" 
		,CAR."IdMesa" 
	    ,LEFT(CP."DocumentoIdentidad",60)     AS "DocumentoIdentidad"                                                   
	    ,LEFT(CP."CodigoPortafolio",60)       AS "Portafolio"
	    ,GAR."IdMonedaInstrumento"             AS "Moneda"                                                                                                        
	    ,CAR."IdTipoProducto"                  AS "Producto"
	    ,GAR."IdClasificacionTipoInstrumento"  AS "ClaseInstrumento"
	    ,GAR."IdTipoInstrumento"               AS "TipoInstrumento"
	    ,GAR."Nemotecnico"                     AS "Nemo"
	    ,CAR."IdBoveda"                        AS "Boveda" 
	    ,CAST(CAR."IdCartera" AS NVARCHAR(60)) AS "IdCartera"      
	    ,GAR."IdEmisor"                        AS "Emisor"
	    ,GAR."IdTipoReajuste"                  AS "TipoReajuste"
	    ,GAR."CodigoIFRS"                      AS "CodigoIFRS"
	    ,'CUSTODIA'                                         AS "TipoCartera"
	    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" 
	    (GAR."IdEmisor", GAR."IdClasificacionTipoInstrumento", 
	     GAR."IdTipoInstrumento", GAR."Nemotecnico", GAR."IdTipoReajuste")
	                                                        AS "ClaseEmisor"
	    ,CAR."IdBolsa"                         AS "Bolsa"
	    ,CASE WHEN CAR."EGRCantidad" = 0 THEN GAR."INGFechaLiquidacion"
	                                ELSE GAR."EGRFechaLiquidacion" END
	                                                                AS "FechaLiquidacion"
	    ,CASE WHEN CAR."EGRCantidad" = 0 THEN CAR."INGFechaOperacion"
	                                ELSE GAR."EGRFechaOperacion" END
	                                                                AS "FechaOperacion"
	    ,GAR."IdConcepto"                              AS "IdConcepto"
	    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
	        GAR."IdClasificacionTipoInstrumento",
	        GAR."IdTipoInstrumento", 
	        GAR."Nemotecnico",  
	        GAR."IdEmisor" )       AS "TipoEmisor"
	    ,COALESCE(DET."Fase",0) AS "Fase"
	FROM  "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"  AS GAR
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = GAR."IdPortafolio" 
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."FolioCartera" = GAR."FolioCarteraOrigen" AND CAR."IdEstado" = 'VIG'
	INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = GAR."IdEmisor" AND DET."IdEstado" = 'ACT'
	WHERE GAR."IdEmpresa" = :p_IdEmpresa
	AND   GAR."IdEstado"='VIG' 
	AND GAR."IdClasificacionTipoInstrumento" in ('RF','IF')
	AND   DET."FlagDeterioro" = 'S'
	AND   DET."PorcentajeDeterioro" > 0
	AND GAR."FechaOperacion" <=:p_FechaContable
	AND CP."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
 
	)
SELECT 
	     "IdMovimiento"        
	    ,ConsultarCliente."DocumentoIdentidad"                                    
	    ,LEFT(ConsultarCliente."NombreCompleto",60)         AS "NombreCliente"                                       
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
	    ,COALESCE(ConsultarEjecutivo."CodigoEmpleado",'')                       AS "Agente"
	    ,COALESCE(LEFT(ConsultarEjecutivo."NombreCompleto",60),'')              AS "NombreAgente"
	    ,COALESCE(ConsultarEjecutivo."CodigoSucursal",'')                       AS "SucursalAgente"
	    ,COALESCE(LEFT(ConsultarEjecutivo."NombreSucursal",60),'')              AS "NombreSucursalAgente"
	    ,COALESCE(LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60),'')    AS "CentroCostoAgente"
	    ,CASE WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
	                        THEN 'CP'
	            WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S'     
	                        THEN 'RE'
	            ELSE    'NR'
	          END                                           AS "Relacion"
	    ,"Bolsa"
	    ,"FechaLiquidacion"
	    ,ConsultarCliente."Tipo"                                  AS "TipoPersona"
	    ,"FechaOperacion"
	    ,"IdConcepto"
	    ,DocInd."DocumentoIdentidad"                            AS "DocumentoIdentidadCAVALI"
	    ,"TipoEmisor"
	    ,COALESCE(MES."Codigo",'')	AS "Mesa"
	    ,"Fase"
FROM v_TabCarteras AS ConsultarCartera
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
        ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
        
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"        AS SoCNeg
ON (ConsultarCartera."IdCliente" = SoCNeg."IdCliente")

LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"              AS DocInd 
ON DocInd."IdPersona"                   = SoCNeg."IdPersona"   
AND DocInd."IdTipoDocumentoIdentidad"   = 'RUT'

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
        ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
        ON ConsultarPortafolio."IdPortafolio" = ConsultarCartera."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
        ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
        ON ConsultarIndicadoresEmpleado."IdEmpleado" =  ConsultarEjecutivo."IdEmpleadoEjecutivo" AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
        ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarCartera."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON ConsultarCartera."IdMesa" = MES."IdMesa"
