-- F3 W3 T6-B: the default data of a Liquidacion case (fake): company 1, parameters, dictionary, modalities. A case overrides what it needs in its seed.sql.
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, 'APV.Habilitado', 'N');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.Parametro" ("IdEmpresa", "NombreTipoParametro", "ValorParametro") VALUES (1, 'LiquidacionCuentaInversionCliente', 'N');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.Parametro" ("IdEmpresa", "NombreTipoParametro", "ValorParametro") VALUES (1, 'VALIDAR_LIQUIDACION_OMNIBUS', 'N');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'NO_SE_INGRESARON_FILTROS', 'No se ingresaron filtros');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'UI_ERROR_APORTE_SIN_OMNIBUS', 'Aporte sin omnibus');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ("IdMedioDePago", "IdModalidadDePago", "IdEstado") VALUES ('CHE', 'MODCAJ', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ("IdMedioDePago", "IdModalidadDePago", "IdEstado") VALUES ('TRA', 'MODDEP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ("IdMedioDePago", "IdModalidadDePago", "IdEstado") VALUES ('CIN', 'MODCIN', 'ACT');
