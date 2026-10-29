--SELECT "IdAgrupador","TipoSolicitud",count(case when "TipoSolicitud" = 'RES' THEN 1 ELSE 0 END) AS COUNTRES,count(case when "TipoSolicitud" = 'APO' THEN 1 ELSE 0 END) AS COUNTAPO FROM (
		SELECT 
		"SolicitudAporteRescate"."IdAgrupador"
		,CASE WHEN "SolicitudAporteRescate"."TipoSolicitud" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."TipoSolicitud" AS NVARCHAR(3)) END AS "TipoSolicitud" 
        , CASE WHEN "SerieDeFondo"."IdSerieFondo" = '' THEN NULL ELSE CAST("SerieDeFondo"."IdSerieFondo" AS NVARCHAR(40)) END AS "IdSerieFondo"-- es el mismo nemotecnico
        , CASE WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" AS NVARCHAR(10)) END AS "IdTipoMovimientoFondoMutuo" 
		, CASE WHEN "SolicitudAporteRescate"."IdSerieFondoDestino" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."IdSerieFondoDestino" AS NVARCHAR(40)) END AS "IdSerieFondoDestino" 
        , CASE WHEN "SerieDeFondo"."IdSerieFondoOrigen" = '' THEN NULL ELSE CAST("SerieDeFondo"."IdSerieFondoOrigen" AS NVARCHAR(40)) END AS "IdSerieFondoOrigen" 
        , CASE WHEN "SolicitudAporteRescate"."IdEmisorDestino" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."IdEmisorDestino" AS NVARCHAR(40)) END AS "IdEmisorDestino" 
        , "SolicitudAporteRescate"."IndicadorMLT"
        , "SolicitudAporteRescate"."Overnight"
        , CASE WHEN "SolicitudAporteRescate"."IdTipoCartera" = '' THEN CAST("SolicitudAporteRescate"."IdTipoCartera" AS NVARCHAR(3)) END AS "IdTipoCartera"
        , "SolicitudAporteRescate"."CalculoPorMontoCuota"
        , CASE WHEN "SolicitudAporteRescate"."IdMoneda" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."IdMoneda" AS NVARCHAR(3))END  AS "IdMoneda" 
        , CASE WHEN "SolicitudAporteRescate"."Estado" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."Estado" AS NVARCHAR(3)) END AS "Estado"        
        , CAST("SolicitudAporteRescate"."IdMedioDePago" AS NVARCHAR(3)) AS "IdMedioDePago" 
        , CASE WHEN "SolicitudAporteRescate"."EstadoLiquidacion" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."EstadoLiquidacion" AS NVARCHAR(3)) END AS "EstadoLiquidacion"       
        , "SolicitudAporteRescate"."OrigenOperacion"
        , "SolicitudAporteRescate"."SolicitudBatch"     
        , "SolicitudAporteRescate"."ComisionInformada"
        , coalesce(CAST("SerieDeFondo"."NombreSerieFondo" AS NVARCHAR(100)),'') AS "NombreSerieFondo" 
        , coalesce(CAST("SerieDeFondo"."CodigoSerie" AS NVARCHAR(10)),'') AS "CodigoSerie" 
        , COALESCE("SerieDeFondo"."ExcluyeGarantia",'') AS "ExcluyeGarantia"
        , COALESCE("SerieDeFondo"."CobraComision",'') AS "CobraComision" 
        , COALESCE("SerieDeFondo"."Omnibus",'')  AS "Omnibus" 
        , COALESCE("SerieDeFondo"."Diferido",'') AS "Diferido"
        , CASE WHEN "SerieDeFondo"."IdMedioDePagoAbono" = '' THEN NULL ELSE CAST("SerieDeFondo"."IdMedioDePagoAbono" AS NVARCHAR(3)) END AS "IdMedioDePagoAbonoAGF"
        , CASE WHEN "SerieDeFondo"."IdMedioDePagoCargo" = '' THEN NULL ELSE CAST("SerieDeFondo"."IdMedioDePagoCargo" AS NVARCHAR(3)) END AS "IdMedioDePagoCargoAGF"
        , CASE WHEN "Fondo"."IdFondo" = '' THEN NULL ELSE CAST("Fondo"."IdFondo" AS NVARCHAR(40)) END AS "IdFondo" 
        , CASE WHEN "Fondo"."IdEmisor" = '' THEN NULL ELSE CAST("Fondo"."IdEmisor" AS NVARCHAR(40)) END AS "IdEmisor" 
        , COALESCE(CAST("Fondo"."Nemotecnico" AS NVARCHAR(40)),'') AS "Nemotecnico" 
        , COALESCE(CAST("Fondo"."NombreFondo" AS NVARCHAR(100)),'') AS "NombreFondo" 
        , CASE WHEN "Fondo"."IdTipoFondo" = '' THEN NULL ELSE CAST("Fondo"."IdTipoFondo" AS NVARCHAR(10)) END AS "IdTipoFondo" 
        , COALESCE(CAST("Fondo"."CodigoSVS" AS NVARCHAR(40)),'') AS "CodigoSVS" 
        , COALESCE("Fondo"."MoneyMarket",'') AS "MoneyMarket" 
        , COALESCE(CAST("Fondo"."IdCategoriaPerfilFondoMutuo" AS NVARCHAR(40)),'') AS "IdCategoriaPerfilFondoMutuo" 
        , COALESCE("Fondo"."Vigencia",'') AS "Vigencia"
        , COALESCE(CAST("Fondo"."IdTipoInstrumento" AS NVARCHAR(3)),'') AS "IdTipoInstrumento" 
        , CASE WHEN "Fondo"."IdMoneda" = '' THEN NULL ELSE CAST("Fondo"."IdMoneda" AS NVARCHAR(3)) END AS "IdMonedaFondo"
        , COALESCE(CAST("TipoFondo"."NombreCorto" AS NVARCHAR(40)),'') AS "NombreCorto" 
        , COALESCE(CAST("TipoFondo"."Descripcion" AS NVARCHAR(100)),'') AS "Descripcion" 
        , COALESCE(CAST("TipoMovimientoFondoMutuo"."Descripcion" AS NVARCHAR(100)),'') AS "DescripcionTipoMovimiento"
        , COALESCE(CAST("Estado"."Nombre" AS NVARCHAR(100)),'') AS "NombreEstado"
        , COALESCE(CAST("ConsultarEmpleado"."NombreCompleto" AS NVARCHAR(302)),'') AS "NombreEmpleado"
        , COALESCE(CAST("MedioDePago"."Nombre" AS NVARCHAR(100)),'') AS "TextoPreferenciaPago"
        , COALESCE(CAST("EstadoLiquidacion"."Nombre" AS NVARCHAR(100)),'') AS "NombreEstadoLiquidacion"
        , CASE WHEN "SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo" = '' THEN NULL ELSE CAST("SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo" AS NVARCHAR(10)) END AS "IdTipoMovimientoFondoMutuoRelacionado"
        , COALESCE(CAST("TipoMovimientoFondoMutuoRelacionado"."Descripcion" AS NVARCHAR(100)),'') AS "DescripcionTipoMovimientoRelacionado"
        , CAST("SolicitudAporteRescate"."FechaEmision" AS CHAR(10)) AS "FechaEmision"
        , "SolicitudAporteRescate"."FechaAceptacion"
        , "SolicitudAporteRescate"."FechaCertificacion" 
        , "SolicitudAporteRescate"."FechaLiquidacion"
        , "SolicitudAporteRescate"."FechaAnulacion" 
        , "Fondo"."FechaCreacion" 
        , COALESCE("Fondo"."FechaConstitucion",'') AS "FechaConstitucion"
        , COALESCE("Fondo"."FechaCierre",'') AS "FechaCierre" 
        ,0 AS "IdSolicitudAporteRescate"--, "SolicitudAporteRescate"."IdSolicitudAporteRescate"
        , CASE WHEN "SolicitudAporteRescate"."IdCliente" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdCliente" END AS "IdCliente"
        ,0 AS "IdPortafolio"--, CASE WHEN "SolicitudAporteRescate"."IdPortafolio" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdPortafolio" END AS "IdPortafolio"
        , CASE WHEN "SolicitudAporteRescate"."IdDividendo" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdDividendo" END AS "IdDividendo"
        , CASE WHEN "SolicitudAporteRescate"."IdClienteDestino" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdClienteDestino" END AS "IdClienteDestino"
        , CASE WHEN "SolicitudAporteRescate"."IdPortafolioDestino" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdPortafolioDestino" END AS "IdPortafolioDestino"
        ,0 AS "IdSolicitudAporteRescateRelacionado"--, CASE WHEN "SolicitudAporteRescate"."IdSolicitudAporteRescateRelacionado" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdSolicitudAporteRescateRelacionado" END AS "IdSolicitudAporteRescateRelacionado"
        ,SUM("SolicitudAporteRescate"."Monto") AS "Monto"--, "SolicitudAporteRescate"."Monto"
        ,SUM("SolicitudAporteRescate"."NumeroCuotas") AS "NumeroCuotas"--, "SolicitudAporteRescate"."NumeroCuotas"
        , CASE WHEN "SolicitudAporteRescate"."Estado" != 'CRT' THEN "ValorCuota"."ValorCuota" ELSE "SolicitudAporteRescate"."ValorCuota" END AS "ValorCuota"
        ,SUM("SolicitudAporteRescate"."CostoUF") AS "CostoUF"--, "SolicitudAporteRescate"."CostoUF"
        ,SUM("SolicitudAporteRescate"."CostoMonedaLocal") AS "CostoMonedaLocal"--, "SolicitudAporteRescate"."CostoMonedaLocal"
        , "SolicitudAporteRescate"."ValorUFFechaInversion"
        , "SolicitudAporteRescate"."ParidadFechaInversion"
        , "SolicitudAporteRescate"."MontoComision"
        ,SUM("SolicitudAporteRescate"."MontoPago") AS "MontoPago"--, "SolicitudAporteRescate"."MontoPago"
        , "SolicitudAporteRescate"."IdEmpresa"
        , "SolicitudAporteRescate"."IdUsuario" 
        , SUM("SolicitudAporteRescate"."MontoInversion") AS "MontoInversion" --, "SolicitudAporteRescate"."MontoInversion"
        ,SUM("SolicitudAporteRescate"."NumeroCuotasInversion") AS "NumeroCuotasInversion"--, "SolicitudAporteRescate"."NumeroCuotasInversion"
        , "SolicitudAporteRescate"."ValorCuotaInversion"
        ,SUM("SolicitudAporteRescate"."CostoMonedaLocalInversion") AS "CostoMonedaLocalInversion"--, "SolicitudAporteRescate"."CostoMonedaLocalInversion"
        , "SolicitudAporteRescate"."ParidadFechaCertificacion"
        ,SUM("SolicitudAporteRescate"."MontoPagoLocal") AS "MontoPagoLocal"--, "SolicitudAporteRescate"."MontoPagoLocal"
        , "SolicitudAporteRescate"."MontoComisionLocal"
        , CASE WHEN "SerieDeFondo"."IdInstrumento" = 0 THEN NULL ELSE  "SerieDeFondo"."IdInstrumento" END AS "IdInstrumento"
        , COALESCE("SerieDeFondo"."MontoMinimo",0.0) AS "MontoMinimo"
        , COALESCE("SerieDeFondo"."MontoMaximo",0.0) AS "MontoMaximo"
        , COALESCE("SerieDeFondo"."DiasRetencionesRescate",0) AS "DiasRetencionesRescate"
        , COALESCE("SerieDeFondo"."DecimalesValorCuota",0) AS "DecimalesValorCuota"
        , COALESCE("SerieDeFondo"."DecimalesCuota",0) AS "DecimalesCuota"
        , CASE WHEN "SerieDeFondo"."IdCliente" = 0 THEN NULL ELSE "SerieDeFondo"."IdCliente" END AS "IdClienteAGF"
        , CASE WHEN "SerieDeFondo"."IdPortafolio" = 0 THEN NULL ELSE "SerieDeFondo"."IdPortafolio" END  AS "IdPortafolioAGF"
        , CASE WHEN "Fondo"."IdPaisOrigen" = 0 THEN NULL ELSE "Fondo"."IdPaisOrigen" END AS "IdPaisOrigen"
        , CASE WHEN "ClienteAGF"."IdPersona" = 0 THEN NULL ELSE "ClienteAGF"."IdPersona" END AS "IdPersonaAGF"
        ,0 as "RentabilidadReal"--, "SolicitudAporteRescate"."RentabilidadReal"
        ,'' aS "IdRegimenTributario"--, "SolicitudAporteRescate"."IdRegimenTributario"
        ,0 as "IdMatrizAgrupador" --, "Portafolio"."IdMatrizAgrupador"
        , "Portafolio"."EsADC"
        FROM "BSCL"."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
        LEFT OUTER  JOIN "BSCL"."BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondo" )
        LEFT OUTER  JOIN "BSCL"."BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieDeFondo"."IdFondo" )
        LEFT OUTER  JOIN "BSCL"."BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TipoFondo" ON ("Fondo"."IdTipoFondo" = "TipoFondo"."IdTipoFondo") 
        LEFT OUTER  JOIN "BSCL"."BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuo" ON ("TipoMovimientoFondoMutuo"."IdTipoMovimientoFondoMutuo" = "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo")
        LEFT OUTER  JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "Estado" ON ("Estado"."IdEstado" = "SolicitudAporteRescate"."Estado")
        LEFT OUTER  JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "EstadoLiquidacion" ON ("EstadoLiquidacion"."IdEstado" = "SolicitudAporteRescate"."EstadoLiquidacion")
        LEFT OUTER  JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS "ConsultarEmpleado" ON ("ConsultarEmpleado"."IdEmpleado" = "SolicitudAporteRescate"."IdUsuario")
        LEFT OUTER  JOIN "BSCL"."BSCL.Plataforma.BD::PAG.MedioDePago" AS "MedioDePago" ON ("MedioDePago"."IdMedioDePago" = "SolicitudAporteRescate"."IdMedioDePago")
        LEFT OUTER  JOIN "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "Portafolio" ON ("Portafolio"."IdPortafolio" = "SolicitudAporteRescate"."IdPortafolio")
        LEFT OUTER  JOIN "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "PortafolioAGF" ON ("PortafolioAGF"."IdPortafolio" = "SerieDeFondo"."IdPortafolio")
        LEFT OUTER  JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ClienteAGF" ON ("SerieDeFondo"."IdCliente" = "ClienteAGF"."IdCliente")
        LEFT OUTER  JOIN "BSCL"."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescateRelacionado" ON ("SolicitudAporteRescateRelacionado"."IdSolicitudAporteRescate" = "SolicitudAporteRescate"."IdSolicitudAporteRescateRelacionado")
        LEFT OUTER  JOIN "BSCL"."BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuoRelacionado" ON ("TipoMovimientoFondoMutuoRelacionado"."IdTipoMovimientoFondoMutuo" = "SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo")
        LEFT OUTER  JOIN "BSCL"."BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "ValorCuota" ON ("ValorCuota"."IdSerieFondo" = "SerieDeFondo"."IdSerieFondo")
        --WHERE "SolicitudAporteRescate"."IdAgrupador" > 1
		WHERE "SolicitudAporteRescate"."IdAgrupador" = 10694
        group by 
        "SolicitudAporteRescate"."IdAgrupador",
        "SolicitudAporteRescate"."TipoSolicitud",
        "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo",
        "SerieDeFondo"."IdSerieFondo",
        "SolicitudAporteRescate"."IdSerieFondoDestino",
        "SerieDeFondo"."IdSerieFondoOrigen",
        "SolicitudAporteRescate"."IdEmisorDestino",
        "SolicitudAporteRescate"."IndicadorMLT",
        "SolicitudAporteRescate"."Overnight",
        "SolicitudAporteRescate"."IdTipoCartera",
        "SolicitudAporteRescate"."CalculoPorMontoCuota",
        "SolicitudAporteRescate"."IdMoneda",
        "SolicitudAporteRescate"."Estado",
        "SolicitudAporteRescate"."IdMedioDePago",
        "SolicitudAporteRescate"."EstadoLiquidacion",
        "SolicitudAporteRescate"."OrigenOperacion",
        "SolicitudAporteRescate"."SolicitudBatch",
        "SolicitudAporteRescate"."ComisionInformada",
        
        "SerieDeFondo"."NombreSerieFondo",
        "SerieDeFondo"."CodigoSerie",
        "SerieDeFondo"."ExcluyeGarantia",
        "SerieDeFondo"."CobraComision",
        "SerieDeFondo"."Omnibus",
        "SerieDeFondo"."Diferido",
        "SerieDeFondo"."IdMedioDePagoAbono",
        "SerieDeFondo"."IdMedioDePagoCargo",
        "ValorCuota"."ValorCuota",
        "Fondo".
        "IdFondo",
        "IdEmisor",
        "Nemotecnico",
        "NombreFondo",
        "Fondo"."IdTipoFondo",
        "CodigoSVS",
        "MoneyMarket",
        "IdCategoriaPerfilFondoMutuo",
        "Vigencia",
        "IdTipoInstrumento",
        "Fondo"."IdMoneda",
        "NombreCorto",
        "TipoFondo"."Descripcion",
        "TipoMovimientoFondoMutuo".
        "Descripcion",
        "Estado"."Nombre",
        "ConsultarEmpleado"."NombreCompleto",
        "MedioDePago"."Nombre",
        "EstadoLiquidacion"."Nombre",
        "SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo",
        "TipoMovimientoFondoMutuoRelacionado"."Descripcion",
        "SolicitudAporteRescate"."FechaEmision",
        "SolicitudAporteRescate"."FechaAceptacion",
        "SolicitudAporteRescate"."FechaCertificacion",
        "SolicitudAporteRescate"."FechaLiquidacion",
        "SolicitudAporteRescate"."FechaAnulacion",
        "Fondo"."FechaCreacion",
        "Fondo"."FechaConstitucion",
        "Fondo"."FechaCierre",
        --"SolicitudAporteRescate"."IdSolicitudAporteRescate",
        "SolicitudAporteRescate"."IdCliente",
        --"SolicitudAporteRescate"."IdPortafolio",
        "SolicitudAporteRescate"."IdDividendo",
        "SolicitudAporteRescate"."IdClienteDestino",
        "SolicitudAporteRescate"."IdPortafolioDestino",
        --"SolicitudAporteRescate"."IdSolicitudAporteRescateRelacionado",
        --"SolicitudAporteRescate"."Monto",
        --"SolicitudAporteRescate"."NumeroCuotas",
        "SolicitudAporteRescate"."ValorCuota",
        --"SolicitudAporteRescate"."CostoUF",
        --"SolicitudAporteRescate"."CostoMonedaLocal",
        "SolicitudAporteRescate"."ValorUFFechaInversion",
        "SolicitudAporteRescate"."ParidadFechaInversion",
        "SolicitudAporteRescate"."MontoComision",
        --"SolicitudAporteRescate"."MontoPago",
        "SolicitudAporteRescate"."IdEmpresa",
        "SolicitudAporteRescate"."IdUsuario",
        --"SolicitudAporteRescate"."MontoInversion",
        --"SolicitudAporteRescate"."NumeroCuotasInversion",
        "SolicitudAporteRescate"."ValorCuotaInversion",
        --"SolicitudAporteRescate"."CostoMonedaLocalInversion",
        "SolicitudAporteRescate"."ParidadFechaCertificacion",
        --"SolicitudAporteRescate"."MontoPagoLocal",
        "SolicitudAporteRescate"."MontoComisionLocal",
        "IdInstrumento","MontoMinimo",
        "MontoMaximo",
        "DiasRetencionesRescate",
        "DecimalesValorCuota",
        "DecimalesCuota",
        "SerieDeFondo"."IdCliente",
        "SerieDeFondo"."IdPortafolio",
        "IdPaisOrigen","ClienteAGF"."IdPersona",
        --"SolicitudAporteRescate"."RentabilidadReal",
        --"SolicitudAporteRescate"."IdRegimenTributario",
        --"Portafolio"."IdMatrizAgrupador",
        "Portafolio"."EsADC"
		
		order by "IdAgrupador"
		
--) group by "IdAgrupador","TipoSolicitud" having count(case when "TipoSolicitud" = 'RES' THEN 1 ELSE 0 END) > 1 and count(case when "TipoSolicitud" = 'APO' THEN 1 ELSE 0 END) > 1