SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoDestinoTraspasoMLT";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoDestinoTraspasoMLT"
AS
SELECT
"IdEmpresa"
,"IdEstado"
,"IdPersona"
,"IdRegimenFondoMutuo"
,"IdMoneda"
,"IdSerieFondo"
,"NombreSerieFondo"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoRegimenUI"
UNION ALL
SELECT
"IdEmpresa"
,"IdEstado"
,"IdPersona"
,"IdRegimenFondoMutuo"
,"IdMoneda"
,"IdSerieFondo"
,"NombreSerieFondo"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondoSinRegistrar";

GRANT SELECT ON  "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoDestinoTraspasoMLT" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


