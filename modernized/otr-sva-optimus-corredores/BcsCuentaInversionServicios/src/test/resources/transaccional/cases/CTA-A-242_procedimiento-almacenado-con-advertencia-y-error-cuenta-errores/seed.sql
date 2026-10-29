INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_CargoAbonoGenerado" ("NumeroCargoAbono","IdCargoAbonoMovimiento","IdTipoMovimiento") VALUES (1,8801,'MOVABN');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_InformacionDetalle" ("NumeroCargoAbono","IdCargoAbonoMovimiento","Codigo","Descripcion") VALUES (1,8801,'ABONO_CARGO_CREADO','creado');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_AdvertenciasDetalle" ("NumeroCargoAbono","Codigo","RequiereAutorizacion","Descripcion") VALUES (1,'CLIENTE_SIN_FLUJO','S','advertencia (SP)');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle" ("NumeroCargoAbono","Codigo","Descripcion") VALUES (2,'MONTO_MENOR_IGUAL_A_CERO','error (SP)');
