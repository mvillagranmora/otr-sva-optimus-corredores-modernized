SET SCHEMA bscl;
DROP TABLE #asdf;
create local temporary table #asdf("NombreParametro" nvarchar(100) , "Valor" nvarchar(5000));
insert into #asdf select 'IdEmpresa','1' from dummy;
insert into #asdf select 'IdUsuario','0' from dummy;
insert into #asdf select 'IdClasificacionTipoInstrumento','RV;FM' from dummy;
insert into #asdf select 'Fecha','2022-05-31' from dummy;
CALL bscl."BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
 (
	1,
	0,
	'pla',
	0,
	'RegistrarPrecioInstrumentosOnDemand',   
	0,
	#asdf,
	'S',
	?,
	?,
	?,	
	?,
	?							
) ;

COMMIT;
