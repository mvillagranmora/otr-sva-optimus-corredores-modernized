SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/

DELETE FROM "BSCL.Normativa.BD::NOR.Producto"
WHERE "IdNormativa" = 'NCG380Ope';


/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAC', 'RV', 'ACN', 'COMACN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAC', 'RV', 'ACN', 'INGCAN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAC', 'RV', 'ACN', 'INGDIV', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAC', 'RV', 'ACN', 'INGOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAC', 'RV', 'ACN', 'SUSOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAC', 'RV', 'ACN', 'VENACN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAC', 'RV', 'EMP', 'ANTEMP', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAC', 'RV', 'EMP', 'EMPRES', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAC', 'RV', 'EMP', 'VENEMP', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAC', 'RV', 'PRT', 'ANTPRT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAC', 'RV', 'PRT', 'PRESTA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAC', 'RV', 'PRT', 'VENPRT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAX', 'RV', 'ACX', 'COMACX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INAX', 'RV', 'ACX', 'VENACX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INCI', 'RV', 'CFI', 'COMCFI', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INCI', 'RV', 'CFI', 'VENCFI', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INCM', 'RV', 'CFM', 'COMCFM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INCM', 'RV', 'CFM', 'VENCFM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INCX', 'RV', 'CFX', 'COMCFX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INCX', 'RV', 'CFX', 'VENCFX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INEN', 'RV', 'ETN', 'COMETN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INEN', 'RV', 'ETN', 'VENETN', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INEX', 'RV', 'ETX', 'COMETX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INEX', 'RV', 'ETX', 'VENETX', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INFU', 'DV', 'FBC', 'COMFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INFU', 'DV', 'FBC', 'VENFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INFU', 'DV', 'FDO', 'COMFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INFU', 'DV', 'FDO', 'VENFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INFU', 'DV', 'FIC', 'COMFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INFU', 'DV', 'FIC', 'VENFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INFU', 'DV', 'FIP', 'COMFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INFU', 'DV', 'FIP', 'VENFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INFU', 'DV', 'FUF', 'COMFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INFU', 'DV', 'FUF', 'VENFUT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INIF', 'IF', 'PAG', 'COMIIF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INIF', 'IF', 'PAG', 'VENIIF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INMI', 'RV', 'MIL', 'COMMIL', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INMI', 'RV', 'MIL', 'VENMIL', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INMO', 'RV', 'MON', 'COMMON', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INMO', 'RV', 'MON', 'VENMON', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INOS', 'RV', 'OSA', 'COMOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INOS', 'RV', 'OSA', 'VENOSA', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', '', 'FRW', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', '', 'FRW', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', '', 'PAV', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', '', 'PAV', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', '', 'PRC', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', '', 'PRC', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BBC', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BBC', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BCA', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BCA', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BCD', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BCD', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BCP', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BCP', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BCU', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BCU', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BCX', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BCX', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BEM', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BEM', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BR', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BR', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BTP', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BTP', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BTU', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BTU', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BUS', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BUS', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BVL', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'BVL', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'CER', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'CER', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'COR', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'COR', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'LTH', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'LTH', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'PAC', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'PAC', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'PCX', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'PCX', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'PRD', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'PRD', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'XER', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'XER', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'ZER', 'COMRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INRF', 'RF', 'ZER', 'VENRF', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INSI', 'RV', 'SIM', 'ANSIMC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INSI', 'RV', 'SIM', 'ANSIMV', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INSI', 'RV', 'SIM', 'COMSIM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'INSI', 'RV', 'SIM', 'VENSIM', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MEFW', 'DV', 'FWD', 'COMFWD', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MEFW', 'DV', 'FWD', 'VENFWD', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MEME', 'SP', 'SPT', 'COMSPT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MEME', 'SP', 'SPT', 'VENSPT', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'IF', 'PAG', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'IF', 'PAG', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'IF', 'PAG', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'IF', 'PAG', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BBC', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BBC', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BBC', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BBC', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCA', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCA', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCA', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCA', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCD', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCD', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCD', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCD', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCP', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCP', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCP', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCP', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCU', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCU', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCU', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCU', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCX', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCX', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCX', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BCX', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BEM', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BEM', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BEM', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BEM', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BH', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BH', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BH', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BH', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BR', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BR', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BR', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BR', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BTP', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BTP', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BTP', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BTP', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BTU', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BTU', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BTU', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BTU', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BUS', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BUS', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BUS', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BUS', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BVL', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BVL', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BVL', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'BVL', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'CER', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'CER', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'CER', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'CER', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'COR', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'COR', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'COR', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'COR', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'LTH', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'LTH', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'LTH', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'LTH', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'PAC', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'PAC', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'PCX', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'PCX', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'PCX', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'PCX', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'PRD', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'PRD', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'PRD', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'PRD', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'XER', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'XER', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'XER', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'XER', 'VENTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'ZER', 'COMPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'ZER', 'COMTER', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'ZER', 'VENPAC', 'ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.Producto"("IdNormativa", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion",  "IdEstado") 
VALUES ('NCG380Ope', 'MERF', 'RF', 'ZER', 'VENTER', 'ACT');

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
