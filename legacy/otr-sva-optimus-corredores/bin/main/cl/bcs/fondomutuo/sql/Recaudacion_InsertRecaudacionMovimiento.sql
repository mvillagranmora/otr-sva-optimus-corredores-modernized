INSERT INTO "BSCL.FondoMutuo.BD::TRA.RecaudacionMovimiento"
(
"IdMovimientoRecaudacion"
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
		"BSCL.FondoMutuo.BD.Secuencias::RecaudacionMovimiento".NEXTVAL
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