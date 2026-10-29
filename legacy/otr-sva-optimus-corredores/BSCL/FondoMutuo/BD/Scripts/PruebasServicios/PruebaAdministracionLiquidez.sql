set schema bscl;

CALL "BSCL.FondoMutuo.BD.Servicios::AdministracionLiquidez" (
	--VARIABLES OBLIGATORIAS
    p_IdEmpresa => 1,
    p_IdUsuario => 21,
    p_IdModulo => 'FMM',
    p_SimularOperacion => 'N',
    p_UsaReglasNegocio => 'S',
    --VARIABLES DEL PROCEDIMIENTO
	p_FechaProceso	=> '2016-12-05',
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);