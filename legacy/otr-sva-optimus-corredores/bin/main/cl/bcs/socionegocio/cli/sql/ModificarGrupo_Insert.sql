INSERT INTO "BSCL.SocioNegocio.BD::CLI.Grupo"
(
 "IdGrupo", 
 "IdEmpresa", 
 "Nombre", 
 "IdEstado", 
 "Codigo"
 )
VALUES
(
 :p_IdGrupo, 
 :p_IdEmpresa, 
 :p_Nombre,
 :p_IdEstado, 
 :p_Codigo
 );