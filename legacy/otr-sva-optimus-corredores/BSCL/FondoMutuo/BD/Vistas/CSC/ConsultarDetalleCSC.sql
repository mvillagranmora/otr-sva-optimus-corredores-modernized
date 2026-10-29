SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarDetalleCSC";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarDetalleCSC"
AS
SELECT "IdFolioCSC","IdCliente","IdPortafolio","Fecha","MontoPesos","MontoUF","TipoPago","IdBanco","IdEmpresa" FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas";


GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotas" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
