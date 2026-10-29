SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarMediosDePagoCSC";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarMediosDePagoCSC"
AS
SELECT "A2"."IdCSCPago", "A2"."IdMedioDePago", "A2"."IdEstado", "A2"."Nombre", "A2"."Orden" FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotasMedioPago" AS "A2"  
INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS "A1"
ON ("A1"."IdMedioDePago" = "A2"."IdMedioDePago");


GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarMediosDePagoCSC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;