SET SCHEMA "BSCL"; 

drop table "BSCL.Plataforma.BD::PAU.AgregarControlPauta_Entrada_Registros";

CREATE GLOBAL TEMPORARY TABLE "BSCL.Plataforma.BD::PAU.AgregarControlPauta_Entrada_Registros"(
	"IdTrabajo"         		BIGINT			not null, 
    "IdPrograma"          		BIGINT			not null 	default 0, 
    "IdTipoPrograma"			NVARCHAR(40)	not null	default '',
    "Ejecucion"          		NVARCHAR(40)	not null	default '', -- MANUAL, PROGRAMADA, REINTENTO 
	"FechaSistema" 				DATE			not null	default '0001-01-01',   
	"IdEstado"					NVARCHAR(3)		not null	default '', -- PEN, TER, ERR
	"Mensaje"					NVARCHAR(5000)	not null	default ''
)ON COMMIT DELETE ROWS;
