SET SCHEMA "BSCL"; 

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/ 
DELETE FROM "BSCL.Plataforma.BD::CRE.ModuloEmpresa"
DELETE FROM "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

--Insertar Modulos para cierre.
INSERT INTO "BSCL"."BSCL.Plataforma.BD::CRE.ModuloEmpresa" ("IdCREModuloEmpresa", "IdEmpresa", "IdModulo", "NombreModuloEmpresa", "IdEstado")
VALUES (1, 1, 'ORD', 'ORDENES', 'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::CRE.ModuloEmpresa" ("IdCREModuloEmpresa", "IdEmpresa", "IdModulo", "NombreModuloEmpresa", "IdEstado")
VALUES (2, 1, 'FAC', 'FACTURACION', 'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::CRE.ModuloEmpresa" ("IdCREModuloEmpresa", "IdEmpresa", "IdModulo", "NombreModuloEmpresa", "IdEstado")
VALUES (3, 1, 'CUS', 'CUSTODIA', 'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::CRE.ModuloEmpresa" ("IdCREModuloEmpresa", "IdEmpresa", "IdModulo", "NombreModuloEmpresa", "IdEstado")
VALUES (4, 1, 'TES', 'TESORERIA', 'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::CRE.ModuloEmpresa" ("IdCREModuloEmpresa", "IdEmpresa", "IdModulo", "NombreModuloEmpresa", "IdEstado")
VALUES (5, 1, 'CTA', 'CUENTA INVERSION', 'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::CRE.ModuloEmpresa" ("IdCREModuloEmpresa", "IdEmpresa", "IdModulo", "NombreModuloEmpresa", "IdEstado")
VALUES (6, 1, 'MRF', 'MESA RF', 'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::CRE.ModuloEmpresa" ("IdCREModuloEmpresa", "IdEmpresa", "IdModulo", "NombreModuloEmpresa", "IdEstado")
VALUES (7, 1, 'MSP', 'MESA SPOT', 'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::CRE.ModuloEmpresa" ("IdCREModuloEmpresa", "IdEmpresa", "IdModulo", "NombreModuloEmpresa", "IdEstado")
VALUES (8, 1, 'MFW', 'MESA FORWARD', 'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::CRE.ModuloEmpresa" ("IdCREModuloEmpresa", "IdEmpresa", "IdModulo", "NombreModuloEmpresa", "IdEstado")
VALUES (9, 1, 'CON', 'CONTABILIDAD', 'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::CRE.ModuloEmpresa" ("IdCREModuloEmpresa", "IdEmpresa", "IdModulo", "NombreModuloEmpresa", "IdEstado")
VALUES (10, 1, 'CLI', 'CLIENTE', 'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::CRE.ModuloEmpresa" ("IdCREModuloEmpresa", "IdEmpresa", "IdModulo", "NombreModuloEmpresa", "IdEstado")
VALUES (11, 1, 'FMM', 'FONDOS MUTUOS', 'ACT');

--Insertar Funciones por Modulo.

--Funciones Tesorería
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	1	,	4,    'Anular Abono CLP',                                                                                    'Anular Abono CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	2	,	4,    'Anular Abono EUR',                                                                                    'Anular Abono EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	3	,	4,    'Anular Abono USD',                                                                                    'Anular Abono USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	4	,	4,    'Anular Abono Regularizado CLP',                                                                       'Anular Abono Regularizado CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	5	,	4,    'Anular Abono Regularizado EUR',                                                                       'Anular Abono Regularizado EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	6	,	4,    'Anular Abono Regularizado USD',                                                                       'Anular Abono Regularizado USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	7	,	4,    'Anular Cargo CLP',                                                                                    'Anular Cargo CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	8	,	4,    'Anular Cargo EUR',                                                                                    'Anular Cargo EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	9	,	4,    'Anular Cargo USD',                                                                                    'Anular Cargo USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	10	,	4,    'Anular Cargo Regularizado CLP',                                                                       'Anular Cargo Regularizado CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	11	,	4,    'Anular Cargo Regularizado EUR',                                                                       'Anular Cargo Regularizado EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	12	,	4,    'Anular Cargo Regularizado USD',                                                                       'Anular Cargo Regularizado USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	13	,	4,    'Anular Operaciones CLP',                                                                              'Anular Operaciones CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	14	,	4,    'Anular Operaciones EUR',                                                                              'Anular Operaciones EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	15	,	4,    'Anular Operaciones USD',                                                                              'Anular Operaciones USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	16	,	4,    'Anular Orden de Ingreso CLP',                                                                         'Anular Orden de Ingreso CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	17	,	4,    'Anular Orden de Ingreso EUR',                                                                         'Anular Orden de Ingreso EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	18	,	4,    'Anular Orden de Ingreso USD',                                                                         'Anular Orden de Ingreso USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	19	,	4,    'Anular Orden de Pago CLP',                                                                            'Anular Orden de Pago CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	20	,	4,    'Anular Orden de Pago EUR',                                                                            'Anular Orden de Pago EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	21	,	4,    'Anular Orden de Pago USD',                                                                            'Anular Orden de Pago USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	22	,	4,    'Anular Partida de Egreso CLP',                                                                        'Anular Partida de Egreso CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	23	,	4,    'Anular Partida de Ingreso CLP',                                                                       'Anular Partida de Ingreso CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	24	,	4,    'Anular Operacion Spot USD',                                                                           'Anular Operacion Spot USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	25	,	4,    'Desmarcar Operacion Spot USD',                                                                        'Desmarcar Operacion Spot USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	26	,	4,    'Asignar Deposito',                                                                                    'Asignar Deposito', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	27	,	4,    'Asignar Moneda CLP',                                                                                  'Asignar Moneda CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	28	,	4,    'Asignar Moneda EUR',                                                                                  'Asignar Moneda EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	29	,	4,    'Asignar Moneda USD',                                                                                  'Asignar Moneda USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	30	,	4,    'Carga Masiva Boton Pago',                                                                             'Carga Masiva Boton Pago', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	31	,	4,    'Informe de comprobantes sin certificación',                                                           'Informe de comprobantes sin certificación', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	32	,	4,    'Informe de Comprobantes - TECOMP',                                                                    'Informe de Comprobantes - TECOMP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	33	,	4,    'Informe de revisión de procesos de cierre',                                                           'Informe de revisión de procesos de cierre', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	34	,	4,    'Demarcar - Cargo Masivo',                                                                             'Demarcar - Cargo Masivo', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	35	,	4,    'Certificar - Cargo Masivo',                                                                           'Certificar - Cargo Masivo', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	36	,	4,    'Certificar Abono CLP',                                                                                'Certificar Abono CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	37	,	4,    'Certificar Abono EUR',                                                                                'Certificar Abono EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	38	,	4,    'Certificar Abono USD',                                                                                'Certificar Abono USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	39	,	4,    'Certificar Abono Regularizado CLP',                                                                   'Certificar Abono Regularizado CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	40	,	4,    'Certificar Abono Regularizado EUR',                                                                   'Certificar Abono Regularizado EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	41	,	4,    'Certificar Abono Regularizado USD',                                                                   'Certificar Abono Regularizado USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	42	,	4,    'Certificar Cargo CLP',                                                                                'Certificar Cargo CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	43	,	4,    'Certificar Cargo EUR',                                                                                'Certificar Cargo EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	44	,	4,    'Certificar Cargo USD',                                                                                'Certificar Cargo USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	45	,	4,    'Girar - Cargo Masivo',                                                                                'Girar - Cargo Masivo', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	46	,	4,    'Certificar Cargo Regularizado CLP',                                                                   'Certificar Cargo Regularizado CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	47	,	4,    'Certificar Cargo Regularizado EUR',                                                                   'Certificar Cargo Regularizado EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	48	,	4,    'Certificar Cargo Regularizado USD',                                                                   'Certificar Cargo Regularizado USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	49	,	4,    'Certificar lector',                                                                                   'Certificar lector', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	50	,	4,    'Certificación Masiva Abono',                                                                          'Certificación Masiva Abono', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	51	,	4,    'Certificación Masiva Cargo',                                                                          'Certificación Masiva Cargo', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	52	,	4,    'Certificación Masiva Egreso',                                                                         'Certificación Masiva Egreso', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	53	,	4,    'Certificación Masiva Ingreso',                                                                        'Certificación Masiva Ingreso', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	54	,	4,    'Certificar Orden de Ingreso CLP',                                                                     'Certificar Orden de Ingreso CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	55	,	4,    'Certificar Orden de Ingreso EUR',                                                                     'Certificar Orden de Ingreso EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	56	,	4,    'Certificar Orden de Ingreso USD',                                                                     'Certificar Orden de Ingreso USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	57	,	4,    'Certificar Orden de Pago CLP',                                                                        'Certificar Orden de Pago CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	58	,	4,    'Certificar Orden de Pago EUR',                                                                        'Certificar Orden de Pago EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	59	,	4,    'Certificar Orden de Pago USD',                                                                        'Certificar Orden de Pago USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	60	,	4,    'Certificar Transferencia CLP',                                                                        'Certificar Transferencia CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	61	,	4,    'Certificar Transferencia EUR',                                                                        'Certificar Transferencia EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	62	,	4,    'Certificar Transferencia USD',                                                                        'Certificar Transferencia USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	63	,	4,    'Aceptar Deposito CLP',                                                                                'Aceptar Deposito CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	64	,	4,    'Aceptar Deposito MX',                                                                                 'Aceptar Deposito MX', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	65	,	4,    'Devolver Deposito CLP',                                                                               'Devolver Deposito CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	66	,	4,    'Devolver Deposito MX',                                                                                'Devolver Deposito MX', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	67	,	4,    'Anular Cheque',                                                                                       'Anular Cheque', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	68	,	4,    'Certificar Cheque',                                                                                   'Certificar Cheque', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	69	,	4,    'Liquidar Clearing Financiero',                                                                        'Liquidar Clearing Financiero', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	70	,	4,    'Deposito Efectivo CLP',                                                                               'Deposito Efectivo CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	71	,	4,    'Deposito Efectivo EUR',                                                                               'Deposito Efectivo EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	72	,	4,    'Deposito Efectivo USD',                                                                               'Deposito Efectivo USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	73	,	4,    'Egreso Billete EUR',                                                                                  'Egreso Billete EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	74	,	4,    'Egreso Billete USD',                                                                                  'Egreso Billete USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	75	,	4,    'Emision Masiva de Cheques',                                                                           'Emision Masiva de Cheques', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	76	,	4,    'Envio a Deposito Documentos CLP',                                                                     'Envio a Deposito Documentos CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	77	,	4,    'Envio a Deposito Documentos MX',                                                                      'Envio a Deposito Documentos MX', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	78	,	4,    'Genera Cargo/Abono Factura (CTLIPH)',                                                                 'Genera Cargo/Abono Factura (CTLIPH)', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	79	,	4,    'Genera Cargo/Abono SCL',                                                                              'Genera Cargo/Abono SCL', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	80	,	4,    'Genera Orden de FFMM',                                                                                'Genera Orden de FFMM', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	81	,	4,    'Genera Traspaso',                                                                                     'Genera Traspaso', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	82	,	4,    'Genera Traspaso ve',                                                                                  'Genera Traspaso ve', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	83	,	4,    'Ingreso Banco',                                                                                       'Ingreso Banco', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	84	,	4,    'Ingreso Billete EUR',                                                                                 'Ingreso Billete EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	85	,	4,    'Ingreso Billete USD',                                                                                 'Ingreso Billete USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	86	,	4,    'Libera Documentos en Canje',                                                                          'Libera Documentos en Canje', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	87	,	4,    'Libera Saldos x Vcto',                                                                                'Libera Saldos x Vcto', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	88	,	4,    'Liquidar Cargo/Abono AGF',                                                                            'Liquidar Cargo/Abono AGF', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	89	,	4,    'Liquidar Corte Cupones',                                                                              'Liquidar Corte Cupones', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	90	,	4,    'Liquidar Dividendos',                                                                                 'Liquidar Dividendos', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	91	,	4,    'Liquidar Operaciones CLP',                                                                            'Liquidar Operaciones CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	92	,	4,    'Liquidar Operaciones USD',                                                                            'Liquidar Operaciones USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	93	,	4,    'Liquidar Operaciones EUR',                                                                            'Liquidar Operaciones EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	94	,	4,    'Liquidar Pactos',                                                                                     'Liquidar Pactos', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	95	,	4,    'Liquidar Aportes/Rescates FFMM',                                                                      'Liquidar Aportes/Rescates FFMM', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	96	,	4,    'Liquidar Resultado Futuro',                                                                           'Liquidar Resultado Futuro', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	97	,	4,    'Liquidar Simultaneas Cliente',                                                                        'Liquidar Simultaneas Cliente', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	98	,	4,    'Liquidar Simultaneas Corredora',                                                                      'Liquidar Simultaneas Corredora', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	99	,	4,    'Liquidar Sorteo de Letras',                                                                           'Liquidar Sorteo de Letras', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	100	,	4,    'Liquidar Suscripcion OSA',                                                                            'Liquidar Suscripcion OSA', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	101	,	4,    'Liquidar Vcto Deposito',                                                                              'Liquidar Vcto Deposito', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	102	,	4,    'Liquidar Vcto RF',                                                                                    'Liquidar Vcto RF', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	103	,	4,    'Liquidar Operacion Spot USD',                                                                         'Liquidar Operacion Spot USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	104	,	4,    'Liquidar Pagos Corredores PH/PM/CN OIB',                                                              'Liquidar Pagos Corredores PH/PM/CN OIB', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	105	,	4,    'Mantenedor de Movtos. Automaticos',                                                                   'Mantenedor de Movtos. Automaticos', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	106	,	4,    'Modifica Banco',                                                                                      'Modifica Banco', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	107	,	4,    'Orden de Ingreso CLP',                                                                                'Orden de Ingreso CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	108	,	4,    'Orden de Ingreso EUR',                                                                                'Orden de Ingreso EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	109	,	4,    'Orden de Ingreso USD',                                                                                'Orden de Ingreso USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	110	,	4,    'Orden de Pago CLP',                                                                                   'Orden de Pago CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	111	,	4,    'Orden de Pago EUR',                                                                                   'Orden de Pago EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	112	,	4,    'Orden de Pago USD',                                                                                   'Orden de Pago USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	113	,	4,    'Pago Electronico',                                                                                    'Pago Electronico', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	114	,	4,    'Partida de Egreso',                                                                                   'Partida de Egreso', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	115	,	4,    'Partida de Ingreso',                                                                                  'Partida de Ingreso', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	116	,	4,    'Ingreso Abono USD',                                                                                   'Ingreso Abono USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	117	,	4,    'Ingreso Cargo USD',                                                                                   'Ingreso Cargo USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	118	,	4,    'Ingreso Abono EUR',                                                                                   'Ingreso Abono EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	119	,	4,    'Ingreso Cargo EUR',                                                                                   'Ingreso Cargo EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	120	,	4,    'Ingreso Abono CLP',                                                                                   'Ingreso Abono CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	121	,	4,    'Ingreso Cargo CLP',                                                                                   'Ingreso Cargo CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	122	,	4,    'Transferencia EUR',                                                                                   'Transferencia EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	123	,	4,    'Transferencia USD',                                                                                   'Transferencia USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	124	,	4,    'Transferencia CLP',                                                                                   'Transferencia CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	125	,	4,    'Cierre de Tesorería',                                                                                 'Cierre de Tesorería', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	126	,	4,    'Anular Abono CLP',                                                                                    'Anular Abono CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	127	,	4,    'Anular Abono EUR',                                                                                    'Anular Abono EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	128	,	4,    'Anular Abono USD',                                                                                    'Anular Abono USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	129	,	4,    'Anular Abono Regularizado CLP',                                                                       'Anular Abono Regularizado CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	130	,	4,    'Anular Abono Regularizado EUR',                                                                       'Anular Abono Regularizado EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	131	,	4,    'Anular Abono Regularizado USD',                                                                       'Anular Abono Regularizado USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	132	,	4,    'Anular Cargo CLP',                                                                                    'Anular Cargo CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	133	,	4,    'Anular Cargo EUR',                                                                                    'Anular Cargo EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	134	,	4,    'Anular Cargo USD',                                                                                    'Anular Cargo USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	135	,	4,    'Anular Cargo Regularizado CLP',                                                                       'Anular Cargo Regularizado CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	136	,	4,    'Anular Cargo Regularizado EUR',                                                                       'Anular Cargo Regularizado EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	137	,	4,    'Anular Cargo Regularizado USD',                                                                       'Anular Cargo Regularizado USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	138	,	4,    'Anular Operaciones CLP',                                                                              'Anular Operaciones CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	139	,	4,    'Anular Operaciones EUR',                                                                              'Anular Operaciones EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	140	,	4,    'Anular Operaciones USD',                                                                              'Anular Operaciones USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	141	,	4,    'Anular Orden de Ingreso CLP',                                                                         'Anular Orden de Ingreso CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	142	,	4,    'Anular Orden de Ingreso EUR',                                                                         'Anular Orden de Ingreso EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	143	,	4,    'Anular Orden de Ingreso USD',                                                                         'Anular Orden de Ingreso USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	144	,	4,    'Anular Orden de Pago CLP',                                                                            'Anular Orden de Pago CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	145	,	4,    'Anular Orden de Pago EUR',                                                                            'Anular Orden de Pago EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	146	,	4,    'Anular Orden de Pago USD',                                                                            'Anular Orden de Pago USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	147	,	4,    'Anular Partida de Egreso CLP',                                                                        'Anular Partida de Egreso CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	148	,	4,    'Anular Partida de Ingreso CLP',                                                                       'Anular Partida de Ingreso CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	149	,	4,    'Anular Operacion Spot USD',                                                                           'Anular Operacion Spot USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	150	,	4,    'Anular Operacion Spot EUR',                                                                           'Anular Operacion Spot EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	151	,	4,    'Anular Operacion Spot MX',                                                                            'Anular Operacion Spot MX', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	152	,	4,    'Desmarcar Operacion Spot USD',                                                                        'Desmarcar Operacion Spot USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	153	,	4,    'Asignar Deposito',                                                                                    'Asignar Deposito', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	154	,	4,    'Asignar Moneda CLP',                                                                                  'Asignar Moneda CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	155	,	4,    'Asignar Moneda EUR',                                                                                  'Asignar Moneda EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	156	,	4,    'Asignar Moneda USD',                                                                                  'Asignar Moneda USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	157	,	4,    'Carga Masiva Boton Pago',                                                                             'Carga Masiva Boton Pago', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	158	,	4,    'Informe de comprobantes sin certificación',                                                           'Informe de comprobantes sin certificación', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	159	,	4,    'Informe de Comprobantes - TECOMP',                                                                    'Informe de Comprobantes - TECOMP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	160	,	4,    'Informe de revisión de procesos de cierre',                                                           'Informe de revisión de procesos de cierre', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	161	,	4,    'Informe de revisión de procesos de cierre CLP',                                                       'Informe de revisión de procesos de cierre CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	162	,	4,    'Informe de revisión de procesos de cierre EUR',                                                       'Informe de revisión de procesos de cierre EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	163	,	4,    'Informe de revisión de procesos de cierre USD',                                                       'Informe de revisión de procesos de cierre USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	164	,	4,    'Girar - Cargo Masivo',                                                                                'Girar - Cargo Masivo', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	165	,	4,    'Certificar - Cargo Masivo',                                                                           'Certificar - Cargo Masivo', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	166	,	4,    'Certificar Abono CLP',                                                                                'Certificar Abono CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	167	,	4,    'Certificar Abono EUR',                                                                                'Certificar Abono EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	168	,	4,    'Certificar Abono USD',                                                                                'Certificar Abono USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	169	,	4,    'Certificar Abono Regularizado CLP',                                                                   'Certificar Abono Regularizado CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	170	,	4,    'Certificar Abono Regularizado EUR',                                                                   'Certificar Abono Regularizado EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	171	,	4,    'Certificar Abono Regularizado USD',                                                                   'Certificar Abono Regularizado USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	172	,	4,    'Certificar Cargo CLP',                                                                                'Certificar Cargo CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	173	,	4,    'Certificar Cargo EUR',                                                                                'Certificar Cargo EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	174	,	4,    'Certificar Cargo USD',                                                                                'Certificar Cargo USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	175	,	4,    'Certificar Cargo Masivo',                                                                             'Certificar Cargo Masivo', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	176	,	4,    'Certificar Cargo Regularizado CLP',                                                                   'Certificar Cargo Regularizado CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	177	,	4,    'Certificar Cargo Regularizado EUR',                                                                   'Certificar Cargo Regularizado EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	178	,	4,    'Certificar Cargo Regularizado USD',                                                                   'Certificar Cargo Regularizado USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	179	,	4,    'Certificar lector',                                                                                   'Certificar lector', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	180	,	4,    'Certificación Masiva Abono',                                                                          'Certificación Masiva Abono', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	181	,	4,    'Certificación Masiva Cargo',                                                                          'Certificación Masiva Cargo', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	182	,	4,    'Certificación Masiva Egreso',                                                                         'Certificación Masiva Egreso', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	183	,	4,    'Certificación Masiva Ingreso',                                                                        'Certificación Masiva Ingreso', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	184	,	4,    'Certificar Orden de Ingreso CLP',                                                                     'Certificar Orden de Ingreso CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	185	,	4,    'Certificar Orden de Ingreso EUR',                                                                     'Certificar Orden de Ingreso EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	186	,	4,    'Certificar Orden de Ingreso USD',                                                                     'Certificar Orden de Ingreso USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	187	,	4,    'Certificar Orden de Pago CLP',                                                                        'Certificar Orden de Pago CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	188	,	4,    'Certificar Orden de Pago EUR',                                                                        'Certificar Orden de Pago EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	189	,	4,    'Certificar Orden de Pago USD',                                                                        'Certificar Orden de Pago USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	190	,	4,    'Certificar Transferencia CLP',                                                                        'Certificar Transferencia CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	191	,	4,    'Certificar Transferencia EUR',                                                                        'Certificar Transferencia EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	192	,	4,    'Certificar Transferencia USD',                                                                        'Certificar Transferencia USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	193	,	4,    'Aceptar Deposito CLP',                                                                                'Aceptar Deposito CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	194	,	4,    'Aceptar Deposito MX',                                                                                 'Aceptar Deposito MX', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	195	,	4,    'Devolver Deposito CLP',                                                                               'Devolver Deposito CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	196	,	4,    'Devolver Deposito MX',                                                                                'Devolver Deposito MX', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	197	,	4,    'Anular Cheque',                                                                                       'Anular Cheque', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	198	,	4,    'Certificar Cheque',                                                                                   'Certificar Cheque', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	199	,	4,    'Clearing',                                                                                            'Clearing', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	200	,	4,    'Deposito Efectivo CLP',                                                                               'Deposito Efectivo CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	201	,	4,    'Deposito Efectivo EUR',                                                                               'Deposito Efectivo EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	202	,	4,    'Deposito Efectivo USD',                                                                               'Deposito Efectivo USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	203	,	4,    'Egreso Billete EUR',                                                                                  'Egreso Billete EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	204	,	4,    'Egreso Billete USD',                                                                                  'Egreso Billete USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	205	,	4,    'Emision Cheque',                                                                                      'Emision Cheque', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	206	,	4,    'Envio a Deposito Documentos',                                                                         'Envio a Deposito Documentos', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	207	,	4,    'Envio a Deposito Documentos CLP',                                                                     'Envio a Deposito Documentos CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	208	,	4,    'Envio a Deposito Documentos USD',                                                                     'Envio a Deposito Documentos USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	209	,	4,    'Genera Cargo/Abono Factura (CTLIPH)',                                                                 'Genera Cargo/Abono Factura (CTLIPH)', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	210	,	4,    'Genera Cargo/Abono SCL',                                                                              'Genera Cargo/Abono SCL', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	211	,	4,    'Genera Orden de FFMM',                                                                                'Genera Orden de FFMM', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	212	,	4,    'Genera Traspaso',                                                                                     'Genera Traspaso', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	213	,	4,    'Genera Traspaso ve',                                                                                  'Genera Traspaso ve', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	214	,	4,    'Ingreso Banco',                                                                                       'Ingreso Banco', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	215	,	4,    'Ingreso Billete EUR',                                                                                 'Ingreso Billete EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	216	,	4,    'Ingreso Billete USD',                                                                                 'Ingreso Billete USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	217	,	4,    'Libera Documentos en Canje',                                                                          'Libera Documentos en Canje', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	218	,	4,    'Libera Saldos x Vcto',                                                                                'Libera Saldos x Vcto', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	219	,	4,    'Liquidar Cargo/Abono AGF',                                                                            'Liquidar Cargo/Abono AGF', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	220	,	4,    'Liquidar Cupones',                                                                                    'Liquidar Cupones', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	221	,	4,    'Liquidar Dividendos',                                                                                 'Liquidar Dividendos', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	222	,	4,    'Liquidar Operaciones CLP',                                                                            'Liquidar Operaciones CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	223	,	4,    'Liquidar Operaciones USD',                                                                            'Liquidar Operaciones USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	224	,	4,    'Liquidar Operaciones EUR',                                                                            'Liquidar Operaciones EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	225	,	4,    'Liquidar Pactos',                                                                                     'Liquidar Pactos', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	226	,	4,    'Liquidar Rescates FFMM',                                                                              'Liquidar Rescates FFMM', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	227	,	4,    'Liquidar Resultado Futuro',                                                                           'Liquidar Resultado Futuro', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	228	,	4,    'Liquidar Simultaneas Cliente',                                                                        'Liquidar Simultaneas Cliente', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	229	,	4,    'Liquidar Simultaneas Corredora',                                                                      'Liquidar Simultaneas Corredora', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	230	,	4,    'Liquidar Sorteo de Letras',                                                                           'Liquidar Sorteo de Letras', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	231	,	4,    'Liquidar Suscripcion Osa',                                                                            'Liquidar Suscripcion Osa', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	232	,	4,    'Liquidar Vcto Deposito',                                                                              'Liquidar Vcto Deposito', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	233	,	4,    'Liquidar Vcto RF',                                                                                    'Liquidar Vcto RF', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	234	,	4,    'Liquidar Operacion Spot USD',                                                                         'Liquidar Operacion Spot USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	235	,	4,    'Liquidar Pagos Corredores PH/PM/CN OIB',                                                              'Liquidar Pagos Corredores PH/PM/CN OIB', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	236	,	4,    'Liquidar Operacion Spot MX',                                                                          'Liquidar Operacion Spot MX', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	237	,	4,    'Mantenedor de Movtos. Automaticos',                                                                   'Mantenedor de Movtos. Automaticos', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	238	,	4,    'Modifica Banco',                                                                                      'Modifica Banco', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	239	,	4,    'Orden de Ingreso CLP',                                                                                'Orden de Ingreso CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	240	,	4,    'Orden de Ingreso EUR',                                                                                'Orden de Ingreso EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	241	,	4,    'Orden de Ingreso USD',                                                                                'Orden de Ingreso USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	242	,	4,    'Orden de Pago CLP',                                                                                   'Orden de Pago CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	243	,	4,    'Orden de Pago EUR',                                                                                   'Orden de Pago EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	244	,	4,    'Orden de Pago USD',                                                                                   'Orden de Pago USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	245	,	4,    'Pago Electronico',                                                                                    'Pago Electronico', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	246	,	4,    'Partida de Egreso',                                                                                   'Partida de Egreso', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	247	,	4,    'Partida de Ingreso',                                                                                  'Partida de Ingreso', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	248	,	4,    'Ingreso Abono USD',                                                                                   'Ingreso Abono USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	249	,	4,    'Ingreso Cargo USD',                                                                                   'Ingreso Cargo USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	250	,	4,    'Ingreso Abono EUR',                                                                                   'Ingreso Abono EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	251	,	4,    'Ingreso Cargo EUR',                                                                                   'Ingreso Cargo EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	252	,	4,    'Ingreso Abono CLP',                                                                                   'Ingreso Abono CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	253	,	4,    'Ingreso Cargo (CRM)',                                                                                 'Ingreso Cargo (CRM)', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	254	,	4,    'Transferencia EUR',                                                                                   'Transferencia EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	255	,	4,    'Transferencia USD',                                                                                   'Transferencia USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	256	,	4,    'Transferencia CLP',                                                                                   'Transferencia CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	257	,	4,    'Cierre por moneda CLP',                                                                               'Cierre por moneda CLP', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	258	,	4,    'Cierre por moneda EUR',                                                                               'Cierre por moneda EUR', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	259	,	4,    'Cierre por moneda USD',                                                                               'Cierre por moneda USD', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (	260	,	4,    'Proceso de cierre de Tesorería',                                                                      'Proceso de cierre de Tesorería', 'ACT');

--FUNCIONES ORDENS
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (261, 1, 'UI_CUADRAR_ORDENES',  'UI_CUADRAR_ORDENES', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (262, 1, 'UI_VENCER_ORDENES',  'UI_VENCER_ORDENES', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (263, 1, 'UI_OFICIALIZAR_ORDENES',  'UI_OFICIALIZAR_ORDENES', 'ACT');

--TIPO REQUISITOS
INSERT INTO "BSCL.Plataforma.BD::CRE.TipoRequisito" ("IdTipoRequisito", "Nombre", "IdEstado")
VALUES('HOR', 'HORA', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.TipoRequisito" ("IdTipoRequisito", "Nombre", "IdEstado")
VALUES('USU', 'USUARIO', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.TipoRequisito" ("IdTipoRequisito", "Nombre", "IdEstado")
VALUES('PRF', 'PERFIL', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::CRE.TipoRequisito" ("IdTipoRequisito", "Nombre", "IdEstado")
VALUES('PRO', 'PROCESO FUNCIONES', 'ACT');
