SET SCHEMA "BSCL";
drop TABLE #TRANSACCION;
CREATE LOCAL TEMPORARY TABLE #TRANSACCION (
		"IdMovimiento" 	BIGINT,
		"IdTransaccion"	BIGINT,
		"Cantidad" DECIMAL (26,8),
		"Monto" DECIMAL (26,8),
		"ValorRescate" DECIMAL (26,8)
		);

drop TABLE #ORDEN;
CREATE LOCAL TEMPORARY TABLE #ORDEN (
		"IdOrden"		BIGINT
		
);

insert into #TRANSACCION select 70286220,70286220,999,0,0 from DUMMY;
insert into #ORDEN select 310864 from dummy;

CALL "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionCronologica" (
	p_IdEmpresa 			=> 1,
	p_IdTipo 				=> 'ASIG',
	p_Origen 				=> '',
	p_Transacciones 		=> #TRANSACCION,
	p_Ordenes 				=> #ORDEN,
	p_out_SimAsignacion 		=> ?);
	
	
						rollback
