SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/

DELETE FROM "BSCL.Normativa.BD::NOR.Producto"
WHERE "IdNormativa" = 'ANEXO';


/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAC', 'RV', 'ACN', 'COMACN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAC', 'RV', 'ACN', 'INGCAN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAC', 'RV', 'ACN', 'INGDIV', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAC', 'RV', 'ACN', 'INGOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAC', 'RV', 'ACN', 'SUSOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAC', 'RV', 'ACN', 'VENACN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAC', 'RV', 'EMP', 'ANTEMP', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAC', 'RV', 'EMP', 'EMPRES', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAC', 'RV', 'EMP', 'VENEMP', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAC', 'RV', 'PRT', 'ANTPRT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAC', 'RV', 'PRT', 'PRESTA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAC', 'RV', 'PRT', 'VENPRT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAX', 'RV', 'ACX', 'COMACX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INAX', 'RV', 'ACX', 'VENACX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INCI', 'RV', 'CFI', 'COMCFI', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INCI', 'RV', 'CFI', 'VENCFI', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INCM', 'RV', 'CFM', 'COMCFM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INCM', 'RV', 'CFM', 'VENCFM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INCX', 'RV', 'CFX', 'COMCFX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INCX', 'RV', 'CFX', 'VENCFX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INEN', 'RV', 'ETN', 'COMETN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INEN', 'RV', 'ETN', 'VENETN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INEX', 'RV', 'ETX', 'COMETX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INEX', 'RV', 'ETX', 'VENETX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INFU', 'DV', 'FBC', 'COMFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INFU', 'DV', 'FBC', 'VENFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INFU', 'DV', 'FDO', 'COMFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INFU', 'DV', 'FDO', 'VENFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INFU', 'DV', 'FIC', 'COMFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INFU', 'DV', 'FIC', 'VENFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INFU', 'DV', 'FIP', 'COMFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INFU', 'DV', 'FIP', 'VENFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INFU', 'DV', 'FUF', 'COMFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INFU', 'DV', 'FUF', 'VENFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INIF', 'IF', 'PAG', 'COMIIF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INIF', 'IF', 'PAG', 'VENIIF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INMI', 'RV', 'MIL', 'COMMIL', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INMI', 'RV', 'MIL', 'VENMIL', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INMO', 'RV', 'MON', 'COMMON', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INMO', 'RV', 'MON', 'VENMON', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INOS', 'RV', 'OSA', 'COMOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INOS', 'RV', 'OSA', 'VENOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', '', 'FRW', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', '', 'FRW', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', '', 'PAV', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', '', 'PAV', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', '', 'PRC', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', '', 'PRC', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BBC', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BBC', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BCA', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BCA', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BCD', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BCD', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BCP', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BCP', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BCU', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BCU', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BCX', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BCX', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BEM', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BEM', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BR', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BR', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BTP', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BTP', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BTU', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BTU', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BUS', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BUS', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BVL', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'BVL', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'CER', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'CER', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'COR', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'COR', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'LTH', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'LTH', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'PAC', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'PAC', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'PCX', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'PCX', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'PRD', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'PRD', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'XER', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'XER', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'ZER', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INRF', 'RF', 'ZER', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INSI', 'RV', 'SIM', 'ANSIMC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INSI', 'RV', 'SIM', 'ANSIMV', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INSI', 'RV', 'SIM', 'COMSIM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'INSI', 'RV', 'SIM', 'VENSIM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MEFW', 'DV', 'FWD', 'COMFWD', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MEFW', 'DV', 'FWD', 'VENFWD', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MEME', 'SP', 'SPT', 'COMSPT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MEME', 'SP', 'SPT', 'VENSPT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'IF', 'PAG', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'IF', 'PAG', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'IF', 'PAG', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'IF', 'PAG', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BBC', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BBC', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BBC', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BBC', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCA', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCA', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCA', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCA', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCD', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCD', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCD', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCD', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCP', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCP', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCP', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCP', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCU', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCU', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCU', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCU', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCX', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCX', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCX', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BCX', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BEM', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BEM', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BEM', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BEM', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BH', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BH', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BH', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BH', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BR', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BR', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BR', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BR', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BTP', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BTP', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BTP', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BTP', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BTU', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BTU', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BTU', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BTU', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BUS', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BUS', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BUS', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BUS', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BVL', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BVL', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BVL', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'BVL', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'CER', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'CER', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'CER', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'CER', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'COR', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'COR', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'COR', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'COR', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'LTH', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'LTH', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'LTH', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'LTH', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'PAC', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'PAC', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'PCX', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'PCX', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'PCX', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'PCX', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'PRD', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'PRD', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'PRD', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'PRD', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'XER', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'XER', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'XER', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'XER', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'ZER', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'ZER', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'ZER', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('ANEXO', 'MERF', 'RF', 'ZER', 'VENTER', 'ACT');
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
