set schema "BSCL";

delete from "BSCL"."BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma" = 'ProcesoProgramadoCartolas';

drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('schema','BSCL');

call "BSCL"."BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"
(
1,
0,
'CAR',

'ProcesoProgramadoCartolas',
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
