SET SCHEMA "BSCL";


CALL "BSCL.Orden.BD.Servicios::ConsultarCustodiaSubyacenteSimultanea" (
	p_IdEmpresa    => 1,
	p_IdUsuario    => 25,
	p_IdModulo    => 'ORD',
	p_IdPais    => 44,

	-- Parametros entrada del servicio publico
	p_IdPortafolio    => 189419,
	p_IdBolsa    => 'BCS',
	p_plazoDias    => 0,
	

	-- Parametros salida del servicio publico
	p_Custodia    => ?,
	p_out_AdvertenciasContador    => ?,
	p_out_AdvertenciasDetalle    => ?,
	p_out_ErroresContador    => ?,
	p_out_ErroresDetalle    => ?
) ;

rollback