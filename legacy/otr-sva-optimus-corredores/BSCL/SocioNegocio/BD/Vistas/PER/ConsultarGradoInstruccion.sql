SET SCHEMA "BSCL";
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarGradoInstruccion"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarGradoInstruccion" AS
	select "IdGradoInstruccion","Codigo","Nombre","IdPais"	
	from "BSCL.SocioNegocio.BD::PER.GradoInstruccion";
--WITH READ ONLY; 
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarGradoInstruccion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
