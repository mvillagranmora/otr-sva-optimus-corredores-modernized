SELECT	ord."IdInterno",
		"BSCL.Orden.BD.Secuencias::Orden".NEXTVAL as "IdOrden",	
		inst."FechaEmision",
		inst."IdTipoReajuste" as "IdTipoReajusteInstrumento",
		tipoinst."IdClasificacionTipoInstrumento",
		inst."IdTipoInstrumento",
		instsim."IdEmisor" as "IdEmisorInstrumentoSubyacente",
		inst."IdEmisor",
		tipope."IdTipoProducto",
		inst."Nemotecnico",
		inst."IdMoneda" AS "IdMonedaInstrumento",
		COALESCE(usuario."IdEntidad",0)  as "IdEmpleadoDigitador",
		inst."IdEstado" as "IdEstadoInstrumento",
		inst."IdInstrumento" as "IdInstrumentoReal",
		tipoinst."Nombre" AS "NombreTipoInstrumento",
		inst."CorteMinimo" as "TamanoCorte", 
		inst."IdMoneda" as "IdMonedaInstrumento",
		inst."FechaVencimiento" AS "FechaVencimientoInstrumento",
		cla."Nombre" AS "NombreClasificacionTipoInstrumento", 
		inst."Tamano" AS "TamanoContrato",
		tipope."IdTipoOperacion" as "IdTipoOperacionReal",
		tipope."Nombre" as "NombreTipoOperacion",
		tipope."AccionOrden" as "AccionOrden",
		bol."IdBolsa" as "IdBolsaReal",
		condliqcon."IdCondicionDeLiquidacion" as "IdCondicionDeLiquidacionContadoReal",
		condliqcon."NombreLargo" as "NombreCondicionDeLiquidacion",
		ordvig."IdTipoVigenciaOrden" as "IdTipoVigenciaReal",
		ordvig."Nombre" as "NombreTipoVigenciaOrden",
		boveda."IdBoveda" as "IdBovedaReal",
		ordformcus."IdTipoFormaCustodiaOrden" as "IdFormaCustodiaReal",
		ordvis."IdPorcentajeVisibilidad" as "IdPorcentajeVisibilidadReal",
		ordtipopre."IdTipoPrecioOrden" as "IdTipoPrecioReal",
		ordtipopre."Nombre" as "NombreTipoPrecio", 
		ordcanal."IdTipoCanalIngreso" as "IdTipoCanalIngresoReal",
		CAST(condliqcon."PlazoEnDias" AS BIGINT) as "DiasLiquidacionContado",
		COALESCE(itr."IdTipoReajuste",'') as "IdTipoReajusteReal",
		instsim."Nemotecnico" as "InstrumentoSubyacente",
		CASE WHEN subprt."IdInstrumento" IS NULL THEN 'N' ELSE 'S' END AS "InstrumentoPermiteVentaCorta",
		CASE WHEN subsim."IdInstrumento" IS NULL THEN 'N' ELSE 'S' END AS "InstrumentoPermiteSimultanea",
		instsim."FechaVencimiento" AS "FechaVencimientoInstrumentoSubyacente",
		"BSCL.Orden.BD.Secuencias::GrupoCobroOrden".NEXTVAL as "IdGrupoCobro",
		ordconfcanaldist."IdTipoCanalDistribucion" as "IdTipoCanalDistribucionReal",
		COALESCE(ordconfcanaldist."GeneraMovimientoFacturacionAutomatico",'N') as "RuteoGeneraMovimientoFacturacionAutomatico",
		COALESCE(ordconfcanaldist."GeneraComprobanteFacturacionAutomatico",'N') as "RuteoGeneraComprobanteFacturacionAutomatico",
		COALESCE(ordconfcanaldist."MontoMaximoDistribucionOrden",-1) AS "MontoMaximoDistribucionOrden",
		COALESCE("ValidaHorario",'N') as "ValidaHorario",
		COALESCE("AplazaOrden",'N') as "AplazaOrdenPorHorarioNegociacion",
		TO_NVARCHAR(COALESCE("HorarioPreMarketInicio",'00:00:00')) AS "HorarioPreMarketInicio",
		TO_NVARCHAR(COALESCE("HorarioPreMarketFin",'00:00:00')) AS "HorarioPreMarketFin",
		TO_NVARCHAR(COALESCE("HorarioMarketInicio",'00:00:00')) AS "HorarioMarketInicio",
		TO_NVARCHAR(COALESCE("HorarioMarketFin",'00:00:00')) AS "HorarioMarketFin",
		CRE."IdEstado" AS "IdEstadoModulo",
		CRE."CodigoModulo" as "NombreModulo",
		CASE CRE."IdEstado" WHEN 'CER' THEN 'S' ELSE 'N' END AS "AplazaOrdenPorModuloCerrado", --deprecado
		port."IdPortafolio" as "IdPortafolioReal",
		port."CodigoPortafolio",
		port."IdEstado" as "IdEstadoPortafolio",
		port."IdEstadoAutorizacion" as "IdEstadoAutorizacionPortafolio",
		port."IdCliente",
		cli."IdPersona" as "IdPersonaCliente",
		cli."EsClienteFinal" as "EsClienteFinal",
		broker."IdPersona" AS "IdPersonaBroker", 		--mi idBroker
		brokerPortafolio."IdPersona" AS "IdPortafolioBroker",	--el broker del portafolio
		cli."IdEstadoPersona",
		cli."IdCliente" as "IdClienteReal",
		cli."IdEstado" as "IdEstadoCliente",
		cli."IdTipoCliente",
		
		cli."DocumentoIdentidad",
		cli."NombreCompleto",
		port."IdPerfilCobros",
		cli."TipoDocumentoIdentidad",
		emp."NombreCompleto" as "NombreEjecutivoPortafolio",
		emp."IdEstado" AS "IdEstadoEmpleadoEjecutivoPortafolio",
		emp."IdSucursal" AS "IdSucursalEmpleadoPortafolio",
		emp."IdEmpleado" as "IdEmpleadoPortafolio",
		COALESCE(empDigitador."IdEstado",'ACT') AS "IdEstadoEmpleadoDigitador",
		empDigitador."IdSucursal" AS "IdSucursalEmpleadoDigitador",
		CASE WHEN COALESCE(DPEMP."IdDetallePlantillaEmpleado",DPEMP_TODOS."IdDetallePlantillaEmpleado",-1) > 0 THEN 'S' ELSE 'N' END as "PlantillaEmpleadoProductoHabilitado",
		COALESCE(DPEMP."PermiteRuteo",DPEMP_TODOS."PermiteRuteo",'N') AS "PlantillaEmpleadoPermiteRuteo",
		COALESCE(DPEMP."PorcentajeDescuentoComision",DPEMP_TODOS."PorcentajeDescuentoComision",0) AS "PlantillaEmpleadoMaximoDescuentoComision",
		COALESCE(DPEMP."MontoMaximoOperacion",DPEMP_TODOS."MontoMaximoOperacion",0) AS "PlantillaEmpleadoMontoMaximoOperacion"
		,COALESCE(cli."IdCategoriaInversionista",'') AS "IdCategoriaInversionista"
		,inst."EsInstrumentoCalificado"
		,CASE WHEN inst."IdInstrumentoRaiz" IS NOT NULL AND inst."IdInstrumentoRaiz" > 0 THEN 'N' ELSE 'S' END AS "EsRaiz"
		FROM "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso" ord
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on ord."IdInstrumento" = inst."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoinst on tipoinst."IdTipoInstrumento" = inst."IdTipoInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" cla on cla."IdClasificacionTipoInstrumento" = tipoinst."IdClasificacionTipoInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" tipope on tipope."IdTipoOperacion" = ord."IdTipoOperacion" and tipope."IdTipoInstrumento" = inst."IdTipoInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarBolsaInstrumento" bol on bol."IdBolsa" = ord."IdBolsa" and bol."IdInstrumento" = inst."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" condliqcon on condliqcon."IdCondicionDeLiquidacion" = ord."IdCondicionDeLiquidacion" and condliqcon."IdTipoOperacion" = ord."IdTipoOperacion"
														
		LEFT JOIN "BSCL.Orden.BD::ORD.TipoVigenciaOrden" ordvig on ordvig."IdTipoVigenciaOrden" = ord."IdTipoVigencia"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" boveda on boveda."IdBoveda" = ord."IdBoveda" AND boveda."IdEstado" = 'VIG'
		LEFT JOIN "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden" ordformcus on ordformcus."IdTipoFormaCustodiaOrden" = ord."IdFormaCustodia"
		LEFT JOIN "BSCL.Orden.BD::ORD.PorcentajeVisibilidad" ordvis on ordvis."IdPorcentajeVisibilidad" = ord."IdPorcentajeVisibilidad"
		LEFT JOIN "BSCL.Orden.BD::ORD.TipoPrecioOrden" ordtipopre on ordtipopre."IdTipoPrecioOrden" = ord."IdTipoPrecio"
		LEFT JOIN "BSCL.Orden.BD::ORD.ConfiguracionHorarioDistribucion" ordhorario on 
			ordhorario."IdTipoCanalDistribucion" = ord."IdTipoCanalDistribucion" and
			ordhorario."IdTipoCanalIngreso" = ord."IdTipoCanalIngreso" and
			ordhorario."IdTipoInstrumento" = inst."IdTipoInstrumento" and
			ordhorario."IdBolsa" = ord."IdBolsa" and
			ordhorario."IdCondicionDeLiquidacion" = ord."IdCondicionDeLiquidacion"
		
		
		LEFT JOIN "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion" ordconfcanaldist on 
			:IdEmpresa = ordconfcanaldist."IdEmpresa" and 
			ordconfcanaldist."IdBolsa" = ord."IdBolsa" and 
			ordconfcanaldist."IdTipoInstrumento" = inst."IdTipoInstrumento" and 
			ordconfcanaldist."IdTipoCanalIngreso" = ord."IdTipoCanalIngreso" and 
			ordconfcanaldist."IdTipoCanalDistribucion" = ord."IdTipoCanalDistribucion" 
						
		LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" ordcanal on ordcanal."IdTipoCanalIngreso" = ord."IdTipoCanalIngreso"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" as itr ON inst."IdInstrumento"=itr."IdInstrumento" and inst."IdTipoInstrumento"=itr."IdTipoInstrumento" and itr."IdTipoReajuste" = ord."IdTipoReajuste" and itr."IdEstado" = 'ACT'  
		
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente" subprt on ord."IdInstrumento" = subprt."IdInstrumento" and subprt."IdTipoInstrumento" IN ('PRT', 'PRF')
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente" subsim on ord."IdInstrumentoSubyacente" = subsim."IdInstrumento" and subsim."IdTipoInstrumento" IN ('SIM')
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" instsim on ord."IdInstrumentoSubyacente" = instsim."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usuario on usuario."IdUsuario" = :IdUsuario AND "TipoEntidad" = 'EMP'
		LEFT JOIN "BSCL.Plataforma.BD::PAU.Modulo" CRE ON CRE."CodigoModulo" = 'ORD' AND CRE."IdEmpresa" = :IdEmpresa
		
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port on ord."IdPortafolio" = port."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on port."IdCliente" = cli."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.BrokerExtranjero" broker ON cli."IdPersona" = broker."IdPersona"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.BrokerExtranjero" brokerPortafolio ON port."IdPersonaBrokerExtranjero" = brokerPortafolio."IdPersona"
		
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" emp on port."IdEmpleadoEjecutivo" = emp."IdEmpleado"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" empDigitador on usuario."IdEntidad" = empDigitador."IdEmpleado"
		
		LEFT JOIN "BSCL.Plataforma.BD::COM.PlantillaEmpleado" as PEMP ON empDigitador."IdPlantillaEmpleado" = PEMP."IdPlantillaEmpleado" AND empDigitador."UtilizaPlantillaEmpleado" = 'S' AND PEMP."IdEstado" = 'ACT'
		LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" as DPEMP ON PEMP."IdPlantillaEmpleado" = DPEMP."IdPlantillaEmpleado" AND DPEMP."IdTipoProducto" = tipope."IdTipoProducto" AND DPEMP."IdTipoOperacion" = ord."IdTipoOperacion" AND DPEMP."IdEstado" = 'ACT'
		LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" as DPEMP_TODOS ON PEMP."IdPlantillaEmpleado" = DPEMP_TODOS."IdPlantillaEmpleado" AND DPEMP_TODOS."IdTipoProducto" = tipope."IdTipoProducto" AND DPEMP_TODOS."IdTipoOperacion" = '0' AND DPEMP_TODOS."IdEstado" = 'ACT'
		WHERE ord."IdTrabajo" = :IdTrabajo;

		
