INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"
select 'InterfazSIGADemanda','Interfaz SIGA a Demanda',1,'Contabilidad','cl.bcs.contabilidad.jobs.CreaInterfazDemanda','JAVA','','N', 'P', '', 'N'
from dummy;


INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"
select 'InterfazSIGADiaria','Interfaz SIGA Diaria',1,'Contabilidad','cl.bcs.contabilidad.jobs.CreaInterfazDiaria','JAVA','','N', 'P', '', 'N'
from dummy;

commit;