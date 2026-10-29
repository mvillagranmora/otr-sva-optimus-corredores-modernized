SELECT
GAR."IdClasificacionTipoInstrumento"
,INS."NombreClasificacionTipoInstrumento"
,GAR."IdTipoInstrumento"
,INS."NombreTipoInstrumento"
,GAR."IdInstrumento"
,GAR."Nemotecnico"
,GAR."IdEmisor"
,GAR."IdCliente"
,GAR."IdPortafolio"
,CAR."IdBoveda" as "IdBoveda"
,cast(IFNULL(GAR."INGCantidad",0) as DECIMAL(26,8)) as "Cantidad"
,CAR."IdRegimenFondoMutuo"
,CLI."IdPersona"
,CLI."DocumentoIdentidad"
,CLI."RazonSocial"
,Portafolio."CodigoPortafolio"
,SOC."IdSocioDeNegocio"
,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
FROM "BSCL.Custodia.BD::HIS.Garantia" AS GAR
INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen" AND CAR."FechaRespaldo" = GAR."FechaRespaldo"
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as INS on CAR."IdInstrumento" = INS."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdCliente" = GAR."IdCliente")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as SOC ON CLI."IdPersona" = SOC."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS Serie ON Serie."IdInstrumento" = GAR."IdInstrumento" AND Serie."IdEmpresa" = :IdEmpresa
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
WHERE
	GAR."IdEmpresa" = :IdEmpresa
AND GAR."FechaRespaldo" = :FechaConsulta
AND GAR."IdCliente" = CASE WHEN :IdCliente > 0 THEN :IdCliente ELSE GAR."IdCliente" END
AND GAR."IdPortafolio" = CASE WHEN :IdPortafolio > 0 THEN :IdPortafolio ELSE GAR."IdPortafolio" END
AND GAR."IdEstado" <> 'ANU'
AND :FechaConsulta BETWEEN GAR."INGFechaLiquidacion" and CASE WHEN IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' THEN '2300-12-31' ELSE IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') END
AND :FechaConsulta < CASE WHEN IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' THEN '2300-12-31' ELSE IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') END
AND GAR."IdClasificacionTipoInstrumento" = 'FM'
AND GAR."IdTipoInstrumento" = 'FFM'
AND GAR."IdInstrumento" = CASE WHEN IFNULL(:IdInstrumento,0) > 0 THEN :IdInstrumento ELSE GAR."IdInstrumento" END
AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
AND (:IdEmisor IS NULL OR CAR."IdEmisor" = :IdEmisor)
AND	Serie."IdFamiliaFondo" = CASE WHEN IFNULL(:IdFamiliaFondo,'') <> '' THEN IFNULL(:IdFamiliaFondo,'') ELSE Serie."IdFamiliaFondo" END;