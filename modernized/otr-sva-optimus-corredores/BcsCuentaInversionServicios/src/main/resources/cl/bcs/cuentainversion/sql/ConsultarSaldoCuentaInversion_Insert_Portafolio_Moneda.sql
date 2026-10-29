INSERT INTO "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso"
("IdTrabajo","IdEjecutivo","IdPortafolio","IdMoneda")
select :IdTrabajo,null as "IdEjecutivo", c."IdPortafolio", cc."IdMoneda" 
from "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" c
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" cc on cc."IdPortafolio" = c."IdPortafolio"
where c."IdMoneda" IS NULL;