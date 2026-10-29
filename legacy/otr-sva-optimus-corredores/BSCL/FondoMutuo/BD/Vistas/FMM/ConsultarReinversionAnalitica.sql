SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarReinversionAnalitica";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarReinversionAnalitica" AS
SELECT  Consulta."IdSolicitudAporteRescate"
	    ,Consulta."FolioContrato"
	    ,Consulta."FechaIngresoRegistro" AS "FechaRegistro"
	    ,Consulta."FechaAceptacion" AS "FechaCurse"
	    ,Consulta."FechaCertificacion" AS "FechaCertificacion"
	    ,Consulta."FechaLiquidacion" AS "FechaLiquidacion"
	    ,Consulta."IdTipoCanalIngreso" AS "Canal"
	    ,Consulta."IdTipoMovimientoFondoMutuo"
	    ,Consulta."TipoSolicitud"
	    ,Consulta."Estado"
	    ,Consulta."EstadoLiquidacion"
	    ,Consulta."FolioCartaLiquidacion"
	    ,Fondo."IdEmisor"
	    ,Fondo."IdFondo"
	    ,Fondo."NombreFondo" AS "NombreFondo"
	    ,SerieDeFondo."IdSerieFondo"
	    ,SerieDeFondo."NombreSerieFondo"
	    ,ConsultarCliente."DocumentoIdentidad"
	    ,ConsultarCliente."NombreCompleto"
	    ,Consulta."IdPortafolio"
	    ,Consulta."IdRegimenTributario"
	    ,Fondo."IdMoneda" AS "IdMonedaFondo"
	    ,Consulta."ValorCuota" AS "ValorCuota"
	    ,Consulta."NumeroCuotas"
	    ,Consulta."Monto"
	    ,Consulta."IdMoneda"
	    ,Consulta."ParidadFechaInversion" AS "TipoCambio"
	    ,Consulta."CostoMonedaLocal" AS "MontoMonedaOrigen"
	    ,Consulta."CostoUF"
	    ,Consulta."MontoMonedaSistema"
	    ,CASE WHEN COALESCE(ConsultarComprobanteFormaDePago."IdMedioDePago",'') = '' THEN MedioDePago."Nombre" ELSE ConsultarComprobanteFormaDePago."NombreMedioDePago" END "FormaDePago"
	    ,Consulta."Spread"
	    ,Consulta."RentabilidadReal"
	    ,Consulta."IdSerieFondoDestino"
	    ,Consulta."IdEmisorDestino" 
	    ,Consulta."Switch"
	    ,MatrizAgrupador."Producto"
	    ,Consulta."IdIngresoEgreso"
	    ,TesoreriaSpot."IdEstado" AS "IdEstadoTesoreriaSpot"
	    ,TO_NVARCHAR(TesoreriaSpot."FechaLiquidacion",'YYYY-MM-DD') AS "FechaLiquidacionTesoreria"
	    ,Consulta."IdOperacionSpot"
	    ,Spot."Estado" AS "IdEstadoSpot"
	    ,CASE WHEN Consulta."IdOperacionSpot" > 0 THEN  (CASE WHEN TipoMovimientoFondoMutuo."IndicadorAporteRescate" = 'APO' THEN 'VENSPT' ELSE  'COMSPT' END) ELSE '' END AS "IdTipoOperacionSpot"
	    ,Consulta."FolioOmnibus"
	    ,Consulta."IdAgrupador"
	    ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
	    ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS Consulta
	LEFT OUTER  JOIN 
	    (SELECT OperSpot.* FROM 
	      (SELECT  ROW_NUMBER() OVER(PARTITION BY "IdOperacionSpotOrigen" ORDER BY "IdOperacionSpot" DESC ) AS r ,*
	        FROM "BSCL.Mesa.BD::SPT.OperacionSpot" ) as OperSpot
	     WHERE OperSpot.r=1
	    ) AS Spot ON (Spot."IdOperacionSpotOrigen"=CASE WHEN Consulta."IdOperacionSpot" > 0 THEN Consulta."IdOperacionSpot" ELSE NULL END)
	LEFT OUTER  JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS TesoreriaSpot ON (Consulta."IdIngresoEgreso" = TesoreriaSpot."IdIngresoEgreso")
	LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON (SerieDeFondo."IdSerieFondo" = Consulta."IdSerieFondo" )
	LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON (Fondo."IdFondo" = SerieDeFondo."IdFondo" )
	LEFT OUTER  JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MedioDePago ON (MedioDePago."IdMedioDePago" = Consulta."IdMedioDePago")
	LEFT OUTER  JOIN 
	    (SELECT "IdTipoComprobanteOrigen", "IdComprobanteOrigen", ComprobanteDetalle."IdEstado",Comprobante."IdComprobante"  FROM
	    "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS ComprobanteDetalle
	        INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante 
	        ON Comprobante."IdComprobante" = ComprobanteDetalle."IdComprobante"
	    ) AS ComprobanteDetalle 
	    ON (ComprobanteDetalle."IdTipoComprobanteOrigen" = 'FMM' AND ComprobanteDetalle."IdComprobanteOrigen" = Consulta."IdSolicitudAporteRescate" AND ComprobanteDetalle."IdEstado" IN ('ING','LIQ','CRT'))
	LEFT OUTER  JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ConsultarComprobanteFormaDePago 
	    ON (ConsultarComprobanteFormaDePago."IdComprobante" = ComprobanteDetalle."IdComprobante" AND ConsultarComprobanteFormaDePago."IdEstado" IN ('CRT','EBV','EMI','GIR','ING','LIQ'))
	LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio  ON Portafolio."IdPortafolio" = Consulta."IdPortafolio" AND Portafolio."IdCliente" = Consulta."IdCliente"
	LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS ConsultarCliente ON ( ConsultarCliente."IdCliente" = Portafolio."IdCliente" )
	LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TipoMovimientoFondoMutuo ON (TipoMovimientoFondoMutuo."IdTipoMovimientoFondoMutuo" = Consulta."IdTipoMovimientoFondoMutuo")
	LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
	WHERE Consulta."IdTipoMovimientoFondoMutuo" in ('RESREINV','RESREINV1N','APOREINV');

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarReinversionAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
