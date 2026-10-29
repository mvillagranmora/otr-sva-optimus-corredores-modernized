SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/

DELETE FROM "BSCL.Normativa.BD::NOR.Producto" WHERE "IdNormativa" IN 
('ART171CartaDos','ART171CartaUno','CIR63','CIRCULARIZACION');


/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/


---PRODUCTO 'Carta 2 Articulo 171'
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INAC','RV','ACN','COMACN','ACT','C'); --COMPRA ACCIONES NACIONALES    
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INAC','RV','ACN','VENACN','ACT','V'); --VENTA ACCIONES NACIONALES       
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion") 
VALUES('ART171CartaDos','INAX','RV','ACX','COMACX','ACT','C'); --COMPRA ACCIONES EXTRANJERAS
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")     
VALUES('ART171CartaDos','INAX','RV','ACX','VENACX','ACT','V'); --VENTA ACCIONES EXTRANJERAS      
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INCI','RV','CFI','COMCFI','ACT','C'); --COMPRA CFI NACIONAL             
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INCI','RV','CFI','VENCFI','ACT','V'); --VENTA CFI NACIONAL              
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INCM','RV','CFM','COMCFM','ACT','C'); --COMPRA CFM                      
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INCM','RV','CFM','VENCFM','ACT','V'); --VENTA CFM                       
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INCX','RV','CFX','COMCFX','ACT','C'); --COMPRA CFI EXTRANJERO           
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INCX','RV','CFX','VENCFX','ACT','V'); --VENTA CFI EXTRANJERO            
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INEN','RV','ETN','COMETN','ACT','C'); --COMPRA ETF NACIONAL             
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INEN','RV','ETN','VENETN','ACT','V'); --VENTA ETF NACIONAL              
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INEX','RV','ETX','COMETX','ACT','C'); --COMPRA ETF EXTRANJERO           
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INEX','RV','ETX','VENETX','ACT','V'); --VENTA ETF EXTRANJERO            
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INIF','IF','PAG','COMIIF','ACT','C'); --COMPRA INTERMEDIACION FINANCIERA
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INIF','IF','PAG','VENIIF','ACT','V'); --VENTA INTERMEDIACION FINANCIERA 
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INMI','RV','MIL','COMMIL','ACT','C'); --COMPRA ACCIONES MILA            
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INMI','RV','MIL','VENMIL','ACT','V'); --VENTA ACCIONES MILA             
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INMO','RV','MON','COMMON','ACT','C'); --COMPRA MONETARIO                
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INMO','RV','MON','VENMON','ACT','V'); --VENTA MONETARIO                 
--MERCADO RENTA FIJA
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BBC','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BBC','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BCA','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BCA','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BCD','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BCD','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BCP','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BCP','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BCU','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BCU','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BCX','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BCX','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BEM','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BEM','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BR','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BR','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BTP','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BTP','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BTU','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BTU','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BUS','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BUS','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BVL','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','BVL','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','CER','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','CER','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','COR','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','COR','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','LTH','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','LTH','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','PAC','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','PAC','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','PCX','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','PCX','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','PRD','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','PRD','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','XER','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','XER','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','ZER','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','INRF' , 'RF','ZER','VENRF','ACT','V');
--MERCADO RENTA FIJA C/V A TERMINO 
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BBC','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BBC','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BCA','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BCA','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BCD','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BCD','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BCP','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BCP','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BCU','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BCU','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BCX','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BCX','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BEM','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BEM','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BH','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BH','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BR','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BR','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BTP','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BTP','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BTU','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BTU','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BUS','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BUS','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BVL','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','BVL','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','CER','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','CER','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','COR','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','COR','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','LTH','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','LTH','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','PAC','COMPAC','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','PAC','VENPAC','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','PCX','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','PCX','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','PRD','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','PRD','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','XER','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','XER','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','ZER','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaDos','MERF' , 'RF','ZER','VENTER','ACT','V');







---PRODUCTO 'Carta 1 Articulo 171'
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INAC','RV','ACN','COMACN','ACT','C'); --COMPRA ACCIONES NACIONALES    
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INAC','RV','ACN','VENACN','ACT','V'); --VENTA ACCIONES NACIONALES       
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion") 
VALUES('ART171CartaUno','INAX','RV','ACX','COMACX','ACT','C'); --COMPRA ACCIONES EXTRANJERAS
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")     
VALUES('ART171CartaUno','INAX','RV','ACX','VENACX','ACT','V'); --VENTA ACCIONES EXTRANJERAS      
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INCI','RV','CFI','COMCFI','ACT','C'); --COMPRA CFI NACIONAL             
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INCI','RV','CFI','VENCFI','ACT','V'); --VENTA CFI NACIONAL              
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INCM','RV','CFM','COMCFM','ACT','C'); --COMPRA CFM                      
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INCM','RV','CFM','VENCFM','ACT','V'); --VENTA CFM                       
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INCX','RV','CFX','COMCFX','ACT','C'); --COMPRA CFI EXTRANJERO           
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INCX','RV','CFX','VENCFX','ACT','V'); --VENTA CFI EXTRANJERO            
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INEN','RV','ETN','COMETN','ACT','C'); --COMPRA ETF NACIONAL             
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INEN','RV','ETN','VENETN','ACT','V'); --VENTA ETF NACIONAL              
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INEX','RV','ETX','COMETX','ACT','C'); --COMPRA ETF EXTRANJERO           
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INEX','RV','ETX','VENETX','ACT','V'); --VENTA ETF EXTRANJERO            
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INIF','IF','PAG','COMIIF','ACT','C'); --COMPRA INTERMEDIACION FINANCIERA
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INIF','IF','PAG','VENIIF','ACT','V'); --VENTA INTERMEDIACION FINANCIERA 
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INMI','RV','MIL','COMMIL','ACT','C'); --COMPRA ACCIONES MILA            
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INMI','RV','MIL','VENMIL','ACT','V'); --VENTA ACCIONES MILA             
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INMO','RV','MON','COMMON','ACT','C'); --COMPRA MONETARIO                
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INMO','RV','MON','VENMON','ACT','V'); --VENTA MONETARIO                 
--MERCADO RENTA FIJA
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BBC','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BBC','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BCA','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BCA','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BCD','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BCD','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BCP','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BCP','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BCU','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BCU','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BCX','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BCX','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BEM','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BEM','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BR','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BR','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BTP','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BTP','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BTU','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BTU','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BUS','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BUS','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BVL','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','BVL','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','CER','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','CER','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','COR','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','COR','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','LTH','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','LTH','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','PAC','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','PAC','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','PCX','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','PCX','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','PRD','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','PRD','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','XER','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','XER','VENRF','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','ZER','COMRF','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','INRF' , 'RF','ZER','VENRF','ACT','V');
--MERCADO RENTA FIJA C/V A TERMINO 
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BBC','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BBC','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BCA','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BCA','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BCD','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BCD','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BCP','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BCP','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BCU','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BCU','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BCX','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BCX','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BEM','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BEM','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BH','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BH','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BR','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BR','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BTP','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BTP','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BTU','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BTU','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BUS','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BUS','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BVL','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','BVL','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','CER','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','CER','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','COR','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','COR','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','LTH','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','LTH','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','PAC','COMPAC','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','PAC','VENPAC','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','PCX','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','PCX','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','PRD','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','PRD','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','XER','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','XER','VENTER','ACT','V');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','ZER','COMTER','ACT','C');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('ART171CartaUno','MERF' , 'RF','ZER','VENTER','ACT','V');




DELETE FROM "BSCL.Normativa.BD::NOR.Producto" WHERE "IdNormativa"='CIR63';

---PRODUCTO 'Circular 63 Transacciones'
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INAC','RV','ACN','COMACN','ACT','COMPRA'); --COMPRA ACCIONES NACIONALES    
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INAC','RV','ACN','VENACN','ACT','VENTA'); --VENTA ACCIONES NACIONALES       
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion") 
VALUES('CIR63','INAX','RV','ACX','COMACX','ACT','COMPRA'); --COMPRA ACCIONES EXTRANJERAS
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")     
VALUES('CIR63','INAX','RV','ACX','VENACX','ACT','VENTA'); --VENTA ACCIONES EXTRANJERAS      
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INCI','RV','CFI','COMCFI','ACT','COMPRA'); --COMPRA CFI NACIONAL             
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INCI','RV','CFI','VENCFI','ACT','VENTA'); --VENTA CFI NACIONAL              
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INCM','RV','CFM','COMCFM','ACT','COMPRA'); --COMPRA CFM                      
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INCM','RV','CFM','VENCFM','ACT','VENTA'); --VENTA CFM                       
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INCX','RV','CFX','COMCFX','ACT','COMPRA'); --COMPRA CFI EXTRANJERO           
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INCX','RV','CFX','VENCFX','ACT','VENTA'); --VENTA CFI EXTRANJERO            
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INEN','RV','ETN','COMETN','ACT','COMPRA'); --COMPRA ETF NACIONAL             
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INEN','RV','ETN','VENETN','ACT','VENTA'); --VENTA ETF NACIONAL              
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INEX','RV','ETX','COMETX','ACT','COMPRA'); --COMPRA ETF EXTRANJERO           
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INEX','RV','ETX','VENETX','ACT','VENTA'); --VENTA ETF EXTRANJERO            
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INIF','IF','PAG','COMIIF','ACT','COMPRA'); --COMPRA INTERMEDIACION FINANCIERA
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INIF','IF','PAG','VENIIF','ACT','VENTA'); --VENTA INTERMEDIACION FINANCIERA 
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INMI','RV','MIL','COMMIL','ACT','COMPRA'); --COMPRA ACCIONES MILA            
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INMI','RV','MIL','VENMIL','ACT','VENTA'); --VENTA ACCIONES MILA             
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INMO','RV','MON','COMMON','ACT','COMPRA'); --COMPRA MONETARIO                
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INMO','RV','MON','VENMON','ACT','VENTA'); --VENTA MONETARIO                 
--MERCADO RENTA FIJA
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BBC','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BBC','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BCA','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BCA','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BCD','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BCD','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BCP','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BCP','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BCU','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BCU','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BCX','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BCX','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BEM','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BEM','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BR','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BR','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BTP','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BTP','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BTU','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BTU','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BUS','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BUS','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BVL','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','BVL','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','CER','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','CER','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','COR','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','COR','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','LTH','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','LTH','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','PAC','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','PAC','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','PCX','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','PCX','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','PRD','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','PRD','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','XER','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','XER','VENRF','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','ZER','COMRF','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','INRF' , 'RF','ZER','VENRF','ACT','VENTA');
--MERCADO RENTA FIJA C/V A TERMINO 
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BBC','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BBC','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BCA','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BCA','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BCD','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BCD','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BCP','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BCP','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BCU','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BCU','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BCX','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BCX','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BEM','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BEM','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BH','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BH','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BR','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BR','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BTP','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BTP','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BTU','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BTU','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BUS','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BUS','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BVL','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','BVL','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','CER','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','CER','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','COR','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','COR','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','LTH','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','LTH','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','PAC','COMPAC','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','PAC','VENPAC','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','PCX','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','PCX','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','PRD','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','PRD','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','XER','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','XER','VENTER','ACT','VENTA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','ZER','COMTER','ACT','COMPRA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIR63','MERF' , 'RF','ZER','VENTER','ACT','VENTA');




DELETE FROM "BSCL.Normativa.BD::NOR.Producto" WHERE "IdNormativa"='CIRCULARIZACION';

---PRODUCTO 'Circualrizacion - Carta/Certificado Custodia'
--SIMULTANEAS
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIRCULARIZACION','INSI','RV','SIM','COMSIM','ACT',''); --COMPRA DE SIMULTÁNEA
--PACTOS
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion")
VALUES('CIRCULARIZACION','MERF','RF','PAC','VENPAC','ACT',''); --VENTA CON PACTO


DELETE FROM "BSCL.Normativa.BD::NOR.Producto" WHERE "IdNormativa"='UAF' AND "IdTipoDocumento"='UAFSOIEST';
---PRODUCTO 'SOI - Proceso Calculo Monto Promedio'
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa","IdTipoDocumento", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado")
VALUES('UAF','UAFSOIEST','INAC' , 'RV','ACN','COMACN','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa","IdTipoDocumento", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado")
VALUES('UAF','UAFSOIEST','INAX' , 'RV','ACX','COMACX','ACT');



/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
