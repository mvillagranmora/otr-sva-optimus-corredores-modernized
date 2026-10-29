-- case seed on top of seed/base-fmm.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.Fondo" SET "EstadoRescateComercial" = 'C' WHERE 1=1;
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("Key", "Value") VALUES ('PERFIL_BACKOFFICE', '6');
INSERT INTO "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario" ("IdPerfil", "IdUsuario") VALUES (6, 8);
INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario", "IdEmpresa", "IdEstado", "TipoEntidad", "IdEntidad") VALUES (8, 1, 'ACT', 'EMP', 70);
