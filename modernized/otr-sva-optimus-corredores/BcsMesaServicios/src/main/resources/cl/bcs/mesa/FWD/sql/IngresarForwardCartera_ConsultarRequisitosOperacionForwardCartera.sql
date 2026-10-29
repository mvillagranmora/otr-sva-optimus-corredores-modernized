SELECT
	 TO_VARCHAR(FWD."FolioInterno")				AS "FolioInterno"
    ,:IdEmpresa									AS "IdEmpresa"
    ,FWD."IdMesa"
    ,MES."IdEstadoMesa"
    ,FWD."MontoOperacionNuevo"					AS "MontoOperacion"
    ,FWD."MontoInicialNuevo"					AS "MontoInicial"
    ,FWD."MontoFinalNuevo"						AS "MontoFinal"
    ,INS."IdTipoInstrumento"
    ,INS."Nemotecnico"
    ,FWD."FechaOperacion"
    ,FWD."IdTipoOperacionOriginal"				AS "IdTipoOperacion"
    ,FWD."FechaVencimientoNuevo"				AS "FechaVencimiento"
    ,FWD."PlazoNuevo"							AS "Plazo"
    ,COALESCE(CTAINVPRI."IdEstado",'')			AS "IdEstadoCuentaInversionMonedaPrincipal"
    ,COALESCE(REQ."ValorIndicador",'N')			AS "ValorIndicadorFirmaCCGFWD"
    ,MLO."IdEstado"								AS "IdEstadoModulo"
	,MLO."TipoEstado"							AS "TipoEstadoModulo"
	,CASE WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'ACT' AND
			   IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'ACT' AND
			   (IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '' OR
				IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = FWD."IdTipoOperacionOriginal" OR
				IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '0') THEN 'OK'
		  ELSE 'NOK' END						AS "ResultadoPerfilProducto"
	,CASE WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'INA'	THEN 'PERFIL_PRODUCTO_INACTIVO'
		  WHEN IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'INA'					THEN 'PRODUCTO_INACTIVO'
		  WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'BLO'	THEN 'PERFIL_PRODUCTO_BLOQUEADO'
		  WHEN IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'BLO'					THEN 'PRODUCTO_BLOQUEADO'
		  WHEN IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") NOT IN ('','0',FWD."IdTipoOperacionOriginal")	THEN 'TIPO_OPERACION_SIN_VALOR'
		  ELSE 'ERROR_PRODUCTO' END				AS "MensajePerfilProducto"
	,COALESCE(CLI."IdCliente",0)				AS "IdCliente"
	,COALESCE(CLI."IdEstado",'')				AS "IdEstadoCliente"
	,COALESCE(POR."IdPortafolio",0)				AS "IdPortafolio"
	,COALESCE(POR."IdEstado",'')				AS "IdEstadoPortafolio"
	,FWD."TCCostoNuevo"							AS "TCCosto"
	,FWD."TCCierreNuevo"						AS "TCCierre"
	,FWD."ParidadCierreNuevo"					AS "ParidadCierre"
	,COALESCE(PARUF."TipoCambioMercado",0)		AS "TipoCambioUF"
FROM "BSCL.Mesa.BD::FWD.IngresarForwardCartera_Proceso" FWD
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" MES
	ON (FWD."IdMesa" = MES."IdMesa" AND
		MES."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS
	ON (FWD."IdInstrumento" = INS."IdInstrumento")
LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarMonedaClienteCuentaInversion" CTAINVPRI
	ON (FWD."IdClienteNuevo" = CTAINVPRI."IdCliente" AND
		FWD."IdPortafolioNuevo" = CTAINVPRI."IdPortafolio" AND
		FWD."IdMonedaPrincipal" = CTAINVPRI."IdMoneda")
LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarRequisitosOperativosFWDCliente" REQ
	ON (FWD."IdClienteNuevo" = REQ."IdCliente" AND
		REQ."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" MLO 
	ON (MLO."IdModulo" = 'MFW' AND
		MLO."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	ON (FWD."IdClienteNuevo" = CLI."IdCliente")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
	ON (FWD."IdPortafolioNuevo" = POR."IdPortafolio" AND
		FWD."IdClienteNuevo" = POR."IdCliente")
LEFT JOIN ( SELECT	PP."IdPerfilProductos", PP."IdEstado" as "EstadoPerfilProductos", PP."IdEmpresa", DPP."IdTipoProducto",
					DPP."IdTipoOperacion",	DPP."IdEstado" as "EstadoProducto"
			FROM	"BSCL.Plataforma.BD::COM.PerfilProductos" as PP
					INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos" as DPP 
						ON (PP."IdPerfilProductos" = DPP."IdPerfilProductos") ) perfExtra
	ON (POR."IdPerfilProductosExtra" = perfExtra."IdPerfilProductos" AND 
		perfExtra."IdTipoProducto" = 'MEFW' AND 
		perfExtra."IdEmpresa" = :IdEmpresa AND 
		(perfExtra."IdTipoOperacion" = FWD."IdTipoOperacionOriginal" OR perfExtra."IdTipoOperacion" = '0'))
LEFT JOIN ( SELECT	PP."IdPerfilProductos", PP."IdEstado" as "EstadoPerfilProductos", PP."IdEmpresa", DPP."IdTipoProducto",
					DPP."IdTipoOperacion",	DPP."IdEstado" as "EstadoProducto"
			FROM	"BSCL.Plataforma.BD::COM.PerfilProductos" as PP
					INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos" as DPP 
						ON (PP."IdPerfilProductos" = DPP."IdPerfilProductos") ) perfBase
	ON (POR."IdPerfilProductos" = perfBase."IdPerfilProductos" AND 
		perfBase."IdTipoProducto" = 'MEFW' AND 
		perfBase."IdEmpresa" = :IdEmpresa AND 
		(perfBase."IdTipoOperacion" = FWD."IdTipoOperacionOriginal" OR perfBase."IdTipoOperacion" = '0'))
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARUF
	ON (PARUF."IdMoneda" = 'UF' AND
		PARUF."Fecha" = FWD."FechaOperacion")
WHERE FWD."FolioInterno" IN (:FolioInterno);
