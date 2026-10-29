SET SCHEMA "BSCL";

CALL "BSCL.Orden.BD.Servicios::CrearCorredorCorresponsal"
(	p_IdEmpresa  	=> 1 ,
	p_IdUsuario  	=> 3 ,
	p_IdModulo  	=> 'ORD' ,
	
	p_IdPais 			=> 44 ,
	p_CodigoCorredor 	=> 'EXTRANJERO 1' ,
	p_IdCobro 			=> 'COVA' ,
	p_IdEstado 			=> 'ACT' ,
	p_ValorCobro 		=> 0.02 ,
	
	p_out_InformacionContador => ?,
	p_out_InformacionDetalle => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);
