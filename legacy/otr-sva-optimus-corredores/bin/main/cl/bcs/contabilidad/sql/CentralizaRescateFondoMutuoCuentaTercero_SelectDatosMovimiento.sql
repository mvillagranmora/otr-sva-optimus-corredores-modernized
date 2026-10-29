SELECT
	Solicitudes."IdSolicitudAporteRescate" 			AS "IdMovimiento"		 
	,LEFT(Solicitudes."DocumentoIdentidad",60)			AS "DocumentoIdentidad"                                    
	,LEFT(Solicitudes."NombreCompleto",60)				AS "NombreCliente"                                       
	,LEFT(Solicitudes."CodigoPortafolio",60)			AS "Portafolio"
	,Solicitudes."IdMoneda"								AS "Moneda"                                                      
	,Solicitudes."IdTipoProducto"						AS "Producto"
	,Solicitudes."IdClasificacionTipoInstrumento" 		AS "ClaseInstrumento"
	,Solicitudes."IdTipoInstrumento" 					AS "TipoInstrumento"
	,Solicitudes."IdSerieFondo"							AS "Nemo"
	,''													AS "Boveda"	
	,'0'												AS "IdCartera"		
	,Solicitudes."IdEmisor"								AS "Emisor"
	,''													AS "TipoReajuste"
	,''													AS "CodigoIFRS"
	,Solicitudes."Agente" 		    					AS "Agente"
	,LEFT(Solicitudes."NombreAgente",60) 				AS "NombreAgente"
	,Solicitudes."SucursalAgente" 						AS "SucursalAgente"
	,LEFT(Solicitudes."NombreSucursalAgente",60) 		AS "NombreSucursalAgente"
	,CASE 
       WHEN Solicitudes."DocumentoIdentidad" = Emp."DocumentoIdentidad" THEN 'CP'
       WHEN IndCli."ValorIndicador" = 'S' THEN 'RE'
    ELSE 'NR'
    END                                                 AS "Relacion"
    ,''													AS "Bolsa"
    ,Solicitudes."FechaCertificacion"					AS "FechaOperacion"
    ,''													AS "CentroCostoAgente"
    ,CASE 
       WHEN Solicitudes."IdMoneda" = Solicitudes."IdMonedaFondo" THEN TO_DECIMAL(0,26,8) 
     ELSE Solicitudes."MontoMonedaSistema" 
     END AS "MontoMonedaSistema"

     ,Solicitudes."IdTipoMovimientoFondoMutuo"          AS "TipoOperacionFFMM"
     

     ,Solicitudes."IdFondo"
     , COALESCE(MedioDePago."Nombre", '') AS "FormaPago"
     ,COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') AS "ClienteBancario"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" AS Emp
    ON (Emp."IdEmpresa" = Solicitudes."IdEmpresa")
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"  AS IndCli
    ON IndCli."IdCliente" =  Solicitudes."IdCliente" 
   AND IndCli."CodigoTipoIndicador"  = 'RELCORR'
  LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MedioDePago ON (MedioDePago."IdMedioDePago" = Solicitudes."IdMedioDePago") 
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndClienteBancario	
	  ON IndClienteBancario."IdCliente" =  Solicitudes."IdCliente"
	  AND IndClienteBancario."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
  AND (Solicitudes."FechaCertificacion" = :FechaContable )
  AND Solicitudes."TipoSolicitud" = 'RES'
  AND Solicitudes."Estado" = 'CRT'
  AND Solicitudes."DocumentoIdentidad" != :DocumentoIdentidadCorredora