SELECT
"IdClasificacionTipoInstrumento"
,"IdTipoInstrumento"
,"IdInstrumento"
,"Nemotecnico"
,"IdEmisor"
,"IdCliente"
,"IdPortafolio"
,"IdBoveda"
,"Cantidad"
, "ValorCompra"	
, "MontoCompra"
, "IdRegimenFondoMutuo"
, "IdPersona"
, "NombreClasificacionTipoInstrumento"
, "NombreTipoInstrumento"
, "DocumentoIdentidad"
, "RazonSocial"
, "CodigoPortafolio"
, "IdSocioDeNegocio"
, "IdMatrizAgrupador"
, "EsADC"
FROM
(
	SELECT
		RANK() over (partition by  CAR."FolioCartera" order by CAR."IdCartera" desc) as RANK
	   ,CAR."IdClasificacionTipoInstrumento"
	   ,INS."NombreClasificacionTipoInstrumento"
	   ,CAR."IdTipoInstrumento"
	   ,INS."NombreTipoInstrumento"
	   ,CAR."IdInstrumento"
	   ,CAR."Nemotecnico" as "Nemotecnico"
	   ,CAR."IdEmisor"
	   ,CAR."IdCliente"
	   ,CAR."IdPortafolio"
	   ,CAR."IdBoveda" 
	   ,CAST(IFNULL(CAR."INGCantidad",0) as DECIMAL(26,8)) as "Cantidad"
	   ,CAR."INGMontoCompraActualMonedaInstrumento" as "ValorCompra"
	   ,CAR."INGMontoATasaCompraActual" as "MontoCompra"					
	   ,CAR."IdRegimenFondoMutuo"
	   ,CLI."IdPersona"
	   ,CLI."DocumentoIdentidad"
	   ,CLI."RazonSocial"
	   ,Portafolio."CodigoPortafolio"
	   ,SOC."IdSocioDeNegocio"
	   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
	   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
       ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
		FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as INS on CAR."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdCliente" = CAR."IdCliente")
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as SOC ON CLI."IdPersona" = SOC."IdPersona"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS Serie ON Serie."IdInstrumento" = CAR."IdInstrumento" AND Serie."IdEmpresa" = :IdEmpresa
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
		WHERE 
			CAR."IdEmpresa" = :IdEmpresa
		AND CAR."FechaRespaldo" = :FechaConsulta
		AND	CAR."IdCliente" = CASE WHEN :IdCliente > 0 THEN :IdCliente ELSE CAR."IdCliente" END
		AND	CAR."IdPortafolio" = CASE WHEN :IdPortafolio > 0 THEN :IdPortafolio ELSE CAR."IdPortafolio" END
		AND CAR."IdClasificacionTipoInstrumento" = 'FM'
		AND	CAR."IdTipoInstrumento" = 'FFM'
		AND	CAR."IdInstrumento" = CASE WHEN IFNULL(:IdInstrumento,0) > 0 THEN :IdInstrumento ELSE CAR."IdInstrumento" END
		AND
		(
			(:TipoFechaConsulta = 'LIQ' AND (
				:FechaConsulta BETWEEN CAR."INGFechaLiquidacion" and CASE WHEN IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' THEN '2300-12-31' ELSE IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') END
				AND
				:FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end	   
			))
			OR
			(:TipoFechaConsulta = 'ADQ' AND (
				:FechaConsulta BETWEEN CAR."INGFechaOperacion" and CASE WHEN IFNULL(CAR."INGFechaOperacion",'0001-01-01') = '0001-01-01' THEN '2300-12-31' ELSE IFNULL(CAR."EGRFechaOperacion",'0001-01-01') END
				AND
				:FechaConsulta < CASE WHEN IFNULL("EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' ELSE IFNULL(CAR."EGRFechaOperacion",'0001-01-01') END	
			))
		)
		AND	CAR."IdEstado" <> 'ANU'
		AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		AND (:IdEmisor IS NULL OR CAR."IdEmisor" = :IdEmisor)
		AND	Serie."IdFamiliaFondo" = CASE WHEN IFNULL(:IdFamiliaFondo,'') <> '' THEN IFNULL(:IdFamiliaFondo,'') ELSE Serie."IdFamiliaFondo" END
)
WHERE RANK = 1

UNION ALL 

SELECT Instrumento."IdClasificacionTipoInstrumento"	   
	   ,Instrumento."IdTipoInstrumento"	   
	   ,SerieFondo."IdInstrumento"
	   ,Solicitudes."IdSerieFondo" as "Nemotecnico"
	   ,Instrumento."IdEmisor"
	   ,Solicitudes."IdCliente"
	   ,Solicitudes."IdPortafolio"
	   ,'' AS "IdBoveda" 
	   ,'0' as "Cantidad"
	   ,Solicitudes."ValorCuota" as "ValorCompra"
	   ,Solicitudes."Monto" as "MontoCompra"					
	   ,Solicitudes."IdRegimenTributario" AS "IdRegimenFondoMutuo"
	   ,Cliente."IdPersona"
	   ,Instrumento."NombreClasificacionTipoInstrumento"
	   ,Instrumento."NombreTipoInstrumento"
	   ,Cliente."DocumentoIdentidad"
	   ,Cliente."RazonSocial"
	   ,Portafolio."CodigoPortafolio"
	   ,SocioNegocio."IdSocioDeNegocio"
	   ,CASE 
	      WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) 
	    ELSE COALESCE(MatrizAgrupadorNoAPV."IdMatrizAgrupadorNOAPV",0) 
	    END AS "IdMatrizAgrupador"
	   ,CASE 
	      WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') 
	    ELSE COALESCE(MatrizAgrupadorNoAPV."EsADC",'N') 
	    END AS "EsADC" 	    
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS Solicitudes
  LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieFondo
    ON SerieFondo."IdSerieFondo" = Solicitudes."IdSerieFondo" 
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cliente
    ON Cliente."IdCliente" = Solicitudes."IdCliente" 
   AND Cliente."IdEmpresa" = :IdEmpresa 
  LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio
    ON Portafolio."IdPortafolio" = Solicitudes."IdPortafolio"
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SocioNegocio 
    ON SocioNegocio."IdCliente" = Solicitudes."IdCliente" 
   AND SocioNegocio."IdEmpresa" = :IdEmpresa 
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS Instrumento 
    ON Instrumento."IdInstrumento" = SerieFondo."IdInstrumento" 
  LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador
    ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
  LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador 
    ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador" 
  LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MatrizAgrupadorNoAPV 
    ON MatrizAgrupadorNoAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" 
   AND Portafolio."IdMatrizAgrupadorAPV" = 0  
WHERE Solicitudes."IdEmpresa" = :IdEmpresa
  AND Solicitudes."IdCliente" = CASE WHEN :IdCliente > 0 THEN :IdCliente ELSE Solicitudes."IdCliente" END
  AND Solicitudes."IdPortafolio" = CASE WHEN :IdPortafolio > 0 THEN :IdPortafolio ELSE Solicitudes."IdPortafolio" END
  AND Solicitudes."Estado" IN ('ING', 'PEN')
  AND Solicitudes."FechaAceptacion" <= :FechaConsulta
  AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
  AND Solicitudes."IdSerieFondo" NOT IN (SELECT Cartera."Nemotecnico"
                             FROM "BSCL.Custodia.BD::CUS.Cartera" AS Cartera 
                             WHERE Cartera."IdEmpresa" = Solicitudes."IdEmpresa"
                               AND Cartera."IdCliente" = Solicitudes."IdCliente"
                               AND ((:TipoFechaConsulta = 'LIQ' AND (:FechaConsulta BETWEEN Cartera."INGFechaLiquidacion" AND CASE 
                                                                                                                            WHEN IFNULL(Cartera."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' THEN '2300-12-31' 
                                                                                                                          ELSE IFNULL(Cartera."EGRFechaLiquidacion",'0001-01-01') 
                                                                                                                          END
                                                                                                                      AND :FechaConsulta < CASE 
                                                                                                                                             WHEN IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' THEN '2300-12-31' 
                                                                                                                                           ELSE IFNULL(Cartera."EGRFechaLiquidacion",'0001-01-01') 
                                                                                                                                           END
                                   ))
                               OR (:TipoFechaConsulta = 'ADQ' AND (	:FechaConsulta BETWEEN Cartera."INGFechaOperacion" AND CASE
                                                                                                                         WHEN IFNULL(Cartera."INGFechaOperacion",'0001-01-01') = '0001-01-01' THEN '2300-12-31' 
                                                                                                                       ELSE IFNULL(Cartera."EGRFechaOperacion",'0001-01-01') 
                                                                                                                       END
                                                                                                                   AND :FechaConsulta < CASE 
                                                                                                                                          WHEN IFNULL("EGRFechaOperacion",'0001-01-01') = '0001-01-01' THEN '2300-12-31' 
                                                                                                                                        ELSE IFNULL(Cartera."EGRFechaOperacion",'0001-01-01') 
                                                                                                                                        END
                                   )))
                            );
