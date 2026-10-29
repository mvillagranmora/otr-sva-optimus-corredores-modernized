-- Actualizacion ce campo para carga masiva
ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1922"
	ALTER ("IdOperacion" BIGINT NULL);

-- ::::: CARGA MASIVA
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1922' AND "Version" = 2020;
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1922' AND "Version" = 2021;

-- :::: 2020
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'ID', '', '', 1, 'Secuencia', '', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'Numero', 'C2', '', 2, 'Numero', '', 'ID', 0, '1', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'RutAdministradora', 'C3', '', 3, 'RUT DE SOC. ADMINISTRADORA', '', 'RUT', 0, '2', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'RutFondo', 'C4', '', 4, 'RUT DEL FONDO MUTUO ', '', 'RUT', 0, '3', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'RunFondo', 'C80', '', 5, 'RUN DEL FONDO MUTUO ', '', 'RUT', 0, '4', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'Fondo', '', '', 6, 'Fondo', '', 'TEXT', 100, '5', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'TipoFondo', 'C5', 'TipoFondo1922', 7, 'TIPO DE FONDO', '', 'NEMO', 100, '6', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'NombreParticipe', 'C6', '', 8, 'NOMBRE O RAZON SOCIAL DEL PARTICIPE O APORTANTE', '', 'TEXT', 250, '7', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'RutCliente', 'C7', '', 9, 'RUT DEL PARTICIPE', '', 'RUT_CLIENTE', 0, '8', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'TaxId', 'C8', '', 10, 'TAX-ID', '', 'TEXT', 250, '9', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CodigoPais', 'C9', 'CodigoPais1922', 11, 'CODIGO PAIS DE RESIDENCIA DEL PARTICIPE O APORTANTE', '', 'NEMO', 100, '10', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'TipoOperacion', 'C10', 'TipoOperacionFondos1922', 12, 'TIPO DE OPERACION', '', 'NEMO', 100, '11', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'FechaInversion', 'C11', '', 13, 'FECHA DE LA OPERACION', '', 'DATE_FILTER', 0, '12', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'MonedaReajuste', 'C12', 'TipoReajuste1922', 14, 'MONEDA O UNIDAD DE REAJUSTE', '', 'NEMO', 100, '13', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'MontoNominalAportes', '', '', 15, 'MONTO NOMINAL DE  APORTES AL FONDO EN   $', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'MontoNominalAportesActualizado', 'C13', '', 16, 'MONTO NOMINAL DE  APORTES AL FONDO EN  $ Actualizado', '', 'AMOUNT', 0, '14', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 15, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'NumeroSerieCuotas', 'C14', '', 17, 'NUMERO DE SERIE DE CUOTAS DE PROPIEDAD DEL PARTICIPE O APORTANTE', '', 'ID', 0, '15', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 16, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CantidadCuotasParticipe', 'C15', '', 18, 'NUMERO DE CUOTAS DE LA SERIE DE PROPIEDAD DEL PARTICIPE O APORTANTE', '', 'AMOUNT', 0, '16', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 17, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'ParticipeCumpleRequisitos', 'C16', 'ParticipeCumpleRequisitos1922', 19, 'PARTICIPE CUMPLE REQUISITOS (Art.82 N1  letra B) iii), Ley Adm.Fondos contenida Art. 1Ã‚ Ley 20.712)', '', 'ID', 0, '17', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 18, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CuotasCumplenRequisitos', 'C17', 'CuotasCumplenRequisitos1922', 20, 'CUOTAS DE FONDOS QUE CUMPLEN REQUISITOS Art.82', '', 'ID', 0, '18', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 19, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'ValorCuotaPesos', 'C18', '', 21, 'VALOR DE LA CUOTA ($)', '', 'AMOUNT', 0, '19', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 20, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'NumCuotasSerieParticipe', 'C64', '', 22, 'VALOR DE MERCADO AL CIERRE DEL EJERCICIO', '', 'AMOUNT', 0, '20', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 21, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'PorcentajeParticipacion31Dic', 'C19', '', 23, '% DE PARTICIPACION EN EL PATRIMONIO DEL FONDO AL 31.12', '', 'AMOUNT', 0, '21', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 22, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'ValNominalCuotasRescatadas', 'C20', '', 24, 'VALOR NOMINAL CUOTAS RESCATADAS O ENAJENADAS', '', 'AMOUNT', 0, '22', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 23, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CantidadCuotasRescatadas', 'C21', '', 25, 'CANTIDAD DE CUOTAS RESCATADAS O ENAJENADAS', '', 'AMOUNT', 0, '23', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 24, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DiferenciaPorRescateOEnajenacion', '', '', 26, 'DIFERENCIA OBTENIDA EN EL RESCATE O ENAJENACION DE CUOTAS', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 25, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DiferenciaPorRescateOEnajenacionActualizado', 'C22', '', 27, 'DIFERENCIA OBTENIDA EN EL RESCATE O ENAJENACION DE CUOTAS (ACTUALIZADA)', '', 'AMOUNT', 0, '24', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 26, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DifPorRescateOEnajenacionCFIAcogido107', '', '', 28, 'DIFERENCIA EN EL RESCATE DE CUOTAS DE FONDOS DE INVERSION ART.107', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 27, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DifPorRescateOEnajenacionCFIAcogido107Actualizado', 'C23', '', 29, 'DIFERENCIA EN EL RESCATE DE CUOTAS DE FONDOS DE INVERSION ART.107 (ACTUALIZADA)', '', 'AMOUNT', 0, '25', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 28, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'ValNominalInversionaFechaLiquidacion', 'C24', '', 30, 'VALOR NOMINAL INVERSION A LA FECHA DE LIQUIDACION O DISMINUCION DE CAPITAL', '', 'AMOUNT', 0, '26', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 29, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CantCuotasCedidasPorLiquidacion', 'C25', '', 31, 'CUOTAS ENAJENADAS LIQUIDACION DEL FONDO O RESCATADAS POR DISMINUCION DE CAPITAL', '', 'AMOUNT', 0, '27', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 30, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DiferenciaValorCuotasFechaLiquidacion', '', '', 32, 'DIFERENCIA VALOR DE CUOTAS A FECHA LIQUIDACION V/S VALOR A FECHA DE ADQUISICION', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 31, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DiferenciaValorCuotasFechaLiquidacionActualizado', 'C26', '', 33, 'DIFERENCIA VALOR DE CUOTAS A FECHA LIQUIDACION V/S VALOR A FECHA DE ADQUISICION (ACTUALIZADO)', '', 'AMOUNT', 0, '28', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 32, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'TributaSa', 'C65', '', 34, 'TRIBUTA S.A.', '', 'TEXT', 0, '29', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 33, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DivRemDistAfectGCConIDPC', 'C66', '', 35, 'DIVIDENDOS, REMESAS O DISTRIBUCIONES CON CRÃ‰DITO POR IDPC GENERADOS A CONTAR DEL 01.01.2017', '', 'AMOUNT', 0, '30', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 34, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DivRemDistAfectGCConIDPCal31122016', 'C67', '', 36, 'CON CDIVIDENDOS, REMESAS O DISTRIBUCIONES RÃ‰DITO POR IDPC ACUMULADOS HASTA EL 31.12.2016', '', 'AMOUNT', 0, '31', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 35, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DivRemDistAfectGCConIDPCVOL', 'C68', '', 37, 'DIVIDENDOS, REMESAS O DISTRIBUCIONES CON CRÃ‰DITO POR PAGO DE IDPC VOLUNTARIO', '', 'AMOUNT', 0, '32', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 36, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DivRemDistAfectGCSINDC', 'C69', '', 38, 'DIVIDENDOS, REMESAS O DISTRIBUCIONES SIN DERECHO A CRÃ‰DITO', '', 'AMOUNT', 0, '33', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 37, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DisRemRentasTribCProvRap_C96', 'C96', '', 39, 'RENTAS TRIB. CUMPLIDA RENTAS PROVENIENTES DEL REGISTRO RAP Y DIFERENCIA INICIAL DE SOCIEDAD', '', 'AMOUNT', 0, '34', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 38, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DisRemRentasTribCORP_C97', 'C97', '', 40, 'RENTAS TRIB. CUMPLIDA OTRAS RENTAS PERCIBIDAS SIN PRIORIDAD EN SU ORDEN DE IMPUTACIÃ“N', '', 'AMOUNT', 0, '35', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 39, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DisRemRentasTribCExDD_C98', 'C98', '', 41, 'RENTAS TRIB. CUMPLIDA EXCESO DISTRIBUCIONES DESPROPORCIONADAS (NÂ°9 ART.14 LETRA A)', '', 'AMOUNT', 0, '36', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 40, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DisRemRentasTribCUtAFFut_C99', 'C99', '', 42, 'RENTAS TRIB. CUMPLIDA UTILIDADES AFECTADAS CON IMPUESTO SUSTITUTIVO AL FUT', '', 'AMOUNT', 0, '37', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 41, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DivRemDistNoRentaFUT', 'C70', '', 43, 'RENTAS TRIB. CUMPLIDA RENTAS GENE. HASTA EL 31.12.1983 Y/O UTILIDADES AFECTADAS ', '', 'AMOUNT', 0, '38', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 42, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DisRemRentasExIGCAfecImptoAdi_C100', 'C100', '', 44, 'RENTAS EXENTAS DE IMPUESTO GLOBAL COMPLEMENTARIO (IGC) , AFECTAS A IMPUESTO ADICIONAL', '', 'AMOUNT', 0, '39', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 43, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosExentosIGCoAdicional', '', '', 45, 'RENTAS EXENTAS DE IMPUESTO GLOBAL COMPLEMENTARIO (IGC) Y/O IMPUESTO ADICIONAL (IA)', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 44, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosExentosIGCoAdicionalActualizado', 'C28', '', 46, 'RENTAS EXENTAS DE IMPUESTO GLOBAL COMPLEMENTARIO (IGC) Y/O IMPUESTO ADICIONAL (IA) (ACTUALIZADO)', '', 'AMOUNT', 0, '40', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 45, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosNoConstitutivosDeRenta', '', '', 47, 'DIVIDENDOS REMESAS O DISTRIBUCIONES NO CONSTITUTIVOS DE RENTA', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 46, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosNoConstitutivosDeRentaActualizado', 'C29', '', 48, 'DIVIDENDOS REMESAS O DISTRIBUCIONES NO CONSTITUTIVOS DE RENTA (ACTUALIZADO)', '', 'AMOUNT', 0, '41', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 47, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DivRemDistNoRentaFE', 'C31', '', 49, 'RENTAS O CANTIDADES DE FUENTE EXTRANJERA (', '', 'AMOUNT', 0, '42', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 48, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosAfectosImptoUnicoTasa10', '', '', 50, 'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 10%', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 49, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosAfectosImptoUnicoTasa10Actualizado', 'C30', '', 51, 'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 10% (ACTUALIZADO)', '', 'AMOUNT', 0, '43', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 50, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosAfectosImptoUnicoTasa4', '', '', 52, 'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 4%', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 51, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosAfectosImptoUnicoTasa4Actualizado', 'C32', '', 53, 'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 4% (ACTUALIZADO)', '', 'AMOUNT', 0, '44', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 52, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DistribucionXDiminucionNoImputadaCapital', 'C33', '', 54, 'DISTRIBUCIONES EFECTUADAS MEDIANTE LA DISMINUCIIN DEL VALOR DE CUOTA DEL FONDO NO IMPUTADA A CAPITAL', '', 'AMOUNT', 0, '45', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 53, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DevolucionesDeCapital', '', '', 55, 'DEVOLUCIONES DE CAPITAL', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 54, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DevolucionesDeCapitalActualizado', 'C34', '', 56, 'DEVOLUCIONES DE CAPITAL (ACTUALIZADO)', '', 'AMOUNT', 0, '46', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 55, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompNSRSD', 'C71', '', 57, 'NO SUJ. A REST. GEN. HASTA EL 31.12.2019 SIN DER. A DEV. NO SUJETOS A REST. GEN. HASTA EL 31.12.2019', '', 'AMOUNT', 0, '47', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 56, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompNSRCD', 'C72', '', 58, 'NO SUJ. A REST. GEN. HASTA EL 31.12.2019 CON DER. A DEV. NO SUJETOS A REST. GEN. HASTA EL 31.12.2019', '', 'AMOUNT', 0, '48', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 57, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredIGCAcumD01012017RANSRD01012020SDD_C101', 'C101', '', 59, 'NO SUJ. A REST. GEN. DESDE 01.01 .2020 SIN DER. A DEV. NO SUJ. A REST. GEN.  DESDE 01.01.2020', '', 'AMOUNT', 0, '49', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 58, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredIGCAcumD01012017RANSRD01012020CDD_C102', 'C102', '', 60, 'NO SUJ. A REST. GEN. DEL 01.01 .2020 CON DER. A DEV. NO SU. A RES. GEN. DESDE DEL 01.01.2020', '', 'AMOUNT', 0, '50', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 59, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompSRSD', 'C73', '', 61, 'SUJETOS A RESTITUCIÃ“N SIN DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS AFECTAS', '', 'AMOUNT', 0, '51', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 60, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompSRCD', 'C74', '', 62, 'SUJETOS A RESTITUCIÃ“N CON DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS AFECTAS', '', 'AMOUNT', 0, '52', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 61, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredIGCAcumD01012017RXSRSDD_C103', 'C103', '', 63, 'ASOCIADOS A RENTAS EXENTAS SIN DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS EXENTAS', '', 'AMOUNT', 0, '53', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 62, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredIGCAcumD01012017RXSRCDD_C104', 'C104', '', 64, 'ASOCIADOS A RENTAS EXENTAS CON DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS EXENTAS', '', 'AMOUNT', 0, '54', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 63, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompCTDCIF', 'C75', '', 65, 'CRÃ‰DITO POR IPE', '', 'AMOUNT', 0, '55', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 64, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompSDD31122016', 'C77', '', 66, 'ACUMULADOS HASTA EL 31.12.2016 SIN DERECHO A DEVOLUCIÃ“N RENTAS AFECTAS', '', 'AMOUNT', 0, '56', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 65, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompCDD31122016', 'C76', '', 67, 'ACUMULADOS HASTA EL 31.12.2016 CON DERECHO A DEVOLUCIÃ“N RENTAS AFECTAS', '', 'AMOUNT', 0, '57', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 66, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredIGCAcumH31122016RXSDD_C105', 'C105', '', 68, 'ACUMULADOS HASTA EL 31.12.2016 SIN DERECHO A DEVOLUCIÃ“N RENTAS EXENTAS', '', 'AMOUNT', 0, '58', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 67, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredIGCAcumH31122016RXCDD_C106', 'C106', '', 69, 'ACUMULADOS HASTA EL 31.12.2016 CON DERECHO A DEVOLUCIÃ“N RENTAS EXENTAS', '', 'AMOUNT', 0, '59', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 68, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompCTDCIF31122016', 'C78', '', 70, 'ACUMULADOS HASTA EL 31.12.2016 CRÃ‰DITO POR IPE', '', 'AMOUNT', 0, '60', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 69, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompCITAExArt21', 'C79', '', 71, 'CRÃ‰DITO POR IMPUESTO TASA ADICIONAL EX. ART. 21 LIR.', '', 'AMOUNT', 0, '61', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 70, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'MontoInversionenCuotasCFIAnterior93', 'C42', '', 72, 'MONTO INVERSION EN CUOTAS DE FONDOS DE INVERSION LEY NÂ° 18.815', '', 'AMOUNT', 0, '62', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 71, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'NumeroCertificado', 'C43', '', 73, 'NUMERO CERT.', '', 'ID', 0, '63', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 72, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DetBenNetoPerRutFdo_C107', 'C107', '', 74, 'RUT FONDO DE INVERSIÃ“N', '', 'RUT', 0, '64', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 73, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DetBenNetoPerPOS_C113', 'C113', '', 75, 'BENEFICIO NETO PERCIBIDO (POSITIVO)', '', 'AMOUNT', 0, '65', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 74, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DetBenNetoPerNEG_C114', 'C114', '', 76, 'BENEFICIO NETO PERCIBIDO (NEGATIVO)', '', 'AMOUNT', 0, '66', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 75, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'IdDocumento', '', '', 77, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'VboUsuario', '', '', 78, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, '0', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'Observaciones', '', '', 79, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '0', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'FuenteDatos', '', '', 80, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'IdCliente', '', '', 81, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'IdPortafolio', '', '', 82, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'IdOperacion', '', '', 83, 'Id Operacion', '', 'ID', 0, '67', 'N', 'N', 'S', 'N', 'S', 'N', 'N', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'IdEmisor', '', '', 84, 'Id Emisor', '', 'NEMO', 0, '0', 'N', 'N', 'S', 'N', 'S', 'N', 'N', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'FactorActualizacion', '', '', 85, 'Factor Actualizacion', '', 'AMOUNT', 0, '0', 'N', 'N', 'S', 'S', 'S', 'N', 'N', 'N', '7. DATOS DE CONTROL', 76, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'FechaOrigenInversion', '', '', 86, 'Fecha Origen Inversion', '', 'DATE', 0, '0', 'N', 'N', 'S', 'S', 'S', 'N', 'N', 'N', '7. DATOS DE CONTROL', 77, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'SecEvento', '', '', 87, 'SecEvento', '', 'ID', 0, '68', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '7. DATOS DE CONTROL', 78, 0);


-- ::::: 2021
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'ID', '', '', 1, 'Secuencia', '', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'Numero', 'C2', '', 2, 'Numero', '', 'ID', 0, '1', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'RutAdministradora', 'C3', '', 3, 'RUT DE SOC. ADMINISTRADORA', '', 'RUT', 0, '2', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'RutFondo', 'C4', '', 4, 'RUT DEL FONDO MUTUO ', '', 'RUT', 0, '3', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'RunFondo', 'C80', '', 5, 'RUN DEL FONDO MUTUO ', '', 'RUT', 0, '4', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'Fondo', '', '', 6, 'Fondo', '', 'TEXT', 100, '5', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'TipoFondo', 'C5', 'TipoFondo1922', 7, 'TIPO DE FONDO', '', 'NEMO', 100, '6', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'NombreParticipe', 'C6', '', 8, 'NOMBRE O RAZON SOCIAL DEL PARTICIPE O APORTANTE', '', 'TEXT', 250, '7', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'RutCliente', 'C7', '', 9, 'RUT DEL PARTICIPE', '', 'RUT_CLIENTE', 0, '8', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'TaxId', 'C8', '', 10, 'TAX-ID', '', 'TEXT', 250, '9', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CodigoPais', 'C9', 'CodigoPais1922', 11, 'CODIGO PAIS DE RESIDENCIA DEL PARTICIPE O APORTANTE', '', 'NEMO', 100, '10', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'TipoOperacion', 'C10', 'TipoOperacionFondos1922', 12, 'TIPO DE OPERACION', '', 'NEMO', 100, '11', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'FechaInversion', 'C11', '', 13, 'FECHA DE LA OPERACION', '', 'DATE_FILTER', 0, '12', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'MonedaReajuste', 'C12', 'TipoReajuste1922', 14, 'MONEDA O UNIDAD DE REAJUSTE', '', 'NEMO', 100, '13', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'MontoNominalAportes', '', '', 15, 'MONTO NOMINAL DE  APORTES AL FONDO EN   $', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'MontoNominalAportesActualizado', 'C13', '', 16, 'MONTO NOMINAL DE  APORTES AL FONDO EN  $ Actualizado', '', 'AMOUNT', 0, '14', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 15, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'NumeroSerieCuotas', 'C14', '', 17, 'NUMERO DE SERIE DE CUOTAS DE PROPIEDAD DEL PARTICIPE O APORTANTE', '', 'ID', 0, '15', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 16, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CantidadCuotasParticipe', 'C15', '', 18, 'NUMERO DE CUOTAS DE LA SERIE DE PROPIEDAD DEL PARTICIPE O APORTANTE', '', 'AMOUNT', 0, '16', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 17, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'ParticipeCumpleRequisitos', 'C16', 'ParticipeCumpleRequisitos1922', 19, 'PARTICIPE CUMPLE REQUISITOS (Art.82 N1  letra B) iii), Ley Adm.Fondos contenida Art. 1Ã‚ Ley 20.712)', '', 'ID', 0, '17', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 18, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CuotasCumplenRequisitos', 'C17', 'CuotasCumplenRequisitos1922', 20, 'CUOTAS DE FONDOS QUE CUMPLEN REQUISITOS Art.82', '', 'ID', 0, '18', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 19, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'ValorCuotaPesos', 'C18', '', 21, 'VALOR DE LA CUOTA ($)', '', 'AMOUNT', 0, '19', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 20, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'NumCuotasSerieParticipe', 'C64', '', 22, 'VALOR DE MERCADO AL CIERRE DEL EJERCICIO', '', 'AMOUNT', 0, '20', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 21, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'PorcentajeParticipacion31Dic', 'C19', '', 23, '% DE PARTICIPACION EN EL PATRIMONIO DEL FONDO AL 31.12', '', 'AMOUNT', 0, '21', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 22, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'ValNominalCuotasRescatadas', 'C20', '', 24, 'VALOR NOMINAL CUOTAS RESCATADAS O ENAJENADAS', '', 'AMOUNT', 0, '22', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 23, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CantidadCuotasRescatadas', 'C21', '', 25, 'CANTIDAD DE CUOTAS RESCATADAS O ENAJENADAS', '', 'AMOUNT', 0, '23', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 24, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DiferenciaPorRescateOEnajenacion', '', '', 26, 'DIFERENCIA OBTENIDA EN EL RESCATE O ENAJENACION DE CUOTAS', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 25, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DiferenciaPorRescateOEnajenacionActualizado', 'C22', '', 27, 'DIFERENCIA OBTENIDA EN EL RESCATE O ENAJENACION DE CUOTAS (ACTUALIZADA)', '', 'AMOUNT', 0, '24', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 26, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DifPorRescateOEnajenacionCFIAcogido107', '', '', 28, 'DIFERENCIA EN EL RESCATE DE CUOTAS DE FONDOS DE INVERSION ART.107', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 27, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DifPorRescateOEnajenacionCFIAcogido107Actualizado', 'C23', '', 29, 'DIFERENCIA EN EL RESCATE DE CUOTAS DE FONDOS DE INVERSION ART.107 (ACTUALIZADA)', '', 'AMOUNT', 0, '25', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 28, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'ValNominalInversionaFechaLiquidacion', 'C24', '', 30, 'VALOR NOMINAL INVERSION A LA FECHA DE LIQUIDACION O DISMINUCION DE CAPITAL', '', 'AMOUNT', 0, '26', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 29, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CantCuotasCedidasPorLiquidacion', 'C25', '', 31, 'CUOTAS ENAJENADAS LIQUIDACION DEL FONDO O RESCATADAS POR DISMINUCION DE CAPITAL', '', 'AMOUNT', 0, '27', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 30, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DiferenciaValorCuotasFechaLiquidacion', '', '', 32, 'DIFERENCIA VALOR DE CUOTAS A FECHA LIQUIDACION V/S VALOR A FECHA DE ADQUISICION', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 31, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DiferenciaValorCuotasFechaLiquidacionActualizado', 'C26', '', 33, 'DIFERENCIA VALOR DE CUOTAS A FECHA LIQUIDACION V/S VALOR A FECHA DE ADQUISICION (ACTUALIZADO)', '', 'AMOUNT', 0, '28', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 32, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'TributaSa', 'C65', '', 34, 'TRIBUTA S.A.', '', 'TEXT', 0, '29', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 33, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DivRemDistAfectGCConIDPC', 'C66', '', 35, 'DIVIDENDOS, REMESAS O DISTRIBUCIONES CON CRÃ‰DITO POR IDPC GENERADOS A CONTAR DEL 01.01.2017', '', 'AMOUNT', 0, '30', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 34, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DivRemDistAfectGCConIDPCal31122016', 'C67', '', 36, 'CON CDIVIDENDOS, REMESAS O DISTRIBUCIONES RÃ‰DITO POR IDPC ACUMULADOS HASTA EL 31.12.2016', '', 'AMOUNT', 0, '31', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 35, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DivRemDistAfectGCConIDPCVOL', 'C68', '', 37, 'DIVIDENDOS, REMESAS O DISTRIBUCIONES CON CRÃ‰DITO POR PAGO DE IDPC VOLUNTARIO', '', 'AMOUNT', 0, '32', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 36, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DivRemDistAfectGCSINDC', 'C69', '', 38, 'DIVIDENDOS, REMESAS O DISTRIBUCIONES SIN DERECHO A CRÃ‰DITO', '', 'AMOUNT', 0, '33', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 37, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DisRemRentasTribCProvRap_C96', 'C96', '', 39, 'RENTAS TRIB. CUMPLIDA RENTAS PROVENIENTES DEL REGISTRO RAP Y DIFERENCIA INICIAL DE SOCIEDAD', '', 'AMOUNT', 0, '34', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 38, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DisRemRentasTribCORP_C97', 'C97', '', 40, 'RENTAS TRIB. CUMPLIDA OTRAS RENTAS PERCIBIDAS SIN PRIORIDAD EN SU ORDEN DE IMPUTACIÃ“N', '', 'AMOUNT', 0, '35', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 39, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DisRemRentasTribCExDD_C98', 'C98', '', 41, 'RENTAS TRIB. CUMPLIDA EXCESO DISTRIBUCIONES DESPROPORCIONADAS (NÂ°9 ART.14 LETRA A)', '', 'AMOUNT', 0, '36', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 40, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DisRemRentasTribCUtAFFut_C99', 'C99', '', 42, 'RENTAS TRIB. CUMPLIDA UTILIDADES AFECTADAS CON IMPUESTO SUSTITUTIVO AL FUT', '', 'AMOUNT', 0, '37', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 41, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DivRemDistNoRentaFUT', 'C70', '', 43, 'RENTAS TRIB. CUMPLIDA RENTAS GENE. HASTA EL 31.12.1983 Y/O UTILIDADES AFECTADAS ', '', 'AMOUNT', 0, '38', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 42, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DisRemRentasExIGCAfecImptoAdi_C100', 'C100', '', 44, 'RENTAS EXENTAS DE IMPUESTO GLOBAL COMPLEMENTARIO (IGC) , AFECTAS A IMPUESTO ADICIONAL', '', 'AMOUNT', 0, '39', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 43, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DividendosExentosIGCoAdicional', '', '', 45, 'RENTAS EXENTAS DE IMPUESTO GLOBAL COMPLEMENTARIO (IGC) Y/O IMPUESTO ADICIONAL (IA)', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 44, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DividendosExentosIGCoAdicionalActualizado', 'C28', '', 46, 'RENTAS EXENTAS DE IMPUESTO GLOBAL COMPLEMENTARIO (IGC) Y/O IMPUESTO ADICIONAL (IA) (ACTUALIZADO)', '', 'AMOUNT', 0, '40', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 45, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DividendosNoConstitutivosDeRenta', '', '', 47, 'DIVIDENDOS REMESAS O DISTRIBUCIONES NO CONSTITUTIVOS DE RENTA', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 46, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DividendosNoConstitutivosDeRentaActualizado', 'C29', '', 48, 'DIVIDENDOS REMESAS O DISTRIBUCIONES NO CONSTITUTIVOS DE RENTA (ACTUALIZADO)', '', 'AMOUNT', 0, '41', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 47, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DivRemDistNoRentaFE', 'C31', '', 49, 'RENTAS O CANTIDADES DE FUENTE EXTRANJERA (', '', 'AMOUNT', 0, '42', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 48, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DividendosAfectosImptoUnicoTasa10', '', '', 50, 'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 10%', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 49, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DividendosAfectosImptoUnicoTasa10Actualizado', 'C30', '', 51, 'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 10% (ACTUALIZADO)', '', 'AMOUNT', 0, '43', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 50, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DividendosAfectosImptoUnicoTasa4', '', '', 52, 'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 4%', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 51, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DividendosAfectosImptoUnicoTasa4Actualizado', 'C32', '', 53, 'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 4% (ACTUALIZADO)', '', 'AMOUNT', 0, '44', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 52, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DistribucionXDiminucionNoImputadaCapital', 'C33', '', 54, 'DISTRIBUCIONES EFECTUADAS MEDIANTE LA DISMINUCIIN DEL VALOR DE CUOTA DEL FONDO NO IMPUTADA A CAPITAL', '', 'AMOUNT', 0, '45', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 53, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DevolucionesDeCapital', '', '', 55, 'DEVOLUCIONES DE CAPITAL', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 54, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DevolucionesDeCapitalActualizado', 'C34', '', 56, 'DEVOLUCIONES DE CAPITAL (ACTUALIZADO)', '', 'AMOUNT', 0, '46', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 55, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredImpGloCompNSRSD', 'C71', '', 57, 'NO SUJ. A REST. GEN. HASTA EL 31.12.2019 SIN DER. A DEV. NO SUJETOS A REST. GEN. HASTA EL 31.12.2019', '', 'AMOUNT', 0, '47', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 56, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredImpGloCompNSRCD', 'C72', '', 58, 'NO SUJ. A REST. GEN. HASTA EL 31.12.2019 CON DER. A DEV. NO SUJETOS A REST. GEN. HASTA EL 31.12.2019', '', 'AMOUNT', 0, '48', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 57, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredIGCAcumD01012017RANSRD01012020SDD_C101', 'C101', '', 59, 'NO SUJ. A REST. GEN. DESDE 01.01 .2020 SIN DER. A DEV. NO SUJ. A REST. GEN.  DESDE 01.01.2020', '', 'AMOUNT', 0, '49', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 58, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredIGCAcumD01012017RANSRD01012020CDD_C102', 'C102', '', 60, 'NO SUJ. A REST. GEN. DEL 01.01 .2020 CON DER. A DEV. NO SU. A RES. GEN. DESDE DEL 01.01.2020', '', 'AMOUNT', 0, '50', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 59, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredImpGloCompSRSD', 'C73', '', 61, 'SUJETOS A RESTITUCIÃ“N SIN DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS AFECTAS', '', 'AMOUNT', 0, '51', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 60, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredImpGloCompSRCD', 'C74', '', 62, 'SUJETOS A RESTITUCIÃ“N CON DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS AFECTAS', '', 'AMOUNT', 0, '52', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 61, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredIGCAcumD01012017RXSRSDD_C103', 'C103', '', 63, 'ASOCIADOS A RENTAS EXENTAS SIN DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS EXENTAS', '', 'AMOUNT', 0, '53', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 62, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredIGCAcumD01012017RXSRCDD_C104', 'C104', '', 64, 'ASOCIADOS A RENTAS EXENTAS CON DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS EXENTAS', '', 'AMOUNT', 0, '54', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 63, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredImpGloCompCTDCIF', 'C75', '', 65, 'CRÃ‰DITO POR IPE', '', 'AMOUNT', 0, '55', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 64, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredImpGloCompSDD31122016', 'C77', '', 66, 'ACUMULADOS HASTA EL 31.12.2016 SIN DERECHO A DEVOLUCIÃ“N RENTAS AFECTAS', '', 'AMOUNT', 0, '56', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 65, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredImpGloCompCDD31122016', 'C76', '', 67, 'ACUMULADOS HASTA EL 31.12.2016 CON DERECHO A DEVOLUCIÃ“N RENTAS AFECTAS', '', 'AMOUNT', 0, '57', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 66, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredIGCAcumH31122016RXSDD_C105', 'C105', '', 68, 'ACUMULADOS HASTA EL 31.12.2016 SIN DERECHO A DEVOLUCIÃ“N RENTAS EXENTAS', '', 'AMOUNT', 0, '58', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 67, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredIGCAcumH31122016RXCDD_C106', 'C106', '', 69, 'ACUMULADOS HASTA EL 31.12.2016 CON DERECHO A DEVOLUCIÃ“N RENTAS EXENTAS', '', 'AMOUNT', 0, '59', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 68, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredImpGloCompCTDCIF31122016', 'C78', '', 70, 'ACUMULADOS HASTA EL 31.12.2016 CRÃ‰DITO POR IPE', '', 'AMOUNT', 0, '60', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 69, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'CredImpGloCompCITAExArt21', 'C79', '', 71, 'CRÃ‰DITO POR IMPUESTO TASA ADICIONAL EX. ART. 21 LIR.', '', 'AMOUNT', 0, '61', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 70, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'MontoInversionenCuotasCFIAnterior93', 'C42', '', 72, 'MONTO INVERSION EN CUOTAS DE FONDOS DE INVERSION LEY NÂ° 18.815', '', 'AMOUNT', 0, '62', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 71, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'NumeroCertificado', 'C43', '', 73, 'NUMERO CERT.', '', 'ID', 0, '63', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 72, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DetBenNetoPerRutFdo_C107', 'C107', '', 74, 'RUT FONDO DE INVERSIÃ“N', '', 'RUT', 0, '64', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 73, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DetBenNetoPerPOS_C113', 'C113', '', 75, 'BENEFICIO NETO PERCIBIDO (POSITIVO)', '', 'AMOUNT', 0, '65', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 74, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'DetBenNetoPerNEG_C114', 'C114', '', 76, 'BENEFICIO NETO PERCIBIDO (NEGATIVO)', '', 'AMOUNT', 0, '66', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 75, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'IdDocumento', '', '', 77, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'VboUsuario', '', '', 78, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, '0', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'Observaciones', '', '', 79, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '0', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'FuenteDatos', '', '', 80, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'IdCliente', '', '', 81, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'IdPortafolio', '', '', 82, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'IdOperacion', '', '', 83, 'Id Operacion', '', 'ID', 0, '67', 'N', 'N', 'S', 'N', 'S', 'N', 'N', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'IdEmisor', '', '', 84, 'Id Emisor', '', 'NEMO', 0, '0', 'N', 'N', 'S', 'N', 'S', 'N', 'N', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'FactorActualizacion', '', '', 85, 'Factor Actualizacion', '', 'AMOUNT', 0, '0', 'N', 'N', 'S', 'S', 'S', 'N', 'N', 'N', '7. DATOS DE CONTROL', 76, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'FechaOrigenInversion', '', '', 86, 'Fecha Origen Inversion', '', 'DATE', 0, '0', 'N', 'N', 'S', 'S', 'S', 'N', 'N', 'N', '7. DATOS DE CONTROL', 77, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2021', 'SecEvento', '', '', 87, 'SecEvento', '', 'ID', 0, '68', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '7. DATOS DE CONTROL', 78, 0);
