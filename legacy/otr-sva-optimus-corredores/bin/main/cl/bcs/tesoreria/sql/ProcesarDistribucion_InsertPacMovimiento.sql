INSERT INTO "BSCL.Tesoreria.BD::TES.PacMovimiento"
(
"IdMovimientoPac"
, "IdRecaudacion"
, "IdEmpresa"
, "IdUsuario"
, "IdModulo"
--, "IdTipoMovimiento" 
, "IdSecuenciaMovimiento"
--, "TipoEntidad"
, "FechaIngreso"
, "FechaIngresoRegistro" 
)
VALUES(
		"BSCL.Tesoreria.BD.Secuencias::PacMovimiento" .NEXTVAL 
		,:IdRecaudacion
		,:IdEmpresa
		,:IdUsuario
		,:IdModulo
--		,:IdTipoMovimiento
		,:IdSecuenciaMovimiento
	--	,:TipoEntidad
		,:FechaIngreso
		,CURRENT_UTCTIMESTAMP 
);