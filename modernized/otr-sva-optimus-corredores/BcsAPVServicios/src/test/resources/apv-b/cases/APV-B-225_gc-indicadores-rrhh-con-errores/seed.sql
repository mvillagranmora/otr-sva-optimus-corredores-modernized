INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona","Tipo") VALUES (117943,'N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" ("IdPersona","DocumentoIdentidad","Predeterminado") VALUES (117943,'7813510-7','S');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" ("IdCliente","IdPersona","IdEstado") VALUES (8001,117943,'ACT');
INSERT INTO "BSCL.Plataforma.BD::IND.TipoIndicador" ("IdTipoIndicador","CodigoTipoIndicador","IdTipoEntidad","GrupoIndicador") VALUES (1,'NOMBRE_ENCARGADO_RRHH','PERSONA','DATOS EMPLEADOR');
INSERT INTO "BSCL.Plataforma.BD::IND.TipoIndicador" ("IdTipoIndicador","CodigoTipoIndicador","IdTipoEntidad","GrupoIndicador") VALUES (2,'MAIL_ENCARGADO_RRHH','PERSONA','DATOS EMPLEADOR');
INSERT INTO "BSCL.Plataforma.BD::IND.TipoIndicador" ("IdTipoIndicador","CodigoTipoIndicador","IdTipoEntidad","GrupoIndicador") VALUES (3,'TELEFONO_ENCARGADO_RRHH','PERSONA','DATOS EMPLEADOR');
