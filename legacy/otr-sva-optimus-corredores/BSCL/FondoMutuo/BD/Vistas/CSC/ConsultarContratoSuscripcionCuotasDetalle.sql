SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasDetalle";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasDetalle"
AS
SELECT 	CC."IdCSCDetalle",
		CC."IdFolioCSC",
		SF."IdSerieFondo",
		SF."NombreSerieFondo",
		SF."NombreFondo",
		CC."Monto",
		CC."Porcentaje",
		CC."Regimen"
FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotasDetalle" AS CC
LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS SF ON (CC."Serie" = SF."IdSerieFondo");


GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;