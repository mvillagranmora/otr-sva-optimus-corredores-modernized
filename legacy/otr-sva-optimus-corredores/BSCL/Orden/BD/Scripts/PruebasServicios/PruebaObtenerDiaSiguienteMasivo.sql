SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT 
(
	"FechaInicial" 	NVARCHAR(10),
	"NumeroDias"	BIGINT
);

INSERT INTO #TT ("FechaInicial","NumeroDias")
select '2016-12-29',364 FROM dummy;

'v_fechasVencimientoIN';FechaInicial;NumeroDias
v_fechasVencimientoIN;2016-12-29;364

CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguienteMasivo"(
	p_IdPais	=> 44,
	p_DiaHabil	=> 'N',
	p_FechasIN	=> #TT,
	p_FechasOUTf => ?
);

select *
FROM "BSCL"."BSCL.Orden.BD::ORD.Orden" ord
INNER JOIN :p_FechasOUTf fec on fec."FechaOperacion" = TO_NVARCHAR(ord."FechaOperacion",'YYYY-MM-DD') and "NumeroDias" = 1
where "IdOrden" = 309700

rollback;