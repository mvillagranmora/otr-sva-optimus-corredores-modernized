select
	"IdUsuario",
	"IdRol"	
from "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesUsuario"
where "IdUsuario" = :p_IdUsuario
