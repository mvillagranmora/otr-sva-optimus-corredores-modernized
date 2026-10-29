SET SCHEMA BSCL;

/*SERVICIOS*/
DELETE "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" IN (3195,3196,3203,3227,3228);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3195,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoCombobox' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3196,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarHorarioFondo' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3203,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieDeFondo' , 1,  'Q', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES (3227,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoRegimenFondoMutuo',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES (3228,'BSCL.FondoMutuo.BD.Servicios::ModificarRegimenFondoMutuo',0,'P','','','');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (3607,'BSCL.FondoMutuo.BD.Servicios::SimularCertificarAporteRescate',0 , 'P','Simular Certificacion con solicitud Aporte Rescate no ingresada');

/*CATEGORIA PERFIL FONDO MUTUO*/
DELETE FROM "BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo";

INSERT INTO "BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo"
("IdCategoriaPerfilFondoMutuo","IdCategoriaInversionista","NombreCategoriaPerfilFondoMutuo","IdEmpresa","Jerarquia")
VALUES('AGRESIVO','AGRESIVO','AGRESIVO',1,2);

INSERT INTO "BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo"
("IdCategoriaPerfilFondoMutuo","IdCategoriaInversionista","NombreCategoriaPerfilFondoMutuo","IdEmpresa","Jerarquia")
VALUES('BALANCEADO','BALANCEADO','BALANCEADO',1,3);

INSERT INTO "BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo"
("IdCategoriaPerfilFondoMutuo","IdCategoriaInversionista","NombreCategoriaPerfilFondoMutuo","IdEmpresa","Jerarquia")
VALUES('MODERADO','MODERADO','MODERADO',1,4);

INSERT INTO "BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo"
("IdCategoriaPerfilFondoMutuo","IdCategoriaInversionista","NombreCategoriaPerfilFondoMutuo","IdEmpresa","Jerarquia")
VALUES('CONSERVADOR','CONSERVADOR','CONSERVADOR',1,5);

/*EMISOR*/
INSERT INTO "BSCL.Plataforma.BD::MER.Emisor" 
VALUES ('CREDICORPAGF','','','SVS','N','N','N','N','S',20241,'ACT','2017-01-11 10:27:49.0','');


/*PORTAFOLIOS FFMM*/
DELETE FROM "BSCL.Portafolio.BD::POR.PropositoPortafolio" WHERE "IdPropositoPortafolio" IN ('APV','PROPOSITOAPV','57BIS','REGIMENGENERAL','FONDOACCIONARIO');

INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('PROPOSITOAPV','APV','ACT');

INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('57BIS','57BIS','ACT');

INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('REGIMENGENERAL','REGIMEN GENERAL','ACT');

INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('FONDOACCIONARIO','FONDO ACCIONARIO','ACT');


/*REGIMENES*/
DELETE FROM "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo";

INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado"	,"PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('APV-A','APV - Regimen A','APV','PROPOSITOAPV','N','S','N','S','APV-A','N','N','N','ACT');

INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('APV-B','APV - Regimen B','APV','PROPOSITOAPV','N','S','N','S','APV-B','N','N','N','ACT');

INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('APVC','APVC - Ahorro Previsional Voluntario Colectivo','APV','PROPOSITOAPV','N','S','N','S','APVC','N','N','N','ACT');

INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('CVO','CVO - Cotizacion Voluntaria','APV','PROPOSITOAPV','N','S','N','S','CVO','N','N','N','ACT');

INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('DCO','DCO - Deposito Convenido','APV','PROPOSITOAPV','N','S','N','N','DCO','N','N','N','ACT');

INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('ART108','Articulo 108 Regimen General','ART108','REGIMENGENERAL','N','S','N','S','ART108','N','S','N','ACT');

INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('ART107','Beneficios Articulo 107 (Ex18Ter)','ART107','FONDOACCIONARIO','N','S','N','S','ART107','N','N','N','ACT');

INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('57BIS','Beneficios 57 Bis','57BIS','57BIS','N','S','N','S','57BIS','N','N','N','ACT');

INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('54BIS','Beneficios 54 Bis','57BIS','57BIS','N','S','N','S','54BIS','N','N','N','ACT');

INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('NINGUNO','Sin Beneficio Tributario','TRIBUTARIOS','','N','N','N','S','ART108','N','N','N','ACT');

/*TIPO OPERACION*/
DELETE "BSCL.Plataforma.BD::INS.TipoOperacion" WHERE "IdTipoOperacion" IN ('ANUAPO','ANURES');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion"
("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion",
"AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion","EsMovimientoComprobante")
VALUES ('ANUAPO','ANUAPORFM','ANULACION APORTE FONDO MUTUO','APORTE','INFM','N','N','N','N','N','APORTE','','RESCFM','P','N','N','N','N');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion"
("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion",
"AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion","EsMovimientoComprobante")
VALUES ('ANURES','ANURESCFM','ANULACION RESCATE FONDO MUTUO','RESCATE','INFM','N','N','N','N','N','RESCATE','','APORFM','R','N','N','N','N');

/*ESTADO REGIMEN*/
DELETE FROM "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" WHERE "NombreEntidad" = 'REGIMEN';
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(28, 'ACT', 'REGIMEN');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(29, 'INA', 'REGIMEN');

/*FLUJO CAJA*/
DELETE FROM "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja" WHERE "IdUsoFlujoCaja" = 3;
DELETE FROM "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja" WHERE "IdUsoFlujoCaja" = 4;
DELETE FROM "BSCL.Plataforma.BD::PLA.UsoFlujoCaja" WHERE "IdUsoFlujoCaja" = 3;
DELETE FROM "BSCL.Plataforma.BD::PLA.UsoFlujoCaja" WHERE "IdUsoFlujoCaja" = 4;

INSERT INTO "BSCL.Plataforma.BD::PLA.UsoFlujoCaja"  ( "IdUsoFlujoCaja","Nombre","Descripcion","DesplegarEnUI","IdEmpresa","IdEstado") 
VALUES ( 3,'INGRESO_APORTE_FMM','INGRESO DE APORTES DE FONDOS MUTUOS','S',1,'ACT'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.UsoFlujoCaja"  ( "IdUsoFlujoCaja","Nombre","Descripcion","DesplegarEnUI","IdEmpresa","IdEstado") 
VALUES ( 4,'CERTIFICACION_APORTE_FMM','CERTIFICACION DE APORTES DE FONDOS MUTUOS','N',1,'ACT'); 

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES(3, 'SALDO_CUENTA_INVERSION','CUENTA_INVERSION','saldo de la cta de inv', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES(3, 'APORTES_EN_TRANSITO','FONDOS_MUTUOS','Aportes ingresados', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES(3, 'APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA','FONDOS_MUTUOS','Aportes ingresados con Instrucción Liquidada', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES(3, 'RESCATES_EN_TRANSITO','FONDOS_MUTUOS','Rescates negociados (*a fecha pago T1)', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (3, 'ORDENES_EN_TRANSITO_COMPRAS','ORDEN','Ordenes de compra', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (3, 'ASIGNACIONES_COMPRAS','ASIGNACION','Ordenes de compras asignadas', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (3, 'ASIGNACIONES_VENTAS','ASIGNACION','Ordenes de venta asignadas', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (3, 'EGRESOS_TESORERIA','TESORERIA','Egresos ingresados', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (3, 'INGRESOS_TESORERIA','TESORERIA','Ingresos ingresados', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (3, 'DIVIDENDOS_EN_DINERO','EVENTO_CAPITAL','Dividendo en dinero', '+','ACT');


INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES(4, 'SALDO_CUENTA_INVERSION','CUENTA_INVERSION','saldo de la cta de inv', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES(4, 'RESCATES_EN_TRANSITO','FONDOS_MUTUOS','Rescates negociados (*a fecha pago T1)', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (4, 'ORDENES_EN_TRANSITO_COMPRAS','ORDEN','Ordenes de compra', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (4, 'ASIGNACIONES_COMPRAS','ASIGNACION','Ordenes de compras asignadas', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (4, 'ASIGNACIONES_VENTAS','ASIGNACION','Ordenes de venta asignadas', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (4, 'EGRESOS_TESORERIA','TESORERIA','Egresos ingresados', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (4, 'INGRESOS_TESORERIA','TESORERIA','Ingresos ingresados', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (4, 'DIVIDENDOS_EN_DINERO','EVENTO_CAPITAL','Dividendo en dinero', '+','ACT');

/*TIPO MOVIMIENTO*/
DELETE FROM "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" WHERE "IdTipoOperacion" = 'RESCFM' AND "IdTipoInstrumento" = 'FFM';

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento"
("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
VALUES(350,'RESCFM','FFM','ACT',0,'');

/*ADM LIQUIDEZ*/
DELETE FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez";

INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez"("IdMoneda","MontoMinimo","IdSerieFondo","IdEmpresa")
VALUES('CLP',0,'',1);

INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez"("IdMoneda","MontoMinimo","IdSerieFondo","IdEmpresa")
VALUES('USD',0,'',1);

INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez"("IdMoneda","MontoMinimo","IdSerieFondo","IdEmpresa")
VALUES('EUR',0,'',1);

/*PARAMETROS*/
DELETE FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "IdParametro" = 'UsaDolarObservado';
INSERT INTO "BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,11,'UsaDolarObservado','S','S','VIG');

