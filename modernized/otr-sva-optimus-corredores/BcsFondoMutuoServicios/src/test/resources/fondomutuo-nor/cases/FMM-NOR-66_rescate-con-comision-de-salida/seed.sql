-- case seed on top of seed/base-fmm.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.Parametro" SET "ValorParametro" = 'N' WHERE "NombreTipoParametro" = 'CALCULAR_COMISION_CERTIFICACION';
INSERT INTO "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTramoComision" ("IdTramo", "IdComision", "DiasDesde", "DiasHasta", "PorcentajeComision", "EstadoTramo", "IdSerieFondo", "Descripcion", "TipoComision", "FechaVigenciaDesde", "FechaVigenciaHasta", "PorcentajeLiberado", "EstadoComision", "IdEmpresa") VALUES (10, 1, 0, 9999, 2.0, 'ACT', 'FM001A', 'tramo', 'SALIDA', DATE '2020-01-01', DATE '2099-12-31', 0, 'ACT', 1);
INSERT INTO "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionMaxima" ("IdSerieFondo", "IdComision") VALUES ('FM001A', 1);
