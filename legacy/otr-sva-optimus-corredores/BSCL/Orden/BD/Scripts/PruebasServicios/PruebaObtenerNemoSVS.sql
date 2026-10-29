SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdInterno"			BIGINT,
				"IdEmisor"			NVARCHAR(40),
				"Fecha"				NVARCHAR(10),
				"Plazo"				BIGINT,
				"Nemotecnico"		NVARCHAR(40),
				"IdTipoReajuste"	NVARCHAR(3),
				"PagaderoEn"		NVARCHAR(3)
);

insert into #TT
select 1,'ARAUCANA','2016-12-30',35,'PAGARE R','USD','CLP' from dummy;

insert into #TT
select 3726044 from dummy;


CALL "BSCL.Plataforma.BD.Servicios::ObtenerNemoSvsMM" (p_in => #tt,p_out_NemoSvs=>?);

SELECT * FROM "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" where "TipoEntidad" = '3' AND "EmiteIIF" = 'S' order by "IdEmisor"

rollback
