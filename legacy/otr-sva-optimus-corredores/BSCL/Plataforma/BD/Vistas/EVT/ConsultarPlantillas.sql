SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillas"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillas"  
AS
select 
"IdPlantilla",
plt."IdEmpresa",
plt."IdUsuario",
"Nombre",
"IdUsuarioModifica",
"cnu"."CodigoUsuario" as "UsuarioModifica",
"consultarusuario"."CodigoUsuario" as "Usuario",
"FechaCreacion",
"FechaModificacion",
BINTOSTR(CAST(to_char("Template") AS binary)) as "Template",
"Asunto" 
FROM "BSCL.Plataforma.BD::EVT.Plantilla" plt
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario"  "consultarusuario"
ON "consultarusuario"."IdUsuario" = plt."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario"  "cnu"
ON "cnu"."IdUsuario" = plt."IdUsuarioModifica";


  GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillas" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 




  