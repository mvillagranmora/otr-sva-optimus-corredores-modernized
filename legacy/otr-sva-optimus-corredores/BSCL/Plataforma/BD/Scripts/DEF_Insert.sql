SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM "BSCL.Plataforma.BD::PLA.ValorDefectoSistema";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoCliente','Tipo Cliente','RETAIL',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoDocumentoIdentidad','Tipo Documento Identidad','R',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoCuentaBancaria','Tipo Cuenta Bancaria','CUCO',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoLibroOrden','Tipo Libro','NORMAL',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoLibroOrden','Estado Libro Orden','ACT',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoPlantillaLimitesDetalle','Estado Plantilla Límites Detalle','ACT',1,3,NOW(),NOW(),'ACT');^
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdMonedaPlantillaLimitesDetalle','Moneda Plantilla Límites Detalle','CLP',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdMonedaLimiteLineaSobregiro','Moneda Límite Línea Sobregiro','CLP',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoLimiteLineaSobregiro','Estado Límite Línea Sobregiro','ACT',1,3,NOW(),NOW(),'ACT');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEntidadClasificadoraRiesgoLimite','Entidad Clasificadora Riesgo Límite ','STDANDPOOR',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoLimiteCuentaPropia','Estado Límite Cuenta Propia','ACT',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoEmisorLimiteCuentaPropia','Estado Emisor Límite Cuenta Propia','ACT',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdMonedaLimiteCuentaPropia','Moneda Límite Cuenta Propia','ACT',1,3,NOW(),NOW(),'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ValorDefectoSistema"
("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado")
VALUES('PLACEHOLDER_RUT','PLACEHOLDER_RUT','12345678-9',1,3,NOW(),NOW(),'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.ValorDefectoSistema"
("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado")
VALUES('PLACEHOLDER_TELEFONO','PLACEHOLDER_TELEFONO','+56 1 2345 6789',1,3,NOW(),NOW(),'ACT');





/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
