SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM "BSCL.Plataforma.BD::MAN.GrupoMantenedor";
DELETE FROM "BSCL.Plataforma.BD::MAN.Mantenedor";
DELETE FROM "BSCL.Plataforma.BD::MAN.AtributoMantenedor"; 
DELETE FROM "BSCL.Plataforma.BD::MAN.AccionMantenedor";  

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::MAN.GrupoMantenedor"( "IdGrupoMantenedor","NombreGrupoMantenedor","IdEstado")  
VALUES('PLATAF','Plataforma','ACT');
INSERT INTO "BSCL.Plataforma.BD::MAN.GrupoMantenedor"( "IdGrupoMantenedor","NombreGrupoMantenedor","IdEstado")  
VALUES('INSTRU','Instrumento','ACT');
INSERT INTO "BSCL.Plataforma.BD::MAN.GrupoMantenedor"( "IdGrupoMantenedor","NombreGrupoMantenedor","IdEstado")  
VALUES('PLACOM','Plataforma Comercial','ACT');
INSERT INTO "BSCL.Plataforma.BD::MAN.GrupoMantenedor"( "IdGrupoMantenedor","NombreGrupoMantenedor","IdEstado")  
VALUES('BANCO','BANCO','ACT');
INSERT INTO "BSCL.Plataforma.BD::MAN.GrupoMantenedor"( "IdGrupoMantenedor","NombreGrupoMantenedor","IdEstado")  
VALUES('CONREG','Configuracion Regional','ACT');
INSERT INTO "BSCL.Plataforma.BD::MAN.GrupoMantenedor"( "IdGrupoMantenedor","NombreGrupoMantenedor","IdEstado")  
VALUES('PAGO','Pago','ACT');
------------ PROPOSITO EN PORTAFOLIO - (mmachmar - lu 22.02.2016 )------------
INSERT INTO "BSCL.Plataforma.BD::MAN.GrupoMantenedor"( "IdGrupoMantenedor","NombreGrupoMantenedor","IdEstado")  
VALUES('CLIENT','Cliente','ACT');
------------ REQUIERE AUTORIZACION - (CAPONTE -  23.03.2016 )------------
INSERT INTO "BSCL.Plataforma.BD::MAN.GrupoMantenedor"( "IdGrupoMantenedor","NombreGrupoMantenedor","IdEstado")  
VALUES('AUTORI','Autorizaciones','ACT');



INSERT INTO "BSCL.Plataforma.BD::MAN.Mantenedor" ( "IdMantenedor","IdGrupoMantenedor","CodigoMantenedor","NombreMantenedor","TablaMantenedor","IdEstado" )  
VALUES('MONEDA','PLATAF','MONEDA','Moneda','"BSCL"."BSCL.Plataforma.BD::PLA.Moneda"','ACT');
INSERT INTO "BSCL.Plataforma.BD::MAN.Mantenedor" ( "IdMantenedor","IdGrupoMantenedor","CodigoMantenedor","NombreMantenedor","TablaMantenedor","IdEstado" )  
VALUES('MODULO','PLATAF','MODULO','Modulo','"BSCL"."BSCL.Plataforma.BD::PLA.Modulo"','ACT');
INSERT INTO "BSCL.Plataforma.BD::MAN.Mantenedor" ( "IdMantenedor","IdGrupoMantenedor","CodigoMantenedor","NombreMantenedor","TablaMantenedor","IdEstado" )  
VALUES('ESTADO','PLATAF','ESTADO','Estado','"BSCL"."BSCL.Plataforma.BD::PLA.Estado"','ACT');
INSERT INTO "BSCL.Plataforma.BD::MAN.Mantenedor" ( "IdMantenedor","IdGrupoMantenedor","CodigoMantenedor","NombreMantenedor","TablaMantenedor","IdEstado" )  
VALUES('PAIS','CONREG','PAIS','Pais','"BSCL"."BSCL.Plataforma.BD::REG.Pais"','ACT');
INSERT INTO "BSCL.Plataforma.BD::MAN.Mantenedor" ( "IdMantenedor","IdGrupoMantenedor","CodigoMantenedor","NombreMantenedor","TablaMantenedor","IdEstado" )  
VALUES('CONFIG','PLATAF','CONFIG','Configuracion','"BSCL.Plataforma.BD::PLA.Configuracion"','ACT');
INSERT INTO "BSCL.Plataforma.BD::MAN.Mantenedor" ( "IdMantenedor","IdGrupoMantenedor","CodigoMantenedor","NombreMantenedor","TablaMantenedor","IdEstado" )  
VALUES('BOLSA','INSTRU','BOLSA','Bolsa','"BSCL.Plataforma.BD::INS.Bolsa"','ACT');
INSERT INTO "BSCL.Plataforma.BD::MAN.Mantenedor" ( "IdMantenedor","IdGrupoMantenedor","CodigoMantenedor","NombreMantenedor","TablaMantenedor","IdEstado" )  
VALUES('TIPINS','INSTRU','TIPINS','Tipo de Instrumento','"BSCL.Plataforma.BD::INS.TipoInstrumento"','ACT');
INSERT INTO "BSCL.Plataforma.BD::MAN.Mantenedor" ( "IdMantenedor","IdGrupoMantenedor","CodigoMantenedor","NombreMantenedor","TablaMantenedor","IdEstado" )  
VALUES('MERCAD','INSTRU','MERCAD','Mercado','"BSCL.Plataforma.BD::INS.Mercado"','ACT');
------------ PROPOSITO EN PORTAFOLIO - (mmachmar - lu 22.02.2016 )------------
INSERT INTO "BSCL.Plataforma.BD::MAN.Mantenedor" ( "IdMantenedor","IdGrupoMantenedor","CodigoMantenedor","NombreMantenedor","TablaMantenedor","IdEstado" )  
VALUES('PROPOS','CLIENT','PROPOS','Propósito','"BSCL.Portafolio.BD::POR.PropositoPortafolio"','ACT');
------------ REQUIERE AUTORIZACION - (CAPONTE -  23.03.2016 )------------
INSERT INTO "BSCL.Plataforma.BD::MAN.Mantenedor" ( "IdMantenedor","IdGrupoMantenedor","CodigoMantenedor","NombreMantenedor","TablaMantenedor","IdEstado" )  
VALUES('PORTAF','AUTORI','PORTAF','Requiere Autorización','"BSCL.Plataforma.BD::PLA.RequiereAutorizacion"','ACT');



INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")    
VALUES(1,'MONEDA','UI_ID_MONEDA','N','S','N', 1, 'bcs-input-texto', '', 'IdMoneda', 'NVARCHAR','3','','S','PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(2,'MONEDA','UI_CODIGO','S','S','N',1,'bcs-input-texto','','Codigo','NVARCHAR','40','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")    
VALUES(3,'MONEDA','UI_NOMBRE','S','S','S', 2,'bcs-input-texto','','Nombre','NVARCHAR','100','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")    
VALUES(4,'MONEDA','UI_SIMBOLO','S','S','S',5,'bcs-input-texto','','Simbolo','NVARCHAR','40','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo") 
VALUES(5,'MONEDA','UI_ID_PAIS','N','S','S',3,'bcs-combobox-base','combobox-pais','IdPais','BIGINT','','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(6,'MONEDA','UI_CODIGO_PAIS','S','N','N',3,'bcs-input-texto','','CodigoPais','NVARCHAR','40','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(7,'MONEDA','UI_NOMBRE_PAIS','S','N','N',4,'bcs-input-texto','','NombrePais','NVARCHAR','100','','N','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(8,'MONEDA','UI_ID_ESTADO','N','S','S',6,'bcs-combobox-base','combobox-estado-moneda','IdEstado','NVARCHAR','3','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(9,'MONEDA','UI_NOMBRE_ESTADO','S','N','N',6,'bcs-input-texto','','NombreEstado','NVARCHAR','100','','N','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")    
VALUES(10,'PAIS','UI_ID_PAIS','N','S','N',1,'bcs-input-texto','','IdPais','BIGINT','','','S','PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo") 
VALUES(11,'PAIS','UI_CODIGO_PAIS','S','S','N',1,'bcs-input-texto','','CodigoPais','NVARCHAR','40','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")  
VALUES(12,'PAIS','UI_NOMBRE_PAIS','S','S','S',1,'bcs-input-texto','','NombrePais','NVARCHAR','100','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")  
VALUES(13,'CONFIG','UI_ID_EMPRESA','N','S','N',1,'bcs-input-texto','','IdEmpresa','BIGINT','','','S','PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")  
VALUES(14,'CONFIG','UI_CODIGO_PARAMETRO','S','S','N',2,'bcs-input-texto','','CodigoParametro','NVARCHAR','40','','S', 'PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")  
VALUES(15,'CONFIG','UI_DESCRIPCION','S','S','S',3,'bcs-input-texto','','Descripcion','NVARCHAR','520','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(16,'CONFIG','UI_VALOR','S','S','S',4,'bcs-input-texto','','Valor','NVARCHAR','520','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(17,'ESTADO','UI_ID_ESTADO','N','S','N',1,'bcs-input-texto','','IdEstado','NVARCHAR','3','','S','PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(18,'ESTADO','UI_NOMBRE_ESTADO','S','S','S',2,'bcs-input-texto','','Nombre','NVARCHAR','100','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(19,'MODULO','UI_ID_MODULO','N','S','N',1,'bcs-input-texto','','IdModulo','NVARCHAR','3','','S','PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(20,'MODULO','UI_CODIGO','S','S','S',2,'bcs-input-texto','','Codigo','NVARCHAR','40','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(21,'MODULO','UI_NOMBRE','S','S','S',3,'bcs-input-texto','','Nombre','NVARCHAR','100','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(22,'MODULO','UI_ID_ESTADO','N','S','S',4,'bcs-combobox-base','combobox-estado-moneda','IdEstado','NVARCHAR','3','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(23,'MODULO','UI_NOMBRE_ESTADO','S','N','N',4,'bcs-input-texto','','NombreEstado','NVARCHAR','100','','N','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(24,'BOLSA','UI_ID_BOLSA','N','S','N',1,'bcs-input-texto','','IdBolsa','NVARCHAR','3','','S','PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(25,'BOLSA','UI_CODIGO','S','S','N',2,'bcs-input-texto','','Codigo','NVARCHAR','40','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(26,'BOLSA','UI_NOMBRE','S','S','S',3,'bcs-input-texto','','Nombre','NVARCHAR','100','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(27,'BOLSA','UI_ID_PAIS','S','S','S',4,'bcs-combobox-base','combobox-pais','IdPais','BIGINT','','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(28,'BOLSA','UI_CODIGO_PAIS','S','N','N',4,'bcs-input-texto','','CodigoPais','NVARCHAR','40','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(29,'BOLSA','UI_NOMBRE_PAIS','S','N','N',4,'bcs-input-texto','','NombrePais','NVARCHAR','100','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(30,'BOLSA','UI_ID_ESTADO','S','S','S',5,'bcs-combobox-base','combobox-estado-moneda','IdEstado','NVARCHAR','3','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(31,'BOLSA','UI_NOMBRE_ESTADO','S','N','N',5,'bcs-input-texto','','NombreEstado','NVARCHAR','100','','N','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(32,'MERCAD','UI_ID_MERCADO','N','S','N',1,'bcs-input-texto','','IdMercado','NVARCHAR','4','','S','PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(33,'MERCAD','UI_CODIGO','S','S','N',2,'bcs-input-texto','','Codigo','NVARCHAR','40','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(34,'MERCAD','UI_NOMBRE','S','S','S',3,'bcs-input-texto','','Nombre','NVARCHAR','100','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(35,'MERCAD','UI_DESCRIPCION','S','S','S',4,'bcs-input-texto','','Descripcion','NVARCHAR','100','','N','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(36,'MERCAD','UI_ID_ESTADO','S','S','S',5,'bcs-combobox-base','combobox-estado-moneda','IdEstado','NVARCHAR','3','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(37,'MERCAD','UI_NOMBRE_ESTADO','S','N','N',5,'bcs-input-texto','','NombreEstado','NVARCHAR','100','','N','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(38,'TIPINS','UI_ID_TIPO_INSTRUMENTO','N','S','N',1,'bcs-input-texto','','IdTipoInstrumento','NVARCHAR','3','','S','PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(39,'TIPINS','UI_CODIGO','S','S','N',2,'bcs-input-texto','','Codigo','NVARCHAR','40','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(40,'TIPINS','UI_NOMBRE','S','S','S',3,'bcs-input-texto','','Nombre','NVARCHAR','100','','N','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(41,'TIPINS','UI_DESCRIPCION','S','S','S',4,'bcs-input-texto','','Descripcion','NVARCHAR','520','','N','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(42,'TIPINS','UI_ID_ESTADO','S','S','S',5,'bcs-combobox-base','combobox-estado-moneda','IdEstado','NVARCHAR','3','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(43,'TIPINS','UI_NOMBRE_ESTADO','S','N','N',5,'bcs-input-texto','','NombreEstado','NVARCHAR','100','','N','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")  
VALUES(44,'TIPINS','UI_ID_CLASIFICACION_TIPO_INSTRUMENTO','S','S','S',6,'bcs-combobox-base','combobox-clasificacion-tipo-instrumento','IdClasificacionTipoInstrumento','NVARCHAR','3','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo","TipoCampo")
VALUES(46,'TIPINS','UI_NOMBRE_CLASIFICACION_TIPO_INSTRUMENTO','S','N','N',6,'bcs-input-texto','','NombreClasificacionTipoInstrumento','NVARCHAR','100','','N','');
------------ PROPOSITO EN PORTAFOLIO - (mmachmar - lu 22.02.2016 )------------
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")    
VALUES(47,'PROPOS','UI_PROPOSITO_PORTAFOLIO','S','S','N', 1, 'bcs-input-texto', '', 'IdPropositoPortafolio', 'NVARCHAR','40','','S','PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")    
VALUES(48,'PROPOS','UI_DESCRIPCION','S','S','S', 2, 'bcs-input-texto', '', 'Descripcion', 'NVARCHAR','100','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(49,'PROPOS','UI_ID_ESTADO','S','S','S',3,'bcs-combobox-base','combobox-estado-rol','IdEstado','NVARCHAR','3','','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(50,'PROPOS','UI_NOMBRE_ESTADO','S','N','N',3,'bcs-input-texto','','NombreEstado','NVARCHAR','100','','N','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(51,'PORTAF','UI_CONCEPTO','S','N','N',1,'bcs-input-texto','','IdConcepto','NVARCHAR','6','','S','PK');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(52,'PORTAF','UI_REQUIERE_AUTORIZACION','S','N','S',2,'bcs-input-texto','','RequiereAutorizacion','NVARCHAR','1','N','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(53,'PORTAF','UI_REQUIERE_AUTORIZACION','S','N','N',3,'bcs-input-texto','','NombreModulo','NVARCHAR','1','N','S','');
INSERT INTO "BSCL.Plataforma.BD::MAN.AtributoMantenedor"( "IdAtributoMantenedor","IdMantenedor","NombreDiccionario", "VisibleGrilla",
           "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo",
           "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")   
VALUES(54,'PORTAF','UI_REQUIERE_AUTORIZACION','S','N','N',4,'bcs-input-texto','','NombreConcepto','NVARCHAR','1','N','S','');

INSERT INTO BSCL."BSCL.Plataforma.BD::MAN.AtributoMantenedor"
("IdAtributoMantenedor", "IdMantenedor", "NombreDiccionario", "VisibleGrilla", "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo", "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")
VALUES(61, 'CTADCV', 'UI_CODIGO', 'S', 'S', 'N', 1, 'bcs-input-texto', '', 'CodigoParametro', 'NVARCHAR', '520', '', 'S', 'PK');

INSERT INTO BSCL."BSCL.Plataforma.BD::MAN.AtributoMantenedor"
("IdAtributoMantenedor", "IdMantenedor", "NombreDiccionario", "VisibleGrilla", "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo", "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")
VALUES(62, 'CTADCV', 'UI_DESCRIPCION', 'S', 'S', 'S', 2, 'bcs-input-texto', '', 'Descripcion', 'NVARCHAR', '520', '', 'S', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::MAN.AtributoMantenedor"
("IdAtributoMantenedor", "IdMantenedor", "NombreDiccionario", "VisibleGrilla", "VisibleAgregar", "VisibleModificar", "Orden", "ComponenteUI", "Formato", "NombreCampo", "TipoDatoCampo", "LargoDatoCampo", "ValorDefectoCampo", "RequeridoCampo", "TipoCampo")
VALUES(63, 'CTADCV', 'UI_VALOR', 'S', 'S', 'S', 3, 'bcs-input-texto', '', 'Valor', 'NVARCHAR', '520', '', 'S', '');



INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(1,'MONEDA','UI_AGREGAR','S',1,'AGREGAR',1);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(2,'MONEDA','UI_MODIFICAR','S',2,'MODIFICAR',2);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(3,'MONEDA','UI_ELIMINAR','S',3,'ELIMINAR',3);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(4,'MONEDA','UI_EXPORTAR_CSV','S',4,'ESPECIAL',500);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(5,'CONFIG','UI_AGREGAR','S',1,'AGREGAR',1);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(6,'CONFIG','UI_MODIFICAR','S',2,'MODIFICAR',2);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(7,'CONFIG','UI_ELIMINAR','S',3,'ELIMINAR',3);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(8,'ESTADO','UI_AGREGAR','S',1,'AGREGAR',1);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(9,'ESTADO','UI_MODIFICAR','S',2,'MODIFICAR',2);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(10,'ESTADO','UI_ELIMINAR','S',3,'ELIMINAR',3);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(11,'MODULO','UI_AGREGAR','S',1,'AGREGAR',1);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(12,'MODULO','UI_MODIFICAR','S',2,'MODIFICAR',2);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(13,'MODULO','UI_ELIMINAR','S',3,'ELIMINAR',3);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(14,'BOLSA','UI_AGREGAR','S',1,'AGREGAR',1);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(15,'BOLSA','UI_MODIFICAR','S',2,'MODIFICAR',2);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(16,'BOLSA','UI_ELIMINAR','S',3,'ELIMINAR',3);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(17,'MERCAD','UI_AGREGAR','S',1,'AGREGAR',1);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(18,'MERCAD','UI_MODIFICAR','S',2,'MODIFICAR',2);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(19,'MERCAD','UI_ELIMINAR','S',3,'ELIMINAR',3);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(20,'TIPINS','UI_AGREGAR','S',1,'AGREGAR',1);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(21,'TIPINS','UI_MODIFICAR','S',2,'MODIFICAR',2);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(22,'TIPINS','UI_ELIMINAR','S',3,'ELIMINAR',3);         
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(23,'PAIS','UI_AGREGAR','S',1,'AGREGAR',1);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(24,'PAIS','UI_MODIFICAR','S',2,'MODIFICAR',2);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(25,'PAIS','UI_ELIMINAR','S',3,'ELIMINAR',3);
------------ PROPOSITO EN PORTAFOLIO - (mmachmar - lu 22.02.2016 )------------
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(26,'PROPOS','UI_AGREGAR','S',1,'AGREGAR',1);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(27,'PROPOS','UI_MODIFICAR','S',2,'MODIFICAR',2);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(28,'PROPOS','UI_ELIMINAR','S',3,'ELIMINAR',3);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(29,'PORTAF','UI_AGREGAR','S',1,'AGREGAR',1);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(30,'PORTAF','UI_MODIFICAR','S',2,'MODIFICAR',2);
INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(31,'PORTAF','UI_ELIMINAR','S',3,'ELIMINAR',3);


INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(40,'CTADCV','UI_ELIMINAR','S',3,'ELIMINAR',3);

INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(38,'CTADCV','UI_AGREGAR','S',3,'AGREGAR',3);

INSERT INTO "BSCL.Plataforma.BD::MAN.AccionMantenedor"( "IdAccionMantenedor","IdMantenedor","NombreDiccionario","Visible", "Orden",
            "Tipo","ServicioBD")  
VALUES(39,'CTADCV','UI_MODIFICAR','S',3,'MODIFICAR',3);

                  


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
