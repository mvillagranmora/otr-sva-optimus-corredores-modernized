SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFormatoArchivoFondoMutuo";
 
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFormatoArchivoFondoMutuo" AS

SELECT
    FORARC."IdFormatoArchivo",
    FORARC."FormatoSalida",
    FORARC."Descripcion",
    FORARC."IdEmpresa",
    FORARC."Servicio",
    FORARC."IdEstado",
    FORARC."RutaArchivo"
FROM "BSCL.FondoMutuo.BD::FMM.FormatoArchivo" AS FORARC;

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFormatoArchivoFondoMutuo" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
