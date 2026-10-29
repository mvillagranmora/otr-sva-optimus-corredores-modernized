INSERT INTO "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso"
("IdTrabajo","IdEjecutivo","IdPortafolio","IdMoneda")
SELECT :p_IdTrabajo,null as "IdEjecutivo","IdPortafolio",:p_IdMoneda  AS "IdMoneda"
from "BSCL.SocioNegocio.BD::CLI.Cliente" c
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" p on c."IdCliente" = p."IdCliente"
where c."IdCliente" = :p_IdCliente;
