SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion";

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion"(
	"IdEmpresa"				BIGINT,
	"Fecha"					TIMESTAMP,
	"IdUsuario"				BIGINT,
	"IdServicioAgrupador"	BIGINT,
	"ServicioAgrupador" 	NVARCHAR(255),
	"Servicio"				NVARCHAR(255),
	"Bandera"				NVARCHAR(80),
	"Detalle"				NVARCHAR(520)
) ON COMMIT DELETE ROWS;

commit
 

 

 

 





 






 

 
 

