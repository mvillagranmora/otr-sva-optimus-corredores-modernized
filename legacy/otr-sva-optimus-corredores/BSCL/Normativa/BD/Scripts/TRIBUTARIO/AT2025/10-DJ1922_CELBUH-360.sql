SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='NombreParticipe';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='MontoNominalAportes';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='MontoNominalAportesActualizado';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='NumCuotasSerieParticipe';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='TaxId';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='ValorCuotaPesos';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='ValNominalCuotasRescatadas';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='NumeroSerieCuotas';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='NumeroCertificado';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='MontoInversionenCuotasCFIAnterior93';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='ValNominalInversionaFechaLiquidacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='Fondo';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='DATE_FILTER', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='FechaInversion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='PorcentajeParticipacion31Dic';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CantidadCuotasParticipe';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CantidadCuotasRescatadas';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='NumSerieCuotasParticipe_C138';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DifPorRescateOEnajenacionCFIAcogido107';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DiferenciaPorRescateOEnajenacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DiferenciaPorRescateOEnajenacionActualizado';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DisRemRentasTribCProvRap_C96';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DifPorRescateOEnajenacionCFIAcogido107Actualizado';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DisRemRentasTribCORP_C97';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DisRemRentasTribCExDD_C98';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DisRemRentasTribCUtAFFut_C99';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DisRemRentasExIGCAfecImptoAdi_C100';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DividendosAfectosImptoUnicoTasa10';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DividendosAfectosImptoUnicoTasa10Actualizado';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DividendosAfectosImptoUnicoTasa4';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DividendosAfectosImptoUnicoTasa4Actualizado';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DistribucionXDiminucionNoImputadaCapital';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DivRemDistAfectGCSINDC';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CantCuotasCedidasPorLiquidacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DiferenciaValorCuotasFechaLiquidacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DiferenciaValorCuotasFechaLiquidacionActualizado';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DivRemDistAfectGCConIDPC';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DivRemDistAfectGCConIDPCal31122016';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DivRemDistAfectGCConIDPCVOL';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DivRemDistNoRentaFUT';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DividendosExentosIGCoAdicional';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DividendosExentosIGCoAdicionalActualizado';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DividendosNoConstitutivosDeRenta';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DividendosNoConstitutivosDeRentaActualizado';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DivRemDistNoRentaFE';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DevolucionesDeCapital';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DevolucionesDeCapitalActualizado';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredImpGloCompNSRSD';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredImpGloCompNSRCD';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredIGCAcumD01012017RANSRD01012020SDD_C101';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredIGCAcumD01012017RANSRD01012020CDD_C102';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredImpGloCompSRSD';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredImpGloCompSRCD';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredIGCAcumD01012017RXSRSDD_C103';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredIGCAcumD01012017RXSRCDD_C104';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredImpGloCompCTDCIF';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredImpGloCompSDD31122016';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredImpGloCompCDD31122016';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredIGCAcumH31122016RXSDD_C105';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredIGCAcumH31122016RXCDD_C106';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredImpGloCompCTDCIF31122016';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CredImpGloCompCITAExArt21';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='RUT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DetBenNetoPerRutFdo_C107';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DetBenNetoPerPOS_C113';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DetBenNetoPerNEG_C114';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DetBenNetoDivPer_C135';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DetBenNetoGanCapPer_C136';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DetBenNetoPerGastDev_C137';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DetBenNetoIntPer_C134';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='DetBenNetoUtilPer_C133';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='TipoOperacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='TributaSa';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='ParticipeCumpleRequisitos';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CuotasCumplenRequisitos';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='RUT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='RutAdministradora';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='RUT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='RutFondo';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='RUT', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='RunFondo';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='RUT_CLIENTE', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='RutCliente';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='TipoFondo';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='MonedaReajuste';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='NEMO', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='CodigoPais';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID_PORTAFOLIO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='7. DATOS DE CONTROL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='IdPortafolio';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID_CLIENTE', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='7. DATOS DE CONTROL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='IdCliente';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='OrigenCalificacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='DATE', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='FechaOrigenInversion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='FactorActualizacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='7. DATOS DE CONTROL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='FuenteDatos';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='7. DATOS DE CONTROL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='VboUsuario';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='7. DATOS DE CONTROL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='IdDocumento';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='7. DATOS DE CONTROL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='Observaciones';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='7. DATOS DE CONTROL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='IdEmisor';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='7. DATOS DE CONTROL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='SecEvento';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='7. DATOS DE CONTROL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='AcogidoISFUT';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='7. DATOS DE CONTROL'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='IdOperacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='1. DATOS DE LOS INFORMADOS'
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='Numero';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"=''
WHERE "Esquema"='DJ1922' AND "Version"='2024' AND "Nombre"='ID';


COMMIT;