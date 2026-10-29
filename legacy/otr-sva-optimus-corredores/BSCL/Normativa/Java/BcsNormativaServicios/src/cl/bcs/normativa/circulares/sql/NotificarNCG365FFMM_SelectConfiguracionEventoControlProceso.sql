SELECT "ControlProceso" 
FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" 
WHERE "IdEmpresa" = :IdEmpresa AND "IdConfiguracionEvento" = (
	SELECT "IdConfiguracionEvento" FROM "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" WHERE "CodigoTipoEvento" = 'NOTIFICAR_NCG365FFMM_CLIENTE'
);
