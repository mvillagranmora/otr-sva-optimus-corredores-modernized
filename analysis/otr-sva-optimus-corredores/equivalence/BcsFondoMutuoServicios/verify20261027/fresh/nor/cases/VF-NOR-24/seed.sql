-- case seed on top of seed/base-fmm.sql (fake data)
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("Key", "Value") VALUES ('PERFIL_BACKOFFICE', '6');
INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario", "IdEmpresa", "IdEstado", "TipoEntidad", "IdEntidad") VALUES (8, 1, 'ACT', 'EMP', 70);
INSERT INTO "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario" ("IdPerfil", "IdUsuario") VALUES (6, 8);
UPDATE "BSCL.FondoMutuo.BD::FMM.Fondo" SET "EstadoRescateBackoffice" = 'C', "EstadoRescateComercial" = 'A' WHERE 1=1;
