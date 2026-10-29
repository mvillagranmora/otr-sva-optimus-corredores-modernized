INSERT INTO "BSCL.Portafolio.BD::GBL.CrearPortafolio_AgregarCuentaInversion" (
"IdCliente",
"IdPortafolio",				
"IdMoneda",	   
"IdCuentaInversion", 
"Nombre",
"CuentaInversionPrincipal",
"Accion"
)
VALUES(
:IdCliente,
:IdPortafolio,
:IdMoneda,
:IdCuentaInversion,
:Nombre,
:CuentaInversionPrincipal,
:Accion
)