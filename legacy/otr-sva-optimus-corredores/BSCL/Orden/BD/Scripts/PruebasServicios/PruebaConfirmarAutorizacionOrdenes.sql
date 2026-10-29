set schema "BSCL";
drop TABLE #TT2;
CREATE LOCAL TEMPORARY TABLE #TT2 ("IdOrden"	BIGINT);
INSERT INTO #TT2 Values(309713);

CALL "BSCL.Orden.BD.Servicios::ConfirmarAutorizacionOrdenes" (
	p_IdEmpresa 	=> 1,
	p_IdUsuario 	=> 4,
	p_IdModulo 	=> 'ORD',
	p_Ordenes 	=> #TT2
) ;

CALL "BSCL.Orden.BD.Servicios::RechazaAutorizacionOrdenes" (
	p_IdEmpresa 	=> 1,
	p_IdUsuario 	=> 4,
	p_IdModulo 	=> 'ORD',
	p_Ordenes 	=> #TT2
);
