SET SCHEMA "BSCL";
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarFolioContratoCSC";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarFolioContratoCSC" 
AS 
SELECT
	"FolioCSC"
	,"TipoPago"
	,"IdPortafolio"
FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas"
WITH READ ONLY;

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarFolioContratoCSC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;	
