SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario"  
AS
Select U."CodigoUsuario",
U."IdUsuario",
U."IdEmpresa",
U."IdEstado"			     
From "BSCL.Plataforma.BD::ACC.Usuario" AS U;	
		

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 