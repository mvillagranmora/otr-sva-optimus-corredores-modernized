SET SCHEMA BSCL;
create local temporary table #asdf("NombreParametro" nvarchar(100) , "Valor" nvarchar(5000));
insert into #asdf select 'IdEmpresa','1' from dummy;
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
 (
	1,
	0,
	'pla',
	0,
	'ACTUALIZAR_INSTRUMENTOS',   
	0,
	#asdf,
	'S',
	?,
	?,
	?,	
	?,
	?							
) ;

commit;

CALL "BSCL.Plataforma.BD.Servicios::MER.ActualizaEmisores"('N',?,?);

