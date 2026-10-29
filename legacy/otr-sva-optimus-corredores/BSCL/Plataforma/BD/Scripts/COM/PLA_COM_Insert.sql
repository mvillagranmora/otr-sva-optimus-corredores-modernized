SET SCHEMA "BSCL";




INSERT INTO BSCL."BSCL.Plataforma.BD::COM_2.DetallePerfilCobro"
("IdEmpresa", "IdPerfilCobros", "IdTipoProducto", "IdTipoOperacion", "IdBolsa", "IdTipoCanalIngreso", "IdMonedaOperacion", "IdMonedaLiquidacion", "CodigoDefinicionCobro")
VALUES(1, 'PERFIL_TEST', 'INAC', 'COMACN', 'BCS', 'AGE', 'CLP', 'CLP', 'DEFINICION1');

INSERT INTO BSCL."BSCL.Plataforma.BD::COM_2.PerfilCobros"
("IdPerfilCobros", "DescripcionPerfilCobros", "EsPerfilPersonalizado", "FechaCreacion", "FechaModificacion", "IdEstado", "PorDefecto", "IdEmpresa", "TipoEntidadReferencia", "IdReferencia", "FechaIngreso", "IdEstadoAutorizacion", "IdUsuarioCreador")
VALUES('PERFIL_TEST', 'PERFIL_TEST', 'N', CURRENT_DATE, CURRENT_DATE, 'ACT', 'N', 1, '', 1, '0001-01-01 00:00:00', '', 0);

INSERT INTO BSCL."BSCL.Plataforma.BD::COM_2.DefinicionCobro"
("IdEmpresa", "IdClaseCobro", "IdTipoCobro", "CodigoDefinicionCobro", "IdEstado", "Descripcion", "CheckCobroMonedaOperacion", "IdMonedaCobroOtraMoneda", "MontoFijo_MontoCobro", "MontoFijo_CheckMontoOperacionMenor", "MontoFijo_ValorMontoOperacionMenor", "MontoFijo_CheckSiNo", "MontoFijo_ValorMontoSiNo", "Otros_CheckOperacionContado", "Otros_CheckOperacionPlazo", "Otros_CheckBaseTasa", "Otros_ValorBaseTasa", "Otros_CheckInteresSimple", "Otros_CheckInteresCompuesto")
VALUES(1, 'COMISION', 'MONTOFIJO', 'MONTO_FIJO_TODAS_LAS_OPERACIONES', 'ACT', 'FFF', 'S', NULL, 5000, 'N', 0, 'N', 0, 'S', 'N', 'N', 0, 'N', 'N');


COMMIT;









SET SCHEMA BSCL;
DELETE FROM "BSCL.Plataforma.BD::COM_2.ClaseCobro";
INSERT INTO "BSCL.Plataforma.BD::COM_2.ClaseCobro"("IdClaseCobro","NombreClaseCobro")
SELECT 'COMISION_BVL','COMISION BVL' FROM DUMMY UNION ALL
SELECT 'COMISION_SAB','COMISION SAB' FROM DUMMY UNION ALL
SELECT 'COMISION_SMV','COMISION SMV' FROM DUMMY UNION ALL
SELECT 'COMISION_CAVALI','COMISION CAVALI' FROM DUMMY UNION ALL
SELECT 'FONDO_GARANTIA','FONDO GARANTIA' FROM DUMMY UNION ALL
SELECT 'FONDO_LIQUIDACION','FONDO LIQUIDACION' FROM DUMMY UNION ALL
SELECT 'COMISION_GENERICA','COMISION GENERICA' FROM DUMMY UNION ALL
SELECT 'IMPUESTO_IGV','IMPUESTO IGV' FROM DUMMY;
COMMIT;



DELETE FROM "BSCL.Plataforma.BD::COM_2.ClaseCobro";
INSERT INTO "BSCL.Plataforma.BD::COM_2.ClaseCobro"("IdClaseCobro","NombreClaseCobro")
SELECT 'COMISION','COMISION' FROM DUMMY UNION ALL
SELECT 'DERECHO','DERECHO' FROM DUMMY UNION ALL
SELECT 'GASTO','GASTO' FROM DUMMY UNION ALL
SELECT 'RETENCION','RETENCION' FROM DUMMY UNION ALL
SELECT 'CONTRIBUCION','CONTRIBUCION' FROM DUMMY UNION ALL
SELECT 'IMPUESTO','IMPUESTO' FROM DUMMY;


DELETE FROM "BSCL.Plataforma.BD::COM_2.TipoCobro";
INSERT INTO  "BSCL.Plataforma.BD::COM_2.TipoCobro"("IdTipoCobro","NombreTipoCobro","PermiteMontoFijo","PermitePorcentajeFijo","PermitePorcentajeRangos")
SELECT 'MONTOFIJO','MONTO FIJO','S','N','N' FROM DUMMY UNION ALL
SELECT 'PORCENTUALVARIABLE','PORCENTUAL VARIABLE','N','S','N' FROM DUMMY UNION ALL
SELECT 'PORCENTUALPORRANGOS','PORCENTUAL POR RANGOS','N','N','S' FROM DUMMY;


DELETE FROM "BSCL.Plataforma.BD::COM_2.EstadoPlataformaComercial";
INSERT INTO "BSCL.Plataforma.BD::COM_2.EstadoPlataformaComercial" ("IdEstadoPlataformaComercial","IdEstado","NombreEntidad")
SELECT 1,'INA','COBRO' FROM DUMMY UNION ALL
SELECT 2,'ACT','COBRO' FROM DUMMY;

DELETE FROM "BSCL.Plataforma.BD::COM_2.BaseTasaCobro";
INSERT INTO "BSCL.Plataforma.BD::COM_2.BaseTasaCobro" ("IdBaseTasaCobro","NombreBaseTasaCobro","ValorBaseTasaCobro")
SELECT 1,'30',30 FROM DUMMY UNION ALL
SELECT 2,'360',360 FROM DUMMY union all
SELECT 3,'365',365 FROM DUMMY;



DELETE FROM "BSCL.Plataforma.BD::COM_2.TipoRangoCobro";
INSERT INTO "BSCL.Plataforma.BD::COM_2.TipoRangoCobro" ("IdTipoRangoCobro","NombreTipoRangoCobro")
SELECT 'CANTIDAD','Por Cantidad Operacion' FROM DUMMY UNION ALL
SELECT 'MONTO','Por Monto Operacion' FROM DUMMY union all
SELECT 'PLAZO','Por Plazo Operacion' FROM DUMMY;


rollback;
commit;

