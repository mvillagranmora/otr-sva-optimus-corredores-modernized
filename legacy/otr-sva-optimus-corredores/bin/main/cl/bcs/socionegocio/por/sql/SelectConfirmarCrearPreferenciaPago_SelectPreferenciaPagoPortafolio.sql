SELECT
"IdPreferenciaPagoPortafolio",
"IdPortafolio",
"IdPreferenciaPago",
"IdCuentaBancaria",
"IdTarjetaBancaria",
"FechaCreacion",
"FechaModificacion",
"IdUsuarioCreador",
"IdEstado",
"IdEmpresa",			
"FechaIngreso",					 
"IdEstadoAutorizacion",
"PorDefecto"
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
WHERE "IdPreferenciaPagoPortafolio"	= :p_IdPreferenciaPagoPortafolio AND "IdEmpresa" = :p_IdEmpresa;