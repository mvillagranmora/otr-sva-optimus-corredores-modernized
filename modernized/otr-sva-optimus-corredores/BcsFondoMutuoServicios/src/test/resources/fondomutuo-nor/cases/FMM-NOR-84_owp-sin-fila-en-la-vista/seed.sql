-- case seed on top of seed/base-fmm.sql (fake data)
ALTER TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" ADD COLUMN "IdOmnibus" BIGINT;
UPDATE "BSCL.Plataforma.BD::PLA.Configuracion" SET "Valor" = 'S' WHERE "CodigoParametro" = 'PLA.NotificarEstadoOrdenOWP';
