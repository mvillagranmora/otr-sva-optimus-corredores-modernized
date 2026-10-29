-- Reference data shared by every case of APV family A (fixtures; no legacy data, no credential). The case seeds add parties, contracts and portfolios on top.
-- Catalogue ids are small numbers chosen here; the legacy only needs them to be consistent between tables.

INSERT INTO "BSCL.APV.BD::SCS.PlanTipoInversion" ("IdPlanTipoInversion", "NombrePlanTipoInversion", "IdEstado") VALUES (1, 'ABIERTO', 'VIG');
INSERT INTO "BSCL.APV.BD::SCS.PlanTipoInversion" ("IdPlanTipoInversion", "NombrePlanTipoInversion", "IdEstado") VALUES (2, 'ACTIVO', 'VIG');

INSERT INTO "BSCL.APV.BD::APV.ProductoAgrupador" ("IdProductoAgrupador", "NombreProductoAgrupador", "IdEstado") VALUES (1, 'APV FONDOS MUTUOS', 'VIG');
INSERT INTO "BSCL.APV.BD::APV.ProductoAgrupador" ("IdProductoAgrupador", "NombreProductoAgrupador", "IdEstado") VALUES (2, 'APV ACCIONES', 'VIG');

INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo", "IdEstado") VALUES (1, 'APV-A', 'VIG');
INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo", "IdEstado") VALUES (2, 'APV-B', 'VIG');
INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo", "IdEstado") VALUES (3, 'DC-R', 'VIG');

-- matriz: 1 = APV-A / plan abierto / grupo 1, 2 = APV-B / plan abierto / grupo 1, 3 = DC-R / plan abierto / grupo 1, 7 = APV-A / plan activo / grupo 1
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "IdPlanTipoInversion", "IdProductoAgrupador", "IdRegimenTributario", "CodigoAgrupador", "IdEstado") VALUES (1, 'PROPOSITOAPV', 1, 1, 1, 'GRUPO 1', 'VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "IdPlanTipoInversion", "IdProductoAgrupador", "IdRegimenTributario", "CodigoAgrupador", "IdEstado") VALUES (2, 'PROPOSITOAPV', 1, 1, 2, 'GRUPO 1', 'VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "IdPlanTipoInversion", "IdProductoAgrupador", "IdRegimenTributario", "CodigoAgrupador", "IdEstado") VALUES (3, 'PROPOSITOAPV', 1, 1, 3, 'GRUPO 1', 'VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "IdPlanTipoInversion", "IdProductoAgrupador", "IdRegimenTributario", "CodigoAgrupador", "IdEstado") VALUES (7, 'PROPOSITOAPV', 2, 1, 1, 'GRUPO 1', 'VIG');

-- the HANA view REC.ConsultarRegimenTributario joins the matriz with the regimen and the plan: its rows are seeded as the view would return them
INSERT INTO "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" ("Codigo", "IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "NombreAgrupador", "CodigoAgrupador", "NombrePlan") VALUES ('APV-A', 1, 'PROPOSITOAPV', 'APV FONDOS MUTUOS', 'GRUPO 1', 'ABIERTO');
INSERT INTO "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" ("Codigo", "IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "NombreAgrupador", "CodigoAgrupador", "NombrePlan") VALUES ('APV-B', 2, 'PROPOSITOAPV', 'APV FONDOS MUTUOS', 'GRUPO 1', 'ABIERTO');
INSERT INTO "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" ("Codigo", "IdMatrizAgrupadorAPV", "IdPropositoPortafolio", "NombreAgrupador", "CodigoAgrupador", "NombrePlan") VALUES ('DC-R', 3, 'PROPOSITOAPV', 'APV FONDOS MUTUOS', 'GRUPO 1', 'ABIERTO');

INSERT INTO "BSCL.APV.BD::SCS.MedioRecaudacion" ("IdMedioRecaudacion", "CodigoMedioRecaudacion", "TipoMedioRecaudacion", "NombreMedioRecaudacion") VALUES (1, 'DIREMP', 'DIRECTA', 'DEPOSITO DIRECTO');
INSERT INTO "BSCL.APV.BD::SCS.MedioRecaudacion" ("IdMedioRecaudacion", "CodigoMedioRecaudacion", "TipoMedioRecaudacion", "NombreMedioRecaudacion") VALUES (2, 'ATREMP', 'INDIRECTA', 'DEPOSITO POR EMPLEADOR');
INSERT INTO "BSCL.APV.BD::SCS.MedioRecaudacion" ("IdMedioRecaudacion", "CodigoMedioRecaudacion", "TipoMedioRecaudacion", "NombreMedioRecaudacion") VALUES (3, 'PAC', 'DIRECTA', 'PAC');

INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES ('VIG', 'VIGENTE');
INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES ('PDI', 'PENDIENTE DE INGRESO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES ('REV', 'REVOCADO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES ('CER', 'CERRADO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES ('ANU', 'ANULADO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES ('NEV', 'NO ENVIADO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ("Codigo", "Nombre") VALUES ('ENV', 'ENVIADO');
