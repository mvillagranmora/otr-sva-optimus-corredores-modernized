SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/

DELETE FROM "BSCL.Normativa.BD::NOR.Producto"
WHERE "IdNormativa" = 'CIR1064';


/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAC', 'RV', 'ACN', 'COMACN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAC', 'RV', 'ACN', 'INGCAN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAC', 'RV', 'ACN', 'INGDIV', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAC', 'RV', 'ACN', 'INGOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAC', 'RV', 'ACN', 'SUSOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAC', 'RV', 'ACN', 'VENACN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAC', 'RV', 'EMP', 'ANTEMP', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAC', 'RV', 'EMP', 'EMPRES', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAC', 'RV', 'EMP', 'VENEMP', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAC', 'RV', 'PRT', 'ANTPRT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAC', 'RV', 'PRT', 'PRESTA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAC', 'RV', 'PRT', 'VENPRT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAX', 'RV', 'ACX', 'COMACX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INAX', 'RV', 'ACX', 'VENACX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INCI', 'RV', 'CFI', 'COMCFI', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INCI', 'RV', 'CFI', 'VENCFI', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INCM', 'RV', 'CFM', 'COMCFM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INCM', 'RV', 'CFM', 'VENCFM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INCX', 'RV', 'CFX', 'COMCFX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INCX', 'RV', 'CFX', 'VENCFX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INEN', 'RV', 'ETN', 'COMETN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INEN', 'RV', 'ETN', 'VENETN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INEX', 'RV', 'ETX', 'COMETX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INEX', 'RV', 'ETX', 'VENETX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INIF', 'IF', 'PAG', 'COMIIF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INIF', 'IF', 'PAG', 'VENIIF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INMI', 'RV', 'MIL', 'COMMIL', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INMI', 'RV', 'MIL', 'VENMIL', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INMO', 'RV', 'MON', 'COMMON', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INMO', 'RV', 'MON', 'VENMON', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INOS', 'RV', 'OSA', 'COMOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INOS', 'RV', 'OSA', 'VENOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', '', 'FRW', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', '', 'FRW', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', '', 'PAV', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', '', 'PAV', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', '', 'PRC', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', '', 'PRC', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BBC', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BBC', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BCA', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BCA', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BCD', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BCD', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BCP', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BCP', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BCU', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BCU', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BCX', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BCX', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BEM', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BEM', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BR', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BR', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BTP', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BTP', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BTU', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BTU', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BUS', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BUS', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BVL', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'BVL', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'CER', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'CER', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'COR', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'COR', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'LTH', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'LTH', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'PAC', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'PAC', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'PCX', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'PCX', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'PRD', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'PRD', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'XER', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'XER', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'ZER', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INRF', 'RF', 'ZER', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INSI', 'RV', 'SIM', 'ANSIMC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INSI', 'RV', 'SIM', 'ANSIMV', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INSI', 'RV', 'SIM', 'COMSIM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'INSI', 'RV', 'SIM', 'VENSIM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MEME', 'SP', 'SPT', 'COMSPT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MEME', 'SP', 'SPT', 'VENSPT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'IF', 'PAG', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'IF', 'PAG', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'IF', 'PAG', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'IF', 'PAG', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BBC', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BBC', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BBC', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BBC', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCA', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCA', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCA', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCA', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCD', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCD', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCD', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCD', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCP', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCP', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCP', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCP', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCU', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCU', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCU', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCU', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCX', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCX', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCX', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BCX', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BEM', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BEM', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BEM', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BEM', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BH', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BH', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BH', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BH', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BR', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BR', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BR', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BR', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BTP', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BTP', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BTP', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BTP', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BTU', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BTU', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BTU', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BTU', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BUS', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BUS', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BUS', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BUS', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BVL', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BVL', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BVL', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'BVL', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'CER', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'CER', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'CER', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'CER', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'COR', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'COR', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'COR', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'COR', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'LTH', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'LTH', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'LTH', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'LTH', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'PAC', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'PAC', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'PCX', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'PCX', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'PCX', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'PCX', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'PRD', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'PRD', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'PRD', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'PRD', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'XER', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'XER', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'XER', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'XER', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'ZER', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'ZER', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'ZER', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('CIR1064', 'MERF', 'RF', 'ZER', 'VENTER', 'ACT');
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
