SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasDetalleAnalitica";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasDetalleAnalitica" AS
SELECT CSCD."IdCSCDetalle",
	  CSCD."IdFolioCSC",
	  CSCD."Serie" AS "IdSerieFondo",
	  SER."IdFondo",
	  CSCD."Regimen",
      CSCD."Monto",
	  CSCD."Porcentaje"
FROM  "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotasDetalle" CSCD
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SER ON SER."IdSerieFondo" = CSCD."Serie";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasDetalleAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
