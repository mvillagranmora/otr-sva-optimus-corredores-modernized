SET SCHEMA "BSCL";

CALL "BSCL.Plataforma.BD.Servicios::ConsultarPlazo"(
		
	p_IdEmpresa         => 1,
    p_IdUsuario         => 1,
    p_IdModulo         => 'ORD',
    p_UsaReglasNegocio => 'S',
    
    --ENUMERAR VARIABLES DE ENTRADA
    p_FechaDesde => '2016-06-16',
    p_FechaHasta =>	'2016-06-27',
    p_DiasHabiles => 'N',
 	p_IdCondicionDeLiquidacion  => 'T2',
	p_IdPais => 44,
	
	p_out_PlazoDias => ?,

	-- Salidas para mensajes de advertencias y errores
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
	
);

CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerPlazo"(
	p_FechaDesde					=> '2016-06-16',
    p_FechaHasta					=> '2016-06-27',
    p_DiasHabiles					=> 'N',
    p_DiasCondicionDeLiquidacion	=> 2,
	p_IdPais						=> 44,
	p_out_PlazoDias 				=> ?
	);