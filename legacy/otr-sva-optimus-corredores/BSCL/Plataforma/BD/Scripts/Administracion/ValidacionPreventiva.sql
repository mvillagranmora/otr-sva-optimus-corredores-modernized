SET SCHEMA BSCL;
DROP TABLE #asdf;
create local temporary table #asdf("NombreParametro" nvarchar(100) , "Valor" nvarchar(5000));
insert into #asdf select 'FechaSistemaEjecucion','2020-09-25' from dummy;
insert into #asdf select 'IdEmpresa','1' from dummy;
insert into #asdf select 'IdUsuario','0' from dummy;
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
 (
	1,
	0,
	'pla',
	0,
	'ValidacionPreventiva',   
	0,
	#asdf,
	'S',
	?,
	?,
	?,	
	?,
	?							
);

--rollback
commit;



SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.ResumenReplica"
