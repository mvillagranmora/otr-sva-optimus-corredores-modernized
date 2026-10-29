SELECT
	Solicitudes."IdSolicitudAporteRescate" 			AS "IdMovimiento"		 
	,CASE 
	   WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP') THEN LEFT(Solicitudes."DocumentoIdentidadDes",60)	
	 ELSE LEFT(Solicitudes."DocumentoIdentidad",60)	
	 END                                                AS "DocumentoIdentidad"                                    
	,CASE 
	   WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP') THEN LEFT(Solicitudes."NombreCompletoDes",60)
	 ELSE LEFT(Solicitudes."NombreCompleto",60)
	 END                                                AS "NombreCliente"                                       
	,CASE 
	   WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP') THEN LEFT(Solicitudes."CodigoPortafolioDes",60)
	 ELSE LEFT(Solicitudes."CodigoPortafolio",60)
	 END                                                AS "Portafolio" 
	,Solicitudes."IdMoneda"								AS "Moneda"                                                      
	,Solicitudes."IdTipoProducto"						AS "Producto"
	,Solicitudes."IdClasificacionTipoInstrumento" 		AS "ClaseInstrumento"
	,Solicitudes."IdTipoInstrumento" 					AS "TipoInstrumento"
	,CASE 
	   WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESCAM') THEN "IdSerieFondoDestino"
	 ELSE Solicitudes."IdSerieFondo" 
	 END                                                AS "Nemo"
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
     END                                                AS "Relacion"
	,''													AS "Bolsa"
	,Solicitudes."FechaCertificacion"					AS "FechaOperacion"
	,CASE 
	   WHEN Solicitudes."IdMoneda" = Solicitudes."IdMonedaFondo" THEN TO_DECIMAL(0,26,8) 
	 ELSE Solicitudes."MontoMonedaSistema" 
	 END AS "MontoMonedaSistema"
	,''													AS "CentroCostoAgente"
    ,CASE 	   
	   WHEN Solicitudes."IdTipoMovimientoFondoMutuo" = 'RESTRASP' THEN 'APOTRASP'
	 ELSE Solicitudes."IdTipoMovimientoFondoMutuo"
	 END                                                AS "TipoOperacionFFMM"
	 
	 ,Solicitudes."IdFondo"
	 ,COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') AS "ClienteBancario"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" AS Emp
    ON (Emp."IdEmpresa" = Solicitudes."IdEmpresa")
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS IndCli
    ON IndCli."IdCliente" =  Solicitudes."IdCliente" 
   AND IndCli."CodigoTipoIndicador"  = 'RELCORR'
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndClienteBancario	
	ON IndClienteBancario."IdCliente" =  Solicitudes."IdCliente"
	AND IndClienteBancario."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
  AND (Solicitudes."FechaCertificacion" = :FechaContable)
  AND (Solicitudes."TipoSolicitud" = 'APO' OR Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP', 'RESCAM')) 
  AND Solicitudes."Estado" = 'CRT'
  AND :DocumentoIdentidadCorredora != CASE 
                                          WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP') THEN Solicitudes."DocumentoIdentidadDes"
                                      ELSE Solicitudes."DocumentoIdentidad"
                                      END
  AND Solicitudes."IdTipoMovimientoFondoMutuo" NOT IN ('RESTRANSF');                                    