UPDATE SPT
SET
	 SPT."IdInstrumento" = CASE WHEN SPT."IdInstrumento" = 0 AND COALESCE(INS."IdInstrumento",0) <> 0 THEN INS."IdInstrumento" ELSE SPT."IdInstrumento" END
	,SPT."IdCliente" = CASE WHEN SPT."IdCliente" = 0 AND COALESCE(CLI."IdCliente",0) <> 0 THEN CLI."IdCliente" ELSE SPT."IdCliente" END
	,SPT."IdPortafolio" = CASE WHEN SPT."IdPortafolio" = 0 AND COALESCE(CLI."IdPortafolioDefecto",0) <> 0 THEN CLI."IdPortafolioDefecto"
							   WHEN SPT."IdPortafolio" = 0 AND COALESCE(POR."IdPortafolio",0) <> 0 THEN POR."IdPortafolio"
							   ELSE SPT."IdPortafolio" END
	,SPT."NombreCliente" = COALESCE(PER."NombreCompleto",'')
	,SPT."IdUsuarioCreador" = CASE WHEN SPT."IdUsuarioCreador" = 0 AND COALESCE(USU."IdUsuario",0) <> 0 THEN USU."IdUsuario" ELSE SPT."IdUsuarioCreador" END
	,SPT."IdOperacionSpot" = CASE WHEN SPT."IdOperacionSpot" = 0 AND COALESCE(SPTUPD."IdOperacionSpot",0) <> 0 THEN SPTUPD."IdOperacionSpot" ELSE SPT."IdOperacionSpot" END
FROM "BSCL.Mesa.BD::SPT.OperacionSpotDatatec" SPT
--IdInstrumento
LEFT JOIN "BSCL.Mesa.BD::MES.InstrumentoMoneda" INS
	ON (SPT."IdMonedaPrincipal" = INS."IdMonedaPrincipal" AND
		SPT."IdEmpresa" = INS."IdEmpresa" AND
		INS."IdMonedaSecundaria" = 'CLP' AND
		INS."IdTipoInstrumento" = 'SPT' AND
		INS."Nemotecnico" = 'INTERB')
--IdCliente
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIPCLI
	ON (TIPCLI."IdTipoEntidad" = 'CLIENTE' AND
		TIPCLI."GrupoIndicador" = 'GENERALES' AND
		TIPCLI."CodigoTipoIndicador" = 'CODIGO_DATATEC' AND
		SPT."IdEmpresa" = TIPCLI."IdEmpresa" AND
		TIPCLI."IdEstado" != 'INA')
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" INDCLI
	ON (TIPCLI."IdTipoIndicador" = INDCLI."IdTipoIndicador" AND
		SPT."InstitucionValidacion" = INDCLI."ValorIndicador")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	ON (INDCLI."IdEntidad" = CLI."IdCliente" AND
		SPT."IdEmpresa" = CLI."IdEmpresa" AND
		CLI."Borrado" = 'N')
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
	ON (CLI."IdPersona" = PER."IdPersona")
--IdPortafolio
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
	ON (INDCLI."IdEntidad" = POR."IdCliente" AND
		POR."IdEstado" != 'INA')
--IdUsuarioCreador
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIPEMP
	ON (TIPEMP."IdTipoEntidad" = 'EMPLEADO' AND
		TIPEMP."GrupoIndicador" = 'GENERAL' AND
		TIPEMP."CodigoTipoIndicador" = 'CODIGO_DATATEC' AND
		SPT."IdEmpresa" = TIPEMP."IdEmpresa" AND
		TIPEMP."IdEstado" != 'INA')
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" INDEMP
	ON (TIPEMP."IdTipoIndicador" = INDEMP."IdTipoIndicador" AND
		SPT."UsuarioValidacion" = INDEMP."ValorIndicador")
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
	ON (INDEMP."IdEntidad" = EMP."IdEmpleado" AND
		SPT."IdEmpresa" = EMP."IdEmpresa" AND
		EMP."Borrado" = 'N')
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU
	ON (INDEMP."IdEntidad" = USU."IdEntidad" AND
		SPT."IdEmpresa" = USU."IdEmpresa" AND
		USU."IdEstado" = 'ACT')
LEFT JOIN  "BSCL.Mesa.BD::SPT.OperacionSpotDatatec" SPTUPD
	ON (SPT."IdDatatec" = SPTUPD."IdDatatec" AND
		SPT."ClaseMensaje" <> SPTUPD."ClaseMensaje" AND
		SPTUPD."IdOperacionSpot" <> 0)
WHERE SPT."IdUnicoDatatec" IN (:IdUnicoDatatec)
  AND SPT."NumeroRegistro" IN (:NumeroRegistro)
  AND SPT."IdEmpresa" = :IdEmpresa
  --AND SPT."ClaseMensaje" = '5'
;