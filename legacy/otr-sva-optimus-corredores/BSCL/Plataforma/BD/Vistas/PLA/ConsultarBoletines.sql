SET SCHEMA "BSCL"; 
 GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoletines"; 
 GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoletines" AS
	SELECT 
	BOL."IdEmpresa",
	BOL."CodigoArchivo",
	BOL."Descripcion",
	CUS."CodigoUsuario",
	BOL."FechaModificacion",
	BOL."Binario",
	BOL."TipoBinario",
	BOL."TipoPresentacion",
	BOL."IdTemplate",
	BOL."IdEstado"
	FROM "BSCL.Plataforma.BD::PLA.Boletines" AS BOL
	INNER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS CUS ON (BOL."IdUsuario" = CUS."IdUsuario");
GO			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoletines" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 