INSERT INTO "BSCL.Mesa.BD::GBL.ConsultarOperacionesCanalAgente_Proceso_Paso"("IdPortafolio", "IdTrabajo")
SELECT "IdPortafolio", :IdTrabajo FROM "BSCL.Portafolio.BD::POR.Portafolio";