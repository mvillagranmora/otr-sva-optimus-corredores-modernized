INSERT INTO "BSCL.SocioNegocio.BD::SOC.ConsultarFichaSocioNegocio_IndicadorEntidad"
	( "IdTrabajo"
	, "IdEmpresa"
	, "IdTipoEntidad"
	, "IdEntidad"
	, "IdTipoIndicador"
	, "CodigoTipoIndicador")
VALUES
	( :IdTrabajo
	, :IdEmpresa
	, :IdTipoEntidad
	, :IdEntidad
	, :IdTipoIndicador
	, :CodigoTipoIndicador);