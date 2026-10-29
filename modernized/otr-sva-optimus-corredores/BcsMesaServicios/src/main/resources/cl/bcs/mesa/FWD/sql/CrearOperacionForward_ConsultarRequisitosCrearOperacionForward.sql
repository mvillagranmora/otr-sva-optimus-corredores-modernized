SELECT
	 TO_VARCHAR(FWD."FolioInterno")				AS "FolioInterno"
    ,:IdEmpresa									AS "IdEmpresa"
    ,FWD."IdMesa"
    ,MES."IdEstadoMesa"
    ,FWD."MontoOperacion"
    ,FWD."MontoInicial"
    ,FWD."MontoFinal"
    ,INS."IdTipoInstrumento"
    ,INS."Nemotecnico"
    ,FWD."FechaOperacion"
    ,FWD."IdTipoOperacion"
    ,FWD."FechaVencimiento"
    ,FWD."Plazo"
    ,COALESCE(CTAINVPRI."IdEstado",'')			AS "IdEstadoCuentaInversionMonedaPrincipal"
    ,COALESCE(REQ."ValorIndicador",'N')			AS "ValorIndicadorFirmaCCGFWD"
    ,MLO."IdEstado"								AS "IdEstadoModulo"
	,MLO."TipoEstado"							AS "TipoEstadoModulo"
	,CASE WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'ACT' AND
			   IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'ACT' AND
			   (IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '' OR
				IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = FWD."IdTipoOperacion" OR
				IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '0') THEN 'OK'
		  ELSE 'NOK' END						AS "ResultadoPerfilProducto"
	,CASE WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'INA'	THEN 'PERFIL_PRODUCTO_INACTIVO'
		  WHEN IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'INA'					THEN 'PRODUCTO_INACTIVO'
		  WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'BLO'	THEN 'PERFIL_PRODUCTO_BLOQUEADO'
		  WHEN IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'BLO'					THEN 'PRODUCTO_BLOQUEADO'
		  WHEN IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") NOT IN ('','0',FWD."IdTipoOperacion")	THEN 'TIPO_OPERACION_SIN_VALOR'
		  ELSE 'ERROR_PRODUCTO' END				AS "MensajePerfilProducto"
	,COALESCE(CLI."IdCliente",0)				AS "IdCliente"
	,COALESCE(CLI."IdEstado",'')				AS "IdEstadoCliente"
	,COALESCE(POR."IdPortafolio",0)				AS "IdPortafolio"
	,COALESCE(POR."IdEstado",'')				AS "IdEstadoPortafolio"
	,FWD."TCCosto"
	,FWD."TCCierre"
	,FWD."ParidadCierre"
	,COALESCE(PARUF."TipoCambioMercado",0)		AS "TipoCambioUF"
	,DOC."DocumentoIdentidad"
	,POR."CodigoPortafolio"
	,CASE WHEN COALESCE(DPEMP."IdDetallePlantillaEmpleado",DPEMP_TODOS."IdDetallePlantillaEmpleado",-1) > 0 THEN 'S' ELSE 'N' END as "PlantillaEmpleadoProductoHabilitado"
FROM "BSCL.Mesa.BD::FWD.CrearOperacionForward_Proceso" FWD
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" MES
	ON (FWD."IdMesa" = MES."IdMesa" AND
		MES."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS
	ON (FWD."IdInstrumento" = INS."IdInstrumento")
LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarMonedaClienteCuentaInversion" CTAINVPRI
	ON (FWD."IdCliente" = CTAINVPRI."IdCliente" AND
		FWD."IdPortafolio" = CTAINVPRI."IdPortafolio" AND
		FWD."IdMonedaPrincipal" = CTAINVPRI."IdMoneda")
LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarRequisitosOperativosFWDCliente" REQ
	ON (FWD."IdCliente" = REQ."IdCliente" AND
		REQ."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" MLO 
	ON (MLO."IdModulo" = 'MFW' AND
		MLO."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	ON (FWD."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
	ON (FWD."IdPortafolio" = POR."IdPortafolio" AND
		FWD."IdCliente" = POR."IdCliente")
LEFT JOIN ( SELECT	PP."IdPerfilProductos", PP."IdEstado" as "EstadoPerfilProductos", PP."IdEmpresa", DPP."IdTipoProducto",
					DPP."IdTipoOperacion",	DPP."IdEstado" as "EstadoProducto"
			FROM	"BSCL.Plataforma.BD::COM.PerfilProductos" as PP
					INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos" as DPP 
						ON (PP."IdPerfilProductos" = DPP."IdPerfilProductos") ) perfExtra
	ON (POR."IdPerfilProductosExtra" = perfExtra."IdPerfilProductos" AND 
		perfExtra."IdTipoProducto" = 'MEFW' AND 
		perfExtra."IdEmpresa" = :IdEmpresa AND 
		(perfExtra."IdTipoOperacion" = FWD."IdTipoOperacion" OR perfExtra."IdTipoOperacion" = '0'))
LEFT JOIN ( SELECT	PP."IdPerfilProductos", PP."IdEstado" as "EstadoPerfilProductos", PP."IdEmpresa", DPP."IdTipoProducto",
					DPP."IdTipoOperacion",	DPP."IdEstado" as "EstadoProducto"
			FROM	"BSCL.Plataforma.BD::COM.PerfilProductos" as PP
					INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos" as DPP 
						ON (PP."IdPerfilProductos" = DPP."IdPerfilProductos") ) perfBase
	ON (POR."IdPerfilProductos" = perfBase."IdPerfilProductos" AND 
		perfBase."IdTipoProducto" = 'MEFW' AND 
		perfBase."IdEmpresa" = :IdEmpresa AND 
		(perfBase."IdTipoOperacion" = FWD."IdTipoOperacion" OR perfBase."IdTipoOperacion" = '0'))
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARUF
	ON (PARUF."IdMoneda" = 'UF' AND
		PARUF."Fecha" = FWD."FechaOperacion")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON CLI."IdPersona" = DOC."IdPersona" AND DOC."Predeterminado" = 'S'
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usuario on usuario."IdUsuario" = :IdUsuario AND "TipoEntidad" = 'EMP'
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" emp on FWD."IdPersonaEjecutivo" = emp."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" empDigitador on usuario."IdEntidad" = empDigitador."IdEmpleado"
LEFT JOIN "BSCL.Plataforma.BD::COM.PlantillaEmpleado" as PEMP ON empDigitador."IdPlantillaEmpleado" = PEMP."IdPlantillaEmpleado" AND empDigitador."UtilizaPlantillaEmpleado" = 'S' AND PEMP."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" as DPEMP ON PEMP."IdPlantillaEmpleado" = DPEMP."IdPlantillaEmpleado" AND DPEMP."IdTipoProducto" = 'MEFW' AND DPEMP."IdTipoOperacion" = FWD."IdTipoOperacion" AND DPEMP."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" as DPEMP_TODOS ON PEMP."IdPlantillaEmpleado" = DPEMP_TODOS."IdPlantillaEmpleado" AND DPEMP_TODOS."IdTipoProducto" = 'MEFW' AND DPEMP_TODOS."IdTipoOperacion" = '0' AND DPEMP_TODOS."IdEstado" = 'ACT'
WHERE FWD."FolioInterno" IN (:FolioInterno);
