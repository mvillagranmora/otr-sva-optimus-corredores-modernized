-- case seed on top of seed/base-fmm.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo" SET "EstadoFondo" = 'A' WHERE "IdTipoFecha" = 'CERTIF';
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo" ("IdEmpresa", "IdModalidad", "IdTipoFecha", "EstadoFondo", "Descripcion", "CantidadDias", "EsHabil", "IdTipoFechaOrigen", "DebeSerHabilFechaOrigen", "ValutaCurse", "ValutaValorCuota", "ValutaCertificacion", "ValutaRetencion", "CalendarioLocal", "CalendarioEmisor", "CalendarioFondo", "CalendarioMoneda", "IdEstado") VALUES (1, 'UNITARIO', 'CERTIF', 'C', 'CERTIF', 3, 'S', 'CURSE', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'ACT');
UPDATE "BSCL.FondoMutuo.BD::FMM.Fondo" SET "EstadoRescateComercial" = 'C' WHERE 1=1;
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("Key", "Value") VALUES ('PERFIL_BACKOFFICE', '6');
INSERT INTO "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario" ("IdPerfil", "IdUsuario") VALUES (6, 8);
INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario", "IdEmpresa", "IdEstado", "TipoEntidad", "IdEntidad") VALUES (8, 1, 'ACT', 'EMP', 70);
