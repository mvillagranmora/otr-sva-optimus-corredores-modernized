INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'NO_SE_INGRESARON_SOLICITUDES_RESCATE', 'No se ingresaron solicitudes de rescate');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'VALOR_CUOTA_INCORRECTO', 'Valor cuota incorrecto');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'MONTO_VALIDO', 'Monto invalido');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'FONDO_BLOQUEADO_REQ_APO_RES', 'Fondo ::IdInstrumento:: bloqueado ::DocumentoIdentidad::');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'FONDO_NO_DIFERIDO_REQ_APO_RES', 'Fondo no diferido ::DocumentoIdentidad::');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo", "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion", "TieneCarga", "TieneIngresoManual", "TieneCertificacion", "IdEstado", "GeneraOmnibus") VALUES ('RESCNN', 'RESCNN', 'RES', 'RESCFM', '', '', '', 'ACT', '');
UPDATE "BSCL.FondoMutuo.BD::FMM.Parametro" SET "ValorParametro" = 'S' WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'RespetaValutasCargaMasiva';
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "DiasRetencionesRescate" = 2, "DiasValutaValorCuotaRescate" = 1, "DiasValutaCertificacionRescate" = 3, "DiasValutaCurseRescate" = 4 WHERE "IdSerieFondo" = 'FM001A';
