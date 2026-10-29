SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/

DELETE FROM "BSCL.Normativa.BD::NOR.Producto"
WHERE "IdNormativa" = 'INFOTRANOFIC';


/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAC', 'RV', 'ACN', 'COMACN', 'ACT','C','AC');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAC', 'RV', 'ACN', 'INGCAN', 'ACT','C','AC');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAC', 'RV', 'ACN', 'INGDIV', 'ACT','C','AC');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAC', 'RV', 'ACN', 'INGOSA', 'ACT','C','AC');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAC', 'RV', 'ACN', 'SUSOSA', 'ACT','C','AC');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAC', 'RV', 'ACN', 'VENACN', 'ACT','V','AC');
/*
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAC', 'RV', 'EMP', 'ANTEMP', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAC', 'RV', 'EMP', 'EMPRES', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAC', 'RV', 'EMP', 'VENEMP', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAC', 'RV', 'PRT', 'ANTPRT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAC', 'RV', 'PRT', 'PRESTA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAC', 'RV', 'PRT', 'VENPRT', 'ACT');
*/
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAX', 'RV', 'ACX', 'COMACX', 'ACT','C','AC');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INAX', 'RV', 'ACX', 'VENACX', 'ACT','V','AC');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INCI', 'RV', 'CFI', 'COMCFI', 'ACT','C','FI');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INCI', 'RV', 'CFI', 'VENCFI', 'ACT','V','FI');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INCM', 'RV', 'CFM', 'COMCFM', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INCM', 'RV', 'CFM', 'VENCFM', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INCX', 'RV', 'CFX', 'COMCFX', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INCX', 'RV', 'CFX', 'VENCFX', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INEN', 'RV', 'ETN', 'COMETN', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INEN', 'RV', 'ETN', 'VENETN', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INEX', 'RV', 'ETX', 'COMETX', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INEX', 'RV', 'ETX', 'VENETX', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INIF', 'IF', 'PAG', 'COMIIF', 'ACT','C','IF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INIF', 'IF', 'PAG', 'VENIIF', 'ACT','V','IF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INMI', 'RV', 'MIL', 'COMMIL', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INMI', 'RV', 'MIL', 'VENMIL', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INMO', 'RV', 'MON', 'COMMON', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INMO', 'RV', 'MON', 'VENMON', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INOS', 'RV', 'OSA', 'COMOSA', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INOS', 'RV', 'OSA', 'VENOSA', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', '', 'FRW', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', '', 'FRW', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', '', 'PAV', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', '', 'PAV', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', '', 'PRC', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', '', 'PRC', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BBC', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BBC', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BCA', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BCA', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BCD', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BCD', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BCP', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BCP', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BCU', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BCU', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BCX', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BCX', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BEM', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BEM', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BR', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BR', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BTP', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BTP', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BTU', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BTU', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BUS', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BUS', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BVL', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'BVL', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'CER', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'CER', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'COR', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'COR', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'LTH', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'LTH', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'PAC', 'COMRF', 'ACT','C','PA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'PAC', 'VENRF', 'ACT','V','PA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'PCX', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'PCX', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'PRD', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'PRD', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'XER', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'XER', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'ZER', 'COMRF', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INRF', 'RF', 'ZER', 'VENRF', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INSI', 'RV', 'SIM', 'ANSIMC', 'ACT','VS','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INSI', 'RV', 'SIM', 'ANSIMV', 'ACT','CS','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INSI', 'RV', 'SIM', 'COMSIM', 'ACT','CS','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'INSI', 'RV', 'SIM', 'VENSIM', 'ACT','VS','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MEME', 'SP', 'SPT', 'COMSPT', 'ACT','C','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MEME', 'SP', 'SPT', 'VENSPT', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'IF', 'PAG', 'COMPAC', 'ACT','CP','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'IF', 'PAG', 'COMTER', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'IF', 'PAG', 'VENPAC', 'ACT','VP','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'IF', 'PAG', 'VENTER', 'ACT','V','OT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BBC', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BBC', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BBC', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BBC', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCA', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCA', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCA', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCA', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCD', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCD', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCD', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCD', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCP', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCP', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCP', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCP', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCU', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCU', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCU', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCU', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCX', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCX', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCX', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BCX', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BEM', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BEM', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BEM', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BEM', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BH', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BH', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BH', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BH', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BR', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BR', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BR', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BR', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BTP', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BTP', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BTP', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BTP', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BTU', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BTU', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BTU', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BTU', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BUS', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BUS', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BUS', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BUS', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BVL', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BVL', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BVL', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'BVL', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'CER', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'CER', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'CER', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'CER', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'COR', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'COR', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'COR', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'COR', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'LTH', 'COMPAC', 'ACT','CP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'LTH', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'LTH', 'VENPAC', 'ACT','VP','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'LTH', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'PAC', 'COMPAC', 'ACT','CP','PA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'PAC', 'VENPAC', 'ACT','VP','PA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'PCX', 'COMPAC', 'ACT','CP','PA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'PCX', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'PCX', 'VENPAC', 'ACT','VP','PA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'PCX', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'PRD', 'COMPAC', 'ACT','CP','PA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'PRD', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'PRD', 'VENPAC', 'ACT','VP','PA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'PRD', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'XER', 'COMPAC', 'ACT','CP','PA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'XER', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'XER', 'VENPAC', 'ACT','VP','PA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'XER', 'VENTER', 'ACT','V','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'ZER', 'COMPAC', 'ACT','CP','PA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'ZER', 'COMTER', 'ACT','C','RF');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'ZER', 'VENPAC', 'ACT','VP','PA');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado","HIdTipoOperacion", "HIdTipoInstrumento") 
VALUES ('INFOTRANOFIC', 'MERF', 'RF', 'ZER', 'VENTER', 'ACT','V','RF');
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
