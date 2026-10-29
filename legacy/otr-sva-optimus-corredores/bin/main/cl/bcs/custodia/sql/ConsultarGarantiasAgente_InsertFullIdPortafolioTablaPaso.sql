INSERT INTO "BSCL.Custodia.BD::GTA.ConsultarGarantiasAgente_Proceso_Paso"("IdPortafolio", "IdTrabajo")
SELECT "IdPortafolio", :IdTrabajo FROM "BSCL.Portafolio.BD::POR.Portafolio";