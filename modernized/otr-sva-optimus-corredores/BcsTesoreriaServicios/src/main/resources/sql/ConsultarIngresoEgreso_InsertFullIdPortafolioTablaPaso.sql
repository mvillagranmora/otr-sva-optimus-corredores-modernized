INSERT INTO "BSCL.Tesoreria.BD::GBL.ConsultarIngresoEgreso_Proceso"("IdPortafolio", "IdTrabajo")
SELECT "IdPortafolio", :IdTrabajo FROM "BSCL.Portafolio.BD::POR.Portafolio";