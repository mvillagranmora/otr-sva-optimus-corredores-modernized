SELECT
	 SPT."NumeroOperacion"
    ,:IdEmpresa AS "IdEmpresa"
    ,SPT."FechaOperacion"
    ,SPT."IdMonedaPrincipal"
    ,SPT."IdMonedaSecundaria"
	,SPT."IdMesa"
    ,MES."IdEstadoMesa"
	,MLO."IdEstado" AS "IdEstadoModulo"
	,MLO."TipoEstado" AS "TipoEstadoModulo"
	,CASE 
		WHEN (COALESCE(TO_CHAR(EJE."FechaFin",'YYYY-MM-DD HH24:MI:SS'),'') = '0001-01-01 00:00:00') AND COALESCE(FEJE."Estado", '') <> 'TER' THEN 'S' 
		ELSE 'N' 
	 END AS "PautaEnEjecucion"
	,COALESCE(DEJL."FechaFin",TO_TIMESTAMP('0001-01-01 04:00:01','YYYY-MM-DD HH24:MI:SS'))  AS "UltimoProcesoCambioFecha"
	,MOD."IdEstado" AS "EstadoMesaSpot"
	,CF."EstadoAporteComercial" AS "EstadoFondo"
	,COALESCE(DEJ."Estado",'') AS "EstadoProcesoCambioFecha"
	,TIPCOM."IdTipoComprobante"
	,TIPCOM."IdEstadoFacturacion"
	,CASE WHEN SPT."IdTipoCanalIngreso" = 'FMM' THEN PARUSDFFMM."TipoCambioMercado"
		  ELSE PARUSD."TipoCambioMercado" END 	AS "TipoCambioUSD"
	,CASE WHEN SPT."IdTipoCanalIngreso" = 'FMM' THEN PARFFMM."TipoCambioMercado"
		  ELSE PAR."TipoCambioMercado" END 	AS "TipoCambioPrincipal"
	,CASE WHEN SPT."IdTipoCanalIngreso" = 'FMM' THEN PARFFMM."TipoCambioMercado"
		  ELSE PAR."TipoCambioMercado" END 	AS "ParidadPrincipal"
	,CLI."IdCliente"
	,CLI."IdEstado" AS "IdEstadoCliente"
	,CLI."EsEnrolado" AS "ClienteEsEnrolado"
	,POR."IdPortafolio"
	,POR."IdEstado" AS "IdEstadoPortafolio"
	,POR."IdEstadoAutorizacion"
	,CTAINVPRI."IdEstado" AS "IdEstadoCuentaInversionMonedaPrincipal"
	,CTAINVSEC."IdEstado" AS "IdEstadoCuentaInversionMonedaSecundaria"
	,CASE WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'ACT' AND
			   IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'ACT' AND
			   (IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '' OR
				IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = SPT."IdTipoOperacion" OR
				IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '0') THEN 'OK'
		  ELSE 'NOK' END						AS "ResultadoPerfilProducto"
	,CASE WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'INA'	THEN 'PERFIL_PRODUCTO_INACTIVO'
		  WHEN IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'INA'					THEN 'PRODUCTO_INACTIVO'
		  WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'BLO'	THEN 'PERFIL_PRODUCTO_BLOQUEADO'
		  WHEN IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'BLO'					THEN 'PRODUCTO_BLOQUEADO'
		  WHEN IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") NOT IN ('','0',SPT."IdTipoOperacion")	THEN 'TIPO_OPERACION_SIN_VALOR'
		  ELSE 'ERROR_PRODUCTO' END				AS "MensajePerfilProducto"
	,SPT."IdSolicitudAporteRescate"
	,POR."CodigoPortafolio"
	,DOC."DocumentoIdentidad"
	,CASE WHEN COALESCE(DPEMP."IdDetallePlantillaEmpleado",DPEMP_TODOS."IdDetallePlantillaEmpleado",-1) > 0 THEN 'S' ELSE 'N' END as "PlantillaEmpleadoProductoHabilitado"
	,CASE WHEN CURRENT_UTCTIMESTAMP <= ADD_SECONDS(CLI."FechaCreacion", COALESCE(TO_BIGINT(OPECIN."Valor"),0)*3600) AND CLI."IdEstado" != 'ACT' THEN 'S' ELSE 'N' END AS "EsClienteNuevoProspecto"
	,INS."Nemotecnico"
    ,SPT."IdEmpleadoDigitador"
FROM "BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso" AS SPT
--LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" AS MES
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES
	ON (SPT."IdMesa" = MES."IdMesa")
--LEFT JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" AS MLO
LEFT JOIN "BSCL.Plataforma.BD::CRE.ModuloEmpresa" AS MLO
	ON (MLO."IdModulo" = 'MSP' AND
		MLO."IdEmpresa" = 1)
LEFT JOIN (SELECT TOP 1 * FROM "BSCL.Plataforma.BD::PAU.Ejecucion"
	ORDER BY "FechaSistema" DESC) AS EJE
	ON (EJE."Estado" = 'PRO')
LEFT JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS MOD
ON MOD."CodigoModulo" = 'MSP' 
LEFT JOIN (SELECT TOP 1 * FROM "BSCL.Plataforma.BD::PAU.EjecucionDetalle" 
WHERE "CodigoProceso" = 'CAMBIO_FECHA_SISTEMA'
	ORDER BY "IdEjecucion" DESC) AS DEJL
	ON DEJL."Estado" <> ''
LEFT JOIN (SELECT TOP 1 * FROM "BSCL.Plataforma.BD::PAU.EjecucionDetalle"
WHERE "CodigoProceso" = 'CAMBIO_FECHA_SISTEMA'
	ORDER BY "IdEjecucion" DESC) AS DEJ
	ON (DEJ."CodigoProceso" = 'CAMBIO_FECHA_SISTEMA')
LEFT JOIN (SELECT TOP 1 * FROM "BSCL.Plataforma.BD::PAU.EjecucionDetalle"
WHERE "CodigoProceso" = 'FIN_PAUTA'
	ORDER BY "IdEjecucion" DESC) AS FEJE
	ON (FEJE."Estado" <> '' AND EJE."FechaSistema" = FEJE."FechaSistema")
--LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante" AS TIPCOM
LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS TIPCOM
	ON (SPT."TipoComprobanteFacturacion" = TIPCOM."IdTipoComprobante" AND
		TIPCOM."IdEstadoFacturacion" = 'ACT')
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PARUSD
	ON (PARUSD."IdMoneda" = 'USD' AND
		TO_CHAR(PARUSD."Fecha",'YYYY-MM-DD') = TO_CHAR(SPT."FechaOperacion",'YYYY-MM-DD'))
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PARUSDFFMM
	ON (PARUSDFFMM."IdMoneda" = 'USD' AND
		TO_CHAR(PARUSDFFMM."Fecha",'YYYY-MM-DD') = TO_CHAR(:FechaSistema,'YYYY-MM-DD'))
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PAR
	ON (PAR."IdMoneda" = SPT."IdMonedaPrincipal" AND
		TO_CHAR(PAR."Fecha",'YYYY-MM-DD') = TO_CHAR(SPT."FechaOperacion",'YYYY-MM-DD'))
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PARFFMM
	ON (PARFFMM."IdMoneda" = SPT."IdMonedaPrincipal" AND
		TO_CHAR(PARFFMM."Fecha",'YYYY-MM-DD') = TO_CHAR(:FechaSistema,'YYYY-MM-DD'))
--LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
	ON (SPT."IdCliente" = CLI."IdCliente")
--LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" AS CF
	ON (CF."IdSerieFondo" = :IdSerieFondo)	
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
	ON (SPT."IdPortafolio" = POR."IdPortafolio")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER 
	ON (CLI."IdPersona" = PER."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC 
	ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
--LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarMonedaClienteCuentaInversion" AS CTAINVPRI
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTAINVPRI
	ON (SPT."IdCliente" = CTAINVPRI."IdCliente" AND
		SPT."IdPortafolio" = CTAINVPRI."IdPortafolio" AND
		SPT."IdMonedaPrincipal" = CTAINVPRI."IdMoneda")
--LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarMonedaClienteCuentaInversion" AS CTAINVSEC
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTAINVSEC
	ON (SPT."IdCliente" = CTAINVSEC."IdCliente" AND
		SPT."IdPortafolio" = CTAINVSEC."IdPortafolio" AND
		SPT."IdMonedaSecundaria" = CTAINVSEC."IdMoneda")
--LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" AS perfExtra
LEFT JOIN (
		SELECT PP."IdPerfilProductos", PP."IdEstado" AS "EstadoPerfilProductos", PP."IdEmpresa", DPP."IdTipoProducto", DPP."IdTipoOperacion", DPP."IdEstado" AS "EstadoProducto"
		FROM "BSCL.Plataforma.BD::COM.PerfilProductos" AS PP
		INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos" AS DPP
			ON (PP."IdPerfilProductos" = DPP."IdPerfilProductos")
	) AS perfExtra
	ON (POR."IdPerfilProductosExtra" = perfExtra."IdPerfilProductos" AND 
		perfExtra."IdTipoProducto" = 'MEME' AND 
		perfExtra."IdEmpresa" = :IdEmpresa AND 
		(perfExtra."IdTipoOperacion" = SPT."IdTipoOperacion" OR perfExtra."IdTipoOperacion" = '0'))
--LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" AS perfBase
LEFT JOIN (
		SELECT PP."IdPerfilProductos", PP."IdEstado" AS "EstadoPerfilProductos", PP."IdEmpresa", DPP."IdTipoProducto", DPP."IdTipoOperacion", DPP."IdEstado" AS "EstadoProducto"
		FROM "BSCL.Plataforma.BD::COM.PerfilProductos" AS PP
		INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos" AS DPP
			ON (PP."IdPerfilProductos" = DPP."IdPerfilProductos")
	) AS perfBase
	ON (POR."IdPerfilProductos" = perfBase."IdPerfilProductos" AND 
		perfBase."IdTipoProducto" = 'MEME' AND 
		perfBase."IdEmpresa" = :IdEmpresa AND 
		(perfBase."IdTipoOperacion" = SPT."IdTipoOperacion" OR perfBase."IdTipoOperacion" = '0'))
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON SPT."IdInstrumento" = INS."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usuario on usuario."IdUsuario" = :IdUsuario AND "TipoEntidad" = 'EMP'
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" emp on SPT."IdAgente" = emp."IdEmpleado"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" empDigitador on usuario."IdEntidad" = empDigitador."IdEmpleado"
LEFT JOIN "BSCL.Plataforma.BD::COM.PlantillaEmpleado" as PEMP ON empDigitador."IdPlantillaEmpleado" = PEMP."IdPlantillaEmpleado" AND empDigitador."UtilizaPlantillaEmpleado" = 'S' AND PEMP."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" as DPEMP ON PEMP."IdPlantillaEmpleado" = DPEMP."IdPlantillaEmpleado" AND DPEMP."IdTipoProducto" = 'MEME' AND DPEMP."IdTipoOperacion" = SPT."IdTipoOperacion" AND DPEMP."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" as DPEMP_TODOS ON PEMP."IdPlantillaEmpleado" = DPEMP_TODOS."IdPlantillaEmpleado" AND DPEMP_TODOS."IdTipoProducto" = 'MEME' AND DPEMP_TODOS."IdTipoOperacion" = '0' AND DPEMP_TODOS."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS OPECIN ON OPECIN."CodigoParametro" = 'PLA.HorasOperaCINClienteNuevoBloqueado'
WHERE SPT."NumeroOperacion" IN (:NumeroOperacion);
