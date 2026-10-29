SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/

DELETE FROM "BSCL.Normativa.BD::NOR.Producto"
WHERE "IdNormativa" = 'SEIL';


/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAC', 'RV', 'ACN', 'COMACN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAC', 'RV', 'ACN', 'INGCAN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAC', 'RV', 'ACN', 'INGDIV', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAC', 'RV', 'ACN', 'INGOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAC', 'RV', 'ACN', 'SUSOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAC', 'RV', 'ACN', 'VENACN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAC', 'RV', 'EMP', 'ANTEMP', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAC', 'RV', 'EMP', 'EMPRES', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAC', 'RV', 'EMP', 'VENEMP', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAC', 'RV', 'PRT', 'ANTPRT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAC', 'RV', 'PRT', 'PRESTA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAC', 'RV', 'PRT', 'VENPRT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAX', 'RV', 'ACX', 'COMACX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INAX', 'RV', 'ACX', 'VENACX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INCI', 'RV', 'CFI', 'COMCFI', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INCI', 'RV', 'CFI', 'VENCFI', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INCM', 'RV', 'CFM', 'COMCFM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INCM', 'RV', 'CFM', 'VENCFM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INCX', 'RV', 'CFX', 'COMCFX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INCX', 'RV', 'CFX', 'VENCFX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INEN', 'RV', 'ETN', 'COMETN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INEN', 'RV', 'ETN', 'VENETN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INEX', 'RV', 'ETX', 'COMETX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INEX', 'RV', 'ETX', 'VENETX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INIF', 'IF', 'PAG', 'COMIIF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INIF', 'IF', 'PAG', 'VENIIF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INMI', 'RV', 'MIL', 'COMMIL', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INMI', 'RV', 'MIL', 'VENMIL', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INMO', 'RV', 'MON', 'COMMON', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INMO', 'RV', 'MON', 'VENMON', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INOS', 'RV', 'OSA', 'COMOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INOS', 'RV', 'OSA', 'VENOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', '', 'FRW', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', '', 'FRW', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', '', 'PAV', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', '', 'PAV', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', '', 'PRC', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', '', 'PRC', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BBC', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BBC', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BCA', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BCA', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BCD', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BCD', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BCP', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BCP', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BCU', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BCU', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BCX', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BCX', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BEM', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BEM', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BR', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BR', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BTP', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BTP', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BTU', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BTU', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BUS', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BUS', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BVL', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'BVL', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'CER', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'CER', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'COR', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'COR', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'LTH', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'LTH', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'PAC', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'PAC', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'PCX', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'PCX', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'PRD', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'PRD', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'XER', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'XER', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'ZER', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INRF', 'RF', 'ZER', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INSI', 'RV', 'SIM', 'ANSIMC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INSI', 'RV', 'SIM', 'ANSIMV', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INSI', 'RV', 'SIM', 'COMSIM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'INSI', 'RV', 'SIM', 'VENSIM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MEME', 'SP', 'SPT', 'COMSPT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MEME', 'SP', 'SPT', 'VENSPT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'IF', 'PAG', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'IF', 'PAG', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'IF', 'PAG', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'IF', 'PAG', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BBC', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BBC', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BBC', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BBC', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCA', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCA', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCA', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCA', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCD', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCD', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCD', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCD', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCP', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCP', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCP', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCP', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCU', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCU', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCU', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCU', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCX', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCX', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCX', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BCX', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BEM', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BEM', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BEM', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BEM', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BH', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BH', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BH', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BH', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BR', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BR', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BR', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BR', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BTP', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BTP', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BTP', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BTP', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BTU', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BTU', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BTU', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BTU', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BUS', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BUS', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BUS', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BUS', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BVL', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BVL', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BVL', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'BVL', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'CER', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'CER', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'CER', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'CER', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'COR', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'COR', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'COR', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'COR', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'LTH', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'LTH', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'LTH', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'LTH', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'PAC', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'PAC', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'PCX', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'PCX', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'PCX', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'PCX', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'PRD', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'PRD', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'PRD', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'PRD', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'XER', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'XER', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'XER', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'XER', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'ZER', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'ZER', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'ZER', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('SEIL', 'MERF', 'RF', 'ZER', 'VENTER', 'ACT');
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
