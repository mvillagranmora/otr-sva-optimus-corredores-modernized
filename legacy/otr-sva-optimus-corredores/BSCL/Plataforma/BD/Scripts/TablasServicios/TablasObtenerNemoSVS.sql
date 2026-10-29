SET SCHEMA "BSCL"; 
GO
DROP TABLE "BSCL.Plataforma.BD::PLA.ObtenerNemoSVS_Entrada_Entrada";
GO
DROP TABLE "BSCL.Plataforma.BD::PLA.ObtenerNemoSVS_Salida_Salida";
GO
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::PLA.ObtenerNemoSVS_Entrada_Entrada"(
	"IdInterno"			BIGINT,
	"IdEmisor"			NVARCHAR(40),
	"Fecha"				NVARCHAR(10),
	"Plazo"				BIGINT,
	"Nemotecnico"		NVARCHAR(40),
	"IdTipoReajuste"	NVARCHAR(3),
	"PagaderoEn"		NVARCHAR(3)
) ON COMMIT DELETE ROWS;
GO

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::PLA.ObtenerNemoSVS_Salida_Salida"(
	"IdInterno"		BIGINT,
	"NemoSVS"		NVARCHAR(40)
) ON COMMIT DELETE ROWS;
GO

 
 

 

 

 





 






 

 
 

