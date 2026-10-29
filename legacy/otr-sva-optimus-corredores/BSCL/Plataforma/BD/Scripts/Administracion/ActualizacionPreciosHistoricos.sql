create local temporary table #asdf("NombreParametro" nvarchar(100) , "Valor" nvarchar(5000));
insert into #asdf select 'FechaDesde','2018-01-01' from dummy;
insert into #asdf select 'FechaHasta','2018-01-31' from dummy;
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
 (
	1,
	0,
	'pla',
	0,
	'ACTUALIZA_PRECIOS_HISTORICOS',   
	0,
	#asdf,
	'S',
	?,
	?,
	?,	
	?,
	?							
) 

rollback
commit
