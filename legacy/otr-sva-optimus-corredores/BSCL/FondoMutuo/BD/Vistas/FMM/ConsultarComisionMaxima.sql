SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionMaxima";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionMaxima" AS (
	SELECT 
		Max("IdComision") As "IdComision"
		, "IdSerieFondo"
		, "Estado" 
	FROM "BSCL.FondoMutuo.BD::FMM.Comision" 
	GROUP BY "IdSerieFondo", "Estado"
);

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionMaxima" TO  BCS_DESARROLLADOR WITH GRANT OPTION;