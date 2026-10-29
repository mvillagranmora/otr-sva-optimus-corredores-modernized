set schema "BSCL";

drop table 		"BSCL.Plataforma.BD::PAU.Dependencia";
drop table 		"BSCL.Plataforma.BD::PAU.ControlPauta";
drop sequence 	"BSCL.Plataforma.BD.Secuencias::ControlPauta";

create column table "BSCL.Plataforma.BD::PAU.Dependencia" (
    "IdTipoPrograma"         	NVARCHAR(40)	not null, 
    "Ejecucion"          		NVARCHAR(40)	not null	default '', -- MANUAL, PROGRAMADA, REINTENTO 
    "RequiereTipoPrograma"		NVARCHAR(40)	not null 	default '',
    "SiguienteTipoPrograma"		NVARCHAR(40)	not null	default '', 
   primary key ("IdTipoPrograma","Ejecucion")
);

create column table "BSCL.Plataforma.BD::PAU.ControlPauta" (
    "IdControlPauta"         	BIGINT			not null, 
    "IdPrograma"          		BIGINT			not null 	default 0, 
    "IdTipoPrograma"			NVARCHAR(40)	not null	default '',
    "Ejecucion"          		NVARCHAR(40)	not null	default '', -- MANUAL, PROGRAMADA, REINTENTO 
	"FechaSistema" 				DATE			not null	default '0001-01-01',   
	"FechaCreacion" 			SECONDDATE 		not null 	default '0001-01-01 00:00:00',
	"IdEstado"					NVARCHAR(3)		not null	default '', -- PEN, TER, ERR
	"Mensaje"					NVARCHAR(5000)	not null	default '',
   primary key ("IdControlPauta")
);

create sequence "BSCL.Plataforma.BD.Secuencias::ControlPauta" 
increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;