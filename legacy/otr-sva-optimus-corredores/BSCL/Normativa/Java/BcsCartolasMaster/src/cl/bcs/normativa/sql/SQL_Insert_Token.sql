INSERT INTO "BSCL.Plataforma.BD::ACC.Sesion" 
("IdTokenSesion", 
"IdUsuario", 
"TokenValido",
"FechaUltimoUso",
"UsoToken",
"FechaIngreso",
"FechaSalida") 
values (
:IdTokenSesion, 
:IdUsuario, 
:TokenValido, 
:FechaUltimoUso, 
:UsoToken, 
:FechaIngreso, 
:FechaSalida);
	