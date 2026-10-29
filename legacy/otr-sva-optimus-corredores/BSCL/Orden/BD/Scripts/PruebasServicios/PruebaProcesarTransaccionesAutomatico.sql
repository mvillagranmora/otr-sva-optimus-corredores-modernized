SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"IdTransaccion"		bigint
);

INSERT INTO #TT ("IdTransaccion")
SELECT 242339 FROM DUMMY;




CALL "BSCL.Orden.BD.Servicios::ProcesarTransaccionesAutomatico" (
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 0,
	p_IdModulo						=> 'ORD',
	p_Transacciones					=> #TT,

	p_out_Resultado					=> ?,
							
	p_out_AdvertenciasDetalleCrearOrdenPostrera		=> ?,
	p_out_ErroresDetalleCrearOrdenPostrera 			=> ?,
	
	p_out_ErroresDetalleCrearMovFacturarAsignacion		=> ?,
	p_out_AdvertenciasDetalleCrearMovFacturarAsignacion	=> ?,
	
	p_out_ErroresDetalleCrearComprobante		=> ?,
	p_out_AdvertenciasDetalleCrearComprobante		=> ?,
	
	p_out_AdvertenciasContador=> ?,
	p_out_AdvertenciasDetalle=> ?,
	p_out_ErroresContador=> ?,
	p_out_ErroresDetalle=> ?
	
	);
	
	ROLLBACK;
	commit;
