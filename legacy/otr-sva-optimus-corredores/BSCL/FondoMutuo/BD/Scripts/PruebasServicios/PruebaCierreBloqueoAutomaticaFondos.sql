SET SCHEMA BSCL;

CALL "BSCL.FondoMutuo.BD.Servicios::CierreBloqueoAutomaticoFondos" (
	--VARIABLES OBLIGATORIAS
    p_IdEmpresa => 1,
    p_IdUsuario => 21,
    p_out_Resultado => ?,
    p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
	);
	
	SELECT "HoraCierreAporteComercial","UsaHorarioAporteDefault" FROM  "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO WHERE FONDO."UsaHorarioAporteDefault" = 'N'
