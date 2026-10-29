SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoRegimen";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoRegimen"  
AS
 SELECT "IdRegimenFondoMutuo", 
		"IdSerieFondo", 
		"IdEstado", 
		"IdEmpresa" 
   FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo";
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoRegimen" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO