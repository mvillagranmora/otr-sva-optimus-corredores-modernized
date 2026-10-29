--GCMBIPAPAS-1 Filtro de clientes por Perfiles
SELECT
	 Consulta."IdSolicitudAporteRescate"
	,Consulta."IdCliente"
	,Consulta."IdPortafolio"
	,Consulta."FechaEmision"	
	,TO_NVARCHAR(Consulta."FechaIngresoRegistro",'YYYY-MM-DD HH24:MI:SS') AS "FechaIngresoRegistro"
	,Consulta."IdTipoMovimientoFondoMutuo"
	,Consulta."TipoSolicitud"
	,Consulta."DescripcionTipoMovimiento"
	,Consulta."EstadoSolicitud"
	,Consulta."NombreEstado"
	,Consulta."Nemotecnico" AS "NombreFondo"
	,Consulta."NombreFondo" AS "NombreFondoReal"
	,Consulta."IdSerieFondo"
	,Consulta."NombreSerieFondo"
	,Consulta."CalculoPorMontoCuotaUI"
	,Consulta."NumeroCuotas"
	,Consulta."ValorCuota"
	,Consulta."Monto"
	,Consulta."DocumentoIdentidad"
	,Consulta."NombreCompleto"
	,Consulta."DescripcionPortafolio"
	
	,TO_NVARCHAR(Consulta."FechaCertificacion",'YYYY-MM-DD') AS "FechaCertificacion"
	,TO_NVARCHAR(Consulta."FechaLiquidacion",'YYYY-MM-DD') AS "FechaLiquidacion"
	,TO_NVARCHAR(Consulta."FechaAceptacion",'YYYY-MM-DD') AS "FechaAceptacion"
	,Consulta."FechaAnulacion"
	,Consulta."FechaAceptacionRegistro"
	,Consulta."FechaCertificacionRegistro"
	,Consulta."FechaLiquidacionRegistro"
	,Consulta."FechaAnulacionRegistro"
	,Consulta."NombreEmpleado"
	,Consulta."IdEmisor"
	,Consulta."IdEmpresa"
	,Consulta."IdMoneda"
	,Consulta."TextoPreferenciaPago"
	,Consulta."Glosa"
	,Consulta."DecimalesValorCuota"
	,Consulta."DecimalesCuota"
	,Consulta."EsOvernightUI"
	,Consulta."IdSolicitudAporteRescateRelacionado"
	,Consulta."DescripcionPortafolioDestino"
	,Consulta."IdPortafolioDestino"
	,Consulta."IdClienteDestino"
	,Consulta."DocumentoIdentidadDestino"
	,Consulta."NombreCompletoDestino"
	,CASE Consulta."IndicadorMLT" WHEN 'N' THEN 'UI_NO' ELSE 'UI_SI' END AS "EsIndicadorMLTUI"
	,Consulta."MontoComision"
	,Consulta."IdTipoMovimientoFondoMutuoRelacionado"
	,Consulta."DescripcionTipoMovimientoRelacionado"
	,Consulta."IdMonedaFondo"
	,Consulta."EsDiferido"
	,Consulta."EsDiferidoUI"
	,Consulta."CostoMonedaLocal"
	,Consulta."ParidadFechaInversion"
	,Consulta."MontoInversion"
	,Consulta."NumeroCuotasInversion"
	,Consulta."ValorCuotaInversion"
	,Consulta."CostoMonedaLocalInversion"
	,Consulta."ParidadFechaCertificacion"
	,Consulta."CalculoPorMontoCuota"
	,Consulta."IdSerieFondoDestino"
	,Consulta."IdEmisorDestino"
	,Consulta."SolicitudBatch"
	,Consulta."OrigenOperacion"
	,Consulta."ComisionInformada"
	,Consulta."NombreEstadoLiquidacion"
	,Consulta."FolioCartaLiquidacion"
	,Consulta."CostoUF"
	,Consulta."ValorUFFechaInversion"
	,Consulta."Spread"
	,Consulta."FechaOriginalInversion"
	,Consulta."MontoMovimientoAsociado"
	,Consulta."NumeroCuotasMovimientoAsociado"
	,Consulta."IndicadorMLT"
	,Consulta."IdEmisorOrigen"
	,Consulta."CodigoPortafolio"
	,Consulta."CodigoSerie"
	,Consulta."IdFondoRelacionado"
	,Consulta."CodigoSerieRelacionado"
	,Consulta."DescripcionRegimenTributario"
	,Consulta."IdFondo"
	,Consulta."DescripcionTipoSolicitud"
	,Consulta."MontoMonedaSistema"
	,Consulta."IdMovimientoCarteraEnTransito"
	,Consulta."IdDividendo"
	,Consulta."Switch"
	,CASE Consulta."Switch" WHEN 'N' THEN 'UI_NO' ELSE 'UI_SI' END AS "SwitchUI"
	,Consulta."IdTipoCanalIngreso","FolioContrato"
	,MatrizAgrupador."Producto"
	,MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") AS "IdProductoAgrupador"
	---SPOT
 	,Consulta."IdIngresoEgreso"
	,Consulta."NombreEstadoTesoreria" 	
	,TO_NVARCHAR(Consulta."FechaLiquidacionTesoreria",'YYYY-MM-DD') AS "FechaLiquidacionTesoreria"
	,Consulta."IdOperacionSpot"
	,Consulta."NombreEstadoSpot"
	,TO_NVARCHAR(Consulta."FechaLiquidacionSpot",'YYYY-MM-DD') AS "FechaLiquidacionSpot"
	,Consulta."IdTipoOperacionSpot"
	,Consulta."NombreTipoOperacionSpot"	
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS Consulta
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
			 ON Portafolio."IdPortafolio" = Consulta."IdPortafolio"
			AND Portafolio."IdCliente" = Consulta."IdCliente"
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador
   			 ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
   			 
WHERE (TO_DATE(Consulta."FechaAceptacion") BETWEEN TO_DATE(:p_FechaDesde) AND TO_DATE(:p_FechaHasta))
  AND (Consulta."EstadoSolicitud" IN (:p_EstadoSolicitud) OR 1 = :p_FiltroEstado)   
  AND Consulta."IdCliente" IN (:p_listaClientes)  
  AND (Consulta."TipoSolicitud" IN (:p_TipoSolicitud) OR 1 = :p_FiltroTipoSolicitud)
  AND (Consulta."IdEmisor" IN (:p_IdEmisor) OR 1 = :p_FiltroEmisor)   
  AND (MatrizAgrupador."Producto" IN (:p_Producto) OR 1 = :p_FiltroProducto)  
  AND (Consulta."IdMoneda" IN (:p_IdMoneda) OR 1 = :p_FiltroMoneda)
  AND (Consulta."IdTipoMovimientoFondoMutuo" IN (:p_TipoOperacion) OR 1 = :p_FiltroTipoOperacion)
  AND (Consulta."IdSerieFondo" IN (:p_Instrumento) OR 1 = :p_FiltroInstrumento)

   			
  /*
WHERE (TO_DATE(Consulta."FechaAceptacion") BETWEEN TO_DATE(:p_FechaDesde) AND TO_DATE(:p_FechaHasta))
  AND Consulta."EstadoSolicitud" = (CASE WHEN :p_EstadoSolicitud <> '' THEN :p_EstadoSolicitud ELSE Consulta."EstadoSolicitud" END)
  AND Consulta."IdCliente" IN (:p_listaClientes)
  AND Consulta."TipoSolicitud" = (CASE WHEN :p_TipoSolicitud <> '' THEN :p_TipoSolicitud ELSE Consulta."TipoSolicitud" END)
  AND Consulta."IdEmisor" = (CASE WHEN :p_IdEmisor <> '' THEN :p_IdEmisor ELSE Consulta."IdEmisor" END)
  AND MatrizAgrupador."Producto" = (CASE WHEN :p_Producto <> '' THEN :p_Producto ELSE MatrizAgrupador."Producto" END)
  AND Consulta."IdMoneda" = (CASE WHEN :p_IdMoneda <> '' THEN :p_IdMoneda ELSE Consulta."IdMoneda" END)
  AND Consulta."IdTipoMovimientoFondoMutuo" = (CASE WHEN :p_TipoOperacion <> '' THEN :p_TipoOperacion ELSE Consulta."IdTipoMovimientoFondoMutuo" END)
  AND Consulta."IdSerieFondo" = (CASE WHEN :p_Instrumento <> '' THEN :p_Instrumento ELSE Consulta."IdSerieFondo" END)
   			 */
/*
  WHERE TO_DATE(Consulta."FechaAceptacion") BETWEEN TO_DATE(:p_FechaDesde) AND TO_DATE(:p_FechaHasta)
  AND :p_EstadoSolicitud = :p_EstadoSolicitud   --(Consulta."EstadoSolicitud" IN (:p_EstadoSolicitud) OR 1 = :p_FiltroEstado)   
  AND :p_FiltroEstado = :p_FiltroEstado
  AND Consulta."IdCliente" IN (:p_listaClientes)
  AND (Consulta."TipoSolicitud" IN (:p_TipoSolicitud) OR 1 = :p_FiltroTipoSolicitud)  --AND :p_TipoSolicitud = :p_TipoSolicitud
  AND :p_FiltroTipoSolicitud= :p_FiltroTipoSolicitud
  AND :p_IdEmisor = :p_IdEmisor
  AND :p_FiltroEmisor = :p_FiltroEmisor 
  AND :p_Producto = :p_Producto  
  AND :p_FiltroProducto = :p_FiltroProducto 
  AND :p_IdMoneda = :p_IdMoneda
  AND :p_FiltroMoneda = :p_FiltroMoneda
  AND :p_TipoOperacion = :p_TipoOperacion
  AND :p_FiltroTipoOperacion = :p_FiltroTipoOperacion
  AND :p_Instrumento = :p_Instrumento 
  AND :p_FiltroInstrumento = :p_FiltroInstrumento
  
  */
 
   			 