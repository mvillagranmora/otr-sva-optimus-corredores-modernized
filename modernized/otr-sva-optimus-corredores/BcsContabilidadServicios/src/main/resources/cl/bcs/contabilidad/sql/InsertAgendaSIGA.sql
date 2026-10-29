set schema "BSCL";

delete from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma" = 'InterfazSIGADiaria';

drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
--insert into #param values('schema','BSCL');

call "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"
(
1,
0,
'CON',
'InterfazSIGADiaria',
'S',
'**',
'*',
'**',
'00',
'00',
'N',
'N',
'N',
'S',
#param,

?,
?,
?,
?,
?,
?);

COMMIT;


SELECT * FROM "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma" = 'InterfazSIGADiaria';