SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdInterno"						BIGINT,
		"IdOrden"						BIGINT,
		"IdTipoCanalIngreso"			NVARCHAR(3),
		"MontoTotalOrden"				DECIMAL(26,8),
		"MontoMaximoDistribucionOrden"	DECIMAL(26,8)
);

insert into #TT
select 1,1,'AGE',12000,100 from dummy;

CALL "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosCrearOrdenFase2"
(	p_Ordenes => #TT,
	p_out_SalidaDetalle => ?,
	p_out_Salida => ?
);

