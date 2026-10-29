SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarPlanillaBancoCentral"; 
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarPlanillaBancoCentral" 
AS
	SELECT
		 "IdEmpresa",
		 "IdPlanillaBancoCentral",
		 "Descripcion",
		 "IdPlanillaBancoCentral"||' - '||"Descripcion" as "IdMasDescripcion",
		 "UsuarioModificacion",
		 "FechaModificacion",
		 "IdEstado" 
	FROM "BSCL.Mesa.BD::MES.PlanillaBancoCentral"
	WHERE "IdEstado" != 'INA';
     

GRANT SELECT 	ON "BSCL.Mesa.BD.Vistas::ConsultarPlanillaBancoCentral" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
