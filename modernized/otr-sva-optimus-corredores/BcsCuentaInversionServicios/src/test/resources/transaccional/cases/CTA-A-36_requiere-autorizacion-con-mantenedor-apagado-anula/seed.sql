DELETE FROM "BSCL.CuentaInversion.BD.Vistas::ConsultaConfiguracionCuentaInversion";
INSERT INTO "BSCL.CuentaInversion.BD.Vistas::ConsultaConfiguracionCuentaInversion" VALUES (1, 'S', 'S');
DELETE FROM "BSCL.Plataforma.BD::PLA.RequiereAutorizacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" VALUES ('ANUCAR', 'N');
INSERT INTO "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" VALUES ('ANUABO', 'S');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ("IdCargoAbonoMovimiento","IdCuentaInversion","IdTipoMovimiento","IdEstado","IdEmpresa","Monto","Saldo","SaldoPorFechaMovimiento","FechaSistema","FechaEfectivaMovimiento","FechaCertificacion","IdConcepto","IdCondicionDeLiquidacion","EsCargoAbonoPorTraspaso","IdCargoAbonoRelacionadoTraspaso","IdUsuario","IdEvento") VALUES (3601,7003,'MOVCAR','CRT',1,300,-300,-300,'2025-04-01','2025-04-01','2025-04-01 09:00:00','AACFIR','T0','N',0,97,0);
