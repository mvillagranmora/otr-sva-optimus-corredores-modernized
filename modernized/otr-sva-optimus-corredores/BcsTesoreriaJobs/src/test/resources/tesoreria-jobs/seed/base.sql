-- baseline of every case: one DUMMY row, the program row of the run (IdPrograma 900), the time window of the mass load and the previous-day
-- execution of the PAUTA; a case seed may DELETE or UPDATE them.
INSERT INTO DUMMY VALUES ('X');
INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" ("IdPrograma", "RutaSalida") VALUES (900, 'SIN-RUTA');
INSERT INTO "BSCL.Plataforma.BD::BAT.RangoHorario" ("TipoProposito", "HoraInicio", "HoraFin") VALUES ('CARGA_MASIVA_INGRESO_EGRESO_DAP_FFMM', '08:00:00', '20:00:00');
INSERT INTO "BSCL.Plataforma.BD::PAU.Ejecucion" ("FechaSistema", "IdEmpresa", "Estado") VALUES ('2025-03-31', 1, 'PRO');
