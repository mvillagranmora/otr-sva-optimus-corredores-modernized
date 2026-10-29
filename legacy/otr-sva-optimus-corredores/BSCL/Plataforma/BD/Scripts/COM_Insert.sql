SET SCHEMA "BSCL"; 

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/ 
-- Productos
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePerfilProductos";
DELETE FROM "BSCL.Plataforma.BD::COM.PerfilProductos";
-- Cobros
DELETE FROM "BSCL.Plataforma.BD::COM.Cobro";
DELETE FROM "BSCL.Plataforma.BD::COM.CobroClasificacion";
DELETE FROM "BSCL.Plataforma.BD::COM.ValorCobro";
DELETE FROM "BSCL.Plataforma.BD::COM.PerfilCobros" ;
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePerfilCobros";
DELETE FROM "BSCL.Plataforma.BD::COM.PerfilCobros";
-- Limites
-- DELETE FROM "BSCL.Plataforma.BD::COM.";
-- Condiciones Operativas
DELETE FROM "BSCL.Plataforma.BD::COM.DetalleCondicionOperativa";
DELETE FROM "BSCL.Plataforma.BD::COM.CondicionOperativa";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoCondicionOperativa";
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePerfilCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.PerfilCondicionesOperativas";
-- Preferencias Facturación
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePreferenciaFacturacion";
DELETE FROM "BSCL.Plataforma.BD::COM.PreferenciaFacturacion";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoPreferenciaFacturacion";
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePerfilFacturacion";
DELETE FROM "BSCL.Plataforma.BD::COM.PerfilFacturacion";
-- Formas de Pago Permitidas
--DELETE FROM "BSCL.Plataforma.BD::COM.FormaPago";
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePerfilFormasPago";
DELETE FROM "BSCL.Plataforma.BD::COM.PerfilFormasPago";
-- Grupo de Activos
 DELETE FROM "BSCL.Plataforma.BD::COM.TipoGrupoActivos";
-- Perfil Comercial Base
DELETE FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase";
-- Categoria Inversionista
DELETE FROM "BSCL.Plataforma.BD::COM.CategoriaInversionista";
-- Condiciones Operativas
DELETE FROM "BSCL.Plataforma.BD::COM.OperacionUsuario";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoEtapa";
DELETE FROM "BSCL.Plataforma.BD::COM.EtapaOperacion";
DELETE FROM "BSCL.Plataforma.BD::COM.EtapaCondicionOperativa";
DELETE FROM "BSCL.Plataforma.BD::COM.CondicionOperativa";
DELETE FROM "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoCondicion" ;

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

-- EstadosPerfilComercial
INSERT INTO "BSCL.Plataforma.BD::COM.EstadoPerfilComercial" ("IdEstadoPerfilComercial","IdEstado","Entidad")
VALUES(1,'ACT','');
INSERT INTO "BSCL.Plataforma.BD::COM.EstadoPerfilComercial" ("IdEstadoPerfilComercial","IdEstado","Entidad")
VALUES(2,'INA','');

-- CategoriaInversionista
INSERT INTO "BSCL.Plataforma.BD::COM.CategoriaInversionista" ("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa")
VALUES ('ARRIESGADO','Arriesgado',44);
INSERT INTO "BSCL.Plataforma.BD::COM.CategoriaInversionista" ("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa")
VALUES ('BALANCEADO','Balanceado',44);
INSERT INTO "BSCL.Plataforma.BD::COM.CategoriaInversionista" ("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa")
VALUES ('CONSERVADOR','Conservador',44);
INSERT INTO "BSCL.Plataforma.BD::COM.CategoriaInversionista" ("IdCategoriaInversionista","NombreCategoriaInversionista","IdEmpresa")
VALUES ('CALIFICADO','Calificado',44);

--*********** PERFIL DE PRODUCTOS*************

-- PerfilProductos
INSERT INTO "BSCL.Plataforma.BD::COM.PerfilProductos" ("IdPerfilProductos","DescripcionPerfilProductos","IdCategoriaInversionista","EsPerfilPersonalizado","FechaCreacion","FechaModificacion","IdEstado","PorDefecto","IdEmpresa")
VALUES ('PROD_CLIENTE_PREMIUM','Cliente Premium','ARRIESGADO','N','2015-10-29','2015-10-29','ACT','N',1);

-- DetallePerfilProductos
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilProductos" ("IdDetallePerfilProductos","IdPerfilProductos","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES (1,'PROD_CLIENTE_PREMIUM','INAC','COMACC','AGE','CLP','2015-10-29','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilProductos" ("IdDetallePerfilProductos","IdPerfilProductos","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES (2,'PROD_CLIENTE_PREMIUM','INSI','COMSIM','AGE','CLP','2015-10-29','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilProductos" ("IdDetallePerfilProductos","IdPerfilProductos","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES (3,'PROD_CLIENTE_PREMIUM','INSI','VENSIM','AGE','CLP','2015-10-29','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilProductos" ("IdDetallePerfilProductos","IdPerfilProductos","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES (4,'PROD_CLIENTE_PREMIUM','INAC','COMACC','CLI','CLP','2015-10-29','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilProductos" ("IdDetallePerfilProductos","IdPerfilProductos","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES (5,'PROD_CLIENTE_PREMIUM','INSI','COMSIM','CLI','CLP','2015-10-29','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilProductos" ("IdDetallePerfilProductos","IdPerfilProductos","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES (6,'PROD_CLIENTE_PREMIUM','INSI','VENSIM','CLI','CLP','2015-10-29','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilProductos" ("IdDetallePerfilProductos","IdPerfilProductos","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES (7,'PROD_CLIENTE_PREMIUM','INAC','VENACC','CLI','CLP','2015-10-29','ACT',1);

-- **********PERFIL DE COBROS**************** 

-- CobroClasificacion
INSERT INTO "BSCL.Plataforma.BD::COM.CobroClasificacion" ("IdCobroClasificacion", "CodigoCobroClasificacion", "DescripcionCobroClasificacion", "IdEmpresa")
VALUES (1,'COMISION','Comisión',1);
INSERT INTO "BSCL.Plataforma.BD::COM.CobroClasificacion" ("IdCobroClasificacion", "CodigoCobroClasificacion", "DescripcionCobroClasificacion", "IdEmpresa")
VALUES (2,'DERECHO','DERECHO',1);
INSERT INTO "BSCL.Plataforma.BD::COM.CobroClasificacion" ("IdCobroClasificacion", "CodigoCobroClasificacion", "DescripcionCobroClasificacion", "IdEmpresa")
VALUES (3,'GASTO','GASTO',1);
INSERT INTO "BSCL.Plataforma.BD::COM.CobroClasificacion" ("IdCobroClasificacion", "CodigoCobroClasificacion", "DescripcionCobroClasificacion", "IdEmpresa")
VALUES (4,'IMPUESTO','Impuesto',1);
INSERT INTO "BSCL.Plataforma.BD::COM.CobroClasificacion" ("IdCobroClasificacion", "CodigoCobroClasificacion", "DescripcionCobroClasificacion", "IdEmpresa")
VALUES (5,'RETENCION','Retención',1);
--Cobro
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('COFI','Comisión Fija',1,'F',5000,'CLP','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('COSI','Comisión Simultáneas',1,'V',0.7,'POR','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('COVA','Comisión Variable',1,'V',0.5,'POR','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('DESI','Derecho Simultáneas',2,'V',0.0033,'POR','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('DEFI','Derecho Fijo',2,'F',1000,'CLP','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('GFOD','Gasto Fijo por Orden Diaria',3,'F',5000,'CLP','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('IVA','Impuesto Iva',4,'V',0.19,'POR','N',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('COAI','Comisión All Inclusive',1,'V',0.04,'POR','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('COEX','Comisión Exenta',1,'V',0.04,'POR','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('COIF','Comisión Especial IIF por plazo',1,'V',0.04,'POR','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('COLH','Comisión Especial LH por emisor',1,'V',0.04,'POR','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('COMD','Comisión Fija Diaria',1,'F',5000,'CLP','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('DEEX','Derecho Exento',2,'V',0.04,'POR','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('DEVA','Derecho Variable',2,'V',0.03,'POR','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('GAEX','Gasto Exento',3,'V',0.03,'POR','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('GFFA','Gasto Fijo por Factura',3,'F',5000,'CLP','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('GFFD','Gasto Fijo por Factura Diaria',3,'F',6000,'CLP','S',1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::COM.Cobro" ("IdCobro","NombreCobro","IdCobroClasificacion","FormaCobro","ValorCobro","UnidadDeCobro","PermiteCambiarValor","IdEmpresa")
VALUES('DAAO','Derecho Futuros',2,'F',6000,'CLP','S',1);


--ValorCobro
--INAC
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INAC_A','COVA_INAC_A','INAC','COVA',0.5,2000,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INAC_B','COVA_INAC_B','INAC','COVA',0.5,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('DEFI_INAC_A','DEFI_INAC_A','INAC','DEFI',2000,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('GFOD_INAC_A','GFOD_INAC_A','INAC','GFOD',5000,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('IVA_INAC_A','IVA_INAC_A','INAC','IVA',19,0,'CLP','2015-11-05','ACT',1);
--INSI
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INSI_A','COVA_INSI_A','INSI','COVA',0.4,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INSI_B','COVA_INSI_B','INSI','COVA',0.4,1100,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('DESI_INSI_A','DEFI_INSI_A','INSI','DEFI',2100,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('GFOD_INSI_A','GFOD_INSI_A','INSI','GFOD',5400,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('IVA_INSI_A','IVA_INSI_A','INSI','IVA',19,0,'CLP','2015-11-05','ACT',1);
--INAX
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INAX_A','COVA_INAX_A','INAX','COVA',0.42,2000,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INAX_B','COVA_INAX_B','INAX','COVA',0.26,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('DEFI_INAX_A','DEFI_INAX_A','INAX','DEFI',2400,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('GFOD_INAX_A','GFOD_INAX_A','INAX','GFOD',5100,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('IVA_INAX_A','IVA_INAX_A','INAX','IVA',19,0,'CLP','2015-11-05','ACT',1);

--INAX
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INAX_A','COVA_INAX_A','IC','COVA',0.42,2000,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INAX_B','COVA_INAX_B','INAX','COVA',0.26,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('DEFI_INAX_A','DEFI_INAX_A','INAX','DEFI',2400,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('GFOD_INAX_A','GFOD_INAX_A','INAX','GFOD',5100,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('IVA_INAX_A','IVA_INAX_A','INAX','IVA',19,0,'CLP','2015-11-05','ACT',1);

--INCI
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INCI_A','COVA_INCI_A','INCI','COVA',0.48,2000,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INCI_B','COVA_INCI_B','INCI','COVA',0.23,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('DEFI_INCI_A','DEFI_INCI_A','INCI','DEFI',6000,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('GFOD_INCI_A','GFOD_INCI_A','INCI','GFOD',5100,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('IVA_INCI_A','IVA_INCI_A','INCI','IVA',19,0,'CLP','2015-11-05','ACT',1);

--INCX
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INCX_A','COVA_INCX_A','INCX','COVA',0.82,2000,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INCX_B','COVA_INCX_B','INCX','COVA',0.41,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('DEFI_INCX_A','DEFI_INCX_A','INCX','DEFI',3000,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('GFOD_INCX_A','GFOD_INCX_A','INCX','GFOD',5700,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('IVA_INCX_A','IVA_INCX_A','INCX','IVA',19,0,'CLP','2015-11-05','ACT',1);
-- INFU
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INFU_A','COVA_INFU_A','INFU','COVA',0.82,2000,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INFU_B','COVA_INFU_B','INFU','COVA',0.41,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('DEFI_INFU_A','DEFI_INFU_A','INFU','DEFI',3000,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('GFOD_INFU_A','GFOD_INFU_A','INFU','GFOD',5700,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('IVA_INFU_A','IVA_INFU_A','INFU','IVA',19,0,'CLP','2015-11-05','ACT',1);


-- INRF
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INRF_A','COVA_INRF_A','INRF','COVA',0.82,2000,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INRF_B','COVA_INRF_B','INRF','COVA',0.41,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('DEFI_INRF_A','DEFI_INRF_A','INRF','DEFI',3000,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('GFOD_INRF_A','GFOD_INRF_A','INRF','GFOD',5700,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('IVA_INRF_A','IVA_INRF_A','INRF','IVA',19,0,'CLP','2015-11-05','ACT',1);

-- INIF
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INIF_A','COVA_INIF_A','INIF','COVA',0.82,2000,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INIF_B','COVA_INIF_B','INIF','COVA',0.41,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('DEFI_INIF_A','DEFI_INIF_A','INIF','DEFI',3000,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('GFOD_INIF_A','GFOD_INIF_A','INIF','GFOD',5700,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('IVA_INIF_A','IVA_INIF_A','INIF','IVA',19,0,'CLP','2015-11-05','ACT',1);


-- INMI
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INMI_A','COVA_INMI_A','INMI','COVA',0.82,2000,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INMI_B','COVA_INMI_B','INMI','COVA',0.41,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('DEFI_INMI_A','DEFI_INMI_A','INMI','DEFI',3000,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('GFOD_INMI_A','GFOD_INMI_A','INMI','GFOD',5700,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('IVA_INMI_A','IVA_INMI_A','INMI','IVA',19,0,'CLP','2015-11-05','ACT',1);


-- INST
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INST_A','COVA_INST_A','INST','COVA',0.82,2000,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INST_B','COVA_INST_B','INST','COVA',0.41,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('DEFI_INST_A','DEFI_INST_A','INST','DEFI',3000,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('GFOD_INST_A','GFOD_INST_A','INST','GFOD',5700,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('IVA_INST_A','IVA_INST_A','INST','IVA',19,0,'CLP','2015-11-05','ACT',1);

-- INEX
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INEX_A','COVA_INEX_A','INEX','COVA',0.82,2000,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('COVA_INEX_B','COVA_INEX_B','INEX','COVA',0.41,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('DEFI_INEX_A','DEFI_INEX_A','INEX','DEFI',3000,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('GFOD_INEX_A','GFOD_INEX_A','INEX','GFOD',5700,0,'CLP','2015-11-05','ACT',1);
INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
VALUES ('IVA_INEX_A','IVA_INEX_A','INEX','IVA',19,0,'CLP','2015-11-05','ACT',1);





--PerfilCobros
INSERT INTO "BSCL.Plataforma.BD::COM.PerfilCobros" ("IdPerfilCobros","DescripcionPerfilCobros","IdCategoriaInversionista","EsPerfilPersonalizado","FechaCreacion","FechaModificacion","IdEstado","PorDefecto","IdEmpresa")
VALUES('COB_CLIENTE_PREMIUM','Cliente Premium','ARRIESGADO','N','2015-10-29','2015-10-29','ACT','S',1);
INSERT INTO "BSCL.Plataforma.BD::COM.PerfilCobros" ("IdPerfilCobros","DescripcionPerfilCobros","IdCategoriaInversionista","EsPerfilPersonalizado","FechaCreacion","FechaModificacion","IdEstado","PorDefecto","IdEmpresa")
VALUES('COB_CLIENTE_PREMIUM_ESP','Cliente Premium Especial','ARRIESGADO','S','2015-10-29','2015-10-29','ACT','N',1);

--DetallePerfilCobros
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(1,'COB_CLIENTE_PREMIUM','COVA_INAC_A','BCS','INAC','COMACC','CLI','CLP',0,1,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(2,'COB_CLIENTE_PREMIUM','DEFI_INAC_A','BCS','INAC','COMACC','CLI','CLP',0,1,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(3,'COB_CLIENTE_PREMIUM','GFOD_INAC_A','BCS','INAC','COMACC','CLI','CLP',0,2,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(4,'COB_CLIENTE_PREMIUM','COVA_INSI_A','BCS','INSI','COMSIM','CLI','CLP',0,3,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(5,'COB_CLIENTE_PREMIUM','DESI_INSI_A','BCS','INSI','COMSIM','CLI','CLP',0,4,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(6,'COB_CLIENTE_PREMIUM','GFOD_INSI_A','BCS','INSI','COMSIM','CLI','CLP',0,5,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(7,'COB_CLIENTE_PREMIUM','COVA_INSI_A','BCS','INSI','VENSIM','CLI','CLP',0,6,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(8,'COB_CLIENTE_PREMIUM','DESI_INSI_A','BCS','INSI','VENSIM','CLI','CLP',0,7,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(9,'COB_CLIENTE_PREMIUM','GFOD_INSI_A','BCS','INSI','VENSIM','CLI','CLP',0,8,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(13,'COB_CLIENTE_PREMIUM','COVA_INAC_A','BCS','INAC','VENACC','CLI','CLP',0,1,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(14,'COB_CLIENTE_PREMIUM','DEFI_INAC_A','BCS','INAC','VENACC','CLI','CLP',0,1,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(12,'COB_CLIENTE_PREMIUM','GFOD_INAC_A','BCS','INAC','VENACC','CLI','CLP',0,2,'2015-10-29','ACT');

INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(18,'COB_CLIENTE_PREMIUM','COVA_INAC_A','BCS','INAC','0','0','CLP',0,10,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(15,'COB_CLIENTE_PREMIUM','COVA_INAC_A','BCS','INAC','0','0','CLP',0,10,'2015-10-29','ACT');

-- + el IVA
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(16,'COB_CLIENTE_PREMIUM','IVA_INAC_A','BCS','INAC','0','0','CLP',0,9,'2015-10-29','ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilCobros" ("IdDetallePerfilCobros","IdPerfilCobros","IdValorCobro","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdMoneda","DiasPlazo","Prioridad","FechaCreacion","IdEstado")
VALUES(17,'COB_CLIENTE_PREMIUM','DEFI_INAC_A','BCS','INAC','0','0','CLP',0,10,'2015-10-29','ACT');

--**************** GRUPO DE ACTIVOS *******************

--Tipo Grupo Activos.
INSERT INTO "BSCL.Plataforma.BD::COM.TipoGrupoActivos" ("IdTipoGrupoActivos","Nombre")
VALUES ('INCLUYENTE','Incluyente');
INSERT INTO "BSCL.Plataforma.BD::COM.TipoGrupoActivos" ("IdTipoGrupoActivos","Nombre")
VALUES ('EXCLUYENTE','Excluyente');

--**************** PERFIL COMERCIAL BASE **************
INSERT INTO "BSCL.Plataforma.BD::COM.PerfilComercialBase" ("IdPerfilComercialBase","DescripcionPerfilComercialBase","IdCategoriaInversionista","IdPerfilProductos","IdPerfilCobros","IdPerfilLimites","IdPerfilCondicionesOperativas","IdPerfilFacturacion","IdPerfilFormasPagoPermitidos","FechaCreacion","FechaModificacion","IdEstado","PorDefecto","IdEmpresa")
VALUES('CLIENTE_PREMIUM','Perfil Clientes Premium','ARRIESGADO','PROD_CLIENTE_PREMIUM','COB_CLIENTE_PREMIUM','0','0','0','0','2015-10-29','2015-10-29','ACT','S',1);



--**************** CONDICIONES OPERATIVAS **************

drop sequence "BSCL.Plataforma.BD.Secuencias::CondicionOperativa";
create sequence "BSCL.Plataforma.BD.Secuencias::CondicionOperativa" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::DetallePlantillaCondicionesOperativas";
create sequence "BSCL.Plataforma.BD.Secuencias::DetallePlantillaCondicionesOperativas" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::EtapaCondicionOperativa";
create sequence "BSCL.Plataforma.BD.Secuencias::EtapaCondicionOperativa" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::EtapaGrupoCondicionesOperativas";
create sequence "BSCL.Plataforma.BD.Secuencias::EtapaGrupoCondicionesOperativas" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::EtapaOperacion";
create sequence "BSCL.Plataforma.BD.Secuencias::EtapaOperacion" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::GrupoCondicionesOperativas";
create sequence "BSCL.Plataforma.BD.Secuencias::GrupoCondicionesOperativas" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::OperacionUsuario";
create sequence "BSCL.Plataforma.BD.Secuencias::OperacionUsuario" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::TipoEtapa";
create sequence "BSCL.Plataforma.BD.Secuencias::TipoEtapa" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;


-- Condiciones Operativas
DELETE FROM "BSCL.Plataforma.BD::COM.OperacionUsuario";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoEtapa";
DELETE FROM "BSCL.Plataforma.BD::COM.EtapaOperacion";
DELETE FROM "BSCL.Plataforma.BD::COM.EtapaCondicionOperativa";
DELETE FROM "BSCL.Plataforma.BD::COM.CondicionOperativa";
DELETE FROM "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoCondicion" ;
--select * from "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion"


--OperacionUsuario
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES ('ENROCLI','Enrolamiento Cliente','',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES ('ENROPER','Enrolamiento Persona','',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES ('NUEPORTF','Creación Portafolio','',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES ('COMACN','Compra Acciones RV','COMACN',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES ('VENACN','Venta Acciones RV','VENACN',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.OperacionUsuario"


-- Tipo Etapa
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES ('INGRESO','Ingreso',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES ('ASINACION','Asignación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES ('ENVFACT','Envío a Facturar',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES ('GCOMFACT','Generación comprobante Facturación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES ('FACTDTE','Facturación DTE',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.TipoEtapa"


-- Etapa Operacion
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('INGRESO_ENROCLI','ENROCLI','INGRESO','Ingreso de cliente','Ingreso',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('INGRESO_ENROPER','ENROPER','INGRESO','Ingreso de cliente','Ingreso',NOW());
--
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('INGRESO_NUEPORTF','NUEPORTF','INGRESO','Ingreso de portafolio','Ingreso',NOW());
--
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('INGRESO_COMACN','COMACN','INGRESO','Ingreso de compra de Acciones RV','Ingreso',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('ASINACION_COMACN','COMACN','ASINACION','Asignación de compra de Acciones RV','Asignación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('ENVFACT_COMACN','COMACN','ENVFACT','Envío a Facturar Compra de Acciones RV','Envío a Facturar',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('GCOMFACT_COMACN','COMACN','GCOMFACT','Generación comprobante Facturación Compra de Acciones RV','Generación comprobante Facturación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('FACTDTE_COMACN','COMACN','FACTDTE','Facturación DTE Compra de Acciones RV','Facturación DTE',NOW());
--
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('INGRESO_VENACN','VENACN','INGRESO','Ingreso de venta de Acciones RV','Ingreso',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('ASINACION_VENACN','VENACN','ASINACION','Asignación de venta de Acciones RV','Asignación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('ENVFACT_VENACN','VENACN','ENVFACT','Envío a Facturar venta de Acciones RV','Envío a Facturar',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('GCOMFACT_VENACN','VENACN','GCOMFACT','Generación comprobante Facturación venta de Acciones RV','Generación comprobante Facturación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('FACTDTE_VENACN','VENACN','FACTDTE','Facturación DTE venta de Acciones RV','Facturación DTE',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.EtapaOperacion"


-- TipoComportamientoCondicion
INSERT INTO "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" ("IdTipoComportamientoCondicion","Nombre")
VALUES ('ADVERTENCIA','Advertencia');
INSERT INTO "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" ("IdTipoComportamientoCondicion","Nombre")
VALUES ('RECHAZO','Rechazo');
INSERT INTO "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" ("IdTipoComportamientoCondicion","Nombre")
VALUES ('AUTORIZACION','Requiere Autorización');
-- SELECT * FROM "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion"


-- TipoCondicion
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES ('VIGDOCID','Vigencia Documento de Identidad','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES ('PEP','Persona Políticame Expuesta (PEP)','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES ('COMPLIANCE','Cliente en Lista Compliance','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES ('DOF','Requiere declaración origen de Fondos (DOF)','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES ('SALDONEG','Operar Saldo Negativo','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES ('NOMINAL','Opera Nominal','N','N','AUTORIZACION',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.TipoCondicion"


-- Condicion Operativa

-- Para: ENROCLI
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROCLI_VIGDOCID','VIGDOCID','ENROCLI',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROCLI_PEP','PEP','ENROCLI',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROCLI_COMPLIANCE','COMPLIANCE','ENROCLI',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROCLI_DOF','DOF','ENROCLI',NOW(),1);

-- Para: ENROPER
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROPER_VIGDOCID','VIGDOCID','ENROPER',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROPER_PEP','PEP','ENROPER',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROPER_DOF','DOF','ENROPER',NOW(),1);

-- Para: NUEPORTF
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('NUEPORTF_VIGDOCID','VIGDOCID','NUEPORTF',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('NUEPORTF_COMPLIANCE','COMPLIANCE','NUEPORTF',NOW(),1);

-- Para COMACN
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('COMACN_COMPLIANCE','COMPLIANCE','COMACN',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('COMACN_SALDONEG','SALDONEG','COMACN',NOW(),1);

-- Para VENACN
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('VENACN_COMPLIANCE','COMPLIANCE','VENACN',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('VENACN_NOMINAL','NOMINAL','VENACN',NOW(),1);



-- Etapa Condicion Operativa
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaCondicionOperativa" ("IdEtapaCondicionOperativa","IdEtapaOperacion","IdCondicionOperativa","IdEstado","FechaCreacion")
SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::EtapaCondicionOperativa".NEXTVAL,"IdEtapaOperacion","IdCondicionOperativa",'ACT',NOW() FROM "BSCL.Plataforma.BD::COM.CondicionOperativa" CO, "BSCL.Plataforma.BD::COM.EtapaOperacion" EO
WHERE CO."IdOperacionUsuario" = EO."IdOperacionUsuario" order by "IdEtapaOperacion","IdCondicionOperativa";



/*
-- PlantillaCondicionesOperativas
INSERT INTO "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas" ("IdPlantillaCondicionesOperativas","Descripcion","IdCategoriaInversionista","IdEmpresa")
VALUES ('PRIMERA_PLANTILLA','Esta es una plantilla de pruebas','ARRIESGADO',1);
-- SELECT * FROM "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas"


-- DetallePlantillaCondicionesOperativas
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas" ("IdDetallePlantillaCondicionesOperativas","IdPlantillaCondicionesOperativas","IdCanalIngreso","IdMoneda","IdOperacionUsuario","FechaCreacion","IdEstado")
VALUES (1,'PRIMERA_PLANTILLA','0','0',1,NOW(),'ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas" ("IdDetallePlantillaCondicionesOperativas","IdPlantillaCondicionesOperativas","IdCanalIngreso","IdMoneda","IdOperacionUsuario","FechaCreacion","IdEstado")
VALUES (2,'PRIMERA_PLANTILLA','0','0',2,NOW(),'ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas" ("IdDetallePlantillaCondicionesOperativas","IdPlantillaCondicionesOperativas","IdCanalIngreso","IdMoneda","IdOperacionUsuario","FechaCreacion","IdEstado")
VALUES (3,'PRIMERA_PLANTILLA','0','0',3,NOW(),'ACT');
-- SELECT * FROM "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas"


-- GrupoCondicionesOperativas
INSERT INTO "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" ("IdGrupoCondicionesOperativas","IdDetallePlantillaCondicionesOperativas","IdCondicionOperativa","AtributoX","AtributoY","IdComportamiento","IdEstado","FechaCreacion")
VALUES (1,1,1,'','','AUTORIZACION','ACT',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" ("IdGrupoCondicionesOperativas","IdDetallePlantillaCondicionesOperativas","IdCondicionOperativa","AtributoX","AtributoY","IdComportamiento","IdEstado","FechaCreacion")
VALUES (2,1,1,'','','AUTORIZACION','ACT',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" ("IdGrupoCondicionesOperativas","IdDetallePlantillaCondicionesOperativas","IdCondicionOperativa","AtributoX","AtributoY","IdComportamiento","IdEstado","FechaCreacion")
VALUES (3,1,1,'','','AUTORIZACION','ACT',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" ("IdGrupoCondicionesOperativas","IdDetallePlantillaCondicionesOperativas","IdCondicionOperativa","AtributoX","AtributoY","IdComportamiento","IdEstado","FechaCreacion")
VALUES (4,2,1,'','','AUTORIZACION','ACT',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" ("IdGrupoCondicionesOperativas","IdDetallePlantillaCondicionesOperativas","IdCondicionOperativa","AtributoX","AtributoY","IdComportamiento","IdEstado","FechaCreacion")
VALUES (5,2,2,'','','AUTORIZACION','ACT',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" ("IdGrupoCondicionesOperativas","IdDetallePlantillaCondicionesOperativas","IdCondicionOperativa","AtributoX","AtributoY","IdComportamiento","IdEstado","FechaCreacion")
VALUES (6,2,3,'','','AUTORIZACION','ACT',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas"

-- Etapa Grupo Condiciones Operativas
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(1,6,5,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(2,6,6,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(3,6,7,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(4,6,8,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(5,6,9,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(6,6,10,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(7,6,13,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(8,6,14,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(9,6,15,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(10,6,18,NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" ("IdEtapaGrupoCondicionesOperativas","IdGrupoCondicionesOperativas","IdEtapaCondicionOperativa","FechaCreacion")
VALUES(11,6,19,NOW());
*/
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
