SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRol"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRol"  
AS
Select U."Nombre",
U."IdRol",
U."IdEmpresa",
U."IdEstado",
EPL."Descripcion" as "EstadoRol"
From "BSCL.Plataforma.BD::ACC.Rol" AS U
left JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla" EPL on EPL."IdEstado" = U."IdEstado" and EPL."NombreEntidad" = 'ROL';	
		

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRol" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 